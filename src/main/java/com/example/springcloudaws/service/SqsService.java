package com.example.springcloudaws.service;

import com.example.springcloudaws.model.dto.SqsMessageRequest;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class SqsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SqsService.class);
    
    private final SqsTemplate sqsTemplate;
    
    public SqsService(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }
    
    public String sendMessage(SqsMessageRequest request) {
        try {
            logger.info("Sending message to queue: {}", request.getQueueName());
            
            var message = MessageBuilder
                .withPayload(request.getMessageBody())
                .build();
            
            var result = sqsTemplate.send(request.getQueueName(), message);
            
            logger.info("Message sent successfully with ID: {}", result.messageId());
            return result.messageId().toString();
            
        } catch (Exception e) {
            logger.error("Failed to send message to SQS queue: {}", request.getQueueName(), e);
            throw new RuntimeException("Failed to send message to SQS queue", e);
        }
    }
}

