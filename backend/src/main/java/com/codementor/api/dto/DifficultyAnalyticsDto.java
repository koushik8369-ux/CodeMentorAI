package com.codementor.api.dto;

public class DifficultyAnalyticsDto {
    private String difficulty;
    private int attempts;
    private double averageScore;
    private int bestScore;

    public DifficultyAnalyticsDto() {
    }

    public DifficultyAnalyticsDto(String difficulty, int attempts, double averageScore, int bestScore) {
        this.difficulty = difficulty;
        this.attempts = attempts;
        this.averageScore = averageScore;
        this.bestScore = bestScore;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
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
