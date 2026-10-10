package com.smartmess.backend.service.impl;

import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.smartmess.backend.dto.response.NotificationResponse;
import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.BillingJob;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Notification;
import com.smartmess.backend.entity.Payment;
import com.smartmess.backend.entity.PaymentOrder;
import com.smartmess.backend.enums.BillingJobStatus;
import com.smartmess.backend.enums.BillStatus;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.enums.PaymentMode;
import com.smartmess.backend.enums.PaymentOrderStatus;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.NotificationMapper;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.BillingJobRepository;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.NotificationRepository;
import com.smartmess.backend.repository.PaymentOrderRepository;
import com.smartmess.backend.repository.PaymentRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.EmailDeliveryService;
import com.smartmess.backend.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final CustomerSecurity customerSecurity;
    private final BillRepository billRepository;
    private final BillingJobRepository billingJobRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentRepository paymentRepository;
    private final EmailDeliveryService emailDeliveryService;
    private final Clock clock;

    public NotificationServiceImpl(
            CustomerRepository customerRepository,
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper,
            SimpMessagingTemplate messagingTemplate,
            CustomerSecurity customerSecurity,
            BillRepository billRepository,
            BillingJobRepository billingJobRepository,
            PaymentOrderRepository paymentOrderRepository,
            PaymentRepository paymentRepository,
            EmailDeliveryService emailDeliveryService,
            Clock clock) {

        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.messagingTemplate = messagingTemplate;
        this.customerSecurity = customerSecurity;
        this.billRepository = billRepository;
        this.billingJobRepository = billingJobRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentRepository = paymentRepository;
        this.emailDeliveryService = emailDeliveryService;
        this.clock = clock;
    }

    @Transactional
    @Override
    public void notifyActiveCustomers(
            NotificationType notificationType,
            String title,
            String message) {

        Long messId = customerSecurity.getCurrentMessId();

        List<Customer> activeCustomers = customerRepository
                .findAllByMess_MessIdAndStatus(
                        messId,
                        CustomerStatus.ACTIVE
                );

        for (Customer customer : activeCustomers) {
            notifyCustomer(customer, notificationType, title, message);
        }
    }

    @Transactional
    @Override
    public void notifyCustomer(
            Customer customer,
            NotificationType notificationType,
            String title,
            String message) {

        Long messId = customerSecurity.getCurrentMessId();

        if (customer == null || customer.getCustomerId() == null) {
            throw notificationAccessDenied();
        }

        Customer recipient = customerRepository
                .findByCustomerIdAndMess_MessId(
                        customer.getCustomerId(),
                        messId
                )
                .orElseThrow(this::notificationAccessDenied);

        if (!canReceiveNotification(recipient, notificationType)) {
            return;
        }

        saveNotification(recipient, notificationType, title, message);
    }

    /*
     * Joins the transaction creating the automated bill.
     * No authenticated owner session is required.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void notifyAutomatedBill(
            Long jobId,
            String leaseToken,
            Long billId) {

        if (jobId == null || leaseToken == null || billId == null) {
            throw notificationAccessDenied();
        }

        BillingJob job = billingJobRepository
                .findByIdForUpdate(jobId)
                .orElseThrow(this::notificationAccessDenied);

        LocalDateTime now = LocalDateTime.now(clock);

        if (job.getStatus() != BillingJobStatus.RUNNING
                || !leaseToken.equals(job.getLeaseToken())
                || job.getLeaseExpiresAt() == null
                || !job.getLeaseExpiresAt().isAfter(now)) {

            throw new AccessDeniedException(
                    "Automated billing job lease is not valid."
            );
        }

        Long messId = job.getMess().getMessId();

        Bill bill = billRepository
                .findByBillIdAndMess_MessId(billId, messId)
                .orElseThrow(this::notificationAccessDenied);

        if (!job.getBillingMonth().equals(bill.getBillingMonth())
                || !job.getBillingYear().equals(bill.getBillingYear())) {

            throw new AccessDeniedException(
                    "Bill does not belong to the automated billing period."
            );
        }

        Customer recipient = customerRepository
                .findByCustomerIdAndMess_MessId(
                        bill.getCustomer().getCustomerId(),
                        messId
                )
                .orElseThrow(this::notificationAccessDenied);

        if (!canReceiveNotification(
                recipient,
                NotificationType.BILL_GENERATED)) {
            return;
        }

        String billingPeriod = Month.of(bill.getBillingMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " "
                + bill.getBillingYear();

        String message = "Your bill for "
                + billingPeriod
                + " is ready. Amount due: "
                + formatAmount(bill.getTotalAmount())
                + ". View your bill for meal details.";

        saveNotification(
                recipient,
                NotificationType.BILL_GENERATED,
                "New Bill Generated",
                message
        );
    }

    /*
     * Called once by settlement, inside its transaction.
     * Validates persisted payment evidence without using JWT context.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void notifyVerifiedPayment(Long paymentOrderId) {

        if (paymentOrderId == null) {
            throw notificationAccessDenied();
        }

        PaymentOrder reference = paymentOrderRepository
                .findById(paymentOrderId)
                .orElseThrow(this::notificationAccessDenied);

        Long messId = reference.getMess().getMessId();
        Long billId = reference.getBill().getBillId();

        /*
         * Preserve settlement's lock order: bill first, order second.
         */
        Bill bill = billRepository
                .findByBillIdAndMessIdForUpdate(billId, messId)
                .orElseThrow(this::notificationAccessDenied);

        PaymentOrder order = paymentOrderRepository
                .findByIdAndMessIdForUpdate(paymentOrderId, messId)
                .orElseThrow(this::notificationAccessDenied);

        Payment payment = paymentRepository
                .findByMess_MessIdAndBill(messId, bill)
                .orElseThrow(this::notificationAccessDenied);

        if (!billId.equals(order.getBill().getBillId())
                || !messId.equals(bill.getCustomer().getMess().getMessId())
                || !messId.equals(payment.getMess().getMessId())
                || order.getProvider() != PaymentMode.CASHFREE
                || payment.getPaymentMode() != PaymentMode.CASHFREE
                || order.getOrderStatus() != PaymentOrderStatus.PAID
                || bill.getBillStatus() != BillStatus.PAID
                || order.getGatewayPaymentId() == null
                || order.getGatewayPaymentId().isBlank()
                || order.getPaidAt() == null
                || payment.getPaidAt() == null
                || !order.getPaidAt().equals(payment.getPaidAt())
                || order.getOrderAmount() == null
                || payment.getPaymentAmount() == null
                || bill.getTotalAmount() == null
                || order.getOrderAmount()
                        .compareTo(payment.getPaymentAmount()) != 0
                || order.getOrderAmount()
                        .compareTo(bill.getTotalAmount()) != 0) {

            throw new AccessDeniedException(
                    "Verified payment notification evidence is invalid."
            );
        }

        Customer recipient = customerRepository
                .findByCustomerIdAndMess_MessId(
                        bill.getCustomer().getCustomerId(),
                        messId
                )
                .orElseThrow(this::notificationAccessDenied);

        if (!canReceiveNotification(
                recipient,
                NotificationType.PAYMENT_RECEIVED)) {
            return;
        }

        String title = "Payment Received";
        String prefix = "";

        if ("SANDBOX".equals(order.getEnvironment())) {
            title = "Test Payment Successful";
            prefix = "Sandbox test payment. No real money was charged. ";
        }

        String billingPeriod = Month.of(bill.getBillingMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " "
                + bill.getBillingYear();

        saveNotification(
                recipient,
                NotificationType.PAYMENT_RECEIVED,
                title,
                prefix
                        + "Your payment of "
                        + formatAmount(payment.getPaymentAmount())
                        + " for "
                        + billingPeriod
                        + " has been verified successfully."
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<NotificationResponse> getMyNotifications() {

        Long messId = customerSecurity.getCurrentMessId();
        Customer customer = getAuthenticatedCustomer();

        return notificationMapper.toResponseList(
                notificationRepository
                        .findByMess_MessIdAndCustomerOrderByCreatedAtDesc(
                                messId, customer
                        )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<NotificationResponse> getMyUnreadNotifications() {

        Long messId = customerSecurity.getCurrentMessId();
        Customer customer = getAuthenticatedCustomer();

        return notificationMapper.toResponseList(
                notificationRepository
                        .findByMess_MessIdAndCustomerAndReadFalseOrderByCreatedAtDesc(
                                messId, customer
                        )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public long getMyUnreadCount() {

        Long messId = customerSecurity.getCurrentMessId();
        Customer customer = getAuthenticatedCustomer();

        return notificationRepository
                .countByMess_MessIdAndCustomerAndReadFalse(
                        messId, customer
                );
    }

    @Transactional
    @Override
    public NotificationResponse markAsRead(Long notificationId) {

        Long messId = customerSecurity.getCurrentMessId();
        Customer customer = getAuthenticatedCustomer();

        Notification notification = notificationRepository
                .findByNotificationIdAndMess_MessIdAndCustomer(
                        notificationId, messId, customer
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with ID: " + notificationId
                ));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    @Override
    public void markAllAsRead() {

        Long messId = customerSecurity.getCurrentMessId();
        Customer customer = getAuthenticatedCustomer();

        List<Notification> unread = notificationRepository
                .findByMess_MessIdAndCustomerAndReadFalseOrderByCreatedAtDesc(
                        messId, customer
                );

        if (unread.isEmpty()) {
            return;
        }

        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private void saveNotification(
            Customer recipient,
            NotificationType notificationType,
            String title,
            String message) {

        Notification notification = new Notification();

        notification.setMess(recipient.getMess());
        notification.setCustomer(recipient);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);

        Notification savedNotification =
                notificationRepository.save(notification);

        emailDeliveryService.queueNotificationEmail(savedNotification);

        NotificationResponse response =
                notificationMapper.toResponse(savedNotification);

        sendAfterCommit(recipient.getEmail(), response);
    }

    private boolean canReceiveNotification(
            Customer customer,
            NotificationType notificationType) {

        if (customer.getStatus() == CustomerStatus.ACTIVE) {
            return true;
        }

        return customer.getStatus() == CustomerStatus.INACTIVE
                && (notificationType == NotificationType.BILL_GENERATED
                    || notificationType == NotificationType.PAYMENT_RECEIVED);
    }

    private Customer getAuthenticatedCustomer() {

        if (customerSecurity.getCurrentUserRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException(
                    "Only customers can access their notifications."
            );
        }

        Long customerId = customerSecurity.getCurrentUserId();
        Long messId = customerSecurity.getCurrentMessId();

        return customerRepository
                .findByCustomerIdAndMess_MessId(customerId, messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId
                ));
    }

    private String formatAmount(java.math.BigDecimal amount) {

        return NumberFormat
                .getCurrencyInstance(Locale.forLanguageTag("en-IN"))
                .format(amount);
    }

    private AccessDeniedException notificationAccessDenied() {

        return new AccessDeniedException(
                "You do not have permission to notify this customer."
        );
    }

    private void sendAfterCommit(
            String recipientEmail,
            NotificationResponse response) {

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            sendLiveNotification(recipientEmail, response);
                        }
                    }
            );

            return;
        }

        sendLiveNotification(recipientEmail, response);
    }

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