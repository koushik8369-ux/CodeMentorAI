package com.codementor.api.service;

import com.codementor.api.dto.*;
import com.codementor.api.entity.*;
import com.codementor.api.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final AuthService authService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<Long, Interview> interviewSessions = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000);

    public InterviewService(AuthService authService, GeminiService geminiService) {
        this.authService = authService;
        this.geminiService = geminiService;
    }

    public InterviewResponse startInterview(StartInterviewRequest request, String userEmail) {
        String email = (userEmail != null && !userEmail.isBlank()) ? userEmail : "candidate@codementor.ai";
        UserDto userDto = authService.getCurrentUser(email);
        User user = new User(userDto.getName(), userDto.getEmail(), "", userDto.getRole());
        user.setId(userDto.getId());

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

        interview.setId(idGenerator.incrementAndGet());
        interview.setInputFormat(problem.getInputFormat());
        interview.setOutputFormat(problem.getOutputFormat());

        try {
            interview.setConstraints(objectMapper.writeValueAsString(problem.getConstraints()));
            interview.setExamples(objectMapper.writeValueAsString(problem.getExamples()));
        } catch (Exception ignored) {
        }

        interviewSessions.put(interview.getId(), interview);
        return mapToResponse(interview);
    }

    public InterviewResponse submitInterview(Long id, SubmitInterviewRequest request, String userEmail) {
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Submitted code cannot be empty");
        }

        Interview interview = interviewSessions.get(id);
        if (interview == null) {
            throw new ResourceNotFoundException("Interview not found with id: " + id);
        }

        if (interview.getUser() != null && !interview.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You are not authorized to submit this interview");
        }

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
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

        interviewSessions.put(interview.getId(), interview);
        return mapToResponse(interview);
    }

    public List<InterviewResponse> getUserInterviews(String userEmail) {
        String email = (userEmail != null && !userEmail.isBlank()) ? userEmail.toLowerCase() : "candidate@codementor.ai";
        return interviewSessions.values().stream()
                .filter(i -> i.getUser() == null || i.getUser().getEmail() == null || i.getUser().getEmail().equalsIgnoreCase(email))
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InterviewResponse getInterviewById(Long id, String userEmail) {
        Interview interview = interviewSessions.get(id);
        if (interview == null) {
            throw new ResourceNotFoundException("Interview not found with id: " + id);
        }

        if (interview.getUser() != null && !interview.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You are not authorized to view this interview");
        }

        return mapToResponse(interview);
    }

    public List<InterviewResponse> getAllInterviews() {
        return interviewSessions.values().stream()
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InterviewResponse createInterview(InterviewRequest request) {
        User user = new User("Candidate", "candidate@codementor.ai", "", "USER");
        user.setId(1L);

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
        interview.setId(idGenerator.incrementAndGet());
        interview.setScore(80);
        interview.setStatus(InterviewStatus.IN_PROGRESS);

        interviewSessions.put(interview.getId(), interview);
        return mapToResponse(interview);
    }

    public void saveSession(Interview interview) {
        if (interview.getId() == null) {
            interview.setId(idGenerator.incrementAndGet());
        }
        interviewSessions.put(interview.getId(), interview);
    }

    public void clearSessions() {
        interviewSessions.clear();
    }

    public InterviewResponse mapToResponse(Interview i) {
        InterviewResponse resp = new InterviewResponse();
        resp.setId(i.getId());
        resp.setUserId(i.getUser() != null ? i.getUser().getId() : 1L);
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
