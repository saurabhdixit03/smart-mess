package com.smartmess.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealFeedback;
import com.smartmess.backend.entity.MealRecord;

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
}