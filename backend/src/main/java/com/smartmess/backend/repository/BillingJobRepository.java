package com.smartmess.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.BillingJob;

import jakarta.persistence.LockModeType;

public interface BillingJobRepository
        extends JpaRepository<BillingJob, Long> {

    boolean existsByMess_MessIdAndBillingMonthAndBillingYear(
            Long messId,
            Integer billingMonth,
            Integer billingYear
    );

    Optional<BillingJob> findByMess_MessIdAndBillingMonthAndBillingYear(
            Long messId,
            Integer billingMonth,
            Integer billingYear
    );

    /*
     * Discovery only.
     *
     * The worker must lock and recheck each job before claiming it.
     * Multiple application instances may discover the same candidate.
     */
    @Query("""
            SELECT job
            FROM BillingJob job
            WHERE (
                job.status IN (
                    com.smartmess.backend.enums.BillingJobStatus.PENDING,
                    com.smartmess.backend.enums.BillingJobStatus.FAILED
                )
                AND (
                    job.nextAttemptAt IS NULL
                    OR job.nextAttemptAt <= :now
                )
            )
            OR (
                job.status =
                    com.smartmess.backend.enums.BillingJobStatus.RUNNING
                AND (
                    job.leaseExpiresAt IS NULL
                    OR job.leaseExpiresAt <= :now
                )
            )
            ORDER BY job.billingYear ASC,
                     job.billingMonth ASC,
                     job.billingJobId ASC
            """)
    List<BillingJob> findReadyJobs(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /*
     * Used inside a short transaction to claim, renew
     * or finish a job after verifying its lease token.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT job
            FROM BillingJob job
            WHERE job.billingJobId = :jobId
            """)
    Optional<BillingJob> findByIdForUpdate(
            @Param("jobId") Long jobId
    );
}