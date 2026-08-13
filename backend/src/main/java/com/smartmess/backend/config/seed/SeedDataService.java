package com.smartmess.backend.config.seed;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeedDataService {
    
    private final MealPricingSeeder mealPricingSeeder;
    private final MessSettingsSeeder messSettingsSeeder;
    private final CustomerSeeder customerSeeder;
    private final MenuSeeder menuSeeder;
    private final MealResponseSeeder mealResponseSeeder;
    private final MealRecordSeeder mealRecordSeeder; 
    //  private final BillSeeder billSeeder;
    // private final PaymentSeeder paymentSeeder;

    
    public void seedDemoData() {

        mealPricingSeeder.seed();

        messSettingsSeeder.seed();

        customerSeeder.seed();

        menuSeeder.seed();

        mealResponseSeeder.seed();
        
        mealRecordSeeder.seedMealRecords();
        
        // intentionally disable to stop generating bills automatically 
        
       // billSeeder.seedDemoData();
        
       // paymentSeeder.seedPayments();

        
    }
   
}
