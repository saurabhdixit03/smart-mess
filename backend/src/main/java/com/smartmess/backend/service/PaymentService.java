package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.CreatePaymentRequest;
import com.smartmess.backend.dto.response.PaymentResponse;
import com.smartmess.backend.dto.response.PendingPaymentResponse;
import com.smartmess.backend.dto.response.UpiPaymentResponse;
import com.smartmess.backend.dto.response.PaymentOverviewResponse;

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
    
    // for payment dashboard
    List<PendingPaymentResponse> getPendingPayments();
    
    long getPendingPaymentCount();
    
    // for UPI QR code
    UpiPaymentResponse generateUpiPayment(Long billId);
    
    
    /*
     * Payment Dashboard Overview
     */
    PaymentOverviewResponse getPaymentOverview();
    
}