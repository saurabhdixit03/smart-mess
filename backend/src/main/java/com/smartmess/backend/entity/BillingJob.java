package com.smartmess.backend.entity;

import java.time.LocalDateTime;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.BillingJobStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "billing_jobs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_billing_jobs_mess_period",
                        columnNames = {
                                "mess_id",
                                "billing_month",
                                "billing_year"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_billing_jobs_status_retry",
                        columnList = "status, next_attempt_at"
                ),
                @Index(
                        name = "idx_billing_jobs_status_lease",
                        columnList = "status, lease_expires_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BillingJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_job_id")
    private Long billingJobId;

    /*
     * Explicit tenant ownership.
     * Scheduled execution does not depend on an owner login.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BillingJobStatus status = BillingJobStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    /*
     * A worker receives a temporary lease.
     * An expired lease allows recovery after a restart.
     *
     * The token identifies the execution that owns the lease.
     */
    @Column(name = "lease_token", length = 36)
    private String leaseToken;

    @Column(name = "lease_expires_at")
    private LocalDateTime leaseExpiresAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /*
     * Store a bounded operational error summary.
     * Credentials and raw provider responses must not be stored here.
     */
    @Column(name = "last_error", length = 1000)
    private String lastError;

    /*
     * Detect conflicting updates to the same job.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}