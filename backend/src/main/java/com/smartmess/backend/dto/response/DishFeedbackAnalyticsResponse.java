package com.smartmess.backend.dto.response;

public record DishFeedbackAnalyticsResponse(
		
        Long dishId,
        
        String dishName,
        
        Long totalComments
        
) {}