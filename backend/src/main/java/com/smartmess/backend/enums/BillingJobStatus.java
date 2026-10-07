package com.smartmess.backend.enums;

/*
 * Persistent automated billing job status.
 *
 * Job progress is stored in the database so billing can
 * resume after application downtime or a restart.
 */
public enum BillingJobStatus {

    PENDING,

    RUNNING,

    COMPLETED,

    FAILED
}