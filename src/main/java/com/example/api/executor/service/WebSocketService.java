package com.example.api.executor.service;

import com.example.api.executor.model.TaskExecutionResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTaskCompletionNotification(TaskExecutionResponse response) {
        messagingTemplate.convertAndSend("/topic/task/" + response.getTaskId(), response);
    }
} 