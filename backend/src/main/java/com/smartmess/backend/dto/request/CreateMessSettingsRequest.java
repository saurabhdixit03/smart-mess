package com.smartmess.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateMessSettingsRequest(

        @NotBlank(message = "UPI ID is required.")
        String upiId,

        @NotBlank(message = "Receiver name is required.")
        String receiverName

) {
}