package com.example.api.executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
public class ApiExecutorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiExecutorApplication.class, args);
    }
} 