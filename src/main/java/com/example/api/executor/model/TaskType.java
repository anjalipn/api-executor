package com.example.api.executor.model;

public enum TaskType {
    CIA_API("CIA API");

    private final String value;

    TaskType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
} 