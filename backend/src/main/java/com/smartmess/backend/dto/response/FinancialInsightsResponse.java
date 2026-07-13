package com.smartmess.backend.dto.response;

import java.math.BigDecimal;

public record FinancialInsightsResponse(

        Long billsGenerated,

        Long paidBills,

        Long pendingBills,

        BigDecimal totalRevenue,

        BigDecimal collectedRevenue,

        BigDecimal pendingRevenue,

        Double collectionRate

) {
}