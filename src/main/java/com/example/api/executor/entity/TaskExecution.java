package com.example.api.executor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "TASK_EXECUTION")
public class TaskExecution {
    @Id
    @Column(name = "TASK_ID")
    private Long taskId;

    @Column(name = "CORRELATION_ID", nullable = false)
    private String correlationId;

    @Column(name = "INVOCATION_ID", nullable = false)
    private String invocationId;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "START_TIME")
    private OffsetDateTime startTime;

    @Column(name = "END_TIME")
    private OffsetDateTime endTime;

    @OneToMany(mappedBy = "taskExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<TaskExecutionState> states = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
} 