package com.smartmess.backend.dto.response.ai;

import com.smartmess.backend.enums.SuggestionPriority;

public record MenuIntelligenceSuggestionResponse(

        String dishName,

        String observation,

        String recommendation,

        SuggestionPriority priority
) {
}