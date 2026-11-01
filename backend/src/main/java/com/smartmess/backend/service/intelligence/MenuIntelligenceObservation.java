package com.smartmess.backend.service.intelligence;

import java.time.LocalDate;

import com.smartmess.backend.enums.MealSession;

public record MenuIntelligenceObservation(
		
        Long mealFeedbackId,
        
        Long menuId,
        
        LocalDate menuDate,
        
        MealSession mealSession,
        
        Long dishId,
        
        String dishName,
        
        Integer rating,
        
        String comment
) {
}