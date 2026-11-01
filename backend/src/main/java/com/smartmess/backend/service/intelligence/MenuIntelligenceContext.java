package com.smartmess.backend.service.intelligence;

import java.util.List;

public record MenuIntelligenceContext(
        Long messId,
        List<MenuIntelligenceObservation> observations,
        List<DishIntelligenceSummary> dishSummaries
) {
}