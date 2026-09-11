package com.smartmess.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdatePaymentSettingsRequest;
import com.smartmess.backend.dto.request.UpdateResponseWindowRequest;
import com.smartmess.backend.dto.request.UpdateWeeklyScheduleRequest;
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

    /*
     * Create mess settings.
     * Owner only.
     */
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
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

    /*
     * Get mess settings.
     * Owner + Customer.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
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

    /*
     * Update payment settings.
     * Owner only.
     */
    @PutMapping("/payment")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MessSettingsResponse>> updatePaymentSettings(
            @Valid @RequestBody UpdatePaymentSettingsRequest request,
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.updatePaymentSettings(request);

        ApiResponse<MessSettingsResponse> apiResponse =
                ApiResponse.success(
                        "Payment settings updated successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }

    /*
     * Update customer response window.
     * Owner only.
     */
    @PutMapping("/response-window")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MessSettingsResponse>> updateResponseWindow(
            @Valid @RequestBody UpdateResponseWindowRequest request,
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.updateResponseWindow(request);

        ApiResponse<MessSettingsResponse> apiResponse =
                ApiResponse.success(
                        "Response window updated successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }

    /*
     * Update recurring weekly schedule.
     * Owner only.
     */
    @PutMapping("/weekly-schedule")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MessSettingsResponse>> updateWeeklySchedule(
            @RequestBody UpdateWeeklyScheduleRequest request,
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.updateWeeklySchedule(request);

        ApiResponse<MessSettingsResponse> apiResponse =
                ApiResponse.success(
                        "Weekly schedule updated successfully.",
                        httpRequest.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }
}