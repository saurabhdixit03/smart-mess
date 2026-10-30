package com.smartmess.backend.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.smartmess.backend.enums.MealSession;

public record PendingMealFeedbackResponse(

        Long mealRecordId,

        LocalDate mealDate,

        MealSession mealSession,

        List<DishFeedbackItem> dishes

) {

    public record DishFeedbackItem(

            Long dishId,

            String dishName

    ) {
    }
}