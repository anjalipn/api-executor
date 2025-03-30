package com.example.api.executor.model;

import lombok.Data;

@Data
public class TaskResponse {
    private String name;
    private String status;
    private TaskOutput output;

    @Data
    public static class TaskOutput {
        private Integer processedItems;
        private String[] errors;
    }
} 