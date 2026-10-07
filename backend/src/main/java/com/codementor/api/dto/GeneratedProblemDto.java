package com.codementor.api.dto;

import java.util.ArrayList;
import java.util.List;

public class GeneratedProblemDto {
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private List<String> constraints = new ArrayList<>();
    private List<ProblemExampleDto> examples = new ArrayList<>();
    private String starterCode;

    public GeneratedProblemDto() {
    }

    public GeneratedProblemDto(String title, String description, String inputFormat, String outputFormat,
                               List<String> constraints, List<ProblemExampleDto> examples, String starterCode) {
        this.title = title;
        this.description = description;
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.constraints = constraints != null ? constraints : new ArrayList<>();
        this.examples = examples != null ? examples : new ArrayList<>();
        this.starterCode = starterCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    public List<ProblemExampleDto> getExamples() {
        return examples;
    }

    public void setExamples(List<ProblemExampleDto> examples) {
        this.examples = examples;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }
}
