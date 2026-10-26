package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.DishCategory;

public record DishResponse(

        Long dishId,

        String name,

        DishCategory category

) {
}