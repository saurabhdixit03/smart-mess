package com.smartmess.backend.service;

import com.smartmess.backend.dto.response.PaymentCheckoutResponse;

public interface PaymentOrderService {

    /*
     * Start or reuse checkout for the authenticated customer's bill.
     */
    PaymentCheckoutResponse createCheckout(
            Long billId
    );

    /*
     * Check the gateway and return the customer's current order state.
     * Client-provided success flags are never used.
     */
    PaymentCheckoutResponse verifyCheckout(
            Long paymentOrderId
    );

    /*
     * Verify the signature before processing the original payload.
     */
    void handleWebhook(
            byte[] rawBody,
            String timestamp,
            String signature
    );

    /*
     * Recover due orders using their persisted tenant context.
     */
    void reconcileDueOrders();
}