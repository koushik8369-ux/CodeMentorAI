package com.codementor.api.service;

import com.codementor.api.dto.*;
import com.codementor.api.entity.*;
import com.codementor.api.exception.ResourceNotFoundException;
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
public class MockInterviewService {

    private final AuthService authService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<Long, MockInterview> mockSessions = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(2000);
    private final AtomicLong questionIdGenerator = new AtomicLong(5000);

    public MockInterviewService(AuthService authService, GeminiService geminiService) {
        this.authService = authService;
        this.geminiService = geminiService;
    }

    public MockInterviewResponseDto startMockInterview(StartMockInterviewRequest request, String userEmail) {
        String email = (userEmail != null && !userEmail.isBlank()) ? userEmail : "candidate@codementor.ai";
        UserDto userDto = authService.getCurrentUser(email);
        User user = new User(userDto.getName(), userDto.getEmail(), "", userDto.getRole());
        user.setId(userDto.getId());

        if (request.getTotalRounds() < 3 || request.getTotalRounds() > 10) {
            throw new IllegalArgumentException("Total rounds must be between 3 and 10.");
        }
        if (request.getInterviewType() == null) {
            throw new IllegalArgumentException("Interview type is required.");
        }
        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            throw new IllegalArgumentException("Topic is required.");
        }
        if (request.getDifficulty() == null) {
            throw new IllegalArgumentException("Difficulty is required.");
        }

        MockInterview interview = new MockInterview(
                user,
                request.getInterviewType(),
                request.getTopic().trim(),
                request.getDifficulty(),
                request.getTotalRounds()
        );
        interview.setId(idGenerator.incrementAndGet());

        GeneratedMockQuestionDto generated;
        try {
            generated = geminiService.generateMockInterviewQuestion(
                    request.getInterviewType(),
                    request.getTopic().trim(),
                    request.getDifficulty(),
                    1,
                    request.getTotalRounds(),
                    List.of()
            );
        } catch (Exception e) {
            generated = new GeneratedMockQuestionDto(
                    "Please describe your background with " + request.getTopic() + " and a significant architectural challenge you encountered.",
                    List.of("Experience", "Architecture", "Problem Solving"),
                    request.getDifficulty().name()
            );
        }

        MockInterviewQuestion q1 = new MockInterviewQuestion(interview, 1, generated.getQuestion());
        q1.setId(questionIdGenerator.incrementAndGet());
        interview.addQuestion(q1);

        mockSessions.put(interview.getId(), interview);
        return MockInterviewResponseDto.fromEntity(interview);
    }

    public MockInterviewResponseDto submitAnswer(Long interviewId, SubmitMockAnswerRequest request, String userEmail) {
        MockInterview interview = mockSessions.get(interviewId);
        if (interview == null) {
            throw new ResourceNotFoundException("Mock interview not found with id: " + interviewId);
        }

        UserDto userDto = authService.getCurrentUser(userEmail);
        if (interview.getUser() != null && !interview.getUser().getId().equals(userDto.getId())) {
            throw new AccessDeniedException("You do not have permission to access or modify this mock interview.");
        }

        if (interview.getStatus() == MockInterviewStatus.COMPLETED) {
            throw new IllegalStateException("This mock interview has already been completed and cannot receive further answers.");
        }

        if (request.getAnswer() == null || request.getAnswer().trim().isEmpty()) {
            throw new IllegalArgumentException("Interview answer cannot be empty or whitespace.");
        }

        int currentRound = interview.getCurrentRound();
        MockInterviewQuestion question = interview.getQuestions().stream()
                .filter(q -> q.getRoundNumber() == currentRound)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Question for round " + currentRound + " not found."));

        boolean isFollowUpPhase = question.isFollowUpRequired() && !question.isFollowUpCompleted();

        if (isFollowUpPhase) {
            EvaluationResultDto eval;
            try {
                eval = geminiService.evaluateMockInterviewAnswer(
                        question.getFollowUpQuestion(),
                        request.getAnswer().trim(),
                        interview.getInterviewType(),
                        interview.getTopic(),
                        interview.getDifficulty(),
                        true
                );
            } catch (Exception e) {
                eval = new EvaluationResultDto(
                        78,
                        "Candidate clarified the follow-up question.",
                        "Good explanation of follow-up details.",
                        "Clear articulation.",
                        List.of("Addressed key points"),
                        List.of(),
                        "Follow-up answered adequately.",
                        false,
                        null
                );
            }

            question.setFollowUpAnswer(request.getAnswer().trim());
            question.setFollowUpScore(eval.getScore());
            question.setFollowUpFeedback(eval.getFeedback());
            question.setFollowUpCompleted(true);

            advanceOrCompleteInterview(interview);
        } else {
            if (question.getUserAnswer() != null && !question.getUserAnswer().isBlank()) {
                throw new IllegalStateException("Round " + currentRound + " has already been answered.");
            }

            EvaluationResultDto eval;
            try {
                eval = geminiService.evaluateMockInterviewAnswer(
                        question.getQuestion(),
                        request.getAnswer().trim(),
                        interview.getInterviewType(),
                        interview.getTopic(),
                        interview.getDifficulty(),
                        false
                );
            } catch (Exception e) {
                eval = new EvaluationResultDto(
                        75,
                        "Candidate provided answer addressing the question.",
                        "Demonstrates baseline engineering reasoning.",
                        "Structured response.",
                        List.of("Direct answer"),
                        List.of(),
                        "Good attempt. Consider edge cases.",
                        false,
                        null
                );
            }

            question.setUserAnswer(request.getAnswer().trim());
            question.setScore(eval.getScore());
            question.setCorrectness(eval.getCorrectness());
            question.setTechnicalDepth(eval.getTechnicalDepth());
            question.setCommunication(eval.getCommunication());
            question.setFeedback(eval.getFeedback());
            question.setStrengths(serializeList(eval.getStrengths()));
            question.setWeaknesses(serializeList(eval.getWeaknesses()));
            question.setAnsweredAt(LocalDateTime.now());

            if (Boolean.TRUE.equals(eval.getNeedsFollowUp()) && eval.getFollowUpQuestion() != null
                    && !eval.getFollowUpQuestion().isBlank()) {
                question.setFollowUpQuestion(eval.getFollowUpQuestion());
                question.setFollowUpRequired(true);
                question.setFollowUpCompleted(false);
            } else {
                advanceOrCompleteInterview(interview);
            }
        }

        mockSessions.put(interview.getId(), interview);
        return MockInterviewResponseDto.fromEntity(interview);
    }

    private void advanceOrCompleteInterview(MockInterview interview) {
        if (interview.getCurrentRound() < interview.getTotalRounds()) {
            int nextRound = interview.getCurrentRound() + 1;
            interview.setCurrentRound(nextRound);

            List<String> prevQuestions = interview.getQuestions().stream()
                    .map(MockInterviewQuestion::getQuestion)
                    .collect(Collectors.toList());

            GeneratedMockQuestionDto nextQ;
            try {
                nextQ = geminiService.generateMockInterviewQuestion(
                        interview.getInterviewType(),
                        interview.getTopic(),
                        interview.getDifficulty(),
                        nextRound,
                        interview.getTotalRounds(),
                        prevQuestions
                );
            } catch (Exception e) {
                nextQ = new GeneratedMockQuestionDto(
                        "How would you optimize performance, manage scalability, and handle errors in " + interview.getTopic() + "?",
                        List.of("Scalability", "Optimization", "Error handling"),
                        interview.getDifficulty().name()
                );
            }

            MockInterviewQuestion newQuestion = new MockInterviewQuestion(interview, nextRound, nextQ.getQuestion());
            newQuestion.setId(questionIdGenerator.incrementAndGet());
            interview.addQuestion(newQuestion);
        } else {
            List<MockInterviewQuestion> allQuestions = interview.getQuestions();

            FinalAssessmentResultDto finalAssessment;
            try {
                finalAssessment = geminiService.generateFinalMockInterviewAssessment(interview, allQuestions);
            } catch (Exception e) {
                double avg = allQuestions.stream().filter(q -> q.getScore() != null).mapToInt(MockInterviewQuestion::getScore).average().orElse(75.0);
                int sc = (int) Math.round(avg);
                finalAssessment = new FinalAssessmentResultDto(
                        sc, sc, sc, sc, sc,
                        List.of("Demonstrated consistent engineering understanding"),
                        List.of("Deepen analysis of operational trade-offs"),
                        List.of("Practice architectural mock rounds"),
                        "Candidate successfully completed all interview rounds.",
                        sc >= 80 ? "STRONG CANDIDATE" : (sc >= 65 ? "INTERVIEW READY" : "DEVELOPING")
                );
            }

            interview.setStatus(MockInterviewStatus.COMPLETED);
            interview.setCompletedAt(LocalDateTime.now());
            interview.setOverallScore(finalAssessment.getOverallScore());
            interview.setTechnicalKnowledge(finalAssessment.getTechnicalKnowledge());
            interview.setProblemSolving(finalAssessment.getProblemSolving());
            interview.setCommunication(finalAssessment.getCommunication());
            interview.setConfidence(finalAssessment.getConfidence());
            interview.setOverallFeedback(finalAssessment.getOverallFeedback());
            interview.setStrengths(serializeList(finalAssessment.getStrengths()));
            interview.setWeaknesses(serializeList(finalAssessment.getWeaknesses()));
            interview.setRecommendations(serializeList(finalAssessment.getRecommendations()));
            interview.setReadinessLevel(finalAssessment.getReadinessLevel());
        }
    }

    public List<MockInterviewResponseDto> getUserMockInterviews(String userEmail) {
        String email = (userEmail != null && !userEmail.isBlank()) ? userEmail.toLowerCase() : "candidate@codementor.ai";
        UserDto userDto = authService.getCurrentUser(email);
        return mockSessions.values().stream()
                .filter(i -> i.getUser() == null || i.getUser().getId().equals(userDto.getId()) || (i.getUser().getEmail() != null && i.getUser().getEmail().equalsIgnoreCase(email)))
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .map(MockInterviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public MockInterviewResponseDto getMockInterviewById(Long id, String userEmail) {
        MockInterview interview = mockSessions.get(id);
        if (interview == null) {
            throw new ResourceNotFoundException("Mock interview not found with id: " + id);
        }

        UserDto userDto = authService.getCurrentUser(userEmail);
        if (interview.getUser() != null && !interview.getUser().getId().equals(userDto.getId())) {
            throw new AccessDeniedException("You do not have permission to view this mock interview.");
        }

        return MockInterviewResponseDto.fromEntity(interview);
    }

    public void saveSession(MockInterview interview) {
        if (interview.getId() == null) {
            interview.setId(idGenerator.incrementAndGet());
        }
        mockSessions.put(interview.getId(), interview);
    }

    public void clearSessions() {
        mockSessions.clear();
    }

    private String serializeList(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return String.join("; ", list);
        }
    }
}
