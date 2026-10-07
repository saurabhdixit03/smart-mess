package com.smartmess.backend.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GenerateBillRequest(

        @NotNull(message = "Billing month is required.")
        @Min(
                value = 1,
                message = "Billing month must be between 1 and 12."
        )
        @Max(
                value = 12,
                message = "Billing month must be between 1 and 12."
        )
        Integer billingMonth,

        @NotNull(message = "Billing year is required.")
        @Min(
                value = 1000,
                message = "Billing year must be between 1000 and 9998."
        )
        @Max(
                value = 9998,
                message = "Billing year must be between 1000 and 9998."
        )
        Integer billingYear,

        /*
         * Optional.
         * Null means generate bills for eligible customers
         * within the authenticated owner's mess.
         */
        @Positive(message = "Customer ID must be positive.")
        Long customerId,

        /*
         * Optional inclusive boundaries within the billing month.
         * The service resolves defaults and validates the range
         * using the application's configured clock.
         */
        LocalDate startDate,

        LocalDate endDate

) {

    /*
     * Preserve existing Java callers using month and year only.
     */
    public GenerateBillRequest(
            Integer billingMonth,
            Integer billingYear) {

        this(
                billingMonth,
                billingYear,
                null,
                null,
                null
        );
    }
}