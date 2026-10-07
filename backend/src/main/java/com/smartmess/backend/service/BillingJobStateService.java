package com.smartmess.backend.service;

import java.util.Optional;

public interface BillingJobStateService {

    /*
     * Immutable execution details passed between transactions.
     */
    record ClaimedJob(
            Long jobId,
            Long messId,
            Integer billingMonth,
            Integer billingYear,
            String leaseToken
    ) {
    }

    Optional<ClaimedJob> claim(Long jobId);

    boolean renewLease(ClaimedJob claimedJob);

    boolean complete(ClaimedJob claimedJob);

    boolean fail(
            ClaimedJob claimedJob,
            String safeErrorSummary
    );
}