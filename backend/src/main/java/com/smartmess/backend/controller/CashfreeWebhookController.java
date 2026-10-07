package com.smartmess.backend.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.service.PaymentOrderService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/webhooks/cashfree")
public class CashfreeWebhookController {

    private static final int MAX_BODY_BYTES = 1_048_576;

    private final PaymentOrderService paymentOrderService;

    private static final Logger log =
            LoggerFactory.getLogger(CashfreeWebhookController.class);

    public CashfreeWebhookController(
            PaymentOrderService paymentOrderService) {

        this.paymentOrderService = paymentOrderService;
    }

    /*
     * Authenticated by gateway signature, not customer JWT.
     * Security configuration will permit only this POST endpoint.
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(
                    name = "x-webhook-timestamp",
                    required = false
            )
            String timestamp,

            @RequestHeader(
                    name = "x-webhook-signature",
                    required = false
            )
            String signature,

            HttpServletRequest request) throws IOException {

        if (request.getContentLengthLong() > MAX_BODY_BYTES) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .build();
        }

        byte[] rawBody =
                request.getInputStream()
                        .readNBytes(MAX_BODY_BYTES + 1);

        if (rawBody.length > MAX_BODY_BYTES) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .build();
        }

        paymentOrderService.handleWebhook(
                rawBody,
                timestamp,
                signature
        );

        log.info("Cashfree webhook handler completed; returning HTTP 200.");

        return ResponseEntity.ok().build();
    }
}