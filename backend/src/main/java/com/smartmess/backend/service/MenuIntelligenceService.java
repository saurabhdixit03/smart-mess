
package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.response.DishFeedbackAnalyticsResponse;
import com.smartmess.backend.dto.response.DishIntelligenceResponse;
import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceOverviewResponse;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;

public interface MenuIntelligenceService {

    List<DishRatingAnalyticsResponse> getDishRatingAnalytics();

    List<DishFeedbackAnalyticsResponse> getDishFeedbackAnalytics();

    List<DishIntelligenceResponse> getDishIntelligence();

    MenuIntelligenceContext buildIntelligenceContext();

    MenuIntelligenceOverviewResponse getIntelligenceOverview();
}
