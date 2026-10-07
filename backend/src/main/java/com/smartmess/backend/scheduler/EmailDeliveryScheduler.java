package com.smartmess.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smartmess.backend.service.EmailDeliveryService;

@Component
@EnableScheduling
@ConditionalOnProperty(
        name = "app.mail.notifications.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class EmailDeliveryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(EmailDeliveryScheduler.class);

    private final EmailDeliveryService emailDeliveryService;

    private volatile boolean applicationReady;

    public EmailDeliveryScheduler(
            EmailDeliveryService emailDeliveryService) {

        this.emailDeliveryService = emailDeliveryService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady = true;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.mail.notifications.poll-interval-ms:60000}",
            initialDelayString =
                    "${app.mail.notifications.initial-delay-ms:30000}"
    )
    public void processEmailQueue() {

        if (!applicationReady) {
            return;
        }

        try {
            emailDeliveryService.processDueEmails();
        } catch (RuntimeException exception) {
            log.warn(
                    "Email queue polling failed; the next poll will retry."
            );
        }
    }
}