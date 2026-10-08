package com.codementor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mock_interview_questions")
public class MockInterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_interview_id", nullable = false)
    @JsonIgnore
    private MockInterview mockInterview;

    @Column(nullable = false)
    private int roundNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT")
    private String userAnswer;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String correctness;

    @Column(columnDefinition = "TEXT")
    private String technicalDepth;

    @Column(columnDefinition = "TEXT")
    private String communication;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String followUpQuestion;

    @Column(columnDefinition = "TEXT")
    private String followUpAnswer;

    private Integer followUpScore;

    @Column(columnDefinition = "TEXT")
    private String followUpFeedback;

    private boolean isFollowUpRequired = false;

    private boolean isFollowUpCompleted = false;

    @Column(nullable = false)
    private LocalDateTime askedAt = LocalDateTime.now();

    private LocalDateTime answeredAt;

    public MockInterviewQuestion() {
    }

    public MockInterviewQuestion(MockInterview mockInterview, int roundNumber, String question) {
        this.mockInterview = mockInterview;
        this.roundNumber = roundNumber;
        this.question = question;
        this.askedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MockInterview getMockInterview() {
        return mockInterview;
    }

    public void setMockInterview(MockInterview mockInterview) {
        this.mockInterview = mockInterview;
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
