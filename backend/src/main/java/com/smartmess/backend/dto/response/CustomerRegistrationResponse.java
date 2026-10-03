package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.CustomerStatus;

public record CustomerRegistrationResponse(

        Long customerId,

        String fullName,

        String mobileNumber,

        CustomerStatus status

) {
}