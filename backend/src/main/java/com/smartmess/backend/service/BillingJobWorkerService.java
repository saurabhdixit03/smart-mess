package com.smartmess.backend.service;

import com.smartmess.backend.service.BillingJobStateService.ClaimedJob;

public interface BillingJobWorkerService {

    /*
     * Discovers missing jobs for completed billing months,
     * claims ready jobs and processes them.
     *
     * Called by the scheduler without an owner session.
     */
    void runScheduledBilling();

    /*
     * Processes one claimed job.
     * Each customer's bill commits separately.
     * Retries select only remaining unbilled meals.
     */
    void execute(ClaimedJob claimedJob);
}