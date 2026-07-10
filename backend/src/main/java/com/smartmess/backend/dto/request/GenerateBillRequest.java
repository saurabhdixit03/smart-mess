package com.smartmess.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateBillRequest(

        @NotNull(message = "Billing month is required.")
        @Min(value = 1, message = "Billing month must be between 1 and 12.")
        @Max(value = 12, message = "Billing month must be between 1 and 12.")
        Integer billingMonth,

        @NotNull(message = "Billing year is required.")
        Integer billingYear

) {
}