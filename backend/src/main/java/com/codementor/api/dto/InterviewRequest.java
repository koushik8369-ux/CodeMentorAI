package com.codementor.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class InterviewRequest {

    private Long userId;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotBlank(message = "Type is required")
    private String type;

    private List<String> topics;

    private Integer questionCount;

    public InterviewRequest() {
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

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}
