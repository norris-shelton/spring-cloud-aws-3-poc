package com.javaninja.model.dto;

import jakarta.validation.constraints.NotBlank;

public class S3ObjectRequest {
    
    @NotBlank(message = "Bucket name is required")
    private String bucketName;
    
    @NotBlank(message = "Object key is required")
    private String objectKey;
    
    private String contentType;
    private byte[] content;
    private String metadata;
    
    // Constructors
    public S3ObjectRequest() {}
    
    public S3ObjectRequest(String bucketName, String objectKey) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }
    
    // Getters and Setters
    public String getBucketName() {
        return bucketName;
    }
    
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public String getObjectKey() {
        return objectKey;
    }
    
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "S3ObjectRequest{" +
                "bucketName='" + bucketName + '\'' +
                ", objectKey='" + objectKey + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + (content != null ? content.length : 0) +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}

