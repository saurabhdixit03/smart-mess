package com.smartmess.backend.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record UpdateResponseWindowRequest(

        @NotNull(message = "Lunch response cutoff time is required.")
        LocalTime lunchResponseCutoff,

        @NotNull(message = "Dinner response cutoff time is required.")
        LocalTime dinnerResponseCutoff

) {

}