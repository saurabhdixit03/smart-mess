package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.PaymentOrderStatus;

public record PaymentCheckoutResponse(

        Long paymentOrderId,

        Long billId,

        String gatewayOrderId,

        String paymentSessionId,

        BigDecimal amount,

        String currency,

        String environment,

        PaymentOrderStatus status,

        LocalDateTime expiresAt

) {
}