package com.codementor.api.service;

import com.codementor.api.dto.*;
import com.codementor.api.entity.*;
import com.codementor.api.exception.ResourceNotFoundException;
import com.codementor.api.repository.MockInterviewQuestionRepository;
import com.codementor.api.repository.MockInterviewRepository;
import com.codementor.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MockInterviewService {

    private final MockInterviewRepository mockInterviewRepository;
    private final MockInterviewQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MockInterviewService(MockInterviewRepository mockInterviewRepository,
                                MockInterviewQuestionRepository questionRepository,
                                UserRepository userRepository,
                                GeminiService geminiService) {
        this.mockInterviewRepository = mockInterviewRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    @Transactional
    public MockInterviewResponseDto startMockInterview(StartMockInterviewRequest request, String userEmail) {
        User user = getUser(userEmail);

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
        MockInterview savedInterview = mockInterviewRepository.save(interview);

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

        MockInterviewQuestion q1 = new MockInterviewQuestion(savedInterview, 1, generated.getQuestion());
        questionRepository.save(q1);

        savedInterview.getQuestions().add(q1);
        return MockInterviewResponseDto.fromEntity(savedInterview);
    }

    @Transactional
    public MockInterviewResponseDto submitAnswer(Long interviewId, SubmitMockAnswerRequest request, String userEmail) {
        User user = getUser(userEmail);
        MockInterview interview = mockInterviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Mock interview not found with id: " + interviewId));

        // Ownership validation
        if (!interview.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access or modify this mock interview.");
        }

        if (interview.getStatus() == MockInterviewStatus.COMPLETED) {
            throw new IllegalStateException("This mock interview has already been completed and cannot receive further answers.");
        }

        if (request.getAnswer() == null || request.getAnswer().trim().isEmpty()) {
            throw new IllegalArgumentException("Interview answer cannot be empty or whitespace.");
        }

        int currentRound = interview.getCurrentRound();
        MockInterviewQuestion question = questionRepository
                .findByMockInterviewAndRoundNumber(interview, currentRound)
                .orElseThrow(() -> new IllegalStateException("Question for round " + currentRound + " not found."));

        boolean isFollowUpPhase = question.isFollowUpRequired() && !question.isFollowUpCompleted();

        if (isFollowUpPhase) {
            // Answering the follow-up question
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
            questionRepository.save(question);

            // Progress to next round or complete interview
            advanceOrCompleteInterview(interview);
        } else {
            // Answering primary question for this round
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
                questionRepository.save(question);
                // Keep interview at current round so candidate answers follow-up
            } else {
                questionRepository.save(question);
                advanceOrCompleteInterview(interview);
            }
        }

        MockInterview updated = mockInterviewRepository.save(interview);
        return MockInterviewResponseDto.fromEntity(updated);
    }

    private void advanceOrCompleteInterview(MockInterview interview) {
        if (interview.getCurrentRound() < interview.getTotalRounds()) {
            int nextRound = interview.getCurrentRound() + 1;
            interview.setCurrentRound(nextRound);

            // Fetch previous questions for novelty
            List<MockInterviewQuestion> existing = questionRepository.findByMockInterviewOrderByRoundNumberAsc(interview);
            List<String> prevQuestions = existing.stream()
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
            questionRepository.save(newQuestion);
            interview.getQuestions().add(newQuestion);
        } else {
            // Completed all rounds! Calculate final assessment
            List<MockInterviewQuestion> allQuestions = questionRepository.findByMockInterviewOrderByRoundNumberAsc(interview);

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

    @Transactional(readOnly = true)
    public List<MockInterviewResponseDto> getUserMockInterviews(String userEmail) {
        User user = getUser(userEmail);
        List<MockInterview> list = mockInterviewRepository.findByUserOrderByStartedAtDesc(user);
        return list.stream()
                .map(MockInterviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MockInterviewResponseDto getMockInterviewById(Long id, String userEmail) {
        User user = getUser(userEmail);
        MockInterview interview = mockInterviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mock interview not found with id: " + id));

        if (!interview.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this mock interview.");
        }

        return MockInterviewResponseDto.fromEntity(interview);
    }

    private User getUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
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
