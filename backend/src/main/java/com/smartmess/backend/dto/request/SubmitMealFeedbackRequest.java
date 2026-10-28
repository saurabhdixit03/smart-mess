package com.smartmess.backend.dto.request;

import java.util.List;

public record SubmitMealFeedbackRequest(

        Long mealRecordId,

        List<DishRating> dishRatings,

        String commonComment

) {

    public record DishRating(

            Long dishId,

            Integer rating

    ) {
    }
}