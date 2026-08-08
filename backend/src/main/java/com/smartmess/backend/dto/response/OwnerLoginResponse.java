package com.smartmess.backend.dto.response;

public record OwnerLoginResponse(

        String accessToken,

        String tokenType,

        Long messOwnerId,

        String fullName,

        String messName

) {
}