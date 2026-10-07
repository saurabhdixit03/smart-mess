package com.smartmess.backend.service;

import com.smartmess.backend.dto.response.PaymentOverviewResponse;
import com.smartmess.backend.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse getPayment(
            Long paymentId
    );

    PaymentResponse getPaymentByBill(
            Long billId
    );

    PaymentOverviewResponse getPaymentOverview();
}