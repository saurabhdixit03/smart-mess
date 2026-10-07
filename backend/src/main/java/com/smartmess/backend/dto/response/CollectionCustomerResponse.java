package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;

public record CollectionCustomerResponse(

        Long customerId,

        String customerName,

        String mobileNumber,

        Long menuId,

        Long mealResponseId,

        MealResponseStatus responseStatus,

        MealOption mealOption,

        Integer extraRotiCount,

        boolean collected

) {
}