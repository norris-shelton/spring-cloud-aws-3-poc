package com.javaninja.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sns.SnsClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestAwsConfig {
    
    @Bean
    @Primary
    public SqsTemplate sqsTemplate() {
        return mock(SqsTemplate.class);
    }
    
    @Bean
    @Primary
    public SnsClient snsClient() {
        return mock(SnsClient.class);
    }
    
    @Bean
    @Primary
    public S3Client s3Client() {
        return mock(S3Client.class);
    }
    
    @Bean
    @Primary
    public SecretsManagerClient secretsManagerClient() {
        return mock(SecretsManagerClient.class);
    }
}

