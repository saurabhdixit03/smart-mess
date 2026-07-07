package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.entity.MealPricing;

@Mapper(componentModel = "spring")
public interface MealPricingMapper {

    MealPricingResponse toResponse(MealPricing mealPricing);

    void updateMealPricingFromRequest(
            UpdateMealPricingRequest request,
            @MappingTarget MealPricing mealPricing
    );

}