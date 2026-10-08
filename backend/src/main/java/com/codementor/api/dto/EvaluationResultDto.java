package com.codementor.api.dto;

import java.util.ArrayList;
import java.util.List;

public class EvaluationResultDto {
    private Integer score;
    private String correctness;
    private String technicalDepth;
    private String communication;
    private List<String> strengths = new ArrayList<>();
    private List<String> weaknesses = new ArrayList<>();
    private String feedback;
    private Boolean needsFollowUp = false;
    private String followUpQuestion;

    public EvaluationResultDto() {
    }

    public EvaluationResultDto(Integer score, String correctness, String technicalDepth,
                               String communication, List<String> strengths, List<String> weaknesses,
                               String feedback, Boolean needsFollowUp, String followUpQuestion) {
        this.score = score;
        this.correctness = correctness;
        this.technicalDepth = technicalDepth;
        this.communication = communication;
        this.strengths = strengths != null ? strengths : new ArrayList<>();
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
        this.feedback = feedback;
        this.needsFollowUp = needsFollowUp != null ? needsFollowUp : false;
        this.followUpQuestion = followUpQuestion;
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

    public String getTechnicalDepth() {
        return technicalDepth;
    }

    public void setTechnicalDepth(String technicalDepth) {
        this.technicalDepth = technicalDepth;
    }

    public String getCommunication() {
        return communication;
    }

    public void setCommunication(String communication) {
        this.communication = communication;
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

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Boolean getNeedsFollowUp() {
        return needsFollowUp;
    }

    public void setNeedsFollowUp(Boolean needsFollowUp) {
        this.needsFollowUp = needsFollowUp;
    }

    public String getFollowUpQuestion() {
        return followUpQuestion;
    }

    public void setFollowUpQuestion(String followUpQuestion) {
        this.followUpQuestion = followUpQuestion;
    }
}
