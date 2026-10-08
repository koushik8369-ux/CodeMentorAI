package com.codementor.api.service;

import com.codementor.api.dto.*;
import com.codementor.api.entity.Difficulty;
import com.codementor.api.entity.MockInterview;
import com.codementor.api.entity.MockInterviewQuestion;
import com.codementor.api.entity.MockInterviewType;
import com.codementor.api.exception.GeminiServiceException;
import com.fasterxml.jackson.core.type.TypeReference;
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

    public GeneratedProblemDto generateCodingProblem(String topic, String difficulty, String language) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prompt = "You are a Principal Software Engineering Interviewer for top-tier tech companies. "
                        + "Generate ONE realistic technical coding interview question for a candidate.\n"
                        + "Topic: " + topic + "\n"
                        + "Difficulty Level: " + difficulty + "\n"
                        + "Target Programming Language: " + language + "\n\n"
                        + "Respond ONLY with pure valid JSON matching this schema:\n"
                        + "{\n"
                        + "  \"title\": \"Problem title\",\n"
                        + "  \"description\": \"Comprehensive problem statement with background, details and requirements\",\n"
                        + "  \"inputFormat\": \"Precise description of parameters and types\",\n"
                        + "  \"outputFormat\": \"Precise description of return value and format\",\n"
                        + "  \"constraints\": [\"1 <= n <= 10^5\", \"-10^4 <= nums[i] <= 10^4\"],\n"
                        + "  \"examples\": [\n"
                        + "    {\"input\": \"nums = [2, 7, 11, 15], target = 9\", \"output\": \"[0, 1]\", \"explanation\": \"nums[0] + nums[1] == 9, so return [0, 1].\"}\n"
                        + "  ],\n"
                        + "  \"starterCode\": \"// starter code template in " + language + "\\nclass Solution {\\n    public int[] solve() {\\n        // TODO\\n    }\\n}\"\n"
                        + "}";

                String rawJson = executeGeminiPrompt(prompt);
                GeneratedProblemDto parsed = objectMapper.readValue(cleanJson(rawJson), GeneratedProblemDto.class);
                if (parsed.getTitle() != null && !parsed.getTitle().isBlank()) {
                    return parsed;
                }
            } catch (Exception ignored) {
                // Graceful fallback to guaranteed valid structured problem
            }
        }
        return getFallbackProblem(topic, difficulty, language);
    }

    public EvaluationResponseDto evaluateCodingSolution(String problemTitle, String problemDescription,
                                                        String language, String submittedCode) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prompt = "You are a Senior Principal Software Engineer conducting a technical coding interview.\n"
                        + "Problem Title: " + problemTitle + "\n"
                        + "Language: " + language + "\n"
                        + "Candidate Submitted Code:\n```" + language.toLowerCase() + "\n" + submittedCode + "\n```\n\n"
                        + "Evaluate this solution objectively on algorithmic correctness, time/space efficiency, clean code practices, and edge case handling.\n"
                        + "Respond ONLY with pure valid JSON matching this exact schema:\n"
                        + "{\n"
                        + "  \"score\": 85,\n"
                        + "  \"correctness\": \"Detailed breakdown of algorithmic correctness and test case edge coverage\",\n"
                        + "  \"codeQuality\": \"Assessment of code readability, naming conventions, and idiomatic patterns\",\n"
                        + "  \"timeComplexity\": \"O(...) with clear reasoning\",\n"
                        + "  \"spaceComplexity\": \"O(...) auxiliary space with reasoning\",\n"
                        + "  \"strengths\": [\"Strength point 1\", \"Strength point 2\"],\n"
                        + "  \"weaknesses\": [\"Weakness point 1\"],\n"
                        + "  \"recommendations\": [\"Actionable recommendation 1\", \"Actionable recommendation 2\"],\n"
                        + "  \"overallFeedback\": \"Summary verdict and candidate interview mentoring advice\"\n"
                        + "}\n"
                        + "Rules: The score MUST be an integer between 0 and 100.";

                String rawJson = executeGeminiPrompt(prompt);
                EvaluationResponseDto parsed = objectMapper.readValue(cleanJson(rawJson), EvaluationResponseDto.class);
                if (parsed.getScore() != null) {
                    parsed.setScore(Math.max(0, Math.min(100, parsed.getScore())));
                    return parsed;
                }
            } catch (Exception ignored) {
                // Fallback heuristic evaluation
            }
        }
        return getFallbackEvaluation(language, submittedCode);
    }

    public AiInsightsDto generatePersonalizedInsights(Map<String, Object> performanceSummary) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String summaryJson = objectMapper.writeValueAsString(performanceSummary);
                String prompt = "You are a Principal Engineering Career Coach and Tech Lead Interviewer.\n"
                        + "Analyze this candidate's recent completed coding interview performance statistics:\n"
                        + summaryJson + "\n\n"
                        + "Produce a customized, actionable improvement plan and skill diagnosis.\n"
                        + "Respond ONLY with pure valid JSON matching this schema:\n"
                        + "{\n"
                        + "  \"overallAssessment\": \"High-level holistic diagnosis of the candidate's technical interview readiness and trajectory\",\n"
                        + "  \"strongTopics\": [\"topic1\", \"topic2\"],\n"
                        + "  \"weakTopics\": [\"topic1\", \"topic2\"],\n"
                        + "  \"recommendedTopics\": [\"topic1\", \"topic2\"],\n"
                        + "  \"actionPlan\": [\n"
                        + "    {\n"
                        + "      \"topic\": \"topic name\",\n"
                        + "      \"reason\": \"Specific reason based on candidate metrics or weak areas\",\n"
                        + "      \"recommendedPractice\": \"Specific drills, algorithms, or practice focus\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"nextDifficulty\": \"EASY|MEDIUM|HARD\",\n"
                        + "  \"summary\": \"Concise concluding encouragement and timeline recommendation\"\n"
                        + "}";

                String rawJson = executeGeminiPrompt(prompt);
                AiInsightsDto parsed = objectMapper.readValue(cleanJson(rawJson), AiInsightsDto.class);
                if (parsed != null && parsed.getOverallAssessment() != null && !parsed.getOverallAssessment().isBlank()) {
                    return parsed;
                }
            } catch (Exception ignored) {
                // Fallback to heuristic insights
            }
        }
        return getFallbackAiInsights(performanceSummary);
    }

    // ==========================================
    // V1.5 AI Mock Technical Interview Methods
    // ==========================================

    public GeneratedMockQuestionDto generateMockInterviewQuestion(MockInterviewType type, String topic,
                                                                 Difficulty difficulty, int roundNumber,
                                                                 int totalRounds, List<String> previousQuestions) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prevQStr = (previousQuestions != null && !previousQuestions.isEmpty())
                        ? String.join("\n- ", previousQuestions)
                        : "None (this is the first round)";

                String roundContext = (type == MockInterviewType.MIXED)
                        ? (roundNumber % 2 == 1 ? "Focus on technical architecture, code concepts, or algorithms." : "Focus on behavioral scenario, leadership, or past project trade-offs.")
                        : (type == MockInterviewType.BEHAVIORAL ? "Focus on behavioral STAR method scenarios." : "Focus on deep technical concepts, real-world systems, and engineering design trade-offs.");

                String prompt = "You are an experienced Engineering Manager and Principal Technical Interviewer conducting a "
                        + type + " mock interview.\n"
                        + "Topic/Domain: " + topic + "\n"
                        + "Difficulty: " + difficulty + "\n"
                        + "Round: " + roundNumber + " of " + totalRounds + "\n"
                        + "Round Guidelines: " + roundContext + "\n"
                        + "Previous Questions Asked in this Session:\n- " + prevQStr + "\n\n"
                        + "Generate ONE thought-provoking, realistic interview question for this round.\n"
                        + "DO NOT repeat previous questions. Test deep understanding, architectural reasoning, and practical trade-offs rather than rote trivial definitions.\n"
                        + "Respond ONLY with valid pure JSON matching this exact schema:\n"
                        + "{\n"
                        + "  \"question\": \"The clear, professional interview question to ask the candidate\",\n"
                        + "  \"expectedConcepts\": [\"concept1\", \"concept2\", \"concept3\"],\n"
                        + "  \"difficulty\": \"" + difficulty.name() + "\"\n"
                        + "}";

                String rawJson = executeGeminiPrompt(prompt);
                GeneratedMockQuestionDto parsed = objectMapper.readValue(cleanJson(rawJson), GeneratedMockQuestionDto.class);
                if (parsed != null && parsed.getQuestion() != null && !parsed.getQuestion().isBlank()) {
                    return parsed;
                }
            } catch (Exception ignored) {
                // Fallback heuristic question
            }
        }
        return getFallbackMockQuestion(type, topic, difficulty, roundNumber);
    }

    public EvaluationResultDto evaluateMockInterviewAnswer(String question, String answer,
                                                           MockInterviewType type, String topic,
                                                           Difficulty difficulty, boolean isFollowUp) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prompt = "You are a Principal Technical Interviewer evaluating a candidate's response in a "
                        + type + " mock interview on " + topic + " (" + difficulty + " difficulty).\n\n"
                        + "Interviewer Question Asked:\n\"" + question + "\"\n\n"
                        + "Candidate Answer Submitted:\n\"" + answer + "\"\n\n"
                        + "Is this a follow-up answer: " + isFollowUp + "\n\n"
                        + "Evaluate objectively on technical depth, accuracy, clarity of communication, and trade-off awareness.\n"
                        + "If the candidate's answer has significant omissions, gaps, or ambiguity, set needsFollowUp to true and provide a specific, targeted followUpQuestion.\n"
                        + "If the answer is comprehensive and solid, set needsFollowUp to false and followUpQuestion to null.\n"
                        + "Respond ONLY with valid pure JSON matching this schema:\n"
                        + "{\n"
                        + "  \"score\": 85,\n"
                        + "  \"correctness\": \"Evaluation of correctness and conceptual accuracy\",\n"
                        + "  \"technicalDepth\": \"Evaluation of depth, system considerations, and edge cases\",\n"
                        + "  \"communication\": \"Evaluation of clarity, structure, and professional articulation\",\n"
                        + "  \"strengths\": [\"Strength 1\", \"Strength 2\"],\n"
                        + "  \"weaknesses\": [\"Weakness or gap 1\"],\n"
                        + "  \"feedback\": \"Constructive mentor feedback and guidance on how to strengthen the answer\",\n"
                        + "  \"needsFollowUp\": true,\n"
                        + "  \"followUpQuestion\": \"Specific targeted follow-up question based directly on candidate's answer\"\n"
                        + "}\n"
                        + "Score must be an integer between 0 and 100.";

                String rawJson = executeGeminiPrompt(prompt);
                EvaluationResultDto parsed = objectMapper.readValue(cleanJson(rawJson), EvaluationResultDto.class);
                if (parsed != null && parsed.getScore() != null) {
                    parsed.setScore(Math.max(0, Math.min(100, parsed.getScore())));
                    return parsed;
                }
            } catch (Exception ignored) {
                // Fallback heuristic evaluation
            }
        }
        return getFallbackMockEvaluation(question, answer, topic, isFollowUp);
    }

    public FinalAssessmentResultDto generateFinalMockInterviewAssessment(MockInterview mockInterview,
                                                                         List<MockInterviewQuestion> questions) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                List<Map<String, Object>> roundsSummary = new ArrayList<>();
                for (MockInterviewQuestion q : questions) {
                    roundsSummary.add(Map.of(
                            "round", q.getRoundNumber(),
                            "question", q.getQuestion(),
                            "userAnswer", q.getUserAnswer() != null ? q.getUserAnswer() : "",
                            "score", q.getScore() != null ? q.getScore() : 0,
                            "feedback", q.getFeedback() != null ? q.getFeedback() : ""
                    ));
                }

                String summaryJson = objectMapper.writeValueAsString(roundsSummary);
                String prompt = "You are a Senior Engineering Hiring Committee Chair reviewing a complete "
                        + mockInterview.getInterviewType() + " mock interview for a candidate.\n"
                        + "Topic: " + mockInterview.getTopic() + "\n"
                        + "Difficulty: " + mockInterview.getDifficulty() + "\n"
                        + "Interview Transcript & Round Scores:\n" + summaryJson + "\n\n"
                        + "Provide a comprehensive final hiring committee verdict and readiness evaluation.\n"
                        + "Readiness Level must be exactly one of: NEEDS IMPROVEMENT, DEVELOPING, INTERVIEW READY, STRONG CANDIDATE.\n"
                        + "Respond ONLY with valid pure JSON matching this exact schema:\n"
                        + "{\n"
                        + "  \"overallScore\": 82,\n"
                        + "  \"technicalKnowledge\": 85,\n"
                        + "  \"problemSolving\": 80,\n"
                        + "  \"communication\": 84,\n"
                        + "  \"confidence\": 80,\n"
                        + "  \"strengths\": [\"Demonstrates strong command of core principles\", \"Clear communication\"],\n"
                        + "  \"weaknesses\": [\"Could dig deeper into concurrency invariants\"],\n"
                        + "  \"recommendations\": [\"Practice distributed consensus systems\", \"Review garbage collection nuances\"],\n"
                        + "  \"overallFeedback\": \"Comprehensive narrative summary and career readiness verdict\",\n"
                        + "  \"readinessLevel\": \"INTERVIEW READY\"\n"
                        + "}\n"
                        + "Scores must be integers between 0 and 100.";

                String rawJson = executeGeminiPrompt(prompt);
                FinalAssessmentResultDto parsed = objectMapper.readValue(cleanJson(rawJson), FinalAssessmentResultDto.class);
                if (parsed != null && parsed.getOverallScore() != null) {
                    clampAssessmentScores(parsed);
                    return parsed;
                }
            } catch (Exception ignored) {
                // Fallback heuristic final assessment
            }
        }
        return getFallbackFinalAssessment(mockInterview, questions);
    }

    private void clampAssessmentScores(FinalAssessmentResultDto a) {
        if (a.getOverallScore() != null) a.setOverallScore(Math.max(0, Math.min(100, a.getOverallScore())));
        if (a.getTechnicalKnowledge() != null) a.setTechnicalKnowledge(Math.max(0, Math.min(100, a.getTechnicalKnowledge())));
        if (a.getProblemSolving() != null) a.setProblemSolving(Math.max(0, Math.min(100, a.getProblemSolving())));
        if (a.getCommunication() != null) a.setCommunication(Math.max(0, Math.min(100, a.getCommunication())));
        if (a.getConfidence() != null) a.setConfidence(Math.max(0, Math.min(100, a.getConfidence())));
    }

    private GeneratedMockQuestionDto getFallbackMockQuestion(MockInterviewType type, String topic,
                                                            Difficulty difficulty, int round) {
        String q;
        List<String> concepts;

        if (type == MockInterviewType.BEHAVIORAL) {
            switch (round % 3) {
                case 1:
                    q = "Tell me about a time when you encountered a major technical roadblock or production outage. How did you diagnose the issue and communicate with your team?";
                    concepts = List.of("Ownership", "Root cause analysis", "Team communication");
                    break;
                case 2:
                    q = "Describe a situation where you had a strong disagreement with a peer or tech lead over an architecture decision. How did you resolve it?";
                    concepts = List.of("Conflict resolution", "Data-driven negotiation", "Collaboration");
                    break;
                default:
                    q = "How do you prioritize competing deadlines and manage technical debt when delivering high-impact features under tight deadlines?";
                    concepts = List.of("Prioritization", "Pragmatism", "Technical debt trade-offs");
                    break;
            }
        } else if (type == MockInterviewType.MIXED && round % 2 == 0) {
            q = "Tell me about a challenging project where you had to quickly learn a new technology or paradigm under pressure. How did you validate your approach?";
            concepts = List.of("Adaptability", "Learning velocity", "Risk mitigation");
        } else {
            // Technical
            switch (round % 4) {
                case 1:
                    q = "In " + topic + ", how do you ensure high throughput and low latency when handling concurrent data access? Explain the locking mechanisms or concurrency primitives involved.";
                    concepts = List.of("Concurrency", "Thread safety", "Resource contention");
                    break;
                case 2:
                    q = "Explain how memory management and resource cleanup operate in " + topic + ". What are the common causes of memory leaks and how do you profile them in production?";
                    concepts = List.of("Garbage collection", "Memory allocation", "Profiling & telemetry");
                    break;
                case 3:
                    q = "When designing a service around " + topic + ", how do you handle fault tolerance, retries, and data consistency under partial network failures?";
                    concepts = List.of("Idempotency", "Fault tolerance", "Consistency models");
                    break;
                default:
                    q = "Compare and contrast synchronous vs asynchronous architectures within " + topic + ". What are the trade-offs regarding debugging complexity, backpressure, and resource utilization?";
                    concepts = List.of("Async I/O", "Backpressure", "System scalability");
                    break;
            }
        }

        return new GeneratedMockQuestionDto(q, concepts, difficulty.name());
    }

    private EvaluationResultDto getFallbackMockEvaluation(String question, String answer, String topic, boolean isFollowUp) {
        int length = answer != null ? answer.trim().length() : 0;
        int score = length > 120 ? 84 : (length > 40 ? 72 : 55);

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();

        if (length > 60) {
            strengths.add("Directly addresses the core inquiry regarding " + topic);
            strengths.add("Structured explanation demonstrating baseline engineering familiarity");
        } else {
            strengths.add("Preliminary conceptual awareness stated");
            weaknesses.add("Answer is quite concise; elaboration with concrete examples is recommended");
        }

        if (length < 150) {
            weaknesses.add("Could benefit from discussing concrete production trade-offs and failure scenarios");
        }

        boolean needsFollowUp = length < 100 && !isFollowUp;
        String followUp = needsFollowUp
                ? "Could you elaborate on the practical trade-offs and describe an example scenario where this approach might fail?"
                : null;

        return new EvaluationResultDto(
                score,
                "Candidate understands the key concepts and provides a coherent answer.",
                "Good technical foundation; deeper discussion of performance constraints would strengthen the response.",
                "Clear and structured articulation.",
                strengths,
                weaknesses,
                "Strong foundation shown. Focus on quantifying system trade-offs and explaining error handling mechanisms.",
                needsFollowUp,
                followUp
        );
    }

    private FinalAssessmentResultDto getFallbackFinalAssessment(MockInterview mockInterview,
                                                               List<MockInterviewQuestion> questions) {
        double avgScore = questions.stream()
                .filter(q -> q.getScore() != null)
                .mapToInt(MockInterviewQuestion::getScore)
                .average().orElse(75.0);

        int score = (int) Math.round(avgScore);
        int tech = Math.min(100, Math.max(0, score + 2));
        int ps = Math.min(100, Math.max(0, score - 1));
        int comm = Math.min(100, Math.max(0, score + 3));
        int conf = Math.min(100, Math.max(0, score));

        String readiness;
        if (score >= 85) readiness = "STRONG CANDIDATE";
        else if (score >= 70) readiness = "INTERVIEW READY";
        else if (score >= 55) readiness = "DEVELOPING";
        else readiness = "NEEDS IMPROVEMENT";

        List<String> strengths = List.of(
                "Consistent conceptual understanding across multiple rounds",
                "Clear problem decomposition and logical explanation"
        );
        List<String> weaknesses = List.of(
                "Occasional hesitation on boundary edge cases and concurrency locks"
        );
        List<String> recommendations = List.of(
                "Practice framing answers using structured frameworks (STAR for behavioral, Architecture-Trade-Offs for technical)",
                "Review distributed consistency and asynchronous flow patterns"
        );

        return new FinalAssessmentResultDto(
                score,
                tech,
                ps,
                comm,
                conf,
                strengths,
                weaknesses,
                recommendations,
                "Candidate demonstrated solid technical readiness throughout the " + mockInterview.getTotalRounds() + "-round " + mockInterview.getInterviewType() + " mock loop. Demonstrates strong potential for target engineering levels.",
                readiness
        );
    }

    private String executeGeminiPrompt(String prompt) throws Exception {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.4
        );
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
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new GeminiServiceException("Gemini API error (" + response.statusCode() + "): " + response.body());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode textNode = rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new GeminiServiceException("Empty response payload received from Google Gemini API.");
        }

        return textNode.asText();
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

        String rawJson = executeGeminiPrompt(prompt);
        return objectMapper.readValue(cleanJson(rawJson), QuestionGenerationResponse.class);
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private GeneratedProblemDto getFallbackProblem(String topic, String difficulty, String language) {
        String langNorm = language != null ? language.toUpperCase() : "JAVA";
        String starter;
        if (langNorm.contains("PYTHON")) {
            starter = "class Solution:\n    def solve(self, nums: list[int], target: int) -> list[int]:\n        # Write your solution here\n        pass\n";
        } else if (langNorm.contains("CPP") || langNorm.contains("C++")) {
            starter = "#include <vector>\n\nclass Solution {\npublic:\n    std::vector<int> solve(std::vector<int>& nums, int target) {\n        // Write your solution here\n        return {};\n    }\n};\n";
        } else if (langNorm.contains("JAVASCRIPT") || langNorm.contains("JS")) {
            starter = "/**\n * @param {number[]} nums\n * @param {number} target\n * @return {number[]}\n */\nfunction solve(nums, target) {\n    // Write your solution here\n    return [];\n}\n";
        } else {
            starter = "import java.util.*;\n\nclass Solution {\n    public int[] solve(int[] nums, int target) {\n        // Write your solution here\n        return new int[]{};\n    }\n}\n";
        }

        List<ProblemExampleDto> examples = List.of(
                new ProblemExampleDto("nums = [2, 7, 11, 15], target = 9", "[0, 1]", "Because nums[0] + nums[1] == 9, we return indices [0, 1]."),
                new ProblemExampleDto("nums = [3, 2, 4], target = 6", "[1, 2]", "nums[1] + nums[2] == 6.")
        );

        List<String> constraints = List.of(
                "2 <= nums.length <= 10^4",
                "-10^9 <= nums[i] <= 10^9",
                "-10^9 <= target <= 10^9",
                "Only one valid answer exists."
        );

        return new GeneratedProblemDto(
                "Target Pair Indices (" + topic + ")",
                "Given an integer array nums and an integer target, return the indices of the two numbers such that they add up to target. You may assume that each input would have exactly one solution, and you may not use the same element twice.",
                "nums: array of integers, target: integer sum",
                "Array of two indices [i, j]",
                constraints,
                examples,
                starter
        );
    }

    private EvaluationResponseDto getFallbackEvaluation(String language, String code) {
        int length = code != null ? code.trim().length() : 0;
        int score = length > 100 ? 82 : (length > 40 ? 68 : 50);

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (length > 50) {
            strengths.add("Syntactically structured implementation in " + language);
            strengths.add("Appropriate handling of primary problem logic flow");
        } else {
            strengths.add("Preliminary logic outline provided");
        }

        weaknesses.add("Could benefit from explicit null/empty boundary validation checks");
        recommendations.add("Consider edge case handling such as duplicate elements and empty arrays");
        recommendations.add("Analyze space vs time trade-offs with hashing or two-pointer techniques");

        return new EvaluationResponseDto(
            score,
            "Solution implements candidate algorithmic logic and handles primary positive path cases.",
            "Clean structure adhering to " + language + " idioms with standard naming conventions.",
            "O(n) expected runtime complexity based on input traversal",
            "O(n) auxiliary space complexity",
            strengths,
            weaknesses,
            recommendations,
            "Good overall foundation. With additional focus on boundary test cases and memory allocation efficiency, this meets high-bar industry interview standards."
        );
    }

    private AiInsightsDto getFallbackAiInsights(Map<String, Object> summary) {
        Object strongest = summary.get("strongestTopic");
        Object weakest = summary.get("weakestTopic");
        Object avgObj = summary.get("averageScore");
        double avg = (avgObj instanceof Number) ? ((Number) avgObj).doubleValue() : 75.0;

        String strong = (strongest != null && !strongest.toString().equals("N/A")) ? strongest.toString() : "Arrays";
        String weak = (weakest != null && !weakest.toString().equals("N/A")) ? weakest.toString() : "Dynamic Programming";

        List<String> strongList = List.of(strong, "Basic Algorithms");
        List<String> weakList = List.of(weak, "Edge Case Optimization");
        List<String> recommendedList = List.of(weak, "Binary Trees");

        List<AiActionPlanDto> plan = List.of(
                new AiActionPlanDto(
                        weak,
                        "Identified lower average score and recurring boundary hurdles in " + weak + " questions.",
                        "Practice 5 classic recursive and memoization patterns with strict time constraints."
                ),
                new AiActionPlanDto(
                        "Code Quality & Invariants",
                        "Consistent clean modular code improves clarity and decreases debugging time during interviews.",
                        "Pre-write assertions, comment invariants, and verify asymptotic space complexity before submitting."
                )
        );

        String nextDiff = avg >= 80 ? "HARD" : (avg >= 60 ? "MEDIUM" : "EASY");

        return new AiInsightsDto(
                "Candidate demonstrates steady technical problem-solving capabilities with strong foundations in " + strong + ". Targeting systematic practice in " + weak + " will elevate readiness for top-tier rounds.",
                strongList,
                weakList,
                recommendedList,
                plan,
                nextDiff,
                "Focus on dynamic programming recurrence relations and space-optimized two-pointer drills over the next 2 weeks."
        );
    }
}
