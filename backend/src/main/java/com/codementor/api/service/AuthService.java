package com.codementor.api.service;

import com.codementor.api.dto.AuthResponse;
import com.codementor.api.dto.LoginRequest;
import com.codementor.api.dto.RegisterRequest;
import com.codementor.api.dto.UserDto;
import com.codementor.api.entity.User;
import com.codementor.api.exception.DuplicateEmailException;
import com.codementor.api.exception.InvalidCredentialsException;
import com.codementor.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ConcurrentHashMap<String, User> demoUsers = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100);

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

        // Seed a default demo candidate user
        User defaultCandidate = new User(
                "Candidate Demo",
                "candidate@codementor.ai",
                passwordEncoder.encode("Password123!"),
                "USER"
        );
        defaultCandidate.setId(1L);
        demoUsers.put("candidate@codementor.ai", defaultCandidate);
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (demoUsers.containsKey(normalizedEmail)) {
            throw new DuplicateEmailException("Email is already registered: " + normalizedEmail);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                hashedPassword,
                "USER"
        );
        user.setId(idGenerator.incrementAndGet());
        demoUsers.put(normalizedEmail, user);

        String token = jwtService.generateToken(user);
        UserDto userDto = new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = demoUsers.get(normalizedEmail);
        if (user == null) {
            // For frictionless demo mode: if user does not exist, check basic password and dynamically create demo profile
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                throw new InvalidCredentialsException("Invalid email or password.");
            }
            user = new User(
                    "Demo User",
                    normalizedEmail,
                    passwordEncoder.encode(request.getPassword()),
                    "USER"
            );
            user.setId(idGenerator.incrementAndGet());
            demoUsers.put(normalizedEmail, user);
        } else {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password.");
            }
        }

        String token = jwtService.generateToken(user);
        UserDto userDto = new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        return new AuthResponse(token, userDto);
    }

    public UserDto getCurrentUser(String email) {
        String normalized = email != null ? email.trim().toLowerCase() : "candidate@codementor.ai";
        User user = demoUsers.get(normalized);
        if (user == null) {
            user = new User(
                    "Candidate Demo",
                    normalized,
                    passwordEncoder.encode("Password123!"),
                    "USER"
            );
            user.setId(idGenerator.incrementAndGet());
            demoUsers.put(normalized, user);
        }

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public void clearDemoUsers() {
        demoUsers.clear();
        User defaultCandidate = new User(
                "Candidate Demo",
                "candidate@codementor.ai",
                passwordEncoder.encode("Password123!"),
                "USER"
        );
        defaultCandidate.setId(1L);
        demoUsers.put("candidate@codementor.ai", defaultCandidate);
    }
}
