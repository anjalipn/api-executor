package com.example.api.executor.service;

import com.example.api.executor.entity.Task;
import com.example.api.executor.entity.TaskExecution;
import com.example.api.executor.entity.TaskExecutionState;
import com.example.api.executor.repository.TaskExecutionStateRepository;
import com.example.api.executor.repository.TaskRepository;
import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.model.TaskExecutionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskExecutionServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskExecutionStateRepository taskExecutionStateRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private TaskExecutionService taskExecutionService;

    private TaskExecution createTaskExecution() {
        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setTaskId(1L);
        taskExecution.setCorrelationId("test-correlation-id");
        taskExecution.setInvocationId("test-invocation-id");
        taskExecution.setCreatedAt(OffsetDateTime.now());
        taskExecution.setUpdatedAt(OffsetDateTime.now());
        taskExecution.setStates(new ArrayList<>());
        return taskExecution;
    }

    private TaskExecutionState createTaskExecutionState(TaskExecution taskExecution, TaskStatus status) {
        TaskExecutionState state = new TaskExecutionState();
        state.setId(1L);
        state.setTaskExecution(taskExecution);
        state.setState(status);
        state.setCreatedAt(OffsetDateTime.now());
        return state;
    }

    @Test
    void recordTaskExecutionState_Success() {
        // Arrange
        TaskExecution taskExecution = createTaskExecution();
        TaskExecutionState taskExecutionState = createTaskExecutionState(taskExecution, TaskStatus.IN_PROGRESS);

        when(taskExecutionStateRepository.save(any())).thenReturn(taskExecutionState);

        // Act
        taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.IN_PROGRESS);

        // Assert
        verify(taskRepository).updateTaskStatus(eq(taskExecution.getTaskId()), eq(TaskStatus.IN_PROGRESS));
        verify(taskRepository).updateTaskStartTime(eq(taskExecution.getTaskId()), any());
        verify(taskExecutionStateRepository).save(any());
        verify(webSocketService).sendTaskCompletionNotification(any(TaskExecutionResponse.class));
    }

    @Test
    void recordTaskExecutionState_WithResponseBody() {
        // Arrange
        TaskExecution taskExecution = createTaskExecution();
        TaskExecutionState taskExecutionState = createTaskExecutionState(taskExecution, TaskStatus.COMPLETED);
        String responseBody = "Test response";
        Integer statusCode = 200;

        when(taskExecutionStateRepository.save(any())).thenReturn(taskExecutionState);

        // Act
        taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.COMPLETED, responseBody, statusCode);

        // Assert
        verify(taskRepository).updateTaskStatus(eq(taskExecution.getTaskId()), eq(TaskStatus.COMPLETED));
        verify(taskRepository).updateTaskEndTime(eq(taskExecution.getTaskId()), any());
        verify(taskExecutionStateRepository).save(any());
        verify(webSocketService).sendTaskCompletionNotification(any(TaskExecutionResponse.class));
    }

    @Test
    void recordTaskExecutionState_Exception() {
        // Arrange
        TaskExecution taskExecution = createTaskExecution();
        when(taskExecutionStateRepository.save(any())).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            taskExecutionService.recordTaskExecutionState(taskExecution, TaskStatus.IN_PROGRESS));
        verify(taskExecutionStateRepository).save(any());
        verify(webSocketService, never()).sendTaskCompletionNotification(any(TaskExecutionResponse.class));
    }
} 