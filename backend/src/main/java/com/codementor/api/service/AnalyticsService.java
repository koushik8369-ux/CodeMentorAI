package com.codementor.api.service;

import com.codementor.api.dto.*;
import com.codementor.api.entity.*;
import com.codementor.api.exception.ResourceNotFoundException;
import com.codementor.api.repository.InterviewRepository;
import com.codementor.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public AnalyticsService(InterviewRepository interviewRepository,
                            UserRepository userRepository,
                            GeminiService geminiService) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    public AnalyticsOverviewDto getOverview(String userEmail) {
        User user = getUser(userEmail);
        List<Interview> completed = interviewRepository.findByUserAndStatusOrderByCompletedAtDesc(user, InterviewStatus.COMPLETED);

        if (completed.isEmpty()) {
            return new AnalyticsOverviewDto(0, 0.0, 0, 0, 0, "N/A", "N/A", 0.0);
        }

        int totalCompleted = completed.size();
        double avgScore = roundOneDecimal(completed.stream()
                .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                .average().orElse(0.0));

        int highestScore = completed.stream()
                .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                .max().orElse(0);

        int lowestScore = completed.stream()
                .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                .min().orElse(0);

        // Recent average score (up to last 5 completed)
        double recentAvg = roundOneDecimal(completed.stream()
                .limit(5)
                .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                .average().orElse(0.0));

        // Group by topic to find strongest and weakest
        Map<String, Double> topicAverages = completed.stream()
                .collect(Collectors.groupingBy(
                        Interview::getTopic,
                        Collectors.averagingInt(i -> i.getScore() != null ? i.getScore() : 0)
                ));

        String strongestTopic = topicAverages.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String weakestTopic = topicAverages.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return new AnalyticsOverviewDto(
                totalCompleted,
                avgScore,
                highestScore,
                lowestScore,
                totalCompleted,
                strongestTopic,
                weakestTopic,
                recentAvg
        );
    }

    public List<TopicAnalyticsDto> getTopicAnalytics(String userEmail) {
        User user = getUser(userEmail);
        List<Interview> completed = interviewRepository.findByUserAndStatus(user, InterviewStatus.COMPLETED);

        if (completed.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<Interview>> grouped = completed.stream()
                .collect(Collectors.groupingBy(Interview::getTopic));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String topic = entry.getKey();
                    List<Interview> list = entry.getValue();
                    int attempts = list.size();
                    double avg = roundOneDecimal(list.stream()
                            .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                            .average().orElse(0.0));
                    int best = list.stream()
                            .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                            .max().orElse(0);
                    return new TopicAnalyticsDto(topic, attempts, avg, best);
                })
                .sorted(Comparator.comparingDouble(TopicAnalyticsDto::getAverageScore).reversed())
                .collect(Collectors.toList());
    }

    public List<DifficultyAnalyticsDto> getDifficultyAnalytics(String userEmail) {
        User user = getUser(userEmail);
        List<Interview> completed = interviewRepository.findByUserAndStatus(user, InterviewStatus.COMPLETED);

        Difficulty[] diffs = Difficulty.values();
        List<DifficultyAnalyticsDto> result = new ArrayList<>();

        for (Difficulty d : diffs) {
            List<Interview> list = completed.stream()
                    .filter(i -> i.getDifficulty() == d)
                    .collect(Collectors.toList());

            int attempts = list.size();
            double avg = attempts > 0 ? roundOneDecimal(list.stream()
                    .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                    .average().orElse(0.0)) : 0.0;
            int best = attempts > 0 ? list.stream()
                    .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                    .max().orElse(0) : 0;

            result.add(new DifficultyAnalyticsDto(d.name(), attempts, avg, best));
        }

        return result;
    }

    public List<LanguageAnalyticsDto> getLanguageAnalytics(String userEmail) {
        User user = getUser(userEmail);
        List<Interview> completed = interviewRepository.findByUserAndStatus(user, InterviewStatus.COMPLETED);

        if (completed.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<Interview>> grouped = completed.stream()
                .collect(Collectors.groupingBy(i -> i.getLanguage() != null ? i.getLanguage().name() : "OTHER"));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String lang = entry.getKey();
                    List<Interview> list = entry.getValue();
                    int attempts = list.size();
                    double avg = roundOneDecimal(list.stream()
                            .mapToInt(i -> i.getScore() != null ? i.getScore() : 0)
                            .average().orElse(0.0));
                    return new LanguageAnalyticsDto(lang, attempts, avg);
                })
                .sorted(Comparator.comparingInt(LanguageAnalyticsDto::getAttempts).reversed())
                .collect(Collectors.toList());
    }

    public List<PerformanceTrendDto> getPerformanceTrend(String userEmail) {
        User user = getUser(userEmail);
        List<Interview> completed = interviewRepository.findByUserAndStatusOrderByCompletedAtAsc(user, InterviewStatus.COMPLETED);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return completed.stream()
                .map(i -> {
                    String date = (i.getCompletedAt() != null ? i.getCompletedAt() : i.getStartedAt()).format(formatter);
                    int score = i.getScore() != null ? i.getScore() : 0;
                    return new PerformanceTrendDto(date, score);
                })
                .collect(Collectors.toList());
    }

    public AiInsightsDto getAiInsights(String userEmail) {
        User user = getUser(userEmail);
        List<Interview> completed = interviewRepository.findByUserAndStatusOrderByCompletedAtDesc(user, InterviewStatus.COMPLETED);

        if (completed.isEmpty()) {
            return new AiInsightsDto(
                    "You have not completed any coding interviews yet. Complete your first practice interview to unlock personalized AI diagnostic insights and targeted improvement roadmaps.",
                    Collections.emptyList(),
                    Collections.emptyList(),
                    List.of("Arrays", "Strings"),
                    List.of(
                            new AiActionPlanDto(
                                    "Arrays",
                                    "Arrays form the core of 80%+ of tech interview questions.",
                                    "Start with an Easy or Medium Array coding interview to establish your baseline score."
                            )
                    ),
                    "EASY",
                    "Take your first diagnostic interview to generate personalized coaching analytics."
            );
        }

        AnalyticsOverviewDto overview = getOverview(userEmail);
        List<TopicAnalyticsDto> topics = getTopicAnalytics(userEmail);
        List<DifficultyAnalyticsDto> diffs = getDifficultyAnalytics(userEmail);
        List<LanguageAnalyticsDto> langs = getLanguageAnalytics(userEmail);

        // Extract aggregated strengths and weaknesses without PII
        List<String> collectedWeaknesses = completed.stream()
                .map(Interview::getWeaknesses)
                .filter(Objects::nonNull)
                .limit(4)
                .collect(Collectors.toList());

        List<String> collectedRecommendations = completed.stream()
                .map(Interview::getRecommendations)
                .filter(Objects::nonNull)
                .limit(4)
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCompleted", overview.getCompletedInterviews());
        summary.put("averageScore", overview.getAverageScore());
        summary.put("highestScore", overview.getHighestScore());
        summary.put("lowestScore", overview.getLowestScore());
        summary.put("strongestTopic", overview.getStrongestTopic());
        summary.put("weakestTopic", overview.getWeakestTopic());
        summary.put("recentAverageScore", overview.getRecentAverageScore());
        summary.put("topics", topics);
        summary.put("difficulties", diffs);
        summary.put("languages", langs);
        summary.put("sampleWeaknesses", collectedWeaknesses);
        summary.put("sampleRecommendations", collectedRecommendations);

        try {
            return geminiService.generatePersonalizedInsights(summary);
        } catch (Exception e) {
            String strong = overview.getStrongestTopic() != null && !overview.getStrongestTopic().equals("N/A")
                    ? overview.getStrongestTopic() : "Arrays";
            String weak = overview.getWeakestTopic() != null && !overview.getWeakestTopic().equals("N/A")
                    ? overview.getWeakestTopic() : "Dynamic Programming";

            return new AiInsightsDto(
                    "Candidate shows steady problem-solving performance. Practice key algorithmic patterns to further improve consistency.",
                    List.of(strong),
                    List.of(weak),
                    List.of(weak, "Binary Search"),
                    List.of(
                            new AiActionPlanDto(
                                    weak,
                                    "Target identified growth areas for consistent interview performance.",
                                    "Review fundamental recursion and edge-case testing."
                            )
                    ),
                    overview.getAverageScore() >= 80 ? "HARD" : (overview.getAverageScore() >= 60 ? "MEDIUM" : "EASY"),
                    "Steady progress across recent coding challenges."
            );
        }
    }

    private User getUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
    }

    private double roundOneDecimal(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
