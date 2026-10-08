package com.codementor.api.dto;

public class AiActionPlanDto {
    private String topic;
    private String reason;
    private String recommendedPractice;

    public AiActionPlanDto() {
    }

    public AiActionPlanDto(String topic, String reason, String recommendedPractice) {
        this.topic = topic;
        this.reason = reason;
        this.recommendedPractice = recommendedPractice;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRecommendedPractice() {
        return recommendedPractice;
    }

    public void setRecommendedPractice(String recommendedPractice) {
        this.recommendedPractice = recommendedPractice;
    }
}
