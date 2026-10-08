package com.codementor.api;

import com.codementor.api.dto.*;
import com.codementor.api.entity.*;
import com.codementor.api.repository.MockInterviewQuestionRepository;
import com.codementor.api.repository.MockInterviewRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MockInterviewTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockInterviewRepository mockInterviewRepository;

    @Autowired
    private MockInterviewQuestionRepository questionRepository;

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
        questionRepository.deleteAll();
        mockInterviewRepository.deleteAll();
        userRepository.deleteAll();

        userAlice = userRepository.save(new User("Alice", "alice@test.com", passwordEncoder.encode("secret123"), "USER"));
        userBob = userRepository.save(new User("Bob", "bob@test.com", passwordEncoder.encode("secret123"), "USER"));

        aliceToken = "Bearer " + jwtService.generateToken(userAlice);
        bobToken = "Bearer " + jwtService.generateToken(userBob);

        // Default mock behaviors for Gemini
        when(geminiService.generateMockInterviewQuestion(any(), anyString(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new GeneratedMockQuestionDto(
                        "Explain how HashMap manages collisions and rehashes in Java 8+.",
                        List.of("Hashing", "Red-black trees", "Load factor"),
                        "MEDIUM"
                ));

        when(geminiService.evaluateMockInterviewAnswer(anyString(), anyString(), any(), anyString(), any(), anyBoolean()))
                .thenReturn(new EvaluationResultDto(
                        85,
                        "Accurate explanation of tree bins and threshold conversion.",
                        "Good conceptual depth on O(log n) treeification.",
                        "Clear and structured articulation.",
                        List.of("Covers load factor of 0.75", "Explains red-black tree conversion"),
                        List.of("Could mention concurrent access alternatives like ConcurrentHashMap"),
                        "Solid grasp of Java collection internals.",
                        false,
                        null
                ));

        when(geminiService.generateFinalMockInterviewAssessment(any(), any()))
                .thenReturn(new FinalAssessmentResultDto(
                        84,
                        86,
                        82,
                        88,
                        80,
                        List.of("Strong foundational knowledge", "Consistent technical articulation"),
                        List.of("Discuss concurrent data structures in greater depth"),
                        List.of("Practice system-scale architectural trade-offs"),
                        "Candidate performed with high technical clarity throughout the mock loop.",
                        "INTERVIEW READY"
                ));
    }

    @Test
    @DisplayName("1. Start mock interview requires authentication")
    void testStartMockInterviewRequiresAuth() throws Exception {
        StartMockInterviewRequest req = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);

        mockMvc.perform(post("/api/mock-interviews/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("2. Valid mock interview creation")
    void testValidMockInterviewCreation() throws Exception {
        StartMockInterviewRequest req = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);

        mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.currentRound", is(1)))
                .andExpect(jsonPath("$.totalRounds", is(3)))
                .andExpect(jsonPath("$.topic", is("Java")))
                .andExpect(jsonPath("$.currentQuestion", notNullValue()))
                .andExpect(jsonPath("$.currentQuestion.question", containsString("HashMap")));
    }

    @Test
    @DisplayName("3. Invalid round count rejected (less than 3 or greater than 10)")
    void testInvalidRoundCountRejected() throws Exception {
        StartMockInterviewRequest reqTooLow = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 2);

        mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqTooLow)))
                .andExpect(status().isBadRequest());

        StartMockInterviewRequest reqTooHigh = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 15);

        mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqTooHigh)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("4. Question generated and saved")
    void testQuestionGeneratedAndSaved() throws Exception {
        StartMockInterviewRequest req = new StartMockInterviewRequest(MockInterviewType.BEHAVIORAL, "Leadership", Difficulty.EASY, 3);

        String responseJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto res = objectMapper.readValue(responseJson, MockInterviewResponseDto.class);
        assertNotNull(res.getCurrentQuestion());

        List<MockInterviewQuestion> questionsInDb = questionRepository.findAll();
        assertEquals(1, questionsInDb.size());
        assertEquals(1, questionsInDb.get(0).getRoundNumber());
        assertNotNull(questionsInDb.get(0).getQuestion());
    }

    @Test
    @DisplayName("5. Answer submission requires authentication")
    void testAnswerSubmissionRequiresAuth() throws Exception {
        SubmitMockAnswerRequest answerReq = new SubmitMockAnswerRequest("My technical answer");

        mockMvc.perform(post("/api/mock-interviews/1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Ownership validation")
    void testOwnershipValidation() throws Exception {
        StartMockInterviewRequest startReq = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);

        // Bob tries to answer Alice's interview
        SubmitMockAnswerRequest answerReq = new SubmitMockAnswerRequest("Bob trying to answer Alice's interview");
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Empty answer rejected")
    void testEmptyAnswerRejected() throws Exception {
        StartMockInterviewRequest startReq = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);

        SubmitMockAnswerRequest emptyReq = new SubmitMockAnswerRequest("   ");
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("8. Answer evaluation saved")
    void testAnswerEvaluationSaved() throws Exception {
        StartMockInterviewRequest startReq = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);

        SubmitMockAnswerRequest answerReq = new SubmitMockAnswerRequest("In Java 8, HashMap converts linked lists into red-black trees when bin count exceeds 8.");
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerReq)))
                .andExpect(status().isOk());

        MockInterviewQuestion q1 = questionRepository.findAll().stream()
                .filter(q -> q.getRoundNumber() == 1)
                .findFirst().orElseThrow();

        assertEquals(85, q1.getScore());
        assertNotNull(q1.getFeedback());
        assertNotNull(q1.getStrengths());
    }

    @Test
    @DisplayName("9. Follow-up question behavior")
    void testFollowUpQuestionBehavior() throws Exception {
        // Mock Gemini to request a follow-up question
        when(geminiService.evaluateMockInterviewAnswer(anyString(), anyString(), any(), anyString(), any(), eq(false)))
                .thenReturn(new EvaluationResultDto(
                        68,
                        "Basic understanding stated.",
                        "Lacks distinction between threshold parameters.",
                        "Concise phrasing.",
                        List.of("Mentioned tree structure"),
                        List.of("Did not specify TREEIFY_THRESHOLD"),
                        "Elaborate on the threshold parameter values.",
                        true,
                        "What is the exact constant value of TREEIFY_THRESHOLD in HashMap?"
                ));

        StartMockInterviewRequest startReq = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);

        SubmitMockAnswerRequest answer1 = new SubmitMockAnswerRequest("Hash collisions turn into trees.");
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answer1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentQuestion.followUpQuestion", containsString("TREEIFY_THRESHOLD")))
                .andExpect(jsonPath("$.currentQuestion.followUpRequired", is(true)));

        // Answering the follow-up
        when(geminiService.evaluateMockInterviewAnswer(anyString(), anyString(), any(), anyString(), any(), eq(true)))
                .thenReturn(new EvaluationResultDto(
                        90,
                        "Accurate follow-up clarification.",
                        "Good precision.",
                        "Direct communication.",
                        List.of("Identified 8 as the threshold"),
                        List.of(),
                        "Well clarified.",
                        false,
                        null
                ));

        SubmitMockAnswerRequest followUpAnswer = new SubmitMockAnswerRequest("TREEIFY_THRESHOLD is 8, and UNTREEIFY_THRESHOLD is 6.");
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(followUpAnswer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRound", is(2)));
    }

    @Test
    @DisplayName("10. Interview progresses correctly between rounds")
    void testInterviewProgressesBetweenRounds() throws Exception {
        StartMockInterviewRequest startReq = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);
        assertEquals(1, created.getCurrentRound());

        SubmitMockAnswerRequest answer1 = new SubmitMockAnswerRequest("Complete solid answer for round 1.");
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answer1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRound", is(2)))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("11. Completed interview cannot receive another answer")
    void testCompletedInterviewCannotReceiveAnotherAnswer() throws Exception {
        StartMockInterviewRequest startReq = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);

        // Submit round 1
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                .header("Authorization", aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SubmitMockAnswerRequest("Answer 1"))));

        // Submit round 2
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                .header("Authorization", aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SubmitMockAnswerRequest("Answer 2"))));

        // Submit round 3 (final round) -> marks COMPLETED
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                .header("Authorization", aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SubmitMockAnswerRequest("Answer 3"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Attempting another answer after completion should fail with 400 Bad Request
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubmitMockAnswerRequest("Extra Answer"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("12. Final assessment is saved")
    void testFinalAssessmentIsSaved() throws Exception {
        StartMockInterviewRequest startReq = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);

        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                .header("Authorization", aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SubmitMockAnswerRequest("Answer 1"))));

        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                .header("Authorization", aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SubmitMockAnswerRequest("Answer 2"))));

        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubmitMockAnswerRequest("Answer 3"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.overallScore", is(84)))
                .andExpect(jsonPath("$.technicalKnowledge", is(86)))
                .andExpect(jsonPath("$.readinessLevel", is("INTERVIEW READY")))
                .andExpect(jsonPath("$.completedAt", notNullValue()));
    }

    @Test
    @DisplayName("13. User history is user-scoped")
    void testUserHistoryIsUserScoped() throws Exception {
        StartMockInterviewRequest reqAlice = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        mockMvc.perform(post("/api/mock-interviews/start")
                .header("Authorization", aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqAlice)));

        StartMockInterviewRequest reqBob = new StartMockInterviewRequest(MockInterviewType.BEHAVIORAL, "Teamwork", Difficulty.EASY, 3);
        mockMvc.perform(post("/api/mock-interviews/start")
                .header("Authorization", bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqBob)));

        // Alice gets only Alice's interviews
        mockMvc.perform(get("/api/mock-interviews")
                        .header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic", is("Java")));

        // Bob gets only Bob's interviews
        mockMvc.perform(get("/api/mock-interviews")
                        .header("Authorization", bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic", is("Teamwork")));
    }

    @Test
    @DisplayName("14. Another user's mock interview cannot be accessed")
    void testAnotherUsersInterviewCannotBeAccessed() throws Exception {
        StartMockInterviewRequest reqAlice = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String aliceJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqAlice)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto aliceInterview = objectMapper.readValue(aliceJson, MockInterviewResponseDto.class);

        // Bob tries to access Alice's interview by id
        mockMvc.perform(get("/api/mock-interviews/" + aliceInterview.getId())
                        .header("Authorization", bobToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. GeminiService is mocked in tests")
    void testGeminiServiceIsMocked() throws Exception {
        StartMockInterviewRequest req = new StartMockInterviewRequest(MockInterviewType.MIXED, "System Design", Difficulty.HARD, 3);

        mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentQuestion.question", containsString("HashMap")));
    }

    @Test
    @DisplayName("16. Gemini failure is handled safely")
    void testGeminiFailureHandledSafely() throws Exception {
        // When Gemini throws an exception during evaluation
        when(geminiService.evaluateMockInterviewAnswer(anyString(), anyString(), any(), anyString(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("Gemini quota exceeded or timeout"));

        StartMockInterviewRequest req = new StartMockInterviewRequest(MockInterviewType.TECHNICAL, "Java", Difficulty.MEDIUM, 3);
        String createdJson = mockMvc.perform(post("/api/mock-interviews/start")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MockInterviewResponseDto created = objectMapper.readValue(createdJson, MockInterviewResponseDto.class);

        SubmitMockAnswerRequest answerReq = new SubmitMockAnswerRequest("My answer under Gemini outage");
        mockMvc.perform(post("/api/mock-interviews/" + created.getId() + "/answer")
                        .header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRound", is(2)));
    }
}
