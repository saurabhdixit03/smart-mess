package com.smartmess.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.MealSession;

public record MenuResponse(

        Long menuId,

        LocalDate menuDate,

        MealSession mealSession,

        String sabjiOne,

        String sabjiTwo,

        String dal,

        String rice,

        String sweet,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}