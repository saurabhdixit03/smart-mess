package com.smartmess.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdateMessSettingsRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.service.MessSettingsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
public class MessSettingsController {

    private final MessSettingsService messSettingsService;

    public MessSettingsController(
            MessSettingsService messSettingsService) {

        this.messSettingsService = messSettingsService;

    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessSettingsResponse>> createSettings(
            @Valid @RequestBody CreateMessSettingsRequest request,
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.createSettings(request);

        ApiResponse<MessSettingsResponse> apiResponse =
                ApiResponse.success(
                        "Mess settings created successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);

    }

    @GetMapping
    public ResponseEntity<ApiResponse<MessSettingsResponse>> getSettings(
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.getSettings();

        ApiResponse<MessSettingsResponse> apiResponse =
                ApiResponse.success(
                        "Mess settings fetched successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);

    }

    @PutMapping
    public ResponseEntity<ApiResponse<MessSettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateMessSettingsRequest request,
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.updateSettings(request);

        ApiResponse<MessSettingsResponse> apiResponse =
                ApiResponse.success(
                        "Mess settings updated successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);

    }

}