package com.smartmess.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.DashboardSummaryResponse;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.service.DashboardService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @RequestParam MealSession mealSession,
            HttpServletRequest request) {

        DashboardSummaryResponse dashboardSummary =
                dashboardService.getDashboardSummary(mealSession);

        ApiResponse<DashboardSummaryResponse> response =
                ApiResponse.success(
                        "Dashboard retrieved successfully.",
                        request.getRequestURI(),
                        dashboardSummary
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}