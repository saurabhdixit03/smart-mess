package com.smartmess.backend.entity;

import java.time.LocalDateTime;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.EmailDeliveryStatus;
import com.smartmess.backend.enums.NotificationType;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "email_deliveries",
        indexes = {
                @Index(
                        name = "idx_email_delivery_status_retry",
                        columnList = "delivery_status, next_attempt_at"
                ),
                @Index(
                        name = "idx_email_delivery_mess_created",
                        columnList = "mess_id, created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EmailDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailDeliveryId;

    /*
     * The delivery, notification and customer
     * must belong to the same mess.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "notification_id",
            nullable = false,
            unique = true
    )
    private Notification notification;

    /*
     * Snapshot the email content when the notification is created.
     * Later profile or notification changes do not rewrite this email.
     */
    @Column(nullable = false, length = 320)
    private String recipientEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String customerName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "delivery_status",
            nullable = false,
            length = 20
    )
    private EmailDeliveryStatus deliveryStatus =
            EmailDeliveryStatus.PENDING;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(length = 36)
    private String leaseToken;

    private LocalDateTime leaseExpiresAt;

    private LocalDateTime lastAttemptAt;

    /*
     * Brevo accepted the email at this time.
     * This does not confirm inbox delivery.
     */
    private LocalDateTime sentAt;

    @Column(length = 1000)
    private String lastError;

    @Version
    @Column(nullable = false)
    private Long version;
}