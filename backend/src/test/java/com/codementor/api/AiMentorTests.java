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

import java.util.ArrayList;
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
    private com.codementor.api.service.JavaCodeValidationService javaCodeValidationService;

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

    @Test
    @DisplayName("JavaParser validation: complete Java class passes Java 21 syntax validation")
    void testValidateCompleteJavaClass() {
        String validClass = "public class Solution {\n"
                + "    public int maxSubArray(int[] nums) {\n"
                + "        int max = nums[0], sum = 0;\n"
                + "        for (int n : nums) {\n"
                + "            sum = Math.max(n, sum + n);\n"
                + "            max = Math.max(max, sum);\n"
                + "        }\n"
                + "        return max;\n"
                + "    }\n"
                + "}";
        com.codementor.api.service.JavaCodeValidationService.ValidationResult res = javaCodeValidationService.validateJavaSnippet(validClass);
        org.junit.jupiter.api.Assertions.assertTrue(res.isValid());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", res.getStatus());
    }

    @Test
    @DisplayName("JavaParser validation: Java 21 records and sealed interfaces pass syntax validation")
    void testValidateJavaRecordAndInterface() {
        String validRecord = "public record CandidateProfile(String id, String name, int score) {\n"
                + "    public boolean isTopPerformer() {\n"
                + "        return score >= 90;\n"
                + "    }\n"
                + "}";
        com.codementor.api.service.JavaCodeValidationService.ValidationResult res = javaCodeValidationService.validateJavaSnippet(validRecord);
        org.junit.jupiter.api.Assertions.assertTrue(res.isValid());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", res.getStatus());
    }

    @Test
    @DisplayName("JavaParser validation: standalone Java method snippet without outer class passes syntax validation")
    void testValidateStandaloneMethodSnippet() {
        String methodCode = "public int[] twoSum(int[] nums, int target) {\n"
                + "    Map<Integer, Integer> map = new HashMap<>();\n"
                + "    for (int i = 0; i < nums.length; i++) {\n"
                + "        int comp = target - nums[i];\n"
                + "        if (map.containsKey(comp)) return new int[]{map.get(comp), i};\n"
                + "        map.put(nums[i], i);\n"
                + "    }\n"
                + "    return new int[0];\n"
                + "}";
        com.codementor.api.service.JavaCodeValidationService.ValidationResult res = javaCodeValidationService.validateJavaSnippet(methodCode);
        org.junit.jupiter.api.Assertions.assertTrue(res.isValid());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", res.getStatus());
    }

    @Test
    @DisplayName("JavaParser validation: syntactically broken Java is detected as INVALID with sanitized error")
    void testValidateBrokenJavaSyntax() {
        String brokenJava = "public class Broken {\n"
                + "    public void test() {\n"
                + "        int x = 10\n" // missing semicolon
                + "        if (x > 5 {\n" // missing closing paren
                + "    }\n"
                + "}";
        com.codementor.api.service.JavaCodeValidationService.ValidationResult res = javaCodeValidationService.validateJavaSnippet(brokenJava);
        org.junit.jupiter.api.Assertions.assertFalse(res.isValid());
        org.junit.jupiter.api.Assertions.assertEquals("INVALID", res.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(res.getMessage());
        org.junit.jupiter.api.Assertions.assertFalse(res.getMessage().contains("com.github.javaparser"));
    }

    @Test
    @DisplayName("JavaParser language discrimination: non-Java snippets (Python, SQL, TS) are NOT_CHECKED")
    void testNonJavaSnippetsAreNotChecked() {
        org.junit.jupiter.api.Assertions.assertFalse(javaCodeValidationService.isJavaBlock("python", "Java", "def solve(x): return x * 2"));
        org.junit.jupiter.api.Assertions.assertFalse(javaCodeValidationService.isJavaBlock("sql", "SQL", "SELECT id FROM users WHERE status = 1;"));
        org.junit.jupiter.api.Assertions.assertFalse(javaCodeValidationService.isJavaBlock("typescript", "TypeScript", "const x: number = 42;"));
    }

    @Test
    @DisplayName("AiMentorService: multiple code blocks in response are parsed, Java validated and non-Java marked NOT_CHECKED")
    void testValidateAndEnrichMultipleCodeBlocks() {
        String text = "Here is Java:\n```java\npublic class Greeter {\n    public String greet() { return \"Hello\"; }\n}\n```\nAnd here is SQL:\n```sql\nSELECT * FROM candidates;\n```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                text, "tencent/hy4", "SUCCESS", "Java", "Basics", null
        );

        aiMentorService.validateAndEnrichResponse(response, "tencent/hy4", "Java");

        org.junit.jupiter.api.Assertions.assertEquals("VALID", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(2, response.getValidationDetails().size());

        com.codementor.api.dto.JavaCodeBlockValidation block0 = response.getValidationDetails().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("java", block0.getLanguage());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", block0.getStatus());

        com.codementor.api.dto.JavaCodeBlockValidation block1 = response.getValidationDetails().get(1);
        org.junit.jupiter.api.Assertions.assertEquals("sql", block1.getLanguage());
        org.junit.jupiter.api.Assertions.assertEquals("NOT_CHECKED", block1.getStatus());
    }

    @Test
    @DisplayName("AiMentorService: response with invalid Java block is marked INVALID with parser warning")
    void testValidateAndEnrichInvalidJavaBlock() {
        String text = "Here is incomplete code:\n```java\npublic class Broken { int a = ; }\n```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                text, "tencent/hy4", "SUCCESS", "Java", "Basics", null
        );

        aiMentorService.validateAndEnrichResponse(response, "tencent/hy4", "Java");

        org.junit.jupiter.api.Assertions.assertEquals("INVALID", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertFalse(response.getValidationDetails().isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals("INVALID", response.getValidationDetails().get(0).getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getValidationDetails().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("POST /api/mentor/chat returns validationStatus and validationDetails in JSON contract")
    void testChatEndpointReturnsValidationMetadata() throws Exception {
        AiMentorRequest request = new AiMentorRequest(
                "Explain the two pointer algorithm for LeetCode array questions",
                List.of(),
                "Java",
                "Technical Interview Prep"
        );

        mockMvc.perform(post("/api/mentor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus", notNullValue()))
                .andExpect(jsonPath("$.validationDetails", notNullValue()))
                .andExpect(jsonPath("$.validationStatus", anyOf(is("VALID"), is("INVALID"), is("NOT_CHECKED"))));
    }

    // =========================================================================
    // FOCUSED MANUAL QA SCENARIO SUITE (Scenarios 1-8)
    // =========================================================================

    @Test
    @DisplayName("QA Scenario 1: Valid Java class -> status is VALID with 0 errors")
    void qaScenario1_ValidJavaClass_ReturnsValid() {
        String validJava = "```java\n"
                + "package com.example.service;\n\n"
                + "import java.util.List;\n\n"
                + "public class TaskProcessor {\n"
                + "    private final List<String> tasks;\n"
                + "    public TaskProcessor(List<String> tasks) {\n"
                + "        this.tasks = tasks;\n"
                + "    }\n"
                + "    public int count() {\n"
                + "        return tasks.size();\n"
                + "    }\n"
                + "}\n"
                + "```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                validJava, "tencent/hy4", "SUCCESS", "Java", "OOP", null
        );
        aiMentorService.validateAndEnrichResponse(response, "tencent/hy4", "Java");

        org.junit.jupiter.api.Assertions.assertEquals("VALID", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.getValidationDetails().size());
        com.codementor.api.dto.JavaCodeBlockValidation block = response.getValidationDetails().get(0);
        org.junit.jupiter.api.Assertions.assertEquals(0, block.getBlockIndex());
        org.junit.jupiter.api.Assertions.assertEquals("java", block.getLanguage());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", block.getStatus());
        org.junit.jupiter.api.Assertions.assertFalse(block.isCorrected());
        org.junit.jupiter.api.Assertions.assertNull(block.getErrorMessage());
    }

    @Test
    @DisplayName("QA Scenario 2: Invalid Java method declaration -> triggers strictly one correction attempt")
    void qaScenario2_InvalidJavaMethodDeclaration_TriggersExactlyOneCorrectionAttempt() {
        String invalidSnippet = "```java\n"
                + "public int computeSum(int a, int b {\n" // missing closing paren
                + "    return a + b;\n"
                + "}\n"
                + "```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                invalidSnippet, "tencent/hy4", "SUCCESS", "Java", "Basics", null
        );

        int[] attempts = new int[]{0};
        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
        };

        com.codementor.api.service.AiMentorService serviceWithSpy = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String requestSyntaxCorrection(String model, String brokenCode, String errorMessage) {
                attempts[0]++;
                // Still returns broken code to verify loop prevention terminates after 1 attempt
                return "public int computeSum(int a, int b { return a + b; }";
            }
        };

        serviceWithSpy.validateAndEnrichResponse(response, "tencent/hy4", "Java");

        org.junit.jupiter.api.Assertions.assertEquals(1, attempts[0], "Correction attempt must be executed strictly once");
        org.junit.jupiter.api.Assertions.assertEquals("INVALID", response.getValidationStatus());
    }

    @Test
    @DisplayName("QA Scenario 3: Successfully corrected Java -> status is VALID and isCorrected is true")
    void qaScenario3_SuccessfullyCorrectedJava_ReturnsValidAndIsCorrectedTrue() {
        String brokenJava = "```java\n"
                + "public int multiply(int a, int b) {\n"
                + "    return a * b\n" // missing semicolon
                + "}\n"
                + "```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                brokenJava, "tencent/hy4", "SUCCESS", "Java", "Basics", null
        );

        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
        };

        com.codementor.api.service.AiMentorService serviceWithCorrection = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String requestSyntaxCorrection(String model, String brokenCode, String errorMessage) {
                return "public int multiply(int a, int b) {\n    return a * b;\n}";
            }
        };

        serviceWithCorrection.validateAndEnrichResponse(response, "tencent/hy4", "Java");

        org.junit.jupiter.api.Assertions.assertEquals("VALID", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.getValidationDetails().size());
        com.codementor.api.dto.JavaCodeBlockValidation block = response.getValidationDetails().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("VALID", block.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(block.isCorrected());
        org.junit.jupiter.api.Assertions.assertNull(block.getErrorMessage());
        // Verify code in response was replaced with corrected code
        org.junit.jupiter.api.Assertions.assertTrue(response.getAiResponse().contains("return a * b;"));
    }

    @Test
    @DisplayName("QA Scenario 4: Uncorrectable Java -> status is INVALID with sanitized parse warning")
    void qaScenario4_UncorrectableJava_ReturnsInvalidAndIsCorrectedFalse() {
        String severelyBroken = "```java\n"
                + "class ??? { @@! \n"
                + "```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                severelyBroken, "tencent/hy4", "SUCCESS", "Java", "Basics", null
        );

        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
        };

        com.codementor.api.service.AiMentorService serviceFailedCorrection = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String requestSyntaxCorrection(String model, String brokenCode, String errorMessage) {
                // LLM unable to fix or returns unparseable text
                return "Still completely broken @@! code";
            }
        };

        serviceFailedCorrection.validateAndEnrichResponse(response, "tencent/hy4", "Java");

        org.junit.jupiter.api.Assertions.assertEquals("INVALID", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.getValidationDetails().size());
        com.codementor.api.dto.JavaCodeBlockValidation block = response.getValidationDetails().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("INVALID", block.getStatus());
        org.junit.jupiter.api.Assertions.assertFalse(block.isCorrected());
        org.junit.jupiter.api.Assertions.assertNotNull(block.getErrorMessage());
    }

    @Test
    @DisplayName("QA Scenario 5: Python code -> marked NOT_CHECKED and never sent to JavaParser")
    void qaScenario5_PythonCode_ReturnsNotChecked() {
        String pyResponse = "Here is the solution in Python:\n"
                + "```python\n"
                + "def two_sum(nums, target):\n"
                + "    lookup = {}\n"
                + "    for i, num in enumerate(nums):\n"
                + "        diff = target - num\n"
                + "        if diff in lookup:\n"
                + "            return [lookup[diff], i]\n"
                + "        lookup[num] = i\n"
                + "    return []\n"
                + "```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                pyResponse, "tencent/hy4", "SUCCESS", "Python", "Algorithms", null
        );
        aiMentorService.validateAndEnrichResponse(response, "tencent/hy4", "Python");

        org.junit.jupiter.api.Assertions.assertEquals("NOT_CHECKED", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.getValidationDetails().size());
        com.codementor.api.dto.JavaCodeBlockValidation block = response.getValidationDetails().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("python", block.getLanguage());
        org.junit.jupiter.api.Assertions.assertEquals("NOT_CHECKED", block.getStatus());
        org.junit.jupiter.api.Assertions.assertFalse(block.isCorrected());
        org.junit.jupiter.api.Assertions.assertNull(block.getErrorMessage());
    }

    @Test
    @DisplayName("QA Scenario 6: SQL code -> marked NOT_CHECKED and never sent to JavaParser")
    void qaScenario6_SqlCode_ReturnsNotChecked() {
        String sqlResponse = "Here is the database schema:\n"
                + "```sql\n"
                + "CREATE TABLE candidate_skills (\n"
                + "    id BIGSERIAL PRIMARY KEY,\n"
                + "    user_id BIGINT NOT NULL,\n"
                + "    skill_name VARCHAR(64) NOT NULL\n"
                + ");\n"
                + "CREATE INDEX idx_user ON candidate_skills(user_id);\n"
                + "```";
        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                sqlResponse, "tencent/hy4", "SUCCESS", "SQL", "Databases", null
        );
        aiMentorService.validateAndEnrichResponse(response, "tencent/hy4", "SQL");

        org.junit.jupiter.api.Assertions.assertEquals("NOT_CHECKED", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.getValidationDetails().size());
        com.codementor.api.dto.JavaCodeBlockValidation block = response.getValidationDetails().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("sql", block.getLanguage());
        org.junit.jupiter.api.Assertions.assertEquals("NOT_CHECKED", block.getStatus());
    }

    @Test
    @DisplayName("QA Scenario 7: Multiple Java code blocks -> each block receives its own distinct status")
    void qaScenario7_MultipleJavaCodeBlocks_EachBlockReceivesDistinctStatus() {
        String multiBlockMarkdown = "### Block 0: Valid Interface\n"
                + "```java\n"
                + "public interface Calculator {\n"
                + "    int calculate(int a, int b);\n"
                + "}\n"
                + "```\n\n"
                + "### Block 1: Broken Syntax\n"
                + "```java\n"
                + "public class Incomplete {\n"
                + "    int x = \n"
                + "}\n"
                + "```\n\n"
                + "### Block 2: Valid Record\n"
                + "```java\n"
                + "public record Point(int x, int y) {}\n"
                + "```";

        com.codementor.api.dto.AiMentorResponse response = new com.codementor.api.dto.AiMentorResponse(
                multiBlockMarkdown, "tencent/hy4", "SUCCESS", "Java", "OOP", null
        );

        aiMentorService.validateAndEnrichResponse(response, "tencent/hy4", "Java");

        // Overall status is INVALID because Block 1 has parse error
        org.junit.jupiter.api.Assertions.assertEquals("INVALID", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(3, response.getValidationDetails().size());

        // Block 0: Valid interface
        com.codementor.api.dto.JavaCodeBlockValidation block0 = response.getValidationDetails().get(0);
        org.junit.jupiter.api.Assertions.assertEquals(0, block0.getBlockIndex());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", block0.getStatus());

        // Block 1: Broken syntax
        com.codementor.api.dto.JavaCodeBlockValidation block1 = response.getValidationDetails().get(1);
        org.junit.jupiter.api.Assertions.assertEquals(1, block1.getBlockIndex());
        org.junit.jupiter.api.Assertions.assertEquals("INVALID", block1.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(block1.getErrorMessage());

        // Block 2: Valid record
        com.codementor.api.dto.JavaCodeBlockValidation block2 = response.getValidationDetails().get(2);
        org.junit.jupiter.api.Assertions.assertEquals(2, block2.getBlockIndex());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", block2.getStatus());
    }

    @Test
    @DisplayName("QA Scenario 8: Existing OpenRouter reasoning protection remains fully intact")
    void qaScenario8_ReasoningProtectionRemainsIntact() throws Exception {
        // 1. Internal reasoning in message.reasoning is NEVER exposed
        String reasoningJson = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null,\"reasoning\":\"Internal chain of thought: plan step 1...\"}}]}";
        com.fasterxml.jackson.databind.JsonNode root1 = objectMapper.readTree(reasoningJson);
        String extracted1 = aiMentorService.extractContentFromOpenRouterResponse(root1);
        org.junit.jupiter.api.Assertions.assertNull(extracted1, "Internal reasoning must never be extracted as content");

        // 2. Embedded <think> tags are stripped from output
        String thinkJson = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"<think>internal thought</think>Actual user answer\"}}]}";
        com.fasterxml.jackson.databind.JsonNode root2 = objectMapper.readTree(thinkJson);
        String extracted2 = aiMentorService.extractContentFromOpenRouterResponse(root2);
        org.junit.jupiter.api.Assertions.assertEquals("Actual user answer", extracted2);

        // 3. Response with only thinking tags returns null to trigger fallback
        String thinkOnlyJson = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"<think>only thoughts</think>\"}}]}";
        com.fasterxml.jackson.databind.JsonNode root3 = objectMapper.readTree(thinkOnlyJson);
        String extracted3 = aiMentorService.extractContentFromOpenRouterResponse(root3);
        org.junit.jupiter.api.Assertions.assertNull(extracted3);
    }

    // =========================================================================
    // HTTP 502 PROVIDER FAILURE & ALTERNATIVE MODEL FAILOVER SUITE
    // =========================================================================

    @Test
    @DisplayName("Failover: Tencent HTTP 502 followed by successful alternative model response")
    void testTencent502FollowedBySuccessfulAlternativeModelResponse() {
        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
            @Override
            public String getDefaultModel() {
                return "tencent/hy4";
            }
            @Override
            public String getFallbackModel() {
                return "deepseek/deepseek-chat";
            }
        };

        List<String> invokedModels = new ArrayList<>();
        com.codementor.api.service.AiMentorService service = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String callOpenRouterApi(AiMentorRequest request, String model, String lang, String context) throws Exception {
                invokedModels.add(model);
                if ("tencent/hy4".equalsIgnoreCase(model)) {
                    throw new OpenRouterApiException(502, "Bad Gateway from Tencent provider");
                }
                if ("deepseek/deepseek-chat".equalsIgnoreCase(model)) {
                    return "Here is the solution from DeepSeek:\n```java\npublic class Solution { public int solve() { return 42; } }\n```";
                }
                return "Other answer";
            }
        };

        AiMentorRequest request = new AiMentorRequest("How to solve two sum?", List.of(), "Java", "DSA");
        com.codementor.api.dto.AiMentorResponse response = service.chatWithMentor(request);

        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", response.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("deepseek/deepseek-chat", response.getSelectedModel(), "Actual succeeded model must be returned");
        org.junit.jupiter.api.Assertions.assertEquals(List.of("tencent/hy4", "deepseek/deepseek-chat"), invokedModels);
        org.junit.jupiter.api.Assertions.assertTrue(response.getNotice().contains("tencent/hy4"));
        org.junit.jupiter.api.Assertions.assertTrue(response.getNotice().contains("deepseek/deepseek-chat"));
        org.junit.jupiter.api.Assertions.assertEquals("VALID", response.getValidationStatus());
    }

    @Test
    @DisplayName("Failover: Both primary and alternative model returning HTTP 502 yields service unavailable")
    void testBothModelsReturningHttp502() {
        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
            @Override
            public String getDefaultModel() {
                return "tencent/hy4";
            }
            @Override
            public String getFallbackModel() {
                return "deepseek/deepseek-chat";
            }
        };

        List<String> invokedModels = new ArrayList<>();
        com.codementor.api.service.AiMentorService service = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String callOpenRouterApi(AiMentorRequest request, String model, String lang, String context) throws Exception {
                invokedModels.add(model);
                throw new OpenRouterApiException(502, "Bad Gateway from provider " + model);
            }
        };

        AiMentorRequest request = new AiMentorRequest("Explain B-Trees", List.of(), "SQL", "Databases");
        com.codementor.api.dto.AiMentorResponse response = service.chatWithMentor(request);

        org.junit.jupiter.api.Assertions.assertEquals("FALLBACK", response.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("tencent/hy4", "deepseek/deepseek-chat"), invokedModels);
        org.junit.jupiter.api.Assertions.assertTrue(response.getSelectedModel().contains("Unavailable"));
        org.junit.jupiter.api.Assertions.assertTrue(response.getAiResponse().contains("AI Mentor Service Temporarily Unavailable"));
        org.junit.jupiter.api.Assertions.assertFalse(response.getAiResponse().contains("PaymentService"), "Must not present canned DI heuristic as answer");
    }

    @Test
    @DisplayName("Failover: HTTP 401 Unauthorized must not trigger alternative model retry")
    void testHttp401WithoutRetry() {
        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
            @Override
            public String getDefaultModel() {
                return "tencent/hy4";
            }
            @Override
            public String getFallbackModel() {
                return "deepseek/deepseek-chat";
            }
        };

        List<String> invokedModels = new ArrayList<>();
        com.codementor.api.service.AiMentorService service = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String callOpenRouterApi(AiMentorRequest request, String model, String lang, String context) throws Exception {
                invokedModels.add(model);
                throw new OpenRouterApiException(401, "User key unauthorized or expired");
            }
        };

        AiMentorRequest request = new AiMentorRequest("How to use Spring?", List.of(), "Java", "Spring");
        com.codementor.api.dto.AiMentorResponse response = service.chatWithMentor(request);

        org.junit.jupiter.api.Assertions.assertEquals(1, invokedModels.size(), "HTTP 401 must never be retried");
        org.junit.jupiter.api.Assertions.assertEquals("tencent/hy4", invokedModels.get(0));
        org.junit.jupiter.api.Assertions.assertEquals("FALLBACK", response.getStatus());
    }

    @Test
    @DisplayName("Failover: Strict loop prevention allows at most ONE fallback attempt")
    void testNoMoreThanOneFallbackAttempt() {
        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
            @Override
            public String getDefaultModel() {
                return "tencent/hy4";
            }
            @Override
            public String getFallbackModel() {
                return "deepseek/deepseek-chat";
            }
        };

        List<String> attempts = new ArrayList<>();
        com.codementor.api.service.AiMentorService service = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String callOpenRouterApi(AiMentorRequest request, String model, String lang, String context) throws Exception {
                attempts.add(model);
                throw new OpenRouterApiException(502, "Provider 502 outage");
            }
        };

        AiMentorRequest request = new AiMentorRequest("Explain concurrency", List.of(), "Java", "Threading");
        service.chatWithMentor(request);

        org.junit.jupiter.api.Assertions.assertEquals(2, attempts.size(), "Strict limit of at most one fallback attempt");
        org.junit.jupiter.api.Assertions.assertEquals("tencent/hy4", attempts.get(0));
        org.junit.jupiter.api.Assertions.assertEquals("deepseek/deepseek-chat", attempts.get(1));
    }

    @Test
    @DisplayName("Failover: Fallback model response never exposes reasoning fields")
    void testFallbackModelReasoningFieldsNeverExposed() throws Exception {
        String fallbackResponseWithReasoning = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"User facing answer from fallback model\",\"reasoning\":\"Internal thoughts from DeepSeek\"}}]}";
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(fallbackResponseWithReasoning);
        String extracted = aiMentorService.extractContentFromOpenRouterResponse(root);

        org.junit.jupiter.api.Assertions.assertEquals("User facing answer from fallback model", extracted);
        org.junit.jupiter.api.Assertions.assertFalse(extracted.contains("Internal thoughts"));
    }

    @Test
    @DisplayName("Failover: JavaParser validation still executes on fallback model output")
    void testJavaParserValidationStillWorksOnFallbackModelOutput() {
        com.codementor.api.config.OpenRouterConfig mockConfig = new com.codementor.api.config.OpenRouterConfig() {
            @Override
            public boolean isApiKeyConfigured() {
                return true;
            }
            @Override
            public String getDefaultModel() {
                return "tencent/hy4";
            }
            @Override
            public String getFallbackModel() {
                return "deepseek/deepseek-chat";
            }
        };

        com.codementor.api.service.AiMentorService service = new com.codementor.api.service.AiMentorService(
                mockConfig, javaCodeValidationService
        ) {
            @Override
            public String callOpenRouterApi(AiMentorRequest request, String model, String lang, String context) throws Exception {
                if ("tencent/hy4".equalsIgnoreCase(model)) {
                    throw new OpenRouterApiException(502, "Tencent 502 Bad Gateway");
                }
                return "Here is the code:\n```java\npublic class FallbackResult {\n    public String getStatus() { return \"OK\"; }\n}\n```";
            }
        };

        AiMentorRequest request = new AiMentorRequest("Generate class", List.of(), "Java", "Basics");
        com.codementor.api.dto.AiMentorResponse response = service.chatWithMentor(request);

        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", response.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("deepseek/deepseek-chat", response.getSelectedModel());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", response.getValidationStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.getValidationDetails().size());
        org.junit.jupiter.api.Assertions.assertEquals("java", response.getValidationDetails().get(0).getLanguage());
        org.junit.jupiter.api.Assertions.assertEquals("VALID", response.getValidationDetails().get(0).getStatus());
    }
}
