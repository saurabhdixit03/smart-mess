package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.SubmitMealResponseRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MealResponseResponse;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MealResponseService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/meal-responses")
public class MealResponseController {

    private final MealResponseService mealResponseService;
    private final CustomerSecurity customerSecurity;

    public MealResponseController(
            MealResponseService mealResponseService,
            CustomerSecurity customerSecurity) {

        this.mealResponseService = mealResponseService;
        this.customerSecurity = customerSecurity;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<MealResponseResponse> submitMealResponse(
            @Valid @RequestBody SubmitMealResponseRequest request,
            HttpServletRequest httpRequest) {

        Long customerId =
                customerSecurity.getCurrentUserId();

        MealResponseResponse response =
                mealResponseService.submitMealResponse(
                        customerId,
                        request);

        return ApiResponse.success(
                "Meal response submitted successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/menu/{menuId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<List<MealResponseResponse>> getResponsesByMenu(
            @PathVariable Long menuId,
            HttpServletRequest httpRequest) {

        List<MealResponseResponse> responses =
                mealResponseService.getResponsesByMenu(menuId);

        return ApiResponse.success(
                "Meal responses retrieved successfully.",
                httpRequest.getRequestURI(),
                responses
        );
    }

    @GetMapping("/customer/{customerId}/menu/{menuId}")
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ApiResponse<MealResponseResponse> getCustomerResponse(
            @PathVariable Long customerId,
            @PathVariable Long menuId,
            HttpServletRequest httpRequest) {

        MealResponseResponse response =
                mealResponseService.getCustomerResponse(
                        customerId,
                        menuId);

        return ApiResponse.success(
                "Meal response retrieved successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }
}