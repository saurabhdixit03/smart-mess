package com.smartmess.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.SubmitMealFeedbackRequest;
import com.smartmess.backend.dto.response.PendingMealFeedbackResponse;
import com.smartmess.backend.service.MealFeedbackService;

@RestController
@RequestMapping("/api/meal-feedback")
public class MealFeedbackController {

    private final MealFeedbackService mealFeedbackService;

    public MealFeedbackController(
            MealFeedbackService mealFeedbackService) {
        this.mealFeedbackService = mealFeedbackService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PendingMealFeedbackResponse> getPendingFeedback() {

        PendingMealFeedbackResponse response =
                mealFeedbackService.getPendingFeedback();

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> submitFeedback(
            @RequestBody SubmitMealFeedbackRequest request) {

        mealFeedbackService.submitFeedback(request);

        return ResponseEntity.noContent().build();
    }
}