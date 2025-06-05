package com.javaninja.controller;

import com.javaninja.model.dto.SecretsManagerRequest;
import com.javaninja.service.SecretsManagerService;
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
 * Unit tests for SecretsManagerController using MockMvc.
 * Tests AWS Secrets Manager integration endpoints.
 */
@WebMvcTest(SecretsManagerController.class)
class SecretsManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecretsManagerService secretsManagerService;

    @Autowired
    private ObjectMapper objectMapper;

    private SecretsManagerRequest validRequest;
    private String testSecretName;
    private String testSecretValue;
    private String expectedArn;

    @BeforeEach
    void setUp() {
        testSecretName = "test-secret";
        testSecretValue = "super-secret-value";
        expectedArn = "arn:aws:secretsmanager:us-east-1:123456789012:secret:test-secret-AbCdEf";
        
        validRequest = new SecretsManagerRequest();
        validRequest.setSecretName(testSecretName);
        validRequest.setSecretValue(testSecretValue);
        validRequest.setDescription("Test secret description");
    }

    @Test
    void createSecret_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        when(secretsManagerService.createSecret(any(SecretsManagerRequest.class))).thenReturn(expectedArn);

        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.secretArn").value(expectedArn))
                .andExpect(jsonPath("$.secretName").value(testSecretName))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(secretsManagerService, times(1)).createSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void createSecret_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - request with missing required fields
        SecretsManagerRequest invalidRequest = new SecretsManagerRequest();
        invalidRequest.setSecretName(""); // Empty secret name
        invalidRequest.setSecretValue(""); // Empty secret value

        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(secretsManagerService, never()).createSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void createSecret_WithMissingSecretName_ShouldReturnBadRequest() throws Exception {
        // Given
        SecretsManagerRequest invalidRequest = new SecretsManagerRequest();
        invalidRequest.setSecretValue(testSecretValue);
        // secretName is null

        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(secretsManagerService, never()).createSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void createSecret_WithMissingSecretValue_ShouldReturnBadRequest() throws Exception {
        // Given
        SecretsManagerRequest invalidRequest = new SecretsManagerRequest();
        invalidRequest.setSecretName(testSecretName);
        // secretValue is null

        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(secretsManagerService, never()).createSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void createSecret_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(secretsManagerService.createSecret(any(SecretsManagerRequest.class)))
                .thenThrow(new RuntimeException("Secret creation failed"));

        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to create secret"))
                .andExpect(jsonPath("$.error").value("Secret creation failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getSecret_WithValidSecretName_ShouldReturnSecretValue() throws Exception {
        // Given
        when(secretsManagerService.getSecretValue(eq(testSecretName))).thenReturn(testSecretValue);

        // When & Then
        mockMvc.perform(get("/api/secrets/{secretName}", testSecretName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.secretName").value(testSecretName))
                .andExpect(jsonPath("$.secretValue").value(testSecretValue))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(secretsManagerService, times(1)).getSecretValue(testSecretName);
    }

    @Test
    void getSecret_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(secretsManagerService.getSecretValue(eq(testSecretName)))
                .thenThrow(new RuntimeException("Secret not found"));

        // When & Then
        mockMvc.perform(get("/api/secrets/{secretName}", testSecretName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to get secret value"))
                .andExpect(jsonPath("$.error").value("Secret not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateSecret_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        String expectedVersionId = "version-12345";
        when(secretsManagerService.updateSecret(any(SecretsManagerRequest.class))).thenReturn(expectedVersionId);

        // When & Then
        mockMvc.perform(put("/api/secrets/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.secretName").value(testSecretName))
                .andExpect(jsonPath("$.versionId").value(expectedVersionId))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(secretsManagerService, times(1)).updateSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void updateSecret_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - request with missing required fields
        SecretsManagerRequest invalidRequest = new SecretsManagerRequest();
        invalidRequest.setSecretName(""); // Empty secret name

        // When & Then
        mockMvc.perform(put("/api/secrets/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(secretsManagerService, never()).updateSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void updateSecret_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(secretsManagerService.updateSecret(any(SecretsManagerRequest.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        mockMvc.perform(put("/api/secrets/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to update secret"))
                .andExpect(jsonPath("$.error").value("Update failed"));
    }

    @Test
    void deleteSecret_WithValidSecretName_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(secretsManagerService).deleteSecret(eq(testSecretName), eq(false));

        // When & Then
        mockMvc.perform(delete("/api/secrets/{secretName}", testSecretName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Secret scheduled for deletion"))
                .andExpect(jsonPath("$.secretName").value(testSecretName))
                .andExpect(jsonPath("$.forceDelete").value(false))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(secretsManagerService, times(1)).deleteSecret(testSecretName, false);
    }

    @Test
    void deleteSecret_WithForceDeleteTrue_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(secretsManagerService).deleteSecret(eq(testSecretName), eq(true));

        // When & Then
        mockMvc.perform(delete("/api/secrets/{secretName}", testSecretName)
                .param("forceDelete", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Secret deleted immediately"))
                .andExpect(jsonPath("$.secretName").value(testSecretName))
                .andExpect(jsonPath("$.forceDelete").value(true));

        verify(secretsManagerService, times(1)).deleteSecret(testSecretName, true);
    }

    @Test
    void deleteSecret_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        doThrow(new RuntimeException("Delete failed")).when(secretsManagerService).deleteSecret(eq(testSecretName), eq(false));

        // When & Then
        mockMvc.perform(delete("/api/secrets/{secretName}", testSecretName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to delete secret"))
                .andExpect(jsonPath("$.error").value("Delete failed"));
    }

    @Test
    void checkSecretExists_WithExistingSecret_ShouldReturnTrue() throws Exception {
        // Given
        when(secretsManagerService.secretExists(eq(testSecretName))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/secrets/{secretName}/exists", testSecretName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.secretName").value(testSecretName))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(secretsManagerService, times(1)).secretExists(testSecretName);
    }

    @Test
    void checkSecretExists_WithNonExistingSecret_ShouldReturnFalse() throws Exception {
        // Given
        when(secretsManagerService.secretExists(eq(testSecretName))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/secrets/{secretName}/exists", testSecretName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.secretName").value(testSecretName))
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void checkSecretExists_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(secretsManagerService.secretExists(eq(testSecretName)))
                .thenThrow(new RuntimeException("Secret check failed"));

        // When & Then
        mockMvc.perform(get("/api/secrets/{secretName}/exists", testSecretName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to check secret existence"))
                .andExpect(jsonPath("$.error").value("Secret check failed"));
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/secrets/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("Secrets Manager"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createSecret_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(secretsManagerService, never()).createSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void createSecret_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(secretsManagerService, never()).createSecret(any(SecretsManagerRequest.class));
    }

    @Test
    void createSecret_WithOptionalDescription_ShouldReturnSuccess() throws Exception {
        // Given
        validRequest.setDescription("Updated description");
        when(secretsManagerService.createSecret(any(SecretsManagerRequest.class))).thenReturn(expectedArn);

        // When & Then
        mockMvc.perform(post("/api/secrets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.secretArn").value(expectedArn));

        verify(secretsManagerService).createSecret(argThat(request -> 
            request.getSecretName().equals(testSecretName) &&
            request.getSecretValue().equals(testSecretValue) &&
            request.getDescription().equals("Updated description")
        ));
    }
}

