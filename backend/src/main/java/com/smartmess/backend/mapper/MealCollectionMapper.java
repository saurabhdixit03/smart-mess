package com.smartmess.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.response.CollectionQueueResponse;
import com.smartmess.backend.entity.MealResponse;

@Mapper(componentModel = "spring")
public interface MealCollectionMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "mealResponseId", source = "mealResponseId")
    CollectionQueueResponse toResponse(
            MealResponse mealResponse);

    List<CollectionQueueResponse> toResponseList(
            List<MealResponse> mealResponses);

}