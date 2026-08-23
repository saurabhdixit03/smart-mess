package com.smartmess.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.OwnerLoginRequest;
import com.smartmess.backend.dto.request.OwnerRegistrationRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.OwnerLoginResponse;
import com.smartmess.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.smartmess.backend.dto.request.ForgotPasswordRequest;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.service.PasswordResetService;
@RestController
@RequestMapping("/api/auth/owner")
@Validated
public class OwnerAuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public OwnerAuthController(
            AuthService authService,
            PasswordResetService passwordResetService) {

        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OwnerLoginResponse>> registerOwner(
            @Valid @RequestBody OwnerRegistrationRequest request,
            HttpServletRequest httpRequest) {

        OwnerLoginResponse response =
                authService.registerOwner(request);

        ApiResponse<OwnerLoginResponse> apiResponse =
                ApiResponse.success(
                        "Mess owner registered successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<OwnerLoginResponse>> loginOwner(
            @Valid @RequestBody OwnerLoginRequest request,
            HttpServletRequest httpRequest) {

        OwnerLoginResponse response =
                authService.loginOwner(request);

        ApiResponse<OwnerLoginResponse> apiResponse =
                ApiResponse.success(
                        "Login successful.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        passwordResetService.requestPasswordReset(
                request,
                UserRole.OWNER
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "If an account exists with this mobile number, a password reset link has been sent to the registered email address.",
                        httpRequest.getRequestURI(),
                        null
                )
        );
    }
   
}