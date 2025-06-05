package com.javaninja.service;

import com.javaninja.model.dto.S3ObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class S3Service {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    
    private final S3Client s3Client;
    
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    
    public String putObject(S3ObjectRequest request) {
        logger.info("Uploading object to S3: bucket={}, key={}", request.getBucketName(), request.getObjectKey());
        
        Map<String, String> metadata = new HashMap<>();
        if (request.getMetadata() != null) {
            metadata.put("custom-metadata", request.getMetadata());
        }
        
        PutObjectRequest.Builder putRequestBuilder = PutObjectRequest.builder()
                .bucket(request.getBucketName())
                .key(request.getObjectKey())
                .metadata(metadata);
        
        if (request.getContentType() != null && !request.getContentType().trim().isEmpty()) {
            putRequestBuilder.contentType(request.getContentType());
        }
        
        RequestBody requestBody = request.getContent() != null 
                ? RequestBody.fromBytes(request.getContent())
                : RequestBody.fromString("Default content");
        
        PutObjectResponse response = s3Client.putObject(putRequestBuilder.build(), requestBody);
        
        logger.info("Object uploaded successfully with ETag: {}", response.eTag());
        return response.eTag();
    }
    
    public byte[] getObject(String bucketName, String objectKey) {
        logger.info("Downloading object from S3: bucket={}, key={}", bucketName, objectKey);
        
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        
        byte[] content = s3Client.getObjectAsBytes(getRequest).asByteArray();
        
        logger.info("Object downloaded successfully, size: {} bytes", content.length);
        return content;
    }
    
    public void deleteObject(String bucketName, String objectKey) {
        logger.info("Deleting object from S3: bucket={}, key={}", bucketName, objectKey);
        
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        
        s3Client.deleteObject(deleteRequest);
        
        logger.info("Object deleted successfully");
    }
    
    public List<String> listObjects(String bucketName, String prefix) {
        logger.info("Listing objects in S3 bucket: {}, prefix: {}", bucketName, prefix);
        
        ListObjectsV2Request.Builder listRequestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName);
        
        if (prefix != null && !prefix.trim().isEmpty()) {
            listRequestBuilder.prefix(prefix);
        }
        
        ListObjectsV2Response response = s3Client.listObjectsV2(listRequestBuilder.build());
        
        List<String> objectKeys = response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
        
        logger.info("Found {} objects", objectKeys.size());
        return objectKeys;
    }
    
    public boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (Exception e) {
            logger.warn("Bucket {} does not exist or is not accessible: {}", bucketName, e.getMessage());
            return false;
        }
    }
}

