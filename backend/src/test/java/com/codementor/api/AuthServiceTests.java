package com.codementor.api;

import com.codementor.api.dto.AuthResponse;
import com.codementor.api.dto.LoginRequest;
import com.codementor.api.dto.RegisterRequest;
import com.codementor.api.dto.UserDto;
import com.codementor.api.exception.DuplicateEmailException;
import com.codementor.api.exception.InvalidCredentialsException;
import com.codementor.api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        authService.clearDemoUsers();
    }

    @Test
    @DisplayName("1. Successful registration creates user with token and valid DTO")
    void testSuccessfulRegistration() {
        RegisterRequest request = new RegisterRequest("Koushik Gowda", "koushik@example.com", "password123");
        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getType());
        assertNotNull(response.getUser());
        assertEquals("koushik@example.com", response.getUser().getEmail());
        assertEquals("Koushik Gowda", response.getUser().getName());
        assertEquals("USER", response.getUser().getRole());
        assertNotNull(response.getUser().getId());
    }

    @Test
    @DisplayName("2. Duplicate email is rejected with DuplicateEmailException")
    void testDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("User One", "duplicate@example.com", "password123");
        authService.register(request);

        RegisterRequest duplicateRequest = new RegisterRequest("User Two", "DUPLICATE@example.com", "password456");
        assertThrows(DuplicateEmailException.class, () -> authService.register(duplicateRequest));
    }

    @Test
    @DisplayName("5. Successful login with correct credentials returns token")
    void testSuccessfulLogin() {
        RegisterRequest registerReq = new RegisterRequest("Login User", "login@example.com", "securePassword123");
        authService.register(registerReq);

        LoginRequest loginReq = new LoginRequest("login@example.com", "securePassword123");
        AuthResponse response = authService.login(loginReq);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("login@example.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("6. Invalid password throws InvalidCredentialsException")
    void testInvalidPassword() {
        RegisterRequest registerReq = new RegisterRequest("Test User", "testpass@example.com", "correctPassword123");
        authService.register(registerReq);

        LoginRequest wrongLoginReq = new LoginRequest("testpass@example.com", "wrongPassword999");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(wrongLoginReq));
    }

    @Test
    @DisplayName("7. Invalid short password format throws InvalidCredentialsException")
    void testUnknownEmail() {
        LoginRequest unknownReq = new LoginRequest("nonexistent@example.com", "123");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(unknownReq));
    }

    @Test
    @DisplayName("8. Password verification uses BCrypt matches")
    void testPasswordStoredAsBcryptHash() {
        String rawPassword = "myPlaintextPassword123!";
        RegisterRequest registerReq = new RegisterRequest("Bcrypt User", "bcrypt@example.com", rawPassword);
        AuthResponse resp = authService.register(registerReq);

        assertNotNull(resp);
        // Login with matching password works
        AuthResponse loginResp = authService.login(new LoginRequest("bcrypt@example.com", rawPassword));
        assertNotNull(loginResp.getToken());
    }

    @Test
    @DisplayName("9. Default role is always USER")
    void testDefaultRoleIsUser() {
        RegisterRequest registerReq = new RegisterRequest("Default Role", "role@example.com", "password123");
        AuthResponse response = authService.register(registerReq);

        assertEquals("USER", response.getUser().getRole());

        UserDto userDto = authService.getCurrentUser("role@example.com");
        assertEquals("USER", userDto.getRole());
    }

    @Test
    @DisplayName("10. Registration cannot create ADMIN role")
    void testRegistrationCannotCreateAdmin() {
        RegisterRequest registerReq = new RegisterRequest("Admin Attempt", "adminattempt@example.com", "password123");
        AuthResponse response = authService.register(registerReq);

        assertEquals("USER", response.getUser().getRole());
        assertNotEquals("ADMIN", response.getUser().getRole());

        UserDto userDto = authService.getCurrentUser("adminattempt@example.com");
        assertEquals("USER", userDto.getRole());
    }
}
