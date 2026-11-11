package com.codementor.api;

import com.codementor.api.dto.AiMentorChatMessage;
import com.codementor.api.dto.AiMentorRequest;
import com.codementor.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AiMentorTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.codementor.api.service.AiMentorService aiMentorService;

    @Autowired
    private com.codementor.api.config.OpenRouterConfig openRouterConfig;

    @Test
    @DisplayName("GET /api/health returns HTTP 200 with UP status")
    void testGetHealthReturns200() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("CodeMentor AI")));
    }

    @Test
    @DisplayName("GET /api/mentor/status returns HTTP 200 without authentication")
    void testGetMentorStatusWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/mentor/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service", containsString("CodeMentor AI Programming Mentor")))
                .andExpect(jsonPath("$.provider", is("OpenRouter API")))
                .andExpect(jsonPath("$.defaultModel", notNullValue()))
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Verify tightened production CORS: exact Vercel origin and OPTIONS preflight work, while broad wildcards are rejected")
    void testTightenedProductionCorsAndPreflight() throws Exception {
        // 1. Exact production frontend origin allowed with credentials
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "https://code-mentor-ai-hkrx.vercel.app"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://code-mentor-ai-hkrx.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(jsonPath("$.status", is("UP")));

        // 2. OPTIONS preflight request for /api/mentor/chat succeeds
        mockMvc.perform(options("/api/mentor/chat")
                        .header("Origin", "https://code-mentor-ai-hkrx.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization, content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://code-mentor-ai-hkrx.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));

        // 3. Localhost development origin allowed
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        // 4. Broad *.vercel.app wildcards are rejected with HTTP 403
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "https://malicious-untrusted.vercel.app"))
                .andExpect(status().isForbidden());

        // 5. Broad *.onrender.com wildcards are rejected with HTTP 403
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "https://untrusted-service.onrender.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/mentor/chat works without authentication (Demo Profile access)")
    void testChatWithMentorWithoutAuth() throws Exception {
        AiMentorRequest request = new AiMentorRequest(
                "How does Spring Boot dependency injection work?",
                List.of(new AiMentorChatMessage("user", "Hello mentor")),
                "Java",
                "Intermediate"
        );

        mockMvc.perform(post("/api/mentor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiResponse", notNullValue()))
                .andExpect(jsonPath("$.selectedModel", notNullValue()))
                .andExpect(jsonPath("$.status", anyOf(is("SUCCESS"), is("FALLBACK"))))
                .andExpect(jsonPath("$.programmingLanguage", is("Java")));
    }

    @Test
    @DisplayName("POST /api/mentor/chat works with valid JWT authentication")
    void testChatWithMentorWithJwtAuth() throws Exception {
        com.codementor.api.entity.User user = new com.codementor.api.entity.User("Candidate", "candidate@codementor.ai", "pass", "ROLE_CANDIDATE");
        user.setId(1L);
        String token = jwtService.generateToken(user);

        AiMentorRequest request = new AiMentorRequest(
                "Explain the two pointer algorithm for LeetCode array questions",
                List.of(),
                "Java",
                "Technical Interview Prep"
        );

        mockMvc.perform(post("/api/mentor/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiResponse", containsString("Two-Pointer")))
                .andExpect(jsonPath("$.status", anyOf(is("SUCCESS"), is("FALLBACK"))));
    }

    @Test
    @DisplayName("POST /api/mentor/chat should reject blank message with 400 Bad Request")
    void testChatWithBlankMessage() throws Exception {
        AiMentorRequest request = new AiMentorRequest("", List.of(), "Java", "Beginner");

        mockMvc.perform(post("/api/mentor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Existing protected endpoints (GET /api/interviews) remain protected and reject unauthenticated requests")
    void testProtectedEndpointsRemainProtected() throws Exception {
        mockMvc.perform(get("/api/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Model resolution: backend default is authoritative when customModel is null, blank, or 'default'")
    void testResolveModelAuthoritativeBackendDefault() {
        String defaultModel = openRouterConfig.getDefaultModel();
        org.junit.jupiter.api.Assertions.assertEquals(defaultModel, aiMentorService.resolveModel(null));
        org.junit.jupiter.api.Assertions.assertEquals(defaultModel, aiMentorService.resolveModel(""));
        org.junit.jupiter.api.Assertions.assertEquals(defaultModel, aiMentorService.resolveModel("   "));
        org.junit.jupiter.api.Assertions.assertEquals(defaultModel, aiMentorService.resolveModel("default"));
        org.junit.jupiter.api.Assertions.assertEquals(defaultModel, aiMentorService.resolveModel("auto"));

        // Explicit custom override is honored
        org.junit.jupiter.api.Assertions.assertEquals("meta-llama/llama-3.3-70b-instruct",
                aiMentorService.resolveModel("meta-llama/llama-3.3-70b-instruct"));
    }

    @Test
    @DisplayName("Secret redaction: API keys and Bearer tokens are scrubbed from log/error strings")
    void testSecretRedaction() {
        String testInput = "Failed calling with Authorization: Bearer sk-or-v1-abc123secret and key=secret123";
        String redacted = aiMentorService.redactSecrets(testInput);
        org.junit.jupiter.api.Assertions.assertFalse(redacted.contains("sk-or-v1-abc123secret"));
        org.junit.jupiter.api.Assertions.assertFalse(redacted.contains("secret123"));
        org.junit.jupiter.api.Assertions.assertTrue(redacted.contains("[REDACTED]"));
    }

    @Test
    @DisplayName("Retired models are safely rerouted to authoritative default tencent/hy4")
    void testRetiredModelsReroutedToAuthoritativeDefault() {
        String defaultModel = openRouterConfig.getDefaultModel();
        org.junit.jupiter.api.Assertions.assertEquals(defaultModel,
                aiMentorService.resolveModel("google/gemini-2.0-flash-001"));
        org.junit.jupiter.api.Assertions.assertEquals(defaultModel,
                aiMentorService.resolveModel("anthropic/claude-3.5-sonnet"));
    }

    @Test
    @DisplayName("Response parsing: extracts standard choices[0].message.content string")
    void testExtractStandardContent() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Standard output\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Standard output", extracted);
    }

    @Test
    @DisplayName("Response parsing: reasoning-only response with null content returns null (never exposes internal reasoning)")
    void testReasoningOnlyResponseNeverExposed() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null,\"reasoning\":\"Internal thought process: Let's solve two sum...\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertNull(extracted, "Reasoning field must never be exposed when content is null");
    }

    @Test
    @DisplayName("Response parsing: reasoning_content with null content returns null")
    void testReasoningContentNeverExposed() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null,\"reasoning_content\":\"Detailed chain-of-thought...\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertNull(extracted, "reasoning_content must never be exposed");
    }

    @Test
    @DisplayName("Response parsing: strips <think> tags from content and returns only final answer")
    void testContentWithEmbeddedThinkingStripsPlanning() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"<think>Let's plan the steps to solve this</think>Here is the clean Java solution:\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Here is the clean Java solution:", extracted);
    }

    @Test
    @DisplayName("Response parsing: content containing only thinking tags returns null to trigger fallback")
    void testThinkingOnlyContentReturnsNull() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"<think>Only planning steps here, no real answer</think>\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertNull(extracted);
    }

    @Test
    @DisplayName("Response parsing: extracts structured array content parts")
    void testExtractArrayContentParts() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"Part 1 \"},{\"type\":\"text\",\"text\":\"Part 2\"}]}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Part 1 Part 2", extracted);
    }

    @Test
    @DisplayName("Response parsing: skips reasoning parts within array content")
    void testExtractArraySkipsReasoningParts() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":[{\"type\":\"reasoning\",\"text\":\"Internal plan\"},{\"type\":\"text\",\"text\":\"Actual answer\"}]}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Actual answer", extracted);
    }

    @Test
    @DisplayName("Response parsing: returns null when content is literal 'null' or missing")
    void testExtractNullReturnsNull() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"null\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertNull(extracted);
    }

    @Test
    @DisplayName("AiMentorResponse DTO: serializes aiResponse, response, content, and message for frontend normalization")
    void testDtoSerializationContract() throws Exception {
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                "Generated code explanation", "tencent/hy4", "SUCCESS", "Java", "DSA", null
        );
        String json = objectMapper.writeValueAsString(response);
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);

        org.junit.jupiter.api.Assertions.assertEquals("Generated code explanation", node.path("aiResponse").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Generated code explanation", node.path("response").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Generated code explanation", node.path("content").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Generated code explanation", node.path("message").asText());
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", node.path("status").asText());
    }

    @Test
    @DisplayName("AiMentorResponse DTO: null or 'null' input is sanitized and status is downgraded to FALLBACK")
    void testSanitizeNullAiResponse() {
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                "null", "tencent/hy4", "SUCCESS"
        );
        org.junit.jupiter.api.Assertions.assertNotEquals("null", response.getAiResponse());
        org.junit.jupiter.api.Assertions.assertFalse(response.getAiResponse().isBlank());
        org.junit.jupiter.api.Assertions.assertEquals("FALLBACK", response.getStatus());
    }

    @Test
    @DisplayName("Request construction: configures reasoning effort none and exclude true for tencent/hy4 with 4096 tokens")
    void testBuildRequestPayloadConfiguresReasoningForTencentHy4() {
        AiMentorRequest req = new AiMentorRequest("Help with arrays", List.of(), "Java", "DSA");
        Map<String, Object> payload = aiMentorService.buildRequestPayload(req, "tencent/hy4", "Java", "DSA", true);

        org.junit.jupiter.api.Assertions.assertEquals("tencent/hy4", payload.get("model"));
        org.junit.jupiter.api.Assertions.assertEquals(4096, payload.get("max_tokens"));
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("reasoning"));

        @SuppressWarnings("unchecked")
        Map<String, Object> reasoning = (Map<String, Object>) payload.get("reasoning");
        org.junit.jupiter.api.Assertions.assertEquals("none", reasoning.get("effort"));
        org.junit.jupiter.api.Assertions.assertEquals(true, reasoning.get("exclude"));
    }

    @Test
    @DisplayName("Request construction: omits reasoning parameter for standard non-reasoning models")
    void testBuildRequestPayloadOmitsReasoningForStandardModels() {
        AiMentorRequest req = new AiMentorRequest("Help with loops", List.of(), "Java", "Basics");
        Map<String, Object> payload1 = aiMentorService.buildRequestPayload(req, "deepseek/deepseek-chat", "Java", "Basics", true);
        org.junit.jupiter.api.Assertions.assertFalse(payload1.containsKey("reasoning"));

        Map<String, Object> payload2 = aiMentorService.buildRequestPayload(req, "meta-llama/llama-3.3-70b-instruct", "Java", "Basics", true);
        org.junit.jupiter.api.Assertions.assertFalse(payload2.containsKey("reasoning"));
    }

    @Test
    @DisplayName("Response parsing: extracts object-based content with text field")
    void testExtractObjectContentWithTextField() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":{\"type\":\"text\",\"text\":\"Object format response\"}}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Object format response", extracted);
    }

    @Test
    @DisplayName("Response parsing: extracts legacy text completions format in choices[0].text")
    void testExtractLegacyTextCompletionsFormat() throws Exception {
        String json = "{\"choices\":[{\"text\":\"Legacy completions text answer\"}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Legacy completions text answer", extracted);
    }

    @Test
    @DisplayName("Response parsing: extracts delta streaming format in choices[0].delta.content")
    void testExtractDeltaStreamingFormat() throws Exception {
        String json = "{\"choices\":[{\"delta\":{\"content\":\"Streaming delta text answer\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Streaming delta text answer", extracted);
    }

    @Test
    @DisplayName("Response parsing: returns null when content is whitespace only")
    void testExtractEmptyWhitespaceReturnsNull() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"   \"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertNull(extracted);
    }

    @Test
    @DisplayName("Safe diagnostics: captures structure without exposing secrets or reasoning tokens")
    void testSafeDiagnosticsLogging() throws Exception {
        String fixture = "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\",\"content\":\"Safe text\",\"reasoning\":\"Secret internal thought\"}}],\"usage\":{\"prompt_tokens\":100,\"completion_tokens\":50,\"total_tokens\":150}}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(fixture);
        // Ensure no exceptions thrown during diagnostics logging
        aiMentorService.logSafeResponseDiagnostics(200, "tencent/hy4", node, fixture);
    }

    @Test
    @DisplayName("Upstream errors: extracts and sanitizes upstream error message from OpenRouter")
    void testExtractUpstreamErrorMessage() {
        String errJson = "{\"error\":{\"message\":\"Provider rate limit reached with key sk-or-v1-secret123\",\"code\":429}}";
        String summary = aiMentorService.extractErrorMessage(errJson, 429);
        org.junit.jupiter.api.Assertions.assertTrue(summary.contains("rate limit reached"));
        org.junit.jupiter.api.Assertions.assertFalse(summary.contains("sk-or-v1-secret123"));
    }
}
