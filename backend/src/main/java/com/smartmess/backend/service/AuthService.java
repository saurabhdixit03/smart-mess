package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.OwnerLoginRequest;
import com.smartmess.backend.dto.request.OwnerRegistrationRequest;
import com.smartmess.backend.dto.response.OwnerLoginResponse;

public interface AuthService {

    OwnerLoginResponse registerOwner(
            OwnerRegistrationRequest request
    );

    OwnerLoginResponse loginOwner(
            OwnerLoginRequest request
    );

}