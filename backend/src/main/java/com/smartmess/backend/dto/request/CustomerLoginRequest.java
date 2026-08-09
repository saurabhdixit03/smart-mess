package com.smartmess.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerLoginRequest(

        @NotBlank(message = "Mobile number is required")
        @Pattern(
                regexp = "^[6-9]\\d{9}$",
                message = "Mobile number must be a valid 10-digit Indian mobile number"
        )
        String mobileNumber,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must be between 8 and 100 characters"
        )
        String password

) {
}