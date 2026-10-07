package com.smartmess.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.PaymentCheckoutResponse;
import com.smartmess.backend.dto.response.PaymentOverviewResponse;
import com.smartmess.backend.dto.response.PaymentResponse;
import com.smartmess.backend.service.PaymentOrderService;
import com.smartmess.backend.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentOrderService paymentOrderService;

    public PaymentController(
            PaymentService paymentService,
            PaymentOrderService paymentOrderService) {

        this.paymentService = paymentService;
        this.paymentOrderService = paymentOrderService;
    }

    /*
     * Start or reuse checkout for the customer's own bill.
     * Amount comes from the backend bill.
     */
    @PostMapping("/checkout/bill/{billId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentCheckoutResponse>> createCheckout(
            @PathVariable Long billId,
            HttpServletRequest request) {

        PaymentCheckoutResponse checkout =
                paymentOrderService.createCheckout(billId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment checkout state retrieved successfully.",
                        request.getRequestURI(),
                        checkout
                )
        );
    }

    /*
     * Verify against the gateway.
     * No client-provided success flag is accepted.
     */
    @PostMapping("/orders/{paymentOrderId}/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentCheckoutResponse>> verifyCheckout(
            @PathVariable Long paymentOrderId,
            HttpServletRequest request) {

        PaymentCheckoutResponse checkout =
                paymentOrderService.verifyCheckout(
                        paymentOrderId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment order state retrieved successfully.",
                        request.getRequestURI(),
                        checkout
                )
        );
    }

    /*
     * Owner: payments within their mess.
     * Customer: payments belonging to their own bills.
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable Long paymentId,
            HttpServletRequest request) {

        PaymentResponse payment =
                paymentService.getPayment(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment retrieved successfully.",
                        request.getRequestURI(),
                        payment
                )
        );
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBill(
            @PathVariable Long billId,
            HttpServletRequest request) {

        PaymentResponse payment =
                paymentService.getPaymentByBill(billId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment retrieved successfully.",
                        request.getRequestURI(),
                        payment
                )
        );
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PaymentOverviewResponse>> getPaymentOverview(
            HttpServletRequest request) {

        PaymentOverviewResponse overview =
                paymentService.getPaymentOverview();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment overview retrieved successfully.",
                        request.getRequestURI(),
                        overview
                )
        );
    }
}