package com.smartmess.backend.config.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

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

    /*
     * Fixed seed keeps demo data reproducible.
     *
     * Every fresh database reset will generate
     * the same response distribution instead of
     * producing different data on every run.
     */
    private final Random random = new Random(20260813L);

    public MealResponseSeeder(
            MealResponseRepository mealResponseRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository) {

        this.mealResponseRepository = mealResponseRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
    }

    public void seed() {

        if (mealResponseRepository.count() > 0) {
            return;
        }

        List<Customer> customers =
                customerRepository.findByStatus(CustomerStatus.ACTIVE);

        if (customers.isEmpty()) {

            log.warn(
                    "Skipping MealResponse seeding because no active customers are available."
            );

            return;
        }

        List<Menu> menus =
                menuRepository.findAllByOrderByMenuDateAscMealSessionAsc();

        if (menus.isEmpty()) {

            log.warn(
                    "Skipping MealResponse seeding because no menus are available."
            );

            return;
        }

        int responseCount = 0;

        for (Menu menu : menus) {

            /*
             * Today's responses are intentionally limited.
             *
             * This keeps today's customer-response flow
             * available for manual testing.
             */
            boolean isToday =
                    menu.getMenuDate().equals(LocalDate.now());

            for (Customer customer : customers) {

                /*
                 * Do not automatically create a response
                 * for every customer.
                 *
                 * Historical customers may or may not
                 * respond to a particular menu.
                 */
                if (!shouldCustomerRespond(isToday)) {
                    continue;
                }

                MealResponse response =
                        createMealResponse(customer, menu);

                mealResponseRepository.save(response);

                responseCount++;
            }
        }

        log.info(
                "Demo Meal Responses seeded successfully. Total responses: {}",
                responseCount
        );
    }

    private boolean shouldCustomerRespond(boolean isToday) {

        /*
         * Today's responses:
         * Keep enough customers without completely
         * occupying the current-day testing flow.
         *
         * Approximately 50% respond.
         */
        if (isToday) {
            return random.nextDouble() < 0.50;
        }

        /*
         * Historical menus:
         * Approximately 75% of customers respond.
         */
        return random.nextDouble() < 0.75;
    }

    private MealResponse createMealResponse(
            Customer customer,
            Menu menu) {

        MealResponse response = new MealResponse();

        response.setCustomer(customer);

        response.setMenu(menu);

        /*
         * Approximately:
         * 75% ACCEPTED
         * 25% DECLINED
         */
        boolean accepted =
                random.nextDouble() < 0.75;

        if (accepted) {

            response.setResponseStatus(
                    MealResponseStatus.ACCEPTED
            );

            /*
             * Approximately:
             * 65% FULL
             * 35% HALF
             */
            MealOption mealOption =
                    random.nextDouble() < 0.65
                            ? MealOption.FULL
                            : MealOption.HALF;

            response.setMealOption(mealOption);

            response.setExtraRotiCount(
                    generateExtraRotiCount()
            );

        } else {

            response.setResponseStatus(
                    MealResponseStatus.DECLINED
            );

            /*
             * Declined response must not have
             * a meal option or extra rotis.
             */
            response.setMealOption(null);

            response.setExtraRotiCount(0);
        }

        /*
         * Historical response should look like it
         * was submitted on the actual menu date.
         *
         * We generate a realistic response time
         * between approximately 7:00 AM and 10:00 PM.
         */
        response.setRespondedAt(
                generateResponseTime(menu.getMenuDate())
        );

        return response;
    }

    private int generateExtraRotiCount() {

        double value = random.nextDouble();

        /*
         * ~75% → no extra roti
         * ~20% → 1 extra roti
         * ~5%  → 2 extra rotis
         */
        if (value < 0.75) {
            return 0;
        }

        if (value < 0.95) {
            return 1;
        }

        return 2;
    }

    private LocalDateTime generateResponseTime(
            LocalDate menuDate) {

        /*
         * Generate a response between 7:00 AM
         * and 10:00 PM on the menu date.
         */
        int hour =
                7 + random.nextInt(16);

        int minute =
                random.nextInt(60);

        return LocalDateTime.of(
                menuDate,
                LocalTime.of(hour, minute)
        );
    }
}