package com.example.api.executor.service;

import com.example.api.executor.entity.Task;
import com.example.api.executor.entity.TaskExecution;
import com.example.api.executor.entity.TaskExecutionState;
import com.example.api.executor.exception.TaskExecutionException;
import com.example.api.executor.model.TaskType;
import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.model.HttpMethod;
import com.example.api.executor.repository.TaskExecutionRepository;
import com.example.api.executor.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskMessageHandlerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskExecutionRepository taskExecutionRepository;

    @Mock
    private TaskLockService taskLockService;

    @Mock
    private TaskExecutionService taskExecutionService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DueByService dueByService;

    @InjectMocks
    private TaskMessageHandler taskMessageHandler;

    private Long taskId;
    private Long taskExecutionId;

    @BeforeEach
    void setUp() {
        taskId = 1L;
        taskExecutionId = 1L;
    }

    private Task createTask(Long taskId) {
        Task task = new Task();
        task.setTaskId(taskId);
        task.setTaskDescription("Test Task");
        task.setState(TaskStatus.PENDING);
        task.setCreatedAt(OffsetDateTime.now());
        task.setCreatedBy("test-user");
        task.setType(TaskType.CIA_API);
        task.setPosition(1);
        
        Task.Request request = new Task.Request();
        request.setUrl("http://test.com");
        request.setMethod(HttpMethod.POST);
        request.setData("{}");
        task.setRequest(request);
        
        return task;
    }

    private TaskExecution createTaskExecution(Long taskId) {
        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setTaskId(taskId);
        taskExecution.setCorrelationId("test-correlation-id");
        taskExecution.setInvocationId("test-invocation-id");
        taskExecution.setCreatedAt(OffsetDateTime.now());
        taskExecution.setUpdatedAt(OffsetDateTime.now());
        return taskExecution;
    }

    @Test
    void handleTaskMessage_Success() {
        // Arrange
        Message<Long> message = mock(Message.class);
        when(message.getPayload()).thenReturn(taskId);

        Task task = createTask(taskId);
        TaskExecution taskExecution = createTaskExecution(taskId);
        TaskExecutionState taskExecutionState = new TaskExecutionState();
        taskExecutionState.setId(taskExecutionId);
        taskExecutionState.setTaskExecution(taskExecution);
        taskExecutionState.setState(TaskStatus.PENDING);
        taskExecutionState.setCreatedAt(OffsetDateTime.now());
        taskExecution.getStates().add(taskExecutionState);

        when(taskRepository.findByTaskId(taskId)).thenReturn(Optional.of(task));
        when(taskExecutionRepository.findByTaskId(taskId)).thenReturn(Optional.of(taskExecution));
        when(taskLockService.tryLock(taskId)).thenReturn(true);

        // Mock RestTemplate response
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Success");
        when(restTemplate.exchange(
            anyString(),
            any(org.springframework.http.HttpMethod.class),
            any(),
            eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        taskMessageHandler.handleTaskMessage(message);

        // Assert
        verify(taskRepository).findByTaskId(taskId);
        verify(taskExecutionRepository).findByTaskId(taskId);
        verify(taskExecutionService).recordTaskExecutionState(any(), eq(TaskStatus.IN_PROGRESS));
        verify(dueByService).scheduleDueBy(taskId);
        verify(taskExecutionService).recordTaskExecutionState(any(), eq(TaskStatus.COMPLETED), eq("Success"), eq(200));
    }

    @Test
    void handleTaskMessage_TaskNotFound() {
        // Arrange
        Message<Long> message = mock(Message.class);
        when(message.getPayload()).thenReturn(taskId);
        when(taskRepository.findByTaskId(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskExecutionException.class, () -> taskMessageHandler.handleTaskMessage(message));
        verify(taskRepository).findByTaskId(taskId);
        verify(taskExecutionService, never()).recordTaskExecutionState(any(), any());
        verify(dueByService, never()).scheduleDueBy(any());
    }

    @Test
    void handleTaskMessage_CannotAcquireLock() {
        // Arrange
        Message<Long> message = mock(Message.class);
        when(message.getPayload()).thenReturn(taskId);

        Task task = createTask(taskId);
        TaskExecution taskExecution = createTaskExecution(taskId);
        TaskExecutionState taskExecutionState = new TaskExecutionState();
        taskExecutionState.setId(taskExecutionId);
        taskExecutionState.setTaskExecution(taskExecution);
        taskExecutionState.setState(TaskStatus.PENDING);
        taskExecutionState.setCreatedAt(OffsetDateTime.now());
        taskExecution.getStates().add(taskExecutionState);

        when(taskRepository.findByTaskId(taskId)).thenReturn(Optional.of(task));
        when(taskExecutionRepository.findByTaskId(taskId)).thenReturn(Optional.of(taskExecution));
        when(taskLockService.tryLock(taskId)).thenReturn(false);

        // Act
        taskMessageHandler.handleTaskMessage(message);

        // Assert
        verify(taskRepository).findByTaskId(taskId);
        verify(taskExecutionRepository).findByTaskId(taskId);
        verify(taskExecutionService, never()).recordTaskExecutionState(any(), any());
        verify(dueByService, never()).scheduleDueBy(any());
    }
} 