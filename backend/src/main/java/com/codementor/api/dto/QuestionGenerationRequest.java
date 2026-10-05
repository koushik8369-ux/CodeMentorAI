package com.codementor.api.dto;

import jakarta.validation.constraints.NotBlank;

public class QuestionGenerationRequest {

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotBlank(message = "Topic is required")
    private String topic;

    public QuestionGenerationRequest() {
    }

    public QuestionGenerationRequest(String role, String language, String difficulty, String topic) {
        this.role = role;
        this.language = language;
        this.difficulty = difficulty;
        this.topic = topic;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
