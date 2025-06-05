package com.javaninja.service;

import com.javaninja.model.dto.SecretsManagerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

@Service
public class SecretsManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecretsManagerService.class);
    
    private final SecretsManagerClient secretsManagerClient;
    
    public SecretsManagerService(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }
    
    public String createSecret(SecretsManagerRequest request) {
        logger.info("Creating secret: {}", request.getSecretName());
        
        CreateSecretRequest.Builder createRequestBuilder = CreateSecretRequest.builder()
                .name(request.getSecretName())
                .secretString(request.getSecretValue());
        
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            createRequestBuilder.description(request.getDescription());
        }
        
        CreateSecretResponse response = secretsManagerClient.createSecret(createRequestBuilder.build());
        
        logger.info("Secret created successfully with ARN: {}", response.arn());
        return response.arn();
    }
    
    public String getSecretValue(String secretName) {
        logger.info("Retrieving secret value: {}", secretName);
        
        GetSecretValueRequest getRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        
        GetSecretValueResponse response = secretsManagerClient.getSecretValue(getRequest);
        
        logger.info("Secret value retrieved successfully");
        return response.secretString();
    }
    
    public String updateSecret(SecretsManagerRequest request) {
        logger.info("Updating secret: {}", request.getSecretName());
        
        UpdateSecretRequest.Builder updateRequestBuilder = UpdateSecretRequest.builder()
                .secretId(request.getSecretName())
                .secretString(request.getSecretValue());
        
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            updateRequestBuilder.description(request.getDescription());
        }
        
        UpdateSecretResponse response = secretsManagerClient.updateSecret(updateRequestBuilder.build());
        
        logger.info("Secret updated successfully, version: {}", response.versionId());
        return response.versionId();
    }
    
    public void deleteSecret(String secretName, boolean forceDelete) {
        logger.info("Deleting secret: {}, forceDelete: {}", secretName, forceDelete);
        
        DeleteSecretRequest.Builder deleteRequestBuilder = DeleteSecretRequest.builder()
                .secretId(secretName);
        
        if (forceDelete) {
            deleteRequestBuilder.forceDeleteWithoutRecovery(true);
        }
        
        DeleteSecretResponse response = secretsManagerClient.deleteSecret(deleteRequestBuilder.build());
        
        logger.info("Secret deletion scheduled, deletion date: {}", response.deletionDate());
    }
    
    public boolean secretExists(String secretName) {
        try {
            DescribeSecretRequest describeRequest = DescribeSecretRequest.builder()
                    .secretId(secretName)
                    .build();
            
            secretsManagerClient.describeSecret(describeRequest);
            return true;
        } catch (ResourceNotFoundException e) {
            logger.warn("Secret {} does not exist", secretName);
            return false;
        } catch (Exception e) {
            logger.warn("Error checking secret existence: {}", e.getMessage());
            return false;
        }
    }
}

