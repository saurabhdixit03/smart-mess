package com.smartmess.backend.repository.projection;

public interface DishFeedbackCommentProjection {

    Long getDishId();

    String getDishName();

    String getComment();
}