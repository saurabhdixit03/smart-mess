package com.smartmess.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.MealPricing;

public interface MealPricingRepository extends JpaRepository<MealPricing, Long> {

}