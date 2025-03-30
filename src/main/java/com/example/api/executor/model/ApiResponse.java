package com.example.api.executor.model;

import lombok.Data;

@Data
public class ApiResponse {
    private String status;
    private Object result;
    private String invocationId;

    public static ApiResponse syncResponse(TaskResponse taskResponse) {
        ApiResponse response = new ApiResponse();
        response.setStatus("completed");
        response.setResult(taskResponse);
        return response;
    }

    public static ApiResponse asyncResponse(String invocationId) {
        ApiResponse response = new ApiResponse();
        response.setStatus("executing");
        response.setInvocationId(invocationId);
        return response;
    }
} 