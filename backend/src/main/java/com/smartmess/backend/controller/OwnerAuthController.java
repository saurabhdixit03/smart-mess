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

@RestController
@RequestMapping("/api/auth/owner")
@Validated
public class OwnerAuthController {

    private final AuthService authService;

    public OwnerAuthController(AuthService authService) {
        this.authService = authService;
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
    
   
}