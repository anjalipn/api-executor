package com.example.api.executor.model;

import lombok.Data;
import java.util.List;

@Data
public class ErrorResponse {
    private List<ErrorInfo> errorInfo;
    private ExtendedDetails extendedDetails;
    private String correlationId;

    @Data
    public static class ErrorInfo {
        private String code;
        private List<String> causes;
        private ErrorSourceDetails sourceDetails;
    }

    @Data
    public static class ErrorSourceDetails {
        private String applicationName;
        private String layer;
    }

    @Data
    public static class ExtendedDetails {
        private String description;
    }
} 