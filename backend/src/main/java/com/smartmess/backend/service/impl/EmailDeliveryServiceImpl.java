package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.EmailDelivery;
import com.smartmess.backend.entity.Notification;
import com.smartmess.backend.enums.EmailDeliveryStatus;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.repository.EmailDeliveryRepository;
import com.smartmess.backend.service.EmailDeliveryService;
import com.smartmess.backend.service.EmailService;

import jakarta.persistence.EntityManager;

@Service
public class EmailDeliveryServiceImpl implements EmailDeliveryService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailDeliveryServiceImpl.class);

    private final EmailDeliveryRepository emailDeliveryRepository;
    private final EmailService emailService;
    private final EntityManager entityManager;
    private final Clock clock;
    private final TransactionTemplate readTransaction;
    private final TransactionTemplate writeTransaction;

    public EmailDeliveryServiceImpl(
            EmailDeliveryRepository emailDeliveryRepository,
            EmailService emailService,
            EntityManager entityManager,
            Clock clock,
            PlatformTransactionManager transactionManager) {

        this.emailDeliveryRepository = emailDeliveryRepository;
        this.emailService = emailService;
        this.entityManager = entityManager;
        this.clock = clock;

        this.readTransaction =
                new TransactionTemplate(transactionManager);

        this.readTransaction.setReadOnly(true);
        this.readTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        this.writeTransaction =
                new TransactionTemplate(transactionManager);

        this.writeTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
    }

    /*
     * The notification and its email job commit together.
     * No email is sent from this transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void queueNotificationEmail(Notification notification) {

        if (notification == null
                || notification.getNotificationId() == null) {

            throw new IllegalArgumentException(
                    "A persisted notification is required."
            );
        }

        Notification savedNotification = entityManager.find(
                Notification.class,
                notification.getNotificationId()
        );

        if (savedNotification == null) {
            throw new IllegalArgumentException(
                    "Notification not found."
            );
        }

        NotificationType type =
                savedNotification.getNotificationType();

        if (!supportsEmail(type)) {
            return;
        }

        Customer customer = savedNotification.getCustomer();

        if (savedNotification.getMess() == null
                || customer == null
                || customer.getMess() == null
                || savedNotification.getMess().getMessId() == null
                || !savedNotification.getMess().getMessId()
                        .equals(customer.getMess().getMessId())) {

            throw new IllegalArgumentException(
                    "Notification and customer must belong to the same mess."
            );
        }

        /*
         * Lock the notification before checking for an existing job.
         * This also serializes concurrent attempts to queue this event.
         */
        entityManager.lock(
                savedNotification,
                jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
        );

        if (emailDeliveryRepository.existsByNotification_NotificationId(
                savedNotification.getNotificationId()
        )) {
            return;
        }

        EmailDelivery delivery = new EmailDelivery();

        delivery.setMess(savedNotification.getMess());
        delivery.setCustomer(customer);
        delivery.setNotification(savedNotification);
        delivery.setRecipientEmail(customer.getEmail());
        delivery.setCustomerName(orEmpty(customer.getFullName()));
        delivery.setMessName(orEmpty(customer.getMess().getMessName()));
        delivery.setNotificationType(type);
        delivery.setTitle(orEmpty(savedNotification.getTitle()));
        delivery.setMessage(orEmpty(savedNotification.getMessage()));
        delivery.setDeliveryStatus(EmailDeliveryStatus.PENDING);
        delivery.setAttemptCount(0);
        delivery.setNextAttemptAt(LocalDateTime.now(clock));

        emailDeliveryRepository.save(delivery);
    }

    /*
     * Network calls occur between short database transactions.
     * Multiple application instances can safely compete for jobs.
     */
    @Override
    public synchronized void processDueEmails() {

        List<Long> deliveryIds = readTransaction.execute(status ->
                emailDeliveryRepository.findDueDeliveryIds(
                        LocalDateTime.now(clock),
                        PageRequest.of(0, 10)
                )
        );

        if (deliveryIds == null) {
            return;
        }

        for (Long deliveryId : deliveryIds) {
            try {
                processDelivery(deliveryId);
            } catch (RuntimeException exception) {
                /*
                 * Do not log provider responses or recipient details.
                 * An unfinished claim becomes eligible after lease expiry.
                 */
                log.warn(
                        "Email queue processing could not finish for delivery {}.",
                        deliveryId
                );
            }
        }
    }

    private void processDelivery(Long deliveryId) {

        ClaimedEmail claimed = writeTransaction.execute(status ->
                claimDelivery(deliveryId)
        );

        if (claimed == null) {
            return;
        }

        try {
            emailService.sendCustomerNotificationEmail(
                    claimed.recipientEmail(),
                    claimed.customerName(),
                    claimed.messName(),
                    claimed.notificationType(),
                    claimed.title(),
                    claimed.message()
            );
        } catch (RuntimeException exception) {
            writeTransaction.executeWithoutResult(status ->
                    recordFailure(claimed)
            );
            return;
        }

        writeTransaction.executeWithoutResult(status ->
                recordSuccess(claimed)
        );
    }

    private ClaimedEmail claimDelivery(Long deliveryId) {

        EmailDelivery delivery = emailDeliveryRepository
                .findByIdForUpdate(deliveryId)
                .orElse(null);

        if (delivery == null
                || delivery.getDeliveryStatus() == EmailDeliveryStatus.SENT) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        if (delivery.getNextAttemptAt() != null
                && delivery.getNextAttemptAt().isAfter(now)) {
            return null;
        }

        if (delivery.getLeaseExpiresAt() != null
                && delivery.getLeaseExpiresAt().isAfter(now)) {
            return null;
        }

        validateOwnership(delivery);

        String leaseToken = UUID.randomUUID().toString();

        int attempts = delivery.getAttemptCount() == null
                ? 0
                : delivery.getAttemptCount();

        delivery.setAttemptCount(
                attempts == Integer.MAX_VALUE
                        ? Integer.MAX_VALUE
                        : attempts + 1
        );

        delivery.setDeliveryStatus(EmailDeliveryStatus.SENDING);
        delivery.setLeaseToken(leaseToken);
        delivery.setLeaseExpiresAt(now.plusMinutes(5));
        delivery.setLastAttemptAt(now);

        emailDeliveryRepository.saveAndFlush(delivery);

        return new ClaimedEmail(
                delivery.getEmailDeliveryId(),
                leaseToken,
                delivery.getRecipientEmail(),
                delivery.getCustomerName(),
                delivery.getMessName(),
                delivery.getNotificationType(),
                delivery.getTitle(),
                delivery.getMessage()
        );
    }

    private void recordSuccess(ClaimedEmail claimed) {

        EmailDelivery delivery = emailDeliveryRepository
                .findByIdForUpdate(claimed.deliveryId())
                .orElse(null);

        if (!ownsClaim(delivery, claimed)) {
            return;
        }

        delivery.setDeliveryStatus(EmailDeliveryStatus.SENT);
        delivery.setSentAt(LocalDateTime.now(clock));
        delivery.setNextAttemptAt(null);
        delivery.setLastError(null);
        clearLease(delivery);

        emailDeliveryRepository.save(delivery);
    }

    private void recordFailure(ClaimedEmail claimed) {

        EmailDelivery delivery = emailDeliveryRepository
                .findByIdForUpdate(claimed.deliveryId())
                .orElse(null);

        if (!ownsClaim(delivery, claimed)) {
            return;
        }

        int attempts = delivery.getAttemptCount();

        long delayMinutes = Math.min(
                60L,
                1L << Math.min(Math.max(attempts - 1, 0), 6)
        );

        delivery.setDeliveryStatus(EmailDeliveryStatus.FAILED);
        delivery.setNextAttemptAt(
                LocalDateTime.now(clock).plusMinutes(delayMinutes)
        );
        delivery.setLastError(
                "Email sending failed or provider acceptance could not be confirmed."
        );
        clearLease(delivery);

        emailDeliveryRepository.save(delivery);

        log.warn(
                "Email delivery {} failed on attempt {}; retry in {} minutes.",
                delivery.getEmailDeliveryId(),
                attempts,
                delayMinutes
        );
    }

    private void validateOwnership(EmailDelivery delivery) {

        Notification notification = delivery.getNotification();
        Customer customer = delivery.getCustomer();

        if (delivery.getMess() == null
                || delivery.getMess().getMessId() == null
                || notification == null
                || notification.getMess() == null
                || notification.getCustomer() == null
                || customer == null
                || customer.getMess() == null
                || !delivery.getMess().getMessId()
                        .equals(notification.getMess().getMessId())
                || !delivery.getMess().getMessId()
                        .equals(customer.getMess().getMessId())
                || !customer.getCustomerId()
                        .equals(notification.getCustomer().getCustomerId())
                || !supportsEmail(delivery.getNotificationType())
                || delivery.getNotificationType()
                        != notification.getNotificationType()) {

            throw new IllegalStateException(
                    "Email delivery ownership or notification type is invalid."
            );
        }
    }

    private boolean ownsClaim(
            EmailDelivery delivery,
            ClaimedEmail claimed) {

        return delivery != null
                && delivery.getDeliveryStatus() == EmailDeliveryStatus.SENDING
                && claimed.leaseToken().equals(delivery.getLeaseToken());
    }

    private void clearLease(EmailDelivery delivery) {
        delivery.setLeaseToken(null);
        delivery.setLeaseExpiresAt(null);
    }

    private boolean supportsEmail(NotificationType type) {
        return type == NotificationType.BILL_GENERATED
                || type == NotificationType.PAYMENT_RECEIVED
                || type == NotificationType.MEAL_PRICING;
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ClaimedEmail(
            Long deliveryId,
            String leaseToken,
            String recipientEmail,
            String customerName,
            String messName,
            NotificationType notificationType,
            String title,
            String message) {
    }
}