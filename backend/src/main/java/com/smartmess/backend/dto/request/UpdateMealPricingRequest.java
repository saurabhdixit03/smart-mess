package com.smartmess.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record UpdateMealPricingRequest(

        @NotNull(message = "Half Meal price is required.")
        @DecimalMin(
                value = "0.01",
                message = "Half Meal price must be greater than 0."
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Half Meal price supports up to 8 whole digits and 2 decimal places."
        )
        BigDecimal halfMealPrice,

        @NotNull(message = "Full Meal price is required.")
        @DecimalMin(
                value = "0.01",
                message = "Full Meal price must be greater than 0."
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Full Meal price supports up to 8 whole digits and 2 decimal places."
        )
        BigDecimal fullMealPrice,

        @NotNull(message = "Extra Roti price is required.")
        @DecimalMin(
                value = "0.01",
                message = "Extra Roti price must be greater than 0."
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Extra Roti price supports up to 8 whole digits and 2 decimal places."
        )
        BigDecimal extraRotiPrice,

        /*
         * Optional for compatibility with existing clients.
         * Past dates are rejected by the service using the app Clock.
         */
        LocalDate effectiveDate

) {

    public UpdateMealPricingRequest(
            BigDecimal halfMealPrice,
            BigDecimal fullMealPrice,
            BigDecimal extraRotiPrice) {

        this(
                halfMealPrice,
                fullMealPrice,
                extraRotiPrice,
                null
        );
    }
}