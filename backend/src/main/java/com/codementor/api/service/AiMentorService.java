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
                + "1. Java programming, modern Java (Java 10 local variable type inference `var`, Java 16+ records, Java 17 LTS sealed types/pattern matching, and Java 21 LTS finalized virtual threads & preview structured concurrency).\n"
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
                + "MODERN JAVA TECHNICAL ACCURACY RULES:\n"
                + "- Local variable type inference (`var`) was introduced in **Java 10** (JEP 286). It applies to local variables with initializers (and lambda parameters since Java 11), never to fields, method parameters, or return types.\n"
                + "- Java **records** (standardized in Java 16) are **shallowly immutable**: component fields are `final` and cannot be reassigned, but mutable objects held by components (e.g., `List`, `Map`, arrays) can still be mutated unless defensively copied (such as `List.copyOf` in a compact constructor).\n"
                + "- **Virtual threads** (Project Loom, JEP 444) are **finalized in Java 21** and available for production use without preview flags.\n"
                + "- **Structured concurrency** (`StructuredTaskScope`, JEP 453) is a **preview feature in Java 21** (requires `--enable-preview` in Java 21).\n"
                + "- **Static Syntax vs. Execution Honesty**: Never claim that provided code was compiled or executed unless an actual compiler or runtime ran it. CodeMentor AI checks Java snippets solely via static syntax parsing (JavaParser).\n\n"
                + "STRUCTURED RESPONSE STYLE FOR TECHNICAL & PROGRAMMING QUESTIONS:\n"
                + "When answering coding or technical queries, always follow this clear, 5-section structured format:\n"
                + "1. **Understand & Clarify**: Briefly validate the core problem, question, or requirements.\n"
                + "2. **Explain the Concept**: Explain the underlying computer science, language version semantics, or architectural mechanism clearly with helpful intuition.\n"
                + "3. **Practical Code Example**: Provide clean, idiomatic, syntactically valid, and nicely formatted code using markdown code blocks.\n"
                + "4. **Step-by-Step Code Walkthrough**: Explain key lines, design trade-offs, and behavior step by step.\n"
                + "5. **Cheat Sheet**: Provide a scannable summary of key rules, version/complexity notes, common pitfalls, and next steps.\n\n"
                + "TONE & BEHAVIOR:\n"
                + "- Warm, encouraging, and collaborative.\n"
                + "- Speak as an experienced colleague mentoring someone who wants to achieve technical mastery.\n"
                + "- Never be condescending; break down complex ideas with clarity.";
    }

    private String generateFallbackMentorResponse(String message, String language, String learningContext) {
        String lower = message != null ? message.toLowerCase() : "";

        // Heuristic detection for Modern Java (var, records, virtual threads, structured concurrency)
        if (lower.contains("record") || lower.contains("virtual thread") || lower.contains("structured concurrency")
                || lower.contains("structuredtaskscope") || lower.contains("type inference") || lower.contains("`var`")
                || lower.matches(".*\\bvar\\b.*") || lower.contains("java 10") || lower.contains("java 21") || lower.contains("loom")) {
            return "### 1. Understand & Clarify\n\n"
                    + "You are asking about modern Java language and concurrency features—specifically release milestones, immutability guarantees, and Java 21 concurrency primitives.\n\n"
                    + "### 2. Explain the Concept\n\n"
                    + "- **Local Variable Type Inference (`var`)**: Introduced in **Java 10** (JEP 286) to reduce boilerplate on local variables with initializers while keeping Java strictly statically typed at compile time.\n"
                    + "- **Java Records (`record`)**: Standardized in Java 16, records are **shallowly immutable** data carriers. Their component fields are `final` (no setters, no reassignment), but if a component holds a mutable object (such as an `ArrayList` or `HashMap`), the contents of that collection can still mutate unless you copy it defensively in a compact constructor.\n"
                    + "- **Virtual Threads**: **Finalized in Java 21** (JEP 444), virtual threads are lightweight JVM-managed threads suited for high-throughput, I/O-bound concurrency without requiring `--enable-preview`.\n"
                    + "- **Structured Concurrency**: Introduced via `StructuredTaskScope` (JEP 453), structured concurrency is a **preview feature in Java 21** (requiring `--enable-preview`), designed to manage related concurrent subtasks as a single atomic unit of work.\n\n"
                    + "### 3. Practical Code Example\n\n"
                    + "```java\n"
                    + "import java.util.List;\n"
                    + "import java.util.concurrent.Executors;\n\n"
                    + "public record TaskBatch(String batchId, List<String> items) {\n"
                    + "    // Compact constructor enforces defensive copy for shallowly immutable record components\n"
                    + "    public TaskBatch {\n"
                    + "        items = List.copyOf(items);\n"
                    + "    }\n\n"
                    + "    public static void runWithVirtualThreads(TaskBatch batch) {\n"
                    + "        // Local variable type inference (introduced in Java 10)\n"
                    + "        var itemCount = batch.items().size();\n\n"
                    + "        // Virtual threads are finalized in Java 21 (no preview flag needed)\n"
                    + "        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {\n"
                    + "            for (var item : batch.items()) {\n"
                    + "                executor.submit(() -> System.out.println(\"Processing \" + item + \" of \" + itemCount));\n"
                    + "            }\n"
                    + "        }\n"
                    + "    }\n"
                    + "}\n"
                    + "```\n\n"
                    + "### 4. Step-by-Step Code Walkthrough\n\n"
                    + "1. `public record TaskBatch(...)` creates `private final` fields for `batchId` and `items`. Because records are shallowly immutable, the compact constructor calls `List.copyOf(items)` so external callers cannot mutate the list afterward.\n"
                    + "2. `var itemCount = ...` uses Java 10 local variable type inference; the compiler infers `int` statically from the initializer.\n"
                    + "3. `Executors.newVirtualThreadPerTaskExecutor()` uses Java 21 finalized virtual threads, automatically waiting for submitted tasks in the `try`-with-resources block.\n"
                    + "4. If you adopt `StructuredTaskScope` in Java 21 to coordinate subtasks with fail-fast cancellation, compile and run with `--enable-preview` since structured concurrency is a preview feature in Java 21.\n\n"
                    + "### 5. Cheat Sheet\n\n"
                    + "- **`var` (Java 10)**: Local variables with initializers only; cannot be used for fields, method parameters, or `null` initializers.\n"
                    + "- **`record` (Shallowly Immutable)**: Fields are `final`, but referenced mutable collections need `List.copyOf(...)` / `Map.copyOf(...)`.\n"
                    + "- **Virtual Threads**: **Finalized in Java 21** — avoid `synchronized` around blocking I/O on Java 21 to prevent carrier-thread pinning.\n"
                    + "- **Structured Concurrency**: **Preview feature in Java 21** (`--enable-preview` required).";
        }

        // Heuristic detection based on the user's question
        if (lower.contains("spring") || lower.contains("bean") || lower.contains("autowired") || lower.contains("dependency injection") || lower.contains("ioc")) {
            return "### 1. Understand & Clarify\n\n"
                    + "You want to understand **Spring Boot Dependency Injection (DI)**—how the Spring IoC Container wires components together and why constructor injection is preferred.\n\n"
                    + "### 2. Explain the Concept\n\n"
                    + "Dependency Injection is a design pattern where objects receive their collaborators from an external assembler (the **Spring IoC Container**) rather than instantiating them with `new`. Constructor injection is preferred over field injection because dependencies can be `private final`, required invariants are enforced at instantiation time, and classes remain easy to unit test without starting Spring.\n\n"
                    + "### 3. Practical Code Example\n\n"
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
                    + "### 4. Step-by-Step Code Walkthrough\n\n"
                    + "1. `@Service` registers `PaymentService` as a managed singleton bean in the Spring `ApplicationContext`.\n"
                    + "2. During startup, Spring inspects the single constructor, locates the `PaymentGateway` bean, and injects it.\n"
                    + "3. `Objects.requireNonNull` validates the contract immediately, and `private final` ensures thread-safe publication.\n\n"
                    + "### 5. Cheat Sheet\n\n"
                    + "- **Prefer Constructor Injection**: Enables `final` fields, explicit dependencies, and plain JUnit/Mockito testing.\n"
                    + "- **Avoid Field Injection (`@Autowired` on fields)**: Hides coupling and complicates unit tests.\n"
                    + "- **Watch for Circular Dependencies**: Break A <-> B cycles by extracting a shared service or publishing domain events.";
        }

        if (lower.contains("react") || lower.contains("useeffect") || lower.contains("hook") || lower.contains("state") || lower.contains("props")) {
            return "### 1. Understand & Clarify\n\n"
                    + "Managing side effects and asynchronous state synchronization safely is essential for predictable React and TypeScript components.\n\n"
                    + "### 2. Explain the Concept\n\n"
                    + "React render logic should stay pure relative to props and state. Side effects such as network requests, subscriptions, and timers belong in `useEffect` with an explicit dependency array and a cleanup callback to cancel stale updates.\n\n"
                    + "### 3. Practical Code Example\n\n"
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
                    + "### 4. Step-by-Step Code Walkthrough\n\n"
                    + "1. `isCancelled` tracks whether the component unmounted or `candidateId` changed before the response arrived.\n"
                    + "2. Only non-cancelled responses call `setData` and `setLoading`, preventing out-of-order race conditions.\n"
                    + "3. React runs the returned cleanup function before the next effect execution and on unmount.\n\n"
                    + "### 5. Cheat Sheet\n\n"
                    + "- **Dependency Array**: Always list reactive values read inside `useEffect` to avoid stale closures or infinite loops.\n"
                    + "- **Race Protection**: Use `AbortController` or a cancellation flag on async fetches.\n"
                    + "- **Next Step**: Consider TanStack Query when you need caching, retries, and deduplication.";
        }

        if (lower.contains("sql") || lower.contains("database") || lower.contains("index") || lower.contains("join") || lower.contains("acid")) {
            return "### 1. Understand & Clarify\n\n"
                    + "Database performance in production hinges on how queries use indexes to avoid costly full-table scans and disk sorts.\n\n"
                    + "### 2. Explain the Concept\n\n"
                    + "A relational index (typically a balanced B-Tree) maintains sorted keys pointing to table rows so the query planner can locate matching records in $O(\\log N)$ time instead of scanning every page ($O(N)$).\n\n"
                    + "### 3. Practical Code Example\n\n"
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
                    + "### 4. Step-by-Step Code Walkthrough\n\n"
                    + "1. The composite index orders entries first by `topic`, then by `created_at DESC`.\n"
                    + "2. The engine jumps directly to the `'Dynamic Programming'` range and reads the top 10 rows already in sorted order.\n"
                    + "3. `EXPLAIN ANALYZE` verifies whether the planner chose an Index Scan and tracks actual row counts and timing.\n\n"
                    + "### 5. Cheat Sheet\n\n"
                    + "- **Leftmost Prefix Rule**: An index on `(A, B)` accelerates `WHERE A = ?` and `WHERE A = ? AND B = ?`, not `WHERE B = ?` alone.\n"
                    + "- **Read vs. Write Trade-off**: Every `INSERT`/`UPDATE`/`DELETE` updates affected indexes, so avoid redundant indexes.\n"
                    + "- **Verify with `EXPLAIN`**: Always inspect actual query plans rather than guessing.";
        }

        if (lower.contains("algorithm") || lower.contains("dsa") || lower.contains("leetcode") || lower.contains("complexity") || lower.contains("array") || lower.contains("tree") || lower.contains("graph")) {
            return "### 1. Understand & Clarify\n\n"
                    + "You want a structured approach to recognizing algorithmic patterns—specifically using the **Two-Pointer** technique on sorted data.\n\n"
                    + "### 2. Explain the Concept\n\n"
                    + "Before coding, map constraints to known patterns:\n"
                    + "- **Sorted arrays / subarrays**: Two Pointers, Sliding Window, or Binary Search.\n"
                    + "- **Hierarchies / Decisions**: Tree DFS/BFS, Recursion, Backtracking.\n"
                    + "- **Optimal Substructure / Overlapping subproblems**: Dynamic Programming.\n"
                    + "- **Shortest path / Networks**: BFS (unweighted) or Dijkstra's algorithm (weighted).\n\n"
                    + "### 3. Practical Code Example\n\n"
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
                    + "### 4. Step-by-Step Code Walkthrough\n\n"
                    + "1. Start two pointers at opposite ends (`left = 0`, `right = length - 1`).\n"
                    + "2. Because `numbers` is sorted, if `sum < target`, moving `left++` is the only move that increases the sum.\n"
                    + "3. If `sum > target`, moving `right--` decreases the sum.\n"
                    + "4. Each step eliminates one boundary element in $O(1)$ time, yielding $O(n)$ total time and $O(1)$ auxiliary space.\n\n"
                    + "### 5. Cheat Sheet\n\n"
                    + "- **Complexity**: $O(n)$ time, $O(1)$ space.\n"
                    + "- **Common Pitfalls**: Integer overflow on `numbers[left] + numbers[right]` (cast to `long` for extreme inputs) and `left < right` vs `left <= right` bounds.\n"
                    + "- **Next Practice**: Extend this pattern to 3Sum ($O(n^2)$) and Container With Most Water.";
        }

        // General Comprehensive Mentor Response
        return "### 1. Understand & Clarify\n\n"
                + "You asked: *\"" + message + "\"*\n\n"
                + "Let's examine this from an engineering and architectural perspective in **" + language + "** (**" + learningContext + "**).\n\n"
                + "### 2. Explain the Concept\n\n"
                + "In production software systems, dependable design balances three core pillars:\n"
                + "1. **Maintainability & Modularity**: Keeping components decoupled, testable, and explicit about contracts.\n"
                + "2. **Algorithmic & Operational Efficiency**: Maintaining predictable $O(n)$ or $O(\\log n)$ paths and bounded resource usage.\n"
                + "3. **Failure Isolation & Resilience**: Validating inputs at boundaries and handling unexpected states gracefully.\n\n"
                + "### 3. Practical Code Example\n\n"
                + "```" + (language.toLowerCase().contains("java") ? "java" : (language.toLowerCase().contains("type") || language.toLowerCase().contains("react") ? "typescript" : "text")) + "\n"
                + "// Production-grade idiomatic design pattern\n"
                + "public class SolutionEngine {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(SolutionEngine.class);\n\n"
                + "    public ExecutionResult executeTask(TaskContext context) {\n"
                + "        if (context == null || context.getPayload() == null) {\n"
                + "            throw new IllegalArgumentException(\"Context payload must not be null\");\n"
                + "        }\n\n"
                + "        try {\n"
                + "            return processInternal(context);\n"
                + "        } catch (Exception ex) {\n"
                + "            logger.error(\"Operation failed safely: {}\", ex.getMessage());\n"
                + "            return ExecutionResult.failure(\"Task execution failed gracefully\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "```\n\n"
                + "### 4. Step-by-Step Code Walkthrough\n\n"
                + "1. **Validation at the Boundary**: `context` and its payload are verified up front to fail fast on invalid input.\n"
                + "2. **Encapsulation**: Core business logic stays inside `processInternal(context)`, behind a clean public method.\n"
                + "3. **Error Boundaries**: Exceptions are caught, logged without leaking secrets, and mapped to a structured result.\n\n"
                + "### 5. Cheat Sheet\n\n"
                + "- **Boundary Guards**: Check nulls, empty collections, and bounds early.\n"
                + "- **Avoid Premature Optimization**: Measure before optimizing, and keep failure paths explicit.\n"
                + "- **Next Step**: Share a specific code snippet or design requirement to walk through targeted edge cases and tests.";
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
                String summary;
                if (javaCodeValidationService.isOnlyComments(block.getCode())) {
                    summary = "Code block contains only comments (syntax validation not applicable)";
                } else if (("java".equalsIgnoreCase(lang) || "text".equalsIgnoreCase(lang)) && javaCodeValidationService.isOrdinaryProse(block.getCode())) {
                    summary = "Plain explanatory text (syntax validation not applicable)";
                } else {
                    summary = "Syntax validation not applicable for " + lang + " snippet";
                }
                details.add(new JavaCodeBlockValidation(
                        block.getIndex(),
                        lang,
                        "NOT_CHECKED",
                        false,
                        null,
                        summary
                ));
                continue;
            }

            JavaCodeValidationService.ValidationResult result = javaCodeValidationService.validateJavaSnippet(block.getCode());

            if ("NOT_CHECKED".equals(result.getStatus())) {
                details.add(new JavaCodeBlockValidation(
                        block.getIndex(),
                        "java",
                        "NOT_CHECKED",
                        false,
                        null,
                        result.getMessage()
                ));
                continue;
            }

            hasJava = true;

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
                                    "Java syntax automatically repaired and parsed with Java 21 grammar (syntax check only; not compiled or executed)"
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
                            "Syntax parse warning (not compiled or executed): " + result.getMessage()
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
            response.setValidationSummary("No Java code snippets present for syntax parsing.");
        } else if (hasInvalid) {
            response.setValidationStatus("INVALID");
            response.setValidationSummary("One or more Java code snippets contain syntax parse issues (syntax check only; not compiled or executed).");
        } else {
            response.setValidationStatus("VALID");
            if (anyCorrected) {
                response.setValidationSummary("Java syntax was automatically repaired and parsed against Java 21 grammar (syntax check only; not compiled or executed).");
            } else {
                response.setValidationSummary("All Java code snippets parsed cleanly against Java 21 syntax grammar (syntax check only; not compiled or executed).");
            }
        }
    }
}
