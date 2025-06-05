package com.javaninja.controller;

import com.javaninja.model.dto.S3ObjectRequest;
import com.javaninja.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for S3Controller using MockMvc.
 * Tests AWS S3 integration endpoints.
 */
@WebMvcTest(S3Controller.class)
class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    private S3ObjectRequest validRequest;
    private String testBucketName;
    private String testObjectKey;
    private String expectedETag;

    @BeforeEach
    void setUp() {
        testBucketName = "test-bucket";
        testObjectKey = "test-object.txt";
        expectedETag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
        
        validRequest = new S3ObjectRequest();
        validRequest.setBucketName(testBucketName);
        validRequest.setObjectKey(testObjectKey);
        validRequest.setContent("Hello S3!".getBytes());
        validRequest.setContentType("text/plain");
    }

    @Test
    void uploadObject_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        when(s3Service.putObject(any(S3ObjectRequest.class))).thenReturn(expectedETag);

        // When & Then
        mockMvc.perform(post("/api/s3/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.eTag").value(expectedETag))
                .andExpect(jsonPath("$.bucketName").value(testBucketName))
                .andExpect(jsonPath("$.objectKey").value(testObjectKey))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(s3Service, times(1)).putObject(any(S3ObjectRequest.class));
    }

    @Test
    void uploadObject_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - request with missing required fields
        S3ObjectRequest invalidRequest = new S3ObjectRequest();
        invalidRequest.setBucketName(""); // Empty bucket name
        invalidRequest.setObjectKey(""); // Empty object key

        // When & Then
        mockMvc.perform(post("/api/s3/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).putObject(any(S3ObjectRequest.class));
    }

    @Test
    void uploadObject_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(s3Service.putObject(any(S3ObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 service error"));

        // When & Then
        mockMvc.perform(post("/api/s3/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to upload object to S3"))
                .andExpect(jsonPath("$.error").value("S3 service error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void uploadFile_WithValidFile_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.txt", 
                "text/plain", 
                "Hello S3 File!".getBytes()
        );
        when(s3Service.putObject(any(S3ObjectRequest.class))).thenReturn(expectedETag);

        // When & Then
        mockMvc.perform(multipart("/api/s3/upload-file")
                .file(file)
                .param("bucketName", testBucketName)
                .param("objectKey", testObjectKey))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.eTag").value(expectedETag))
                .andExpect(jsonPath("$.bucketName").value(testBucketName))
                .andExpect(jsonPath("$.objectKey").value(testObjectKey))
                .andExpect(jsonPath("$.fileSize").value(14))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(s3Service, times(1)).putObject(any(S3ObjectRequest.class));
    }

    @Test
    void uploadFile_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.txt", 
                "text/plain", 
                "Hello S3 File!".getBytes()
        );
        when(s3Service.putObject(any(S3ObjectRequest.class)))
                .thenThrow(new RuntimeException("File upload failed"));

        // When & Then
        mockMvc.perform(multipart("/api/s3/upload-file")
                .file(file)
                .param("bucketName", testBucketName)
                .param("objectKey", testObjectKey))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to upload file to S3"))
                .andExpect(jsonPath("$.error").value("File upload failed"));
    }

    @Test
    void downloadObject_WithValidParameters_ShouldReturnFile() throws Exception {
        // Given
        byte[] fileContent = "Hello S3 Download!".getBytes();
        when(s3Service.getObject(eq(testBucketName), eq(testObjectKey))).thenReturn(fileContent);

        // When & Then
        mockMvc.perform(get("/api/s3/download/{bucketName}/{objectKey}", testBucketName, testObjectKey))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + testObjectKey + "\""))
                .andExpect(content().bytes(fileContent));

        verify(s3Service, times(1)).getObject(testBucketName, testObjectKey);
    }

    @Test
    void downloadObject_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(s3Service.getObject(eq(testBucketName), eq(testObjectKey)))
                .thenThrow(new RuntimeException("Object not found"));

        // When & Then
        mockMvc.perform(get("/api/s3/download/{bucketName}/{objectKey}", testBucketName, testObjectKey))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteObject_WithValidParameters_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(s3Service).deleteObject(eq(testBucketName), eq(testObjectKey));

        // When & Then
        mockMvc.perform(delete("/api/s3/{bucketName}/{objectKey}", testBucketName, testObjectKey))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Object deleted successfully"))
                .andExpect(jsonPath("$.bucketName").value(testBucketName))
                .andExpect(jsonPath("$.objectKey").value(testObjectKey))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(s3Service, times(1)).deleteObject(testBucketName, testObjectKey);
    }

    @Test
    void deleteObject_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        doThrow(new RuntimeException("Delete failed")).when(s3Service).deleteObject(eq(testBucketName), eq(testObjectKey));

        // When & Then
        mockMvc.perform(delete("/api/s3/{bucketName}/{objectKey}", testBucketName, testObjectKey))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to delete object from S3"))
                .andExpect(jsonPath("$.error").value("Delete failed"));
    }

    @Test
    void listObjects_WithoutPrefix_ShouldReturnObjectList() throws Exception {
        // Given
        List<String> objectKeys = Arrays.asList("file1.txt", "file2.txt", "file3.txt");
        when(s3Service.listObjects(eq(testBucketName), eq(null))).thenReturn(objectKeys);

        // When & Then
        mockMvc.perform(get("/api/s3/list/{bucketName}", testBucketName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.bucketName").value(testBucketName))
                .andExpect(jsonPath("$.prefix").value(""))
                .andExpect(jsonPath("$.objectCount").value(3))
                .andExpect(jsonPath("$.objects").isArray())
                .andExpect(jsonPath("$.objects[0]").value("file1.txt"))
                .andExpect(jsonPath("$.objects[1]").value("file2.txt"))
                .andExpect(jsonPath("$.objects[2]").value("file3.txt"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(s3Service, times(1)).listObjects(testBucketName, null);
    }

    @Test
    void listObjects_WithPrefix_ShouldReturnFilteredObjectList() throws Exception {
        // Given
        String prefix = "documents/";
        List<String> objectKeys = Arrays.asList("documents/doc1.pdf", "documents/doc2.pdf");
        when(s3Service.listObjects(eq(testBucketName), eq(prefix))).thenReturn(objectKeys);

        // When & Then
        mockMvc.perform(get("/api/s3/list/{bucketName}", testBucketName)
                .param("prefix", prefix))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.bucketName").value(testBucketName))
                .andExpect(jsonPath("$.prefix").value(prefix))
                .andExpect(jsonPath("$.objectCount").value(2))
                .andExpect(jsonPath("$.objects").isArray())
                .andExpect(jsonPath("$.objects[0]").value("documents/doc1.pdf"))
                .andExpect(jsonPath("$.objects[1]").value("documents/doc2.pdf"));

        verify(s3Service, times(1)).listObjects(testBucketName, prefix);
    }

    @Test
    void listObjects_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(s3Service.listObjects(eq(testBucketName), any()))
                .thenThrow(new RuntimeException("List operation failed"));

        // When & Then
        mockMvc.perform(get("/api/s3/list/{bucketName}", testBucketName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to list objects in S3 bucket"))
                .andExpect(jsonPath("$.error").value("List operation failed"));
    }

    @Test
    void checkBucketExists_WithExistingBucket_ShouldReturnTrue() throws Exception {
        // Given
        when(s3Service.bucketExists(eq(testBucketName))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/s3/bucket/{bucketName}/exists", testBucketName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bucketName").value(testBucketName))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(s3Service, times(1)).bucketExists(testBucketName);
    }

    @Test
    void checkBucketExists_WithNonExistingBucket_ShouldReturnFalse() throws Exception {
        // Given
        when(s3Service.bucketExists(eq(testBucketName))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/s3/bucket/{bucketName}/exists", testBucketName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bucketName").value(testBucketName))
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void checkBucketExists_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(s3Service.bucketExists(eq(testBucketName)))
                .thenThrow(new RuntimeException("Bucket check failed"));

        // When & Then
        mockMvc.perform(get("/api/s3/bucket/{bucketName}/exists", testBucketName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to check bucket existence"))
                .andExpect(jsonPath("$.error").value("Bucket check failed"));
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/s3/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("S3"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}

