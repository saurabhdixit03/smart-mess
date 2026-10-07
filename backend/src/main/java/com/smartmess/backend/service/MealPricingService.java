package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;

public interface MealPricingService {

    MealPricingResponse getCurrentPricing();

    MealPricingResponse updatePricing(
            UpdateMealPricingRequest request
    );

    /*
     * Returns the upcoming price change, if configured.
     */
    List<MealPricingResponse> getScheduledPricing();

    /*
     * Cancels a future price change within the owner's mess.
     * Effective pricing and historical prices cannot be deleted.
     */
    void cancelScheduledPricing(Long mealPricingId);
}