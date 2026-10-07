package com.smartmess.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.smartmess.backend.entity.*;
import com.smartmess.backend.enums.*;
import com.smartmess.backend.repository.BillingJobRepository;
import com.smartmess.backend.service.BillingJobStateService;
import com.smartmess.backend.service.BillingJobStateService.ClaimedJob;
import com.smartmess.backend.service.BillingJobWorkerService;
import com.smartmess.backend.service.EmailDeliveryService;
import com.smartmess.backend.service.EmailService;

@SpringBootTest(classes = {com.smartmess.backend.BackendApplication.class,
    BillingWorkerDatabaseIntegrationTest.FixedClockConfiguration.class}, properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/smart_mess_worker_test",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "app.seed-demo-data=false",
    "app.billing.automation.enabled=false",
    "app.mail.notifications.enabled=false",
    "app.cashfree.enabled=false",
    "app.mail.brevo.api-key=unused-in-integration-tests"
})
@ContextConfiguration(initializers = BillingWorkerDatabaseIntegrationTest.TestDatabaseGuard.class)
class BillingWorkerDatabaseIntegrationTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 7, 10, 0);
    private static final AtomicLong MOBILE = new AtomicLong(9000000000L);

    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired BillingJobRepository jobs;
    @Autowired BillingJobStateService state;
    @Autowired BillingJobWorkerService worker;
    @MockitoBean EmailService emailService;
    @MockitoSpyBean EmailDeliveryService emailDelivery;
    private TransactionTemplate transaction;

    public static class TestDatabaseGuard implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override public void initialize(ConfigurableApplicationContext context) {
            String url = context.getEnvironment().getProperty("spring.datasource.url", "");
            if (!"jdbc:mysql://localhost:3306/smart_mess_worker_test".equals(url)) {
                throw new IllegalStateException("Integration tests require the exact localhost smart_mess_worker_test database.");
            }
        }
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean @Primary Clock workerIntegrationClock() {
            return Clock.fixed(Instant.parse("2026-10-07T04:30:00Z"), ZoneId.of("Asia/Kolkata"));
        }
    }

    @BeforeEach void setup() {
        transaction = new TransactionTemplate(transactionManager);
        reset(emailDelivery);
        clearInvocations(emailService);
    }

    private record Fixture(Long messId, Long customerId, Long recordId, Long jobId) {}

    private Fixture fixture(CustomerStatus status, int month, BigDecimal amount) {
        return transaction.execute(tx -> {
            Mess mess = new Mess();
            mess.setMessName("Worker integration " + UUID.randomUUID());
            mess.setRegistrationCode(UUID.randomUUID().toString());
            entityManager.persist(mess);
            Customer customer = new Customer();
            customer.setMess(mess);
            customer.setFullName("Worker Test Customer");
            customer.setMobileNumber(Long.toString(MOBILE.incrementAndGet()));
            customer.setEmail(UUID.randomUUID() + "@example.com");
            customer.setPassword("unused-test-password");
            customer.setJoiningDate(LocalDate.of(2026, 1, 1));
            customer.setStatus(status);
            entityManager.persist(customer);
            Menu menu = new Menu();
            menu.setMess(mess);
            menu.setMenuDate(LocalDate.of(2026, month, 10));
            menu.setMealSession(MealSession.LUNCH);
            menu.setSabjiOne("Test Sabji");
            entityManager.persist(menu);
            MealRecord record = new MealRecord();
            record.setMess(mess);
            record.setCustomer(customer);
            record.setMenu(menu);
            record.setMealOption(MealOption.FULL);
            record.setMealPrice(amount);
            record.setExtraRotiCount(0);
            record.setExtraRotiPrice(new BigDecimal("10.00"));
            record.setTotalAmount(amount);
            record.setCollectedAt(menu.getMenuDate().atTime(12, 0));
            entityManager.persist(record);
            BillingJob job = new BillingJob();
            job.setMess(mess);
            job.setBillingMonth(month);
            job.setBillingYear(2026);
            job.setNextAttemptAt(NOW);
            entityManager.persist(job);
            entityManager.flush();
            return new Fixture(mess.getMessId(), customer.getCustomerId(), record.getMealRecordId(), job.getBillingJobId());
        });
    }

    private long count(String table, Long messId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE mess_id = ?", Long.class, messId);
    }

    private void assertCommitted(Fixture fixture) {
        assertEquals(1, count("bills", fixture.messId()));
        assertEquals(1, count("notifications", fixture.messId()));
        assertEquals(1, count("email_deliveries", fixture.messId()));
        Long billId = jdbc.queryForObject("SELECT bill_id FROM meal_records WHERE meal_record_id = ?", Long.class, fixture.recordId());
        assertNotNull(billId);
        assertEquals(0, new BigDecimal("80.00").compareTo(jdbc.queryForObject(
            "SELECT total_amount FROM bills WHERE bill_id = ?", BigDecimal.class, billId)));
        assertEquals("COMPLETED", jdbc.queryForObject("SELECT status FROM billing_jobs WHERE billing_job_id = ?", String.class, fixture.jobId()));
        assertEquals("PENDING", jdbc.queryForObject("SELECT delivery_status FROM email_deliveries WHERE mess_id = ?", String.class, fixture.messId()));
        verifyNoInteractions(emailService);
    }

    @Test void billMealNotificationAndEmailQueueCommitTogether() {
        Fixture own = fixture(CustomerStatus.ACTIVE, 9, new BigDecimal("80.00"));
        Fixture foreign = fixture(CustomerStatus.ACTIVE, 9, new BigDecimal("80.00"));
        worker.execute(state.claim(own.jobId()).orElseThrow());
        assertCommitted(own);
        assertEquals(0, count("bills", foreign.messId()));
        assertEquals(0, count("notifications", foreign.messId()));
        assertEquals(0, count("email_deliveries", foreign.messId()));
        assertNull(jdbc.queryForObject("SELECT bill_id FROM meal_records WHERE meal_record_id = ?", Long.class, foreign.recordId()));
    }

    @Test void queueFailureRollsBackCustomerBillAndRetryCreatesOneBill() {
        Fixture fixture = fixture(CustomerStatus.ACTIVE, 9, new BigDecimal("80.00"));
        transaction.executeWithoutResult(tx -> {
            doThrow(new IllegalStateException("Injected queue failure"))
                    .when(emailDelivery)
                    .queueNotificationEmail(any(Notification.class));
        });
        worker.execute(state.claim(fixture.jobId()).orElseThrow());
        assertEquals(0, count("bills", fixture.messId()));
        assertEquals(0, count("notifications", fixture.messId()));
        assertEquals(0, count("email_deliveries", fixture.messId()));
        assertNull(jdbc.queryForObject("SELECT bill_id FROM meal_records WHERE meal_record_id = ?", Long.class, fixture.recordId()));
        assertEquals("FAILED", jdbc.queryForObject("SELECT status FROM billing_jobs WHERE billing_job_id = ?", String.class, fixture.jobId()));
        reset(emailDelivery);
        transaction.executeWithoutResult(tx -> {
            BillingJob job = jobs.findByIdForUpdate(fixture.jobId()).orElseThrow();
            job.setNextAttemptAt(NOW);
            jobs.saveAndFlush(job);
        });
        ClaimedJob retry = state.claim(fixture.jobId()).orElseThrow();
        worker.execute(retry);
        assertCommitted(fixture);
        worker.execute(retry);
        assertCommitted(fixture);
        assertTrue(state.claim(fixture.jobId()).isEmpty());
    }

    @Test void inactiveCustomerStillReceivesHistoricalBillAndEmailJob() {
        Fixture fixture = fixture(CustomerStatus.INACTIVE, 9, new BigDecimal("80.00"));
        worker.execute(state.claim(fixture.jobId()).orElseThrow());
        assertCommitted(fixture);
    }

    @Test void concurrentClaimsHaveOnlyOneWinner() throws Exception {
        Fixture fixture = fixture(CustomerStatus.ACTIVE, 9, new BigDecimal("80.00"));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Optional<ClaimedJob>> attempt = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Claim start timed out");
            return state.claim(fixture.jobId());
        };
        try {
            Future<Optional<ClaimedJob>> first = executor.submit(attempt);
            Future<Optional<ClaimedJob>> second = executor.submit(attempt);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            List<ClaimedJob> winners = new ArrayList<>();
            first.get(20, TimeUnit.SECONDS).ifPresent(winners::add);
            second.get(20, TimeUnit.SECONDS).ifPresent(winners::add);
            assertEquals(1, winners.size());
            worker.execute(winners.get(0));
            assertCommitted(fixture);
            assertEquals(1, jdbc.queryForObject("SELECT attempt_count FROM billing_jobs WHERE billing_job_id = ?", Integer.class, fixture.jobId()));
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    @Test void expiredClaimIsRecoveredAndOldWorkerCannotBill() {
        Fixture fixture = fixture(CustomerStatus.ACTIVE, 9, new BigDecimal("80.00"));
        ClaimedJob old = state.claim(fixture.jobId()).orElseThrow();
        transaction.executeWithoutResult(tx -> {
            BillingJob job = jobs.findByIdForUpdate(fixture.jobId()).orElseThrow();
            job.setLeaseExpiresAt(NOW);
            jobs.saveAndFlush(job);
        });
        ClaimedJob replacement = state.claim(fixture.jobId()).orElseThrow();
        assertNotEquals(old.leaseToken(), replacement.leaseToken());
        worker.execute(old);
        assertEquals(0, count("bills", fixture.messId()));
        worker.execute(replacement);
        assertCommitted(fixture);
    }
}
