package com.smartmess.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.CustomerLoginRequest;
import com.smartmess.backend.dto.request.CustomerRegistrationRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.CustomerLoginResponse;
import com.smartmess.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.smartmess.backend.dto.request.ForgotPasswordRequest;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.service.PasswordResetService;
@RestController
@RequestMapping("/api/auth/customer")
@Validated
public class CustomerAuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public CustomerAuthController(
            AuthService authService,
            PasswordResetService passwordResetService) {

        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerLoginResponse>> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request,
            HttpServletRequest httpRequest) {

        CustomerLoginResponse response =
                authService.registerCustomer(request);

        ApiResponse<CustomerLoginResponse> apiResponse =
                ApiResponse.success(
                        "Customer registered successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<CustomerLoginResponse>> loginCustomer(
            @Valid @RequestBody CustomerLoginRequest request,
            HttpServletRequest httpRequest) {

        CustomerLoginResponse response =
                authService.loginCustomer(request);

        ApiResponse<CustomerLoginResponse> apiResponse =
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
                UserRole.CUSTOMER
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