package com.smartmess.backend.config.seed;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.MessConfigurationInitializer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedDataService {

    private final MessRepository messRepository;
    private final MessConfigurationInitializer messConfigurationInitializer;
    private final DemoMessSeeder demoMessSeeder;
    private final CustomerSeeder customerSeeder;
    private final MenuSeeder menuSeeder;
    private final MealResponseSeeder mealResponseSeeder;
    private final MealRecordSeeder mealRecordSeeder;

    /*
     * Required application configuration.
     *
     * These records are initialized in every environment
     * when they do not already exist for an existing mess.
     *
     * New messes receive the same configuration during
     * owner registration.
     */
    public void initializeRequiredConfiguration() {

        for (Mess mess : messRepository.findAll()) {

            messConfigurationInitializer.initialize(mess);
        }
    }

    /*
     * Optional demo operational data.
     *
     * This method runs only when
     * app.seed-demo-data is enabled.
     *
     * All sample data belongs to the dedicated demo mess.
     * The entire sequence participates in one transaction.
     */
    @Transactional
    public void seedDemoData() {

        Mess demoMess =
                demoMessSeeder.seed();

        customerSeeder.seed(demoMess);

        menuSeeder.seed(demoMess);

        mealResponseSeeder.seed(demoMess);

        mealRecordSeeder.seedMealRecords(demoMess);
    }
}