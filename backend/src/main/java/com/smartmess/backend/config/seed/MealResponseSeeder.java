package com.smartmess.backend.config.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;

@Component
public class MealResponseSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(MealResponseSeeder.class);

    private final MealResponseRepository mealResponseRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;

    public MealResponseSeeder(
            MealResponseRepository mealResponseRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository) {

        this.mealResponseRepository = mealResponseRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
    }

    private record MealResponseSeed(

            int customerIndex,

            MealSession mealSession,

            MealResponseStatus responseStatus,

            MealOption mealOption,

            int extraRotiCount

    ) {
    }

    public void seed() {

        if (mealResponseRepository.count() > 0) {
            return;
        }

        List<Customer> customers =
                customerRepository.findByStatus(CustomerStatus.ACTIVE);

        if (customers.isEmpty()) {

            log.warn("Skipping MealResponse seeding because no active customers are available.");

            return;
        }

        Menu todayLunch = menuRepository
                .findByMenuDateAndMealSession(
                        LocalDate.now(),
                        MealSession.LUNCH)
                .orElseThrow(() ->
                        new IllegalStateException("Today's Lunch menu not found."));

        Menu todayDinner = menuRepository
                .findByMenuDateAndMealSession(
                        LocalDate.now(),
                        MealSession.DINNER)
                .orElseThrow(() ->
                        new IllegalStateException("Today's Dinner menu not found."));

        for (MealResponseSeed seed : buildDemoResponses()) {

            Menu menu =
                    seed.mealSession() == MealSession.LUNCH
                            ? todayLunch
                            : todayDinner;

            mealResponseRepository.save(
                    createMealResponse(
                            customers.get(seed.customerIndex()),
                            menu,
                            seed));
        }

        log.info("Demo Meal Responses seeded successfully.");
    }
    
    private MealResponse createMealResponse(
            Customer customer,
            Menu menu,
            MealResponseSeed seed) {

        MealResponse response = new MealResponse();

        response.setCustomer(customer);

        response.setMenu(menu);

        response.setResponseStatus(seed.responseStatus());

        response.setMealOption(seed.mealOption());

        response.setExtraRotiCount(seed.extraRotiCount());

        response.setRespondedAt(LocalDateTime.now());

        return response;
    }
    
    private List<MealResponseSeed> buildDemoResponses() {
    	
    	return List.of(
    
            // ---------------- TODAY LUNCH ----------------

            new MealResponseSeed(0, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 1),
            new MealResponseSeed(1, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
            new MealResponseSeed(2, MealSession.LUNCH, MealResponseStatus.DECLINED, null, 0),
            new MealResponseSeed(3, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 2),
            new MealResponseSeed(4, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
            new MealResponseSeed(5, MealSession.LUNCH, MealResponseStatus.DECLINED, null, 0),
            new MealResponseSeed(6, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 1),
            new MealResponseSeed(7, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 0),

            // ---------------- TODAY DINNER ----------------

            new MealResponseSeed(0, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.FULL, 0),
            new MealResponseSeed(1, MealSession.DINNER, MealResponseStatus.DECLINED, null, 0),
            new MealResponseSeed(2, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
            new MealResponseSeed(3, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.FULL, 1),
            new MealResponseSeed(4, MealSession.DINNER, MealResponseStatus.DECLINED, null, 0),
            new MealResponseSeed(5, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
            new MealResponseSeed(6, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.FULL, 2),
            new MealResponseSeed(7, MealSession.DINNER, MealResponseStatus.DECLINED, null, 0)

    		);
    }
}
