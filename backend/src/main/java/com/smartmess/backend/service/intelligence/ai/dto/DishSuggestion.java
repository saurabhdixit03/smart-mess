package com.smartmess.backend.service.intelligence.ai.dto;

import com.smartmess.backend.enums.SuggestionPriority;

public record DishSuggestion(

        String dishName,

        String observation,

        String recommendation,

        SuggestionPriority priority
) {
}