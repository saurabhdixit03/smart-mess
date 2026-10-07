package com.smartmess.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.response.BillPaymentReceiptResponse;
import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Payment;
import com.smartmess.backend.entity.PaymentOrder;
import com.smartmess.backend.enums.PaymentOrderStatus;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.repository.PaymentOrderRepository;
import com.smartmess.backend.repository.PaymentRepository;
import com.smartmess.backend.security.CustomerSecurity;

@Service
public class BillPaymentReceiptService {

    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final CustomerSecurity customerSecurity;

    public BillPaymentReceiptService(
            PaymentRepository paymentRepository,
            PaymentOrderRepository paymentOrderRepository,
            CustomerSecurity customerSecurity) {

        this.paymentRepository = paymentRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.customerSecurity = customerSecurity;
    }

    @Transactional(readOnly = true)
    public BillPaymentReceiptResponse getReceipt(Bill bill) {

        Long messId = customerSecurity.getCurrentMessId();

        if (!messId.equals(bill.getMess().getMessId())
                || !messId.equals(
                        bill.getCustomer().getMess().getMessId()
                )) {

            throw new AccessDeniedException(
                    "Bill tenant ownership is invalid."
            );
        }

        customerSecurity.checkCustomerAccess(
                bill.getCustomer().getCustomerId()
        );

        Payment payment = paymentRepository
                .findByMess_MessIdAndBill(messId, bill)
                .orElse(null);

        if (payment == null) {
            return null;
        }

        List<PaymentOrder> settledOrders = paymentOrderRepository
                .findByMess_MessIdAndBill_BillIdOrderByCreatedAtDesc(
                        messId,
                        bill.getBillId()
                )
                .stream()
                .filter(order ->
                        order.getOrderStatus() == PaymentOrderStatus.PAID
                )
                .toList();

        if (settledOrders.size() > 1) {
            throw new BusinessException(
                    "Payment receipt requires reconciliation."
            );
        }

        PaymentOrder settledOrder = settledOrders.isEmpty()
                ? null
                : settledOrders.get(0);

        if (settledOrder != null
                && !matchesPayment(settledOrder, payment)) {

            throw new BusinessException(
                    "Payment receipt details are inconsistent."
            );
        }

        /*
         * Historical payments may have no associated gateway order.
         * Preserve their recorded payment without inventing metadata.
         */
        return new BillPaymentReceiptResponse(
                payment.getPaymentId(),
                payment.getPaymentAmount(),
                payment.getPaymentMode(),
                payment.getPaidAt(),
                settledOrder == null
                        ? null
                        : settledOrder.getEnvironment(),
                settledOrder == null
                        ? null
                        : settledOrder.getCurrency(),
                settledOrder == null
                        ? null
                        : settledOrder.getGatewayOrderId(),
                settledOrder == null
                        ? null
                        : settledOrder.getGatewayPaymentId()
        );
    }

    private boolean matchesPayment(
            PaymentOrder order,
            Payment payment) {

        return order.getProvider() == payment.getPaymentMode()
                && order.getOrderAmount() != null
                && payment.getPaymentAmount() != null
                && order.getOrderAmount().compareTo(
                        payment.getPaymentAmount()
                ) == 0
                && order.getPaidAt() != null
                && order.getPaidAt().equals(payment.getPaidAt())
                && order.getGatewayPaymentId() != null
                && !order.getGatewayPaymentId().isBlank();
    }
}