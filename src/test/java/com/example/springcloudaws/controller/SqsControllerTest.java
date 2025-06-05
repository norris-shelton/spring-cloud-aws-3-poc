package com.example.springcloudaws.controller;

import com.example.springcloudaws.model.dto.SqsMessageRequest;
import com.example.springcloudaws.service.SqsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simplified unit tests for SqsController using MockMvc.
 * Tests basic functionality with proper authentication.
 */
@WebMvcTest(SqsController.class)
class SqsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SqsService sqsService;

    @Autowired
    private ObjectMapper objectMapper;

    private SqsMessageRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SqsMessageRequest();
        validRequest.setQueueName("test-queue");
        validRequest.setMessageBody("Test message body");
        validRequest.setDelaySeconds(10);
    }

    @Test
    void sendMessage_WithValidCredentials_ShouldReturnSuccess() throws Exception {
        // Given
        String expectedMessageId = "msg-12345";
        when(sqsService.sendMessage(any(SqsMessageRequest.class))).thenReturn(expectedMessageId);

        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.messageId").value(expectedMessageId))
                .andExpect(jsonPath("$.queueName").value("test-queue"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(sqsService, times(1)).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void sendMessage_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());

        verify(sqsService, never()).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void sendMessage_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .with(httpBasic("admin", "wrongpassword"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());

        verify(sqsService, never()).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void sendMessage_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .with(httpBasic("user", "user123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(sqsService, never()).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void health_WithValidCredentials_ShouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sqs/health")
                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("SQS"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void health_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sqs/health"))
                .andExpect(status().isUnauthorized());
    }
}

