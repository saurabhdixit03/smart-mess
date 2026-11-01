package com.smartmess.backend.repository.projection;

public interface DishRatingAnalyticsProjection {

    Long getDishId();

    String getDishName();

    Long getTotalRatings();

    Double getAverageRating();

    Long getOneStarCount();

    Long getTwoStarCount();

    Long getThreeStarCount();

    Long getFourStarCount();

    Long getFiveStarCount();
}