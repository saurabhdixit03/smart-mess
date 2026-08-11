package com.smartmess.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreateMealRecordRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MealRecordResponse;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.service.MealRecordService;

import jakarta.validation.Valid;

import jakarta.servlet.http.HttpServletRequest;

import com.smartmess.backend.dto.response.CollectionQueueResponse;

@RestController
@RequestMapping("/api/meal-records")
public class MealRecordController {

    private final MealRecordService mealRecordService;

    public MealRecordController(
            MealRecordService mealRecordService) {

        this.mealRecordService = mealRecordService;
    }
    
    /*
     * OWNER ONLY
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
                .body(ApiResponse.success(
                        "Meal record created successfully.",
                        httpRequest.getRequestURI(),
                        response));
    }
    
    // for meal records  
   
    /*
     * OWNER ONLY
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
                        response));
    }
    
    /*
     * OWNER + CUSTOMER
     *
     * OWNER:
     * Can view any customer's meal history.
     *
     * CUSTOMER:
     * Must only be allowed to view their own meal history.
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
                        response));
    }
    
    /*
     * OWNER ONLY
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
                        response));
    }

}