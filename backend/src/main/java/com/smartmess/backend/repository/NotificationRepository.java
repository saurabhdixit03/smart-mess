package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /*
     * Notification history for one customer within their mess.
     */
    List<Notification> findByMess_MessIdAndCustomerOrderByCreatedAtDesc(
            Long messId,
            Customer customer
    );

    /*
     * Unread notification count within the customer's mess.
     */
    long countByMess_MessIdAndCustomerAndReadFalse(
            Long messId,
            Customer customer
    );

    /*
     * Unread notification history within the customer's mess.
     */
    List<Notification>
            findByMess_MessIdAndCustomerAndReadFalseOrderByCreatedAtDesc(
                    Long messId,
                    Customer customer
            );

    /*
     * Resolves a notification by its tenant and recipient.
     */
    Optional<Notification> findByNotificationIdAndMess_MessIdAndCustomer(
            Long notificationId,
            Long messId,
            Customer customer
    );
}