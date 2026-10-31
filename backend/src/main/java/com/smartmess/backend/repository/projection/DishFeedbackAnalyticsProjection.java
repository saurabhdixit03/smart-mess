package com.smartmess.backend.repository.projection;

public interface DishFeedbackAnalyticsProjection {

    Long getDishId();

    String getDishName();

    Long getTotalComments();
}