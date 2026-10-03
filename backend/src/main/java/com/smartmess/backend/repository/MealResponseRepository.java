package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;

public interface MealResponseRepository
        extends JpaRepository<MealResponse, Long> {

    /*
     * Tenant-scoped response lookups.
     */
    Optional<MealResponse> findByMess_MessIdAndCustomerAndMenu(
            Long messId,
            Customer customer,
            Menu menu
    );

    Optional<MealResponse> findByMealResponseIdAndMess_MessId(
            Long mealResponseId,
            Long messId
    );

    List<MealResponse> findByMess_MessIdAndMenu(
            Long messId,
            Menu menu
    );

    List<MealResponse> findByMess_MessIdAndCustomer(
            Long messId,
            Customer customer
    );

    // Live Dashboard

    /*
     * Tenant-scoped counts preserve the existing response
     * status and meal-option counting rules.
     */
    long countByMess_MessIdAndMenuAndResponseStatus(
            Long messId,
            Menu menu,
            MealResponseStatus responseStatus
    );

    long countByMess_MessIdAndMenuAndMealOption(
            Long messId,
            Menu menu,
            MealOption mealOption
    );

    @Query("""
            SELECT COALESCE(SUM(m.extraRotiCount), 0)
            FROM MealResponse m
            WHERE m.mess.messId = :messId
              AND m.menu = :menu
              AND m.responseStatus =
                  com.smartmess.backend.enums.MealResponseStatus.ACCEPTED
            """)
    Long getTotalExtraRotisByMess(
            @Param("messId") Long messId,
            @Param("menu") Menu menu
    );

    // Collection queue for meal record module

    /*
     * Tenant-scoped queue.
     *
     * Preserves the existing rule that excludes responses
     * already connected to a meal record.
     */
    @Query("""
            SELECT mr
            FROM MealResponse mr
            WHERE mr.mess.messId = :messId
              AND mr.menu = :menu
              AND mr.responseStatus =
                  com.smartmess.backend.enums.MealResponseStatus.ACCEPTED
              AND NOT EXISTS (
                    SELECT 1
                    FROM MealRecord rec
                    WHERE rec.mealResponse = mr
              )
            ORDER BY mr.customer.fullName
            """)
    List<MealResponse> findCollectionQueueByMess(
            @Param("messId") Long messId,
            @Param("menu") Menu menu
    );
}