package com.smartmess.backend.config.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.smartmess.backend.enums.MealResponseStatus;
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

    public void seedDemoData() {

        seedMealRecords();
    }

    public void seedMealRecords() {

        if (mealRecordRepository.count() > 0) {

            log.info("Meal Records already exist. Skipping demo seeding.");

            return;
        }

        List<Customer> customers =
                customerRepository.findByStatus(
                        CustomerStatus.ACTIVE
                );

        if (customers.isEmpty()) {

            log.warn(
                    "Skipping MealRecord seeding because no active customers are available."
            );

            return;
        }

        List<Menu> menus =
                menuRepository.findAllByOrderByMenuDateAscMealSessionAsc();

        if (menus.isEmpty()) {

            log.warn(
                    "Skipping MealRecord seeding because no menus are available."
            );

            return;
        }

        MealPricing pricing =
                mealPricingRepository
                        .findTopByOrderByUpdatedAtDesc()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Meal pricing missing."
                                ));

        int recordsCreated = 0;
        int walkInRecords = 0;
        int responseBasedRecords = 0;

        /*
         * Process every historical menu.
         *
         * Example:
         *
         * July 1 Lunch
         * July 1 Dinner
         * July 2 Lunch
         * July 2 Dinner
         * ...
         * August 13 Lunch
         * August 13 Dinner
         */
        for (Menu menu : menus) {

            for (int customerIndex = 0;
                 customerIndex < customers.size();
                 customerIndex++) {

                Customer customer =
                        customers.get(customerIndex);

                MealResponse mealResponse =
                        mealResponseRepository
                                .findByCustomerAndMenu(
                                        customer,
                                        menu
                                )
                                .orElse(null);

                /*
                 * No response means there is nothing to seed
                 * for this customer/menu combination.
                 */
                if (mealResponse == null) {
                    continue;
                }

                /*
                 * Generate a deterministic collection decision.
                 *
                 * This avoids using random values, so restarting
                 * the application produces the same demo dataset.
                 */
                int pattern =
                        (menu.getMenuId().intValue()
                                + customerIndex) % 10;

                /*
                 * ACCEPTED responses:
                 *
                 * Most accepted responses are collected.
                 *
                 * Some are intentionally left uncollected to
                 * represent customers who responded but didn't
                 * actually take the meal.
                 */
                if (mealResponse.getResponseStatus()
                        == MealResponseStatus.ACCEPTED) {

                    /*
                     * 0 and 1 = not collected
                     * 2-9 = collected
                     *
                     * Roughly 80% collection rate.
                     */
                    if (pattern <= 1) {
                        continue;
                    }

                    MealOption servedMeal =
                            getServedMeal(
                                    mealResponse,
                                    pattern
                            );

                    int extraRotis =
                            getExtraRotiCount(
                                    mealResponse,
                                    pattern
                            );

                    MealRecord mealRecord =
                            createMealRecord(
                                    customer,
                                    menu,
                                    mealResponse,
                                    servedMeal,
                                    extraRotis,
                                    pricing,
                                    customerIndex
                            );

                    mealRecordRepository.save(mealRecord);

                    recordsCreated++;
                    responseBasedRecords++;

                    continue;
                }

                /*
                 * DECLINED responses:
                 *
                 * Occasionally simulate a walk-in customer.
                 *
                 * The customer declined the menu beforehand but
                 * still came and took a meal.
                 *
                 * No MealResponse is attached to that MealRecord.
                 */
                if (mealResponse.getResponseStatus()
                        == MealResponseStatus.DECLINED) {

                    /*
                     * Only a small percentage of declined
                     * responses become walk-ins.
                     */
                    if (pattern != 7) {
                        continue;
                    }

                    MealOption servedMeal =
                            customerIndex % 3 == 0
                                    ? MealOption.FULL
                                    : MealOption.HALF;

                    int extraRotis =
                            customerIndex % 4 == 0
                                    ? 1
                                    : 0;

                    MealRecord mealRecord =
                            createMealRecord(
                                    customer,
                                    menu,
                                    null,
                                    servedMeal,
                                    extraRotis,
                                    pricing,
                                    customerIndex
                            );

                    mealRecordRepository.save(mealRecord);

                    recordsCreated++;
                    walkInRecords++;
                }
            }
        }

        log.info(
                "Demo Meal Records seeded successfully. " +
                "Total: {}, Response-based: {}, Walk-ins: {}",
                recordsCreated,
                responseBasedRecords,
                walkInRecords
        );
    }

    /*
     * Occasionally simulate the owner changing the served
     * meal from what the customer originally requested.
     *
     * This gives us realistic examples such as:
     *
     * Response: FULL
     * Served:   HALF
     *
     * Response: HALF
     * Served:   FULL
     */
    private MealOption getServedMeal(
            MealResponse mealResponse,
            int pattern) {

        MealOption requestedMeal =
                mealResponse.getMealOption();

        /*
         * Pattern 3:
         * Occasionally serve the opposite meal option.
         */
        if (pattern == 3) {

            if (requestedMeal == MealOption.FULL) {
                return MealOption.HALF;
            }

            if (requestedMeal == MealOption.HALF) {
                return MealOption.FULL;
            }
        }

        return requestedMeal;
    }

    /*
     * Mostly preserve the customer's requested extra rotis.
     *
     * Occasionally add one extra roti to represent
     * an owner-side adjustment during collection.
     */
    private int getExtraRotiCount(
            MealResponse mealResponse,
            int pattern) {

        int requestedRotis =
                mealResponse.getExtraRotiCount();

        /*
         * Pattern 5:
         * Occasionally add one extra roti.
         */
        if (pattern == 5) {

            return requestedRotis + 1;
        }

        return requestedRotis;
    }

    /*
     * Creates a MealRecord while keeping the actual
     * collection timestamp aligned with the menu date.
     */
    private MealRecord createMealRecord(
            Customer customer,
            Menu menu,
            MealResponse mealResponse,
            MealOption servedMeal,
            int extraRotis,
            MealPricing pricing,
            int customerIndex) {

        BigDecimal mealPrice =
                servedMeal == MealOption.FULL
                        ? pricing.getFullMealPrice()
                        : pricing.getHalfMealPrice();

        BigDecimal extraRotiPrice =
                pricing.getExtraRotiPrice();

        BigDecimal totalAmount =
                mealPrice.add(
                        extraRotiPrice.multiply(
                                BigDecimal.valueOf(
                                        extraRotis
                                )
                        )
                );

        LocalDate menuDate =
                menu.getMenuDate();

        /*
         * Lunch collections happen around lunch time.
         * Dinner collections happen around dinner time.
         *
         * Customer index is used only to slightly vary
         * the collection timestamp.
         */
        LocalTime collectionTime;

        if (menu.getMealSession() == MealSession.LUNCH) {

            collectionTime =
                    LocalTime.of(
                            12,
                            30 + (customerIndex % 30)
                    );

        } else {

            collectionTime =
                    LocalTime.of(
                            19,
                            30 + (customerIndex % 30)
                    );
        }

        LocalDateTime collectedAt =
                LocalDateTime.of(
                        menuDate,
                        collectionTime
                );

        return MealRecord.builder()

                .customer(customer)

                .menu(menu)

                /*
                 * Response-based collection:
                 * response is linked.
                 *
                 * Walk-in:
                 * response is null.
                 */
                .mealResponse(mealResponse)

                .mealOption(servedMeal)

                .mealPrice(mealPrice)

                .extraRotiCount(extraRotis)

                .extraRotiPrice(extraRotiPrice)

                .totalAmount(totalAmount)

                .collectedAt(collectedAt)

                .build();
    }
}