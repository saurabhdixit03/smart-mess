package com.smartmess.backend.dto.request;

import com.smartmess.backend.enums.PaymentMode;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "Bill ID is required.")
        Long billId,

        @NotNull(message = "Payment mode is required.")
        PaymentMode paymentMode

) {
}