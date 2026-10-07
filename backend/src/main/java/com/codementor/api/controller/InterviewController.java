package com.codementor.api.controller;

import com.codementor.api.dto.*;
import com.codementor.api.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public ResponseEntity<InterviewResponse> startInterview(@Valid @RequestBody StartInterviewRequest request,
                                                           Authentication authentication) {
        String email = authentication.getName();
        InterviewResponse response = interviewService.startInterview(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<InterviewResponse> submitInterview(@PathVariable Long id,
                                                            @Valid @RequestBody SubmitInterviewRequest request,
                                                            Authentication authentication) {
        String email = authentication.getName();
        InterviewResponse response = interviewService.submitInterview(id, request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getUserInterviews(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(interviewService.getUserInterviews(email));
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewResponse>> getInterviewHistory(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(interviewService.getUserInterviews(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id,
                                                             Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(interviewService.getInterviewById(id, email));
    }

    // Backward-compatible endpoint for legacy callers
    @PostMapping
    public ResponseEntity<InterviewResponse> createInterviewLegacy(@Valid @RequestBody InterviewRequest request) {
        InterviewResponse created = interviewService.createInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
