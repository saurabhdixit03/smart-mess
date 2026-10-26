package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;

import com.smartmess.backend.dto.response.DishResponse;
import com.smartmess.backend.entity.Dish;

@Mapper(componentModel = "spring")
public interface DishMapper {

    DishResponse toResponse(Dish dish);
}