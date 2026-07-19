package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;

public interface MealPricingService {

    MealPricingResponse getCurrentPricing();

    MealPricingResponse updatePricing(
            UpdateMealPricingRequest request
    );

}