package com.javaninja.service;

import com.javaninja.model.dto.SnsMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
public class SnsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SnsService.class);
    
    private final SnsClient snsClient;
    
    public SnsService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }
    
    public String publishMessage(SnsMessageRequest request) {
        logger.info("Publishing message to topic: {}", request.getTopicArn());
        
        PublishRequest.Builder publishRequestBuilder = PublishRequest.builder()
                .topicArn(request.getTopicArn())
                .message(request.getMessage());
        
        if (request.getSubject() != null && !request.getSubject().trim().isEmpty()) {
            publishRequestBuilder.subject(request.getSubject());
        }
        
        if (request.getMessageGroupId() != null && !request.getMessageGroupId().trim().isEmpty()) {
            publishRequestBuilder.messageGroupId(request.getMessageGroupId());
        }
        
        if (request.getMessageDeduplicationId() != null && !request.getMessageDeduplicationId().trim().isEmpty()) {
            publishRequestBuilder.messageDeduplicationId(request.getMessageDeduplicationId());
        }
        
        PublishResponse response = snsClient.publish(publishRequestBuilder.build());
        
        logger.info("Message published successfully with ID: {}", response.messageId());
        return response.messageId();
    }
    
    public boolean checkTopicExists(String topicArn) {
        try {
            // Simple check by attempting to get topic attributes
            snsClient.getTopicAttributes(builder -> builder.topicArn(topicArn));
            return true;
        } catch (Exception e) {
            logger.warn("Topic {} does not exist or is not accessible: {}", topicArn, e.getMessage());
            return false;
        }
    }
}

