package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.BillStatus;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository
        extends JpaRepository<Bill, Long> {

    /*
     * Check whether a bill already exists
     * for a customer and billing period.
     */
    boolean existsByCustomerAndBillingMonthAndBillingYear(
            Customer customer,
            Integer billingMonth,
            Integer billingYear
    );

    /*
     * Find a customer's bill
     * for a specific billing period.
     */
    Optional<Bill> findByCustomerAndBillingMonthAndBillingYear(
            Customer customer,
            Integer billingMonth,
            Integer billingYear
    );

    /*
     * View all bills of a customer.
     */
    List<Bill> findByCustomerOrderByGeneratedAtDesc(
            Customer customer
    );
    
    // for payment dashboard 
    List<Bill> findByBillStatusOrderByGeneratedAtAsc(
            BillStatus billStatus
    );
    
    long countByBillStatus(BillStatus billStatus);
    
    /*
     * Monthly financial insights.
     */
    @Query(value = """
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

        WHERE b.billing_month = :billingMonth
          AND b.billing_year = :billingYear
        """,
        nativeQuery = true)
    List<Object[]> getMonthlyFinancialInsights(
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear
    );

}