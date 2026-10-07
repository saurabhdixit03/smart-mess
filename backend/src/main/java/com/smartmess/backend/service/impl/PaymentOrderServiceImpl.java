package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.smartmess.backend.config.CashfreeProperties;
import com.smartmess.backend.dto.response.PaymentCheckoutResponse;
import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Payment;
import com.smartmess.backend.entity.PaymentOrder;
import com.smartmess.backend.enums.BillStatus;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.PaymentMode;
import com.smartmess.backend.enums.PaymentOrderStatus;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.PaymentOrderRepository;
import com.smartmess.backend.repository.PaymentRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.CashfreeGatewayService;
import com.smartmess.backend.service.CashfreeGatewayService.CreateOrderCommand;
import com.smartmess.backend.service.CashfreeGatewayService.FailureKind;
import com.smartmess.backend.service.CashfreeGatewayService.GatewayException;
import com.smartmess.backend.service.CashfreeGatewayService.GatewayOrder;
import com.smartmess.backend.service.CashfreeGatewayService.GatewayPayment;
import com.smartmess.backend.service.PaymentOrderService;
import com.smartmess.backend.service.NotificationService;

@Service
public class PaymentOrderServiceImpl implements PaymentOrderService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentOrderServiceImpl.class);

    private final PaymentOrderRepository orderRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final CashfreeGatewayService gateway;
    private final CashfreeProperties properties;
    private final CustomerSecurity security;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final TransactionTemplate readTransaction;
    private final TransactionTemplate writeTransaction;
    private final NotificationService notificationService;

    public PaymentOrderServiceImpl(
            PaymentOrderRepository orderRepository,
            BillRepository billRepository,
            PaymentRepository paymentRepository,
            CashfreeGatewayService gateway,
            CashfreeProperties properties,
            CustomerSecurity security,
            ObjectMapper objectMapper,
            Clock clock,
            NotificationService notificationService,
            PlatformTransactionManager transactionManager) {

        this.orderRepository = orderRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
        this.properties = properties;
        this.security = security;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
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

    @Override
    public PaymentCheckoutResponse createCheckout(Long billId) {

        requireCustomer();
        validateConfiguration();

        Long messId = security.getCurrentMessId();
        Long customerId = security.getCurrentUserId();

        Long orderId = writeTransaction.execute(status -> {

            Bill bill = lockBill(billId, messId);
            checkOwnership(bill, customerId);

            if (bill.getBillStatus() == BillStatus.PAID
                    || paymentRepository.existsByMess_MessIdAndBill(
                            messId, bill
                    )) {

                throw new BusinessException(
                        "This bill has already been paid."
                );
            }

            Customer customer = bill.getCustomer();

            if (customer.getStatus() != CustomerStatus.ACTIVE
                    && customer.getStatus() != CustomerStatus.INACTIVE) {

                throw new AccessDeniedException(
                        "This customer cannot start checkout."
                );
            }

            PaymentOrder existing =
                    orderRepository.findByMess_MessIdAndActiveBillId(
                            messId, billId
                    ).orElse(null);

            if (existing != null) {
                return existing.getPaymentOrderId();
            }

            BigDecimal amount = bill.getTotalAmount();

            if (amount == null
                    || amount.compareTo(BigDecimal.ONE) < 0
                    || amount.compareTo(
                            new BigDecimal("99999999.99")
                    ) > 0
                    || amount.stripTrailingZeros().scale() > 2) {

                throw new BusinessException(
                        "Online payment requires a valid bill amount of at least ₹1."
                );
            }

            OffsetDateTime expiresAt =
                    OffsetDateTime.now(clock).plusMinutes(
                            properties.getOrderExpiryMinutes()
                    );

            String reference =
                    "sm_" + UUID.randomUUID().toString().replace("-", "");

            String idempotencyKey = UUID.randomUUID().toString();

            CreateOrderCommand command = new CreateOrderCommand(
                    reference,
                    idempotencyKey,
                    properties.getEnvironment().name(),
                    amount,
                    "INR",
                    "mess_" + messId + "_customer_" + customerId,
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getMobileNumber(),
                    expiresAt,
                    properties.getReturnUrl(),
                    properties.getNotifyUrl()
            );

            PaymentOrder order = new PaymentOrder();
            order.setMess(bill.getMess());
            order.setBill(bill);
            order.setActiveBillId(billId);
            order.setProvider(PaymentMode.CASHFREE);
            order.setEnvironment(command.environment());
            order.setGatewayOrderId(reference);
            order.setIdempotencyKey(idempotencyKey);
            order.setCreationRequest(serialize(command));
            order.setOrderAmount(amount);
            order.setCurrency("INR");
            order.setOrderStatus(PaymentOrderStatus.CREATING);
            order.setExpiresAt(toLocal(expiresAt));
            order.setNextCheckAt(LocalDateTime.now(clock));

            return orderRepository.saveAndFlush(order)
                    .getPaymentOrderId();
        });

        processOrder(orderId);
        return getOwnedResponse(orderId, messId, customerId);
    }

    @Override
    public PaymentCheckoutResponse verifyCheckout(Long paymentOrderId) {

        requireCustomer();

        Long messId = security.getCurrentMessId();
        Long customerId = security.getCurrentUserId();

        getOwnedResponse(paymentOrderId, messId, customerId);
        processOrder(paymentOrderId);

        return getOwnedResponse(
                paymentOrderId, messId, customerId
        );
    }

    @Override
    public void handleWebhook(
            byte[] rawBody,
            String timestamp,
            String signature) {

        if (rawBody == null || rawBody.length > 1_048_576
                || !gateway.verifyWebhookSignature(
                        rawBody, timestamp, signature
                )) {

            throw new AccessDeniedException(
                    "Invalid payment webhook signature."
            );
        }

        String gatewayOrderId;

        try {
            JsonNode payload = objectMapper.readTree(rawBody);
            JsonNode reference = payload == null
                    ? null
                    : payload.path("data").path("order").get("order_id");

            if (reference == null
                    || !reference.isTextual()
                    || reference.asText().isBlank()) {

                throw new BusinessException(
                        "Payment webhook order reference is missing."
                );
            }

            gatewayOrderId = reference.asText();

        } catch (java.io.IOException exception) {
            throw new BusinessException(
                    "Payment webhook payload is invalid."
            );
        }

        Long orderId = readTransaction.execute(status ->
                orderRepository.findByGatewayOrderId(gatewayOrderId)
                        .map(PaymentOrder::getPaymentOrderId)
                        .orElse(null)
        );

        /*
         * Events for orders outside this application are ignored.
         * No payment is accepted directly from webhook fields.
         */
        if (orderId != null) {
            processOrder(orderId);
        }
    }

    @Override
    public synchronized void reconcileDueOrders() {

        if (!properties.isEnabled()) {
            return;
        }

        List<Long> ids = readTransaction.execute(status ->
                orderRepository.findOrdersDueForVerification(
                        LocalDateTime.now(clock),
                        PageRequest.of(0, 20)
                )
        );

        if (ids == null) {
            return;
        }

        for (Long id : ids) {
            try {
                processOrder(id);
            } catch (RuntimeException exception) {
                log.warn(
                        "Payment order recovery failed for order {}.",
                        id
                );
            }
        }
    }

    /*
     * API calls run between short database transactions.
     * All writes acquire the bill lock before the order lock.
     */
    private void processOrder(Long orderId) {

        OrderContext context = loadContext(orderId);

        if (context == null) {
            return;
        }

        ClaimedOrder claim = writeTransaction.execute(status -> {

            lockBill(context.billId(), context.messId());

            PaymentOrder order = orderRepository
                    .findByIdAndMessIdForUpdate(
                            orderId, context.messId()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payment order not found."
                    ));

            validateTenant(order);

            if (isTerminal(order.getOrderStatus())) {
                return null;
            }

            LocalDateTime now = LocalDateTime.now(clock);

            if (order.getLeaseExpiresAt() != null
                    && order.getLeaseExpiresAt().isAfter(now)) {
                return null;
            }

            String token = UUID.randomUUID().toString();

            order.setLeaseToken(token);
            order.setLeaseExpiresAt(now.plusMinutes(3));
            order.setVerificationAttemptCount(
                    order.getVerificationAttemptCount() + 1
            );

            orderRepository.save(order);

            return new ClaimedOrder(
                    context,
                    token,
                    order.getGatewayOrderId(),
                    order.getEnvironment(),
                    order.getOrderStatus(),
                    order.getCreationRequest(),
                    order.getProviderOrderId()
            );
        });

        if (claim == null) {
            return;
        }

        try {
            GatewayOrder remote;

            try {
                remote = gateway.getOrder(
                        claim.gatewayOrderId(),
                        claim.environment()
                );

            } catch (GatewayException exception) {

                if (exception.getFailureKind() != FailureKind.NOT_FOUND
                        || claim.providerOrderId() != null
                        || (claim.status() != PaymentOrderStatus.CREATING
                            && claim.status()
                                != PaymentOrderStatus.RECONCILIATION_REQUIRED)) {

                    throw exception;
                }

                CreateOrderCommand command =
                        deserialize(claim.creationRequest());

                validateCommand(claim, command);

                /*
                 * Retry the original order and original idempotency key.
                 * Never invent a replacement after a timeout.
                 */
                remote = gateway.createOrder(command);
            }

            GatewayOrder confirmedOrder = remote;

            Boolean confirmationSaved = writeTransaction.execute(status ->
                    rememberConfirmedOrder(claim, confirmedOrder)
            );

            if (!Boolean.TRUE.equals(confirmationSaved)) {
                return;
            }

            List<GatewayPayment> payments =
                    gateway.getOrderPayments(
                            claim.gatewayOrderId(),
                            claim.environment()
                    );

            GatewayOrder verifiedOrder = remote;

            writeTransaction.executeWithoutResult(status ->
                    applyGatewayResult(
                            claim, verifiedOrder, payments
                    )
            );

        } catch (GatewayException exception) {
            saveFailure(
                    claim,
                    exception.getFailureKind(),
                    exception.getMessage()
            );

        } catch (RuntimeException exception) {
            log.warn(
                    "Payment verification needs recovery for order {}.",
                    orderId
            );

            saveFailure(
                    claim,
                    FailureKind.UNCERTAIN,
                    "Payment verification could not be completed."
            );
        }
    }

    private void applyGatewayResult(
            ClaimedOrder claim,
            GatewayOrder remote,
            List<GatewayPayment> payments) {

        Bill bill = lockBill(
                claim.context().billId(),
                claim.context().messId()
        );

        PaymentOrder order = lockOrder(claim);

        if (!ownsLease(order, claim.token())) {
            return;
        }

        validateTenant(order);

        if (remote == null
                || !order.getGatewayOrderId()
                        .equals(remote.gatewayOrderId())
                || !sameAmount(order.getOrderAmount(), remote.amount())
                || !order.getCurrency().equals(remote.currency())
                || !sameAmount(order.getOrderAmount(), bill.getTotalAmount())
                || (order.getProviderOrderId() != null
                    && !order.getProviderOrderId()
                            .equals(remote.providerOrderId()))) {

            throw new BusinessException(
                    "Gateway order verification did not match the bill."
            );
        }

        if (remote.providerOrderId() == null
                || remote.providerOrderId().isBlank()) {

            throw new BusinessException(
                    "Gateway order reference is missing."
            );
        }

        order.setProviderOrderId(remote.providerOrderId());

        for (GatewayPayment payment : payments) {
            if (!order.getGatewayOrderId()
                    .equals(payment.gatewayOrderId())) {

                throw new BusinessException(
                        "Gateway payment belongs to another order."
                );
            }
        }

        List<GatewayPayment> successful =
                payments.stream()
                        .filter(payment ->
                                "SUCCESS".equals(payment.status())
                        )
                        .toList();

        if (successful.size() > 1) {
            throw new BusinessException(
                    "Multiple successful transactions require reconciliation."
            );
        }

        if (successful.size() == 1) {

            GatewayPayment payment = successful.get(0);

            if (!payment.captured()
                    || !sameAmount(
                            order.getOrderAmount(), payment.amount()
                    )
                    || !order.getCurrency().equals(payment.currency())
                    || payment.completedAt() == null
                    || payment.gatewayPaymentId() == null
                    || payment.gatewayPaymentId().isBlank()) {

                throw new BusinessException(
                        "Successful payment details could not be verified."
                );
            }

            settle(order, bill, payment);
            return;
        }

        if ("PAID".equals(remote.status())) {
            throw new BusinessException(
                    "Paid order is awaiting successful transaction details."
            );
        }

        PaymentOrderStatus remoteStatus;

        try {
            remoteStatus = PaymentOrderStatus.valueOf(remote.status());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(
                    "Gateway returned an unsupported order state."
            );
        }

        if (remoteStatus != PaymentOrderStatus.ACTIVE
                && remoteStatus != PaymentOrderStatus.EXPIRED
                && remoteStatus != PaymentOrderStatus.TERMINATED
                && remoteStatus
                        != PaymentOrderStatus.TERMINATION_REQUESTED) {

            throw new BusinessException(
                    "Gateway order state could not be verified."
            );
        }

        boolean pendingTransaction = payments.stream()
                .anyMatch(payment ->
                        "PENDING".equals(payment.status())
                        || "NOT_ATTEMPTED".equals(payment.status())
                );

        if ((remoteStatus == PaymentOrderStatus.EXPIRED
                || remoteStatus == PaymentOrderStatus.TERMINATED)
                && pendingTransaction) {

            throw new BusinessException(
                    "Gateway transaction is still awaiting a final outcome."
            );
        }

        if (remoteStatus == PaymentOrderStatus.ACTIVE
                && (remote.paymentSessionId() == null
                    || remote.paymentSessionId().isBlank())) {

            throw new BusinessException(
                    "Gateway checkout session is unavailable."
            );
        }

        order.setProviderOrderId(remote.providerOrderId());
        order.setPaymentSessionId(remote.paymentSessionId());
        order.setOrderStatus(remoteStatus);

        if (remote.expiresAt() != null) {
            order.setExpiresAt(toLocal(remote.expiresAt()));
        }

        if (remoteStatus == PaymentOrderStatus.EXPIRED
                || remoteStatus == PaymentOrderStatus.TERMINATED) {
            order.setActiveBillId(null);
        }

        finishCheck(order, isTerminal(remoteStatus));
        orderRepository.saveAndFlush(order);
    }

    private void settle(
            PaymentOrder order,
            Bill bill,
            GatewayPayment verifiedPayment) {

        Payment existing = paymentRepository
                .findByMess_MessIdAndBill(
                        order.getMess().getMessId(), bill
                )
                .orElse(null);

        /*
         * Never attribute an existing payment to a different order.
         */
        if (existing != null
                || bill.getBillStatus() == BillStatus.PAID) {

            throw new BusinessException(
                    "Bill already has a payment requiring reconciliation."
            );
        }

        LocalDateTime paidAt =
                toLocal(verifiedPayment.completedAt());

        Payment payment = new Payment();
        payment.setMess(bill.getMess());
        payment.setBill(bill);
        payment.setPaymentAmount(order.getOrderAmount());
        payment.setPaymentMode(PaymentMode.CASHFREE);
        payment.setPaidAt(paidAt);

        paymentRepository.saveAndFlush(payment);

        bill.setBillStatus(BillStatus.PAID);
        billRepository.save(bill);

        order.setGatewayPaymentId(
                verifiedPayment.gatewayPaymentId()
        );
        order.setPaidAt(paidAt);
        order.setOrderStatus(PaymentOrderStatus.PAID);
        order.setActiveBillId(null);
        order.setPaymentSessionId(null);

        finishCheck(order, true);
        orderRepository.saveAndFlush(order);

        notificationService.notifyVerifiedPayment(
                order.getPaymentOrderId()
        );
    }

    private void saveFailure(
            ClaimedOrder claim,
            FailureKind kind,
            String safeMessage) {

        writeTransaction.executeWithoutResult(status -> {

            lockBill(
                    claim.context().billId(),
                    claim.context().messId()
            );

            PaymentOrder order = lockOrder(claim);

            if (!ownsLease(order, claim.token())) {
                return;
            }

            /*
             * Only a definitive rejection of a newly created order
             * releases checkout. Uncertain results retain the reservation.
             */
            boolean rejected =
                    kind == FailureKind.REJECTED
                            && claim.status() == PaymentOrderStatus.CREATING;

            order.setOrderStatus(
                    rejected
                            ? PaymentOrderStatus.CREATION_FAILED
                            : PaymentOrderStatus.RECONCILIATION_REQUIRED
            );

            if (rejected) {
                order.setActiveBillId(null);
            }

            order.setLastCheckedAt(LocalDateTime.now(clock));
            order.setLastError(
                    safeMessage == null
                            ? "Payment verification requires recovery."
                            : safeMessage.substring(
                                    0, Math.min(1000, safeMessage.length())
                            )
            );

            int exponent = Math.min(
                    6,
                    Math.max(
                            0,
                            order.getVerificationAttemptCount() - 1
                    )
            );

            order.setNextCheckAt(
                    rejected
                            ? null
                            : LocalDateTime.now(clock).plusMinutes(
                                    Math.min(60, 1L << exponent)
                            )
            );

            order.setLeaseToken(null);
            order.setLeaseExpiresAt(null);
            orderRepository.saveAndFlush(order);
        });
    }

    private PaymentCheckoutResponse getOwnedResponse(
            Long orderId,
            Long messId,
            Long customerId) {

        return readTransaction.execute(status -> {

            PaymentOrder order = orderRepository
                    .findByPaymentOrderIdAndMess_MessId(orderId, messId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payment order not found."
                    ));

            validateTenant(order);
            checkOwnership(order.getBill(), customerId);

            return new PaymentCheckoutResponse(
                    order.getPaymentOrderId(),
                    order.getBill().getBillId(),
                    order.getGatewayOrderId(),
                    order.getOrderStatus() == PaymentOrderStatus.ACTIVE
                            ? order.getPaymentSessionId()
                            : null,
                    order.getOrderAmount(),
                    order.getCurrency(),
                    order.getEnvironment(),
                    order.getOrderStatus(),
                    order.getExpiresAt()
            );
        });
    }

    private OrderContext loadContext(Long orderId) {

        return readTransaction.execute(status ->
                orderRepository.findById(orderId)
                        .map(order -> new OrderContext(
                                order.getPaymentOrderId(),
                                order.getMess().getMessId(),
                                order.getBill().getBillId()
                        ))
                        .orElse(null)
        );
    }

    private Bill lockBill(Long billId, Long messId) {

        return billRepository
                .findByBillIdAndMessIdForUpdate(billId, messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill not found."
                ));
    }

    private PaymentOrder lockOrder(ClaimedOrder claim) {

        return orderRepository.findByIdAndMessIdForUpdate(
                claim.context().orderId(),
                claim.context().messId()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Payment order not found."
        ));
    }

    private boolean ownsLease(PaymentOrder order, String token) {

        return !isTerminal(order.getOrderStatus())
                && Objects.equals(order.getLeaseToken(), token)
                && order.getLeaseExpiresAt() != null
                && order.getLeaseExpiresAt()
                        .isAfter(LocalDateTime.now(clock));
    }

    private void validateTenant(PaymentOrder order) {

        Long messId = order.getMess().getMessId();

        if (!messId.equals(order.getBill().getMess().getMessId())
                || !messId.equals(
                        order.getBill().getCustomer()
                                .getMess().getMessId()
                )) {

            throw new AccessDeniedException(
                    "Payment order tenant ownership is invalid."
            );
        }
    }

    private void checkOwnership(Bill bill, Long customerId) {

        if (!bill.getCustomer().getCustomerId().equals(customerId)) {
            throw new AccessDeniedException(
                    "You cannot access this bill."
            );
        }
    }

    private void requireCustomer() {

        if (security.getCurrentUserRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException(
                    "Only customers can use checkout."
            );
        }
    }

    private void validateConfiguration() {

        if (!properties.isEnabled()
                || properties.getEnvironment() == null
                || properties.getClientId() == null
                || properties.getClientId().isBlank()
                || properties.getClientSecret() == null
                || properties.getClientSecret().isBlank()
                || properties.getOrderExpiryMinutes() < 5
                || properties.getOrderExpiryMinutes() > 1440) {

            throw new BusinessException(
                    "Online payment configuration is incomplete."
            );
        }

        validateUrl(properties.getReturnUrl(), false);
        validateUrl(properties.getNotifyUrl(), true);
    }

    private void validateUrl(String value, boolean webhook) {

        try {
            URI uri = URI.create(
                    value.replace("{order_id}", "order")
            );

            boolean https = "https".equalsIgnoreCase(uri.getScheme());
            boolean localReturn =
                    !webhook
                    && properties.getEnvironment()
                            == CashfreeProperties.Environment.SANDBOX
                    && "http".equalsIgnoreCase(uri.getScheme())
                    && ("localhost".equalsIgnoreCase(uri.getHost())
                        || "127.0.0.1".equals(uri.getHost()));

            if (uri.getHost() == null
                    || uri.getUserInfo() != null
                    || (!https && !localReturn)) {

                throw new IllegalArgumentException();
            }

        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(
                    "Payment return URL or webhook URL is invalid."
            );
        }
    }

    private void validateCommand(
            ClaimedOrder claim,
            CreateOrderCommand command) {

        if (!claim.gatewayOrderId().equals(command.gatewayOrderId())
                || !claim.environment().equals(command.environment())) {

            throw new BusinessException(
                    "Stored payment request is inconsistent."
            );
        }
    }

    private String serialize(CreateOrderCommand command) {

        try {
            return objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    "Payment request could not be stored."
            );
        }
    }

    private CreateOrderCommand deserialize(String payload) {

        try {
            return objectMapper.readValue(
                    payload, CreateOrderCommand.class
            );
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            throw new BusinessException(
                    "Stored payment request could not be recovered."
            );
        }
    }

    private LocalDateTime toLocal(OffsetDateTime value) {

        return value.atZoneSameInstant(clock.getZone())
                .toLocalDateTime()
                .truncatedTo(java.time.temporal.ChronoUnit.MICROS);
    }

    private boolean sameAmount(BigDecimal first, BigDecimal second) {

        return first != null
                && second != null
                && first.compareTo(second) == 0;
    }

    private boolean isTerminal(PaymentOrderStatus status) {

        return status == PaymentOrderStatus.PAID
                || status == PaymentOrderStatus.EXPIRED
                || status == PaymentOrderStatus.TERMINATED
                || status == PaymentOrderStatus.CREATION_FAILED;
    }

    private void finishCheck(PaymentOrder order, boolean terminal) {

        LocalDateTime now = LocalDateTime.now(clock);

        order.setLastCheckedAt(now);
        order.setNextCheckAt(
                terminal ? null : now.plusMinutes(2)
        );
        order.setLastError(null);
        order.setLeaseToken(null);
        order.setLeaseExpiresAt(null);
    }

    private record OrderContext(
            Long orderId,
            Long messId,
            Long billId
    ) {
    }

    private record ClaimedOrder(
            OrderContext context,
            String token,
            String gatewayOrderId,
            String environment,
            PaymentOrderStatus status,
            String creationRequest,
            String providerOrderId
    ) {
    }

    private boolean rememberConfirmedOrder(
            ClaimedOrder claim,
            GatewayOrder remote) {

        Bill bill = lockBill(
                claim.context().billId(),
                claim.context().messId()
        );

        PaymentOrder order = lockOrder(claim);

        if (!ownsLease(order, claim.token())) {
            return false;
        }

        validateTenant(order);

        if (remote == null
                || !order.getGatewayOrderId().equals(remote.gatewayOrderId())
                || remote.providerOrderId() == null
                || remote.providerOrderId().isBlank()
                || !sameAmount(order.getOrderAmount(), remote.amount())
                || !order.getCurrency().equals(remote.currency())
                || !sameAmount(order.getOrderAmount(), bill.getTotalAmount())
                || (order.getProviderOrderId() != null
                    && !order.getProviderOrderId()
                            .equals(remote.providerOrderId()))) {

            throw new BusinessException(
                    "Gateway order confirmation did not match the bill."
            );
        }

        order.setProviderOrderId(remote.providerOrderId());
        orderRepository.saveAndFlush(order);

        return true;
    }
}