package com.smartmess.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.MonthlyInsightsResponse;
import com.smartmess.backend.service.InsightsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping("/monthly")
    public MonthlyInsightsResponse getMonthlyInsights(

            @RequestParam Integer month,

            @RequestParam Integer year) {

        return insightsService.getMonthlyInsights(
                month,
                year
        );

    }

}