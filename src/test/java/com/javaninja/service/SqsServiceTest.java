package com.javaninja.service;

import com.javaninja.model.dto.SqsMessageRequest;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SqsService.
 * Tests business logic for AWS SQS operations.
 */
@ExtendWith(MockitoExtension.class)
class SqsServiceTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private SendResult<Object> sendResult;

    @InjectMocks
    private SqsService sqsService;

    private SqsMessageRequest validRequest;
    private UUID expectedMessageId;

    @BeforeEach
    void setUp() {
        validRequest = new SqsMessageRequest();
        validRequest.setQueueName("test-queue");
        validRequest.setMessageBody("Test message body");
        validRequest.setDelaySeconds(10);

        expectedMessageId = UUID.randomUUID();
    }

    @Test
    void sendMessage_WithValidRequest_ShouldReturnMessageId() {
        // Given
        when(sqsTemplate.send(eq("test-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(validRequest);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("test-queue"), any(Message.class));
        verify(sendResult, times(2)).messageId(); // Called once for logging, once for return
    }
}

