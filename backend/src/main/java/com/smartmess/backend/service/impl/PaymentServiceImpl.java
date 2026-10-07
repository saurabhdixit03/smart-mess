package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.response.PaymentOverviewResponse;
import com.smartmess.backend.dto.response.PaymentResponse;
import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Payment;
import com.smartmess.backend.enums.BillStatus;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.BillMapper;
import com.smartmess.backend.mapper.PaymentMapper;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.PaymentRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final PaymentMapper paymentMapper;
    private final BillMapper billMapper;
    private final CustomerSecurity customerSecurity;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BillRepository billRepository,
            PaymentMapper paymentMapper,
            BillMapper billMapper,
            CustomerSecurity customerSecurity) {

        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.paymentMapper = paymentMapper;
        this.billMapper = billMapper;
        this.customerSecurity = customerSecurity;
    }

    /*
     * Owner may view payments within their mess.
     * Customer may view only payments for their own bills.
     */
    @Transactional(readOnly = true)
    @Override
    public PaymentResponse getPayment(Long paymentId) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Payment payment =
                paymentRepository
                        .findByPaymentIdAndMess_MessId(
                                paymentId,
                                messId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found."
                                )
                        );

        validatePaymentOwnership(payment, messId);

        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentResponse getPaymentByBill(Long billId) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Bill bill =
                billRepository
                        .findByBillIdAndMess_MessId(
                                billId,
                                messId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bill not found."
                                )
                        );

        customerSecurity.checkCustomerAccess(
                bill.getCustomer().getCustomerId()
        );

        Payment payment =
                paymentRepository
                        .findByMess_MessIdAndBill(
                                messId,
                                bill
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found for this bill."
                                )
                        );

        validatePaymentOwnership(payment, messId);

        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentOverviewResponse getPaymentOverview() {

        requireOwner();

        Long messId =
                customerSecurity.getCurrentMessId();

        List<Bill> unpaidBills =
                billRepository
                        .findByMess_MessIdAndBillStatusOrderByGeneratedAtAsc(
                                messId,
                                BillStatus.UNPAID
                        );

        List<Bill> paidBills =
                billRepository
                        .findByMess_MessIdAndBillStatusOrderByGeneratedAtAsc(
                                messId,
                                BillStatus.PAID
                        );

        BigDecimal outstandingAmount =
                unpaidBills.stream()
                        .map(Bill::getTotalAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        /*
         * Count actual recorded payments, rather than treating
         * a bill's status alone as evidence of collected money.
         */
        BigDecimal collectedAmount =
                paidBills.stream()
                        .map(bill ->
                                paymentRepository
                                        .findByMess_MessIdAndBill(
                                                messId,
                                                bill
                                        )
                                        .map(Payment::getPaymentAmount)
                                        .orElse(BigDecimal.ZERO)
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        PaymentOverviewResponse response =
                new PaymentOverviewResponse();

        response.setUnpaidBillCount(
                (long) unpaidBills.size()
        );
        response.setPaidBillCount(
                (long) paidBills.size()
        );
        response.setTotalCollectedAmount(collectedAmount);
        response.setTotalOutstandingAmount(outstandingAmount);

        response.setUnpaidBills(
                billMapper.toResponseList(unpaidBills)
        );
        response.setPaidBills(
                billMapper.toResponseList(paidBills)
        );

        return response;
    }

    private void validatePaymentOwnership(
            Payment payment,
            Long messId) {

        Bill bill = payment.getBill();

        if (!messId.equals(payment.getMess().getMessId())
                || !messId.equals(bill.getMess().getMessId())
                || !messId.equals(
                        bill.getCustomer().getMess().getMessId()
                )) {

            throw new AccessDeniedException(
                    "Payment tenant ownership is invalid."
            );
        }

        customerSecurity.checkCustomerAccess(
                bill.getCustomer().getCustomerId()
        );
    }

    private void requireOwner() {

        if (customerSecurity.getCurrentUserRole()
                != UserRole.OWNER) {

            throw new AccessDeniedException(
                    "Only mess owners can access payment reporting."
            );
        }
    }
}