package com.smartmess.backend.dto.response;

public record MealInsightsResponse(

        Long totalMeals,

        Long fullMeals,

        Long halfMeals,

        Long totalRotis,

        Long extraRotis

) {
}