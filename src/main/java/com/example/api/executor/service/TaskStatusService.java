package com.example.api.executor.service;

import com.example.api.executor.model.ApiResponse;
import com.example.api.executor.model.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskStatusService {
    private final JdbcTemplate jdbcTemplate;

    public TaskStatusService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS TASK_STATUS (
                invocation_id VARCHAR(36) PRIMARY KEY,
                task_id INTEGER NOT NULL,
                status VARCHAR(20) NOT NULL,
                result CLOB,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP NOT NULL
            )
        """);
    }

    public TaskStatus createTaskStatus(Integer taskId) {
        String invocationId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update("""
            INSERT INTO TASK_STATUS (invocation_id, task_id, status, created_at, updated_at)
            VALUES (?, ?, 'PENDING', ?, ?)
        """, invocationId, taskId, now, now);

        TaskStatus status = new TaskStatus();
        status.setInvocationId(invocationId);
        status.setTaskId(taskId);
        status.setStatus("PENDING");
        status.setCreatedAt(now);
        status.setUpdatedAt(now);
        return status;
    }

    public void updateTaskStatus(String invocationId, String status, ApiResponse result) {
        jdbcTemplate.update("""
            UPDATE TASK_STATUS 
            SET status = ?, result = ?, updated_at = ?
            WHERE invocation_id = ?
        """, status, result, LocalDateTime.now(), invocationId);
    }

    public TaskStatus getTaskStatus(String invocationId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT * FROM TASK_STATUS WHERE invocation_id = ?", 
            invocationId
        );

        TaskStatus status = new TaskStatus();
        status.setInvocationId((String) row.get("invocation_id"));
        status.setTaskId((Integer) row.get("task_id"));
        status.setStatus((String) row.get("status"));
        status.setResult((ApiResponse) row.get("result"));
        status.setCreatedAt(((java.sql.Timestamp) row.get("created_at")).toLocalDateTime());
        status.setUpdatedAt(((java.sql.Timestamp) row.get("updated_at")).toLocalDateTime());
        return status;
    }
} 