package com.smartmess.backend.dto.response;

public record MonthlyInsightsResponse(

        String month,

        Integer year,

        FinancialInsightsResponse financial,

        CustomerInsightsResponse customers,

        MealInsightsResponse meals

) {
}