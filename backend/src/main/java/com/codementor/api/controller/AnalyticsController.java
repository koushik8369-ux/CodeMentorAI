package com.codementor.api.controller;

import com.codementor.api.dto.*;
import com.codementor.api.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewDto> getOverview(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(analyticsService.getOverview(email));
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicAnalyticsDto>> getTopicAnalytics(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(analyticsService.getTopicAnalytics(email));
    }

    @GetMapping("/difficulty")
    public ResponseEntity<List<DifficultyAnalyticsDto>> getDifficultyAnalytics(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(analyticsService.getDifficultyAnalytics(email));
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LanguageAnalyticsDto>> getLanguageAnalytics(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(analyticsService.getLanguageAnalytics(email));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<PerformanceTrendDto>> getPerformanceTrend(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(analyticsService.getPerformanceTrend(email));
    }

    @GetMapping("/ai-insights")
    public ResponseEntity<AiInsightsDto> getAiInsights(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(analyticsService.getAiInsights(email));
    }
}
