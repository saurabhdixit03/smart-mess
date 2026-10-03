package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.BillStatus;

public interface BillRepository
        extends JpaRepository<Bill, Long> {

    /*
     * Finds a bill only within the specified mess.
     */
    Optional<Bill> findByBillIdAndMess_MessId(
            Long billId,
            Long messId
    );

    /*
     * Check whether a bill already exists
     * for a customer and billing period within one mess.
     */
    boolean existsByMess_MessIdAndCustomerAndBillingMonthAndBillingYear(
            Long messId,
            Customer customer,
            Integer billingMonth,
            Integer billingYear
    );

    /*
     * Find a customer's bill
     * for a specific billing period within one mess.
     */
    Optional<Bill> findByMess_MessIdAndCustomerAndBillingMonthAndBillingYear(
            Long messId,
            Customer customer,
            Integer billingMonth,
            Integer billingYear
    );

    /*
     * View all bills of a customer within one mess.
     */
    List<Bill> findByMess_MessIdAndCustomerOrderByGeneratedAtDesc(
            Long messId,
            Customer customer
    );

    // For payment dashboard: tenant-scoped queries

    List<Bill> findByMess_MessIdAndBillStatusOrderByGeneratedAtAsc(
            Long messId,
            BillStatus billStatus
    );

    long countByMess_MessIdAndBillStatus(
            Long messId,
            BillStatus billStatus
    );

    /*
     * View all bills for a billing period within one mess.
     */
    List<Bill> findByMess_MessIdAndBillingMonthAndBillingYearOrderByGeneratedAtDesc(
            Long messId,
            Integer billingMonth,
            Integer billingYear
    );

    /*
     * Monthly financial insights.
     *
     * Preserves the existing calculations and result positions.
     * Bills are restricted to the specified mess.
     * Only payments belonging to the same mess are joined.
     */
    @Query(
            value = """
                    SELECT
                        COUNT(DISTINCT b.customer_id) AS activeCustomers,

                        COUNT(b.bill_id) AS billsGenerated,

                        SUM(
                            CASE
                                WHEN b.bill_status = 'PAID'
                                THEN 1
                                ELSE 0
                            END
                        ) AS paidBills,

                        SUM(
                            CASE
                                WHEN b.bill_status IN ('UNPAID', 'PAYMENT_PENDING')
                                THEN 1
                                ELSE 0
                            END
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
                                    WHEN b.bill_status IN ('UNPAID', 'PAYMENT_PENDING')
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