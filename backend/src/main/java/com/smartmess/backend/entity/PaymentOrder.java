package com.smartmess.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.PaymentMode;
import com.smartmess.backend.enums.PaymentOrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "payment_orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_orders_gateway_order",
                        columnNames = "gateway_order_id"
                ),
                @UniqueConstraint(
                        name = "uk_payment_orders_idempotency",
                        columnNames = "idempotency_key"
                ),
                @UniqueConstraint(
                        name = "uk_payment_orders_active_bill",
                        columnNames = "active_bill_id"
                ),
                @UniqueConstraint(
                        name = "uk_payment_orders_gateway_payment",
                        columnNames = "gateway_payment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_payment_orders_mess_bill_created",
                        columnList = "mess_id, bill_id, created_at"
                ),
                @Index(
                        name = "idx_payment_orders_status_check",
                        columnList = "order_status, next_check_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PaymentOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentOrderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    /*
     * Reserves checkout for this bill.
     * Clear only after a verified outcome permits releasing it.
     */
    @Column(name = "active_bill_id")
    private Long activeBillId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMode provider;

    @Column(nullable = false, length = 10)
    private String environment;

    @Column(
            name = "gateway_order_id",
            nullable = false,
            length = 100
    )
    private String gatewayOrderId;

    @Column(length = 100)
    private String providerOrderId;

    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 36
    )
    private String idempotencyKey;

    /*
     * Original backend-generated request for exact retries.
     * Contains customer details, amount and checkout URLs.
     * Never expose through API responses or logs.
     */
    @Column(columnDefinition = "TEXT")
    private String creationRequest;

    /*
     * Return only to the authorized bill owner; never log.
     */
    @Column(columnDefinition = "TEXT")
    private String paymentSessionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 40)
    private PaymentOrderStatus orderStatus;

    private LocalDateTime expiresAt;

    private LocalDateTime lastCheckedAt;

    @Column(name = "next_check_at")
    private LocalDateTime nextCheckAt;

    @Column(nullable = false)
    private Integer verificationAttemptCount = 0;

    @Column(length = 36)
    private String leaseToken;

    private LocalDateTime leaseExpiresAt;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    private LocalDateTime paidAt;

    @Column(length = 1000)
    private String lastError;

    @Version
    @Column(nullable = false)
    private Long version;
}