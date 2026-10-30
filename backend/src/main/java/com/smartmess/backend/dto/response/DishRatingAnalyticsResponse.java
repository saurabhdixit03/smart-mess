package com.smartmess.backend.dto.response;

public record DishRatingAnalyticsResponse(
        Long dishId,
        String dishName,
        Long totalRatings,
        Double averageRating,
        Long oneStarCount,
        Long twoStarCount,
        Long threeStarCount,
        Long fourStarCount,
        Long fiveStarCount
) {
}