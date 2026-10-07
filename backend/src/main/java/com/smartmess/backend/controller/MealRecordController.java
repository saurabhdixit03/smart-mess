package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.CreateMealRecordRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.CollectionCustomerResponse;
import com.smartmess.backend.dto.response.CollectionQueueResponse;
import com.smartmess.backend.dto.response.MealRecordResponse;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.service.MealRecordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/meal-records")
public class MealRecordController {

    private final MealRecordService mealRecordService;

    public MealRecordController(
            MealRecordService mealRecordService) {

        this.mealRecordService = mealRecordService;
    }

    /*
     * Record collection with or without a submitted response.
     */
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MealRecordResponse>> createMealRecord(
            @Valid @RequestBody CreateMealRecordRequest request,
            HttpServletRequest httpRequest) {

        MealRecordResponse response =
                mealRecordService.createMealRecord(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Meal record created successfully.",
                                httpRequest.getRequestURI(),
                                response
                        )
                );
    }

    /*
     * Accepted responses awaiting collection.
     */
    @GetMapping("/collection-queue")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<CollectionQueueResponse>>> getCollectionQueue(
            @RequestParam MealSession mealSession,
            HttpServletRequest httpRequest) {

        List<CollectionQueueResponse> response =
                mealRecordService.getCollectionQueue(mealSession);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Collection queue retrieved successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    /*
     * Search active customers within the owner's mess.
     * Includes response details and today's collection status.
     */
    @GetMapping("/collection-customers")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<CollectionCustomerResponse>>> searchCollectionCustomers(
            @RequestParam MealSession mealSession,
            @RequestParam String search,
            HttpServletRequest httpRequest) {

        List<CollectionCustomerResponse> response =
                mealRecordService.searchCollectionCustomers(
                        mealSession,
                        search
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Collection customers retrieved successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    /*
     * Owners can view customer history within their mess.
     * Customers can view only their own history.
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<MealRecordResponse>>> getCustomerMealHistory(
            @PathVariable Long customerId,
            HttpServletRequest httpRequest) {

        List<MealRecordResponse> response =
                mealRecordService.getCustomerMealHistory(customerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer meal history fetched successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    /*
     * Collections for today's selected meal session.
     */
    @GetMapping("/today")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<MealRecordResponse>>> getTodayMealRecords(
            @RequestParam MealSession mealSession,
            HttpServletRequest httpRequest) {

        List<MealRecordResponse> response =
                mealRecordService.getTodayMealRecords(mealSession);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Today's meal records fetched successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }
}