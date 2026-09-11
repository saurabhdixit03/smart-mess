package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.MealSession;

public record MenuAvailabilityResponse(

        MealSession mealSession,

        boolean canPublish,

        String reason

) {
}