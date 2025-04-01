package com.example.api.executor.service;

import com.example.api.executor.entity.Task;
import com.example.api.executor.entity.TaskExecution;
import com.example.api.executor.repository.TaskRepository;
import com.example.api.executor.repository.TaskExecutionRepository;
import com.example.api.executor.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DueByServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskExecutionRepository taskExecutionRepository;

    @Mock
    private TaskExecutionService taskExecutionService;

    @Mock
    private MessageChannel dueByInputChannel;

    @InjectMocks
    private DueByService dueByService;

    private PodamFactory podamFactory;

    @BeforeEach
    void setUp() {
        podamFactory = new PodamFactoryImpl();
    }

    @Test
    void scheduleDueBy_Success() {
        // Arrange
        Long taskId = 1L;
        Task task = podamFactory.manufacturePojo(Task.class);

        when(taskRepository.findByTaskId(taskId)).thenReturn(Optional.of(task));

        // Act
        dueByService.scheduleDueBy(taskId);

        // Assert
        verify(taskRepository).findByTaskId(taskId);
        verify(dueByInputChannel).send(any(Message.class));
    }

    @Test
    void scheduleDueBy_TaskNotFound() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.findByTaskId(taskId)).thenReturn(Optional.empty());

        // Act
        dueByService.scheduleDueBy(taskId);

        // Assert
        verify(taskRepository).findByTaskId(taskId);
        verify(dueByInputChannel, never()).send(any(Message.class));
    }

    @Test
    void handleDueBy_TaskInProgress() {
        // Arrange
        Long taskId = 1L;
        Message<Long> message = mock(Message.class);
        when(message.getPayload()).thenReturn(taskId);
        when(taskRepository.isTaskInProgress(taskId)).thenReturn(true);

        // Act
        dueByService.handleDueBy(message);

        // Assert
        verify(taskRepository).isTaskInProgress(taskId);
        verify(taskExecutionService, never()).recordTaskExecutionState(any(), any(), any(), any());
    }

    @Test
    void handleDueBy_TaskNotInProgress() {
        // Arrange
        Long taskId = 1L;
        Message<Long> message = mock(Message.class);
        TaskExecution taskExecution = podamFactory.manufacturePojo(TaskExecution.class);

        when(message.getPayload()).thenReturn(taskId);
        when(taskRepository.isTaskInProgress(taskId)).thenReturn(false);
        when(taskExecutionRepository.findByTaskId(taskId)).thenReturn(Optional.of(taskExecution));

        // Act
        dueByService.handleDueBy(message);

        // Assert
        verify(taskRepository).isTaskInProgress(taskId);
        verify(taskExecutionRepository).findByTaskId(taskId);
        verify(taskExecutionService).recordTaskExecutionState(
            eq(taskExecution),
            eq(TaskStatus.TIMED_OUT),
            eq("Task execution timed out"),
            eq(null)
        );
    }
} 