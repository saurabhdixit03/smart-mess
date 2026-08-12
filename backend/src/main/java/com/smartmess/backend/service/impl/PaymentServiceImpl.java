
package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreatePaymentRequest;
import com.smartmess.backend.dto.response.BillResponse;
import com.smartmess.backend.dto.response.PaymentOverviewResponse;
import com.smartmess.backend.dto.response.PaymentResponse;
import com.smartmess.backend.dto.response.PendingPaymentResponse;
import com.smartmess.backend.dto.response.UpiPaymentResponse;
import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.entity.Payment;
import com.smartmess.backend.enums.BillStatus;
import com.smartmess.backend.enums.PaymentMode;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.BillMapper;
import com.smartmess.backend.mapper.PaymentMapper;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.MessSettingsRepository;
import com.smartmess.backend.repository.PaymentRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final BillRepository billRepository;

    private final PaymentMapper paymentMapper;

    private final MessSettingsRepository messSettingsRepository;

    private final BillMapper billMapper;

    private final CustomerSecurity customerSecurity;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BillRepository billRepository,
            PaymentMapper paymentMapper,
            MessSettingsRepository messSettingsRepository,
            BillMapper billMapper,
            CustomerSecurity customerSecurity) {

        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.paymentMapper = paymentMapper;
        this.messSettingsRepository = messSettingsRepository;
        this.billMapper = billMapper;
        this.customerSecurity = customerSecurity;
    }

    /*
     * Collect Payment.
     *
     * Owner only.
     */
    @Override
    public PaymentResponse collectPayment(
            CreatePaymentRequest request) {

        Bill bill =
                billRepository.findById(
                        request.billId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bill not found with ID: "
                                        + request.billId()
                        ));

        if (bill.getBillStatus() == BillStatus.PAID) {

            throw new BusinessException(
                    "This bill has already been paid."
            );
        }

        if (request.paymentMode() == PaymentMode.CASH
                && bill.getBillStatus() != BillStatus.UNPAID) {

            throw new BusinessException(
                    "Cash payment can only be collected for unpaid bills."
            );
        }

        if (request.paymentMode() == PaymentMode.UPI
                && bill.getBillStatus() != BillStatus.PAYMENT_PENDING) {

            throw new BusinessException(
                    "UPI payment must be requested before it can be approved."
            );
        }

        if (paymentRepository.existsByBill(bill)) {

            throw new BusinessException(
                    "Payment has already been collected for this bill."
            );
        }

        Payment payment =
                new Payment();

        payment.setBill(bill);

        payment.setPaymentAmount(
                bill.getTotalAmount()
        );

        payment.setPaymentMode(
                request.paymentMode()
        );

        payment.setPaidAt(
                LocalDateTime.now()
        );

        /*
         * Update Bill Status.
         */
        bill.setBillStatus(
                BillStatus.PAID
        );

        billRepository.save(bill);

        Payment savedPayment =
                paymentRepository.save(payment);

        return paymentMapper.toResponse(
                savedPayment
        );
    }

    /*
     * View Payment.
     *
     * Owner only.
     */
    @Override
    public PaymentResponse getPayment(
            Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found with ID: "
                                        + paymentId
                        ));

        return paymentMapper.toResponse(
                payment
        );
    }

    /*
     * View Payment By Bill.
     *
     * Owner only.
     */
    @Override
    public PaymentResponse getPaymentByBill(
            Long billId) {

        Bill bill =
                billRepository.findById(billId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bill not found with ID: "
                                        + billId
                        ));

        Payment payment =
                paymentRepository.findByBill(bill)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found for Bill ID: "
                                        + billId
                        ));

        return paymentMapper.toResponse(
                payment
        );
    }

    /*
     * Customer requests UPI payment verification.
     *
     * Customer can request payment only
     * for their own bill.
     */
    @Override
    public void requestUpiPayment(
            Long billId) {

        Bill bill =
                billRepository.findById(billId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bill not found with ID: "
                                        + billId
                        ));

        /*
         * Ownership validation.
         *
         * OWNER:
         *     checkCustomerAccess() allows access.
         *
         * CUSTOMER:
         *     checkCustomerAccess() verifies that
         *     the bill belongs to the authenticated customer.
         */
        customerSecurity.checkCustomerAccess(
                bill.getCustomer().getCustomerId()
        );

        if (bill.getBillStatus() != BillStatus.UNPAID) {

            throw new BusinessException(
                    "Payment request can only be submitted for unpaid bills."
            );
        }

        bill.setBillStatus(
                BillStatus.PAYMENT_PENDING
        );

        billRepository.save(bill);
    }

    /*
     * View pending payment requests.
     *
     * Owner only.
     */
    @Override
    public List<PendingPaymentResponse> getPendingPayments() {

        List<Bill> pendingBills =
                billRepository.findByBillStatusOrderByGeneratedAtAsc(
                        BillStatus.PAYMENT_PENDING
                );

        return pendingBills.stream()
                .map(bill -> new PendingPaymentResponse(

                        bill.getBillId(),

                        bill.getCustomer().getCustomerId(),

                        bill.getCustomer().getFullName(),

                        bill.getBillingMonth(),

                        bill.getBillingYear(),

                        bill.getTotalAmount(),

                        bill.getBillStatus()

                ))
                .toList();
    }

    /*
     * Pending payment count.
     *
     * Owner only.
     */
    @Override
    public long getPendingPaymentCount() {

        return billRepository.countByBillStatus(
                BillStatus.PAYMENT_PENDING
        );
    }

    /*
     * Generate UPI payment link / QR data.
     *
     * Customer can generate payment data
     * only for their own bill.
     */
    @Override
    public UpiPaymentResponse generateUpiPayment(
            Long billId) {

        Bill bill =
                billRepository.findById(billId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bill not found with ID: "
                                        + billId
                        ));

        /*
         * Ownership validation.
         *
         * CUSTOMER can generate UPI payment
         * only for their own bill.
         */
        customerSecurity.checkCustomerAccess(
                bill.getCustomer().getCustomerId()
        );

        if (bill.getBillStatus() != BillStatus.UNPAID) {

            throw new BusinessException(
                    "UPI payment can only be generated for unpaid bills."
            );
        }

        MessSettings settings =
                messSettingsRepository
                        .findTopByOrderBySettingsIdAsc()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mess settings not found."
                                ));

        String upiUrl =
                "upi://pay"
                + "?pa=" + URLEncoder.encode(
                        settings.getUpiId(),
                        StandardCharsets.UTF_8
                )
                + "&pn=" + URLEncoder.encode(
                        settings.getReceiverName(),
                        StandardCharsets.UTF_8
                )
                + "&am=" + bill.getTotalAmount()
                + "&cu=INR"
                + "&tn=" + URLEncoder.encode(
                        "Bill-" + bill.getBillId(),
                        StandardCharsets.UTF_8
                );

        return new UpiPaymentResponse(

                upiUrl,

                settings.getUpiId(),

                settings.getReceiverName(),

                bill.getTotalAmount(),

                bill.getBillId()
        );
    }

    /*
     * Payment Dashboard Overview.
     *
     * Owner only.
     */
    @Override
    public PaymentOverviewResponse getPaymentOverview() {

        PaymentOverviewResponse response =
                new PaymentOverviewResponse();

        // Dashboard Counts

        response.setUnpaidBillCount(
                billRepository.countByBillStatus(
                        BillStatus.UNPAID
                )
        );

        response.setPendingRequestCount(
                billRepository.countByBillStatus(
                        BillStatus.PAYMENT_PENDING
                )
        );

        response.setPaidBillCount(
                billRepository.countByBillStatus(
                        BillStatus.PAID
                )
        );

        // Unpaid Bills Table

        List<Bill> unpaidBills =
                billRepository.findByBillStatusOrderByGeneratedAtAsc(
                        BillStatus.UNPAID
                );

        response.setUnpaidBills(
                billMapper.toResponseList(
                        unpaidBills
                )
        );

        // Pending Payment Requests Table

        response.setPendingPayments(
                getPendingPayments()
        );

        // Total Collected Revenue

        List<Bill> paidBills =
                billRepository.findByBillStatusOrderByGeneratedAtAsc(
                        BillStatus.PAID
                );

        response.setTotalCollectedAmount(
                paidBills.stream()
                        .map(Bill::getTotalAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
        );

        return response;
    }
}
