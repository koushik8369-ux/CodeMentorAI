package com.codementor.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        this.status = sanitizeStatus(aiResponse, status);
        this.aiResponse = sanitizeResponse(aiResponse);
        this.selectedModel = selectedModel;
        this.timestamp = Instant.now().toString();
    }

    public AiMentorResponse(String aiResponse, String selectedModel, String status,
                            String programmingLanguage, String learningContext, String notice) {
        this.status = sanitizeStatus(aiResponse, status);
        this.aiResponse = sanitizeResponse(aiResponse);
        this.selectedModel = selectedModel;
        this.programmingLanguage = programmingLanguage;
        this.learningContext = learningContext;
        this.timestamp = Instant.now().toString();
        this.notice = notice;
    }

    private String sanitizeResponse(String response) {
        if (response == null || response.isBlank() || "null".equalsIgnoreCase(response.trim())) {
            return "Unable to retrieve generated response from mentor engine. Please try again or rephrase your question.";
        }
        return response.trim();
    }

    private String sanitizeStatus(String rawResponse, String currentStatus) {
        if (rawResponse == null || rawResponse.isBlank() || "null".equalsIgnoreCase(rawResponse.trim())) {
            return "FALLBACK";
        }
        return currentStatus != null ? currentStatus : "SUCCESS";
    }

    @JsonProperty("aiResponse")
    public String getAiResponse() {
        return aiResponse;
    }

    @JsonProperty("response")
    public String getResponse() {
        return aiResponse;
    }

    @JsonProperty("content")
    public String getContent() {
        return aiResponse;
    }

    @JsonProperty("message")
    public String getMessage() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank() || "null".equalsIgnoreCase(aiResponse.trim())) {
            this.status = "FALLBACK";
        }
        this.aiResponse = sanitizeResponse(aiResponse);
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
