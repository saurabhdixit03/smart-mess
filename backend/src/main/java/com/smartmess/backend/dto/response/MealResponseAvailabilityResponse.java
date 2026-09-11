package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.MealSession;

public record MealResponseAvailabilityResponse(
        Long menuId,
        MealSession mealSession,
        boolean canRespond,
        String reason
) {
}