package com.smartmess.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;

public interface MealRecordRepository
        extends JpaRepository<MealRecord, Long> {

    /*
     * Customer Meal History
     *
     * Restricted to the specified mess.
     */
    List<MealRecord> findByMess_MessIdAndCustomerOrderByCollectedAtDesc(
            Long messId,
            Customer customer
    );

    /*
     * Today's Meal Records
     *
     * The service resolves today's menu within the same mess.
     */
    List<MealRecord> findByMess_MessIdAndMenu(
            Long messId,
            Menu menu
    );

    /*
     * Prevent duplicate records
     *
     * Preserves the response-linked duplicate lookup.
     */
    Optional<MealRecord> findByMess_MessIdAndMealResponse(
            Long messId,
            MealResponse mealResponse
    );

    /*
     * Manual Collection duplicate prevention
     *
     * Restricted to the specified mess.
     */
    boolean existsByMess_MessIdAndCustomerAndMenu(
            Long messId,
            Customer customer,
            Menu menu
    );

    // For billing module: tenant-scoped queries

    List<MealRecord> findByMess_MessIdAndCustomerAndCollectedAtBetween(
            Long messId,
            Customer customer,
            LocalDateTime start,
            LocalDateTime end
    );

    List<MealRecord> findByMess_MessIdAndBillOrderByCollectedAtAsc(
            Long messId,
            Bill bill
    );

    //************************************************************************//

    /*
     * Monthly meal insights.
     *
     * Preserves the existing billing-period basis:
     * only records linked to bills for the selected period.
     * Both the meal record and its bill must belong to the same mess.
     */
    @Query(
            value = """
                    SELECT
                        COUNT(mr.meal_record_id) AS totalMeals,

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
                        ) AS totalRotis,

                        COALESCE(
                            SUM(mr.extra_roti_count),
                            0
                        ) AS extraRotis

                    FROM meal_records mr
                    INNER JOIN bills b
                            ON mr.bill_id = b.bill_id
                           AND mr.mess_id = b.mess_id

                    WHERE mr.mess_id = :messId
                      AND b.billing_month = :billingMonth
                      AND b.billing_year = :billingYear
                    """,
            nativeQuery = true
    )
    List<Object[]> getMonthlyMealInsightsByMess(
            @Param("messId") Long messId,
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear
    );
}