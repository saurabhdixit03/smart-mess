package com.smartmess.backend.dto.response;

public record CustomerLoginResponse(

        String accessToken,

        String tokenType,

        Long customerId,

        String fullName,

        String mobileNumber

) {
}