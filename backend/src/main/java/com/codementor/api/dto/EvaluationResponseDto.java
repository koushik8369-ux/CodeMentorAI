package com.codementor.api.dto;

import java.util.ArrayList;
import java.util.List;

public class EvaluationResponseDto {
    private Integer score;
    private String correctness;
    private String codeQuality;
    private String timeComplexity;
    private String spaceComplexity;
    private List<String> strengths = new ArrayList<>();
    private List<String> weaknesses = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private String overallFeedback;

    public EvaluationResponseDto() {
    }

    public EvaluationResponseDto(Integer score, String correctness, String codeQuality,
                                 String timeComplexity, String spaceComplexity,
                                 List<String> strengths, List<String> weaknesses,
                                 List<String> recommendations, String overallFeedback) {
        this.score = score;
        this.correctness = correctness;
        this.codeQuality = codeQuality;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
        this.strengths = strengths != null ? strengths : new ArrayList<>();
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
        this.overallFeedback = overallFeedback;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getCorrectness() {
        return correctness;
    }

    public void setCorrectness(String correctness) {
        this.correctness = correctness;
    }

    public String getCodeQuality() {
        return codeQuality;
    }

    public void setCodeQuality(String codeQuality) {
        this.codeQuality = codeQuality;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
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
}
