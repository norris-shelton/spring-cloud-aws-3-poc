package com.example.springcloudaws.model.dto;

public class SqsMessageResponse {
    private String messageId;
    private String md5OfBody;
    private String status;
    private String timestamp;
    
    public SqsMessageResponse() {}
    
    public SqsMessageResponse(String messageId, String md5OfBody, String status) {
        this.messageId = messageId;
        this.md5OfBody = md5OfBody;
        this.status = status;
        this.timestamp = java.time.Instant.now().toString();
    }
    
    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public String getMd5OfBody() { return md5OfBody; }
    public void setMd5OfBody(String md5OfBody) { this.md5OfBody = md5OfBody; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

