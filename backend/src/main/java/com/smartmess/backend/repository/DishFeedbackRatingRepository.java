package com.smartmess.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.DishFeedbackRating;
import com.smartmess.backend.entity.MealFeedback;

public interface DishFeedbackRatingRepository
        extends JpaRepository<DishFeedbackRating, Long> {

    /*
     * Returns all dish ratings belonging to a feedback submission.
     */
    List<DishFeedbackRating> findByMealFeedback(
            MealFeedback mealFeedback
    );
}