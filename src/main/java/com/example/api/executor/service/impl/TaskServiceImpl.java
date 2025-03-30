package com.example.api.executor.service.impl;

import com.example.api.executor.model.ApiResponse;
import com.example.api.executor.model.TaskResponse;
import com.example.api.executor.service.TaskService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {
    
    @Override
    public ApiResponse executeTask(Integer taskId) {
        // Simulate task execution logic
        // In a real application, this would be determined by the actual task execution
        if (shouldExecuteAsync(taskId)) {
            return ApiResponse.asyncResponse(UUID.randomUUID().toString());
        } else {
            TaskResponse taskResponse = new TaskResponse();
            taskResponse.setName("Sample Task");
            taskResponse.setStatus("SUCCESS");
            
            TaskResponse.TaskOutput output = new TaskResponse.TaskOutput();
            output.setProcessedItems(100);
            output.setErrors(new String[]{});
            
            taskResponse.setOutput(output);
            return ApiResponse.syncResponse(taskResponse);
        }
    }

    private boolean shouldExecuteAsync(Integer taskId) {
        // This is a placeholder for actual task execution logic
        // In a real application, this would be determined by:
        // 1. Task configuration
        // 2. Task type
        // 3. Task complexity
        // 4. System load
        // 5. Business rules
        return taskId % 2 == 1;
    }
} 