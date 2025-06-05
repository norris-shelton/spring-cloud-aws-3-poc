package com.example.springcloudaws.service;

import com.example.springcloudaws.model.dto.SqsMessageRequest;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SqsService.
 * Tests the business logic and AWS SQS integration.
 */
@ExtendWith(MockitoExtension.class)
class SqsServiceTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private SendResult sendResult;

    private SqsService sqsService;

    @BeforeEach
    void setUp() {
        sqsService = new SqsService(sqsTemplate);
    }

    @Test
    void sendMessage_WithValidRequest_ShouldReturnMessageId() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("test-queue");
        request.setMessageBody("Test message body");

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("test-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("test-queue"), any(Message.class));
        verify(sendResult, times(2)).messageId(); // Called once for logging, once for return
    }

    @Test
    void sendMessage_WithCompleteRequest_ShouldHandleAllFields() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("complete-queue");
        request.setMessageBody("Complete message body");
        request.setMessageGroupId("group-123");
        request.setMessageDeduplicationId("dedup-456");
        request.setDelaySeconds(30);

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("complete-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("complete-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WithMinimalRequest_ShouldWork() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest("minimal-queue", "Minimal message");

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("minimal-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("minimal-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WhenSqsTemplateThrowsException_ShouldThrowRuntimeException() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("error-queue");
        request.setMessageBody("Error message");

        when(sqsTemplate.send(eq("error-queue"), any(Message.class)))
                .thenThrow(new RuntimeException("SQS connection error"));

        // When & Then
        assertThatThrownBy(() -> sqsService.sendMessage(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send message to SQS queue")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(sqsTemplate, times(1)).send(eq("error-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WhenSqsTemplateThrowsSpecificException_ShouldWrapException() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("specific-error-queue");
        request.setMessageBody("Specific error message");

        RuntimeException specificException = new RuntimeException("Queue does not exist");
        when(sqsTemplate.send(eq("specific-error-queue"), any(Message.class)))
                .thenThrow(specificException);

        // When & Then
        assertThatThrownBy(() -> sqsService.sendMessage(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send message to SQS queue")
                .hasCause(specificException);

        verify(sqsTemplate, times(1)).send(eq("specific-error-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WithNullOptionalFields_ShouldWork() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("null-fields-queue");
        request.setMessageBody("Message with null fields");
        request.setMessageGroupId(null);
        request.setMessageDeduplicationId(null);
        request.setDelaySeconds(null);

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("null-fields-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("null-fields-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WithSpecialCharactersInMessage_ShouldWork() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("special-chars-queue");
        request.setMessageBody("Message with special chars: !@#$%^&*()_+{}|:<>?[]\\;'\",./ and unicode: 你好 🚀");

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("special-chars-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("special-chars-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WithLongMessage_ShouldWork() {
        // Given
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a long message part ").append(i).append(". ");
        }

        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("long-message-queue");
        request.setMessageBody(longMessage.toString());

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("long-message-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("long-message-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WithZeroDelaySeconds_ShouldWork() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("zero-delay-queue");
        request.setMessageBody("Zero delay message");
        request.setDelaySeconds(0);

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("zero-delay-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("zero-delay-queue"), any(Message.class));
    }

    @Test
    void sendMessage_WithMaxDelaySeconds_ShouldWork() {
        // Given
        SqsMessageRequest request = new SqsMessageRequest();
        request.setQueueName("max-delay-queue");
        request.setMessageBody("Max delay message");
        request.setDelaySeconds(900); // 15 minutes - SQS maximum

        UUID expectedMessageId = UUID.randomUUID();
        when(sqsTemplate.send(eq("max-delay-queue"), any(Message.class))).thenReturn(sendResult);
        when(sendResult.messageId()).thenReturn(expectedMessageId);

        // When
        String result = sqsService.sendMessage(request);

        // Then
        assertThat(result).isEqualTo(expectedMessageId.toString());
        verify(sqsTemplate, times(1)).send(eq("max-delay-queue"), any(Message.class));
    }
}

