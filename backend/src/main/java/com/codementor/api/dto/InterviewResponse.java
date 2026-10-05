package com.codementor.api.dto;

import java.time.LocalDateTime;

public class InterviewResponse {

    private Long id;
    private Long userId;
    private String role;
    private String language;
    private String difficulty;
    private String type;
    private Integer score;
    private String status;
    private LocalDateTime createdAt;

    public InterviewResponse() {
    }

    public InterviewResponse(Long id, Long userId, String role, String language, String difficulty,
                             String type, Integer score, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.language = language;
        this.difficulty = difficulty;
        this.type = type;
        this.score = score;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
