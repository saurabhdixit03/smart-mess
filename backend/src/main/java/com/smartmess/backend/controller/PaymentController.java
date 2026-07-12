package com.smartmess.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreatePaymentRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.PaymentResponse;
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

}