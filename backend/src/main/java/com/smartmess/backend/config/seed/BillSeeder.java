package com.smartmess.backend.config.seed;

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

    private final BillService billService;
    
    private final BillRepository billRepository;

    public BillSeeder(
            BillRepository billRepository,
            BillService billService) {

        this.billRepository = billRepository;
        this.billService = billService;
    }
    
    private static final Logger log =
            LoggerFactory.getLogger(BillSeeder.class);
    
    public void seedDemoData() {

        seedBills();
    }

    public void seedBills() {

        if (billRepository.count() > 0) {
            return;
        }

        GenerateBillRequest request =
                new GenerateBillRequest(
                        LocalDate.now().getMonthValue(),
                        LocalDate.now().getYear()
                );

        try {

            billService.generateBills(request);

            log.info("Demo Bills generated successfully.");

        } catch (BusinessException ex) {

            log.warn("Skipping Bill seeding: {}", ex.getMessage());

        }
    }

}