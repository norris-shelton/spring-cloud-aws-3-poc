package com.javaninja.controller;

import com.javaninja.model.dto.SnsMessageRequest;
import com.javaninja.service.SnsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SnsController using MockMvc.
 * Tests AWS SNS integration endpoints.
 */
@WebMvcTest(SnsController.class)
class SnsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SnsService snsService;

    @Autowired
    private ObjectMapper objectMapper;

    private SnsMessageRequest validRequest;
    private String testTopicArn;

    @BeforeEach
    void setUp() {
        testTopicArn = "arn:aws:sns:us-east-1:123456789012:test-topic";
        
        validRequest = new SnsMessageRequest();
        validRequest.setTopicArn(testTopicArn);
        validRequest.setMessage("Test SNS message");
        validRequest.setSubject("Test Subject");
    }

    @Test
    void publishMessage_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        String expectedMessageId = "msg-12345";
        when(snsService.publishMessage(any(SnsMessageRequest.class))).thenReturn(expectedMessageId);

        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.messageId").value(expectedMessageId))
                .andExpect(jsonPath("$.topicArn").value(testTopicArn))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(snsService, times(1)).publishMessage(any(SnsMessageRequest.class));
    }

    @Test
    void publishMessage_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - request with missing required fields
        SnsMessageRequest invalidRequest = new SnsMessageRequest();
        invalidRequest.setTopicArn(""); // Empty topic ARN
        invalidRequest.setMessage(""); // Empty message

        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(snsService, never()).publishMessage(any(SnsMessageRequest.class));
    }

    @Test
    void publishMessage_WithMissingTopicArn_ShouldReturnBadRequest() throws Exception {
        // Given
        SnsMessageRequest invalidRequest = new SnsMessageRequest();
        invalidRequest.setMessage("Test message");
        // topicArn is null

        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(snsService, never()).publishMessage(any(SnsMessageRequest.class));
    }

    @Test
    void publishMessage_WithMissingMessage_ShouldReturnBadRequest() throws Exception {
        // Given
        SnsMessageRequest invalidRequest = new SnsMessageRequest();
        invalidRequest.setTopicArn(testTopicArn);
        // message is null

        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(snsService, never()).publishMessage(any(SnsMessageRequest.class));
    }

    @Test
    void publishMessage_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(snsService.publishMessage(any(SnsMessageRequest.class)))
                .thenThrow(new RuntimeException("SNS service error"));

        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to publish message to SNS topic"))
                .andExpect(jsonPath("$.error").value("SNS service error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void publishMessage_WithOptionalFields_ShouldReturnSuccess() throws Exception {
        // Given
        validRequest.setMessageGroupId("test-group");
        validRequest.setMessageDeduplicationId("dedup-123");
        String expectedMessageId = "msg-67890";
        when(snsService.publishMessage(any(SnsMessageRequest.class))).thenReturn(expectedMessageId);

        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.messageId").value(expectedMessageId));

        verify(snsService).publishMessage(argThat(request -> 
            request.getTopicArn().equals(testTopicArn) &&
            request.getMessage().equals("Test SNS message") &&
            request.getSubject().equals("Test Subject") &&
            request.getMessageGroupId().equals("test-group") &&
            request.getMessageDeduplicationId().equals("dedup-123")
        ));
    }

    @Test
    void checkTopicExists_WithExistingTopic_ShouldReturnTrue() throws Exception {
        // Given
        when(snsService.checkTopicExists(eq(testTopicArn))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/sns/topic/{topicArn}/exists", testTopicArn))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.topicArn").value(testTopicArn))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(snsService, times(1)).checkTopicExists(testTopicArn);
    }

    @Test
    void checkTopicExists_WithNonExistingTopic_ShouldReturnFalse() throws Exception {
        // Given
        when(snsService.checkTopicExists(eq(testTopicArn))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/sns/topic/{topicArn}/exists", testTopicArn))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.topicArn").value(testTopicArn))
                .andExpect(jsonPath("$.exists").value(false))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void checkTopicExists_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(snsService.checkTopicExists(eq(testTopicArn)))
                .thenThrow(new RuntimeException("Topic check failed"));

        // When & Then
        mockMvc.perform(get("/api/sns/topic/{topicArn}/exists", testTopicArn))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to check topic existence"))
                .andExpect(jsonPath("$.error").value("Topic check failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sns/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("SNS"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void publishMessage_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(snsService, never()).publishMessage(any(SnsMessageRequest.class));
    }

    @Test
    void publishMessage_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sns/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(snsService, never()).publishMessage(any(SnsMessageRequest.class));
    }
}

