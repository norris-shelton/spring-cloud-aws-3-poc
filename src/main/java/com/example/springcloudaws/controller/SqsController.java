package com.example.springcloudaws.controller;

import com.example.springcloudaws.model.dto.SqsMessageRequest;
import com.example.springcloudaws.service.SqsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sqs")
@Tag(name = "SQS", description = "Amazon SQS operations")
public class SqsController {
    
    private static final Logger logger = LoggerFactory.getLogger(SqsController.class);
    
    private final SqsService sqsService;
    
    public SqsController(SqsService sqsService) {
        this.sqsService = sqsService;
    }
    
    @PostMapping("/send")
    @Operation(summary = "Send message to SQS queue")
    public ResponseEntity<Map<String, Object>> sendMessage(@Valid @RequestBody SqsMessageRequest request) {
        try {
            String messageId = sqsService.sendMessage(request);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "messageId", messageId,
                "queueName", request.getQueueName(),
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to send SQS message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to send message to SQS queue",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Check SQS service health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "service", "SQS",
            "status", "UP",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}

