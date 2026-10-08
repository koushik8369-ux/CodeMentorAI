package com.codementor.api.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmitMockAnswerRequest {

    @NotBlank(message = "Answer cannot be blank")
    private String answer;

    public SubmitMockAnswerRequest() {
    }

    public SubmitMockAnswerRequest(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
