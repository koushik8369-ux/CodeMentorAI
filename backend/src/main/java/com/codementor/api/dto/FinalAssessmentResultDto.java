package com.codementor.api.dto;

import java.util.ArrayList;
import java.util.List;

public class FinalAssessmentResultDto {
    private Integer overallScore;
    private Integer technicalKnowledge;
    private Integer problemSolving;
    private Integer communication;
    private Integer confidence;
    private List<String> strengths = new ArrayList<>();
    private List<String> weaknesses = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private String overallFeedback;
    private String readinessLevel;

    public FinalAssessmentResultDto() {
    }

    public FinalAssessmentResultDto(Integer overallScore, Integer technicalKnowledge, Integer problemSolving,
                                    Integer communication, Integer confidence, List<String> strengths,
                                    List<String> weaknesses, List<String> recommendations,
                                    String overallFeedback, String readinessLevel) {
        this.overallScore = overallScore;
        this.technicalKnowledge = technicalKnowledge;
        this.problemSolving = problemSolving;
        this.communication = communication;
        this.confidence = confidence;
        this.strengths = strengths != null ? strengths : new ArrayList<>();
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
        this.overallFeedback = overallFeedback;
        this.readinessLevel = readinessLevel;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getTechnicalKnowledge() {
        return technicalKnowledge;
    }

    public void setTechnicalKnowledge(Integer technicalKnowledge) {
        this.technicalKnowledge = technicalKnowledge;
    }

    public Integer getProblemSolving() {
        return problemSolving;
    }

    public void setProblemSolving(Integer problemSolving) {
        this.problemSolving = problemSolving;
    }

    public Integer getCommunication() {
        return communication;
    }

    public void setCommunication(Integer communication) {
        this.communication = communication;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public String getReadinessLevel() {
        return readinessLevel;
    }

    public void setReadinessLevel(String readinessLevel) {
        this.readinessLevel = readinessLevel;
    }
}
