package com.example.api.executor.service;

import com.example.api.executor.model.ApiResponse;

public interface TaskService {
    ApiResponse executeTask(Integer taskId);
} 