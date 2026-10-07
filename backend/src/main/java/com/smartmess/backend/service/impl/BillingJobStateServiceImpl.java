package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.BillingJob;
import com.smartmess.backend.enums.BillingJobStatus;
import com.smartmess.backend.repository.BillingJobRepository;
import com.smartmess.backend.service.BillingJobStateService;

@Service
public class BillingJobStateServiceImpl implements BillingJobStateService {

    private static final long LEASE_MINUTES = 5;

    private final BillingJobRepository billingJobRepository;
    private final Clock clock;

    public BillingJobStateServiceImpl(
            BillingJobRepository billingJobRepository,
            Clock clock) {

        this.billingJobRepository = billingJobRepository;
        this.clock = clock;
    }

    @Transactional
    @Override
    public Optional<ClaimedJob> claim(Long jobId) {

        Optional<BillingJob> result =
                billingJobRepository.findByIdForUpdate(jobId);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        BillingJob job = result.get();
        LocalDateTime now = LocalDateTime.now(clock);

        if (!isReady(job, now)) {
            return Optional.empty();
        }

        String leaseToken = UUID.randomUUID().toString();

        job.setStatus(BillingJobStatus.RUNNING);
        job.setAttemptCount(job.getAttemptCount() + 1);
        job.setStartedAt(now);
        job.setCompletedAt(null);
        job.setNextAttemptAt(null);
        job.setLeaseToken(leaseToken);
        job.setLeaseExpiresAt(now.plusMinutes(LEASE_MINUTES));

        billingJobRepository.save(job);

        return Optional.of(new ClaimedJob(
                job.getBillingJobId(),
                job.getMess().getMessId(),
                job.getBillingMonth(),
                job.getBillingYear(),
                leaseToken
        ));
    }

    @Transactional
    @Override
    public boolean renewLease(ClaimedJob claimedJob) {

        Optional<BillingJob> result =
                billingJobRepository.findByIdForUpdate(claimedJob.jobId());

        if (result.isEmpty()) {
            return false;
        }

        BillingJob job = result.get();
        LocalDateTime now = LocalDateTime.now(clock);

        if (!ownsActiveLease(job, claimedJob, now)) {
            return false;
        }

        job.setLeaseExpiresAt(now.plusMinutes(LEASE_MINUTES));

        billingJobRepository.save(job);

        return true;
    }

    @Transactional
    @Override
    public boolean complete(ClaimedJob claimedJob) {

        Optional<BillingJob> result =
                billingJobRepository.findByIdForUpdate(claimedJob.jobId());

        if (result.isEmpty()) {
            return false;
        }

        BillingJob job = result.get();
        LocalDateTime now = LocalDateTime.now(clock);

        if (!ownsActiveLease(job, claimedJob, now)) {
            return false;
        }

        job.setStatus(BillingJobStatus.COMPLETED);
        job.setCompletedAt(now);
        job.setNextAttemptAt(null);
        job.setLastError(null);

        clearLease(job);

        billingJobRepository.save(job);

        return true;
    }

    /*
     * Callers supply a safe operational summary.
     * Do not pass raw exception messages or credentials.
     */
    @Transactional
    @Override
    public boolean fail(
            ClaimedJob claimedJob,
            String safeErrorSummary) {

        Optional<BillingJob> result =
                billingJobRepository.findByIdForUpdate(claimedJob.jobId());

        if (result.isEmpty()) {
            return false;
        }

        BillingJob job = result.get();
        LocalDateTime now = LocalDateTime.now(clock);

        if (!ownsActiveLease(job, claimedJob, now)) {
            return false;
        }

        /*
         * Retry after 1, 2, 4, 8, 16, 32 and then 60 minutes.
         */
        int exponent = Math.min(
                Math.max(job.getAttemptCount() - 1, 0),
                6
        );

        long retryMinutes = Math.min(1L << exponent, 60L);

        job.setStatus(BillingJobStatus.FAILED);
        job.setCompletedAt(null);
        job.setNextAttemptAt(now.plusMinutes(retryMinutes));
        job.setLastError(boundedSummary(safeErrorSummary));

        clearLease(job);

        billingJobRepository.save(job);

        return true;
    }

    private boolean isReady(
            BillingJob job,
            LocalDateTime now) {

        if (job.getStatus() == BillingJobStatus.COMPLETED) {
            return false;
        }

        if (job.getStatus() == BillingJobStatus.RUNNING) {
            return job.getLeaseExpiresAt() == null
                    || !job.getLeaseExpiresAt().isAfter(now);
        }

        if (job.getStatus() == BillingJobStatus.PENDING
                || job.getStatus() == BillingJobStatus.FAILED) {
            return job.getNextAttemptAt() == null
                    || !job.getNextAttemptAt().isAfter(now);
        }

        return false;
    }

    private boolean ownsActiveLease(
            BillingJob job,
            ClaimedJob claimedJob,
            LocalDateTime now) {

        return job.getStatus() == BillingJobStatus.RUNNING
                && claimedJob.leaseToken() != null
                && claimedJob.leaseToken().equals(job.getLeaseToken())
                && claimedJob.messId().equals(
                        job.getMess().getMessId()
                )
                && job.getLeaseExpiresAt() != null
                && job.getLeaseExpiresAt().isAfter(now);
    }

    private void clearLease(BillingJob job) {
        job.setLeaseToken(null);
        job.setLeaseExpiresAt(null);
    }

    private String boundedSummary(String summary) {

        if (summary == null || summary.isBlank()) {
            return "Automated billing failed. A retry is scheduled.";
        }

        return summary.substring(0, Math.min(summary.length(), 1000));
    }
}