package com.smartmess.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import com.smartmess.backend.entity.BillingJob;
import com.smartmess.backend.repository.*;
import com.smartmess.backend.service.BillingJobStateService;
import com.smartmess.backend.service.BillingJobStateService.ClaimedJob;
import com.smartmess.backend.service.NotificationService;

class BillingJobWorkerServiceImplTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-07T04:30:00Z"), ZoneId.of("Asia/Kolkata"));
    private BillingJobRepository jobs;
    private MealRecordRepository records;
    private CustomerRepository customers;
    private BillRepository bills;
    private BillingJobStateService state;
    private NotificationService notifications;
    private BillingJobWorkerServiceImpl service;
    private ClaimedJob claimed;

    @BeforeEach void setup() {
        jobs = mock(BillingJobRepository.class);
        records = mock(MealRecordRepository.class);
        customers = mock(CustomerRepository.class);
        bills = mock(BillRepository.class);
        state = mock(BillingJobStateService.class);
        notifications = mock(NotificationService.class);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any(TransactionDefinition.class)))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        service = new BillingJobWorkerServiceImpl(jobs, customers, records, bills,
                mock(MessRepository.class), state, notifications, clock, transactions);
        claimed = new ClaimedJob(1L, 11L, 9, 2026, "lease");
        when(state.renewLease(claimed)).thenReturn(true);
        when(records.findUnbilledCustomerIdsForPeriod(eq(11L), any(), any())).thenReturn(List.of());
    }

    @Test void emptyCompletedPeriodCompletesJob() {
        service.execute(claimed);
        verify(records, times(2)).findUnbilledCustomerIdsForPeriod(11L,
                LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTime.of(2026, 10, 1, 0, 0));
        verify(state).complete(claimed);
        verify(state, never()).fail(any(), anyString());
        verifyNoInteractions(bills, notifications);
    }

    @Test void currentMonthIsRejectedBeforeCustomerDiscovery() {
        ClaimedJob current = new ClaimedJob(1L, 11L, 10, 2026, "lease");
        service.execute(current);
        verify(state).fail(eq(current), anyString());
        verifyNoInteractions(records, bills, notifications);
    }

    @Test void invalidTenantOrPeriodIsRejected() {
        for (ClaimedJob invalid : List.of(
                new ClaimedJob(1L, null, 9, 2026, "lease"),
                new ClaimedJob(1L, 11L, 0, 2026, "lease"),
                new ClaimedJob(1L, 11L, 9, 9999, "lease"))) {
            service.execute(invalid);
            verify(state).fail(eq(invalid), anyString());
        }
        verifyNoInteractions(records, bills, notifications);
    }

    @Test void lostLeaseStopsBeforeBilling() {
        when(state.renewLease(claimed)).thenReturn(false);
        service.execute(claimed);
        verifyNoInteractions(records, bills, notifications);
        verify(state, never()).complete(any());
        verify(state, never()).fail(any(), anyString());
    }

    @Test void discoveryFailureSchedulesSafeRetry() {
        when(records.findUnbilledCustomerIdsForPeriod(eq(11L), any(), any()))
                .thenThrow(new IllegalStateException("Sensitive database message"));
        service.execute(claimed);
        verify(state).fail(claimed, "Automated billing could not complete. A retry is scheduled.");
        verify(state, never()).complete(any());
    }

    @Test void remainingUnbilledCustomersKeepJobRetryable() {
        when(records.findUnbilledCustomerIdsForPeriod(eq(11L), any(), any()))
                .thenReturn(List.of()).thenReturn(List.of(22L));
        service.execute(claimed);
        verify(state).fail(eq(claimed), contains("Remaining unbilled meals"));
        verify(state, never()).complete(any());
    }

    @Test void missingLockedJobStopsWithoutGeneratingBill() {
        when(records.findUnbilledCustomerIdsForPeriod(eq(11L), any(), any())).thenReturn(List.of(22L));
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        service.execute(claimed);
        verifyNoInteractions(customers, bills, notifications);
        verify(state, never()).complete(any());
        verify(state, never()).fail(any(), anyString());
    }

    @Test void readyJobsStillRunWhenPeriodDiscoveryFails() {
        when(records.findUnbilledCompletedPeriods(any()))
                .thenThrow(new IllegalStateException("Discovery unavailable"));
        BillingJob job = mock(BillingJob.class);
        when(job.getBillingJobId()).thenReturn(1L);
        when(jobs.findReadyJobs(any(), any())).thenReturn(List.of(job));
        when(state.claim(1L)).thenReturn(Optional.of(claimed));
        service.runScheduledBilling();
        verify(state).claim(1L);
        verify(state).complete(claimed);
    }

    @Test void anotherWorkerClaimingJobPreventsExecution() {
        when(records.findUnbilledCompletedPeriods(any())).thenReturn(List.of());
        BillingJob job = mock(BillingJob.class);
        when(job.getBillingJobId()).thenReturn(1L);
        when(jobs.findReadyJobs(any(), any())).thenReturn(List.of(job));
        when(state.claim(1L)).thenReturn(Optional.empty());
        service.runScheduledBilling();
        verify(state, never()).renewLease(any());
        verifyNoInteractions(bills, notifications);
    }

    @Test void nullClaimIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null));
    }
}
