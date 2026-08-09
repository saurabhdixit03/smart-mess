package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.CustomerLoginRequest;
import com.smartmess.backend.dto.request.CustomerRegistrationRequest;
import com.smartmess.backend.dto.request.OwnerLoginRequest;
import com.smartmess.backend.dto.request.OwnerRegistrationRequest;
import com.smartmess.backend.dto.response.CustomerLoginResponse;
import com.smartmess.backend.dto.response.OwnerLoginResponse;

public interface AuthService {

    // =========================
    // Mess Owner Authentication
    // =========================

    OwnerLoginResponse registerOwner(
            OwnerRegistrationRequest request
    );

    OwnerLoginResponse loginOwner(
            OwnerLoginRequest request
    );

    // =========================
    // Customer Authentication
    // =========================

    CustomerLoginResponse registerCustomer(
            CustomerRegistrationRequest request
    );

    CustomerLoginResponse loginCustomer(
            CustomerLoginRequest request
    );
}