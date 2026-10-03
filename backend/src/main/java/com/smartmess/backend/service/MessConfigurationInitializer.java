package com.smartmess.backend.service;

import java.math.BigDecimal;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.repository.MessSettingsRepository;

@Service
public class MessConfigurationInitializer {

    private static final LocalTime LUNCH_RESPONSE_CUTOFF =
            LocalTime.of(11, 0);

    private static final LocalTime DINNER_RESPONSE_CUTOFF =
            LocalTime.of(18, 0);

    private static final BigDecimal HALF_MEAL_PRICE =
            new BigDecimal("60.00");

    private static final BigDecimal FULL_MEAL_PRICE =
            new BigDecimal("80.00");

    private static final BigDecimal EXTRA_ROTI_PRICE =
            new BigDecimal("10.00");

    private final MessSettingsRepository messSettingsRepository;
    private final MealPricingRepository mealPricingRepository;

    public MessConfigurationInitializer(
            MessSettingsRepository messSettingsRepository,
            MealPricingRepository mealPricingRepository) {

        this.messSettingsRepository = messSettingsRepository;
        this.mealPricingRepository = mealPricingRepository;
    }

    /*
     * Required application configuration for one mess.
     *
     * Preserves the existing application's initial settings
     * and prices without overwriting configuration already present.
     */
    @Transactional
    public void initialize(Mess mess) {

        if (mess == null || mess.getMessId() == null) {

            throw new IllegalArgumentException(
                    "A persisted mess is required to initialize configuration."
            );
        }

        initializeSettings(mess);
        initializePricing(mess);
    }

    /*
     * UPI details remain empty until the owner configures them.
     * No weekly closed day is configured initially.
     */
    private void initializeSettings(Mess mess) {

        if (messSettingsRepository.existsByMess_MessId(
                mess.getMessId()
        )) {
            return;
        }

        MessSettings settings =
                new MessSettings();

        settings.setMess(mess);
        settings.setUpiId(null);
        settings.setReceiverName(null);
        settings.setLunchResponseCutoff(LUNCH_RESPONSE_CUTOFF);
        settings.setDinnerResponseCutoff(DINNER_RESPONSE_CUTOFF);
        settings.setWeeklyClosedDay(null);
        settings.setWeeklyLunchClosed(false);
        settings.setWeeklyDinnerClosed(false);

        messSettingsRepository.save(settings);
    }

    /*
     * Preserves the existing initial prices.
     *
     * Owners can update their own pricing through the existing API.
     */
    private void initializePricing(Mess mess) {

        if (mealPricingRepository.findByMess_MessId(
                mess.getMessId()
        ).isPresent()) {
            return;
        }

        MealPricing pricing =
                new MealPricing();

        pricing.setMess(mess);
        pricing.setHalfMealPrice(HALF_MEAL_PRICE);
        pricing.setFullMealPrice(FULL_MEAL_PRICE);
        pricing.setExtraRotiPrice(EXTRA_ROTI_PRICE);

        mealPricingRepository.save(pricing);
    }
}