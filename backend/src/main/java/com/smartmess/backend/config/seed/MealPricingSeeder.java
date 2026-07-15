package com.smartmess.backend.config.seed;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.repository.MealPricingRepository;

@Component
public class MealPricingSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(MealPricingSeeder.class);

    private final MealPricingRepository mealPricingRepository;

    public MealPricingSeeder(MealPricingRepository mealPricingRepository) {
        this.mealPricingRepository = mealPricingRepository;
    }

    public void seed() {

        if (mealPricingRepository.count() > 0) {
            return;
        }

        MealPricing mealPricing = new MealPricing();

        mealPricing.setHalfMealPrice(BigDecimal.valueOf(60));

        mealPricing.setFullMealPrice(BigDecimal.valueOf(80));

        mealPricing.setExtraRotiPrice(BigDecimal.valueOf(10));

        mealPricingRepository.save(mealPricing);

        log.info("Demo Meal Pricing seeded successfully.");
    }
}