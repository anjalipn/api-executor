package com.example.api.executor.entity;

import com.example.api.executor.model.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "TASK_EXECUTION_STATE")
public class TaskExecutionState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASK_ID", nullable = false)
    private TaskExecution taskExecution;

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_STATE", nullable = false)
    private TaskStatus state;

    @Column(name = "HTTP_RESPONSE", columnDefinition = "CLOB")
    private String httpResponse;

    @Column(name = "HTTP_STATUS_CODE")
    private Integer httpStatusCode;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
} 