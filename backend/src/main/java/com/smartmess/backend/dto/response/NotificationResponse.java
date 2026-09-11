package com.smartmess.backend.dto.response;

import java.time.LocalDateTime;

import com.smartmess.backend.enums.NotificationType;

public record NotificationResponse(

        Long notificationId,

        Long customerId,

        NotificationType notificationType,

        String title,

        String message,

        boolean read,

        LocalDateTime createdAt

) {
}