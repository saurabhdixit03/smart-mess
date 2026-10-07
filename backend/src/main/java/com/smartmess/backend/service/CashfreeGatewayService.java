package com.smartmess.backend.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface CashfreeGatewayService {

    GatewayOrder createOrder(
            CreateOrderCommand command
    );

    GatewayOrder getOrder(
            String gatewayOrderId,
            String environment
    );

    List<GatewayPayment> getOrderPayments(
            String gatewayOrderId,
            String environment
    );

    boolean verifyWebhookSignature(
            byte[] rawBody,
            String timestamp,
            String signature
    );

    /*
     * Values come from persisted backend data and configuration.
     * Repeated creation requests reuse the same order ID and key.
     */
    record CreateOrderCommand(
            String gatewayOrderId,
            String idempotencyKey,
            String environment,
            BigDecimal amount,
            String currency,
            String customerReference,
            String customerName,
            String customerEmail,
            String customerPhone,
            OffsetDateTime expiresAt,
            String returnUrl,
            String notifyUrl
    ) {
    }

    record GatewayOrder(
            String gatewayOrderId,
            String providerOrderId,
            String paymentSessionId,
            String status,
            BigDecimal amount,
            String currency,
            OffsetDateTime expiresAt
    ) {
    }

    record GatewayPayment(
            String gatewayOrderId,
            String gatewayPaymentId,
            String status,
            BigDecimal amount,
            String currency,
            boolean captured,
            OffsetDateTime completedAt
    ) {
    }

    enum FailureKind {

        REJECTED,

        NOT_FOUND,

        UNCERTAIN,

        CONFIGURATION
    }

    /*
     * Carries only a safe summary.
     * Raw responses and credentials must not enter client errors.
     */
    final class GatewayException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final FailureKind failureKind;

        public GatewayException(
                FailureKind failureKind,
                String safeMessage) {

            super(safeMessage);
            this.failureKind = failureKind;
        }

        public FailureKind getFailureKind() {
            return failureKind;
        }
    }
}