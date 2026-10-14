package com.codementor.api.entity;

import java.time.LocalDateTime;

public class Problem {

    private Long id;
    private String slug;
    private String title;
    private String description;
    private String difficulty;
    private String topic;
    private String language;
    private String acceptanceRate;
    private String examplesJson;
    private String constraintsJson;
    private String starterCodeJson;
    private LocalDateTime createdAt;

    public Problem() {
        this.createdAt = LocalDateTime.now();
    }

    public Problem(String slug, String title, String description, String difficulty, String topic, String language) {
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.topic = topic;
        this.language = language;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(String acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public String getExamplesJson() {
        return examplesJson;
    }

    public void setExamplesJson(String examplesJson) {
        this.examplesJson = examplesJson;
    }

    public String getConstraintsJson() {
        return constraintsJson;
    }

    public void setConstraintsJson(String constraintsJson) {
        this.constraintsJson = constraintsJson;
    }

    public String getStarterCodeJson() {
        return starterCodeJson;
    }

    public void setStarterCodeJson(String starterCodeJson) {
        this.starterCodeJson = starterCodeJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
