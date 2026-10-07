package com.smartmess.backend.config.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.MessOwnerStatus;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.repository.MessOwnerRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.MessConfigurationInitializer;

@Component
public class DemoMessSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(DemoMessSeeder.class);

    private static final String DEMO_OWNER_EMAIL =
            "demo.owner@example.com";

    private static final String DEMO_OWNER_MOBILE =
            "9876500099";

    private static final String DEMO_OWNER_NAME =
            "Demo Owner";

    private static final String DEMO_MESS_NAME =
            "Annapurna Demo Mess";

    private static final String DEMO_PASSWORD =
            "Password@123";

    private static final LocalDateTime BASELINE_EFFECTIVE_FROM =
            LocalDateTime.of(1970, 1, 1, 0, 0);

    private final MessOwnerRepository messOwnerRepository;
    private final MessRepository messRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessConfigurationInitializer messConfigurationInitializer;
    private final MealPricingRepository mealPricingRepository;

    public DemoMessSeeder(
            MessOwnerRepository messOwnerRepository,
            MessRepository messRepository,
            PasswordEncoder passwordEncoder,
            MessConfigurationInitializer messConfigurationInitializer,
            MealPricingRepository mealPricingRepository) {

        this.messOwnerRepository = messOwnerRepository;
        this.messRepository = messRepository;
        this.passwordEncoder = passwordEncoder;
        this.messConfigurationInitializer = messConfigurationInitializer;
        this.mealPricingRepository = mealPricingRepository;
    }

    /*
     * Called by the optional demo-data workflow.
     * Resolves only the dedicated demo account and mess.
     */
    @Transactional
    public Mess seed() {

        MessOwner existingOwner = messOwnerRepository
                .findByEmail(DEMO_OWNER_EMAIL)
                .orElse(null);

        if (existingOwner != null) {

            if (!DEMO_OWNER_MOBILE.equals(
                    existingOwner.getMobileNumber()
            )
                    || existingOwner.getMess() == null
                    || !DEMO_MESS_NAME.equals(
                            existingOwner.getMess().getMessName()
                    )) {

                throw new IllegalStateException(
                        "The reserved demo owner email is associated with an unexpected account."
                );
            }

            Mess existingMess = existingOwner.getMess();

            initializeDemoConfiguration(existingMess);

            log.info(
                    "Demo mess already exists. Reusing mess ID {}.",
                    existingMess.getMessId()
            );

            return existingMess;
        }

        if (messOwnerRepository.existsByMobileNumber(
                DEMO_OWNER_MOBILE
        )) {
            throw new IllegalStateException(
                    "The reserved demo owner mobile number is already in use."
            );
        }

        Mess mess = new Mess();

        mess.setMessName(DEMO_MESS_NAME);
        mess.setRegistrationCode(UUID.randomUUID().toString());

        Mess savedMess = messRepository.save(mess);

        MessOwner owner = new MessOwner();

        owner.setFullName(DEMO_OWNER_NAME);
        owner.setMess(savedMess);
        owner.setMobileNumber(DEMO_OWNER_MOBILE);
        owner.setEmail(DEMO_OWNER_EMAIL);
        owner.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        owner.setStatus(MessOwnerStatus.ACTIVE);

        messOwnerRepository.save(owner);

        initializeDemoConfiguration(savedMess);

        log.info(
                "Demo owner and mess created successfully. Mess ID {}.",
                savedMess.getMessId()
        );

        return savedMess;
    }

    private void initializeDemoConfiguration(Mess demoMess) {

        messConfigurationInitializer.initialize(demoMess);

        /*
         * Preserve existing demo pricing versions.
         * Sample prices are created only for this demo mess.
         */
        if (mealPricingRepository.existsByMess_MessId(
                demoMess.getMessId()
        )) {
            return;
        }

        MealPricing pricing = new MealPricing();

        pricing.setMess(demoMess);
        pricing.setEffectiveFrom(BASELINE_EFFECTIVE_FROM);
        pricing.setHalfMealPrice(new BigDecimal("60.00"));
        pricing.setFullMealPrice(new BigDecimal("80.00"));
        pricing.setExtraRotiPrice(new BigDecimal("10.00"));

        mealPricingRepository.save(pricing);
    }
}