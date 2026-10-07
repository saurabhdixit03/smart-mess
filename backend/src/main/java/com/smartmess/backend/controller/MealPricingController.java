package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.service.MealPricingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/meal-pricing")
public class MealPricingController {

    private final MealPricingService mealPricingService;

    public MealPricingController(
            MealPricingService mealPricingService) {

        this.mealPricingService = mealPricingService;
    }

    /*
     * Current effective pricing for the authenticated mess.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<MealPricingResponse>> getCurrentPricing(
            HttpServletRequest request) {

        MealPricingResponse pricing =
                mealPricingService.getCurrentPricing();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Meal pricing retrieved successfully.",
                        request.getRequestURI(),
                        pricing
                )
        );
    }

    /*
     * Upcoming price change for the authenticated owner's mess.
     */
    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<MealPricingResponse>>> getScheduledPricing(
            HttpServletRequest request) {

        List<MealPricingResponse> pricing =
                mealPricingService.getScheduledPricing();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Upcoming meal pricing retrieved successfully.",
                        request.getRequestURI(),
                        pricing
                )
        );
    }

    /*
     * Apply prices immediately or save an upcoming price change.
     */
    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MealPricingResponse>> updatePricing(
            @Valid @RequestBody UpdateMealPricingRequest request,
            HttpServletRequest httpRequest) {

        MealPricingResponse pricing =
                mealPricingService.updatePricing(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Meal pricing saved successfully.",
                        httpRequest.getRequestURI(),
                        pricing
                )
        );
    }

    /*
     * Cancel a future price change.
     * Tenant ownership and effective time are checked by the service.
     */
    @DeleteMapping("/scheduled/{mealPricingId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> cancelScheduledPricing(
            @PathVariable Long mealPricingId,
            HttpServletRequest request) {

        mealPricingService.cancelScheduledPricing(mealPricingId);

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "Upcoming meal price change cancelled successfully.",
                        request.getRequestURI(),
                        null
                )
        );
    }
}