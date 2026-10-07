package com.smartmess.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.cashfree")
@Getter
@Setter
public class CashfreeProperties {

    public enum Environment {
        SANDBOX,
        PRODUCTION
    }

    private boolean enabled = false;

    private Environment environment = Environment.SANDBOX;

    private String clientId;

    private String clientSecret;

    private String apiVersion = "2026-01-01";

    /*
     * Frontend destination after checkout.
     * Returning here does not prove that payment succeeded.
     */
    private String returnUrl;

    /*
     * Public backend endpoint for signed gateway webhooks.
     */
    private String notifyUrl;

    private int connectTimeoutMillis = 5000;

    private int readTimeoutMillis = 10000;

    private int orderExpiryMinutes = 30;

    public String getBaseUrl() {

        if (environment == Environment.PRODUCTION) {
            return "https://api.cashfree.com/pg";
        }

        return "https://sandbox.cashfree.com/pg";
    }

    public String getCheckoutMode() {

        return environment == Environment.PRODUCTION
                ? "production"
                : "sandbox";
    }
}