package com.example.springcloudaws.model.dto;

import jakarta.validation.constraints.NotBlank;

public class SqsMessageRequest {
    @NotBlank(message = "Queue name is required")
    private String queueName;
    
    @NotBlank(message = "Message body is required")
    private String messageBody;
    
    private String messageGroupId;
    private String messageDeduplicationId;
    private Integer delaySeconds;
    
    // Constructors
    public SqsMessageRequest() {}
    
    public SqsMessageRequest(String queueName, String messageBody) {
        this.queueName = queueName;
        this.messageBody = messageBody;
    }
    
    // Getters and Setters
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    
    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    
    public String getMessageGroupId() { return messageGroupId; }
    public void setMessageGroupId(String messageGroupId) { this.messageGroupId = messageGroupId; }
    
    public String getMessageDeduplicationId() { return messageDeduplicationId; }
    public void setMessageDeduplicationId(String messageDeduplicationId) { this.messageDeduplicationId = messageDeduplicationId; }
    
    public Integer getDelaySeconds() { return delaySeconds; }
    public void setDelaySeconds(Integer delaySeconds) { this.delaySeconds = delaySeconds; }
}

