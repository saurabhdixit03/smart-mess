package com.smartmess.backend.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.repository.MessSettingsRepository;

@Service
public class MessConfigurationInitializer {

    private static final LocalTime LUNCH_RESPONSE_CUTOFF =
            LocalTime.of(11, 0);

    private static final LocalTime DINNER_RESPONSE_CUTOFF =
            LocalTime.of(18, 0);

    private final MessSettingsRepository messSettingsRepository;

    public MessConfigurationInitializer(
            MessSettingsRepository messSettingsRepository) {

        this.messSettingsRepository = messSettingsRepository;
    }

    /*
     * Initialize operational settings for a persisted mess.
     * Meal prices must be configured by its owner.
     */
    @Transactional
    public void initialize(Mess mess) {

        if (mess == null || mess.getMessId() == null) {
            throw new IllegalArgumentException(
                    "A persisted mess is required to initialize configuration."
            );
        }

        if (messSettingsRepository.existsByMess_MessId(
                mess.getMessId()
        )) {
            return;
        }

        MessSettings settings = new MessSettings();

        settings.setMess(mess);
        settings.setLunchResponseCutoff(LUNCH_RESPONSE_CUTOFF);
        settings.setDinnerResponseCutoff(DINNER_RESPONSE_CUTOFF);
        settings.setWeeklyClosedDay(null);
        settings.setWeeklyLunchClosed(false);
        settings.setWeeklyDinnerClosed(false);

        messSettingsRepository.save(settings);
    }
}