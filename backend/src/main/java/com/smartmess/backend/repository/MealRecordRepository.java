package com.smartmess.backend.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    /*
     * Customer Meal History
     */
    List<MealRecord> findByCustomerOrderByCollectedAtDesc(Customer customer);

    /*
     * Today's Meal Records
     */
    List<MealRecord> findByMenu(Menu menu);

    /*
     * Prevent duplicate records
     */
    Optional<MealRecord> findByMealResponse(MealResponse mealResponse);

    /*
     * Manual Collection duplicate prevention
     */
    boolean existsByCustomerAndMenu(Customer customer, Menu menu);
    
    // for billing module
    
    List<MealRecord> findByCustomerAndCollectedAtBetween(
            Customer customer,
            LocalDateTime start,
            LocalDateTime end
    );

    List<MealRecord> findByBillOrderByCollectedAtAsc(
            Bill bill
    );
    
    //*********************************************************************************//
    
    /*
     * Monthly meal insights.
     */
    @Query(value = """

        SELECT

            COUNT(mr.meal_record_id)                                           AS totalMeals,

            COUNT(
    CASE
        WHEN mr.meal_option = 'FULL'
        THEN 1
    END
) AS fullMeals,

            COUNT(
    CASE
        WHEN mr.meal_option = 'HALF'
        THEN 1
    END
) AS halfMeals,

            COALESCE(
                SUM(
                    CASE
                        WHEN mr.meal_option = 'FULL'
                        THEN 3 + mr.extra_roti_count
                        WHEN mr.meal_option = 'HALF'
                        THEN 3 + mr.extra_roti_count
                        ELSE 0
                    END
                ),
                0
            )                                                                  AS totalRotis,

            COALESCE(
                SUM(mr.extra_roti_count),
                0
            )                                                                  AS extraRotis

        FROM meal_records mr

        INNER JOIN bills b
                ON mr.bill_id = b.bill_id

        WHERE b.billing_month = :billingMonth
          AND b.billing_year = :billingYear

        """,
        nativeQuery = true)
    List<Object[]> getMonthlyMealInsights(
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear
    );
}