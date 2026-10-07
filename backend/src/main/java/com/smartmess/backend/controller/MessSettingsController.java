package com.smartmess.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<MessSettingsResponse>> getSettings(
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.getSettings();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Mess settings fetched successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    @PutMapping("/response-window")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MessSettingsResponse>> updateResponseWindow(
            @Valid @RequestBody UpdateResponseWindowRequest request,
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.updateResponseWindow(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Response window updated successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    @PutMapping("/weekly-schedule")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MessSettingsResponse>> updateWeeklySchedule(
            @RequestBody UpdateWeeklyScheduleRequest request,
            HttpServletRequest httpRequest) {

        MessSettingsResponse response =
                messSettingsService.updateWeeklySchedule(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Weekly schedule updated successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }
}