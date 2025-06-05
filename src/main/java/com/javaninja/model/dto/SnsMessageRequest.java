package com.javaninja.model.dto;

import jakarta.validation.constraints.NotBlank;

public class SnsMessageRequest {
    
    @NotBlank(message = "Topic ARN is required")
    private String topicArn;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String subject;
    private String messageGroupId;
    private String messageDeduplicationId;
    
    // Constructors
    public SnsMessageRequest() {}
    
    public SnsMessageRequest(String topicArn, String message) {
        this.topicArn = topicArn;
        this.message = message;
    }
    
    // Getters and Setters
    public String getTopicArn() {
        return topicArn;
    }
    
    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessageGroupId() {
        return messageGroupId;
    }
    
    public void setMessageGroupId(String messageGroupId) {
        this.messageGroupId = messageGroupId;
    }
    
    public String getMessageDeduplicationId() {
        return messageDeduplicationId;
    }
    
    public void setMessageDeduplicationId(String messageDeduplicationId) {
        this.messageDeduplicationId = messageDeduplicationId;
    }
    
    @Override
    public String toString() {
        return "SnsMessageRequest{" +
                "topicArn='" + topicArn + '\'' +
                ", message='" + message + '\'' +
                ", subject='" + subject + '\'' +
                ", messageGroupId='" + messageGroupId + '\'' +
                ", messageDeduplicationId='" + messageDeduplicationId + '\'' +
                '}';
    }
}

