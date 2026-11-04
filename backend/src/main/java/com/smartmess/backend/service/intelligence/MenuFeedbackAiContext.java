package com.smartmess.backend.service.intelligence;

import java.time.LocalDate;
import java.util.List;

public record MenuFeedbackAiContext(
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd,
        List<DishFeedbackAiContext> dishFeedback,
        List<ProcessedFeedbackComment> overallMealComments,
        List<ProcessedFeedbackComment> currentPeriodOverallComments
) {
}