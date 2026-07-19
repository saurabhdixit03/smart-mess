package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.MealOption;

public record MealRecordResponse(

        Long mealRecordId,

        Long customerId,

        String customerName,

        Long menuId,

        Long mealResponseId,

        MealOption mealOption,

        BigDecimal mealPrice,

        Integer extraRotiCount,

        BigDecimal extraRotiPrice,

        BigDecimal totalAmount,

        LocalDateTime collectedAt

) {
}