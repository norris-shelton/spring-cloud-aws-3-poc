package com.javaninja.controller;

import com.javaninja.model.dto.S3ObjectRequest;
import com.javaninja.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3")
@Tag(name = "S3", description = "Amazon S3 operations")
public class S3Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Controller.class);
    
    private final S3Service s3Service;
    
    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }
    
    @PostMapping("/upload")
    @Operation(summary = "Upload object to S3 bucket")
    public ResponseEntity<Map<String, Object>> uploadObject(@Valid @RequestBody S3ObjectRequest request) {
        try {
            String eTag = s3Service.putObject(request);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "eTag", eTag,
                "bucketName", request.getBucketName(),
                "objectKey", request.getObjectKey(),
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to upload S3 object", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to upload object to S3",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @PostMapping("/upload-file")
    @Operation(summary = "Upload file to S3 bucket")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bucketName") String bucketName,
            @RequestParam("objectKey") String objectKey) {
        try {
            S3ObjectRequest request = new S3ObjectRequest();
            request.setBucketName(bucketName);
            request.setObjectKey(objectKey);
            request.setContentType(file.getContentType());
            request.setContent(file.getBytes());
            
            String eTag = s3Service.putObject(request);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "eTag", eTag,
                "bucketName", bucketName,
                "objectKey", objectKey,
                "fileSize", file.getSize(),
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to upload file to S3", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to upload file to S3",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/download/{bucketName}/{objectKey}")
    @Operation(summary = "Download object from S3 bucket")
    public ResponseEntity<byte[]> downloadObject(
            @PathVariable String bucketName,
            @PathVariable String objectKey) {
        try {
            byte[] content = s3Service.getObject(bucketName, objectKey);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + objectKey + "\"")
                .body(content);
        } catch (Exception e) {
            logger.error("Failed to download S3 object", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{bucketName}/{objectKey}")
    @Operation(summary = "Delete object from S3 bucket")
    public ResponseEntity<Map<String, Object>> deleteObject(
            @PathVariable String bucketName,
            @PathVariable String objectKey) {
        try {
            s3Service.deleteObject(bucketName, objectKey);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Object deleted successfully",
                "bucketName", bucketName,
                "objectKey", objectKey,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to delete S3 object", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to delete object from S3",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/list/{bucketName}")
    @Operation(summary = "List objects in S3 bucket")
    public ResponseEntity<Map<String, Object>> listObjects(
            @PathVariable String bucketName,
            @RequestParam(required = false) String prefix) {
        try {
            List<String> objectKeys = s3Service.listObjects(bucketName, prefix);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "bucketName", bucketName,
                "prefix", prefix != null ? prefix : "",
                "objectCount", objectKeys.size(),
                "objects", objectKeys,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to list S3 objects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to list objects in S3 bucket",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/bucket/{bucketName}/exists")
    @Operation(summary = "Check if S3 bucket exists")
    public ResponseEntity<Map<String, Object>> checkBucketExists(@PathVariable String bucketName) {
        try {
            boolean exists = s3Service.bucketExists(bucketName);
            
            return ResponseEntity.ok(Map.of(
                "bucketName", bucketName,
                "exists", exists,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to check bucket existence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to check bucket existence",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Check S3 service health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "service", "S3",
            "status", "UP",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}

