package com.smartmess.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.smartmess.backend.entity.BillingJob;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.enums.BillingJobStatus;
import com.smartmess.backend.repository.BillingJobRepository;
import com.smartmess.backend.service.BillingJobStateService.ClaimedJob;

class BillingJobStateServiceImplTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-07T04:30:00Z"), ZoneId.of("Asia/Kolkata"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private BillingJobRepository repository;
    private BillingJobStateServiceImpl service;
    private BillingJob job;

    @BeforeEach void setup() {
        repository = mock(BillingJobRepository.class);
        service = new BillingJobStateServiceImpl(repository, clock);
        Mess mess = mock(Mess.class);
        when(mess.getMessId()).thenReturn(11L);
        job = new BillingJob();
        job.setBillingJobId(1L);
        job.setMess(mess);
        job.setBillingMonth(9);
        job.setBillingYear(2026);
        job.setNextAttemptAt(now);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(job));
    }

    private ClaimedJob claim() { return service.claim(1L).orElseThrow(); }

    @Test void pendingJobReceivesLeaseAndCannotBeClaimedTwice() {
        ClaimedJob claimed = claim();
        assertEquals(BillingJobStatus.RUNNING, job.getStatus());
        assertEquals(1, job.getAttemptCount());
        assertEquals(now.plusMinutes(5), job.getLeaseExpiresAt());
        assertEquals(job.getLeaseToken(), claimed.leaseToken());
        assertEquals(11L, claimed.messId());
        assertNull(job.getNextAttemptAt());
        assertTrue(service.claim(1L).isEmpty());
        verify(repository, times(1)).save(job);
    }

    @Test void futureRetryIsNotClaimed() {
        job.setStatus(BillingJobStatus.FAILED);
        job.setNextAttemptAt(now.plusSeconds(1));
        assertTrue(service.claim(1L).isEmpty());
        verify(repository, never()).save(any());
    }

    @Test void retryDueExactlyNowIsClaimed() {
        job.setStatus(BillingJobStatus.FAILED);
        assertTrue(service.claim(1L).isPresent());
    }

    @Test void expiredRunningJobGetsNewTokenAndRejectsOldWorker() {
        ClaimedJob old = claim();
        job.setLeaseExpiresAt(now);
        ClaimedJob replacement = claim();
        assertNotEquals(old.leaseToken(), replacement.leaseToken());
        assertEquals(2, job.getAttemptCount());
        assertFalse(service.renewLease(old));
        assertFalse(service.complete(old));
        assertFalse(service.fail(old, "Old worker"));
        assertEquals(BillingJobStatus.RUNNING, job.getStatus());
    }

    @Test void runningJobWithoutLeaseCanBeRecovered() {
        job.setStatus(BillingJobStatus.RUNNING);
        job.setLeaseExpiresAt(null);
        assertTrue(service.claim(1L).isPresent());
    }

    @Test void activeLeaseCanBeRenewed() {
        ClaimedJob claimed = claim();
        job.setLeaseExpiresAt(now.plusSeconds(1));
        assertTrue(service.renewLease(claimed));
        assertEquals(now.plusMinutes(5), job.getLeaseExpiresAt());
    }

    @Test void expiredLeaseCannotRenewCompleteOrFail() {
        ClaimedJob claimed = claim();
        job.setLeaseExpiresAt(now);
        assertFalse(service.renewLease(claimed));
        assertFalse(service.complete(claimed));
        assertFalse(service.fail(claimed, "Failure"));
    }

    @Test void differentTenantCannotChangeJobState() {
        ClaimedJob claimed = claim();
        ClaimedJob foreign = new ClaimedJob(1L, 99L, 9, 2026, claimed.leaseToken());
        assertFalse(service.renewLease(foreign));
        assertFalse(service.complete(foreign));
        assertFalse(service.fail(foreign, "Failure"));
    }

    @Test void completionClearsLeaseAndCannotBeReclaimed() {
        ClaimedJob claimed = claim();
        job.setLastError("Previous failure");
        assertTrue(service.complete(claimed));
        assertEquals(BillingJobStatus.COMPLETED, job.getStatus());
        assertEquals(now, job.getCompletedAt());
        assertNull(job.getLastError());
        assertNull(job.getLeaseToken());
        assertNull(job.getLeaseExpiresAt());
        assertTrue(service.claim(1L).isEmpty());
    }

    @Test void retryBackoffIsCappedAtSixtyMinutes() {
        int[] attempts = {1, 2, 3, 4, 5, 6, 7, 100};
        long[] minutes = {1, 2, 4, 8, 16, 32, 60, 60};
        for (int i = 0; i < attempts.length; i++) {
            job.setStatus(BillingJobStatus.RUNNING);
            job.setLeaseToken("lease");
            job.setLeaseExpiresAt(now.plusMinutes(5));
            job.setAttemptCount(attempts[i]);
            assertTrue(service.fail(new ClaimedJob(1L, 11L, 9, 2026, "lease"), "Retry"));
            assertEquals(now.plusMinutes(minutes[i]), job.getNextAttemptAt());
            assertEquals(BillingJobStatus.FAILED, job.getStatus());
            assertNull(job.getLeaseToken());
        }
    }

    @Test void failureSummaryIsBoundedAndHasFallback() {
        ClaimedJob claimed = claim();
        assertTrue(service.fail(claimed, "x".repeat(1200)));
        assertEquals(1000, job.getLastError().length());
        job.setNextAttemptAt(now);
        assertTrue(service.fail(claim(), " "));
        assertEquals("Automated billing failed. A retry is scheduled.", job.getLastError());
    }

    @Test void missingJobIsNotClaimed() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertTrue(service.claim(1L).isEmpty());
    }
}
