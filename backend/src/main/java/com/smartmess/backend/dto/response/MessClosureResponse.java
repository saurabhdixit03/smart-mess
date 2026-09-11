package com.smartmess.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.MealSession;

public record MessClosureResponse(

        Long closureId,
        LocalDate startDate,
        MealSession startSession,
        LocalDate endDate,
        MealSession endSession,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}