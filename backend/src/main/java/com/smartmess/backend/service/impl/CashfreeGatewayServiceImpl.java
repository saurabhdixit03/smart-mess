package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartmess.backend.config.CashfreeProperties;
import com.smartmess.backend.service.CashfreeGatewayService;

@Service
public class CashfreeGatewayServiceImpl
        implements CashfreeGatewayService {

    private final CashfreeProperties properties;

    public CashfreeGatewayServiceImpl(
            CashfreeProperties properties) {

        this.properties = properties;
    }

    @Override
    public GatewayOrder createOrder(
            CreateOrderCommand command) {

        if (command == null) {
            throw configurationError(
                    "Payment order details are required."
            );
        }

        requireConfiguration(command.environment());
        validateOrderId(command.gatewayOrderId());

        try {
            UUID.fromString(command.idempotencyKey());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw configurationError(
                    "Payment idempotency key is invalid."
            );
        }

        if (command.amount() == null
                || command.amount().compareTo(BigDecimal.ONE) < 0
                || command.amount().compareTo(
                        new BigDecimal("99999999.99")
                ) > 0
                || command.amount().stripTrailingZeros().scale() > 2
                || !"INR".equals(command.currency())) {

            throw configurationError(
                    "Gateway order requires a valid INR amount of at least ₹1."
            );
        }

        if (isBlank(command.customerReference())
                || isBlank(command.customerPhone())
                || command.expiresAt() == null
                || isBlank(command.returnUrl())
                || isBlank(command.notifyUrl())) {

            throw configurationError(
                    "Payment customer details or checkout configuration are missing."
            );
        }

        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customer_id", command.customerReference());
        customer.put("customer_phone", command.customerPhone());

        if (!isBlank(command.customerName())) {
            customer.put("customer_name", command.customerName());
        }

        if (!isBlank(command.customerEmail())) {
            customer.put("customer_email", command.customerEmail());
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("return_url", command.returnUrl());
        metadata.put("notify_url", command.notifyUrl());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("order_id", command.gatewayOrderId());
        body.put("order_amount", command.amount());
        body.put("order_currency", command.currency());
        body.put("customer_details", customer);
        body.put("order_expiry_time", command.expiresAt().toString());
        body.put("order_meta", metadata);

        JsonNode response;

        try {
            response = buildClient()
                    .post()
                    .uri("/orders")
                    .header(
                            "x-idempotency-key",
                            command.idempotencyKey()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException exception) {
            throw translateHttpFailure(exception, true);

        } catch (RestClientException exception) {
            throw uncertainError();
        }

        return parseOrder(response);
    }

    @Override
    public GatewayOrder getOrder(
            String gatewayOrderId,
            String environment) {

        requireConfiguration(environment);
        validateOrderId(gatewayOrderId);

        JsonNode response;

        try {
            response = buildClient()
                    .get()
                    .uri("/orders/{orderId}", gatewayOrderId)
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException exception) {
            throw translateHttpFailure(exception, false);

        } catch (RestClientException exception) {
            throw uncertainError();
        }

        return parseOrder(response);
    }

    @Override
    public List<GatewayPayment> getOrderPayments(
            String gatewayOrderId,
            String environment) {

        requireConfiguration(environment);
        validateOrderId(gatewayOrderId);

        JsonNode response;

        try {
            response = buildClient()
                    .get()
                    .uri(
                            "/orders/{orderId}/payments",
                            gatewayOrderId
                    )
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException exception) {
            throw translateHttpFailure(exception, false);

        } catch (RestClientException exception) {
            throw uncertainError();
        }

        if (response == null || !response.isArray()) {
            throw uncertainError();
        }

        List<GatewayPayment> payments = new ArrayList<>();

        try {
            for (JsonNode payment : response) {

                OffsetDateTime completedAt =
                        optionalDate(
                                payment,
                                "payment_completion_time"
                        );

                if (completedAt == null) {
                    completedAt = optionalDate(
                            payment,
                            "payment_time"
                    );
                }

                payments.add(new GatewayPayment(
                        requiredText(payment, "order_id"),
                        requiredText(payment, "cf_payment_id"),
                        requiredText(payment, "payment_status"),
                        requiredAmount(payment, "payment_amount"),
                        requiredText(payment, "payment_currency"),
                        payment.path("is_captured").asBoolean(false),
                        completedAt
                ));
            }

        } catch (IllegalArgumentException exception) {
            throw uncertainError();
        }

        return List.copyOf(payments);
    }

    @Override
    public boolean verifyWebhookSignature(
            byte[] rawBody,
            String timestamp,
            String signature) {

        if (!properties.isEnabled()
                || isBlank(properties.getClientSecret())
                || rawBody == null
                || isBlank(timestamp)
                || isBlank(signature)
                || timestamp.length() > 30
                || signature.length() > 100) {

            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    properties.getClientSecret()
                            .getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            mac.update(timestamp.getBytes(StandardCharsets.UTF_8));

            byte[] expected =
                    mac.doFinal(rawBody);

            byte[] received =
                    Base64.getDecoder().decode(signature);

            return MessageDigest.isEqual(expected, received);

        } catch (IllegalArgumentException exception) {
            return false;

        } catch (GeneralSecurityException exception) {
            throw configurationError(
                    "Webhook signature verification is unavailable."
            );
        }
    }

    private RestClient buildClient() {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                properties.getConnectTimeoutMillis()
        );

        requestFactory.setReadTimeout(
                properties.getReadTimeoutMillis()
        );

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(
                        "x-client-id",
                        properties.getClientId()
                )
                .defaultHeader(
                        "x-client-secret",
                        properties.getClientSecret()
                )
                .defaultHeader(
                        "x-api-version",
                        properties.getApiVersion()
                )
                .defaultHeader(
                        "Accept",
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    private void requireConfiguration(String environment) {

        if (!properties.isEnabled()) {
            throw configurationError(
                    "Online payments are not enabled."
            );
        }

        if (properties.getEnvironment() == null
                || !properties.getEnvironment().name()
                        .equals(environment)) {

            throw configurationError(
                    "The payment order environment does not match gateway configuration."
            );
        }

        if (isBlank(properties.getClientId())
                || isBlank(properties.getClientSecret())
                || isBlank(properties.getApiVersion())
                || properties.getConnectTimeoutMillis() <= 0
                || properties.getReadTimeoutMillis() <= 0) {

            throw configurationError(
                    "Online payment configuration is incomplete."
            );
        }
    }

    private void validateOrderId(String orderId) {

        if (orderId == null
                || !orderId.matches("[A-Za-z0-9_-]{3,45}")) {

            throw configurationError(
                    "Payment order reference is invalid."
            );
        }
    }

    private GatewayOrder parseOrder(JsonNode response) {

        if (response == null || !response.isObject()) {
            throw uncertainError();
        }

        try {
            return new GatewayOrder(
                    requiredText(response, "order_id"),
                    requiredText(response, "cf_order_id"),
                    optionalText(response, "payment_session_id"),
                    requiredText(response, "order_status"),
                    requiredAmount(response, "order_amount"),
                    requiredText(response, "order_currency"),
                    optionalDate(response, "order_expiry_time")
            );

        } catch (IllegalArgumentException exception) {
            throw uncertainError();
        }
    }

    private String requiredText(
            JsonNode node,
            String field) {

        String value = optionalText(node, field);

        if (isBlank(value)) {
            throw new IllegalArgumentException(
                    "Required gateway field is missing."
            );
        }

        return value;
    }

    private String optionalText(
            JsonNode node,
            String field) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        if (!value.isValueNode()) {
            throw new IllegalArgumentException(
                    "Invalid gateway field."
            );
        }

        return value.asText();
    }

    private BigDecimal requiredAmount(
            JsonNode node,
            String field) {

        return new BigDecimal(requiredText(node, field));
    }

    private OffsetDateTime optionalDate(
            JsonNode node,
            String field) {

        String value = optionalText(node, field);

        if (isBlank(value)) {
            return null;
        }

        return OffsetDateTime.parse(value);
    }

    private GatewayException translateHttpFailure(
            RestClientResponseException exception,
            boolean creating) {

        int status = exception.getStatusCode().value();

        if (status == 401 || status == 403) {
            return configurationError(
                    "Gateway authentication failed. Check backend payment configuration."
            );
        }

        if (!creating && status == 404) {
            return new GatewayException(
                    FailureKind.NOT_FOUND,
                    "Gateway order was not found."
            );
        }

        /*
         * Conflicts, idempotency errors, throttling and server errors
         * require reconciliation rather than a new order.
         */
        if (creating && status == 400) {
            return new GatewayException(
                    FailureKind.REJECTED,
                    "Gateway rejected the payment order details."
            );
        }

        return uncertainError();
    }

    private GatewayException configurationError(String message) {

        return new GatewayException(
                FailureKind.CONFIGURATION,
                message
        );
    }

    private GatewayException uncertainError() {

        return new GatewayException(
                FailureKind.UNCERTAIN,
                "Gateway outcome could not be confirmed. Payment verification will be retried."
        );
    }

    private boolean isBlank(String value) {

        return value == null || value.isBlank();
    }
}