package com.smartmess.backend.dto.request;

import java.time.DayOfWeek;

public record UpdateWeeklyScheduleRequest(

        DayOfWeek weeklyClosedDay,

        boolean weeklyLunchClosed,

        boolean weeklyDinnerClosed

) {

}