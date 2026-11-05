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
    @DisplayName("Response parsing: extracts choices[0].message.reasoning when content is null (Tencent Hy4 reasoning)")
    void testExtractReasoningWhenContentIsNull() throws Exception {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null,\"reasoning\":\"Reasoning output from Hy4\"}}]}";
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(node);
        org.junit.jupiter.api.Assertions.assertEquals("Reasoning output from Hy4", extracted);
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
}
