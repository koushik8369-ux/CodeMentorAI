package com.codementor.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JavaCodeBlockValidation {

    private int blockIndex;
    private String language;
    private String status; // "VALID", "INVALID", "NOT_CHECKED"
    private boolean corrected;
    private String errorMessage;
    private String validationSummary;

    public JavaCodeBlockValidation() {
    }

    public JavaCodeBlockValidation(int blockIndex, String language, String status, boolean corrected, String errorMessage, String validationSummary) {
        this.blockIndex = blockIndex;
        this.language = language;
        this.status = status;
        this.corrected = corrected;
        this.errorMessage = errorMessage;
        this.validationSummary = validationSummary;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("isCorrected")
    public boolean isCorrected() {
        return corrected;
    }

    public void setCorrected(boolean corrected) {
        this.corrected = corrected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getValidationSummary() {
        return validationSummary;
    }

    public void setValidationSummary(String validationSummary) {
        this.validationSummary = validationSummary;
    }
}
