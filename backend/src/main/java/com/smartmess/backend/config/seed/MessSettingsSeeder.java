package com.smartmess.backend.config.seed;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.repository.MessSettingsRepository;

@Component
public class MessSettingsSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MessSettingsSeeder.class
            );

    private static final LocalTime LUNCH_RESPONSE_CUTOFF =
            LocalTime.of(11, 0);

    private static final LocalTime DINNER_RESPONSE_CUTOFF =
            LocalTime.of(18, 0);

    private final MessSettingsRepository messSettingsRepository;

    public MessSettingsSeeder(
            MessSettingsRepository messSettingsRepository) {

        this.messSettingsRepository =
                messSettingsRepository;
    }

    public void seed() {

        if (messSettingsRepository.count() > 0) {

            log.info(
                    "Mess Settings already exist. Skipping demo seeding."
            );

            return;
        }

        MessSettings settings =
                new MessSettings();

        /*
         * UPI details are intentionally left empty.
         *
         * The owner must configure their own UPI ID
         * and receiver name from the Settings page
         * before enabling customer UPI payments.
         */
        settings.setUpiId(
                null
        );

        settings.setReceiverName(
                null
        );

        settings.setLunchResponseCutoff(
                LUNCH_RESPONSE_CUTOFF
        );

        settings.setDinnerResponseCutoff(
                DINNER_RESPONSE_CUTOFF
        );

        /*
         * No weekly closed day is configured initially.
         *
         * The owner can configure the weekly schedule
         * manually from the Settings page.
         */
        settings.setWeeklyClosedDay(
                null
        );

        settings.setWeeklyLunchClosed(
                false
        );

        settings.setWeeklyDinnerClosed(
                false
        );

        messSettingsRepository.save(
                settings
        );

        log.info(
                "Demo Mess Settings seeded successfully."
        );
    }
}