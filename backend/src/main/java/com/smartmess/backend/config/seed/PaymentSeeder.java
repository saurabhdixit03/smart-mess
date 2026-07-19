package com.smartmess.backend.config.seed;

import java.time.LocalDateTime;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Payment;
import com.smartmess.backend.enums.BillStatus;
import com.smartmess.backend.enums.PaymentMode;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.PaymentRepository;


@Component
public class PaymentSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentSeeder.class); 
    
    private final BillRepository billRepository;

	private final PaymentRepository paymentRepository;

    public PaymentSeeder(

            PaymentRepository paymentRepository,

            BillRepository billRepository

    ) {

        this.paymentRepository = paymentRepository;

        this.billRepository = billRepository;

    }

    public void seedDemoData() {

        seedPayments();

    }
    
    private record PaymentSeed(

            int billIndex,

            PaymentMode paymentMode,

            BillStatus billStatus

    ) {
    }

    public void seedPayments() {
    	
    	if (paymentRepository.count() > 0) {
    	    return;
    	}
    
    	List<Bill> bills = billRepository.findAll();

    	if (bills.isEmpty()) {

    	    log.warn("Skipping Payment seeding because bills are missing.");

    	    return;
    	}
    	
    	List<PaymentSeed> payments = List.of(

    	        new PaymentSeed(0, PaymentMode.CASH, BillStatus.PAID),

    	        new PaymentSeed(1, PaymentMode.UPI, BillStatus.PAID),

    	        new PaymentSeed(2, PaymentMode.CASH, BillStatus.PAID),

    	        new PaymentSeed(3, PaymentMode.UPI, BillStatus.PAYMENT_PENDING),

    	        new PaymentSeed(4, PaymentMode.CASH, BillStatus.UNPAID),

    	        new PaymentSeed(5, PaymentMode.UPI, BillStatus.PAID)

    	);
    	
    	
    	for (PaymentSeed seed : payments) {
    		
    		if (seed.billIndex() >= bills.size()) {

    		    log.warn(
    		        "Skipping payment seed because bill index {} does not exist.",
    		        seed.billIndex()
    		    );

    		    continue;
    		}

    		    Bill bill = bills.get(seed.billIndex());
    		    
     		    if (seed.billStatus() == BillStatus.UNPAID) {

       		        continue;
    		    }

    		    if (seed.billStatus() == BillStatus.PAYMENT_PENDING) {

       		        bill.setBillStatus(BillStatus.PAYMENT_PENDING);

    		        billRepository.save(bill);

    		        continue;
    		    }

    		    Payment payment = new Payment();

    		    payment.setBill(bill);

    		    payment.setPaymentAmount(
    		            bill.getTotalAmount()
    		    );

    		    payment.setPaymentMode(
    		            seed.paymentMode()
    		    );

    		    payment.setPaidAt(
    		            LocalDateTime.now()
    		    );

    		    paymentRepository.save(payment);
    		    
     		    bill.setBillStatus(BillStatus.PAID);

    		    billRepository.save(bill);

    		}
    	
    	log.info("Demo Payments seeded successfully.");

    	}

}