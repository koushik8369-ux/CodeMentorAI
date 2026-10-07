package com.codementor.api.dto;

import jakarta.validation.constraints.NotBlank;

public class StartInterviewRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotBlank(message = "Language is required")
    private String language;

    public StartInterviewRequest() {
    }

    public StartInterviewRequest(String topic, String difficulty, String language) {
        this.topic = topic;
        this.difficulty = difficulty;
        this.language = language;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
