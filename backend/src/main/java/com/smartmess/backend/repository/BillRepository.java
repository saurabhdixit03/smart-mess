package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.BillStatus;

import jakarta.persistence.LockModeType;

public interface BillRepository
        extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillIdAndMess_MessId(
            Long billId,
            Long messId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Bill b
            WHERE b.billId = :billId
              AND b.mess.messId = :messId
            """)
    Optional<Bill> findByBillIdAndMessIdForUpdate(
            @Param("billId") Long billId,
            @Param("messId") Long messId
    );

    boolean existsByMess_MessIdAndCustomerAndBillingMonthAndBillingYear(
            Long messId,
            Customer customer,
            Integer billingMonth,
            Integer billingYear
    );

    List<Bill> findAllByMess_MessIdAndCustomerAndBillingMonthAndBillingYearOrderByGeneratedAtDescBillIdDesc(
            Long messId,
            Customer customer,
            Integer billingMonth,
            Integer billingYear
    );

    List<Bill> findByMess_MessIdAndCustomerOrderByGeneratedAtDesc(
            Long messId,
            Customer customer
    );

    List<Bill> findByMess_MessIdAndBillStatusOrderByGeneratedAtAsc(
            Long messId,
            BillStatus billStatus
    );

    long countByMess_MessIdAndBillStatus(
            Long messId,
            BillStatus billStatus
    );

    List<Bill> findByMess_MessIdAndBillingMonthAndBillingYearOrderByGeneratedAtDesc(
            Long messId,
            Integer billingMonth,
            Integer billingYear
    );

    /*
     * Combined Bills page.
     * Both period parameters are null for all periods.
     * The service validates that they are supplied together.
     */
    @Query("""
            SELECT b
            FROM Bill b
            JOIN FETCH b.customer
            WHERE b.mess.messId = :messId
              AND (
                  :billingMonth IS NULL
                  OR b.billingMonth = :billingMonth
              )
              AND (
                  :billingYear IS NULL
                  OR b.billingYear = :billingYear
              )
            ORDER BY b.generatedAt DESC, b.billId DESC
            """)
    List<Bill> findForOverview(
            @Param("messId") Long messId,
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear
    );

    /*
     * Summary for all periods or one billing period.
     *
     * Collections mean payments attached to the selected bills,
     * regardless of the date on which those payments occurred.
     *
     * Result:
     * [0] total bills
     * [1] paid bills
     * [2] unpaid bills
     * [3] total billed amount
     * [4] recorded payment amount
     * [5] outstanding amount
     */
    @Query(
            value = """
                    SELECT
                        COUNT(b.bill_id),

                        COALESCE(SUM(
                            CASE
                                WHEN b.bill_status = 'PAID' THEN 1
                                ELSE 0
                            END
                        ), 0),

                        COALESCE(SUM(
                            CASE
                                WHEN b.bill_status = 'UNPAID' THEN 1
                                ELSE 0
                            END
                        ), 0),

                        COALESCE(SUM(b.total_amount), 0),

                        COALESCE(SUM(p.payment_amount), 0),

                        COALESCE(SUM(
                            CASE
                                WHEN b.bill_status = 'UNPAID'
                                THEN b.total_amount
                                ELSE 0
                            END
                        ), 0)

                    FROM bills b
                    LEFT JOIN payment p
                           ON p.bill_id = b.bill_id
                          AND p.mess_id = b.mess_id

                    WHERE b.mess_id = :messId
                      AND (
                          :billingMonth IS NULL
                          OR b.billing_month = :billingMonth
                      )
                      AND (
                          :billingYear IS NULL
                          OR b.billing_year = :billingYear
                      )
                    """,
            nativeQuery = true
    )
    List<Object[]> getFinancialSummaryForOverview(
            @Param("messId") Long messId,
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear
    );

    /*
     * Existing result positions are preserved for insights.
     * The unique payment.bill_id prevents multiplied bill rows.
     */
    @Query(
            value = """
                    SELECT
                        COUNT(DISTINCT b.customer_id) AS activeCustomers,

                        COUNT(b.bill_id) AS billsGenerated,

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN b.bill_status = 'PAID'
                                    THEN 1
                                    ELSE 0
                                END
                            ),
                            0
                        ) AS paidBills,

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN b.bill_status = 'UNPAID'
                                    THEN 1
                                    ELSE 0
                                END
                            ),
                            0
                        ) AS pendingBills,

                        COALESCE(
                            SUM(b.total_amount),
                            0
                        ) AS totalRevenue,

                        COALESCE(
                            SUM(p.payment_amount),
                            0
                        ) AS collectedRevenue,

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN b.bill_status = 'UNPAID'
                                    THEN b.total_amount
                                    ELSE 0
                                END
                            ),
                            0
                        ) AS pendingRevenue

                    FROM bills b
                    LEFT JOIN payment p
                           ON b.bill_id = p.bill_id
                          AND b.mess_id = p.mess_id

                    WHERE b.mess_id = :messId
                      AND b.billing_month = :billingMonth
                      AND b.billing_year = :billingYear
                    """,
            nativeQuery = true
    )
    List<Object[]> getMonthlyFinancialInsightsByMess(
            @Param("messId") Long messId,
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear
    );
}