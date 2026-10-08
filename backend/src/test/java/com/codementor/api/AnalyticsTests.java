package com.codementor.api;

import com.codementor.api.dto.*;
import com.codementor.api.entity.*;
import com.codementor.api.repository.InterviewRepository;
import com.codementor.api.repository.UserRepository;
import com.codementor.api.service.GeminiService;
import com.codementor.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AnalyticsTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeminiService geminiService;

    private User userAlice;
    private User userBob;
    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void setUp() {
        interviewRepository.deleteAll();
        userRepository.deleteAll();

        userAlice = userRepository.save(new User(
                "Alice",
                "alice@example.com",
                passwordEncoder.encode("password123"),
                "USER"
        ));

        userBob = userRepository.save(new User(
                "Bob",
                "bob@example.com",
                passwordEncoder.encode("password123"),
                "USER"
        ));

        aliceToken = "Bearer " + jwtService.generateToken(userAlice);
        bobToken = "Bearer " + jwtService.generateToken(userBob);

        AiInsightsDto mockInsights = new AiInsightsDto(
                "Strong analytical foundation in Arrays with room for growth in Dynamic Programming.",
                List.of("Arrays", "Searching"),
                List.of("Dynamic Programming"),
                List.of("Dynamic Programming", "Trees"),
                List.of(new AiActionPlanDto("Dynamic Programming", "Low average score in recursion", "Solve 5 memoization questions")),
                "MEDIUM",
                "Keep practicing DP drills over the next week."
        );
        when(geminiService.generatePersonalizedInsights(any())).thenReturn(mockInsights);
    }

    private Interview createInterview(User user, String topic, Difficulty diff, Language lang,
                                      Integer score, InterviewStatus status, LocalDateTime completedAt) {
        Interview interview = new Interview(
                user,
                topic,
                diff,
                lang,
                "Problem for " + topic,
                "Description for " + topic,
                "// starter"
        );
        interview.setScore(score);
        interview.setStatus(status);
        interview.setStartedAt(completedAt != null ? completedAt.minusMinutes(30) : LocalDateTime.now());
        interview.setCompletedAt(completedAt);
        return interviewRepository.save(interview);
    }

    @Test
    @DisplayName("1. Analytics endpoint requires authentication")
    void testAnalyticsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/analytics/topics"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/analytics/difficulty"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/analytics/languages"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/analytics/trend"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/analytics/ai-insights"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("2. Overview returns only authenticated user's completed interviews")
    void testOverviewReturnsOnlyAuthenticatedUsersCompleted() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.MEDIUM, Language.JAVA, 90, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));
        createInterview(userAlice, "Strings", Difficulty.EASY, Language.JAVA, 80, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(2));
        createInterview(userBob, "Trees", Difficulty.HARD, Language.PYTHON, 99, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInterviews", is(2)))
                .andExpect(jsonPath("$.completedInterviews", is(2)))
                .andExpect(jsonPath("$.averageScore", is(85.0)));
    }

    @Test
    @DisplayName("3. IN_PROGRESS interviews are excluded from statistics")
    void testInProgressInterviewsExcluded() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.MEDIUM, Language.JAVA, 85, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));
        // In-progress interview should NOT be counted in overview
        createInterview(userAlice, "Arrays", Difficulty.HARD, Language.JAVA, null, InterviewStatus.IN_PROGRESS, null);

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInterviews", is(1)))
                .andExpect(jsonPath("$.completedInterviews", is(1)))
                .andExpect(jsonPath("$.averageScore", is(85.0)));
    }

    @Test
    @DisplayName("4. Average score is calculated correctly")
    void testAverageScoreCalculatedCorrectly() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.EASY, Language.JAVA, 80, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(3));
        createInterview(userAlice, "Strings", Difficulty.MEDIUM, Language.JAVA, 90, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(2));
        createInterview(userAlice, "Trees", Difficulty.HARD, Language.JAVA, 70, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore", is(80.0)));
    }

    @Test
    @DisplayName("5. Highest and lowest scores are calculated correctly")
    void testHighestAndLowestScoresCalculated() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.EASY, Language.JAVA, 95, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(3));
        createInterview(userAlice, "Strings", Difficulty.MEDIUM, Language.JAVA, 62, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(2));
        createInterview(userAlice, "Trees", Difficulty.HARD, Language.JAVA, 84, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highestScore", is(95)))
                .andExpect(jsonPath("$.lowestScore", is(62)));
    }

    @Test
    @DisplayName("6. Topic analytics are calculated correctly and sorted by averageScore desc")
    void testTopicAnalyticsCalculatedCorrectly() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.EASY, Language.JAVA, 90, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(4));
        createInterview(userAlice, "Arrays", Difficulty.MEDIUM, Language.JAVA, 80, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(3));
        createInterview(userAlice, "Dynamic Programming", Difficulty.HARD, Language.JAVA, 60, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(2));

        mockMvc.perform(get("/api/analytics/topics")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].topic", is("Arrays")))
                .andExpect(jsonPath("$[0].attempts", is(2)))
                .andExpect(jsonPath("$[0].averageScore", is(85.0)))
                .andExpect(jsonPath("$[0].bestScore", is(90)))
                .andExpect(jsonPath("$[1].topic", is("Dynamic Programming")))
                .andExpect(jsonPath("$[1].attempts", is(1)))
                .andExpect(jsonPath("$[1].averageScore", is(60.0)));
    }

    @Test
    @DisplayName("7. Difficulty analytics are calculated correctly")
    void testDifficultyAnalyticsCalculatedCorrectly() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.EASY, Language.JAVA, 92, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(2));
        createInterview(userAlice, "Strings", Difficulty.MEDIUM, Language.JAVA, 78, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get("/api/analytics/difficulty")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].difficulty", is("EASY")))
                .andExpect(jsonPath("$[0].attempts", is(1)))
                .andExpect(jsonPath("$[0].averageScore", is(92.0)))
                .andExpect(jsonPath("$[1].difficulty", is("MEDIUM")))
                .andExpect(jsonPath("$[1].attempts", is(1)))
                .andExpect(jsonPath("$[1].averageScore", is(78.0)))
                .andExpect(jsonPath("$[2].difficulty", is("HARD")))
                .andExpect(jsonPath("$[2].attempts", is(0)));
    }

    @Test
    @DisplayName("8. Language analytics are calculated correctly")
    void testLanguageAnalyticsCalculatedCorrectly() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.EASY, Language.JAVA, 85, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(3));
        createInterview(userAlice, "Strings", Difficulty.EASY, Language.JAVA, 89, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(2));
        createInterview(userAlice, "Trees", Difficulty.MEDIUM, Language.PYTHON, 72, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get("/api/analytics/languages")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].language", is("JAVA")))
                .andExpect(jsonPath("$[0].attempts", is(2)))
                .andExpect(jsonPath("$[0].averageScore", is(87.0)))
                .andExpect(jsonPath("$[1].language", is("PYTHON")))
                .andExpect(jsonPath("$[1].attempts", is(1)))
                .andExpect(jsonPath("$[1].averageScore", is(72.0)));
    }

    @Test
    @DisplayName("9. Trend is ordered chronologically")
    void testTrendOrderedChronologically() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.EASY, Language.JAVA, 65, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(5));
        createInterview(userAlice, "Strings", Difficulty.MEDIUM, Language.JAVA, 75, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(3));
        createInterview(userAlice, "Trees", Difficulty.HARD, Language.JAVA, 85, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get("/api/analytics/trend")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].score", is(65)))
                .andExpect(jsonPath("$[1].score", is(75)))
                .andExpect(jsonPath("$[2].score", is(85)));
    }

    @Test
    @DisplayName("10. Another user's interview data cannot appear in analytics")
    void testAnotherUsersDataCannotAppearInAnalytics() throws Exception {
        createInterview(userBob, "Dynamic Programming", Difficulty.HARD, Language.PYTHON, 100, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        // Alice has no interviews
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInterviews", is(0)))
                .andExpect(jsonPath("$.completedInterviews", is(0)))
                .andExpect(jsonPath("$.highestScore", is(0)));

        mockMvc.perform(get("/api/analytics/topics")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("11. AI insights use mocked GeminiService")
    void testAiInsightsUsesMockedGeminiService() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.MEDIUM, Language.JAVA, 88, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get("/api/analytics/ai-insights")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallAssessment", notNullValue()))
                .andExpect(jsonPath("$.strongTopics", hasItem("Arrays")))
                .andExpect(jsonPath("$.actionPlan", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("12. Empty interview history returns valid empty analytics")
    void testEmptyHistoryReturnsValidEmptyAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInterviews", is(0)))
                .andExpect(jsonPath("$.completedInterviews", is(0)))
                .andExpect(jsonPath("$.averageScore", is(0.0)))
                .andExpect(jsonPath("$.highestScore", is(0)))
                .andExpect(jsonPath("$.lowestScore", is(0)))
                .andExpect(jsonPath("$.strongestTopic", is("N/A")))
                .andExpect(jsonPath("$.weakestTopic", is("N/A")));

        mockMvc.perform(get("/api/analytics/topics")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/analytics/trend")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/analytics/ai-insights")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallAssessment", notNullValue()))
                .andExpect(jsonPath("$.nextDifficulty", is("EASY")));
    }

    @Test
    @DisplayName("13. Gemini failure is handled safely")
    void testGeminiFailureHandledSafely() throws Exception {
        createInterview(userAlice, "Arrays", Difficulty.MEDIUM, Language.JAVA, 75, InterviewStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        when(geminiService.generatePersonalizedInsights(any()))
                .thenThrow(new RuntimeException("Gemini quota exceeded or timeout"));

        mockMvc.perform(get("/api/analytics/ai-insights")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallAssessment", notNullValue()))
                .andExpect(jsonPath("$.actionPlan", hasSize(greaterThanOrEqualTo(1))));
    }
}
