package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MealPricingResponse(

        Long mealPricingId,

        BigDecimal halfMealPrice,

        BigDecimal fullMealPrice,

        BigDecimal extraRotiPrice,

        LocalDateTime updatedAt

) {
}