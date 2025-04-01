package com.example.api.executor.controller;

import com.example.api.executor.model.TaskExecutionResponse;
import com.example.api.executor.model.TaskRequest;
import com.example.api.executor.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskLaunchController {
    private final TaskService taskService;

    public TaskLaunchController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/run")
    public ResponseEntity<TaskExecutionResponse> runTask(@Valid @RequestBody TaskRequest request) {
        TaskExecutionResponse response = taskService.adHocLaunch(request.getTaskId());
        return ResponseEntity.ok(response);
    }
} 