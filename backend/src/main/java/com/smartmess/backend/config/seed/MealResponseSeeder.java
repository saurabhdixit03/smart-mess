package com.smartmess.backend.config.seed;

import java.time.Clock;
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
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;

@Component
public class MealResponseSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MealResponseSeeder.class
            );

    private static final long RANDOM_SEED =
            20260917L;

    private final MealResponseRepository mealResponseRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final Clock clock;

    public MealResponseSeeder(
            MealResponseRepository mealResponseRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            Clock clock) {

        this.mealResponseRepository =
                mealResponseRepository;

        this.customerRepository =
                customerRepository;

        this.menuRepository =
                menuRepository;

        this.clock =
                clock;
    }

    public void seed() {

        if (mealResponseRepository.count() > 0) {

            log.info(
                    "Meal Responses already exist. Skipping demo seeding."
            );

            return;
        }

        List<Customer> customers =
                customerRepository.findByStatus(
                        CustomerStatus.ACTIVE
                );

        if (customers.isEmpty()) {

            log.warn(
                    "Skipping Meal Response seeding because no active customers are available."
            );

            return;
        }

        List<Menu> menus =
                menuRepository
                        .findAllByOrderByMenuDateAscMealSessionAsc();

        if (menus.isEmpty()) {

            log.warn(
                    "Skipping Meal Response seeding because no menus are available."
            );

            return;
        }

        Random random =
                new Random(
                        RANDOM_SEED
                );

        LocalDate today =
                LocalDate.now(clock);

        int historicalResponseCount = 0;
        int todayResponseCount = 0;

        for (Menu menu : menus) {

            boolean isToday =
                    menu.getMenuDate()
                            .equals(today);

            for (int customerIndex = 0;
                 customerIndex < customers.size();
                 customerIndex++) {

                Customer customer =
                        customers.get(
                                customerIndex
                        );

                /*
                 * Only half of the active customers receive
                 * a seeded response for today's lunch.
                 *
                 * The remaining customers can submit their
                 * responses manually.
                 */
                if (isToday) {

                    if (!shouldSeedTodayResponse(
                            customerIndex
                    )) {
                        continue;
                    }

                    MealResponse response =
                            createTodayResponse(
                                    customer,
                                    menu,
                                    customerIndex
                            );

                    mealResponseRepository.save(
                            response
                    );

                    todayResponseCount++;

                    continue;
                }

                /*
                 * Approximately 75% of customers respond
                 * to each historical menu.
                 */
                if (!shouldSeedHistoricalResponse(
                        random
                )) {
                    continue;
                }

                MealResponse response =
                        createHistoricalResponse(
                                customer,
                                menu,
                                random
                        );

                mealResponseRepository.save(
                        response
                );

                historicalResponseCount++;
            }
        }

        log.info(
                "Demo Meal Responses seeded successfully. "
                        + "Historical: {}, Today: {}, Total: {}.",
                historicalResponseCount,
                todayResponseCount,
                historicalResponseCount
                        + todayResponseCount
        );
    }

    private boolean shouldSeedTodayResponse(
            int customerIndex) {

        /*
         * With eight active demo customers, indexes
         * 0, 2, 4 and 6 receive a response.
         *
         * This leaves four customers available for
         * manual response testing.
         */
        return customerIndex % 2 == 0;
    }

    private boolean shouldSeedHistoricalResponse(
            Random random) {

        return random.nextDouble() < 0.75;
    }

    private MealResponse createTodayResponse(
            Customer customer,
            Menu menu,
            int customerIndex) {

        MealResponse response =
                new MealResponse();

        response.setCustomer(
                customer
        );

        response.setMenu(
                menu
        );

        /*
         * Customer index 4 represents a declined response.
         *
         * The other seeded customers accept today's lunch,
         * giving the dashboard both accepted and declined
         * response examples.
         */
        if (customerIndex == 4) {

            response.setResponseStatus(
                    MealResponseStatus.DECLINED
            );

            response.setMealOption(
                    null
            );

            response.setExtraRotiCount(
                    0
            );

        } else {

            response.setResponseStatus(
                    MealResponseStatus.ACCEPTED
            );

            MealOption mealOption =
                    customerIndex % 3 == 0
                            ? MealOption.FULL
                            : MealOption.HALF;

            response.setMealOption(
                    mealOption
            );

            response.setExtraRotiCount(
                    customerIndex == 6
                            ? 1
                            : 0
            );
        }

        response.setRespondedAt(
                LocalDateTime.now(clock)
        );

        return response;
    }

    private MealResponse createHistoricalResponse(
            Customer customer,
            Menu menu,
            Random random) {

        MealResponse response =
                new MealResponse();

        response.setCustomer(
                customer
        );

        response.setMenu(
                menu
        );

        /*
         * Approximately:
         *
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
             *
             * 65% FULL
             * 35% HALF
             */
            MealOption mealOption =
                    random.nextDouble() < 0.65
                            ? MealOption.FULL
                            : MealOption.HALF;

            response.setMealOption(
                    mealOption
            );

            response.setExtraRotiCount(
                    generateExtraRotiCount(
                            random
                    )
            );

        } else {

            response.setResponseStatus(
                    MealResponseStatus.DECLINED
            );

            response.setMealOption(
                    null
            );

            response.setExtraRotiCount(
                    0
            );
        }

        response.setRespondedAt(
                generateHistoricalResponseTime(
                        menu.getMenuDate(),
                        random
                )
        );

        return response;
    }

    private int generateExtraRotiCount(
            Random random) {

        double value =
                random.nextDouble();

        /*
         * Approximately:
         *
         * 75% no extra roti
         * 20% one extra roti
         * 5% two extra rotis
         */
        if (value < 0.75) {
            return 0;
        }

        if (value < 0.95) {
            return 1;
        }

        return 2;
    }

    private LocalDateTime generateHistoricalResponseTime(
            LocalDate menuDate,
            Random random) {

        /*
         * Generate a response between 7:00 AM
         * and 10:59 PM on the menu date.
         */
        int hour =
                7 + random.nextInt(16);

        int minute =
                random.nextInt(60);

        return LocalDateTime.of(
                menuDate,
                LocalTime.of(
                        hour,
                        minute
                )
        );
    }
}