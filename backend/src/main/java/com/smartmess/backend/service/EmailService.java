package com.smartmess.backend.service;

import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.enums.UserRole;

public interface EmailService {

    void sendPasswordResetEmail(
            String recipientEmail,
            String resetToken,
            UserRole userRole
    );

    void sendCustomerApprovalEmail(
            String recipientEmail,
            String customerName,
            String messName
    );

    /*
     * Sends a customer notification using the shared email template.
     * Durable queue processing will call this method.
     */
    void sendCustomerNotificationEmail(
            String recipientEmail,
            String customerName,
            String messName,
            NotificationType notificationType,
            String title,
            String message
    );
}