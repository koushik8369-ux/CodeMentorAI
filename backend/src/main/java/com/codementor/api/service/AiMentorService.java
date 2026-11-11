package com.codementor.api.service;

import com.codementor.api.config.OpenRouterConfig;
import com.codementor.api.dto.AiMentorChatMessage;
import com.codementor.api.dto.AiMentorRequest;
import com.codementor.api.dto.AiMentorResponse;
import com.codementor.api.dto.JavaCodeBlockValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class AiMentorService {

    private static final Logger logger = LoggerFactory.getLogger(AiMentorService.class);

    private final OpenRouterConfig openRouterConfig;
    private final JavaCodeValidationService javaCodeValidationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public AiMentorService(OpenRouterConfig openRouterConfig) {
        this(openRouterConfig, new JavaCodeValidationService());
    }

    @Autowired
    public AiMentorService(OpenRouterConfig openRouterConfig, JavaCodeValidationService javaCodeValidationService) {
        this.openRouterConfig = openRouterConfig;
        this.javaCodeValidationService = javaCodeValidationService != null ? javaCodeValidationService : new JavaCodeValidationService();
    }

    public AiMentorResponse chatWithMentor(AiMentorRequest request) {
        String primaryModel = resolveModel(request.getCustomModel());
        String alternativeModel = resolveAlternativeModel(primaryModel);
        String actualModel = primaryModel;

        String lang = request.getProgrammingLanguage() != null && !request.getProgrammingLanguage().isBlank()
                ? request.getProgrammingLanguage().trim()
                : "General / Polyglot";
        String context = request.getLearningContext() != null && !request.getLearningContext().isBlank()
                ? request.getLearningContext().trim()
                : "Full-Stack Software Engineering";

        String lastErrorNotice = null;
        String primaryErrorNotice = null;
        boolean primaryFailedWith502 = false;
        boolean allProvidersFailed = false;

        if (openRouterConfig.isApiKeyConfigured()) {
            String aiText = null;
            try {
                aiText = callOpenRouterApi(request, primaryModel, lang, context);
            } catch (OpenRouterApiException e) {
                primaryErrorNotice = "Upstream OpenRouter error for " + primaryModel + " (HTTP " + e.getStatusCode() + ": " + e.getResponseSummary() + ").";
                logger.warn("OpenRouter API error for model {} (HTTP {}): {}", primaryModel, e.getStatusCode(), e.getResponseSummary());
                if (isRetryableProviderFailure(e.getStatusCode())) {
                    primaryFailedWith502 = true;
                } else {
                    allProvidersFailed = true;
                    lastErrorNotice = primaryErrorNotice;
                }
            } catch (Exception e) {
                String safeMsg = redactSecrets(e.getMessage());
                primaryErrorNotice = "OpenRouter API call failed for " + primaryModel + " (" + safeMsg + ").";
                logger.warn("OpenRouter API call encountered an error for model {}: {}", primaryModel, safeMsg);
                allProvidersFailed = true;
                lastErrorNotice = primaryErrorNotice;
            }

            // If primary model returned a retryable provider failure (HTTP 502/503/504), retry at most once with alternative model
            if (primaryFailedWith502 && alternativeModel != null && !alternativeModel.equalsIgnoreCase(primaryModel)) {
                logger.info("Primary model '{}' experienced provider failure (HTTP 502). Retrying once with alternative model '{}'",
                        primaryModel, alternativeModel);
                try {
                    aiText = callOpenRouterApi(request, alternativeModel, lang, context);
                    actualModel = alternativeModel;
                } catch (OpenRouterApiException e2) {
                    logger.warn("Alternative model '{}' also failed (HTTP {}): {}",
                            alternativeModel, e2.getStatusCode(), e2.getResponseSummary());
                    lastErrorNotice = primaryErrorNotice + " Alternative model " + alternativeModel + " also failed (HTTP " + e2.getStatusCode() + ": " + e2.getResponseSummary() + ").";
                    allProvidersFailed = true;
                } catch (Exception e2) {
                    String safeMsg2 = redactSecrets(e2.getMessage());
                    logger.warn("Alternative model '{}' failed: {}", alternativeModel, safeMsg2);
                    lastErrorNotice = primaryErrorNotice + " Alternative model " + alternativeModel + " failed (" + safeMsg2 + ").";
                    allProvidersFailed = true;
                }
            }

            // If we successfully received AI content from primary or alternative model:
            if (aiText != null && !aiText.isBlank() && !"null".equalsIgnoreCase(aiText.trim())) {
                String notice = !actualModel.equalsIgnoreCase(primaryModel)
                        ? "Primary model " + primaryModel + " returned provider failure (HTTP 502). Successfully failed over to " + actualModel + "."
                        : null;
                AiMentorResponse response = new AiMentorResponse(aiText.trim(), actualModel, "SUCCESS", lang, context, notice);
                validateAndEnrichResponse(response, actualModel, lang);
                return response;
            }

            if (allProvidersFailed) {
                // Return a clear service-unavailable response rather than presenting a canned heuristic response as a successful AI answer
                String unavailableText = buildServiceUnavailableResponse(primaryModel, alternativeModel, lastErrorNotice);
                String notice = lastErrorNotice != null ? lastErrorNotice : "Upstream AI providers unavailable. Service-unavailable response provided.";
                AiMentorResponse unavailableResponse = new AiMentorResponse(unavailableText, primaryModel + " (Unavailable)", "FALLBACK", lang, context, notice);
                validateAndEnrichResponse(unavailableResponse, primaryModel, lang);
                return unavailableResponse;
            }
        }

        // Graceful fallback response when OPENROUTER_API_KEY is not configured (offline / local development)
        String fallbackAnswer = generateFallbackMentorResponse(request.getMessage(), lang, context);
        String notice = "OpenRouter API key not detected in backend environment. To enable live LLM generation, set OPENROUTER_API_KEY in Render environment variables.";

        AiMentorResponse fallbackResponse = new AiMentorResponse(fallbackAnswer, primaryModel + " (Fallback Heuristic)", "FALLBACK", lang, context, notice);
        validateAndEnrichResponse(fallbackResponse, primaryModel, lang);
        return fallbackResponse;
    }

    private static final Set<String> VERIFIED_MODELS = Set.of(
            "tencent/hy4",
            "deepseek/deepseek-chat",
            "meta-llama/llama-3.3-70b-instruct",
            "openai/gpt-4o-mini"
    );

    public String resolveModel(String customOverride) {
        if (customOverride != null && !customOverride.isBlank()) {
            String trimmed = customOverride.trim();
            // Automatically reroute retired or broken models and sentinels to authoritative default
            if ("google/gemini-2.0-flash-001".equalsIgnoreCase(trimmed)
                    || "anthropic/claude-3.5-sonnet".equalsIgnoreCase(trimmed)
                    || "default".equalsIgnoreCase(trimmed)
                    || "auto".equalsIgnoreCase(trimmed)) {
                logger.info("Custom model '{}' is retired or default sentinel. Routing to authoritative default '{}'",
                        trimmed, openRouterConfig.getDefaultModel());
                return openRouterConfig.getDefaultModel();
            }
            // If the model matches configured default or is in verified list, allow it
            if (trimmed.equalsIgnoreCase(openRouterConfig.getDefaultModel()) || VERIFIED_MODELS.contains(trimmed)) {
                return trimmed;
            }
            // For any other model with valid organization/name format, allow it
            if (trimmed.contains("/") && !trimmed.contains(" ")) {
                return trimmed;
            }
        }
        return openRouterConfig.getDefaultModel();
    }

    public boolean isRetryableProviderFailure(int statusCode) {
        // HTTP 502 (Bad Gateway), HTTP 503 (Service Unavailable), HTTP 504 (Gateway Timeout)
        return statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    public String getFallbackModel() {
        return openRouterConfig.getFallbackModel();
    }

    public String resolveAlternativeModel(String primaryModel) {
        String configuredFallback = openRouterConfig.getFallbackModel();
        if (configuredFallback != null && !configuredFallback.isBlank() && !configuredFallback.equalsIgnoreCase(primaryModel)) {
            return configuredFallback.trim();
        }
        if ("deepseek/deepseek-chat".equalsIgnoreCase(primaryModel)) {
            return "meta-llama/llama-3.3-70b-instruct";
        }
        if ("tencent/hy4".equalsIgnoreCase(primaryModel)) {
            return "deepseek/deepseek-chat";
        }
        return "deepseek/deepseek-chat";
    }

    public String buildServiceUnavailableResponse(String primaryModel, String alternativeModel, String errorNotice) {
        String models = (alternativeModel != null && !alternativeModel.equalsIgnoreCase(primaryModel))
                ? primaryModel + " and alternative model " + alternativeModel
                : primaryModel;

        return "### ⚠️ AI Mentor Service Temporarily Unavailable\n\n"
                + "The upstream AI provider (" + models + ") encountered a temporary provider outage (HTTP 502 / Bad Gateway).\n\n"
                + "Our resilience pipeline automatically attempted to route your request to an alternative verified model, but all upstream providers are currently reporting downtime.\n\n"
                + "**What you can do:**\n"
                + "- **Retry shortly**: Upstream OpenRouter gateway errors are usually transient and recover quickly.\n"
                + "- **Switch Models**: Use the model selector in the top bar to try another verified model (e.g., **DeepSeek V3**, **Llama 3.3 70B**, or **GPT-4o Mini**).\n"
                + "- **Practice & Review**: In the meantime, you can review your saved conversation history, inspect previous interview questions, or take a mock interview.";
    }

    public boolean isConfigured() {
        return openRouterConfig.isApiKeyConfigured();
    }

    public String redactSecrets(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String sanitized = text;
        String apiKey = openRouterConfig.getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            sanitized = sanitized.replace(apiKey, "[REDACTED_API_KEY]");
        }
        sanitized = sanitized.replaceAll("(?i)Bearer\\s+[a-zA-Z0-9_\\-\\.]+", "Bearer [REDACTED]");
        sanitized = sanitized.replaceAll("sk-or-v1-[a-zA-Z0-9]+", "sk-or-v1-[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)key=[a-zA-Z0-9_\\-]+", "key=[REDACTED]");
        return sanitized;
    }

    public String extractErrorMessage(String rawBody, int statusCode) {
        if (rawBody == null || rawBody.isBlank()) {
            return "No response body returned from upstream";
        }
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                String msg = errorNode.path("message").asText();
                if (msg != null && !msg.isBlank()) {
                    return redactSecrets(msg);
                }
            }
        } catch (Exception ignored) {
            // Not valid JSON
        }
        String truncated = rawBody.length() > 200 ? rawBody.substring(0, 200) + "..." : rawBody;
        return redactSecrets(truncated);
    }

    public boolean isReasoningEffortSupported(String model) {
        if (model == null) return false;
        String m = model.toLowerCase().trim();
        return m.startsWith("tencent/hy4") || m.startsWith("tencent/hy3") || m.contains("hunyuan");
    }

    public Map<String, Object> buildRequestPayload(AiMentorRequest request, String model, String lang, String context, boolean includeReasoning) {
        String systemPrompt = buildSystemPrompt(lang, context);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Append recent conversation history (capped at last 12 messages for token sanity)
        List<AiMentorChatMessage> history = request.getConversationHistory();
        if (history != null && !history.isEmpty()) {
            int startIdx = Math.max(0, history.size() - 12);
            for (int i = startIdx; i < history.size(); i++) {
                AiMentorChatMessage turn = history.get(i);
                if (turn.getContent() != null && !turn.getContent().isBlank()) {
                    String role = "assistant".equalsIgnoreCase(turn.getRole()) ? "assistant" : "user";
                    messages.add(Map.of("role", role, "content", turn.getContent().trim()));
                }
            }
        }

        // Append active user query
        messages.add(Map.of("role", "user", "content", request.getMessage().trim()));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("temperature", 0.3);
        payload.put("max_tokens", 4096);

        if (includeReasoning && isReasoningEffortSupported(model)) {
            Map<String, Object> reasoning = new HashMap<>();
            reasoning.put("effort", "none");
            reasoning.put("exclude", true);
            payload.put("reasoning", reasoning);
        }

        return payload;
    }

    public String callOpenRouterApi(AiMentorRequest request, String model, String lang, String context) throws Exception {
        boolean tryWithReasoning = isReasoningEffortSupported(model);
        Map<String, Object> payload = buildRequestPayload(request, model, lang, context, tryWithReasoning);
        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openRouterConfig.getApiUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openRouterConfig.getApiKey())
                .header("HTTP-Referer", "https://codementor.ai")
                .header("X-Title", "CodeMentor AI Programming Mentor")
                .timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        // If HTTP 400 occurred and we sent reasoning parameter, retry once without reasoning parameter
        if (response.statusCode() == 400 && tryWithReasoning) {
            String safeBody = redactSecrets(response.body() != null ? response.body() : "");
            logger.warn("OpenRouter rejected reasoning configuration for model {} (HTTP 400: {}). Retrying without reasoning parameter.", model, safeBody);
            Map<String, Object> retryPayload = buildRequestPayload(request, model, lang, context, false);
            String retryJsonBody = objectMapper.writeValueAsString(retryPayload);

            HttpRequest retryRequest = HttpRequest.newBuilder()
                    .uri(URI.create(openRouterConfig.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openRouterConfig.getApiKey())
                    .header("HTTP-Referer", "https://codementor.ai")
                    .header("X-Title", "CodeMentor AI Programming Mentor")
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(retryJsonBody))
                    .build();

            response = httpClient.send(retryRequest, HttpResponse.BodyHandlers.ofString());
        }

        String rawBody = response.body() != null ? response.body() : "";

        if (response.statusCode() != 200) {
            String safeBody = redactSecrets(rawBody);
            logger.warn("OpenRouter API returned HTTP {}: {}", response.statusCode(), safeBody);
            logSafeResponseDiagnostics(response.statusCode(), model, null, safeBody);
            String errorSummary = extractErrorMessage(rawBody, response.statusCode());
            throw new OpenRouterApiException(response.statusCode(), errorSummary);
        }

        JsonNode rootNode = objectMapper.readTree(rawBody);
        logSafeResponseDiagnostics(response.statusCode(), model, rootNode, rawBody);

        String extractedContent = extractContentFromOpenRouterResponse(rootNode);

        if (extractedContent == null || extractedContent.isBlank() || "null".equalsIgnoreCase(extractedContent.trim())) {
            logger.warn("OpenRouter model '{}' returned 200 OK, but no user-facing content could be extracted. (Finish reason / structure captured in diagnostics above).", model);
            throw new RuntimeException("OpenRouter response did not contain user-facing content (null, empty, or reasoning-only).");
        }

        return extractedContent.trim();
    }

    public void logSafeResponseDiagnostics(int statusCode, String model, JsonNode rootNode, String rawBody) {
        try {
            if (rootNode != null && !rootNode.isMissingNode() && !rootNode.isNull()) {
                JsonNode choices = rootNode.path("choices");
                int choiceCount = choices.isArray() ? choices.size() : 0;
                JsonNode firstChoice = choiceCount > 0 ? choices.get(0) : null;
                String finishReason = firstChoice != null ? firstChoice.path("finish_reason").asText("N/A") : "none";
                JsonNode messageNode = firstChoice != null ? firstChoice.path("message") : null;
                String contentNodeType = (messageNode != null && !messageNode.path("content").isMissingNode())
                        ? messageNode.path("content").getNodeType().name()
                        : "MISSING";
                boolean hasReasoning = messageNode != null
                        && !messageNode.path("reasoning").isMissingNode()
                        && !messageNode.path("reasoning").isNull();
                boolean hasReasoningContent = messageNode != null
                        && !messageNode.path("reasoning_content").isMissingNode()
                        && !messageNode.path("reasoning_content").isNull();

                JsonNode usage = rootNode.path("usage");
                int promptTokens = usage.path("prompt_tokens").asInt(-1);
                int completionTokens = usage.path("completion_tokens").asInt(-1);
                int totalTokens = usage.path("total_tokens").asInt(-1);

                logger.info("OpenRouter response diagnostics: status={}, model={}, choices={}, finish_reason={}, contentNodeType={}, hasReasoning={}, hasReasoningContent={}, tokens=[prompt:{}, completion:{}, total:{}]",
                        statusCode, model, choiceCount, finishReason, contentNodeType, hasReasoning, hasReasoningContent, promptTokens, completionTokens, totalTokens);
            } else {
                String safeBody = redactSecrets(rawBody != null ? rawBody : "");
                logger.info("OpenRouter response diagnostics: status={}, model={}, rawBodyLength={}", statusCode, model, safeBody.length());
            }
        } catch (Exception e) {
            logger.debug("Failed to log safe diagnostics: {}", e.getMessage());
        }
    }

    private static final java.util.regex.Pattern THINK_TAG_PATTERN =
            java.util.regex.Pattern.compile("(?is)<(think|thought)[^>]*>.*?</\\1>");
    private static final java.util.regex.Pattern LOOSE_THINK_TAG_PATTERN =
            java.util.regex.Pattern.compile("(?is)</?(think|thought)[^>]*>");

    public String cleanUserFacingContent(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = THINK_TAG_PATTERN.matcher(text).replaceAll("");
        cleaned = LOOSE_THINK_TAG_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned.trim();
        if (cleaned.isBlank() || "null".equalsIgnoreCase(cleaned)) {
            return null;
        }
        return cleaned;
    }

    public String extractContentFromOpenRouterResponse(JsonNode rootNode) {
        if (rootNode == null || rootNode.isMissingNode() || rootNode.isNull()) {
            return null;
        }

        JsonNode choices = rootNode.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }

        JsonNode firstChoice = choices.get(0);
        JsonNode messageNode = firstChoice.path("message");
        JsonNode contentNode = messageNode.path("content");

        // 1. Plain string content
        if (!contentNode.isMissingNode() && !contentNode.isNull()) {
            if (contentNode.isTextual()) {
                return cleanUserFacingContent(contentNode.asText());
            }

            // 2. Array-based structured content, e.g. [{"type": "text", "text": "..."}]
            if (contentNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode part : contentNode) {
                    if (part.isTextual()) {
                        sb.append(part.asText());
                    } else if (part.isObject()) {
                        String partType = part.path("type").asText("");
                        // Explicitly skip any reasoning/thought parts
                        if ("reasoning".equalsIgnoreCase(partType) || "thought".equalsIgnoreCase(partType) || "thinking".equalsIgnoreCase(partType)) {
                            continue;
                        }
                        JsonNode textNode = part.path("text");
                        if (!textNode.isMissingNode() && !textNode.isNull() && !textNode.asText().isBlank()) {
                            sb.append(textNode.asText());
                        } else {
                            JsonNode contentSubNode = part.path("content");
                            if (!contentSubNode.isMissingNode() && !contentSubNode.isNull() && !contentSubNode.asText().isBlank()) {
                                sb.append(contentSubNode.asText());
                            }
                        }
                    }
                }
                return cleanUserFacingContent(sb.toString());
            }

            // 3. Object-based structured content with "text" field
            if (contentNode.isObject()) {
                JsonNode textNode = contentNode.path("text");
                if (!textNode.isMissingNode() && !textNode.isNull()) {
                    return cleanUserFacingContent(textNode.asText());
                }
            }
        }

        // 4. Legacy text completions format in choices[0].text
        JsonNode choiceTextNode = firstChoice.path("text");
        if (!choiceTextNode.isMissingNode() && !choiceTextNode.isNull() && choiceTextNode.isTextual()) {
            return cleanUserFacingContent(choiceTextNode.asText());
        }

        // 5. Delta format in choices[0].delta.content (if returned by streaming/delta endpoints)
        JsonNode deltaContent = firstChoice.path("delta").path("content");
        if (!deltaContent.isMissingNode() && !deltaContent.isNull() && deltaContent.isTextual()) {
            return cleanUserFacingContent(deltaContent.asText());
        }

        return null;
    }

    public static class OpenRouterApiException extends RuntimeException {
        private final int statusCode;
        private final String responseSummary;

        public OpenRouterApiException(int statusCode, String responseSummary) {
            super("OpenRouter returned HTTP " + statusCode + ": " + responseSummary);
            this.statusCode = statusCode;
            this.responseSummary = responseSummary;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getResponseSummary() {
            return responseSummary;
        }
    }

    private String buildSystemPrompt(String language, String learningContext) {
        return "You are CodeMentor AI's Principal Software Engineering Mentor—an elite, friendly, and patient programming mentor for aspiring and practicing software engineers.\n\n"
                + "CURRENT CONTEXT:\n"
                + "- Primary Programming Language / Tech Stack: " + language + "\n"
                + "- Learning Context: " + learningContext + "\n\n"
                + "YOUR CORE DOMAINS OF EXPERTISE:\n"
                + "1. Java programming, modern Java (Java 17/21 features, records, virtual threads, pattern matching).\n"
                + "2. Object-Oriented Programming (SOLID principles, clean architecture, design patterns, encapsulation, polymorphism).\n"
                + "3. Data Structures & Algorithms (arrays, linked lists, trees, graphs, heaps, dynamic programming, two pointers, binary search).\n"
                + "4. Systematic problem-solving techniques and algorithmic complexity analysis (time O(n), auxiliary space).\n"
                + "5. Modern React & TypeScript (hooks, state management, render cycles, typing generics, best practices).\n"
                + "6. Spring Boot 3 & RESTful API Architecture (dependency injection, controller-service-repository patterns, security, exception handling).\n"
                + "7. Full-stack development workflows and contract design between React and Spring Boot.\n"
                + "8. SQL, relational database indexing, ACID transactions, normalization, and query performance.\n"
                + "9. Code reviews, static analysis, refactoring, and debugging techniques.\n"
                + "10. Technical interview preparation (FAANG-style technical screens, behavioral STAR responses, system design).\n"
                + "11. Scalable project architecture and operational development guidance.\n\n"
                + "STRUCTURED RESPONSE STYLE FOR TECHNICAL & PROGRAMMING QUESTIONS:\n"
                + "When answering coding or technical queries, always follow this clear, structured format:\n"
                + "1. **Understand & Clarify**: Briefly validate the core problem, question, or requirements.\n"
                + "2. **Explain the Concept**: Explain the underlying computer science or architectural mechanism clearly with helpful intuition.\n"
                + "3. **Practical Code Example**: Provide clean, idiomatic, fully working, and nicely formatted code using markdown code blocks.\n"
                + "4. **Step-by-Step Code Walkthrough**: Explain key lines and execution flow step by step.\n"
                + "5. **Common Pitfalls & Mistakes**: Highlight subtle traps, edge cases, or antipatterns candidates often make.\n"
                + "6. **Improvements & Next Steps**: Suggest time/space optimizations, real-world alternatives, or thought-provoking follow-up questions to encourage independent growth.\n\n"
                + "TONE & BEHAVIOR:\n"
                + "- Warm, encouraging, and collaborative.\n"
                + "- Speak as an experienced colleague mentoring someone who wants to achieve technical mastery.\n"
                + "- Never be condescending; break down complex ideas with clarity.";
    }

    private String generateFallbackMentorResponse(String message, String language, String learningContext) {
        String lower = message != null ? message.toLowerCase() : "";

        // Heuristic detection based on the user's question
        if (lower.contains("spring") || lower.contains("bean") || lower.contains("autowired") || lower.contains("dependency injection") || lower.contains("ioc")) {
            return "### 1. Understanding the Concept: Spring Boot Dependency Injection\n\n"
                    + "At its core, **Dependency Injection (DI)** is a software design pattern where objects receive their dependencies from an external assembler (the **Spring IoC Container**) rather than creating them directly with `new`.\n\n"
                    + "### 2. How it Works\n"
                    + "Instead of tightly coupling your classes, Spring manages the lifecycle of your components (called **Beans**) and injects them where needed. Constructor injection is preferred over field injection because it ensures immutability, facilitates unit testing, and prevents `NullPointerException`s.\n\n"
                    + "### 3. Practical Code Example\n"
                    + "```java\n"
                    + "@Service\n"
                    + "public class PaymentService {\n"
                    + "    private final PaymentGateway gateway;\n\n"
                    + "    // Constructor injection: Spring automatically resolves PaymentGateway\n"
                    + "    public PaymentService(PaymentGateway gateway) {\n"
                    + "        this.gateway = Objects.requireNonNull(gateway, \"gateway must not be null\");\n"
                    + "    }\n\n"
                    + "    public TransactionResult process(BigDecimal amount) {\n"
                    + "        return gateway.charge(amount);\n"
                    + "    }\n"
                    + "}\n"
                    + "```\n\n"
                    + "### 4. Step-by-Step Walkthrough\n"
                    + "1. `@Service` registers `PaymentService` as a managed singleton bean in the Spring ApplicationContext.\n"
                    + "2. During application startup, Spring scans constructor parameters, finds the matching `PaymentGateway` bean, and injects it.\n"
                    + "3. The field is `private final`, ensuring thread safety and preventing reassignment.\n\n"
                    + "### 5. Common Pitfalls\n"
                    + "- **Field injection (`@Autowired private Gateway gateway`)**: Makes unit testing difficult without starting Spring, and hides dependencies.\n"
                    + "- **Circular dependencies**: Bean A requires Bean B, which requires Bean A. Redesign the boundary or use events.\n\n"
                    + "### 6. Mentoring Tip\n"
                    + "In technical interviews, interviewers love hearing how DI enables test-driven development (TDD) via mocks (`Mockito.mock(...)`).";
        }

        if (lower.contains("react") || lower.contains("useeffect") || lower.contains("hook") || lower.contains("state") || lower.contains("props")) {
            return "### 1. Understanding the Question\n\n"
                    + "Managing side effects and state synchronizations correctly is one of the most critical aspects of production React and TypeScript applications.\n\n"
                    + "### 2. The Concept\n"
                    + "React components must be pure functions of their props and state. Side effects (data fetching, DOM subscriptions, timers) belong inside `useEffect`, synchronized with an explicit dependency array.\n\n"
                    + "### 3. Practical Example: Safe Async Fetching with Cleanup\n"
                    + "```typescript\n"
                    + "import React, { useState, useEffect } from 'react';\n\n"
                    + "interface CandidateData {\n"
                    + "  id: string;\n"
                    + "  name: string;\n"
                    + "}\n\n"
                    + "export const CandidateProfile: React.FC<{ candidateId: string }> = ({ candidateId }) => {\n"
                    + "  const [data, setData] = useState<CandidateData | null>(null);\n"
                    + "  const [loading, setLoading] = useState<boolean>(true);\n\n"
                    + "  useEffect(() => {\n"
                    + "    let isCancelled = false;\n"
                    + "    setLoading(true);\n\n"
                    + "    fetch(`/api/candidates/${candidateId}`)\n"
                    + "      .then(res => res.json())\n"
                    + "      .then(result => {\n"
                    + "        if (!isCancelled) {\n"
                    + "          setData(result);\n"
                    + "          setLoading(false);\n"
                    + "        }\n"
                    + "      })\n"
                    + "      .catch(() => !isCancelled && setLoading(false));\n\n"
                    + "    // Cleanup function cancels out stale race conditions\n"
                    + "    return () => {\n"
                    + "      isCancelled = true;\n"
                    + "    };\n"
                    + "  }, [candidateId]);\n\n"
                    + "  if (loading) return <div>Loading candidate profile...</div>;\n"
                    + "  return <div>{data?.name}</div>;\n"
                    + "};\n"
                    + "```\n\n"
                    + "### 4. Step-by-Step Walkthrough\n"
                    + "1. The `isCancelled` flag tracks whether the component unmounted or `candidateId` changed before the fetch resolved.\n"
                    + "2. The cleanup return function executes before the next effect or upon unmount, preventing memory leaks and state updates on unmounted trees.\n\n"
                    + "### 5. Common Pitfalls\n"
                    + "- Forgetting the dependency array causes infinite re-render loops.\n"
                    + "- Putting object/array literals directly in dependencies triggers effect re-runs every render.\n\n"
                    + "### 6. Recommended Next Step\n"
                    + "Consider using TanStack Query (React Query) for caching, automated deduplication, and background refetching in enterprise apps.";
        }

        if (lower.contains("sql") || lower.contains("database") || lower.contains("index") || lower.contains("join") || lower.contains("acid")) {
            return "### 1. Understanding Relational Query Optimization & Indexing\n\n"
                    + "Database performance in production often hinges on understanding how queries interact with disk I/O and B-Tree indexes.\n\n"
                    + "### 2. Core Concept: B-Tree Indexes\n"
                    + "A database index is a data structure (typically a balanced B-Tree) that allows the database engine to find specific rows in $O(\\log N)$ time rather than conducting an expensive Full Table Scan ($O(N)$ disk reads).\n\n"
                    + "### 3. Practical Example: Composite Indexing\n"
                    + "```sql\n"
                    + "-- Table with millions of candidate submissions\n"
                    + "CREATE TABLE submissions (\n"
                    + "    id BIGSERIAL PRIMARY KEY,\n"
                    + "    candidate_id VARCHAR(64) NOT NULL,\n"
                    + "    topic VARCHAR(32) NOT NULL,\n"
                    + "    score INT NOT NULL,\n"
                    + "    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP\n"
                    + ");\n\n"
                    + "-- Optimal composite index for querying recent scores by topic:\n"
                    + "CREATE INDEX idx_submissions_topic_created \n"
                    + "ON submissions (topic, created_at DESC);\n\n"
                    + "-- Efficient query utilizing index scan:\n"
                    + "EXPLAIN ANALYZE\n"
                    + "SELECT * FROM submissions\n"
                    + "WHERE topic = 'Dynamic Programming'\n"
                    + "ORDER BY created_at DESC\n"
                    + "LIMIT 10;\n"
                    + "```\n\n"
                    + "### 4. Step-by-Step Walkthrough\n"
                    + "1. The composite index orders entries first by `topic`, then by `created_at DESC`.\n"
                    + "2. The engine immediately navigates to the 'Dynamic Programming' branch and reads the first 10 rows in already-sorted order.\n"
                    + "3. `EXPLAIN ANALYZE` confirms that an **Index Scan** is performed without any disk sort.\n\n"
                    + "### 5. Common Pitfalls\n"
                    + "- **Leftmost Prefix Violation**: An index on `(A, B)` cannot be used for queries filtering only on `WHERE B = ?`.\n"
                    + "- **Over-indexing**: Every write (INSERT/UPDATE/DELETE) must maintain all indexes, which degrades write throughput.\n\n"
                    + "### 6. Mentoring Advice\n"
                    + "In senior system design interviews, explain indexing trade-offs: faster reads vs slower writes and index bloat.";
        }

        if (lower.contains("algorithm") || lower.contains("dsa") || lower.contains("leetcode") || lower.contains("complexity") || lower.contains("array") || lower.contains("tree") || lower.contains("graph")) {
            return "### 1. Problem-Solving Strategy: The 5-Step Interview Framework\n\n"
                    + "When tackling data structure and algorithm problems in top-tier technical interviews, systematic communication is just as important as the code.\n\n"
                    + "### 2. The Algorithmic Pattern Framework\n"
                    + "Before writing any code, identify the underlying pattern:\n"
                    + "- **Sorted arrays / subarrays**: Two Pointers, Sliding Window, or Binary Search.\n"
                    + "- **Hierarchies / Decisions**: Recursion, Tree DFS/BFS, Backtracking.\n"
                    + "- **Optimal Substructure / Overlapping subproblems**: Dynamic Programming (Memoization or Tabulation).\n"
                    + "- **Shortest path / Networks**: Breadth-First Search (unweighted) or Dijkstra's (weighted).\n\n"
                    + "### 3. Practical Code Example: Two-Pointer Technique\n"
                    + "```java\n"
                    + "public class TwoSumSorted {\n"
                    + "    /**\n"
                    + "     * Finds two numbers in a sorted array that sum to target.\n"
                    + "     * Time Complexity: O(n)\n"
                    + "     * Space Complexity: O(1)\n"
                    + "     */\n"
                    + "    public static int[] twoSum(int[] numbers, int target) {\n"
                    + "        int left = 0;\n"
                    + "        int right = numbers.length - 1;\n\n"
                    + "        while (left < right) {\n"
                    + "            int sum = numbers[left] + numbers[right];\n"
                    + "            if (sum == target) {\n"
                    + "                return new int[]{left + 1, right + 1}; // 1-indexed\n"
                    + "            } else if (sum < target) {\n"
                    + "                left++;  // Need a larger sum\n"
                    + "            } else {\n"
                    + "                right--; // Need a smaller sum\n"
                    + "            }\n"
                    + "        }\n"
                    + "        return new int[]{};\n"
                    + "    }\n"
                    + "}\n"
                    + "```\n\n"
                    + "### 4. Step-by-Step Walkthrough\n"
                    + "1. Initialize two pointers at opposite extremes (`left = 0`, `right = length - 1`).\n"
                    + "2. Because the array is sorted, if `sum < target`, incrementing `left` is the only move that can increase the sum.\n"
                    + "3. Conversely, if `sum > target`, decrementing `right` decreases the sum.\n"
                    + "4. Each iteration discards one candidate in $O(1)$ time, guaranteeing $O(n)$ termination with zero auxiliary memory.\n\n"
                    + "### 5. Common Pitfalls\n"
                    + "- Integer overflow when summing large integers (use `(long) a + b` if values approach $2^{31}-1$).\n"
                    + "- Off-by-one bounds errors on loop conditions (`left < right` vs `left <= right`).\n\n"
                    + "### 6. Suggested Practice\n"
                    + "Try extending this pattern to 3Sum ($O(n^2)$) and Container With Most Water!";
        }

        // General Comprehensive Mentor Response
        return "### 1. Understanding Your Question\n\n"
                + "You asked: *\"" + message + "\"*\n\n"
                + "Let's explore this thoroughly from an engineering and architectural perspective in the context of **" + language + "** and **" + learningContext + "**.\n\n"
                + "### 2. Core Architectural & Conceptual Principles\n\n"
                + "In production software systems, robust design requires balancing three fundamental pillars:\n"
                + "1. **Maintainability & Modularity**: Keeping code decoupled, testable, and intuitive for new team members.\n"
                + "2. **Algorithmic & Operational Efficiency**: Ensuring predictable execution profiles (minimizing $O(n)$ time complexity and resource leaks).\n"
                + "3. **Failure Isolation & Resilience**: Anticipating network partitions, input boundary violations, and edge cases.\n\n"
                + "### 3. Practical Implementation Example\n\n"
                + "Here is how to structure a clean, idiomatic implementation in " + language + ":\n\n"
                + "```" + (language.toLowerCase().contains("java") ? "java" : (language.toLowerCase().contains("type") || language.toLowerCase().contains("react") ? "typescript" : "text")) + "\n"
                + "// Production-grade idiomatic design pattern\n"
                + "public class SolutionEngine {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(SolutionEngine.class);\n\n"
                + "    public ExecutionResult executeTask(TaskContext context) {\n"
                + "        if (context == null || context.getPayload() == null) {\n"
                + "            throw new IllegalArgumentException(\"Context payload must not be null\");\n"
                + "        }\n\n"
                + "        try {\n"
                + "            // 1. Process data with optimal time complexity\n"
                + "            return processInternal(context);\n"
                + "        } catch (Exception ex) {\n"
                + "            logger.error(\"Operation failed safely: {}\", ex.getMessage());\n"
                + "            return ExecutionResult.failure(\"Task execution failed gracefully\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "```\n\n"
                + "### 4. Step-by-Step Walkthrough\n"
                + "1. **Validation at the Boundary**: Inputs are checked before any heavy computing begins, preventing `NullPointerException`s.\n"
                + "2. **Encapsulation**: Internal business logic is isolated within `processInternal`, exposing only a clean public interface.\n"
                + "3. **Error Boundaries**: Unhandled exceptions are caught, logged without leaking secrets, and transformed into structured error contracts.\n\n"
                + "### 5. Common Traps & Antipatterns\n"
                + "- **Premature Optimization**: Don't optimize before measuring with profiling tools.\n"
                + "- **Ignoring Edge Cases**: Empty collections, null references, and concurrent writes are where 80% of production outages occur.\n\n"
                + "### 6. Mentoring Recommendations & Next Steps\n"
                + "- Would you like to dive deeper into testing strategies (e.g. Unit testing with JUnit/Jest or Integration testing)?\n"
                + "- If you have a specific code snippet you are currently writing, paste it here and we can conduct an in-depth code review together!";
    }

    public String requestSyntaxCorrection(String model, String brokenCode, String errorMessage) {
        if (!openRouterConfig.isApiKeyConfigured()) {
            return null;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are a precise Java code syntax repair assistant. Fix the syntax errors in the provided Java snippet so it is valid under Java 21 grammar. Preserve the original logic, variable names, and intended functionality. Return ONLY the corrected Java code inside a single ```java ... ``` code block without additional conversational commentary."));
            messages.add(Map.of("role", "user", "content",
                    "The following Java code failed Java 21 syntax validation.\nParser Error: " + errorMessage
                            + "\n\nOriginal Code:\n```java\n" + brokenCode + "\n```\n\nProvide the corrected Java code:"));
            payload.put("messages", messages);
            payload.put("temperature", 0.1);
            payload.put("max_tokens", 4096);

            String jsonBody = objectMapper.writeValueAsString(payload);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(openRouterConfig.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openRouterConfig.getApiKey())
                    .header("HTTP-Referer", "https://codementor.ai")
                    .header("X-Title", "CodeMentor AI Java Syntax Validator")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                JsonNode root = objectMapper.readTree(response.body());
                String extracted = extractContentFromOpenRouterResponse(root);
                if (extracted != null && !extracted.isBlank()) {
                    List<JavaCodeValidationService.ExtractedBlock> blocks = javaCodeValidationService.extractCodeBlocks(extracted);
                    if (!blocks.isEmpty()) {
                        return blocks.get(0).getCode().trim();
                    }
                    return extracted.trim();
                }
            }
        } catch (Exception e) {
            logger.warn("Syntax correction request to OpenRouter failed: {}", redactSecrets(e.getMessage()));
        }
        return null;
    }

    public void validateAndEnrichResponse(AiMentorResponse response, String model, String conversationLanguage) {
        if (response == null || response.getAiResponse() == null) {
            return;
        }
        String text = response.getAiResponse();
        List<JavaCodeValidationService.ExtractedBlock> blocks = javaCodeValidationService.extractCodeBlocks(text);
        if (blocks.isEmpty()) {
            response.setValidationStatus("NOT_CHECKED");
            response.setValidationSummary("No code blocks detected in response.");
            return;
        }

        List<JavaCodeBlockValidation> details = new ArrayList<>();
        Map<Integer, String> replacements = new HashMap<>();
        boolean hasJava = false;
        boolean hasInvalid = false;
        boolean anyCorrected = false;

        for (JavaCodeValidationService.ExtractedBlock block : blocks) {
            boolean isJava = javaCodeValidationService.isJavaBlock(block.getLanguage(), conversationLanguage, block.getCode());
            if (!isJava) {
                String lang = (block.getLanguage() != null && !block.getLanguage().isBlank()) ? block.getLanguage() : "text";
                details.add(new JavaCodeBlockValidation(
                        block.getIndex(),
                        lang,
                        "NOT_CHECKED",
                        false,
                        null,
                        "Syntax validation not applicable for " + lang + " snippet"
                ));
                continue;
            }

            hasJava = true;
            JavaCodeValidationService.ValidationResult result = javaCodeValidationService.validateJavaSnippet(block.getCode());

            if (result.isValid()) {
                details.add(new JavaCodeBlockValidation(
                        block.getIndex(),
                        "java",
                        "VALID",
                        false,
                        null,
                        result.getMessage()
                ));
            } else {
                // Attempt single correction if API is configured
                boolean correctedSuccess = false;
                String correctedCode = null;
                if (openRouterConfig.isApiKeyConfigured()) {
                    correctedCode = requestSyntaxCorrection(model, block.getCode(), result.getMessage());
                    if (correctedCode != null && !correctedCode.isBlank()) {
                        JavaCodeValidationService.ValidationResult recheck = javaCodeValidationService.validateJavaSnippet(correctedCode);
                        if (recheck.isValid()) {
                            correctedSuccess = true;
                            anyCorrected = true;
                            replacements.put(block.getIndex(), correctedCode);
                            details.add(new JavaCodeBlockValidation(
                                    block.getIndex(),
                                    "java",
                                    "VALID",
                                    true,
                                    null,
                                    "Java syntax automatically corrected and validated (Java 21 parser)"
                            ));
                        }
                    }
                }

                if (!correctedSuccess) {
                    hasInvalid = true;
                    details.add(new JavaCodeBlockValidation(
                            block.getIndex(),
                            "java",
                            "INVALID",
                            false,
                            result.getMessage(),
                            "Syntax warning: " + result.getMessage()
                    ));
                }
            }
        }

        // If any blocks were corrected, update the text in reverse order to preserve indices
        if (!replacements.isEmpty()) {
            StringBuilder sb = new StringBuilder(text);
            for (int i = blocks.size() - 1; i >= 0; i--) {
                JavaCodeValidationService.ExtractedBlock b = blocks.get(i);
                if (replacements.containsKey(b.getIndex())) {
                    String langTag = (b.getLanguage() != null && !b.getLanguage().isBlank()) ? b.getLanguage() : "java";
                    String newBlockMarkdown = "```" + langTag + "\n" + replacements.get(b.getIndex()) + "\n```";
                    sb.replace(b.getStartIndex(), b.getEndIndex(), newBlockMarkdown);
                }
            }
            response.setAiResponse(sb.toString());
        }

        response.setValidationDetails(details);
        if (!hasJava) {
            response.setValidationStatus("NOT_CHECKED");
            response.setValidationSummary("No Java code snippets present for syntax validation.");
        } else if (hasInvalid) {
            response.setValidationStatus("INVALID");
            response.setValidationSummary("One or more Java code snippets contain syntax parse issues. Check the highlighted warnings.");
        } else {
            response.setValidationStatus("VALID");
            if (anyCorrected) {
                response.setValidationSummary("Java syntax was automatically repaired and validated with Java 21 parser.");
            } else {
                response.setValidationSummary("All Java code snippets validated successfully against Java 21 syntax grammar.");
            }
        }
    }
}
