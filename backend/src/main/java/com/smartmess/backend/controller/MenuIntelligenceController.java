package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.service.MenuIntelligenceService;

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
}