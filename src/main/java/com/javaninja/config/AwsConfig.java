package com.javaninja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsConfig {
    
    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder().build();
    }
    
    @Bean
    public S3Client s3Client() {
        return S3Client.builder().build();
    }
    
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder().build();
    }
}

