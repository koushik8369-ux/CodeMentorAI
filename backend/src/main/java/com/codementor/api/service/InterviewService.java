package com.codementor.api.service;

import com.codementor.api.dto.*;
import com.codementor.api.entity.*;
import com.codementor.api.exception.ResourceNotFoundException;
import com.codementor.api.repository.InterviewRepository;
import com.codementor.api.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Fallback in-memory list for transient resilience
    private final List<InterviewResponse> fallbackSessions = new CopyOnWriteArrayList<>();

    public InterviewService(InterviewRepository interviewRepository,
                            UserRepository userRepository,
                            GeminiService geminiService) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    public InterviewResponse startInterview(StartInterviewRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Difficulty difficulty = Difficulty.fromString(request.getDifficulty());
        Language language = Language.fromString(request.getLanguage());
        String topic = request.getTopic() != null ? request.getTopic().trim() : "Algorithms";

        GeneratedProblemDto problem = geminiService.generateCodingProblem(topic, difficulty.name(), language.name());

        Interview interview = new Interview(
                user,
                topic,
                difficulty,
                language,
                problem.getTitle(),
                problem.getDescription(),
                problem.getStarterCode()
        );

        interview.setInputFormat(problem.getInputFormat());
        interview.setOutputFormat(problem.getOutputFormat());

        try {
            interview.setConstraints(objectMapper.writeValueAsString(problem.getConstraints()));
            interview.setExamples(objectMapper.writeValueAsString(problem.getExamples()));
        } catch (Exception ignored) {
        }

        Interview saved = interviewRepository.save(interview);
        return mapToResponse(saved);
    }

    public InterviewResponse submitInterview(Long id, SubmitInterviewRequest request, String userEmail) {
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Submitted code cannot be empty");
        }

        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));

        if (!interview.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You are not authorized to submit this interview");
        }

        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new IllegalStateException("Interview has already been completed");
        }

        String submittedCode = request.getCode().trim();
        interview.setUserCode(submittedCode);

        EvaluationResponseDto evaluation = geminiService.evaluateCodingSolution(
                interview.getProblemTitle(),
                interview.getProblemDescription(),
                interview.getLanguage().name(),
                submittedCode
        );

        int score = evaluation.getScore() != null ? Math.max(0, Math.min(100, evaluation.getScore())) : 75;
        interview.setScore(score);
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setCompletedAt(LocalDateTime.now());
        interview.setCorrectness(evaluation.getCorrectness());
        interview.setCodeQuality(evaluation.getCodeQuality());
        interview.setTimeComplexity(evaluation.getTimeComplexity());
        interview.setSpaceComplexity(evaluation.getSpaceComplexity());
        interview.setAiFeedback(evaluation.getOverallFeedback());

        try {
            interview.setStrengths(objectMapper.writeValueAsString(evaluation.getStrengths()));
            interview.setWeaknesses(objectMapper.writeValueAsString(evaluation.getWeaknesses()));
            interview.setRecommendations(objectMapper.writeValueAsString(evaluation.getRecommendations()));
        } catch (Exception ignored) {
        }

        Interview updated = interviewRepository.save(interview);
        return mapToResponse(updated);
    }

    public List<InterviewResponse> getUserInterviews(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Interview> list = interviewRepository.findByUserOrderByStartedAtDesc(user);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public InterviewResponse getInterviewById(Long id, String userEmail) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));

        if (!interview.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You are not authorized to view this interview");
        }

        return mapToResponse(interview);
    }

    // Backward-compatible methods
    public List<InterviewResponse> getAllInterviews() {
        try {
            List<Interview> list = interviewRepository.findAllByOrderByStartedAtDesc();
            if (!list.isEmpty()) {
                return list.stream().map(this::mapToResponse).collect(Collectors.toList());
            }
        } catch (Exception ignored) {
        }
        return fallbackSessions;
    }

    public InterviewResponse createInterview(InterviewRequest request) {
        User user = userRepository.findById(request.getUserId() != null ? request.getUserId() : 1L)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

        if (user == null) {
            user = new User("Candidate", "candidate@example.com", "password", "USER");
            user = userRepository.save(user);
        }

        Difficulty difficulty = Difficulty.fromString(request.getDifficulty());
        Language language = Language.fromString(request.getLanguage());
        String topic = (request.getTopics() != null && !request.getTopics().isEmpty()) ? request.getTopics().get(0) : "Arrays";

        Interview interview = new Interview(
                user,
                topic,
                difficulty,
                language,
                "Coding Challenge (" + topic + ")",
                "Solve the algorithmic problem efficiently.",
                "// Starter code in " + language
        );
        interview.setScore(80);
        interview.setStatus(InterviewStatus.IN_PROGRESS);

        Interview saved = interviewRepository.save(interview);
        return mapToResponse(saved);
    }

    public InterviewResponse mapToResponse(Interview i) {
        InterviewResponse resp = new InterviewResponse();
        resp.setId(i.getId());
        resp.setUserId(i.getUser() != null ? i.getUser().getId() : null);
        resp.setTopic(i.getTopic());
        resp.setDifficulty(i.getDifficulty() != null ? i.getDifficulty().name() : "MEDIUM");
        resp.setLanguage(i.getLanguage() != null ? i.getLanguage().name() : "JAVA");
        resp.setProblemTitle(i.getProblemTitle());
        resp.setProblemDescription(i.getProblemDescription());
        resp.setInputFormat(i.getInputFormat());
        resp.setOutputFormat(i.getOutputFormat());
        resp.setStarterCode(i.getStarterCode());
        resp.setUserCode(i.getUserCode());
        resp.setScore(i.getScore());
        resp.setStatus(i.getStatus() != null ? i.getStatus().name() : "IN_PROGRESS");
        resp.setCorrectness(i.getCorrectness());
        resp.setCodeQuality(i.getCodeQuality());
        resp.setTimeComplexity(i.getTimeComplexity());
        resp.setSpaceComplexity(i.getSpaceComplexity());
        resp.setAiFeedback(i.getAiFeedback());
        resp.setStartedAt(i.getStartedAt());
        resp.setCompletedAt(i.getCompletedAt());
        resp.setCreatedAt(i.getStartedAt());
        resp.setRole(i.getUser() != null ? i.getUser().getRole() : "USER");
        resp.setType("Coding Interview");

        if (i.getConstraints() != null) {
            try {
                resp.setConstraints(objectMapper.readValue(i.getConstraints(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                resp.setConstraints(List.of(i.getConstraints()));
            }
        }

        if (i.getExamples() != null) {
            try {
                resp.setExamples(objectMapper.readValue(i.getExamples(), new TypeReference<List<ProblemExampleDto>>() {}));
            } catch (Exception ignored) {
            }
        }

        if (i.getStrengths() != null) {
            try {
                resp.setStrengths(objectMapper.readValue(i.getStrengths(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                resp.setStrengths(List.of(i.getStrengths()));
            }
        }

        if (i.getWeaknesses() != null) {
            try {
                resp.setWeaknesses(objectMapper.readValue(i.getWeaknesses(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                resp.setWeaknesses(List.of(i.getWeaknesses()));
            }
        }

        if (i.getRecommendations() != null) {
            try {
                resp.setRecommendations(objectMapper.readValue(i.getRecommendations(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                resp.setRecommendations(List.of(i.getRecommendations()));
            }
        }

        return resp;
    }
}
