package com.example.api.executor.controller;

import com.example.api.executor.model.ApiResponse;
import com.example.api.executor.model.TaskRequest;
import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.service.TaskService;
import com.example.api.executor.service.TaskStatusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private QueueChannel taskExecutionQueue;

    @Autowired
    private TaskStatusService taskStatusService;

    @PostMapping("/run-task")
    public ResponseEntity<ApiResponse> runTask(@Valid @RequestBody TaskRequest request) {
        // Send the taskId to the queue
        taskExecutionQueue.send(MessageBuilder.withPayload(request.getTaskId()).build());
        
        // For now, return a default async response
        // In a real application, you might want to wait for the actual response
        return ResponseEntity.ok(ApiResponse.asyncResponse("Task queued for processing"));
    }

    @GetMapping("/task-status/{invocationId}")
    public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable String invocationId) {
        return ResponseEntity.ok(taskStatusService.getTaskStatus(invocationId));
    }
} 