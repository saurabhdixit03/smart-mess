package com.smartmess.backend.repository.projection;

import java.time.LocalDate;

import com.smartmess.backend.enums.MealSession;

public interface DishFeedbackObservationProjection {

	Long getMealFeedbackId();
	
	Long getMessId();

    Long getMenuId();

    LocalDate getMenuDate();

    MealSession getMealSession();

    Long getDishId();

    String getDishName();

    Integer getRating();

    String getComment();
}