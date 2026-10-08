package com.codementor.api.dto;

import com.codementor.api.entity.Difficulty;
import com.codementor.api.entity.MockInterview;
import com.codementor.api.entity.MockInterviewStatus;
import com.codementor.api.entity.MockInterviewType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockInterviewResponseDto {
    private Long id;
    private Long userId;
    private MockInterviewType interviewType;
    private String topic;
    private Difficulty difficulty;
    private MockInterviewStatus status;
    private int currentRound;
    private int totalRounds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer overallScore;
    private Integer technicalKnowledge;
    private Integer problemSolving;
    private Integer communication;
    private Integer confidence;
    private String overallFeedback;
    private String strengths;
    private String weaknesses;
    private String recommendations;
    private String readinessLevel;
    private MockInterviewQuestionDto currentQuestion;
    private List<MockInterviewQuestionDto> questions = new ArrayList<>();

    public MockInterviewResponseDto() {
    }

    public static MockInterviewResponseDto fromEntity(MockInterview entity) {
        MockInterviewResponseDto dto = new MockInterviewResponseDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
        }
        dto.setInterviewType(entity.getInterviewType());
        dto.setTopic(entity.getTopic());
        dto.setDifficulty(entity.getDifficulty());
        dto.setStatus(entity.getStatus());
        dto.setCurrentRound(entity.getCurrentRound());
        dto.setTotalRounds(entity.getTotalRounds());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setOverallScore(entity.getOverallScore());
        dto.setTechnicalKnowledge(entity.getTechnicalKnowledge());
        dto.setProblemSolving(entity.getProblemSolving());
        dto.setCommunication(entity.getCommunication());
        dto.setConfidence(entity.getConfidence());
        dto.setOverallFeedback(entity.getOverallFeedback());
        dto.setStrengths(entity.getStrengths());
        dto.setWeaknesses(entity.getWeaknesses());
        dto.setRecommendations(entity.getRecommendations());
        dto.setReadinessLevel(entity.getReadinessLevel());

        if (entity.getQuestions() != null) {
            List<MockInterviewQuestionDto> qList = entity.getQuestions().stream()
                    .map(MockInterviewQuestionDto::fromEntity)
                    .collect(Collectors.toList());
            dto.setQuestions(qList);

            // Active or latest question
            if (!qList.isEmpty()) {
                dto.setCurrentQuestion(qList.get(qList.size() - 1));
            }
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public MockInterviewType getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(MockInterviewType interviewType) {
        this.interviewType = interviewType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public MockInterviewStatus getStatus() {
        return status;
    }

    public void setStatus(MockInterviewStatus status) {
        this.status = status;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
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

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getReadinessLevel() {
        return readinessLevel;
    }

    public void setReadinessLevel(String readinessLevel) {
        this.readinessLevel = readinessLevel;
    }

    public MockInterviewQuestionDto getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(MockInterviewQuestionDto currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public List<MockInterviewQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<MockInterviewQuestionDto> questions) {
        this.questions = questions;
    }
}
