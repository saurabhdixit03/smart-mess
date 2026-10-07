package com.smartmess.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;

import jakarta.persistence.LockModeType;

public interface MealRecordRepository
        extends JpaRepository<MealRecord, Long> {

    List<MealRecord> findByMess_MessIdAndCustomerOrderByCollectedAtDesc(
            Long messId,
            Customer customer
    );

    List<MealRecord> findByMess_MessIdAndMenu(
            Long messId,
            Menu menu
    );

    Optional<MealRecord> findByMess_MessIdAndMealResponse(
            Long messId,
            MealResponse mealResponse
    );

    boolean existsByMess_MessIdAndCustomerAndMenu(
            Long messId,
            Customer customer,
            Menu menu
    );

    /*
     * Collection status for active customer search results.
     *
     * Customer/menu matching includes records without a linked
     * meal response. Call only when customerIds is non-empty.
     */
    @Query("""
            SELECT DISTINCT record.customer.customerId
            FROM MealRecord record
            WHERE record.mess.messId = :messId
              AND record.customer.mess.messId = :messId
              AND record.menu.mess.messId = :messId
              AND record.menu = :menu
              AND record.customer.customerId IN :customerIds
            """)
    List<Long> findCollectedCustomerIdsForMenu(
            @Param("messId") Long messId,
            @Param("menu") Menu menu,
            @Param("customerIds") List<Long> customerIds
    );

    List<MealRecord> findByMess_MessIdAndCustomerAndCollectedAtBetween(
            Long messId,
            Customer customer,
            LocalDateTime start,
            LocalDateTime end
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT mr
            FROM MealRecord mr
            WHERE mr.mess.messId = :messId
              AND mr.customer.mess.messId = :messId
              AND mr.bill IS NULL
              AND mr.collectedAt >= :start
              AND mr.collectedAt < :endExclusive
            ORDER BY mr.customer.customerId ASC, mr.mealRecordId ASC
            """)
    List<MealRecord> findUnbilledForPeriodForUpdate(
            @Param("messId") Long messId,
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    @Query("""
            SELECT DISTINCT mr.customer.customerId
            FROM MealRecord mr
            WHERE mr.mess.messId = :messId
              AND mr.customer.mess.messId = :messId
              AND mr.customer.status IN (
                  com.smartmess.backend.enums.CustomerStatus.ACTIVE,
                  com.smartmess.backend.enums.CustomerStatus.INACTIVE
              )
              AND mr.bill IS NULL
              AND mr.collectedAt >= :start
              AND mr.collectedAt < :endExclusive
            ORDER BY mr.customer.customerId ASC
            """)
    List<Long> findUnbilledCustomerIdsForPeriod(
            @Param("messId") Long messId,
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT mr
            FROM MealRecord mr
            WHERE mr.mess.messId = :messId
              AND mr.customer.mess.messId = :messId
              AND mr.customer.customerId = :customerId
              AND mr.bill IS NULL
              AND mr.collectedAt >= :start
              AND mr.collectedAt < :endExclusive
            ORDER BY mr.mealRecordId ASC
            """)
    List<MealRecord> findUnbilledForCustomerForUpdate(
            @Param("messId") Long messId,
            @Param("customerId") Long customerId,
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    /*
     * Internal scheduler discovery across tenants.
     *
     * Returns:
     * [0] mess ID
     * [1] collection year
     * [2] collection month
     *
     * completedBefore is the start of the current month,
     * resolved using the application's configured clock.
     * Billing execution subsequently validates each job's tenant.
     */
    @Query("""
            SELECT mr.mess.messId,
                   YEAR(mr.collectedAt),
                   MONTH(mr.collectedAt)
            FROM MealRecord mr
            WHERE mr.customer.mess.messId = mr.mess.messId
              AND mr.customer.status IN (
                  com.smartmess.backend.enums.CustomerStatus.ACTIVE,
                  com.smartmess.backend.enums.CustomerStatus.INACTIVE
              )
              AND mr.bill IS NULL
              AND mr.collectedAt < :completedBefore
            GROUP BY mr.mess.messId,
                     YEAR(mr.collectedAt),
                     MONTH(mr.collectedAt)
            ORDER BY YEAR(mr.collectedAt) ASC,
                     MONTH(mr.collectedAt) ASC,
                     mr.mess.messId ASC
            """)
    List<Object[]> findUnbilledCompletedPeriods(
            @Param("completedBefore") LocalDateTime completedBefore
    );

    List<MealRecord> findByMess_MessIdAndBillOrderByCollectedAtAsc(
            Long messId,
            Bill bill
    );

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