package com.example.api.executor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.api.executor.model.RunbookState;
import com.example.api.executor.model.RunbookCreationMethodType;

@Entity
@Getter
@Setter
@Table(name = "runbooks")
public class Runbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "runbook_id")
    private Long runbookId;

    @Column(name = "runbook_description", columnDefinition = "TEXT")
    private String runbookDescription;

    @Column(name = "change_record")
    private String changeRecord;

    @Column(name = "creator")
    private String creator;

    @Column(name = "scheduled_time")
    private OffsetDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunbookState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunbookCreationMethodType creationMethod;

    @Column(name = "is_scheduled", nullable = false)
    private Boolean isScheduled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @OneToMany(mappedBy = "runbook", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Task> tasks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        createdBy = creator;
    }
} 