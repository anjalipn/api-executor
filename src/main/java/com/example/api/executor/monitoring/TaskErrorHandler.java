package com.example.api.executor.monitoring;

import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class TaskErrorHandler implements MessageHandler {
    private final LoggingHandler loggingHandler;

    public TaskErrorHandler() {
        this.loggingHandler = new LoggingHandler(LoggingHandler.Level.ERROR);
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            loggingHandler.handleMessage(message);
        } catch (Exception e) {
            throw new MessagingException(message, "Error handling message", e);
        }
    }
} 