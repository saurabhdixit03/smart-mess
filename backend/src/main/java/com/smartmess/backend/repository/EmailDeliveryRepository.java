package com.smartmess.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.EmailDelivery;

import jakarta.persistence.LockModeType;

public interface EmailDeliveryRepository
        extends JpaRepository<EmailDelivery, Long> {

    boolean existsByNotification_NotificationId(
            Long notificationId
    );

    /*
     * Internal worker query.
     *
     * Finds pending emails, retryable failures and interrupted
     * deliveries whose lease has expired.
     */
    @Query("""
            SELECT delivery.emailDeliveryId
            FROM EmailDelivery delivery
            WHERE (
                    delivery.deliveryStatus =
                        com.smartmess.backend.enums.EmailDeliveryStatus.PENDING
                 OR delivery.deliveryStatus =
                        com.smartmess.backend.enums.EmailDeliveryStatus.FAILED
                 OR delivery.deliveryStatus =
                        com.smartmess.backend.enums.EmailDeliveryStatus.SENDING
            )
              AND (
                    delivery.nextAttemptAt IS NULL
                 OR delivery.nextAttemptAt <= :now
              )
              AND (
                    delivery.leaseExpiresAt IS NULL
                 OR delivery.leaseExpiresAt <= :now
              )
            ORDER BY delivery.createdAt ASC,
                     delivery.emailDeliveryId ASC
            """)
    List<Long> findDueDeliveryIds(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /*
     * Must be called inside a write transaction.
     * The worker validates eligibility again after taking the lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT delivery
            FROM EmailDelivery delivery
            WHERE delivery.emailDeliveryId = :emailDeliveryId
            """)
    Optional<EmailDelivery> findByIdForUpdate(
            @Param("emailDeliveryId") Long emailDeliveryId
    );
}