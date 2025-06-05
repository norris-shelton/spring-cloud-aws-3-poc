package com.javaninja.controller;

import com.javaninja.model.dto.SecretsManagerRequest;
import com.javaninja.service.SecretsManagerService;
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
@RequestMapping("/api/secrets")
@Tag(name = "Secrets Manager", description = "AWS Secrets Manager operations")
public class SecretsManagerController {
    
    private static final Logger logger = LoggerFactory.getLogger(SecretsManagerController.class);
    
    private final SecretsManagerService secretsManagerService;
    
    public SecretsManagerController(SecretsManagerService secretsManagerService) {
        this.secretsManagerService = secretsManagerService;
    }
    
    @PostMapping("/create")
    @Operation(summary = "Create a new secret")
    public ResponseEntity<Map<String, Object>> createSecret(@Valid @RequestBody SecretsManagerRequest request) {
        try {
            String arn = secretsManagerService.createSecret(request);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "secretArn", arn,
                "secretName", request.getSecretName(),
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to create secret", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to create secret",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/{secretName}")
    @Operation(summary = "Get secret value")
    public ResponseEntity<Map<String, Object>> getSecret(@PathVariable String secretName) {
        try {
            String secretValue = secretsManagerService.getSecretValue(secretName);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "secretName", secretName,
                "secretValue", secretValue,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to get secret value", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to get secret value",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @PutMapping("/update")
    @Operation(summary = "Update secret value")
    public ResponseEntity<Map<String, Object>> updateSecret(@Valid @RequestBody SecretsManagerRequest request) {
        try {
            String versionId = secretsManagerService.updateSecret(request);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "secretName", request.getSecretName(),
                "versionId", versionId,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to update secret", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to update secret",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @DeleteMapping("/{secretName}")
    @Operation(summary = "Delete secret")
    public ResponseEntity<Map<String, Object>> deleteSecret(
            @PathVariable String secretName,
            @RequestParam(defaultValue = "false") boolean forceDelete) {
        try {
            secretsManagerService.deleteSecret(secretName, forceDelete);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", forceDelete ? "Secret deleted immediately" : "Secret scheduled for deletion",
                "secretName", secretName,
                "forceDelete", forceDelete,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to delete secret", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to delete secret",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/{secretName}/exists")
    @Operation(summary = "Check if secret exists")
    public ResponseEntity<Map<String, Object>> checkSecretExists(@PathVariable String secretName) {
        try {
            boolean exists = secretsManagerService.secretExists(secretName);
            
            return ResponseEntity.ok(Map.of(
                "secretName", secretName,
                "exists", exists,
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to check secret existence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to check secret existence",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Check Secrets Manager service health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "service", "Secrets Manager",
            "status", "UP",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}

