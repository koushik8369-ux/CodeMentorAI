package com.codementor.api.dto;

public class AnalyticsOverviewDto {
    private int totalInterviews;
    private double averageScore;
    private int highestScore;
    private int lowestScore;
    private int completedInterviews;
    private String strongestTopic;
    private String weakestTopic;
    private double recentAverageScore;

    public AnalyticsOverviewDto() {
    }

    public AnalyticsOverviewDto(int totalInterviews, double averageScore, int highestScore,
                                int lowestScore, int completedInterviews, String strongestTopic,
                                String weakestTopic, double recentAverageScore) {
        this.totalInterviews = totalInterviews;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.completedInterviews = completedInterviews;
        this.strongestTopic = strongestTopic;
        this.weakestTopic = weakestTopic;
        this.recentAverageScore = recentAverageScore;
    }

    public int getTotalInterviews() {
        return totalInterviews;
    }

    public void setTotalInterviews(int totalInterviews) {
        this.totalInterviews = totalInterviews;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(int lowestScore) {
        this.lowestScore = lowestScore;
    }

    public int getCompletedInterviews() {
        return completedInterviews;
    }

    public void setCompletedInterviews(int completedInterviews) {
        this.completedInterviews = completedInterviews;
    }

    public String getStrongestTopic() {
        return strongestTopic;
    }

    public void setStrongestTopic(String strongestTopic) {
        this.strongestTopic = strongestTopic;
    }

    public String getWeakestTopic() {
        return weakestTopic;
    }

    public void setWeakestTopic(String weakestTopic) {
        this.weakestTopic = weakestTopic;
    }

    public double getRecentAverageScore() {
        return recentAverageScore;
    }

    public void setRecentAverageScore(double recentAverageScore) {
        this.recentAverageScore = recentAverageScore;
    }
}
