package com.codementor.api;

import com.codementor.api.dto.AuthResponse;
import com.codementor.api.dto.LoginRequest;
import com.codementor.api.dto.RegisterRequest;
import com.codementor.api.entity.User;
import com.codementor.api.exception.DuplicateEmailException;
import com.codementor.api.exception.InvalidCredentialsException;
import com.codementor.api.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
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
    @DisplayName("7. Unknown email throws InvalidCredentialsException")
    void testUnknownEmail() {
        LoginRequest unknownReq = new LoginRequest("nonexistent@example.com", "somePassword123");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(unknownReq));
    }

    @Test
    @DisplayName("8. Password is stored strictly as a BCrypt hash and not plaintext")
    void testPasswordStoredAsBcryptHash() {
        String rawPassword = "myPlaintextPassword123!";
        RegisterRequest registerReq = new RegisterRequest("Bcrypt User", "bcrypt@example.com", rawPassword);
        authService.register(registerReq);

        User savedUser = userRepository.findByEmail("bcrypt@example.com").orElseThrow();

        assertNotEquals(rawPassword, savedUser.getPassword());
        assertTrue(savedUser.getPassword().startsWith("$2a$") || savedUser.getPassword().startsWith("$2b$") || savedUser.getPassword().startsWith("$2y$"),
                "Password must be hashed using BCrypt algorithm");
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }

    @Test
    @DisplayName("9. Default role is always USER")
    void testDefaultRoleIsUser() {
        RegisterRequest registerReq = new RegisterRequest("Default Role", "role@example.com", "password123");
        AuthResponse response = authService.register(registerReq);

        assertEquals("USER", response.getUser().getRole());

        User savedUser = userRepository.findByEmail("role@example.com").orElseThrow();
        assertEquals("USER", savedUser.getRole());
    }

    @Test
    @DisplayName("10. Registration cannot create ADMIN role")
    void testRegistrationCannotCreateAdmin() {
        RegisterRequest registerReq = new RegisterRequest("Admin Attempt", "adminattempt@example.com", "password123");
        AuthResponse response = authService.register(registerReq);

        // Verification that standard registration path always enforces USER
        assertEquals("USER", response.getUser().getRole());
        assertNotEquals("ADMIN", response.getUser().getRole());

        User savedUser = userRepository.findByEmail("adminattempt@example.com").orElseThrow();
        assertEquals("USER", savedUser.getRole());
    }
}
