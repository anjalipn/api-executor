package com.example.api.executor.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRequest {
    @NotNull
    private Long taskId;
} 