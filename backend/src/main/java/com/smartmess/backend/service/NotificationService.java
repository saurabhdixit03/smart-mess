package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.response.NotificationResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.NotificationType;

public interface NotificationService {

    void notifyActiveCustomers(
            NotificationType notificationType,
            String title,
            String message
    );

    /*
     * Notify a customer within the authenticated mess.
     */
    void notifyCustomer(
            Customer customer,
            NotificationType notificationType,
            String title,
            String message
    );

    /*
     * Internal notification validated against a running billing job.
     * Not exposed through a controller.
     */
    void notifyAutomatedBill(
            Long jobId,
            String leaseToken,
            Long billId
    );

    /*
     * Internal notification for a verified, settled payment order.
     * Implementation must validate its payment, bill and tenant.
     * Must join the settlement transaction.
     * Not exposed through a controller.
     */
    void notifyVerifiedPayment(
            Long paymentOrderId
    );

    List<NotificationResponse> getMyNotifications();

    List<NotificationResponse> getMyUnreadNotifications();

    long getMyUnreadCount();

    NotificationResponse markAsRead(
            Long notificationId
    );

    void markAllAsRead();
}