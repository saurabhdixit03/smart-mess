package com.smartmess.backend.dto.request;

import com.smartmess.backend.enums.MealOption;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateMealRecordRequest(

        @NotNull(message = "Customer ID is required.")
        Long customerId,

        @NotNull(message = "Menu ID is required.")
        Long menuId,

        /*
         * Optional.
         * Null when the meal is collected without
         * a prior meal response.
         */
        Long mealResponseId,

        @NotNull(message = "Please select the actual meal served (HALF or FULL).")
        MealOption mealOption,

        @NotNull(message = "Extra roti count is required.")
        @Min(value = 0, message = "Extra roti count cannot be negative.")
        @Max(value = 5, message = "Extra roti count cannot exceed 5.")
        Integer extraRotiCount

) {
}