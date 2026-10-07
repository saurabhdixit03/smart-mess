package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.smartmess.backend.entity.Bill;
import com.smartmess.backend.entity.BillingJob;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.enums.BillingJobStatus;
import com.smartmess.backend.enums.BillStatus;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.BillingJobRepository;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.BillingJobStateService;
import com.smartmess.backend.service.BillingJobStateService.ClaimedJob;
import com.smartmess.backend.service.BillingJobWorkerService;
import com.smartmess.backend.service.NotificationService;

@Service
public class BillingJobWorkerServiceImpl
        implements BillingJobWorkerService {

    private static final Logger log =
            LoggerFactory.getLogger(BillingJobWorkerServiceImpl.class);

    private static final BigDecimal MAX_BILL_AMOUNT =
            new BigDecimal("99999999.99");

    private static final int JOBS_PER_RUN = 10;

    private final BillingJobRepository billingJobRepository;
    private final CustomerRepository customerRepository;
    private final MealRecordRepository mealRecordRepository;
    private final BillRepository billRepository;
    private final MessRepository messRepository;
    private final BillingJobStateService jobStateService;
    private final NotificationService notificationService;
    private final Clock clock;

    private final TransactionTemplate readTransaction;
    private final TransactionTemplate writeTransaction;

    public BillingJobWorkerServiceImpl(
            BillingJobRepository billingJobRepository,
            CustomerRepository customerRepository,
            MealRecordRepository mealRecordRepository,
            BillRepository billRepository,
            MessRepository messRepository,
            BillingJobStateService jobStateService,
            NotificationService notificationService,
            Clock clock,
            PlatformTransactionManager transactionManager) {

        this.billingJobRepository = billingJobRepository;
        this.customerRepository = customerRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.billRepository = billRepository;
        this.messRepository = messRepository;
        this.jobStateService = jobStateService;
        this.notificationService = notificationService;
        this.clock = clock;

        this.readTransaction = new TransactionTemplate(transactionManager);
        this.readTransaction.setReadOnly(true);
        this.readTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        this.writeTransaction = new TransactionTemplate(transactionManager);
        this.writeTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
    }

    /*
     * Prevent overlapping scheduled runs in this application instance.
     * Database job locks coordinate separate application instances.
     */
    @Override
    public synchronized void runScheduledBilling() {

        try {
            discoverMissingJobs();
        } catch (RuntimeException exception) {
            /*
             * Existing retryable jobs can still run even if
             * discovery temporarily fails.
             */
            log.error("Automated billing job discovery failed.", exception);
        }

        List<Long> readyJobIds;

        try {
            readyJobIds = readTransaction.execute(status ->
                    billingJobRepository.findReadyJobs(
                            LocalDateTime.now(clock),
                            PageRequest.of(0, JOBS_PER_RUN)
                    )
                    .stream()
                    .map(BillingJob::getBillingJobId)
                    .toList()
            );
        } catch (RuntimeException exception) {
            log.error("Could not load ready billing jobs.", exception);
            return;
        }

        if (readyJobIds == null) {
            return;
        }

        for (Long jobId : readyJobIds) {
            try {
                jobStateService.claim(jobId).ifPresent(this::execute);
            } catch (RuntimeException exception) {
                log.error(
                        "Could not process automated billing job {}.",
                        jobId,
                        exception
                );
            }
        }
    }

    private void discoverMissingJobs() {

        LocalDateTime completedBefore = YearMonth.now(clock)
                .atDay(1)
                .atStartOfDay();

        List<Object[]> periods = readTransaction.execute(status ->
                mealRecordRepository.findUnbilledCompletedPeriods(
                        completedBefore
                )
        );

        if (periods == null) {
            throw new IllegalStateException(
                    "Billing period discovery returned no result."
            );
        }

        for (Object[] row : periods) {

            Long messId = ((Number) row[0]).longValue();
            Integer year = ((Number) row[1]).intValue();
            Integer month = ((Number) row[2]).intValue();

            try {
                writeTransaction.executeWithoutResult(status ->
                        createMissingJob(messId, month, year)
                );
            } catch (DataIntegrityViolationException exception) {
                /*
                 * Another instance may have created the same
                 * mess/month job. Check after this transaction
                 * has rolled back.
                 */
                Boolean jobExists = readTransaction.execute(status ->
                        billingJobRepository
                                .existsByMess_MessIdAndBillingMonthAndBillingYear(
                                        messId,
                                        month,
                                        year
                                )
                );

                if (!Boolean.TRUE.equals(jobExists)) {
                    log.error(
                            "Could not create billing job for mess {}, period {}-{}.",
                            messId,
                            year,
                            month,
                            exception
                    );
                }
            } catch (RuntimeException exception) {
                log.error(
                        "Could not create billing job for mess {}, period {}-{}.",
                        messId,
                        year,
                        month,
                        exception
                );
            }
        }
    }

    private void createMissingJob(
            Long messId,
            Integer month,
            Integer year) {

        if (billingJobRepository
                .existsByMess_MessIdAndBillingMonthAndBillingYear(
                        messId,
                        month,
                        year
                )) {
            return;
        }

        Mess mess = messRepository.findById(messId)
                .orElseThrow(() -> new BusinessException(
                        "Billing mess no longer exists."
                ));

        BillingJob job = new BillingJob();

        job.setMess(mess);
        job.setBillingMonth(month);
        job.setBillingYear(year);
        job.setStatus(BillingJobStatus.PENDING);
        job.setAttemptCount(0);
        job.setNextAttemptAt(LocalDateTime.now(clock));

        billingJobRepository.saveAndFlush(job);
    }

    /*
     * No surrounding batch transaction.
     * Each customer's bill is committed independently.
     */
    @Override
    public void execute(ClaimedJob claimedJob) {

        if (claimedJob == null) {
            throw new IllegalArgumentException(
                    "Claimed billing job is required."
            );
        }

        try {
            YearMonth period = validatePeriod(claimedJob);

            LocalDateTime start = period.atDay(1).atStartOfDay();
            LocalDateTime endExclusive =
                    period.plusMonths(1).atDay(1).atStartOfDay();

            if (!jobStateService.renewLease(claimedJob)) {
                return;
            }

            List<Long> customerIds = discoverCustomers(
                    claimedJob.messId(),
                    start,
                    endExclusive
            );

            boolean customerFailed = false;

            for (Long customerId : customerIds) {

                if (!jobStateService.renewLease(claimedJob)) {
                    return;
                }

                try {
                    writeTransaction.executeWithoutResult(status ->
                            billCustomer(
                                    claimedJob,
                                    customerId,
                                    start,
                                    endExclusive
                            )
                    );
                } catch (LeaseLostException exception) {
                    return;
                } catch (RuntimeException exception) {
                    customerFailed = true;

                    log.error(
                            "Automated billing failed for job {}, customer {}.",
                            claimedJob.jobId(),
                            customerId,
                            exception
                    );
                }
            }

            if (!jobStateService.renewLease(claimedJob)) {
                return;
            }

            boolean remainingRecords = !discoverCustomers(
                    claimedJob.messId(),
                    start,
                    endExclusive
            ).isEmpty();

            if (customerFailed || remainingRecords) {
                jobStateService.fail(
                        claimedJob,
                        "Some customer bills could not be completed. "
                                + "Remaining unbilled meals will be retried."
                );
                return;
            }

            if (jobStateService.complete(claimedJob)) {
                log.info(
                        "Automated billing job {} completed for mess {}, period {}.",
                        claimedJob.jobId(),
                        claimedJob.messId(),
                        period
                );
            }

        } catch (RuntimeException exception) {
            log.error(
                    "Automated billing job {} failed.",
                    claimedJob.jobId(),
                    exception
            );

            jobStateService.fail(
                    claimedJob,
                    "Automated billing could not complete. "
                            + "A retry is scheduled."
            );
        }
    }

    private List<Long> discoverCustomers(
            Long messId,
            LocalDateTime start,
            LocalDateTime endExclusive) {

        List<Long> customerIds = readTransaction.execute(status ->
                mealRecordRepository.findUnbilledCustomerIdsForPeriod(
                        messId,
                        start,
                        endExclusive
                )
        );

        if (customerIds == null) {
            throw new IllegalStateException(
                    "Billing customer discovery returned no result."
            );
        }

        return customerIds;
    }

    /*
     * Lock order: job, customer, meal records.
     * The job lock prevents reclamation during this transaction.
     */
    private void billCustomer(
            ClaimedJob claimedJob,
            Long customerId,
            LocalDateTime start,
            LocalDateTime endExclusive) {

        BillingJob job = billingJobRepository
                .findByIdForUpdate(claimedJob.jobId())
                .orElseThrow(LeaseLostException::new);

        LocalDateTime now = LocalDateTime.now(clock);

        verifyLease(job, claimedJob, now);

        Customer customer = customerRepository
                .findByCustomerIdAndMessIdForUpdate(
                        customerId,
                        claimedJob.messId()
                )
                .orElseThrow(() -> new BusinessException(
                        "Billing customer no longer exists."
                ));

        if (customer.getStatus() != CustomerStatus.ACTIVE
                && customer.getStatus() != CustomerStatus.INACTIVE) {
            return;
        }

        List<MealRecord> records = mealRecordRepository
                .findUnbilledForCustomerForUpdate(
                        claimedJob.messId(),
                        customerId,
                        start,
                        endExclusive
                );

        if (records.isEmpty()) {
            return;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MealRecord record : records) {

            BigDecimal amount = record.getTotalAmount();

            if (amount == null || amount.signum() < 0) {
                throw new BusinessException(
                        "A meal record has an invalid billing amount."
                );
            }

            totalAmount = totalAmount.add(amount);
        }

        if (totalAmount.compareTo(MAX_BILL_AMOUNT) > 0) {
            throw new BusinessException(
                    "The customer bill exceeds the supported amount."
            );
        }

        Bill bill = new Bill();

        bill.setMess(customer.getMess());
        bill.setCustomer(customer);
        bill.setBillingMonth(claimedJob.billingMonth());
        bill.setBillingYear(claimedJob.billingYear());
        bill.setMealRecordCount(records.size());
        bill.setTotalAmount(totalAmount);
        bill.setBillStatus(BillStatus.UNPAID);
        bill.setGeneratedAt(now);

        Bill savedBill = billRepository.save(bill);

        for (MealRecord record : records) {
            record.setBill(savedBill);
        }

        mealRecordRepository.saveAll(records);

        job.setLeaseExpiresAt(
                LocalDateTime.now(clock).plusMinutes(5)
        );

        billingJobRepository.save(job);

        notificationService.notifyAutomatedBill(
                claimedJob.jobId(),
                claimedJob.leaseToken(),
                savedBill.getBillId()
        );
    }

    private void verifyLease(
            BillingJob job,
            ClaimedJob claimedJob,
            LocalDateTime now) {

        if (job.getStatus() != BillingJobStatus.RUNNING
                || claimedJob.leaseToken() == null
                || !claimedJob.leaseToken().equals(job.getLeaseToken())
                || !claimedJob.messId().equals(
                        job.getMess().getMessId()
                )
                || !claimedJob.billingMonth().equals(job.getBillingMonth())
                || !claimedJob.billingYear().equals(job.getBillingYear())
                || job.getLeaseExpiresAt() == null
                || !job.getLeaseExpiresAt().isAfter(now)) {
            throw new LeaseLostException();
        }
    }

    private YearMonth validatePeriod(ClaimedJob claimedJob) {

        if (claimedJob.messId() == null
                || claimedJob.billingMonth() == null
                || claimedJob.billingMonth() < 1
                || claimedJob.billingMonth() > 12
                || claimedJob.billingYear() == null
                || claimedJob.billingYear() < 1000
                || claimedJob.billingYear() > 9998) {
            throw new BusinessException(
                    "Automated billing job has an invalid period or tenant."
            );
        }

        YearMonth period = YearMonth.of(
                claimedJob.billingYear(),
                claimedJob.billingMonth()
        );

        if (!period.isBefore(YearMonth.now(clock))) {
            throw new BusinessException(
                    "Automated billing processes completed months only."
            );
        }

        return period;
    }

    private static class LeaseLostException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}