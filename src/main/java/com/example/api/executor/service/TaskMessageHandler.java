package com.example.api.executor.service;

import com.example.api.executor.model.ApiResponse;
import com.example.api.executor.model.TaskResponse;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskMessageHandler {

    @ServiceActivator(inputChannel = "taskQueue")
    public ApiResponse handleTaskMessage(Message<Integer> message) {
        try {
            Integer taskId = message.getPayload();
            
            // Simulate task execution logic
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
        } catch (Exception e) {
            // Log the error and rethrow
            throw new RuntimeException("Error processing task: " + e.getMessage(), e);
        }
    }

    private boolean shouldExecuteAsync(Integer taskId) {
        // This is a placeholder for actual task execution logic
        return taskId % 2 == 1;
    }
} 