package com.smartmess.backend.mapper;

import java.util.List;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.response.MealRecordResponse;
import com.smartmess.backend.entity.MealRecord;

@Mapper(componentModel = "spring")
public interface MealRecordMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "menuId", source = "menu.menuId")
    @Mapping(
            target = "mealResponseId",
            source = "mealResponse.mealResponseId"
    )
    
    @Mapping(
            target = "mealSession",
            source = "menu.mealSession"
    )
    
    MealRecordResponse toResponse(MealRecord mealRecord);

    List<MealRecordResponse> toResponseList(
            List<MealRecord> mealRecords
    );

}