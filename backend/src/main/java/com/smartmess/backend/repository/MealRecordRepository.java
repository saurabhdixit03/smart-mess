package com.smartmess.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;

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

}