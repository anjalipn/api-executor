package com.example.api.executor.service;

import com.example.api.executor.model.TaskExecutionResponse;
import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.model.RunbookState;
import com.example.api.executor.entity.Task;
import com.example.api.executor.entity.TaskExecution;
import com.example.api.executor.entity.TaskExecutionState;
import com.example.api.executor.repository.TaskRepository;
import com.example.api.executor.repository.TaskExecutionRepository;
import com.example.api.executor.exception.TaskExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final QueueChannel taskExecutionQueue;

    public TaskExecutionResponse adHocLaunch(Long taskId) {
        log.info("Starting ad-hoc launch for task ID: {}", taskId);
        
        Task task = findTaskById(taskId);
        log.debug("Found task: ID={}, Description={}, Current Status={}", 
            task.getTaskId(), task.getTaskDescription(), task.getState());
        
        validateTaskStatus(task);
        validateRunbookStatus(task);
        
        // Create initial task execution
        TaskExecution taskExecution = createTaskExecution(task);
        log.info("Created task execution: ID={}, Correlation ID={}", 
            taskExecution.getTaskId(), taskExecution.getCorrelationId());
        
        // Send to execution queue
        sendToExecutionQueue(task);
        
        log.info("Successfully queued task ID: {} for execution", taskId);
        return createResponse(taskExecution);
    }

    private Task findTaskById(Long taskId) {
        log.debug("Looking up task with ID: {}", taskId);
        return taskRepository.findByTaskId(taskId)
            .orElseThrow(() -> {
                log.error("Task not found with ID: {}", taskId);
                return new RuntimeException("Task not found: " + taskId);
            });
    }

    private void validateTaskStatus(Task task) {
        log.info("Validating task status for task ID: {}, Current status: {}", 
            task.getTaskId(), task.getState());
        if (task.getState() != null && task.getState() != TaskStatus.PENDING) {
            log.warn("Task {} is not in PENDING status. Current status: {}", 
                task.getTaskId(), task.getState());
            throw new RuntimeException("Task is already in progress or completed");
        }
        log.debug("Task status validation passed for task ID: {}", task.getTaskId());
    }

    private void validateRunbookStatus(Task task) {
        log.debug("Validating runbook status for task ID: {}", task.getTaskId());
        if (task.getRunbook() == null) {
            log.warn("Task {} is not associated with any runbook", task.getTaskId());
            throw new TaskExecutionException("Task is not associated with any runbook");
        }

        log.debug("Task {} is associated with runbook ID: {}, Status: {}", 
            task.getTaskId(), task.getRunbook().getRunbookId(), task.getRunbook().getState());
        
        if (task.getRunbook().getState() != RunbookState.READY) {
            log.warn("Runbook {} is not in READY status. Current status: {}", 
                task.getRunbook().getRunbookId(), task.getRunbook().getState());
            throw new TaskExecutionException("Runbook is not in READY status. Current status: " + task.getRunbook().getState());
        }
        log.info("Runbook status validation passed for task ID: {}", task.getTaskId());
    }

    @Transactional
    private TaskExecution createTaskExecution(Task task) {
        
        taskRepository.updateTaskStatus(task.getTaskId(), TaskStatus.QUEUED_FOR_EXECUTION);
        log.info("Updated task status to QUEUED_FOR_EXECUTION for task ID: {}", task.getTaskId());

        log.info("Creating task execution for task ID: {}", task.getTaskId());
        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setTaskId(task.getTaskId());
        taskExecution.setCorrelationId(UUID.randomUUID().toString());
        log.info("Created task execution with correlation ID: {}", taskExecution.getCorrelationId());

        TaskExecutionState initialState = new TaskExecutionState();
        initialState.setTaskExecution(taskExecution);
        initialState.setState(TaskStatus.QUEUED_FOR_EXECUTION);
        taskExecution.getStates().add(initialState);
        log.info("Added initial state QUEUED_FOR_EXECUTION to task execution");

        TaskExecution savedExecution = taskExecutionRepository.save(taskExecution);
        log.info("Saved task execution: ID={}, Correlation ID={}", 
            savedExecution.getTaskId(), savedExecution.getCorrelationId());
        return savedExecution;
    }

    private void sendToExecutionQueue(Task task) {
        log.info("Sending task ID: {} to execution queue", task.getTaskId());
        boolean sent = taskExecutionQueue.send(MessageBuilder.withPayload(task).build());
        if (sent) {
            log.info("Successfully sent task ID: {} to execution queue", task.getTaskId());
        } else {
            log.error("Failed to send task ID: {} to execution queue", task.getTaskId());
        }
    }

    private TaskExecutionResponse createResponse(TaskExecution taskExecution) {
        log.info("Creating response for task execution ID: {}", taskExecution.getTaskId());
        TaskExecutionResponse response = new TaskExecutionResponse();
        response.setTaskId(taskExecution.getTaskId());
        response.setCorrelationId(taskExecution.getCorrelationId());
        response.setState(TaskStatus.QUEUED_FOR_EXECUTION);
        log.info("Created response with correlation ID: {}", response.getCorrelationId());
        return response;
    }
}
