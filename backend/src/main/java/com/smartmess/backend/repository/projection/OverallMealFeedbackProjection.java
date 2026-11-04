package com.smartmess.backend.repository.projection;

import java.time.LocalDate;

import com.smartmess.backend.enums.MealSession;

public interface OverallMealFeedbackProjection {

    Long getMenuId();

    LocalDate getMenuDate();

    MealSession getMealSession();

    String getComment();
}
