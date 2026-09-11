package com.smartmess.backend.config.seed;

import java.time.Clock;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.dto.request.GenerateBillRequest;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.service.BillService;

@Component
public class BillSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(BillSeeder.class);

    private final BillService billService;

    private final BillRepository billRepository;

    private final Clock clock;

    public BillSeeder(
            BillRepository billRepository,
            BillService billService,
            Clock clock) {

        this.billRepository = billRepository;
        this.billService = billService;
        this.clock = clock;
    }

    public void seedDemoData() {

        seedBills();
    }

    public void seedBills() {

        if (billRepository.count() > 0) {
            return;
        }

        LocalDate today =
                LocalDate.now(clock);

        GenerateBillRequest request =
                new GenerateBillRequest(
                        today.getMonthValue(),
                        today.getYear()
                );

        try {

            billService.generateBills(
                    request
            );

            log.info(
                    "Demo Bills generated successfully."
            );

        } catch (BusinessException ex) {

            log.warn(
                    "Skipping Bill seeding: {}",
                    ex.getMessage()
            );
        }
    }
}