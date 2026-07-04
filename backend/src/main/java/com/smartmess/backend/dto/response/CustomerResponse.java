package com.smartmess.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.CustomerStatus;

public record CustomerResponse(

        Long customerId,

        String fullName,

        String mobileNumber,

        String email,

        String remarks,

        LocalDate joiningDate,

        CustomerStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}