package com.smartmess.backend.service.intelligence.ai;

import java.util.List;

import com.smartmess.backend.service.intelligence.ai.dto.OverallMealSuggestion;

public record ValidatedMenuFeedbackSuggestions(

        List<ValidatedDishSuggestion> dishSuggestions,

        List<OverallMealSuggestion> overallMealSuggestions
) {
}