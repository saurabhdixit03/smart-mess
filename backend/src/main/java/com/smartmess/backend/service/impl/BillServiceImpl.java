package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.request.GenerateBillRequest;
import com.smartmess.backend.dto.response.BillDetailResponse;
import com.smartmess.backend.dto.response.BillResponse;
import com.smartmess.backend.dto.response.BillingOverviewResponse;
import com.smartmess.backend.dto.response.BillingSummaryResponse;
import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.enums.BillStatus;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.BillMapper;
import com.smartmess.backend.mapper.MealRecordMapper;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.BillService;
import com.smartmess.backend.service.NotificationService;

@Service
public class BillServiceImpl implements BillService {

    private static final Locale INDIA_LOCALE =
            Locale.forLanguageTag("en-IN");

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final MealRecordRepository mealRecordRepository;
    private final BillMapper billMapper;
    private final MealRecordMapper mealRecordMapper;
    private final CustomerSecurity customerSecurity;
    private final NotificationService notificationService;
    private final Clock clock;

    public BillServiceImpl(
            BillRepository billRepository,
            CustomerRepository customerRepository,
            MealRecordRepository mealRecordRepository,
            BillMapper billMapper,
            MealRecordMapper mealRecordMapper,
            CustomerSecurity customerSecurity,
            NotificationService notificationService,
            Clock clock) {

        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.billMapper = billMapper;
        this.mealRecordMapper = mealRecordMapper;
        this.customerSecurity = customerSecurity;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    /*
     * Generate Bills
     *
     * Owner only.
     * Customers and meal records are restricted to the owner's mess.
     */
    @Transactional
    @Override
    public List<BillResponse> generateBills(
            GenerateBillRequest request) {

        Long messId =
                customerSecurity.getCurrentMessId();

        List<Customer> customers =
                customerRepository.findAllByMess_MessIdAndStatus(
                        messId,
                        CustomerStatus.ACTIVE
                );

        List<BillResponse> generatedBills =
                new ArrayList<>();

        int existingBillCount = 0;
        int noMealRecordCount = 0;

        LocalDate startDate =
                LocalDate.of(
                        request.billingYear(),
                        request.billingMonth(),
                        1
                );

        LocalDate endDate =
                startDate.withDayOfMonth(
                        startDate.lengthOfMonth()
                );

        LocalDateTime start =
                startDate.atStartOfDay();

        LocalDateTime end =
                endDate.atTime(
                        LocalTime.MAX
                );

        for (Customer customer : customers) {

            /*
             * Skip duplicate Bills.
             */
            boolean billExists =
                    billRepository
                            .existsByMess_MessIdAndCustomerAndBillingMonthAndBillingYear(
                                    messId,
                                    customer,
                                    request.billingMonth(),
                                    request.billingYear()
                            );

            if (billExists) {

                existingBillCount++;
                continue;
            }

            /*
             * Get meal records for the customer's
             * selected billing period.
             *
             * Records are restricted to the same mess.
             */
            List<MealRecord> mealRecords =
                    mealRecordRepository
                            .findByMess_MessIdAndCustomerAndCollectedAtBetween(
                                    messId,
                                    customer,
                                    start,
                                    end
                            );

            /*
             * Skip customers having no meal records.
             */
            if (mealRecords.isEmpty()) {

                noMealRecordCount++;
                continue;
            }

            BigDecimal totalAmount =
                    mealRecords.stream()
                            .map(MealRecord::getTotalAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            Bill bill =
                    new Bill();

            bill.setMess(customer.getMess());
            bill.setCustomer(customer);
            bill.setBillingMonth(request.billingMonth());
            bill.setBillingYear(request.billingYear());
            bill.setMealRecordCount(mealRecords.size());
            bill.setTotalAmount(totalAmount);
            bill.setBillStatus(BillStatus.UNPAID);
            bill.setGeneratedAt(LocalDateTime.now(clock));

            Bill savedBill =
                    billRepository.save(bill);

            /*
             * Link Meal Records to the generated Bill.
             */
            for (MealRecord mealRecord : mealRecords) {
                mealRecord.setBill(savedBill);
            }

            mealRecordRepository.saveAll(
                    mealRecords
            );

            /*
             * Notify the customer only after the bill
             * and its meal records have been saved.
             *
             * Saving occurs within this transaction;
             * commit happens after the method completes.
             */
            notificationService.notifyCustomer(
                    customer,
                    NotificationType.BILL_GENERATED,
                    "New Bill Generated",
                    buildBillNotificationMessage(
                            savedBill
                    )
            );

            generatedBills.add(
                    billMapper.toResponse(savedBill)
            );
        }

        if (generatedBills.isEmpty()) {

            throw new BusinessException(
                    buildNoBillsGeneratedMessage(
                            request,
                            customers.size(),
                            existingBillCount,
                            noMealRecordCount
                    )
            );
        }

        return generatedBills;
    }

    /*
     * Customer Bill History
     *
     * Owner use case.
     *
     * The owner can request bills for any customer within their mess.
     * Role authorization is handled at the controller level.
     * Customer ownership is also validated in the service.
     */
    @Transactional(readOnly = true)
    @Override
    public List<BillResponse> getCustomerBills(
            Long customerId) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                findCustomer(customerId, messId);

        customerSecurity.checkCustomerAccess(customerId);

        List<Bill> bills =
                billRepository
                        .findByMess_MessIdAndCustomerOrderByGeneratedAtDesc(
                                messId,
                                customer
                        );

        return billMapper.toResponseList(
                bills
        );
    }

    /*
     * Authenticated Customer Bill History
     *
     * Customer ID comes directly from the JWT-backed principal.
     *
     * The client does NOT provide a customer ID.
     */
    @Transactional(readOnly = true)
    @Override
    public List<BillResponse> getMyBills() {

        Long customerId =
                customerSecurity.getCurrentUserId();

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                findCustomer(customerId, messId);

        List<Bill> bills =
                billRepository
                        .findByMess_MessIdAndCustomerOrderByGeneratedAtDesc(
                                messId,
                                customer
                        );

        return billMapper.toResponseList(
                bills
        );
    }

    /*
     * Bill Details
     *
     * Owner can view any bill within their mess.
     *
     * Customer can view only their own bill.
     */
    @Transactional(readOnly = true)
    @Override
    public BillDetailResponse getBillDetails(
            Long billId) {

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
                                        "Bill not found with ID: "
                                                + billId
                                ));

        /*
         * Ownership validation.
         *
         * OWNER:
         *     checkCustomerAccess() permits customers in their mess.
         *
         * CUSTOMER:
         *     checkCustomerAccess() verifies that
         *     the bill belongs to the authenticated customer.
         */
        customerSecurity.checkCustomerAccess(
                bill.getCustomer().getCustomerId()
        );

        List<MealRecord> mealRecords =
                mealRecordRepository
                        .findByMess_MessIdAndBillOrderByCollectedAtAsc(
                                messId,
                                bill
                        );

        BillDetailResponse response =
                billMapper.toDetailResponse(
                        bill
                );

        response.setMealRecords(
                mealRecordMapper.toResponseList(
                        mealRecords
                )
        );

        return response;
    }

    /*
     * Billing Overview
     *
     * Owner only.
     * Bills and financial summaries are restricted to their mess.
     */
    @Transactional(readOnly = true)
    @Override
    public BillingOverviewResponse getBillingOverview(
            Integer billingMonth,
            Integer billingYear) {

        validateBillingMonth(
                billingMonth
        );

        Long messId =
                customerSecurity.getCurrentMessId();

        List<Bill> bills =
                billRepository
                        .findByMess_MessIdAndBillingMonthAndBillingYearOrderByGeneratedAtDesc(
                                messId,
                                billingMonth,
                                billingYear
                        );

        if (bills.isEmpty()) {

            throw new BusinessException(
                    "No bills found for the selected billing period."
            );
        }

        List<Object[]> summaryResult =
                billRepository.getMonthlyFinancialInsightsByMess(
                        messId,
                        billingMonth,
                        billingYear
                );

        BillingSummaryResponse summary =
                new BillingSummaryResponse();

        Object[] row =
                summaryResult.get(0);

        summary.setTotalBills(
                ((Number) row[1]).longValue()
        );

        summary.setPaidBills(
                ((Number) row[2]).longValue()
        );

        summary.setUnpaidBills(
                ((Number) row[3]).longValue()
        );

        summary.setTotalRevenue(
                (BigDecimal) row[4]
        );

        summary.setCollectedRevenue(
                (BigDecimal) row[5]
        );

        summary.setPendingRevenue(
                (BigDecimal) row[6]
        );

        List<BillResponse> billResponses =
                billMapper.toResponseList(
                        bills
                );

        BillingOverviewResponse response =
                new BillingOverviewResponse();

        response.setSummary(
                summary
        );

        response.setBills(
                billResponses
        );

        return response;
    }

    /*
     * Resolves a customer only within the authenticated mess.
     */
    private Customer findCustomer(
            Long customerId,
            Long messId) {

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
     * Build the customer-facing bill notification.
     */
    private String buildBillNotificationMessage(
            Bill bill) {

        String billingPeriod =
                buildBillingPeriod(
                        bill.getBillingMonth(),
                        bill.getBillingYear()
                );

        String formattedAmount =
                NumberFormat
                        .getCurrencyInstance(
                                INDIA_LOCALE
                        )
                        .format(
                                bill.getTotalAmount()
                        );

        return "Your bill for "
                + billingPeriod
                + " has been generated. Amount due: "
                + formattedAmount
                + ".";
    }

    /*
     * Explain why no new bills were generated.
     */
    private String buildNoBillsGeneratedMessage(
            GenerateBillRequest request,
            int activeCustomerCount,
            int existingBillCount,
            int noMealRecordCount) {

        String billingPeriod =
                buildBillingPeriod(
                        request.billingMonth(),
                        request.billingYear()
                );

        if (activeCustomerCount == 0) {
            return "No active customers were found.";
        }

        if (existingBillCount == activeCustomerCount) {

            return "Bills have already been generated for all active customers for "
                    + billingPeriod
                    + ".";
        }

        if (noMealRecordCount == activeCustomerCount) {

            return "No meal records were found for active customers for "
                    + billingPeriod
                    + ".";
        }

        return "No new bills were generated for "
                + billingPeriod
                + ". Bills already exist for "
                + existingBillCount
                + (existingBillCount == 1
                        ? " customer"
                        : " customers")
                + ", and "
                + noMealRecordCount
                + (noMealRecordCount == 1
                        ? " customer has"
                        : " customers have")
                + " no meal records.";
    }

    /*
     * Format a billing period for customer-facing messages.
     */
    private String buildBillingPeriod(
            Integer billingMonth,
            Integer billingYear) {

        String monthName =
                Month.of(
                        billingMonth
                ).getDisplayName(
                        TextStyle.FULL,
                        Locale.ENGLISH
                );

        return monthName
                + " "
                + billingYear;
    }

    private void validateBillingMonth(
            Integer billingMonth) {

        if (billingMonth == null
                || billingMonth < 1
                || billingMonth > 12) {

            throw new BusinessException(
                    "Billing month must be between 1 and 12."
            );
        }
    }
}