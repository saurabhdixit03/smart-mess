package com.smartmess.backend.dto.response.ai;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MenuIntelligenceAnalysisResponse(

        LocalDate periodStart,

        LocalDate periodEnd,

        LocalDateTime completedAt,

        int suggestionCount
) {
}