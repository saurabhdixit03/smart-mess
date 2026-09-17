package com.smartmess.backend.service.impl;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.response.NotificationResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Notification;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.NotificationMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.NotificationRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.NotificationService;

@Service
public class NotificationServiceImpl
        implements NotificationService {

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

    @Override
    public void notifyActiveCustomers(
            NotificationType notificationType,
            String title,
            String message) {

        List<Customer> activeCustomers =
                customerRepository.findAllByStatus(
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
     * Notify one specific active customer.
     */
    @Override
    public void notifyCustomer(
            Customer customer,
            NotificationType notificationType,
            String title,
            String message) {

        if (customer.getStatus()
                != CustomerStatus.ACTIVE) {

            return;
        }

        Notification notification =
                new Notification();

        notification.setCustomer(customer);
        notification.setNotificationType(
                notificationType
        );
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);

        Notification savedNotification =
                notificationRepository.save(
                        notification
                );

        NotificationResponse response =
                notificationMapper.toResponse(
                        savedNotification
                );

        messagingTemplate.convertAndSendToUser(
                customer.getEmail(),
                "/queue/notifications",
                response
        );
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {

        Customer customer =
                getAuthenticatedCustomer();

        List<Notification> notifications =
                notificationRepository
                        .findByCustomerOrderByCreatedAtDesc(
                                customer
                        );

        return notificationMapper.toResponseList(
                notifications
        );
    }

    @Override
    public List<NotificationResponse>
            getMyUnreadNotifications() {

        Customer customer =
                getAuthenticatedCustomer();

        List<Notification> notifications =
                notificationRepository
                        .findByCustomerAndReadFalseOrderByCreatedAtDesc(
                                customer
                        );

        return notificationMapper.toResponseList(
                notifications
        );
    }

    @Override
    public long getMyUnreadCount() {

        Customer customer =
                getAuthenticatedCustomer();

        return notificationRepository
                .countByCustomerAndReadFalse(
                        customer
                );
    }

    @Override
    public NotificationResponse markAsRead(
            Long notificationId) {

        Customer customer =
                getAuthenticatedCustomer();

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                )
                        );

        if (!notification.getCustomer()
                .getCustomerId()
                .equals(customer.getCustomerId())) {

            throw new AccessDeniedException(
                    "You do not have permission to access this notification."
            );
        }

        if (!notification.isRead()) {

            notification.setRead(true);

            notification =
                    notificationRepository.save(
                            notification
                    );
        }

        return notificationMapper.toResponse(
                notification
        );
    }

    @Override
    public void markAllAsRead() {

        Customer customer =
                getAuthenticatedCustomer();

        List<Notification> unreadNotifications =
                notificationRepository
                        .findByCustomerAndReadFalseOrderByCreatedAtDesc(
                                customer
                        );

        if (unreadNotifications.isEmpty()) {
            return;
        }

        unreadNotifications.forEach(
                notification ->
                        notification.setRead(true)
        );

        notificationRepository.saveAll(
                unreadNotifications
        );
    }

    private Customer getAuthenticatedCustomer() {

        Long customerId =
                customerSecurity.getCurrentUserId();

        return customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: "
                                        + customerId
                        )
                );
    }
}