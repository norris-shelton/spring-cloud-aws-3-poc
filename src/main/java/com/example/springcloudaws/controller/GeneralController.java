package com.example.springcloudaws.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "General", description = "General application endpoints")
public class GeneralController {
    
    @GetMapping("/health")
    @Operation(summary = "Application health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "application", "Spring Cloud AWS 3 POC",
            "version", "1.0.0",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    @GetMapping("/info")
    @Operation(summary = "Application information")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "application", "Spring Cloud AWS 3 POC",
            "description", "Demonstration of Spring Cloud AWS 3.0 integration",
            "version", "1.0.0",
            "springBoot", "3.4.6",
            "springCloudAws", "3.3.0",
            "java", System.getProperty("java.version"),
            "supportedServices", new String[]{
                "SQS - Simple Queue Service",
                "SNS - Simple Notification Service", 
                "S3 - Simple Storage Service",
                "DynamoDB - NoSQL Database",
                "SES - Simple Email Service",
                "Secrets Manager",
                "Parameter Store",
                "CloudWatch"
            }
        ));
    }
    
    @PostMapping("/test")
    @Operation(summary = "Test endpoint")
    public ResponseEntity<Map<String, Object>> test(@RequestBody(required = false) Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "Test endpoint working correctly",
            "receivedData", request != null ? request : "No data received",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}

