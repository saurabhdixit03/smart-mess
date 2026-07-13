package com.smartmess.backend.service;

import java.math.BigDecimal;

import com.smartmess.backend.dto.response.MonthlyInsightsResponse;

public interface InsightsService {

    MonthlyInsightsResponse getMonthlyInsights(
            Integer month,
            Integer year
    );

  
}