package com.codementor.api.service;

import com.codementor.api.dto.AuthResponse;
import com.codementor.api.dto.LoginRequest;
import com.codementor.api.dto.RegisterRequest;
import com.codementor.api.dto.UserDto;
import com.codementor.api.entity.User;
import com.codementor.api.exception.DuplicateEmailException;
import com.codementor.api.exception.InvalidCredentialsException;
import com.codementor.api.exception.ResourceNotFoundException;
import com.codementor.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email is already registered: " + normalizedEmail);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Default role is always USER. Registration cannot assign ADMIN role.
        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                hashedPassword,
                "USER"
        );

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        UserDto userDto = new UserDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );

        return new AuthResponse(token, userDto);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
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

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
