package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.CreatePaymentRequest;
import com.smartmess.backend.dto.response.PaymentResponse;

public interface PaymentService {

    /*
     * Collect Payment
     */
    PaymentResponse collectPayment(
            CreatePaymentRequest request
    );

    /*
     * View Payment
     */
    PaymentResponse getPayment(
            Long paymentId
    );

    /*
     * View Payment By Bill
     */
    PaymentResponse getPaymentByBill(
            Long billId
    );

    void requestUpiPayment(Long billId);
    
}