package com.codementor.api.service;

import com.codementor.api.dto.QuestionGenerationRequest;
import com.codementor.api.dto.QuestionGenerationResponse;
import com.codementor.api.exception.GeminiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.model:gemini-3.8-flash}")
    private String geminiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public QuestionGenerationResponse generateQuestion(QuestionGenerationRequest request) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new GeminiServiceException(
                    "Google Gemini API is unavailable: GEMINI_API_KEY is not configured in the Spring Boot backend environment."
            );
        }

        try {
            return callGeminiApi(request);
        } catch (GeminiServiceException gse) {
            throw gse;
        } catch (Exception e) {
            throw new GeminiServiceException("Failed to generate question via Google Gemini API: " + e.getMessage(), e);
        }
    }

    private QuestionGenerationResponse callGeminiApi(QuestionGenerationRequest request) throws Exception {
        String prompt = "You are a Principal Software Engineering Interviewer for top-tier tech companies. "
                + "Generate a realistic technical coding interview question for a candidate applying for " + request.getRole() + ".\n"
                + "Target Language: " + request.getLanguage() + "\n"
                + "Difficulty Level: " + request.getDifficulty() + "\n"
                + "Focus Topic: " + request.getTopic() + "\n\n"
                + "Respond with pure valid JSON matching this schema:\n"
                + "{\n"
                + "  \"title\": \"Problem title\",\n"
                + "  \"description\": \"Clear requirements statement\",\n"
                + "  \"difficulty\": \"" + request.getDifficulty() + "\",\n"
                + "  \"topic\": \"" + request.getTopic() + "\",\n"
                + "  \"examples\": [{\"input\": \"sample input\", \"output\": \"sample output\", \"explanation\": \"reasoning\"}],\n"
                + "  \"constraints\": [\"constraint 1\", \"constraint 2\"],\n"
                + "  \"starterCode\": {\"" + request.getLanguage() + "\": \"boilerplate code\"}\n"
                + "}";

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(content),
                "generationConfig", generationConfig
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);
        String endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(25))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new GeminiServiceException(
                    "Google Gemini API error (HTTP " + response.statusCode() + "): " + response.body()
            );
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode candidateNode = rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text");

        if (candidateNode.isMissingNode() || candidateNode.asText().isBlank()) {
            throw new GeminiServiceException("Empty response payload received from Google Gemini API.");
        }

        String rawJson = candidateNode.asText();
        return objectMapper.readValue(rawJson, QuestionGenerationResponse.class);
    }
}
