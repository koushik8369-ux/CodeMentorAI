package com.codementor.api.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmitInterviewRequest {

    @NotBlank(message = "Submitted code cannot be empty")
    private String code;

    public SubmitInterviewRequest() {
    }

    public SubmitInterviewRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
