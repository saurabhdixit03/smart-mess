package com.smartmess.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.SubmitMealResponseRequest;
import com.smartmess.backend.dto.response.MealResponseResponse;
import com.smartmess.backend.entity.MealResponse;

@Mapper(componentModel = "spring")
public interface MealResponseMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "mobileNumber", source = "customer.mobileNumber")
    @Mapping(target = "menuId", source = "menu.menuId")
    MealResponseResponse toResponse(MealResponse mealResponse);

    /*
     * Updates response choices only.
     *
     * The service manages tenant ownership, customer/menu
     * relationships and respondedAt.
     * BaseEntity auditing manages createdAt and updatedAt.
     */
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "menu", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "mealResponseId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    void updateMealResponseFromRequest(
            SubmitMealResponseRequest request,
            @MappingTarget MealResponse mealResponse
    );

    List<MealResponseResponse> toResponseList(
            List<MealResponse> mealResponses
    );
}