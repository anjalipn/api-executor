package com.example.api.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "api.endpoints")
@Data
public class EndpointConfig {
    private String baseUrl;
    private String taskStatusPath;
    private String websocketPath;
} 