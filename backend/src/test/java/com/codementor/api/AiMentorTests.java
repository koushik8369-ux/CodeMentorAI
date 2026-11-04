package com.codementor.api;

import com.codementor.api.dto.AiMentorChatMessage;
import com.codementor.api.dto.AiMentorRequest;
import com.codementor.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AiMentorTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/health returns HTTP 200 with UP status")
    void testGetHealthReturns200() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("CodeMentor AI")));
    }

    @Test
    @DisplayName("GET /api/mentor/status returns HTTP 200 without authentication")
    void testGetMentorStatusWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/mentor/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service", containsString("CodeMentor AI Programming Mentor")))
                .andExpect(jsonPath("$.provider", is("OpenRouter API")))
                .andExpect(jsonPath("$.defaultModel", notNullValue()))
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Verify tightened production CORS: exact Vercel origin and OPTIONS preflight work, while broad wildcards are rejected")
    void testTightenedProductionCorsAndPreflight() throws Exception {
        // 1. Exact production frontend origin allowed with credentials
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "https://code-mentor-ai-hkrx.vercel.app"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://code-mentor-ai-hkrx.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(jsonPath("$.status", is("UP")));

        // 2. OPTIONS preflight request for /api/mentor/chat succeeds
        mockMvc.perform(options("/api/mentor/chat")
                        .header("Origin", "https://code-mentor-ai-hkrx.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization, content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://code-mentor-ai-hkrx.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));

        // 3. Localhost development origin allowed
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        // 4. Broad *.vercel.app wildcards are rejected with HTTP 403
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "https://malicious-untrusted.vercel.app"))
                .andExpect(status().isForbidden());

        // 5. Broad *.onrender.com wildcards are rejected with HTTP 403
        mockMvc.perform(get("/api/mentor/status")
                        .header("Origin", "https://untrusted-service.onrender.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/mentor/chat works without authentication (Demo Profile access)")
    void testChatWithMentorWithoutAuth() throws Exception {
        AiMentorRequest request = new AiMentorRequest(
                "How does Spring Boot dependency injection work?",
                List.of(new AiMentorChatMessage("user", "Hello mentor")),
                "Java",
                "Intermediate"
        );

        mockMvc.perform(post("/api/mentor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiResponse", notNullValue()))
                .andExpect(jsonPath("$.selectedModel", notNullValue()))
                .andExpect(jsonPath("$.status", anyOf(is("SUCCESS"), is("FALLBACK"))))
                .andExpect(jsonPath("$.programmingLanguage", is("Java")));
    }

    @Test
    @DisplayName("POST /api/mentor/chat works with valid JWT authentication")
    void testChatWithMentorWithJwtAuth() throws Exception {
        com.codementor.api.entity.User user = new com.codementor.api.entity.User("Candidate", "candidate@codementor.ai", "pass", "ROLE_CANDIDATE");
        user.setId(1L);
        String token = jwtService.generateToken(user);

        AiMentorRequest request = new AiMentorRequest(
                "Explain the two pointer algorithm for LeetCode array questions",
                List.of(),
                "Java",
                "Technical Interview Prep"
        );

        mockMvc.perform(post("/api/mentor/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiResponse", containsString("Two-Pointer")))
                .andExpect(jsonPath("$.status", anyOf(is("SUCCESS"), is("FALLBACK"))));
    }

    @Test
    @DisplayName("POST /api/mentor/chat should reject blank message with 400 Bad Request")
    void testChatWithBlankMessage() throws Exception {
        AiMentorRequest request = new AiMentorRequest("", List.of(), "Java", "Beginner");

        mockMvc.perform(post("/api/mentor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Existing protected endpoints (GET /api/interviews) remain protected and reject unauthenticated requests")
    void testProtectedEndpointsRemainProtected() throws Exception {
        mockMvc.perform(get("/api/interviews"))
                .andExpect(status().isForbidden());
    }
}
