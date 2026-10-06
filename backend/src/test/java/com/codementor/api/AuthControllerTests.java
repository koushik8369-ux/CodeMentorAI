package com.codementor.api;

import com.codementor.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("3. Invalid email format returns HTTP 400 Bad Request")
    void testInvalidEmailReturns400() throws Exception {
        Map<String, String> payload = Map.of(
                "name", "Invalid Email User",
                "email", "not-a-valid-email",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details.email", notNullValue()));
    }

    @Test
    @DisplayName("4. Short password (< 8 chars) returns HTTP 400 Bad Request")
    void testShortPasswordReturns400() throws Exception {
        Map<String, String> payload = Map.of(
                "name", "Short Password User",
                "email", "short@example.com",
                "password", "short"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details.password", containsString("at least 8 characters")));
    }

    @Test
    @DisplayName("Successful registration returns HTTP 201 Created without exposing password")
    void testRegistrationReturns201() throws Exception {
        Map<String, String> payload = Map.of(
                "name", "Koushik Gowda",
                "email", "koushik.api@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("koushik.api@example.com")))
                .andExpect(jsonPath("$.user.name", is("Koushik Gowda")))
                .andExpect(jsonPath("$.user.role", is("USER")))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @DisplayName("Duplicate email registration returns HTTP 409 Conflict")
    void testDuplicateEmailReturns409() throws Exception {
        Map<String, String> payload = Map.of(
                "name", "First User",
                "email", "duplicate.api@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Duplicate Email")));
    }

    @Test
    @DisplayName("Successful login returns HTTP 200 OK with token")
    void testLoginReturns200() throws Exception {
        Map<String, String> registerPayload = Map.of(
                "name", "Login User",
                "email", "login.api@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isCreated());

        Map<String, String> loginPayload = Map.of(
                "email", "login.api@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("login.api@example.com")))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @DisplayName("Invalid login returns HTTP 401 Unauthorized")
    void testInvalidLoginReturns401() throws Exception {
        Map<String, String> loginPayload = Map.of(
                "email", "unknown@example.com",
                "password", "wrongpassword123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }
}
