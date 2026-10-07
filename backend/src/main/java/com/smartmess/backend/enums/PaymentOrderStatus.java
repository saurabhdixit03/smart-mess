package com.smartmess.backend.enums;

public enum PaymentOrderStatus {

    /*
     * Local order saved; gateway creation is in progress.
     */
    CREATING,

    /*
     * Gateway order is available for checkout.
     */
    ACTIVE,

    /*
     * Successful payment verified by the backend.
     */
    PAID,

    EXPIRED,

    TERMINATION_REQUESTED,

    TERMINATED,

    /*
     * Gateway explicitly rejected order creation.
     */
    CREATION_FAILED,

    /*
     * Outcome is uncertain, for example after a timeout.
     * Verify the existing order before creating another.
     */
    RECONCILIATION_REQUIRED
}