package com.example.api.executor.model;

public enum TaskStatus {
    PENDING, // not yet executed
    QUEUED_FOR_EXECUTION, // queued for execution
    IN_PROGRESS, // executing
    ASYNC_ACKNOWLEDGED, // async API has acknowledged the request and is processing it
    COMPLETED, // task completed successfully
    FAILED, // task failed
    TIMED_OUT, // task timed out
    ERROR, // error occurred while processing the task
    CANCELLED // task was cancelled
} 