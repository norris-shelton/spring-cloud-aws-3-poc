package com.javaninja.model.dto;

import jakarta.validation.constraints.NotBlank;

public class SecretsManagerRequest {
    
    @NotBlank(message = "Secret name is required")
    private String secretName;
    
    private String secretValue;
    private String description;
    private String versionStage;
    
    // Constructors
    public SecretsManagerRequest() {}
    
    public SecretsManagerRequest(String secretName) {
        this.secretName = secretName;
    }
    
    public SecretsManagerRequest(String secretName, String secretValue) {
        this.secretName = secretName;
        this.secretValue = secretValue;
    }
    
    // Getters and Setters
    public String getSecretName() {
        return secretName;
    }
    
    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }
    
    public String getSecretValue() {
        return secretValue;
    }
    
    public void setSecretValue(String secretValue) {
        this.secretValue = secretValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getVersionStage() {
        return versionStage;
    }
    
    public void setVersionStage(String versionStage) {
        this.versionStage = versionStage;
    }
    
    @Override
    public String toString() {
        return "SecretsManagerRequest{" +
                "secretName='" + secretName + '\'' +
                ", description='" + description + '\'' +
                ", versionStage='" + versionStage + '\'' +
                '}';
    }
}

