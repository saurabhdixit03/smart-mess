
package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.SuggestionPriority;

public record DishIntelligenceResponse(

        Long dishId,

        String dishName,

        boolean active,

        Long totalRatings,

        Double averageRating,

        Long oneStarCount,

        Long twoStarCount,

        Long threeStarCount,

        Long fourStarCount,

        Long fiveStarCount,

        Long totalComments,

        AiSuggestion aiSuggestion

) {

    public record AiSuggestion(

            String observation,

            String recommendation,

            SuggestionPriority priority

    ) {
    }
}
