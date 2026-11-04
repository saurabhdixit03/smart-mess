package com.smartmess.backend.service.intelligence.ai.dto;

import com.smartmess.backend.enums.SuggestionPriority;

public record OverallMealSuggestion(

        String observation,

        String recommendation,

        SuggestionPriority priority
) {
}