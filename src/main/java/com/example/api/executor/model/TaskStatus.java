package com.example.api.executor.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskStatus {
    private String invocationId;
    private Integer taskId;
    private String status; // PENDING, COMPLETED, FAILED
    private ApiResponse result;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 