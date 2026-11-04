package com.smartmess.backend.service.intelligence.ai.dto;

import java.util.List;

public record MenuFeedbackAiResponse(

        List<DishSuggestion> dishSuggestions,

        List<OverallMealSuggestion> overallMealSuggestions
) {
}