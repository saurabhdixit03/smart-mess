package com.smartmess.backend.service.intelligence;

import java.util.List;

public record DishIntelligenceSummary(
        Long dishId,
        String dishName,
        Long ratingCount,
        Double averageRating,
        Long oneStarCount,
        Long twoStarCount,
        Long threeStarCount,
        Long fourStarCount,
        Long fiveStarCount,
        Double lowRatingPercentage,
        Double threeStarOrBelowPercentage,
        List<String> comments
) {
}