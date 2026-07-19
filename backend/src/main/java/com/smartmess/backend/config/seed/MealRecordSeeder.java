package com.smartmess.backend.config.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;

@Component
public class MealRecordSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(MealRecordSeeder.class);

    private final MealRecordRepository mealRecordRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MealResponseRepository mealResponseRepository;
    private final MealPricingRepository mealPricingRepository;

    public MealRecordSeeder(
            MealRecordRepository mealRecordRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MealResponseRepository mealResponseRepository,
            MealPricingRepository mealPricingRepository) {

        this.mealRecordRepository = mealRecordRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.mealResponseRepository = mealResponseRepository;
        this.mealPricingRepository = mealPricingRepository;
    }

    private record MealRecordSeed(

            int customerIndex,

            LocalDate menuDate,

            MealSession mealSession,

            boolean useMealResponse,

            MealOption servedMeal,

            int extraRotis

    ) {
    }
    
    public void seedDemoData() {

        seedMealRecords();
    }

    public void seedMealRecords() {
    	
    	if (mealRecordRepository.count() > 0) {
    	    return;
    	}
    	
    	List<Customer> customers =
    	        customerRepository.findByStatus(CustomerStatus.ACTIVE);

    	if (customers.size() < 8) {

    	    log.warn("Skipping MealRecord seeding because active customers are missing.");

    	    return;
    	}
    	
    	Menu todayLunch =
    	        menuRepository.findByMenuDateAndMealSession(
    	                LocalDate.now(),
    	                MealSession.LUNCH)
    	        .orElseThrow(() ->
    	                new IllegalStateException("Today's lunch menu not found"));

    	Menu todayDinner =
    	        menuRepository.findByMenuDateAndMealSession(
    	                LocalDate.now(),
    	                MealSession.DINNER)
    	        .orElseThrow(() ->
    	                new IllegalStateException("Today's dinner menu not found"));
    	
    	MealPricing pricing =
    	        mealPricingRepository
    	                .findTopByOrderByUpdatedAtDesc()
    	                .orElseThrow(() ->
    	                        new IllegalStateException("Meal pricing missing"));

    	
    	List<MealRecordSeed> records = List.of(

    		    // ---------------- TODAY LUNCH ----------------

    		    // Normal
    		    new MealRecordSeed(0, LocalDate.now(), MealSession.LUNCH, true, MealOption.FULL, 1),

    		    // Normal
    		    new MealRecordSeed(1, LocalDate.now(), MealSession.LUNCH, true, MealOption.HALF, 0),

    		    // Edited
    		    // Response = FULL + 2 roti
    		    // Served = HALF + 2 roti
    		    new MealRecordSeed(3, LocalDate.now(), MealSession.LUNCH, true, MealOption.HALF, 2),

    		    // Normal
    		    new MealRecordSeed(4, LocalDate.now(), MealSession.LUNCH, true, MealOption.HALF, 0),

    		    // Walk-in (Lunch response was DECLINED)
    		    new MealRecordSeed(5, LocalDate.now(), MealSession.LUNCH, false, MealOption.HALF, 0),

    		    // Edited
    		    // Response = FULL
    		    // Served = HALF
    		    new MealRecordSeed(6, LocalDate.now(), MealSession.LUNCH, true, MealOption.HALF, 2),

    		    // Customer 7 -> Accepted but not collected


    		    // ---------------- TODAY DINNER ----------------

    		    // Normal
    		    new MealRecordSeed(0, LocalDate.now(), MealSession.DINNER, true, MealOption.FULL, 0),

    		    // Walk-in (Dinner response DECLINED)
    		    new MealRecordSeed(1, LocalDate.now(), MealSession.DINNER, false, MealOption.FULL, 2),

    		    // Customer 2 -> Accepted but not collected

    		    // Edited
    		    new MealRecordSeed(3, LocalDate.now(), MealSession.DINNER, true, MealOption.HALF, 1),

    		    // Customer 4 -> Declined (No record)

    		    // Normal
    		    new MealRecordSeed(5, LocalDate.now(), MealSession.DINNER, true, MealOption.HALF, 0),

    		    // Edited
    		    new MealRecordSeed(6, LocalDate.now(), MealSession.DINNER, true, MealOption.FULL, 1)

    		    // Customer 7 -> Declined (No record)
    		);    
    	
    	for (MealRecordSeed seed : records) {

    	    Customer customer =
    	            customers.get(seed.customerIndex());

    	    Menu menu =
    	            seed.mealSession() == MealSession.LUNCH
    	                    ? todayLunch
    	                    : todayDinner;

    	    MealResponse mealResponse = null;

    	    if (seed.useMealResponse()) {

    	        mealResponse = mealResponseRepository
    	                .findByCustomerAndMenu(customer, menu)
    	                .orElseThrow(() ->
    	                        new IllegalStateException(
    	                                "MealResponse not found for "
    	                                        + customer.getFullName()
    	                                        + " - "
    	                                        + menu.getMealSession()
    	                        ));
    	    }
    	    
    	    BigDecimal mealPrice =
    	            seed.servedMeal() == MealOption.FULL
    	                    ? pricing.getFullMealPrice()
    	                    : pricing.getHalfMealPrice();

    	    BigDecimal totalAmount =
    	            mealPrice.add(
    	                    pricing.getExtraRotiPrice()
    	                            .multiply(
    	                                    BigDecimal.valueOf(
    	                                            seed.extraRotis()
    	                                    )
    	                            )
    	            );
    	    
    	    MealRecord mealRecord = MealRecord.builder()

    	            .customer(customer)

    	            .menu(menu)

    	            .mealResponse(mealResponse)

    	            .mealOption(seed.servedMeal())

    	            .mealPrice(mealPrice)

    	            .extraRotiCount(seed.extraRotis())

    	            .extraRotiPrice(pricing.getExtraRotiPrice())

    	            .totalAmount(totalAmount)

    	            .collectedAt(LocalDateTime.now())

    	            .build();
    	    
    	    mealRecordRepository.save(mealRecord);

    	}
    	
    	log.info("Demo Meal Records seeded successfully.");
    	
    }
    
}