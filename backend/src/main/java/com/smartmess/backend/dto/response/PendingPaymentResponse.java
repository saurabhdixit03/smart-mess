package com.smartmess.backend.dto.response;

import java.math.BigDecimal;

import com.smartmess.backend.enums.BillStatus;

public record PendingPaymentResponse(

        Long billId,

        Long customerId,

        String customerName,

        Integer billingMonth,

        Integer billingYear,

        BigDecimal totalAmount,

        BillStatus billStatus

) {
}