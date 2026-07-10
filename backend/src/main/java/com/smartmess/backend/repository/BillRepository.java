package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;

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

}