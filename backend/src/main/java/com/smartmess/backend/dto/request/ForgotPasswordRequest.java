package com.smartmess.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        @Size(
                max = 100,
                message = "Email cannot exceed 100 characters"
        )
        String email

) {
}