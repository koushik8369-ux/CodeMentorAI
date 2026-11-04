package com.codementor.api.dto;

public class AiMentorChatMessage {

    private String role; // "user", "assistant", or "system"
    private String content;

    public AiMentorChatMessage() {
    }

    public AiMentorChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
