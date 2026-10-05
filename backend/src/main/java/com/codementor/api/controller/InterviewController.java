package com.codementor.api.controller;

import com.codementor.api.dto.InterviewRequest;
import com.codementor.api.dto.InterviewResponse;
import com.codementor.api.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getInterviews() {
        return ResponseEntity.ok(interviewService.getAllInterviews());
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> createInterview(@Valid @RequestBody InterviewRequest request) {
        InterviewResponse created = interviewService.createInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
