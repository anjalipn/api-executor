package com.example.api.executor.service;

import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.entity.Task;
import com.example.api.executor.entity.TaskExecution;
import com.example.api.executor.repository.TaskRepository;
import com.example.api.executor.repository.TaskExecutionRepository;
import com.example.api.executor.exception.TaskExecutionException;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskMessageHandler {
    private final TaskExecutionService taskExecutionService;
    private final TaskExecutionRepository taskExecutionRepository;
    private final TaskRepository taskRepository;
    private final RestTemplate restTemplate;
    private final TaskLockService taskLockService;
    private final DueByService dueByService;

    @ServiceActivator(inputChannel = "taskExecutionQueue")
    public void handleTaskMessage(Message<Long> message) {
        Long taskId = message.getPayload();
        log.info("Received task message for task ID: {}", taskId);

        Task task = taskRepository.findByTaskId(taskId)
            .orElseThrow(() -> {
                log.error("Task not found for task ID: {}", taskId);
                return new TaskExecutionException("Task not found for task ID: " + taskId);
            });
        log.info("Found task: ID={}, Description={}, URL={}, Method={}", 
            task.getTaskId(), task.getTaskDescription(), task.getRequest().getUrl(), task.getRequest().getMethod());

        TaskExecution taskExecution = taskExecutionRepository.findByTaskId(taskId)
            .orElseThrow(() -> {
                log.error("Task execution not found for task ID: {}", taskId);
                return new TaskExecutionException("Task execution not found for task ID: " + taskId);
            });
        log.info("Found task execution: ID={}, Correlation ID={}", 
            taskExecution.getTaskId(), taskExecution.getCorrelationId());

        if (!taskLockService.tryLock(taskId)) {
            log.error("Could not acquire lock for task: {}", taskId);
            return;
        }
        log.info("Successfully acquired lock for task: {}", taskId);

        try {
            log.info("Starting execution of task: {}", taskId);
            taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.IN_PROGRESS);
            
            ResponseEntity<String> response = executeHttpRequest(task, taskExecution);
            String httpResponse = response.getBody();
            log.info("Received response for task {}: Status={}, Body={}", 
                taskId, response.getStatusCode(), httpResponse);

            // Update task status based on response
            if (response.getStatusCode().is2xxSuccessful()) {
                String invocationId = response.getHeaders().getFirst("Invocation-Id");
                if (invocationId != null) {
                    //asynchronous api
                    log.info("Task {} is asynchronous, received invocation ID: {}", taskId, invocationId);
                    taskExecution.setInvocationId(invocationId);
                    taskExecutionRepository.save(taskExecution);
                    taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.ASYNC_ACKNOWLEDGED, httpResponse, response.getStatusCode().value());
                } else {
                    //synchronous api
                    log.info("Task {} completed synchronously", taskId);
                    taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.COMPLETED, httpResponse, response.getStatusCode().value());
                }
            } else {
                log.error("Task {} failed with status code: {}", taskId, response.getStatusCode());
                taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.FAILED, httpResponse, response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("Error processing task {}: {}", taskId, e.getMessage(), e);
            String httpResponse = "Task processing Error: " + e.getMessage();
            taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.FAILED, httpResponse, null);
        } finally {
            taskLockService.unlock(taskId);
            log.info("Released lock for task: {}", taskId);
        }
    }

    private ResponseEntity<String> executeHttpRequest(Task task, TaskExecution taskExecution) {
        log.info("Preparing HTTP request for task {}: URL={}, Method={}", 
            task.getTaskId(), task.getRequest().getUrl(), task.getRequest().getMethod());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Correlation-ID", taskExecution.getCorrelationId());
        
        HttpEntity<String> requestEntity = new HttpEntity<>(
            task.getRequest().getData(),
            headers
        );

        log.info("Sending HTTP request for task {} with correlation ID: {}", 
            task.getTaskId(), taskExecution.getCorrelationId());

        // Schedule Due By check before making the request
        dueByService.scheduleDueBy(task.getTaskId());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                task.getRequest().getUrl(),
                HttpMethod.valueOf(task.getRequest().getMethod().name()),
                requestEntity,
                String.class
            );
            log.info("Received HTTP response for task {}: Status={}", 
                task.getTaskId(), response.getStatusCode());
            return response;
        } catch (Exception e) {
            log.error("HTTP request failed for task {}: {}", task.getTaskId(), e.getMessage(), e);
            throw e;
        }
    }
} 