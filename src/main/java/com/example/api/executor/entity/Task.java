package com.example.api.executor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

import com.example.api.executor.model.TaskType;
import com.example.api.executor.model.TaskStatus;
import com.example.api.executor.model.HttpMethod;

@Entity
@Getter
@Setter
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "task_type")
    private TaskType type;

    @Column(name = "task_description", nullable = false, columnDefinition = "TEXT")
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private TaskStatus state;

    @Embedded
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runbook_id", referencedColumnName = "runbook_id", nullable = false)
    private Runbook runbook;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    @Embeddable
    @Getter
    @Setter
    public static class Request {
        @Column(name = "request_url", nullable = false)
        private String url;

        @Enumerated(EnumType.STRING)
        @Column(name = "request_method", nullable = false)
        private HttpMethod method;

        @Column(name = "request_data", columnDefinition = "json")
        private String data;
    }
} 