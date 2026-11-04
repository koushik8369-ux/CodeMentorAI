package com.codementor.api.controller;

import com.codementor.api.dto.AiMentorRequest;
import com.codementor.api.dto.AiMentorResponse;
import com.codementor.api.service.AiMentorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mentor")
public class AiMentorController {

    private final AiMentorService aiMentorService;

    public AiMentorController(AiMentorService aiMentorService) {
        this.aiMentorService = aiMentorService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiMentorResponse> chatWithMentor(@Valid @RequestBody AiMentorRequest request) {
        AiMentorResponse response = aiMentorService.chatWithMentor(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMentorStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "CodeMentor AI Programming Mentor Agent",
                "provider", "OpenRouter API",
                "defaultModel", aiMentorService.resolveModel(null),
                "isConfigured", aiMentorService.isConfigured(),
                "status", "UP"
        ));
    }
}
