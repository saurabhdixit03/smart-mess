package com.smartmess.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import com.smartmess.backend.entity.*;
import com.smartmess.backend.enums.*;
import com.smartmess.backend.repository.EmailDeliveryRepository;
import com.smartmess.backend.service.EmailService;

class EmailDeliveryServiceImplTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-07T04:30:00Z"), ZoneId.of("Asia/Kolkata"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private EmailDeliveryRepository repository;
    private EmailService email;
    private EntityManager entityManager;
    private EmailDeliveryServiceImpl service;
    private EmailDelivery delivery;
    private Notification notification;

    @BeforeEach void setup() {
        repository = mock(EmailDeliveryRepository.class);
        email = mock(EmailService.class);
        entityManager = mock(EntityManager.class);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any(TransactionDefinition.class)))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        service = new EmailDeliveryServiceImpl(repository, email, entityManager, clock, transactions);
        Mess mess = mock(Mess.class);
        when(mess.getMessId()).thenReturn(11L);
        when(mess.getMessName()).thenReturn("Test Mess");
        Customer customer = mock(Customer.class);
        when(customer.getCustomerId()).thenReturn(22L);
        when(customer.getMess()).thenReturn(mess);
        when(customer.getEmail()).thenReturn("customer@example.com");
        when(customer.getFullName()).thenReturn("Test Customer");
        notification = mock(Notification.class);
        when(notification.getNotificationId()).thenReturn(33L);
        when(notification.getMess()).thenReturn(mess);
        when(notification.getCustomer()).thenReturn(customer);
        when(notification.getNotificationType()).thenReturn(NotificationType.MEAL_PRICING);
        when(notification.getTitle()).thenReturn("Meal Price Update");
        when(notification.getMessage()).thenReturn("New prices apply tomorrow.");
        when(entityManager.find(Notification.class, 33L)).thenReturn(notification);
        delivery = new EmailDelivery();
        delivery.setEmailDeliveryId(44L);
        delivery.setMess(mess);
        delivery.setCustomer(customer);
        delivery.setNotification(notification);
        delivery.setRecipientEmail("snapshot@example.com");
        delivery.setCustomerName("Snapshot Customer");
        delivery.setMessName("Snapshot Mess");
        delivery.setNotificationType(NotificationType.MEAL_PRICING);
        delivery.setTitle("Snapshot Title");
        delivery.setMessage("Snapshot Message");
        delivery.setNextAttemptAt(now);
        when(repository.findDueDeliveryIds(eq(now), any())).thenReturn(List.of(44L));
        when(repository.findByIdForUpdate(44L)).thenReturn(Optional.of(delivery));
    }

    private void failProvider() {
        doThrow(new IllegalStateException("Sensitive provider response"))
                .when(email).sendCustomerNotificationEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test void queueSnapshotsContentWithoutSendingEmail() {
        service.queueNotificationEmail(notification);
        ArgumentCaptor<EmailDelivery> captor = ArgumentCaptor.forClass(EmailDelivery.class);
        verify(repository).save(captor.capture());
        EmailDelivery queued = captor.getValue();
        assertEquals("customer@example.com", queued.getRecipientEmail());
        assertEquals("Test Customer", queued.getCustomerName());
        assertEquals("Test Mess", queued.getMessName());
        assertEquals("Meal Price Update", queued.getTitle());
        assertEquals("New prices apply tomorrow.", queued.getMessage());
        assertEquals(EmailDeliveryStatus.PENDING, queued.getDeliveryStatus());
        assertEquals(now, queued.getNextAttemptAt());
        verifyNoInteractions(email);
    }

    @Test void duplicateNotificationDoesNotQueueAgain() {
        when(repository.existsByNotification_NotificationId(33L)).thenReturn(true);
        service.queueNotificationEmail(notification);
        verify(repository, never()).save(any());
    }

    @Test void unsupportedNotificationDoesNotQueueEmail() {
        when(notification.getNotificationType()).thenReturn(NotificationType.MENU_PUBLISHED);
        service.queueNotificationEmail(notification);
        verify(repository, never()).save(any());
    }

    @Test void queueRejectsCrossTenantNotification() {
        Mess foreign = mock(Mess.class);
        when(foreign.getMessId()).thenReturn(99L);
        when(notification.getMess()).thenReturn(foreign);
        assertThrows(IllegalArgumentException.class, () -> service.queueNotificationEmail(notification));
        verify(repository, never()).save(any());
    }

    @Test void successfulSendUsesSnapshotAndClearsClaim() {
        service.processDueEmails();
        verify(email).sendCustomerNotificationEmail("snapshot@example.com", "Snapshot Customer", "Snapshot Mess",
                NotificationType.MEAL_PRICING, "Snapshot Title", "Snapshot Message");
        assertEquals(EmailDeliveryStatus.SENT, delivery.getDeliveryStatus());
        assertEquals(now, delivery.getSentAt());
        assertEquals(1, delivery.getAttemptCount());
        assertNull(delivery.getLeaseToken());
        assertNull(delivery.getLeaseExpiresAt());
        assertNull(delivery.getNextAttemptAt());
        assertNull(delivery.getLastError());
        service.processDueEmails();
        verify(email, times(1)).sendCustomerNotificationEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test void providerFailureSchedulesRetryWithoutStoringProviderDetails() {
        failProvider();
        service.processDueEmails();
        assertEquals(EmailDeliveryStatus.FAILED, delivery.getDeliveryStatus());
        assertEquals(now.plusMinutes(1), delivery.getNextAttemptAt());
        assertNull(delivery.getSentAt());
        assertNull(delivery.getLeaseToken());
        assertFalse(delivery.getLastError().contains("Sensitive"));
        service.processDueEmails();
        verify(email, times(1)).sendCustomerNotificationEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test void repeatedFailureUsesCappedBackoff() {
        failProvider();
        delivery.setAttemptCount(6);
        service.processDueEmails();
        assertEquals(7, delivery.getAttemptCount());
        assertEquals(now.plusMinutes(60), delivery.getNextAttemptAt());
    }

    @Test void activeLeasePreventsSending() {
        delivery.setDeliveryStatus(EmailDeliveryStatus.SENDING);
        delivery.setLeaseExpiresAt(now.plusSeconds(1));
        service.processDueEmails();
        verifyNoInteractions(email);
    }

    @Test void interruptedDeliveryWithExpiredLeaseIsRecovered() {
        delivery.setDeliveryStatus(EmailDeliveryStatus.SENDING);
        delivery.setLeaseToken("old");
        delivery.setLeaseExpiresAt(now);
        delivery.setAttemptCount(1);
        service.processDueEmails();
        assertEquals(EmailDeliveryStatus.SENT, delivery.getDeliveryStatus());
        assertEquals(2, delivery.getAttemptCount());
    }

    @Test void crossTenantDeliveryNeverReachesProvider() {
        Mess foreign = mock(Mess.class);
        when(foreign.getMessId()).thenReturn(99L);
        delivery.setMess(foreign);
        service.processDueEmails();
        verifyNoInteractions(email);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void reclaimedTokenPreventsOldWorkerMarkingSuccess() {
        doAnswer(invocation -> {
            delivery.setLeaseToken("replacement-token");
            return null;
        }).when(email).sendCustomerNotificationEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString());
        service.processDueEmails();
        assertEquals(EmailDeliveryStatus.SENDING, delivery.getDeliveryStatus());
        assertEquals("replacement-token", delivery.getLeaseToken());
        assertNull(delivery.getSentAt());
    }

    @Test void reclaimedTokenPreventsOldWorkerRecordingFailure() {
        doAnswer(invocation -> {
            delivery.setLeaseToken("replacement-token");
            throw new IllegalStateException("Failure");
        }).when(email).sendCustomerNotificationEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString());
        service.processDueEmails();
        assertEquals(EmailDeliveryStatus.SENDING, delivery.getDeliveryStatus());
        assertEquals("replacement-token", delivery.getLeaseToken());
        assertNull(delivery.getLastError());
    }
}
