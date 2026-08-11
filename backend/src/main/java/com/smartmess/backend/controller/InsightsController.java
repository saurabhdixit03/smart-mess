
package com.smartmess.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MonthlyInsightsResponse;
import com.smartmess.backend.service.InsightsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping("/monthly")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MonthlyInsightsResponse>> getMonthlyInsights(

            @RequestParam Integer month,

            @RequestParam Integer year,

            HttpServletRequest request) {

        MonthlyInsightsResponse response =
                insightsService.getMonthlyInsights(
                        month,
                        year
                );

        ApiResponse<MonthlyInsightsResponse> apiResponse =
                ApiResponse.success(
                        "Monthly insights retrieved successfully.",
                        request.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }
}

