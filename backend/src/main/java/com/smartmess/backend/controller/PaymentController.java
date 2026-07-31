package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreatePaymentRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.PaymentOverviewResponse;
import com.smartmess.backend.dto.response.PaymentResponse;
import com.smartmess.backend.dto.response.PendingPaymentResponse;
import com.smartmess.backend.dto.response.UpiPaymentResponse;
import com.smartmess.backend.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;

    }
    
    /*
     * Customer requests UPI payment verification.
     */
    @PostMapping("/request/{billId}")
    public ResponseEntity<ApiResponse<Void>> requestUpiPayment(

            @PathVariable Long billId,

            HttpServletRequest request) {

        paymentService.requestUpiPayment(billId);

        ApiResponse<Void> response =
                ApiResponse.success(
                        "Payment request submitted successfully.",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingPaymentResponse>>> getPendingPayments(
            HttpServletRequest request) {

        List<PendingPaymentResponse> pendingPayments =
                paymentService.getPendingPayments();

        ApiResponse<List<PendingPaymentResponse>> response =
                ApiResponse.success(
                        "Pending payment requests fetched successfully.",
                        request.getRequestURI(),
                        pendingPayments
                );

        return ResponseEntity.ok(response);

    }
    

    /*
     * Collect Payment
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> collectPayment(

            @Valid
            @RequestBody
            CreatePaymentRequest request,

            HttpServletRequest httpRequest) {

        PaymentResponse payment =
                paymentService.collectPayment(request);

        ApiResponse<PaymentResponse> response =
                ApiResponse.success(
                        "Payment collected successfully.",
                        httpRequest.getRequestURI(),
                        payment
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    /*
     * View Payment
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(

            @PathVariable Long paymentId,

            HttpServletRequest request) {

        PaymentResponse payment =
                paymentService.getPayment(paymentId);

        ApiResponse<PaymentResponse> response =
                ApiResponse.success(
                        "Payment retrieved successfully.",
                        request.getRequestURI(),
                        payment
                );

        return ResponseEntity.ok(response);

    }

    /*
     * View Payment by Bill
     */
    @GetMapping("/bill/{billId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBill(

            @PathVariable Long billId,

            HttpServletRequest request) {

        PaymentResponse payment =
                paymentService.getPaymentByBill(billId);

        ApiResponse<PaymentResponse> response =
                ApiResponse.success(
                        "Payment retrieved successfully.",
                        request.getRequestURI(),
                        payment
                );

        return ResponseEntity.ok(response);

    }
    
    @GetMapping("/pending/count")
    public ResponseEntity<ApiResponse<Long>> getPendingPaymentCount(
            HttpServletRequest request) {

        long pendingCount =
                paymentService.getPendingPaymentCount();

        ApiResponse<Long> response =
                ApiResponse.success(
                        "Pending payment count fetched successfully.",
                        request.getRequestURI(),
                        pendingCount
                );

        return ResponseEntity.ok(response);

    }
    
    // QR Code Generation link 
    @GetMapping("/upi/{billId}")
    public ResponseEntity<ApiResponse<UpiPaymentResponse>> generateUpiPayment(
            @PathVariable Long billId,
            HttpServletRequest request) {

        UpiPaymentResponse response =
                paymentService.generateUpiPayment(
                        billId
                );

        ApiResponse<UpiPaymentResponse> apiResponse =
                ApiResponse.success(
                        "UPI payment link generated successfully.",
                        request.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(
                apiResponse
        );

    }
    
    // payment overview
    
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<PaymentOverviewResponse>> getPaymentOverview(
            HttpServletRequest request) {

        PaymentOverviewResponse overview =
                paymentService.getPaymentOverview();

        ApiResponse<PaymentOverviewResponse> response =
                ApiResponse.success(
                        "Payment overview fetched successfully.",
                        request.getRequestURI(),
                        overview
                );

        return ResponseEntity.ok(response);
    }
    

}