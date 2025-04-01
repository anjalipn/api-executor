package com.example.api.executor.model;

import lombok.Data;

@Data
public class TaskExecutionResponse {
    private Long taskId;
    private TaskStatus state;
    private Object result;
    private String correlationId;
    private String invocationId;
    private String statusEndpoint;
    private String websocketEndpoint;

    public void syncResponse(String correlationId) {
        this.state = TaskStatus.QUEUED_FOR_EXECUTION;
        this.correlationId = correlationId;
    }

    public void asyncResponse(Long taskId, String invocationId, Object result) {
        this.state = TaskStatus.IN_PROGRESS;
        this.invocationId = invocationId;
        this.result = result;
    }
} 