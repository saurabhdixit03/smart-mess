package com.smartmess.backend.config.seed;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Mess;
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
    private final Clock clock;

    public MealRecordSeeder(
            MealRecordRepository mealRecordRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MealResponseRepository mealResponseRepository,
            MealPricingRepository mealPricingRepository,
            Clock clock) {

        this.mealRecordRepository = mealRecordRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.mealResponseRepository = mealResponseRepository;
        this.mealPricingRepository = mealPricingRepository;
        this.clock = clock;
    }

    /*
     * Seeds historical collections only for the supplied demo mess.
     */
    @Transactional
    public void seedMealRecords(Mess mess) {

        if (mess == null || mess.getMessId() == null) {
            throw new IllegalArgumentException(
                    "A persisted demo mess is required."
            );
        }

        Long messId = mess.getMessId();

        List<Customer> customers =
                customerRepository.findAllByMess_MessIdAndStatus(
                        messId,
                        CustomerStatus.ACTIVE
                );

        if (customers.isEmpty()) {
            log.warn(
                    "Skipping Meal Record seeding because no active customers "
                            + "are available for demo mess {}.",
                    messId
            );
            return;
        }

        List<Menu> menus =
                menuRepository
                        .findAllByMess_MessIdOrderByMenuDateAscMealSessionAsc(
                                messId
                        );

        if (menus.isEmpty()) {
            log.warn(
                    "Skipping Meal Record seeding because no menus "
                            + "are available for demo mess {}.",
                    messId
            );
            return;
        }

        /*
         * Preserves the existing skip-if-records-exist strategy,
         * restricted to the demo mess's menus.
         */
        for (Menu menu : menus) {
            if (!mealRecordRepository.findByMess_MessIdAndMenu(
                    messId,
                    menu
            ).isEmpty()) {
                log.info(
                        "Meal Records already exist for demo mess {}. "
                                + "Skipping demo seeding.",
                        messId
                );
                return;
            }
        }

        LocalDate today = LocalDate.now(clock);

        int recordsCreated = 0;
        int responseBasedRecords = 0;
        int walkInRecords = 0;

        for (Menu menu : menus) {

            /*
             * Today's records are intentionally not seeded.
             * Accepted customers remain available for manual testing.
             */
            if (!menu.getMenuDate().isBefore(today)) {
                continue;
            }

            for (int customerIndex = 0;
                 customerIndex < customers.size();
                 customerIndex++) {

                Customer customer = customers.get(customerIndex);

                MealResponse mealResponse =
                        mealResponseRepository
                                .findByMess_MessIdAndCustomerAndMenu(
                                        messId,
                                        customer,
                                        menu
                                )
                                .orElse(null);

                if (mealResponse == null) {
                    continue;
                }

                /*
                 * A deterministic pattern makes the seeded
                 * collection history reproducible.
                 */
                int pattern =
                        (menu.getMenuId().intValue() + customerIndex) % 10;

                if (mealResponse.getResponseStatus()
                        == MealResponseStatus.ACCEPTED) {

                    /*
                     * Approximately 80% of accepted responses
                     * become collected meal records.
                     */
                    if (pattern <= 1) {
                        continue;
                    }

                    MealOption servedMeal =
                            getServedMeal(mealResponse, pattern);

                    int extraRotis =
                            getExtraRotiCount(mealResponse, pattern);

                    MealRecord mealRecord =
                            createMealRecord(
                                    mess,
                                    customer,
                                    menu,
                                    mealResponse,
                                    servedMeal,
                                    extraRotis,
                                    customerIndex
                            );

                    mealRecordRepository.save(mealRecord);

                    recordsCreated++;
                    responseBasedRecords++;
                    continue;
                }

                /*
                 * Some declined responses become walk-in records.
                 * These records are not linked to the declined response.
                 */
                if (mealResponse.getResponseStatus()
                        == MealResponseStatus.DECLINED
                        && pattern == 7) {

                    MealOption servedMeal =
                            customerIndex % 3 == 0
                                    ? MealOption.FULL
                                    : MealOption.HALF;

                    int extraRotis =
                            customerIndex % 4 == 0 ? 1 : 0;

                    MealRecord mealRecord =
                            createMealRecord(
                                    mess,
                                    customer,
                                    menu,
                                    null,
                                    servedMeal,
                                    extraRotis,
                                    customerIndex
                            );

                    mealRecordRepository.save(mealRecord);

                    recordsCreated++;
                    walkInRecords++;
                }
            }
        }

        log.info(
                "Demo Meal Records seeded successfully for mess {}. "
                        + "Total: {}, Response-based: {}, Walk-ins: {}.",
                messId,
                recordsCreated,
                responseBasedRecords,
                walkInRecords
        );
    }

    private MealOption getServedMeal(
            MealResponse mealResponse,
            int pattern) {

        MealOption requestedMeal =
                mealResponse.getMealOption();

        /*
         * Occasionally serve the opposite meal option
         * to represent an owner-side adjustment.
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

    private int getExtraRotiCount(
            MealResponse mealResponse,
            int pattern) {

        int requestedRotis =
                mealResponse.getExtraRotiCount();

        if (pattern == 5) {
            return requestedRotis + 1;
        }

        return requestedRotis;
    }

    private MealRecord createMealRecord(
            Mess mess,
            Customer customer,
            Menu menu,
            MealResponse mealResponse,
            MealOption servedMeal,
            int extraRotis,
            int customerIndex) {

        LocalTime collectionTime =
                getCollectionTime(
                        menu.getMealSession(),
                        customerIndex
                );

        LocalDateTime collectedAt =
                LocalDateTime.of(
                        menu.getMenuDate(),
                        collectionTime
                );

        /*
         * Historical demo meals use the pricing effective
         * at their collection time, excluding later changes.
         */
        MealPricing pricing =
                mealPricingRepository
                        .findTopByMess_MessIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                                mess.getMessId(),
                                collectedAt
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Meal pricing is missing for demo mess "
                                                + mess.getMessId()
                                                + " at "
                                                + collectedAt
                                                + "."
                                )
                        );

        BigDecimal mealPrice =
                servedMeal == MealOption.FULL
                        ? pricing.getFullMealPrice()
                        : pricing.getHalfMealPrice();

        BigDecimal extraRotiPrice =
                pricing.getExtraRotiPrice();

        BigDecimal totalAmount =
                mealPrice.add(
                        extraRotiPrice.multiply(
                                BigDecimal.valueOf(extraRotis)
                        )
                );

        return MealRecord.builder()
                .mess(mess)
                .customer(customer)
                .menu(menu)
                .mealResponse(mealResponse)
                .mealOption(servedMeal)
                .mealPrice(mealPrice)
                .extraRotiCount(extraRotis)
                .extraRotiPrice(extraRotiPrice)
                .totalAmount(totalAmount)
                .collectedAt(collectedAt)
                .build();
    }

    private LocalTime getCollectionTime(
            MealSession mealSession,
            int customerIndex) {

        int minute = 30 + (customerIndex % 30);

        if (mealSession == MealSession.LUNCH) {
            return LocalTime.of(12, minute);
        }

        return LocalTime.of(19, minute);
    }
}