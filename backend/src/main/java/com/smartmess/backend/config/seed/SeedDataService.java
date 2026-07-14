package com.smartmess.backend.config.seed;

import java.math.BigDecimal;


import org.springframework.stereotype.Service;

import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.repository.MessSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeedDataService {

    private final MealPricingRepository mealPricingRepository;

    private final MessSettingsRepository messSettingsRepository;
    
    private final CustomerRepository customerRepository;

    public void seedDemoData() {

        seedMealPricing();

        seedMessSettings();
        
        seedCustomers();
        
        

        /*
         * Future Modules
         *
         * seedCustomers();
         * seedMenus();
         * seedMealResponses();
         * seedMealRecords();
         * seedBills();
         * seedPayments();
         */

    }

    private void seedMealPricing() {

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

    private void seedMessSettings() {

        if (messSettingsRepository.count() > 0) {
            return;
        }

        MessSettings settings = new MessSettings();

        settings.setReceiverName("Smart Mess");

        settings.setUpiId("smartmess@upi");

        messSettingsRepository.save(settings);

        log.info("Demo Mess Settings seeded successfully.");

    }
    
    private record CustomerSeed(

            String fullName,

            String mobileNumber,

            String email,

            String remarks,

            LocalDate joiningDate,

            CustomerStatus status

    ) {
    }
    
    private void seedCustomers() {

        if (customerRepository.count() > 0) {
            return;
        }
        
        List<CustomerSeed> customers = List.of(

                new CustomerSeed(
                        "Aarav Sharma",
                        "9876500001",
                        "aarav.sharma@example.com",
                        "Prefers full meal",
                        LocalDate.of(2026, 1, 10),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Priya Patil",
                        "9876500002",
                        "priya.patil@example.com",
                        "Prefers half meal",
                        LocalDate.of(2026, 2, 5),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Rohan Kulkarni",
                        "9876500003",
                        "rohan.kulkarni@example.com",
                        "Extra roti occasionally",
                        LocalDate.of(2026, 2, 18),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Sneha Joshi",
                        "9876500004",
                        "sneha.joshi@example.com",
                        null,
                        LocalDate.of(2026, 3, 12),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Aditya Deshmukh",
                        "9876500005",
                        "aditya.deshmukh@example.com",
                        "Night shift customer",
                        LocalDate.of(2026, 4, 2),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Neha Jadhav",
                        "9876500006",
                        "neha.jadhav@example.com",
                        null,
                        LocalDate.of(2026, 4, 25),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Rahul Pawar",
                        "9876500007",
                        "rahul.pawar@example.com",
                        "Vegetarian",
                        LocalDate.of(2026, 5, 15),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Anjali Shinde",
                        "9876500008",
                        "anjali.shinde@example.com",
                        null,
                        LocalDate.of(2026, 6, 8),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Vikas More",
                        "9876500009",
                        "vikas.more@example.com",
                        null,
                        LocalDate.of(2026, 1, 22),
                        CustomerStatus.INACTIVE
                ),

                new CustomerSeed(
                        "Pooja Kale",
                        "9876500010",
                        "pooja.kale@example.com",
                        null,
                        LocalDate.of(2026, 3, 30),
                        CustomerStatus.INACTIVE
                )

        );
        
        for (CustomerSeed seed : customers) {

            Customer customer = new Customer();

            customer.setFullName(seed.fullName());

            customer.setMobileNumber(seed.mobileNumber());

            customer.setEmail(seed.email());

            customer.setRemarks(seed.remarks());

            customer.setJoiningDate(seed.joiningDate());

            customer.setStatus(seed.status());

            customerRepository.save(customer);

        }

        log.info("Demo Customers seeded successfully.");

    }

}