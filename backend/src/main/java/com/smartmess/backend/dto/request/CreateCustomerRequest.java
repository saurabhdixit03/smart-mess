package com.smartmess.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName,

        @NotBlank(message = "Mobile number is required")
        @Pattern(
                regexp = "^[6-9]\\d{9}$",
                message = "Mobile number must be a valid 10-digit Indian mobile number"
        )
        String mobileNumber,

        @Email(message = "Invalid email address")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,

        @Size(max = 500, message = "Remarks cannot exceed 500 characters")
        String remarks

) {
}