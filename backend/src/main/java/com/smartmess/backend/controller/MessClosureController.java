package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreateMessClosureRequest;
import com.smartmess.backend.dto.request.UpdateMessClosureRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MessClosureResponse;
import com.smartmess.backend.service.MessClosureService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mess-closures")
@Validated
public class MessClosureController {

    private final MessClosureService messClosureService;

    public MessClosureController(
            MessClosureService messClosureService) {

        this.messClosureService = messClosureService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<MessClosureResponse> createClosure(
            @Valid @RequestBody CreateMessClosureRequest request,
            HttpServletRequest httpRequest) {

        MessClosureResponse response =
                messClosureService.createClosure(request);

        return ApiResponse.success(
                "Mess closure created successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @PutMapping("/{closureId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<MessClosureResponse> updateClosure(
            @PathVariable Long closureId,
            @Valid @RequestBody UpdateMessClosureRequest request,
            HttpServletRequest httpRequest) {

        MessClosureResponse response =
                messClosureService.updateClosure(
                        closureId,
                        request
                );

        return ApiResponse.success(
                "Mess closure updated successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @DeleteMapping("/{closureId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<Void> deleteClosure(
            @PathVariable Long closureId,
            HttpServletRequest httpRequest) {

        messClosureService.deleteClosure(
                closureId);

        return ApiResponse.success(
                "Mess closure deleted successfully.",
                httpRequest.getRequestURI(),
                null
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ApiResponse<List<MessClosureResponse>>
            getCurrentAndUpcomingClosures(
                    HttpServletRequest httpRequest) {

        List<MessClosureResponse> response =
                messClosureService
                        .getCurrentAndUpcomingClosures();

        return ApiResponse.success(
                "Current and upcoming mess closures fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<List<MessClosureResponse>>
            getClosureHistory(
                    HttpServletRequest httpRequest) {

        List<MessClosureResponse> response =
                messClosureService
                        .getClosureHistory();

        return ApiResponse.success(
                "Mess closure history fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }
}