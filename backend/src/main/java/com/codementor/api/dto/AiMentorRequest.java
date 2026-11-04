package com.codementor.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class AiMentorRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 12000, message = "Message exceeds maximum allowed length of 12000 characters")
    private String message;

    private List<AiMentorChatMessage> conversationHistory = new ArrayList<>();

    @Size(max = 100, message = "Programming language descriptor too long")
    private String programmingLanguage;

    @Size(max = 100, message = "Learning context descriptor too long")
    private String learningContext;

    @Size(max = 100, message = "Custom model override descriptor too long")
    private String customModel;

    public AiMentorRequest() {
    }

    public AiMentorRequest(String message, List<AiMentorChatMessage> conversationHistory,
                           String programmingLanguage, String learningContext) {
        this.message = message;
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
        this.programmingLanguage = programmingLanguage;
        this.learningContext = learningContext;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AiMentorChatMessage> getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(List<AiMentorChatMessage> conversationHistory) {
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
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

    public String getCustomModel() {
        return customModel;
    }

    public void setCustomModel(String customModel) {
        this.customModel = customModel;
    }
}
