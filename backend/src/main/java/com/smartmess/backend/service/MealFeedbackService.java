package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.SubmitMealFeedbackRequest;
import com.smartmess.backend.dto.response.PendingMealFeedbackResponse;

public interface MealFeedbackService {

    PendingMealFeedbackResponse getPendingFeedback();

    void submitFeedback(SubmitMealFeedbackRequest request);
}