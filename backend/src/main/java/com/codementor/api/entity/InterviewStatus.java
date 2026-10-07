package com.codementor.api.entity;

public enum InterviewStatus {
    IN_PROGRESS,
    COMPLETED;

    public static InterviewStatus fromString(String value) {
        if (value == null) return IN_PROGRESS;
        for (InterviewStatus s : values()) {
            if (s.name().equalsIgnoreCase(value.trim()) || s.name().replace("_", " ").equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        return IN_PROGRESS;
    }
}
