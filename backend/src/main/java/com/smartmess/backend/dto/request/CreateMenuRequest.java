package com.smartmess.backend.dto.request;

import java.time.LocalDate;

import com.smartmess.backend.enums.MealSession;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMenuRequest(

        @NotNull(message = "Menu date is required")
        LocalDate menuDate,

        @NotNull(message = "Meal session is required")
        MealSession mealSession,

        @NotBlank(message = "First sabji is required")
        @Size(max = 100, message = "First sabji cannot exceed 100 characters")
        String sabjiOne,

        @Size(max = 100, message = "Second sabji cannot exceed 100 characters")
        String sabjiTwo,

        @Size(max = 100, message = "Dal cannot exceed 100 characters")
        String dal,

        @Size(max = 100, message = "Rice cannot exceed 100 characters")
        String rice,

        @Size(max = 100, message = "Sweet cannot exceed 100 characters")
        String sweet

) {
}