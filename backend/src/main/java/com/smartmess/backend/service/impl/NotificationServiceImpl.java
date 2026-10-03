package com.smartmess.backend.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.smartmess.backend.dto.response.NotificationResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Notification;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.NotificationMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.NotificationRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.NotificationService;

@Service
public class NotificationServiceImpl
        implements NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final CustomerSecurity customerSecurity;

    public NotificationServiceImpl(
            CustomerRepository customerRepository,
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper,
            SimpMessagingTemplate messagingTemplate,
            CustomerSecurity customerSecurity) {

        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.messagingTemplate = messagingTemplate;
        this.customerSecurity = customerSecurity;
    }

    /*
     * Notify active customers within the authenticated mess.
     *
     * Notification broadcasts must never select customers
     * from another mess.
     */
    @Transactional
    @Override
    public void notifyActiveCustomers(
            NotificationType notificationType,
            String title,
            String message) {

        Long messId =
                customerSecurity.getCurrentMessId();

        List<Customer> activeCustomers =
                customerRepository.findAllByMess_MessIdAndStatus(
                        messId,
                        CustomerStatus.ACTIVE
                );

        for (Customer customer : activeCustomers) {

            notifyCustomer(
                    customer,
                    notificationType,
                    title,
                    message
            );
        }
    }

    /*
     * Notify one specific active customer within
     * the authenticated mess.
     *
     * Live delivery occurs only after the transaction commits.
     */
    @Transactional
    @Override
    public void notifyCustomer(
            Customer customer,
            NotificationType notificationType,
            String title,
            String message) {

        Long messId =
                customerSecurity.getCurrentMessId();

        if (customer == null
                || customer.getCustomerId() == null) {

            throw new AccessDeniedException(
                    "You do not have permission to notify this customer."
            );
        }

        Customer recipient =
                customerRepository
                        .findByCustomerIdAndMess_MessId(
                                customer.getCustomerId(),
                                messId
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have permission to notify this customer."
                                ));

        if (recipient.getStatus() != CustomerStatus.ACTIVE) {
            return;
        }

        Notification notification =
                new Notification();

        notification.setMess(recipient.getMess());
        notification.setCustomer(recipient);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);

        Notification savedNotification =
                notificationRepository.save(notification);

        NotificationResponse response =
                notificationMapper.toResponse(savedNotification);

        sendAfterCommit(
                recipient.getEmail(),
                response
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<NotificationResponse> getMyNotifications() {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                getAuthenticatedCustomer();

        List<Notification> notifications =
                notificationRepository
                        .findByMess_MessIdAndCustomerOrderByCreatedAtDesc(
                                messId,
                                customer
                        );

        return notificationMapper.toResponseList(
                notifications
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<NotificationResponse> getMyUnreadNotifications() {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                getAuthenticatedCustomer();

        List<Notification> notifications =
                notificationRepository
                        .findByMess_MessIdAndCustomerAndReadFalseOrderByCreatedAtDesc(
                                messId,
                                customer
                        );

        return notificationMapper.toResponseList(
                notifications
        );
    }

    @Transactional(readOnly = true)
    @Override
    public long getMyUnreadCount() {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                getAuthenticatedCustomer();

        return notificationRepository
                .countByMess_MessIdAndCustomerAndReadFalse(
                        messId,
                        customer
                );
    }

    @Transactional
    @Override
    public NotificationResponse markAsRead(
            Long notificationId) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                getAuthenticatedCustomer();

        Notification notification =
                notificationRepository
                        .findByNotificationIdAndMess_MessIdAndCustomer(
                                notificationId,
                                messId,
                                customer
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                ));

        if (!notification.isRead()) {

            notification.setRead(true);

            notification =
                    notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    @Override
    public void markAllAsRead() {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                getAuthenticatedCustomer();

        List<Notification> unreadNotifications =
                notificationRepository
                        .findByMess_MessIdAndCustomerAndReadFalseOrderByCreatedAtDesc(
                                messId,
                                customer
                        );

        if (unreadNotifications.isEmpty()) {
            return;
        }

        unreadNotifications.forEach(
                notification -> notification.setRead(true)
        );

        notificationRepository.saveAll(
                unreadNotifications
        );
    }

    /*
     * Resolves the authenticated customer within their mess.
     *
     * Notification reads and read-status updates remain
     * restricted to that customer's own account.
     */
    private Customer getAuthenticatedCustomer() {

        if (customerSecurity.getCurrentUserRole()
                != UserRole.CUSTOMER) {

            throw new AccessDeniedException(
                    "Only customers can access their notifications."
            );
        }

        Long customerId =
                customerSecurity.getCurrentUserId();

        Long messId =
                customerSecurity.getCurrentMessId();

        return customerRepository
                .findByCustomerIdAndMess_MessId(
                        customerId,
                        messId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: "
                                        + customerId
                        ));
    }

    /*
     * Defers live delivery until the surrounding transaction commits.
     *
     * When invoked during bill generation, this callback belongs
     * to the billing transaction and is not run after a rollback.
     */
    private void sendAfterCommit(
            String recipientEmail,
            NotificationResponse response) {

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {

                            sendLiveNotification(
                                    recipientEmail,
                                    response
                            );
                        }
                    }
            );

            return;
        }

        /*
         * Defensive fallback for execution without a surrounding
         * transaction. The repository save has already completed.
         */
        sendLiveNotification(
                recipientEmail,
                response
        );
    }

    /*
     * A failed live delivery must not report a committed
     * database operation as failed.
     *
     * The customer can still retrieve the saved notification
     * through the notification API.
     */
    private void sendLiveNotification(
            String recipientEmail,
            NotificationResponse response) {

        try {

            messagingTemplate.convertAndSendToUser(
                    recipientEmail,
                    "/queue/notifications",
                    response
            );

        } catch (RuntimeException exception) {

            log.warn(
                    "Live delivery failed for notification {}",
                    response.notificationId(),
                    exception
            );
        }
    }
}