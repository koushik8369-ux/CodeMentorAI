package com.codementor.api.dto;

import java.util.List;
import java.util.Map;

public class ProblemDto {

    private String id;
    private String title;
    private String description;
    private String difficulty;
    private String topic;
    private String language;
    private String acceptanceRate;
    private List<QuestionGenerationResponse.ExampleDto> examples;
    private List<String> constraints;
    private Map<String, String> starterCode;

    public ProblemDto() {
    }

    public ProblemDto(String id, String title, String description, String difficulty, String topic, String language) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.topic = topic;
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<QuestionGenerationResponse.ExampleDto> getExamples() {
        return examples;
    }

    public void setExamples(List<QuestionGenerationResponse.ExampleDto> examples) {
        this.examples = examples;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    public Map<String, String> getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(Map<String, String> starterCode) {
        this.starterCode = starterCode;
    }
}
