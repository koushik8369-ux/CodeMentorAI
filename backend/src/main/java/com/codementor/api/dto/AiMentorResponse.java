package com.codementor.api.dto;

import java.time.Instant;

public class AiMentorResponse {

    private String aiResponse;
    private String selectedModel;
    private String status; // "SUCCESS", "FALLBACK", "ERROR"
    private String programmingLanguage;
    private String learningContext;
    private String timestamp;
    private String notice;

    public AiMentorResponse() {
        this.timestamp = Instant.now().toString();
    }

    public AiMentorResponse(String aiResponse, String selectedModel, String status) {
        this.aiResponse = aiResponse;
        this.selectedModel = selectedModel;
        this.status = status;
        this.timestamp = Instant.now().toString();
    }

    public AiMentorResponse(String aiResponse, String selectedModel, String status,
                            String programmingLanguage, String learningContext, String notice) {
        this.aiResponse = aiResponse;
        this.selectedModel = selectedModel;
        this.status = status;
        this.programmingLanguage = programmingLanguage;
        this.learningContext = learningContext;
        this.timestamp = Instant.now().toString();
        this.notice = notice;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public String getLearningContext() {
        return learningContext;
    }

    public void setLearningContext(String learningContext) {
        this.learningContext = learningContext;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
