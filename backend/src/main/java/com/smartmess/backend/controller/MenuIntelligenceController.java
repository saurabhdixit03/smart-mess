package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.DishFeedbackAnalyticsResponse;
import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.repository.projection.DishFeedbackObservationProjection;
import com.smartmess.backend.service.MenuIntelligenceService;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;

@RestController
@RequestMapping("/api/menu-intelligence")
public class MenuIntelligenceController {

    private final MenuIntelligenceService menuIntelligenceService;

    public MenuIntelligenceController(
            MenuIntelligenceService menuIntelligenceService) {
        this.menuIntelligenceService =
                menuIntelligenceService;
    }

    @GetMapping("/dish-ratings")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<DishRatingAnalyticsResponse>>
            getDishRatingAnalytics() {

        return ResponseEntity.ok(
                menuIntelligenceService.getDishRatingAnalytics()
        );
    }
    
    @GetMapping("/dish-feedback")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<DishFeedbackAnalyticsResponse>> getDishFeedbackAnalytics() {
        return ResponseEntity.ok(menuIntelligenceService.getDishFeedbackAnalytics());
    }
    
    @GetMapping("/dish-feedback-observations")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<DishFeedbackObservationProjection>> getDishFeedbackObservations() {

        return ResponseEntity.ok(
                menuIntelligenceService.getDishFeedbackObservations()
        );
    }
    
    @GetMapping("/context")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuIntelligenceContext> getIntelligenceContext() {
        return ResponseEntity.ok(
                menuIntelligenceService.buildIntelligenceContext()
        );
    }
}