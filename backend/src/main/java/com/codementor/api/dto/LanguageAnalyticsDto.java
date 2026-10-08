package com.codementor.api.dto;

public class LanguageAnalyticsDto {
    private String language;
    private int attempts;
    private double averageScore;

    public LanguageAnalyticsDto() {
    }

    public LanguageAnalyticsDto(String language, int attempts, double averageScore) {
        this.language = language;
        this.attempts = attempts;
        this.averageScore = averageScore;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
}
