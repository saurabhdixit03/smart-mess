package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.entity.MealPricing;

@Mapper(componentModel = "spring")
public interface MealPricingMapper {

    @Mapping(target = "effectiveFrom", source = "effectiveFrom")
    MealPricingResponse toResponse(
            MealPricing mealPricing
    );

    /*
     * Updates meal prices only.
     *
     * Tenant ownership, identity, effective time
     * and auditing fields are managed separately.
     */
    @Mapping(target = "mealPricingId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "effectiveFrom", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMealPricingFromRequest(
            UpdateMealPricingRequest request,
            @MappingTarget MealPricing mealPricing
    );
}