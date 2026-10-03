package com.smartmess.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MessRegistrationInfoResponse;
import com.smartmess.backend.dto.response.MessRegistrationLinkResponse;
import com.smartmess.backend.service.MessRegistrationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class MessRegistrationController {

    private final MessRegistrationService messRegistrationService;

    public MessRegistrationController(
            MessRegistrationService messRegistrationService) {

        this.messRegistrationService = messRegistrationService;
    }

    /*
     * Owner portal: retrieve the link for sharing and QR rendering.
     */
    @GetMapping("/api/mess/registration-link")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MessRegistrationLinkResponse>>
            getRegistrationLink(HttpServletRequest request) {

        MessRegistrationLinkResponse response =
                messRegistrationService.getRegistrationLink();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Mess registration link retrieved successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }

    /*
     * Customer registration page: identify the mess from its link.
     */
    @GetMapping("/api/public/messes/registration/{registrationCode}")
    public ResponseEntity<ApiResponse<MessRegistrationInfoResponse>>
            getRegistrationInfo(
                    @PathVariable("registrationCode") String registrationCode,
                    HttpServletRequest request) {

        MessRegistrationInfoResponse response =
                messRegistrationService.getRegistrationInfo(
                        registrationCode
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Mess registration information retrieved successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }
}