package com.smartmess.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.ResetPasswordRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService) {

        this.passwordResetService =
                passwordResetService;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successfully. Please login with your new password.",
                        httpRequest.getRequestURI(),
                        null
                )
        );
    }
}