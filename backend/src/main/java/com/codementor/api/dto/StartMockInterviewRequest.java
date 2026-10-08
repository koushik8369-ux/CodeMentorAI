package com.codementor.api.dto;

import com.codementor.api.entity.Difficulty;
import com.codementor.api.entity.MockInterviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StartMockInterviewRequest {

    @NotNull(message = "Interview type is required")
    private MockInterviewType interviewType;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(value = 3, message = "Total rounds must be at least 3")
    @Max(value = 10, message = "Total rounds cannot exceed 10")
    private int totalRounds = 5;

    public StartMockInterviewRequest() {
    }

    public StartMockInterviewRequest(MockInterviewType interviewType, String topic, Difficulty difficulty, int totalRounds) {
        this.interviewType = interviewType;
        this.topic = topic;
        this.difficulty = difficulty;
        this.totalRounds = totalRounds;
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

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }
}
