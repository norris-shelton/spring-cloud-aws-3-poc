package com.example.springcloudaws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simplified unit tests for GeneralController using MockMvc.
 * Tests public and protected endpoints with proper authentication.
 */
@WebMvcTest(GeneralController.class)
class GeneralControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void health_ShouldReturnHealthStatus_WithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Spring Cloud AWS 3 POC"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void info_ShouldReturnApplicationInfo_WithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.application").value("Spring Cloud AWS 3 POC"))
                .andExpect(jsonPath("$.description").value("Demonstration of Spring Cloud AWS 3.0 integration"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.springBoot").value("3.4.6"))
                .andExpect(jsonPath("$.springCloudAws").value("3.3.0"))
                .andExpect(jsonPath("$.java").exists())
                .andExpect(jsonPath("$.supportedServices").isArray())
                .andExpect(jsonPath("$.supportedServices[0]").value("SQS - Simple Queue Service"));
    }

    @Test
    void test_WithValidCredentials_ShouldReturnSuccess() throws Exception {
        // Given
        Map<String, Object> requestBody = Map.of(
                "testField", "testValue",
                "number", 123,
                "boolean", true
        );

        // When & Then
        mockMvc.perform(post("/api/test")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Test endpoint working correctly"))
                .andExpect(jsonPath("$.receivedData.testField").value("testValue"))
                .andExpect(jsonPath("$.receivedData.number").value(123))
                .andExpect(jsonPath("$.receivedData.boolean").value(true))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void test_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Given
        Map<String, Object> requestBody = Map.of("test", "value");

        // When & Then
        mockMvc.perform(post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Given
        Map<String, Object> requestBody = Map.of("test", "value");

        // When & Then
        mockMvc.perform(post("/api/test")
                .with(httpBasic("user", "user123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        Map<String, Object> requestBody = Map.of("test", "value");

        // When & Then
        mockMvc.perform(post("/api/test")
                .with(httpBasic("admin", "wrongpassword"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_WithEmptyRequestBody_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/test")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Test endpoint working correctly"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void nonExistentEndpoint_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/nonexistent")
                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}

