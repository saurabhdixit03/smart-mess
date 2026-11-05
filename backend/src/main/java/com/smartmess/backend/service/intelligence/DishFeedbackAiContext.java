package com.smartmess.backend.service.intelligence;

import java.util.List;

public record DishFeedbackAiContext(
        String dishName,

        // All-time statistics
        Long ratingCount,
        Double averageRating,
        Long oneStarCount,
        Long twoStarCount,
        Long threeStarCount,
        Long fourStarCount,
        Long fiveStarCount,
        Double lowRatingPercentage,
        Double threeStarOrBelowPercentage,
        List<ProcessedFeedbackComment> comments,

        // Current and previous period comparison
        Long currentPeriodRatingCount,
        Double currentPeriodAverageRating,
        Long previousPeriodRatingCount,
        Double previousPeriodAverageRating,
        String ratingTrend,

        // Current period comments
        List<ProcessedFeedbackComment> currentPeriodComments
) {
}