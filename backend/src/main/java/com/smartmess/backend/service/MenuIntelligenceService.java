package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;

public interface MenuIntelligenceService {

    List<DishRatingAnalyticsResponse> getDishRatingAnalytics();
}