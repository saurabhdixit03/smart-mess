
package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.BillMapper;
import com.smartmess.backend.mapper.MealRecordMapper;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.BillService;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    private final CustomerRepository customerRepository;

    private final MealRecordRepository mealRecordRepository;

    private final BillMapper billMapper;

    private final MealRecordMapper mealRecordMapper;

    private final CustomerSecurity customerSecurity;

    public BillServiceImpl(
            BillRepository billRepository,
            CustomerRepository customerRepository,
            MealRecordRepository mealRecordRepository,
            BillMapper billMapper,
            MealRecordMapper mealRecordMapper,
            CustomerSecurity customerSecurity) {

        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.billMapper = billMapper;
        this.mealRecordMapper = mealRecordMapper;
        this.customerSecurity = customerSecurity;
    }

    /*
     * Generate Bills
     *
     * Owner only.
     */
    @Override
    public List<BillResponse> generateBills(
            GenerateBillRequest request) {

        List<Customer> customers =
                customerRepository.findByStatus(
                        CustomerStatus.ACTIVE
                );

        List<BillResponse> generatedBills =
                new ArrayList<>();

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
                            .existsByCustomerAndBillingMonthAndBillingYear(
                                    customer,
                                    request.billingMonth(),
                                    request.billingYear()
                            );

            if (billExists) {
                continue;
            }

            /*
             * Get meal records for the customer's
             * selected billing period.
             */
            List<MealRecord> mealRecords =
                    mealRecordRepository
                            .findByCustomerAndCollectedAtBetween(
                                    customer,
                                    start,
                                    end
                            );

            /*
             * Skip customers having no meal records.
             */
            if (mealRecords.isEmpty()) {
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

            bill.setCustomer(customer);

            bill.setBillingMonth(
                    request.billingMonth()
            );

            bill.setBillingYear(
                    request.billingYear()
            );

            bill.setMealRecordCount(
                    mealRecords.size()
            );

            bill.setTotalAmount(
                    totalAmount
            );

            bill.setBillStatus(
                    BillStatus.UNPAID
            );

            Bill savedBill =
                    billRepository.save(bill);

            /*
             * Link Meal Records to the generated Bill.
             */
            for (MealRecord mealRecord : mealRecords) {

                mealRecord.setBill(savedBill);

            }

            mealRecordRepository.saveAll(mealRecords);

            generatedBills.add(
                    billMapper.toResponse(savedBill)
            );
        }

        if (generatedBills.isEmpty()) {

            throw new BusinessException(
                    "No bills were generated for the selected billing period."
            );
        }

        return generatedBills;
    }

    /*
     * Customer Bill History
     *
     * Owner use case.
     *
     * The owner can request bills for any customer.
     * Authorization is handled at the controller level.
     */
    @Override
    public List<BillResponse> getCustomerBills(
            Long customerId) {

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with ID: "
                                                + customerId
                                ));

        List<Bill> bills =
                billRepository
                        .findByCustomerOrderByGeneratedAtDesc(
                                customer
                        );

        return billMapper.toResponseList(
                bills
        );
    }

    /*
     * Authenticated Customer Bill History
     *
     * Customer ID comes directly from the JWT.
     *
     * The client does NOT provide a customer ID.
     */
    @Override
    public List<BillResponse> getMyBills() {

        Long customerId =
                customerSecurity.getCurrentUserId();

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with ID: "
                                                + customerId
                                ));

        List<Bill> bills =
                billRepository
                        .findByCustomerOrderByGeneratedAtDesc(
                                customer
                        );

        return billMapper.toResponseList(
                bills
        );
    }

    /*
     * Bill Details
     *
     * Owner can view any bill.
     *
     * Customer can view only their own bill.
     */
    @Override
    public BillDetailResponse getBillDetails(
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

        List<MealRecord> mealRecords =
                mealRecordRepository
                        .findByBillOrderByCollectedAtAsc(
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
     */
    @Override
    public BillingOverviewResponse getBillingOverview(
            Integer billingMonth,
            Integer billingYear) {

        List<Bill> bills =
                billRepository
                        .findByBillingMonthAndBillingYearOrderByGeneratedAtDesc(
                                billingMonth,
                                billingYear
                        );

        if (bills.isEmpty()) {

            throw new BusinessException(
                    "No bills found for the selected billing period."
            );
        }

        List<Object[]> summaryResult =
                billRepository.getMonthlyFinancialInsights(
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
}
