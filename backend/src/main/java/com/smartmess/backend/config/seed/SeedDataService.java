package com.smartmess.backend.config.seed;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedDataService {

    private final MealPricingSeeder mealPricingSeeder;
    private final MessSettingsSeeder messSettingsSeeder;
    private final CustomerSeeder customerSeeder;
    private final MenuSeeder menuSeeder;
    private final MealResponseSeeder mealResponseSeeder;
    private final MealRecordSeeder mealRecordSeeder;

    public void seedDemoData() {

        /*
         * Seed foundational configuration first.
         */
        mealPricingSeeder.seed();

        messSettingsSeeder.seed();

        /*
         * Seed customers before operational data.
         */
        customerSeeder.seed();

        /*
         * Seed data in dependency order:
         *
         * Menu
         * Meal Response
         * Meal Record
         */
        menuSeeder.seed();

        mealResponseSeeder.seed();

        mealRecordSeeder.seedMealRecords();
    }
}