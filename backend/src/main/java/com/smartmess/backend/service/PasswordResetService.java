package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.ForgotPasswordRequest;
import com.smartmess.backend.dto.request.ResetPasswordRequest;
import com.smartmess.backend.enums.UserRole;

public interface PasswordResetService {

    void requestPasswordReset(
            ForgotPasswordRequest request,
            UserRole userRole
    );

    void resetPassword(
            ResetPasswordRequest request
    );
}