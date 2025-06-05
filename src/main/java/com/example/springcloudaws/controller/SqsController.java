package com.example.springcloudaws.controller;

import com.example.springcloudaws.model.dto.SqsMessageRequest;
import com.example.springcloudaws.service.SqsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sqs")
@Tag(name = "SQS", description = "Amazon SQS operations")
public class SqsController {
    
    private final SqsService sqsService;
    
    public SqsController(SqsService sqsService) {
        this.sqsService = sqsService;
    }
    
    @PostMapping("/send")
    @Operation(summary = "Send message to SQS queue")
    public ResponseEntity<Map<String, Object>> sendMessage(@Valid @RequestBody SqsMessageRequest request) {
        String messageId = sqsService.sendMessage(request);
        
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "messageId", messageId,
            "queueName", request.getQueueName(),
            "timestamp", java.time.Instant.now().toString()
        ));
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

