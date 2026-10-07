package com.smartmess.backend.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MessSettingsResponse(

        Long settingsId,

        LocalTime lunchResponseCutoff,

        LocalTime dinnerResponseCutoff,

        DayOfWeek weeklyClosedDay,

        boolean weeklyLunchClosed,

        boolean weeklyDinnerClosed,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}