package com.example.api.executor.service;

import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.repository.TaskRepository;
import com.example.api.executor.repository.TaskExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.handler.DelayHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DueByService {
    private final MessageChannel dueByInputChannel;
    private final TaskRepository taskRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final TaskExecutionService taskExecutionService;
    
    @Value("${task.timeout.seconds:300}")
    private int taskTimeoutSeconds;

    public void scheduleDueBy(Long taskId) {
        if (taskRepository.findByTaskId(taskId).isPresent()) {
            Message<Long> message = MessageBuilder.withPayload(taskId)
                .setHeader("taskId", taskId)
                .build();
            dueByInputChannel.send(message);
            log.info("Scheduled Due By check for task ID: {}", taskId);
        }
    }

    @ServiceActivator(inputChannel = "dueByInputChannel")
    @Bean
    public DelayHandler delayHandler() {
        DelayHandler delayHandler = new DelayHandler("dueByGroup");
        delayHandler.setDefaultDelay(taskTimeoutSeconds * 1000L);
        delayHandler.setOutputChannelName("dueByOutputChannel");
        return delayHandler;
    }

    @ServiceActivator(inputChannel = "dueByOutputChannel")
    @Transactional
    public void handleDueBy(Message<Long> message) {
        Long taskId = message.getPayload();
        log.info("Processing Due By check for task ID: {}", taskId);
        
        if (!taskRepository.isTaskInProgress(taskId)) {
            log.info("Task {} has timed out", taskId);
            taskExecutionRepository.findByTaskId(taskId).ifPresent(taskExecution -> 
                taskExecutionService.recordTaskExecutionState(
                    taskExecution,
                    TaskStatus.TIMED_OUT,
                    "Task execution timed out",
                    null
                )
            );
        }
    }
} 