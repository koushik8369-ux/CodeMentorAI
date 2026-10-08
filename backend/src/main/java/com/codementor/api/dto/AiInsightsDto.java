package com.codementor.api.dto;

import java.util.ArrayList;
import java.util.List;

public class AiInsightsDto {
    private String overallAssessment;
    private List<String> strongTopics = new ArrayList<>();
    private List<String> weakTopics = new ArrayList<>();
    private List<String> recommendedTopics = new ArrayList<>();
    private List<AiActionPlanDto> actionPlan = new ArrayList<>();
    private String nextDifficulty;
    private String summary;

    public AiInsightsDto() {
    }

    public AiInsightsDto(String overallAssessment, List<String> strongTopics, List<String> weakTopics,
                         List<String> recommendedTopics, List<AiActionPlanDto> actionPlan,
                         String nextDifficulty, String summary) {
        this.overallAssessment = overallAssessment;
        this.strongTopics = strongTopics != null ? strongTopics : new ArrayList<>();
        this.weakTopics = weakTopics != null ? weakTopics : new ArrayList<>();
        this.recommendedTopics = recommendedTopics != null ? recommendedTopics : new ArrayList<>();
        this.actionPlan = actionPlan != null ? actionPlan : new ArrayList<>();
        this.nextDifficulty = nextDifficulty;
        this.summary = summary;
    }

    public String getOverallAssessment() {
        return overallAssessment;
    }

    public void setOverallAssessment(String overallAssessment) {
        this.overallAssessment = overallAssessment;
    }

    public List<String> getStrongTopics() {
        return strongTopics;
    }

    public void setStrongTopics(List<String> strongTopics) {
        this.strongTopics = strongTopics;
    }

    public List<String> getWeakTopics() {
        return weakTopics;
    }

    public void setWeakTopics(List<String> weakTopics) {
        this.weakTopics = weakTopics;
    }

    public List<String> getRecommendedTopics() {
        return recommendedTopics;
    }

    public void setRecommendedTopics(List<String> recommendedTopics) {
        this.recommendedTopics = recommendedTopics;
    }

    public List<AiActionPlanDto> getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(List<AiActionPlanDto> actionPlan) {
        this.actionPlan = actionPlan;
    }

    public String getNextDifficulty() {
        return nextDifficulty;
    }

    public void setNextDifficulty(String nextDifficulty) {
        this.nextDifficulty = nextDifficulty;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
