package com.example.api.executor.service;

import com.example.api.executor.model.ApiResponse;
import com.example.api.executor.model.TaskResponse;
import com.example.api.executor.model.TaskStatus;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class TaskMessageHandler {
    private final WebSocketService webSocketService;
    private final TaskStatusService taskStatusService;

    public TaskMessageHandler(WebSocketService webSocketService, TaskStatusService taskStatusService) {
        this.webSocketService = webSocketService;
        this.taskStatusService = taskStatusService;
    }

    @ServiceActivator(inputChannel = "taskExecutionQueue")
    public ApiResponse handleTaskMessage(Message<Integer> message) {
        try {
            Integer taskId = message.getPayload();
            
            // Create task status record
            TaskStatus taskStatus = taskStatusService.createTaskStatus(taskId);
            
            // Simulate task execution logic
            if (shouldExecuteAsync(taskId)) {
                ApiResponse response = ApiResponse.asyncResponse(taskStatus.getInvocationId());
                taskStatusService.updateTaskStatus(taskStatus.getInvocationId(), "COMPLETED", response);
                webSocketService.sendTaskCompletionNotification(taskStatus.getInvocationId(), response);
                return response;
            } else {
                TaskResponse taskResponse = new TaskResponse();
                taskResponse.setName("Sample Task");
                taskResponse.setStatus("SUCCESS");
                
                TaskResponse.TaskOutput output = new TaskResponse.TaskOutput();
                output.setProcessedItems(100);
                output.setErrors(new String[]{});
                
                taskResponse.setOutput(output);
                ApiResponse response = ApiResponse.syncResponse(taskResponse);
                taskStatusService.updateTaskStatus(taskStatus.getInvocationId(), "COMPLETED", response);
                return response;
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