package com.smartmess.backend.config.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoDataLoader
        implements CommandLineRunner {

    private final SeedDataService seedDataService;

    @Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

    @Override
    public void run(
            String... args) {

        /*
         * Required configuration is initialized
         * in every environment.
         */
        seedDataService
                .initializeRequiredConfiguration();

        /*
         * Demo operational data remains optional.
         */
        if (!seedDemoData) {
            return;
        }

        seedDataService.seedDemoData();
    }
}