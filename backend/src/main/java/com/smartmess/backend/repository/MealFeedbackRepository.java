package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealFeedback;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.repository.projection.DishFeedbackAnalyticsProjection;
import com.smartmess.backend.repository.projection.DishFeedbackCommentProjection;

public interface MealFeedbackRepository
        extends JpaRepository<MealFeedback, Long> {

    /*
     * Checks whether feedback already exists for a completed
     * meal record within the current tenant.
     */
    boolean existsByMess_MessIdAndMealRecord(
            Long messId,
            MealRecord mealRecord
    );

    /*
     * Tenant-scoped feedback lookup for a specific meal record.
     */
    Optional<MealFeedback> findByMess_MessIdAndMealRecord(
            Long messId,
            MealRecord mealRecord
    );

    /*
     * Tenant-scoped customer + meal record lookup.
     *
     * Useful when validating that feedback belongs to the
     * authenticated customer.
     */
    Optional<MealFeedback> findByMess_MessIdAndCustomerAndMealRecord(
            Long messId,
            Customer customer,
            MealRecord mealRecord
    );
    
    // Feedback Comments Count
    
    @Query("""
    	       SELECT
    	           feedback.commentedDish.dishId AS dishId,
    	           feedback.commentedDish.name AS dishName,
    	           COUNT(feedback) AS totalComments

    	       FROM MealFeedback feedback

    	       WHERE feedback.mess.messId = :messId
    	         AND feedback.commentedDish IS NOT NULL

    	       GROUP BY
    	           feedback.commentedDish.dishId,
    	           feedback.commentedDish.name

    	       ORDER BY
    	           COUNT(feedback) DESC
    	       """)
    	List<DishFeedbackAnalyticsProjection> getDishFeedbackAnalytics(
    	       @Param("messId") Long messId
    	);
    
    // To Fetch Actual Comments
    
    @Query("""
    	       SELECT
    	           feedback.commentedDish.dishId AS dishId,
    	           feedback.commentedDish.name AS dishName,
    	           feedback.commonComment AS comment

    	       FROM MealFeedback feedback

    	       WHERE feedback.mess.messId = :messId
    	         AND feedback.commentedDish IS NOT NULL
    	         AND feedback.commonComment IS NOT NULL

    	       ORDER BY
    	           feedback.commentedDish.dishId,
    	           feedback.mealFeedbackId
    	       """)
    	List<DishFeedbackCommentProjection> getDishFeedbackComments(
    	        @Param("messId") Long messId
    	);
}