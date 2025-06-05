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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SqsController using MockMvc.
 * Tests AWS SQS integration endpoints without security.
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
    private SqsMessageRequest invalidRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SqsMessageRequest();
        validRequest.setQueueName("test-queue");
        validRequest.setMessageBody("Test message body");
        validRequest.setDelaySeconds(10);

        invalidRequest = new SqsMessageRequest();
        invalidRequest.setQueueName(""); // Invalid: empty queue name
        invalidRequest.setMessageBody(""); // Invalid: empty message body
    }

    @Test
    void sendMessage_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        String expectedMessageId = "msg-12345";
        when(sqsService.sendMessage(any(SqsMessageRequest.class))).thenReturn(expectedMessageId);

        // When & Then
        mockMvc.perform(post("/api/sqs/send")
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
    void sendMessage_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(sqsService, never()).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void sendMessage_WithMissingRequestBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(sqsService, never()).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void sendMessage_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(sqsService.sendMessage(any(SqsMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS service error"));

        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());

        verify(sqsService, times(1)).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sqs/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("SQS"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void sendMessage_WithCompleteRequest_ShouldIncludeAllFields() throws Exception {
        // Given
        SqsMessageRequest completeRequest = new SqsMessageRequest();
        completeRequest.setQueueName("complete-queue");
        completeRequest.setMessageBody("Complete message body");
        completeRequest.setMessageGroupId("group-123");
        completeRequest.setMessageDeduplicationId("dedup-456");
        completeRequest.setDelaySeconds(30);

        String expectedMessageId = "msg-complete-12345";
        when(sqsService.sendMessage(any(SqsMessageRequest.class))).thenReturn(expectedMessageId);

        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.messageId").value(expectedMessageId))
                .andExpect(jsonPath("$.queueName").value("complete-queue"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(sqsService, times(1)).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void sendMessage_WithNullValues_ShouldHandleGracefully() throws Exception {
        // Given
        SqsMessageRequest requestWithNulls = new SqsMessageRequest();
        requestWithNulls.setQueueName("test-queue");
        requestWithNulls.setMessageBody("Test message");
        requestWithNulls.setMessageGroupId(null);
        requestWithNulls.setMessageDeduplicationId(null);
        requestWithNulls.setDelaySeconds(null);

        String expectedMessageId = "msg-nulls-12345";
        when(sqsService.sendMessage(any(SqsMessageRequest.class))).thenReturn(expectedMessageId);

        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithNulls)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.messageId").value(expectedMessageId));

        verify(sqsService, times(1)).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void sendMessage_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sqs/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(sqsService, never()).sendMessage(any(SqsMessageRequest.class));
    }

    @Test
    void health_WithDifferentHttpMethods_ShouldReturnMethodNotAllowed() throws Exception {
        // Test POST method on health endpoint
        mockMvc.perform(post("/api/sqs/health"))
                .andExpect(status().isMethodNotAllowed());

        // Test PUT method on health endpoint
        mockMvc.perform(put("/api/sqs/health"))
                .andExpect(status().isMethodNotAllowed());

        // Test DELETE method on health endpoint
        mockMvc.perform(delete("/api/sqs/health"))
                .andExpect(status().isMethodNotAllowed());
    }
}

