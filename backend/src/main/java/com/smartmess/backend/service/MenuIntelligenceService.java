package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.dto.response.DishFeedbackAnalyticsResponse;
import com.smartmess.backend.repository.projection.DishFeedbackObservationProjection;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;

public interface MenuIntelligenceService {

    List<DishRatingAnalyticsResponse> getDishRatingAnalytics();
    
    List<DishFeedbackAnalyticsResponse> getDishFeedbackAnalytics();
    
    MenuIntelligenceContext buildIntelligenceContext();
}