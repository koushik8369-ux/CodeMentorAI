package com.codementor.api.dto;

import java.util.List;
import java.util.Map;

public class QuestionGenerationResponse {

    private String title;
    private String description;
    private String difficulty;
    private String topic;
    private List<ExampleDto> examples;
    private List<String> constraints;
    private Map<String, String> starterCode;

    public QuestionGenerationResponse() {
    }

    public QuestionGenerationResponse(String title, String description, String difficulty, String topic,
                                      List<ExampleDto> examples, List<String> constraints, Map<String, String> starterCode) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.topic = topic;
        this.examples = examples;
        this.constraints = constraints;
        this.starterCode = starterCode;
    }

    public static class ExampleDto {
        private String input;
        private String output;
        private String explanation;

        public ExampleDto() {
        }

        public ExampleDto(String input, String output, String explanation) {
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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<ExampleDto> getExamples() {
        return examples;
    }

    public void setExamples(List<ExampleDto> examples) {
        this.examples = examples;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    public Map<String, String> getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(Map<String, String> starterCode) {
        this.starterCode = starterCode;
    }
}
