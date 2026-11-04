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
import com.smartmess.backend.repository.projection.OverallMealFeedbackProjection;

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
                AND feedback.commonComment IS NOT NULL

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

        /*
     * Fetches overall meal comments separately from dish-specific comments.
     * Results are restricted to the authenticated mess.
     */
    @Query("""
            SELECT feedback.commonComment

            FROM MealFeedback feedback

            WHERE feedback.mess.messId = :messId
              AND feedback.commentedDish IS NULL
              AND feedback.commonComment IS NOT NULL

            ORDER BY feedback.mealFeedbackId
            """)
    List<String> getOverallMealComments(
            @Param("messId") Long messId
    );

    /*
     * Fetches overall meal comments with their actual menu occurrence.
     * Uses menuDate, not feedback submission time.
     */
    @Query("""
            SELECT
                menu.menuId AS menuId,
                menu.menuDate AS menuDate,
                menu.mealSession AS mealSession,
                feedback.commonComment AS comment

            FROM MealFeedback feedback

            JOIN feedback.mealRecord mealRecord
            JOIN mealRecord.menu menu

            WHERE feedback.mess.messId = :messId
              AND feedback.commentedDish IS NULL
              AND feedback.commonComment IS NOT NULL

            ORDER BY menu.menuDate DESC, menu.mealSession, menu.menuId
            """)
    List<OverallMealFeedbackProjection> getOverallMealFeedbackObservations(
            @Param("messId") Long messId
    );
}