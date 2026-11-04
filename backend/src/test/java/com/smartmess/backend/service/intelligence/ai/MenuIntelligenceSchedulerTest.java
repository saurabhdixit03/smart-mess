
package com.smartmess.backend.service.intelligence.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.scheduler.MenuIntelligenceScheduler;

@ExtendWith(MockitoExtension.class)
class MenuIntelligenceSchedulerTest {

    @Mock
    private MessRepository messRepository;

    @Mock
    private MenuIntelligenceAnalysisService analysisService;

    private MenuIntelligenceScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new MenuIntelligenceScheduler(
                messRepository,
                analysisService
        );
    }

    @Test
    void shouldNotExecuteBeforeApplicationReady() {
        scheduler.analyzeAllMesses();

        verifyNoInteractions(messRepository, analysisService);
    }

    @Test
    void shouldProcessAllMessesWithCorrectWeeklyPeriods() {
        scheduler.onApplicationReady();

        Mess firstMess = new Mess();
        firstMess.setMessId(1L);

        Mess secondMess = new Mess();
        secondMess.setMessId(2L);

        when(messRepository.findAll())
                .thenReturn(List.of(firstMess, secondMess));

        scheduler.analyzeAllMesses();

        LocalDate currentPeriodEnd =
                LocalDate.now(ZoneId.of("Asia/Kolkata"))
                        .with(TemporalAdjusters.previousOrSame(
                                DayOfWeek.SATURDAY));

        LocalDate currentPeriodStart =
                currentPeriodEnd.minusDays(6);

        LocalDate previousPeriodEnd =
                currentPeriodStart.minusDays(1);

        LocalDate previousPeriodStart =
                previousPeriodEnd.minusDays(6);

        verify(analysisService).analyzeMess(
                1L,
                currentPeriodStart,
                currentPeriodEnd,
                previousPeriodStart,
                previousPeriodEnd
        );

        verify(analysisService).analyzeMess(
                2L,
                currentPeriodStart,
                currentPeriodEnd,
                previousPeriodStart,
                previousPeriodEnd
        );

        verify(messRepository).findAll();
    }

    @Test
    void shouldContinueProcessingWhenOneMessFails() {
        scheduler.onApplicationReady();

        Mess firstMess = new Mess();
        firstMess.setMessId(1L);

        Mess secondMess = new Mess();
        secondMess.setMessId(2L);

        when(messRepository.findAll())
                .thenReturn(List.of(firstMess, secondMess));

        when(analysisService.analyzeMess(
                eq(1L), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Simulated failure"));

        when(analysisService.analyzeMess(
                eq(2L), any(), any(), any(), any()))
                .thenReturn(true);

        scheduler.analyzeAllMesses();

        verify(analysisService).analyzeMess(
                eq(1L), any(), any(), any(), any());

        verify(analysisService).analyzeMess(
                eq(2L), any(), any(), any(), any());
    }

    @Test
    void shouldContinueWhenAnalysisReturnsFalse() {
        scheduler.onApplicationReady();

        Mess firstMess = new Mess();
        firstMess.setMessId(1L);

        Mess secondMess = new Mess();
        secondMess.setMessId(2L);

        when(messRepository.findAll())
                .thenReturn(List.of(firstMess, secondMess));

        when(analysisService.analyzeMess(
                eq(1L), any(), any(), any(), any()))
                .thenReturn(false);

        when(analysisService.analyzeMess(
                eq(2L), any(), any(), any(), any()))
                .thenReturn(true);

        scheduler.analyzeAllMesses();

        verify(analysisService, times(1)).analyzeMess(
                eq(1L), any(), any(), any(), any());

        verify(analysisService, times(1)).analyzeMess(
                eq(2L), any(), any(), any(), any());
    }

    @Test
    void shouldReleaseExecutionGuardAfterFailure() {
        scheduler.onApplicationReady();

        when(messRepository.findAll())
                .thenThrow(new RuntimeException("Database unavailable"));

        scheduler.analyzeAllMesses();

        // Use doReturn to avoid invoking the previous throwing stub.
        doReturn(List.of())
                .when(messRepository)
                .findAll();

        scheduler.analyzeAllMesses();

        verify(messRepository, times(2)).findAll();
    }
}
