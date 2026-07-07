package com.smartmess.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    public MealPricingController(MealPricingService mealPricingService) {
        this.mealPricingService = mealPricingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MealPricingResponse>> getCurrentPricing(
            HttpServletRequest request) {

        MealPricingResponse response = mealPricingService.getCurrentPricing();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Meal pricing retrieved successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<MealPricingResponse>> updatePricing(
            @Valid @RequestBody UpdateMealPricingRequest request,
            HttpServletRequest httpRequest) {

        MealPricingResponse response = mealPricingService.updatePricing(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Meal pricing updated successfully.",
                                httpRequest.getRequestURI(),
                                response
                        )
                );
    }
}