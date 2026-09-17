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
     * Notify a specific customer.
     */
    void notifyCustomer(
            Customer customer,
            NotificationType notificationType,
            String title,
            String message
    );

    List<NotificationResponse> getMyNotifications();

    List<NotificationResponse> getMyUnreadNotifications();

    long getMyUnreadCount();

    NotificationResponse markAsRead(
            Long notificationId
    );

    void markAllAsRead();
}