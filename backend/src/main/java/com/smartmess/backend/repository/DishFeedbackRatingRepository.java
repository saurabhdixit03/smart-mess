package com.smartmess.backend.repository;

import java.util.List;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.repository.projection.DishRatingAnalyticsProjection;

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
    
    // for analystics purpose 
    
    @Query("""
            SELECT
                rating.dish.dishId AS dishId,
                rating.dish.name AS dishName,

                COUNT(rating) AS totalRatings,

                AVG(rating.rating) AS averageRating,

                SUM(
                    CASE
                        WHEN rating.rating = 1 THEN 1
                        ELSE 0
                    END
                ) AS oneStarCount,

                SUM(
                    CASE
                        WHEN rating.rating = 2 THEN 1
                        ELSE 0
                    END
                ) AS twoStarCount,

                SUM(
                    CASE
                        WHEN rating.rating = 3 THEN 1
                        ELSE 0
                    END
                ) AS threeStarCount,

                SUM(
                    CASE
                        WHEN rating.rating = 4 THEN 1
                        ELSE 0
                    END
                ) AS fourStarCount,

                SUM(
                    CASE
                        WHEN rating.rating = 5 THEN 1
                        ELSE 0
                    END
                ) AS fiveStarCount

            FROM DishFeedbackRating rating

            WHERE rating.mealFeedback.mess.messId = :messId

            GROUP BY
                rating.dish.dishId,
                rating.dish.name

            ORDER BY
                AVG(rating.rating) DESC,
                COUNT(rating) DESC
            """)
    List<DishRatingAnalyticsProjection> getDishRatingAnalytics(
            @Param("messId") Long messId
    );   
    
}