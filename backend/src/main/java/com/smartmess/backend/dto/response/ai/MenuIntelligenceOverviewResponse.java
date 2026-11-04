package com.smartmess.backend.dto.response.ai;

import java.util.List;

public record MenuIntelligenceOverviewResponse(

        MenuIntelligenceAnalysisResponse analysis,

        List<MenuIntelligenceSuggestionResponse> suggestions
) {
}