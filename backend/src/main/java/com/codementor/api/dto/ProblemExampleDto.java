package com.codementor.api.dto;

public class ProblemExampleDto {
    private String input;
    private String output;
    private String explanation;

    public ProblemExampleDto() {
    }

    public ProblemExampleDto(String input, String output, String explanation) {
        this.input = input;
        this.output = output;
        this.explanation = explanation;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
