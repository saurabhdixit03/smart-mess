package com.smartmess.backend.dto.response;

public record MessRegistrationLinkResponse(

        Long messId,

        String messName,

        String registrationCode,

        String registrationUrl

) {
}