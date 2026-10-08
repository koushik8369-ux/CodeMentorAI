package com.codementor.api.dto;

public class TopicAnalyticsDto {
    private String topic;
    private int attempts;
    private double averageScore;
    private int bestScore;

    public TopicAnalyticsDto() {
    }

    public TopicAnalyticsDto(String topic, int attempts, double averageScore, int bestScore) {
        this.topic = topic;
        this.attempts = attempts;
        this.averageScore = averageScore;
        this.bestScore = bestScore;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }
}
