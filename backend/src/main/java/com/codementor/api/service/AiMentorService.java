package com.codementor.api.service;

import com.codementor.api.config.OpenRouterConfig;
import com.codementor.api.dto.AiMentorChatMessage;
import com.codementor.api.dto.AiMentorRequest;
import com.codementor.api.dto.AiMentorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public AiMentorService(OpenRouterConfig openRouterConfig) {
        this.openRouterConfig = openRouterConfig;
    }

    public AiMentorResponse chatWithMentor(AiMentorRequest request) {
        String model = resolveModel(request.getCustomModel());
        String lang = request.getProgrammingLanguage() != null && !request.getProgrammingLanguage().isBlank()
                ? request.getProgrammingLanguage().trim()
                : "General / Polyglot";
        String context = request.getLearningContext() != null && !request.getLearningContext().isBlank()
                ? request.getLearningContext().trim()
                : "Full-Stack Software Engineering";

        String lastErrorNotice = null;
        if (openRouterConfig.isApiKeyConfigured()) {
            try {
                String aiText = callOpenRouterApi(request, model, lang, context);
                if (aiText != null && !aiText.isBlank() && !"null".equalsIgnoreCase(aiText.trim())) {
                    return new AiMentorResponse(aiText.trim(), model, "SUCCESS", lang, context, null);
                }
            } catch (OpenRouterApiException e) {
                lastErrorNotice = "Upstream OpenRouter error (HTTP " + e.getStatusCode() + ": " + e.getResponseSummary() + "). Heuristic mentor response provided.";
                logger.warn("OpenRouter API error (HTTP {}): {}. Falling back to heuristic mentor response.", e.getStatusCode(), e.getResponseSummary());
            } catch (Exception e) {
                String safeMsg = redactSecrets(e.getMessage());
                lastErrorNotice = "OpenRouter API call failed (" + safeMsg + "). Heuristic mentor response provided.";
                logger.warn("OpenRouter API call encountered an error: {}. Falling back to heuristic mentor response.", safeMsg);
            }
        }

        // Graceful fallback response when OPENROUTER_API_KEY is not configured or upstream fails
        String fallbackAnswer = generateFallbackMentorResponse(request.getMessage(), lang, context);
        String notice = lastErrorNotice != null
                ? lastErrorNotice
                : (openRouterConfig.isApiKeyConfigured()
                    ? "OpenRouter API response unavailable. Heuristic mentor response provided."
                    : "OpenRouter API key not detected in backend environment. To enable live LLM generation, set OPENROUTER_API_KEY in Render environment variables.");

        return new AiMentorResponse(fallbackAnswer, model + " (Fallback Heuristic)", "FALLBACK", lang, context, notice);
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

    private String extractErrorMessage(String rawBody, int statusCode) {
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

    private String callOpenRouterApi(AiMentorRequest request, String model, String lang, String context) throws Exception {
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
        payload.put("temperature", 0.4);
        payload.put("max_tokens", 2500);

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openRouterConfig.getApiUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openRouterConfig.getApiKey())
                .header("HTTP-Referer", "https://codementor.ai")
                .header("X-Title", "CodeMentor AI Programming Mentor")
                .timeout(Duration.ofSeconds(35))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String rawBody = response.body() != null ? response.body() : "";
            String safeBody = redactSecrets(rawBody);
            logger.warn("OpenRouter API returned HTTP {}: {}", response.statusCode(), safeBody);
            String errorSummary = extractErrorMessage(rawBody, response.statusCode());
            throw new OpenRouterApiException(response.statusCode(), errorSummary);
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        String extractedContent = extractContentFromOpenRouterResponse(rootNode);

        if (extractedContent == null || extractedContent.isBlank() || "null".equalsIgnoreCase(extractedContent.trim())) {
            throw new RuntimeException("Empty or null response content received from OpenRouter.");
        }

        return extractedContent.trim();
    }

    public String extractContentFromOpenRouterResponse(JsonNode rootNode) {
        if (rootNode == null || rootNode.isMissingNode() || rootNode.isNull()) {
            return null;
        }

        JsonNode choices = rootNode.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            // Check top-level text if available
            JsonNode topText = rootNode.path("text");
            if (!topText.isMissingNode() && !topText.isNull() && !topText.asText().isBlank()) {
                String val = topText.asText().trim();
                return "null".equalsIgnoreCase(val) ? null : val;
            }
            return null;
        }

        JsonNode firstChoice = choices.get(0);
        JsonNode messageNode = firstChoice.path("message");

        // 1. Try message.content as text string
        JsonNode contentNode = messageNode.path("content");
        if (!contentNode.isMissingNode() && !contentNode.isNull()) {
            if (contentNode.isTextual()) {
                String text = contentNode.asText().trim();
                if (!text.isEmpty() && !"null".equalsIgnoreCase(text)) {
                    return text;
                }
            } else if (contentNode.isArray()) {
                // 2. Try message.content as array of structured parts (multimodal/reasoning)
                StringBuilder sb = new StringBuilder();
                for (JsonNode part : contentNode) {
                    JsonNode textPart = part.path("text");
                    if (!textPart.isMissingNode() && !textPart.isNull() && !textPart.asText().isBlank()) {
                        sb.append(textPart.asText());
                    }
                }
                String combined = sb.toString().trim();
                if (!combined.isEmpty() && !"null".equalsIgnoreCase(combined)) {
                    return combined;
                }
            }
        }

        // 3. Try message.reasoning or message.reasoning_content (Tencent Hy4 / reasoning models on OpenRouter)
        JsonNode reasoningNode = messageNode.path("reasoning");
        if (!reasoningNode.isMissingNode() && !reasoningNode.isNull() && !reasoningNode.asText().isBlank()) {
            String reasoningText = reasoningNode.asText().trim();
            if (!reasoningText.isEmpty() && !"null".equalsIgnoreCase(reasoningText)) {
                return reasoningText;
            }
        }

        JsonNode reasoningContentNode = messageNode.path("reasoning_content");
        if (!reasoningContentNode.isMissingNode() && !reasoningContentNode.isNull() && !reasoningContentNode.asText().isBlank()) {
            String rcText = reasoningContentNode.asText().trim();
            if (!rcText.isEmpty() && !"null".equalsIgnoreCase(rcText)) {
                return rcText;
            }
        }

        // 4. Try choice.text (legacy / completions format fallback)
        JsonNode textNode = firstChoice.path("text");
        if (!textNode.isMissingNode() && !textNode.isNull() && !textNode.asText().isBlank()) {
            String choiceText = textNode.asText().trim();
            if (!choiceText.isEmpty() && !"null".equalsIgnoreCase(choiceText)) {
                return choiceText;
            }
        }

        // 5. Try message.delta.content (streaming chunk fallback)
        JsonNode deltaContent = firstChoice.path("delta").path("content");
        if (!deltaContent.isMissingNode() && !deltaContent.isNull() && !deltaContent.asText().isBlank()) {
            String deltaText = deltaContent.asText().trim();
            if (!deltaText.isEmpty() && !"null".equalsIgnoreCase(deltaText)) {
                return deltaText;
            }
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
}
