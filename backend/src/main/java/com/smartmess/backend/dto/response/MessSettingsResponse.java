package com.smartmess.backend.dto.response;

import java.time.LocalDateTime;

public record MessSettingsResponse(

        Long settingsId,

        String upiId,

        String receiverName,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}