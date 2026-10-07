package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
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
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.BillMapper;
import com.smartmess.backend.mapper.MealRecordMapper;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.BillPaymentReceiptService;
import com.smartmess.backend.service.BillService;
import com.smartmess.backend.service.NotificationService;

@Service
public class BillServiceImpl implements BillService {

    private static final Locale INDIA_LOCALE =
            Locale.forLanguageTag("en-IN");

    private static final BigDecimal MAX_BILL_AMOUNT =
            new BigDecimal("99999999.99");

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final MealRecordRepository mealRecordRepository;
    private final BillMapper billMapper;
    private final MealRecordMapper mealRecordMapper;
    private final CustomerSecurity customerSecurity;
    private final NotificationService notificationService;
    private final Clock clock;
    private final BillPaymentReceiptService billPaymentReceiptService;

    public BillServiceImpl(
            BillRepository billRepository,
            CustomerRepository customerRepository,
            MealRecordRepository mealRecordRepository,
            BillMapper billMapper,
            MealRecordMapper mealRecordMapper,
            CustomerSecurity customerSecurity,
            NotificationService notificationService,
            Clock clock,
            BillPaymentReceiptService billPaymentReceiptService) {

        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.billMapper = billMapper;
        this.mealRecordMapper = mealRecordMapper;
        this.customerSecurity = customerSecurity;
        this.notificationService = notificationService;
        this.clock = clock;
        this.billPaymentReceiptService = billPaymentReceiptService;
    }

    /*
     * Generates additional bills from unbilled collected meals.
     * Existing bills and their meal associations remain unchanged.
     */
    @Transactional
    @Override
    public List<BillResponse> generateBills(
            GenerateBillRequest request) {

        if (customerSecurity.getCurrentUserRole() != UserRole.OWNER) {
            throw new AccessDeniedException(
                    "Only mess owners can generate bills."
            );
        }

        if (request == null) {
            throw new BusinessException(
                    "Billing request is required."
            );
        }

        YearMonth period = validateBillingPeriod(
                request.billingMonth(),
                request.billingYear()
        );

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();

        if (period.isAfter(YearMonth.from(today))) {
            throw new BusinessException(
                    "Bills cannot be generated for a future month."
            );
        }

        LocalDate startDate = request.startDate() == null
                ? period.atDay(1)
                : request.startDate();

        LocalDate defaultEndDate = period.atEndOfMonth();

        if (defaultEndDate.isAfter(today)) {
            defaultEndDate = today;
        }

        LocalDate endDate = request.endDate() == null
                ? defaultEndDate
                : request.endDate();

        validateDateRange(period, startDate, endDate, today);

        Long messId = customerSecurity.getCurrentMessId();
        Long selectedCustomerId = request.customerId();

        if (selectedCustomerId != null) {
            if (selectedCustomerId <= 0) {
                throw new BusinessException(
                        "Customer ID must be positive."
                );
            }

            Customer selectedCustomer =
                    findCustomer(selectedCustomerId, messId);

            if (!isBillingEligible(selectedCustomer)) {
                throw new BusinessException(
                        "Bills can only be generated for active or inactive customers."
                );
            }
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive =
                endDate.plusDays(1).atStartOfDay();

        /*
         * Individual generation locks only the selected customer's
         * unbilled records. Bulk generation locks the selected period.
         */
        List<MealRecord> lockedRecords =
                selectedCustomerId == null
                        ? mealRecordRepository.findUnbilledForPeriodForUpdate(
                                messId,
                                start,
                                endExclusive
                        )
                        : mealRecordRepository.findUnbilledForCustomerForUpdate(
                                messId,
                                selectedCustomerId,
                                start,
                                endExclusive
                        );

        List<MealRecord> unbilledRecords = lockedRecords.stream()
                .filter(record ->
                        isBillingEligible(record.getCustomer()))
                .toList();

        if (unbilledRecords.isEmpty()) {
            throw new BusinessException(
                    "No unbilled meal records were found"
                            + (selectedCustomerId == null
                                    ? ""
                                    : " for the selected customer")
                            + " between "
                            + startDate
                            + " and "
                            + endDate
                            + "."
            );
        }

        Map<Long, List<MealRecord>> recordsByCustomer =
                new LinkedHashMap<>();

        for (MealRecord record : unbilledRecords) {
            recordsByCustomer
                    .computeIfAbsent(
                            record.getCustomer().getCustomerId(),
                            ignored -> new ArrayList<>()
                    )
                    .add(record);
        }

        List<BillResponse> generatedBills = new ArrayList<>();

        for (List<MealRecord> records : recordsByCustomer.values()) {
            Customer customer = records.get(0).getCustomer();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (MealRecord record : records) {
                BigDecimal amount = record.getTotalAmount();

                if (amount == null || amount.signum() < 0) {
                    throw new BusinessException(
                            "Meal record "
                                    + record.getMealRecordId()
                                    + " has an invalid amount."
                    );
                }

                totalAmount = totalAmount.add(amount);
            }

            if (totalAmount.compareTo(MAX_BILL_AMOUNT) > 0) {
                throw new BusinessException(
                        "The bill amount exceeds the supported limit for customer "
                                + customer.getCustomerId()
                                + "."
                );
            }

            Bill bill = new Bill();

            bill.setMess(customer.getMess());
            bill.setCustomer(customer);
            bill.setBillingMonth(period.getMonthValue());
            bill.setBillingYear(period.getYear());
            bill.setMealRecordCount(records.size());
            bill.setTotalAmount(totalAmount);
            bill.setBillStatus(BillStatus.UNPAID);
            bill.setGeneratedAt(now);

            Bill savedBill = billRepository.save(bill);

            for (MealRecord record : records) {
                record.setBill(savedBill);
            }

            mealRecordRepository.saveAll(records);

            /*
             * Notification persistence joins this transaction.
             * Live delivery happens after commit.
             */
            notificationService.notifyCustomer(
                    customer,
                    NotificationType.BILL_GENERATED,
                    "New Bill Generated",
                    buildBillNotificationMessage(savedBill)
            );

            generatedBills.add(billMapper.toResponse(savedBill));
        }

        return generatedBills;
    }

    @Transactional(readOnly = true)
    @Override
    public List<BillResponse> getCustomerBills(Long customerId) {
        Long messId = customerSecurity.getCurrentMessId();
        Customer customer = findCustomer(customerId, messId);

        customerSecurity.checkCustomerAccess(customerId);

        List<Bill> bills = billRepository
                .findByMess_MessIdAndCustomerOrderByGeneratedAtDesc(
                        messId,
                        customer
                );

        return billMapper.toResponseList(bills);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BillResponse> getMyBills() {
        Long customerId = customerSecurity.getCurrentUserId();
        Long messId = customerSecurity.getCurrentMessId();

        Customer customer = findCustomer(customerId, messId);

        List<Bill> bills = billRepository
                .findByMess_MessIdAndCustomerOrderByGeneratedAtDesc(
                        messId,
                        customer
                );

        return billMapper.toResponseList(bills);
    }

    @Transactional(readOnly = true)
    @Override
    public BillDetailResponse getBillDetails(Long billId) {
        Long messId = customerSecurity.getCurrentMessId();

        Bill bill = billRepository
                .findByBillIdAndMess_MessId(billId, messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill not found with ID: " + billId
                ));

        customerSecurity.checkCustomerAccess(
                bill.getCustomer().getCustomerId()
        );

        List<MealRecord> records = mealRecordRepository
                .findByMess_MessIdAndBillOrderByCollectedAtAsc(
                        messId,
                        bill
                );

        BillDetailResponse response =
                billMapper.toDetailResponse(bill);

        response.setMealRecords(
                mealRecordMapper.toResponseList(records)
        );

        response.setPayment(
                billPaymentReceiptService.getReceipt(bill)
        );

        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public BillingOverviewResponse getBillingOverview(
            Integer billingMonth,
            Integer billingYear) {

        if (customerSecurity.getCurrentUserRole() != UserRole.OWNER) {
            throw new AccessDeniedException(
                    "Only mess owners can access billing reporting."
            );
        }

        boolean hasMonth = billingMonth != null;
        boolean hasYear = billingYear != null;

        if (hasMonth != hasYear) {
            throw new BusinessException(
                    "Supply both billing month and year, or omit both for all periods."
            );
        }

        if (hasMonth) {
            validateBillingPeriod(billingMonth, billingYear);
        }

        Long messId = customerSecurity.getCurrentMessId();

        List<Bill> bills = billRepository.findForOverview(
                messId,
                billingMonth,
                billingYear
        );

        BillingSummaryResponse summary =
                new BillingSummaryResponse();

        summary.setTotalBills(0L);
        summary.setPaidBills(0L);
        summary.setUnpaidBills(0L);
        summary.setTotalRevenue(BigDecimal.ZERO);
        summary.setCollectedRevenue(BigDecimal.ZERO);
        summary.setPendingRevenue(BigDecimal.ZERO);

        List<Object[]> summaryRows = billRepository
                .getFinancialSummaryForOverview(
                        messId,
                        billingMonth,
                        billingYear
                );

        if (!summaryRows.isEmpty()) {
            Object[] row = summaryRows.get(0);

            summary.setTotalBills(asLong(row[0]));
            summary.setPaidBills(asLong(row[1]));
            summary.setUnpaidBills(asLong(row[2]));
            summary.setTotalRevenue(asAmount(row[3]));
            summary.setCollectedRevenue(asAmount(row[4]));
            summary.setPendingRevenue(asAmount(row[5]));
        }

        BillingOverviewResponse response =
                new BillingOverviewResponse();

        response.setSummary(summary);
        response.setBills(billMapper.toResponseList(bills));

        return response;
    }

    private boolean isBillingEligible(Customer customer) {
        return customer.getStatus() == CustomerStatus.ACTIVE
                || customer.getStatus() == CustomerStatus.INACTIVE;
    }

    private void validateDateRange(
            YearMonth period,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate today) {

        if (!YearMonth.from(startDate).equals(period)
                || !YearMonth.from(endDate).equals(period)) {
            throw new BusinessException(
                    "Billing dates must belong to the selected billing month and year."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    "Billing start date must not be after the end date."
            );
        }

        if (startDate.isAfter(today) || endDate.isAfter(today)) {
            throw new BusinessException(
                    "Billing dates cannot be in the future."
            );
        }
    }

    private Customer findCustomer(Long customerId, Long messId) {
        return customerRepository
                .findByCustomerIdAndMess_MessId(customerId, messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId
                ));
    }

    private String buildBillNotificationMessage(Bill bill) {
        String amount = NumberFormat
                .getCurrencyInstance(INDIA_LOCALE)
                .format(bill.getTotalAmount());

        return "Bill #"
                + bill.getBillId()
                + " for "
                + buildBillingPeriod(
                        bill.getBillingMonth(),
                        bill.getBillingYear()
                )
                + " has been generated for "
                + bill.getMealRecordCount()
                + " collected meals. Amount due: "
                + amount
                + ".";
    }

    private String buildBillingPeriod(
            Integer billingMonth,
            Integer billingYear) {

        return Month.of(billingMonth)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " "
                + billingYear;
    }

    private YearMonth validateBillingPeriod(
            Integer billingMonth,
            Integer billingYear) {

        if (billingMonth == null
                || billingMonth < 1
                || billingMonth > 12) {
            throw new BusinessException(
                    "Billing month must be between 1 and 12."
            );
        }

        if (billingYear == null
                || billingYear < 1000
                || billingYear > 9998) {
            throw new BusinessException(
                    "Billing year must be between 1000 and 9998."
            );
        }

        return YearMonth.of(billingYear, billingMonth);
    }

    private long asLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private BigDecimal asAmount(Object value) {
        return value == null ? BigDecimal.ZERO : (BigDecimal) value;
    }
}