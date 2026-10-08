package com.codementor.api.dto;

import java.util.ArrayList;
import java.util.List;

public class GeneratedMockQuestionDto {
    private String question;
    private List<String> expectedConcepts = new ArrayList<>();
    private String difficulty;

    public GeneratedMockQuestionDto() {
    }

    public GeneratedMockQuestionDto(String question, List<String> expectedConcepts, String difficulty) {
        this.question = question;
        this.expectedConcepts = expectedConcepts != null ? expectedConcepts : new ArrayList<>();
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getExpectedConcepts() {
        return expectedConcepts;
    }

    public void setExpectedConcepts(List<String> expectedConcepts) {
        this.expectedConcepts = expectedConcepts;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
