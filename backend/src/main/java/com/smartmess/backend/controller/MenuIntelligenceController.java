
package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.DishFeedbackAnalyticsResponse;
import com.smartmess.backend.dto.response.DishIntelligenceResponse;
import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceOverviewResponse;
import com.smartmess.backend.service.MenuIntelligenceService;
import com.smartmess.backend.service.intelligence.ai.MenuIntelligenceTriggerService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/menu-intelligence")
public class MenuIntelligenceController {

    private final MenuIntelligenceService menuIntelligenceService;
    private final MenuIntelligenceTriggerService triggerService;

    public MenuIntelligenceController(
            MenuIntelligenceService menuIntelligenceService,
            MenuIntelligenceTriggerService triggerService) {

        this.menuIntelligenceService = menuIntelligenceService;
        this.triggerService = triggerService;
    }

    @GetMapping("/dish-ratings")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<DishRatingAnalyticsResponse>>
            getDishRatingAnalytics() {

        return ResponseEntity.ok(
                menuIntelligenceService.getDishRatingAnalytics());
    }

    @GetMapping("/dish-feedback")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<DishFeedbackAnalyticsResponse>>
            getDishFeedbackAnalytics() {

        return ResponseEntity.ok(
                menuIntelligenceService.getDishFeedbackAnalytics());
    }

    @GetMapping("/dishes")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<DishIntelligenceResponse>>
            getDishIntelligence() {

        return ResponseEntity.ok(
                menuIntelligenceService.getDishIntelligence());
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuIntelligenceOverviewResponse>
            getIntelligenceOverview() {

        return ResponseEntity.ok(
                menuIntelligenceService.getIntelligenceOverview());
    }

    @PostMapping("/analyze")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> analyzeMenuIntelligence(
            HttpServletRequest request) {

        try {

            boolean successful = triggerService.triggerAnalysis();

            if (!successful) {
                return ResponseEntity
                        .status(HttpStatus.BAD_GATEWAY)
                        .body(ApiResponse.failure(
                                "AI menu analysis could not be completed. Please try again later.",
                                request.getRequestURI(),
                                null
                        ));
            }

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "AI menu analysis completed successfully.",
                            request.getRequestURI(),
                            null
                    )
            );

        } catch (IllegalStateException exception) {

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.failure(
                            "AI menu analysis is currently unavailable.",
                            request.getRequestURI(),
                            null
                    ));
        }
    }
}
