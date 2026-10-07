package com.smartmess.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smartmess.backend.service.PaymentOrderService;

@Component
@EnableScheduling
@ConditionalOnProperty(
        prefix = "app.cashfree",
        name = "enabled",
        havingValue = "true"
)
public class PaymentReconciliationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentReconciliationScheduler.class
            );

    private final PaymentOrderService paymentOrderService;

    private volatile boolean applicationReady;

    public PaymentReconciliationScheduler(
            PaymentOrderService paymentOrderService) {

        this.paymentOrderService = paymentOrderService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady = true;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.cashfree.reconciliation.poll-interval-ms:60000}",
            initialDelayString =
                    "${app.cashfree.reconciliation.initial-delay-ms:30000}"
    )
    public void reconcilePayments() {

        if (!applicationReady) {
            return;
        }

        try {
            paymentOrderService.reconcileDueOrders();

        } catch (RuntimeException exception) {
            log.warn(
                    "Payment reconciliation could not complete this cycle."
            );
        }
    }
}