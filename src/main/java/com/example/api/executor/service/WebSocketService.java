package com.example.api.executor.service;

import com.example.api.executor.model.ApiResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTaskCompletionNotification(String invocationId, ApiResponse response) {
        messagingTemplate.convertAndSend("/topic/task/" + invocationId, response);
    }
} 