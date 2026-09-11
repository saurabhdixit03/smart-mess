package com.smartmess.backend.dto.request;

import java.time.LocalDate;

import com.smartmess.backend.enums.MealSession;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMessClosureRequest(

        @NotNull(message = "Start date is required.")
        LocalDate startDate,

        @NotNull(message = "Start session is required.")
        MealSession startSession,

        @NotNull(message = "End date is required.")
        LocalDate endDate,

        @NotNull(message = "End session is required.")
        MealSession endSession,

        @NotBlank(message = "Closure reason is required.")
        @Size(
                max = 255,
                message = "Closure reason must not exceed 255 characters."
        )
        String reason

) {}