package com.smartmess.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.MealPricing;

public interface MealPricingRepository
        extends JpaRepository<MealPricing, Long> {

    /*
     * Returns the current pricing configured for a specific mess.
     */
    Optional<MealPricing> findByMess_MessId(Long messId);
}