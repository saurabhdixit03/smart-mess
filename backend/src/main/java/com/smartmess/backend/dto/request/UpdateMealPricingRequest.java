package com.smartmess.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateMealPricingRequest(

        @NotNull(message = "Half Meal price is required")
        @DecimalMin(value = "0.01", message = "Half Meal price must be greater than 0")
        BigDecimal halfMealPrice,

        @NotNull(message = "Full Meal price is required")
        @DecimalMin(value = "0.01", message = "Full Meal price must be greater than 0")
        BigDecimal fullMealPrice,

        @NotNull(message = "Extra Roti price is required")
        @DecimalMin(value = "0.01", message = "Extra Roti price must be greater than 0")
        BigDecimal extraRotiPrice

) {
}