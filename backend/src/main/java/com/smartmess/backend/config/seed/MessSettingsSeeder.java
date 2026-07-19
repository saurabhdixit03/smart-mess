package com.smartmess.backend.config.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.repository.MessSettingsRepository;

@Component
public class MessSettingsSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(MessSettingsSeeder.class);

    private final MessSettingsRepository messSettingsRepository;

    public MessSettingsSeeder(MessSettingsRepository messSettingsRepository) {
        this.messSettingsRepository = messSettingsRepository;
    }

    public void seed() {

        if (messSettingsRepository.count() > 0) {
            return;
        }

        MessSettings settings = new MessSettings();

        settings.setReceiverName("Smart Mess");

        settings.setUpiId("smartmess@upi");

        messSettingsRepository.save(settings);

        log.info("Demo Mess Settings seeded successfully.");
    }
}