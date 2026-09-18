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

    /*
     * Required application configuration.
     *
     * These records are initialized in every environment
     * when they do not already exist.
     */
    public void initializeRequiredConfiguration() {

        mealPricingSeeder.seed();

        messSettingsSeeder.seed();
    }

    /*
     * Optional demo operational data.
     *
     * This method runs only when
     * app.seed-demo-data is enabled.
     */
    public void seedDemoData() {

        customerSeeder.seed();

        menuSeeder.seed();

        mealResponseSeeder.seed();

        mealRecordSeeder.seedMealRecords();
    }
}