package com.smartmess.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smartmess.backend.service.BillingJobWorkerService;

@Component
@EnableScheduling
@ConditionalOnProperty(
        name = "app.billing.automation.enabled",
        havingValue = "true"
)
public class BillingScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(BillingScheduler.class);

    private final BillingJobWorkerService billingJobWorkerService;

    private volatile boolean applicationReady;

    public BillingScheduler(
            BillingJobWorkerService billingJobWorkerService) {

        this.billingJobWorkerService = billingJobWorkerService;
    }

    /*
     * Wait until application startup and data initialization finish.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady = true;
    }

    /*
     * Fixed delay prevents overlapping invocations from this trigger.
     * Persisted job leases coordinate different application instances.
     */
    @Scheduled(
            fixedDelayString =
                    "${app.billing.automation.poll-delay-ms:60000}",
            initialDelayString =
                    "${app.billing.automation.initial-delay-ms:30000}"
    )
    public void processBillingJobs() {

        if (!applicationReady) {
            return;
        }

        try {
            billingJobWorkerService.runScheduledBilling();
        } catch (RuntimeException exception) {
            log.error(
                    "Scheduled billing run failed. "
                            + "The next scheduled run will retry.",
                    exception
            );
        }
    }
}