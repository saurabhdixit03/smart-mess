package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.SubmitMealResponseRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MealResponseResponse;
import com.smartmess.backend.service.MealResponseService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/meal-responses")
public class MealResponseController {

    private final MealResponseService mealResponseService;

    public MealResponseController(MealResponseService mealResponseService) {
        this.mealResponseService = mealResponseService;
    }

    @PostMapping
    public ApiResponse<MealResponseResponse> submitMealResponse(
            @Valid @RequestBody SubmitMealResponseRequest request,
            HttpServletRequest httpRequest) {

        MealResponseResponse response =
                mealResponseService.submitMealResponse(request);

        return ApiResponse.success(
                "Meal response submitted successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/menu/{menuId}")
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

}