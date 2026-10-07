package com.smartmess.backend.service;

import com.smartmess.backend.entity.Notification;

public interface EmailDeliveryService {

    /*
     * Queues a supported notification email.
     *
     * Must join the transaction saving the notification.
     * The implementation validates tenant ownership.
     */
    void queueNotificationEmail(
            Notification notification
    );

    /*
     * Processes pending emails and retries failed deliveries.
     *
     * Internal worker operation; requires no owner session.
     */
    void processDueEmails();
}