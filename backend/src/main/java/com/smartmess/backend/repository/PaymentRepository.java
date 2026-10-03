package com.smartmess.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Payment;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    /*
     * Finds a payment only within the specified mess.
     */
    Optional<Payment> findByPaymentIdAndMess_MessId(
            Long paymentId,
            Long messId
    );

    /*
     * Finds the payment for a bill within the same mess.
     */
    Optional<Payment> findByMess_MessIdAndBill(
            Long messId,
            Bill bill
    );

    /*
     * Checks whether payment has already been collected
     * for a bill within the specified mess.
     */
    boolean existsByMess_MessIdAndBill(
            Long messId,
            Bill bill
    );
}