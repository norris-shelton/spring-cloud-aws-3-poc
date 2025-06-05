package com.javaninja.controller;

import com.javaninja.model.dto.SnsMessageRequest;
import com.javaninja.service.SnsService;
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
@RequestMapping("/api/sns")
@Tag(name = "SNS", description = "Amazon SNS operations")
public class SnsController {
    
    private static final Logger logger = LoggerFactory.getLogger(SnsController.class);
    
    private final SnsService snsService;
    
    public SnsController(SnsService snsService) {
        this.snsService = snsService;
    }
    
    @PostMapping("/publish")
    @Operation(summary = "Publish message to SNS topic")
    public ResponseEntity<Map<String, Object>> publishMessage(@Valid @RequestBody SnsMessageRequest request) {
        try {
            String messageId = snsService.publishMessage(request);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "messageId", messageId,
                "topicArn", request.getTopicArn(),
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to publish SNS message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to publish message to SNS topic",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/topic/{topicArn}/exists")
    @Operation(summary = "Check if SNS topic exists")
    public ResponseEntity<Map<String, Object>> checkTopicExists(@PathVariable String topicArn) {
        try {
            boolean exists = snsService.checkTopicExists(topicArn);
            
            return ResponseEntity.ok(Map.of(
                "topicArn", topicArn,
                "exists", exists,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to check topic existence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to check topic existence",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Check SNS service health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "service", "SNS",
            "status", "UP",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}

