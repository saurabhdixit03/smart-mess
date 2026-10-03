package com.smartmess.backend.config.seed;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.MessOwnerStatus;
import com.smartmess.backend.repository.MessOwnerRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.MessConfigurationInitializer;

@Component
public class DemoMessSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(DemoMessSeeder.class);

    /*
     * Reserved sample account for optional demo data.
     *
     * These credentials are for local/demo use only.
     * The password is stored as a BCrypt hash.
     */
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

    private final MessOwnerRepository messOwnerRepository;
    private final MessRepository messRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessConfigurationInitializer messConfigurationInitializer;

    public DemoMessSeeder(
            MessOwnerRepository messOwnerRepository,
            MessRepository messRepository,
            PasswordEncoder passwordEncoder,
            MessConfigurationInitializer messConfigurationInitializer) {

        this.messOwnerRepository = messOwnerRepository;
        this.messRepository = messRepository;
        this.passwordEncoder = passwordEncoder;
        this.messConfigurationInitializer = messConfigurationInitializer;
    }

    /*
     * Creates or resolves the dedicated demo mess.
     *
     * Called only when optional demo-data seeding is enabled.
     * Does not select an arbitrary existing owner's mess.
     */
    @Transactional
    public Mess seed() {

        MessOwner existingOwner =
                messOwnerRepository
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

            Mess existingMess =
                    existingOwner.getMess();

            messConfigurationInitializer.initialize(
                    existingMess
            );

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

        Mess mess =
                new Mess();

        mess.setMessName(DEMO_MESS_NAME);
        mess.setRegistrationCode(UUID.randomUUID().toString());

        Mess savedMess =
                messRepository.save(mess);

        MessOwner owner =
                new MessOwner();

        owner.setFullName(DEMO_OWNER_NAME);
        owner.setMess(savedMess);
        owner.setMobileNumber(DEMO_OWNER_MOBILE);
        owner.setEmail(DEMO_OWNER_EMAIL);
        owner.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        owner.setStatus(MessOwnerStatus.ACTIVE);

        messOwnerRepository.save(owner);

        messConfigurationInitializer.initialize(
                savedMess
        );

        log.info(
                "Demo owner and mess created successfully. Mess ID {}.",
                savedMess.getMessId()
        );

        return savedMess;
    }
}