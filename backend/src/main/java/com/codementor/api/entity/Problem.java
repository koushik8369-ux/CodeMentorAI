package com.codementor.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String language;

    @Column(name = "acceptance_rate")
    private String acceptanceRate;

    @Column(columnDefinition = "TEXT")
    private String examplesJson;

    @Column(columnDefinition = "TEXT")
    private String constraintsJson;

    @Column(columnDefinition = "TEXT")
    private String starterCodeJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Problem() {
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

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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
