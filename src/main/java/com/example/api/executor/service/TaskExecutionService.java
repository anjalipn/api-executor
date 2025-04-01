package com.example.api.executor.service;

import com.example.api.executor.model.TaskExecutionResponse;
import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.entity.TaskExecution;
import com.example.api.executor.entity.TaskExecutionState;
import com.example.api.executor.repository.TaskRepository;
import com.example.api.executor.repository.TaskExecutionStateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecutionService {
    private final TaskRepository taskRepository;
    private final TaskExecutionStateRepository taskExecutionStateRepository;
    private final WebSocketService webSocketService;

    /**
     * Records a new task execution state and updates the task status.
     * This ensures that whenever a new execution state is created, the task status is also updated.
     */
    @Transactional
    public void recordTaskExecutionState(TaskExecution taskExecution, TaskStatus status) {
        recordTaskExecutionState(taskExecution, status, null, null);
    }

    /**
     * Records a new task execution state with response and updates the task status.
     * This ensures that whenever a new execution state is created, the task status is also updated.
     */
    @Transactional
    public void recordTaskExecutionState(TaskExecution taskExecution, TaskStatus status, String httpResponse, Integer httpStatusCode) {
        log.info("Recording task execution state with response: Task ID={}, Status={}, HTTP Status={}", 
            taskExecution.getTaskId(), status, httpStatusCode);

        // Update task status
        taskRepository.updateTaskStatus(taskExecution.getTaskId(), status);
        
        // Update start time if status is IN_PROGRESS
        if (status == TaskStatus.IN_PROGRESS) {
            taskRepository.updateTaskStartTime(taskExecution.getTaskId(), OffsetDateTime.now());
            log.info("Updated task start time for task ID: {}", taskExecution.getTaskId());
        }
        
        // Update end time if status is COMPLETED or FAILED or TIMED_OUT
        if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED|| status == TaskStatus.TIMED_OUT) {
            taskRepository.updateTaskEndTime(taskExecution.getTaskId(), OffsetDateTime.now());
            log.info("Updated task end time for task ID: {}", taskExecution.getTaskId());
        }
        
        log.info("Updated task status in database to: {}", status);

        // Then create and save the task execution state with response
        TaskExecutionState state = new TaskExecutionState();
        state.setTaskExecution(taskExecution);
        state.setState(status);
        state.setHttpResponse(httpResponse);
        state.setHttpStatusCode(httpStatusCode);
        taskExecutionStateRepository.save(state);
        log.info("Saved task execution state with response to database");

        // Finally notify via WebSocket
        notifyTaskStatus(taskExecution, status);
    }

    private void notifyTaskStatus(TaskExecution taskExecution, TaskStatus status) {
        log.info("Notifying task status: Task ID={}, Status={}", 
            taskExecution.getTaskId(), status);

        TaskExecutionResponse response = new TaskExecutionResponse();
        response.setTaskId(taskExecution.getTaskId());
        response.setCorrelationId(taskExecution.getCorrelationId());
        response.setInvocationId(taskExecution.getInvocationId());
        response.setState(status);
        webSocketService.sendTaskCompletionNotification(response);
        log.info("Sent task status notification via WebSocket");
    }
} 