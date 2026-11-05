package com.smartmess.backend.service.intelligence;

import java.time.LocalDate;
import java.util.List;

import com.smartmess.backend.repository.projection.OverallMealFeedbackProjection;

public record MenuIntelligenceContext(
        Long messId,
        List<MenuIntelligenceObservation> observations,
        List<DishIntelligenceSummary> dishSummaries,
        List<ProcessedFeedbackComment> overallMealComments,
        List<OverallMealFeedbackProjection> overallMealFeedbackObservations,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd
) {
}