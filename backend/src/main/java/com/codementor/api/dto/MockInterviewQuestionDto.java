package com.codementor.api.dto;

import com.codementor.api.entity.MockInterviewQuestion;
import java.time.LocalDateTime;

public class MockInterviewQuestionDto {
    private Long id;
    private int roundNumber;
    private String question;
    private String userAnswer;
    private Integer score;
    private String correctness;
    private String technicalDepth;
    private String communication;
    private String feedback;
    private String strengths;
    private String weaknesses;
    private String followUpQuestion;
    private String followUpAnswer;
    private Integer followUpScore;
    private String followUpFeedback;
    private boolean isFollowUpRequired;
    private boolean isFollowUpCompleted;
    private LocalDateTime askedAt;
    private LocalDateTime answeredAt;

    public MockInterviewQuestionDto() {
    }

    public static MockInterviewQuestionDto fromEntity(MockInterviewQuestion q) {
        MockInterviewQuestionDto dto = new MockInterviewQuestionDto();
        dto.setId(q.getId());
        dto.setRoundNumber(q.getRoundNumber());
        dto.setQuestion(q.getQuestion());
        dto.setUserAnswer(q.getUserAnswer());
        dto.setScore(q.getScore());
        dto.setCorrectness(q.getCorrectness());
        dto.setTechnicalDepth(q.getTechnicalDepth());
        dto.setCommunication(q.getCommunication());
        dto.setFeedback(q.getFeedback());
        dto.setStrengths(q.getStrengths());
        dto.setWeaknesses(q.getWeaknesses());
        dto.setFollowUpQuestion(q.getFollowUpQuestion());
        dto.setFollowUpAnswer(q.getFollowUpAnswer());
        dto.setFollowUpScore(q.getFollowUpScore());
        dto.setFollowUpFeedback(q.getFollowUpFeedback());
        dto.setFollowUpRequired(q.isFollowUpRequired());
        dto.setFollowUpCompleted(q.isFollowUpCompleted());
        dto.setAskedAt(q.getAskedAt());
        dto.setAnsweredAt(q.getAnsweredAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
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

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getFollowUpQuestion() {
        return followUpQuestion;
    }

    public void setFollowUpQuestion(String followUpQuestion) {
        this.followUpQuestion = followUpQuestion;
    }

    public String getFollowUpAnswer() {
        return followUpAnswer;
    }

    public void setFollowUpAnswer(String followUpAnswer) {
        this.followUpAnswer = followUpAnswer;
    }

    public Integer getFollowUpScore() {
        return followUpScore;
    }

    public void setFollowUpScore(Integer followUpScore) {
        this.followUpScore = followUpScore;
    }

    public String getFollowUpFeedback() {
        return followUpFeedback;
    }

    public void setFollowUpFeedback(String followUpFeedback) {
        this.followUpFeedback = followUpFeedback;
    }

    public boolean isFollowUpRequired() {
        return isFollowUpRequired;
    }

    public void setFollowUpRequired(boolean followUpRequired) {
        isFollowUpRequired = followUpRequired;
    }

    public boolean isFollowUpCompleted() {
        return isFollowUpCompleted;
    }

    public void setFollowUpCompleted(boolean followUpCompleted) {
        isFollowUpCompleted = followUpCompleted;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
