package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.MealOption;

public record CollectionQueueResponse(

        Long customerId,

        String customerName,

        Long mealResponseId,

        MealOption mealOption,

        Integer extraRotiCount

) {
}