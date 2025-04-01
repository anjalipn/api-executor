package com.example.api.executor.model;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class RunbookResponse {
    private Long runbookId;
    private String runbookDescription;
    private String changeRecord;
    private String creator;
    private OffsetDateTime scheduledTime;
    private RunbookState state;
    private RunbookCreationMethodType creationMethod;
    private Boolean isScheduled;
    private OffsetDateTime createdAt;
    private String createdBy;
    private List<TaskResponse> tasks;

    @Data
    public static class TaskResponse {
        private Long taskId;
        private Integer position;
        private TaskType type;
        private String taskDescription;
        private TaskStatus state;
        private TaskRequest request;
        private OffsetDateTime createdAt;
        private String createdBy;
    }

    @Data
    public static class TaskRequest {
        private String url;
        private HttpMethod method;
        private String data;
    }
} 