package com.codementor.api.controller;

import com.codementor.api.dto.QuestionGenerationRequest;
import com.codementor.api.dto.QuestionGenerationResponse;
import com.codementor.api.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generate-question")
    public ResponseEntity<QuestionGenerationResponse> generateQuestion(
            @Valid @RequestBody QuestionGenerationRequest request) {
        QuestionGenerationResponse response = geminiService.generateQuestion(request);
        return ResponseEntity.ok(response);
    }
}
