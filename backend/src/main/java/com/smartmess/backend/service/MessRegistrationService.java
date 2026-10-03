package com.smartmess.backend.service;

import com.smartmess.backend.dto.response.MessRegistrationInfoResponse;
import com.smartmess.backend.dto.response.MessRegistrationLinkResponse;

public interface MessRegistrationService {

    MessRegistrationLinkResponse getRegistrationLink();

    MessRegistrationInfoResponse getRegistrationInfo(
            String registrationCode
    );
}