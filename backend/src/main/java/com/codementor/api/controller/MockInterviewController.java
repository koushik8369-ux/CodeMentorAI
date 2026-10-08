package com.codementor.api.controller;

import com.codementor.api.dto.MockInterviewResponseDto;
import com.codementor.api.dto.StartMockInterviewRequest;
import com.codementor.api.dto.SubmitMockAnswerRequest;
import com.codementor.api.service.MockInterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mock-interviews")
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    public MockInterviewController(MockInterviewService mockInterviewService) {
        this.mockInterviewService = mockInterviewService;
    }

    @PostMapping("/start")
    public ResponseEntity<MockInterviewResponseDto> startMockInterview(
            @Valid @RequestBody StartMockInterviewRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        MockInterviewResponseDto response = mockInterviewService.startMockInterview(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<MockInterviewResponseDto> submitAnswer(
            @PathVariable Long id,
            @Valid @RequestBody SubmitMockAnswerRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        MockInterviewResponseDto response = mockInterviewService.submitAnswer(id, request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MockInterviewResponseDto>> getUserMockInterviews(Authentication authentication) {
        String email = authentication.getName();
        List<MockInterviewResponseDto> list = mockInterviewService.getUserMockInterviews(email);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MockInterviewResponseDto> getMockInterviewById(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        MockInterviewResponseDto response = mockInterviewService.getMockInterviewById(id, email);
        return ResponseEntity.ok(response);
    }
}
