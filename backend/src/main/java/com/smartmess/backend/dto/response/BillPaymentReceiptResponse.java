package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.PaymentMode;

public record BillPaymentReceiptResponse(

        Long paymentId,

        BigDecimal paymentAmount,

        PaymentMode paymentMode,

        LocalDateTime paidAt,

        /*
         * Taken from the successfully settled payment order.
         * Null when historical gateway metadata is unavailable.
         */
        String environment,

        String currency,

        String gatewayOrderId,

        String gatewayPaymentId

) {
}