package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.entity.MealPricing;

@Mapper(componentModel = "spring")
public interface MealPricingMapper {

    MealPricingResponse toResponse(
            MealPricing mealPricing
    );

    /*
     * Updates meal prices only.
     *
     * Tenant ownership, record identity and auditing
     * fields remain protected.
     */
    @Mapping(target = "mealPricingId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMealPricingFromRequest(
            UpdateMealPricingRequest request,
            @MappingTarget MealPricing mealPricing
    );
}