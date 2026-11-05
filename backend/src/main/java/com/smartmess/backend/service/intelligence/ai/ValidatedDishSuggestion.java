package com.smartmess.backend.service.intelligence.ai;

import com.smartmess.backend.enums.SuggestionPriority;

public record ValidatedDishSuggestion(

        Long dishId,

        String dishName,

        String observation,

        String recommendation,

        SuggestionPriority priority
) {
}