package com.codementor.api;

import com.codementor.api.dto.*;
import com.codementor.api.entity.User;
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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class InterviewTests {

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

    private User candidateUser;
    private User anotherUser;
    private String candidateToken;
    private String anotherToken;

    @BeforeEach
    void setUp() {
        interviewRepository.deleteAll();
        userRepository.deleteAll();

        candidateUser = userRepository.save(new User(
                "Alice Candidate",
                "alice@example.com",
                passwordEncoder.encode("password123"),
                "USER"
        ));

        anotherUser = userRepository.save(new User(
                "Bob Engineer",
                "bob@example.com",
                passwordEncoder.encode("password123"),
                "USER"
        ));

        candidateToken = "Bearer " + jwtService.generateToken(candidateUser);
        anotherToken = "Bearer " + jwtService.generateToken(anotherUser);

        GeneratedProblemDto mockProblem = new GeneratedProblemDto(
                "Two Sum Challenge",
                "Find two numbers that add up to target.",
                "nums array, target integer",
                "indices array [i, j]",
                List.of("2 <= nums.length <= 10^4"),
                List.of(new ProblemExampleDto("nums = [2, 7], target = 9", "[0, 1]", "2 + 7 = 9")),
                "class Solution { public int[] solve() { return new int[]{}; } }"
        );

        when(geminiService.generateCodingProblem(anyString(), anyString(), anyString()))
                .thenReturn(mockProblem);

        EvaluationResponseDto mockEval = new EvaluationResponseDto(
                88,
                "Correct two-pass hash map algorithm.",
                "Clean idiomatic code.",
                "O(n)",
                "O(n)",
                List.of("Optimal time complexity", "Clean variable names"),
                List.of("Could handle null inputs"),
                List.of("Consider single pass hash map"),
                "Strong performance meeting interview requirements."
        );

        when(geminiService.evaluateCodingSolution(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockEval);
    }

    @Test
    @DisplayName("1. Start interview requires authentication")
    void testStartInterviewRequiresAuthentication() throws Exception {
        Map<String, String> payload = Map.of(
                "topic", "Arrays",
                "difficulty", "MEDIUM",
                "language", "JAVA"
        );

        mockMvc.perform(post("/api/interviews/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("2. Start interview creates an interview record")
    void testStartInterviewCreatesInterview() throws Exception {
        Map<String, String> payload = Map.of(
                "topic", "Arrays",
                "difficulty", "MEDIUM",
                "language", "JAVA"
        );

        mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.problemTitle", is("Two Sum Challenge")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.language", is("JAVA")))
                .andExpect(jsonPath("$.difficulty", is("MEDIUM")));
    }

    @Test
    @DisplayName("3. Interview belongs to authenticated user")
    void testInterviewBelongsToAuthenticatedUser() throws Exception {
        Map<String, String> payload = Map.of(
                "topic", "Strings",
                "difficulty", "EASY",
                "language", "PYTHON"
        );

        mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(candidateUser.getId().intValue())));
    }

    @Test
    @DisplayName("4. User cannot access another user's interview")
    void testUserCannotAccessAnotherUsersInterview() throws Exception {
        Map<String, String> payload = Map.of(
                "topic", "Trees",
                "difficulty", "HARD",
                "language", "JAVA"
        );

        String createResponse = mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long interviewId = objectMapper.readTree(createResponse).get("id").asLong();

        // Bob tries to access Alice's interview
        mockMvc.perform(get("/api/interviews/" + interviewId)
                        .header("Authorization", anotherToken))
                .andExpect(status().isForbidden());

        // Bob tries to submit to Alice's interview
        mockMvc.perform(post("/api/interviews/" + interviewId + "/submit")
                        .header("Authorization", anotherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "some code"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. Empty code submission is rejected")
    void testEmptyCodeSubmissionIsRejected() throws Exception {
        Map<String, String> startPayload = Map.of(
                "topic", "Arrays",
                "difficulty", "MEDIUM",
                "language", "JAVA"
        );

        String createResponse = mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long interviewId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(post("/api/interviews/" + interviewId + "/submit")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "   "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. Completed interview cannot be submitted again")
    void testCompletedInterviewCannotBeSubmittedAgain() throws Exception {
        Map<String, String> startPayload = Map.of(
                "topic", "Dynamic Programming",
                "difficulty", "HARD",
                "language", "JAVA"
        );

        String createResponse = mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long interviewId = objectMapper.readTree(createResponse).get("id").asLong();

        // First submission succeeds
        mockMvc.perform(post("/api/interviews/" + interviewId + "/submit")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "public int solve() { return 1; }"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Second submission must be rejected
        mockMvc.perform(post("/api/interviews/" + interviewId + "/submit")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "public int solve() { return 2; }"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7. Score validation works (clamped between 0 and 100)")
    void testScoreValidationWorks() throws Exception {
        Map<String, String> startPayload = Map.of(
                "topic", "Graphs",
                "difficulty", "MEDIUM",
                "language", "JAVA"
        );

        String createResponse = mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long interviewId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(post("/api/interviews/" + interviewId + "/submit")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "class Solution {}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(88)))
                .andExpect(jsonPath("$.score", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.score", lessThanOrEqualTo(100)))
                .andExpect(jsonPath("$.strengths", hasSize(2)))
                .andExpect(jsonPath("$.weaknesses", hasSize(1)));
    }

    @Test
    @DisplayName("8. Interview history returns only authenticated user's interviews")
    void testInterviewHistoryReturnsOnlyAuthenticatedUsersInterviews() throws Exception {
        Map<String, String> payload = Map.of(
                "topic", "Arrays",
                "difficulty", "EASY",
                "language", "JAVA"
        );

        // Alice starts 2 interviews
        mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        // Bob starts 1 interview
        mockMvc.perform(post("/api/interviews/start")
                        .header("Authorization", anotherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        // Alice fetches history
        mockMvc.perform(get("/api/interviews/history")
                        .header("Authorization", candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(candidateUser.getId().intValue())))
                .andExpect(jsonPath("$[1].userId", is(candidateUser.getId().intValue())));

        // Bob fetches history
        mockMvc.perform(get("/api/interviews/history")
                        .header("Authorization", anotherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(anotherUser.getId().intValue())));
    }
}
