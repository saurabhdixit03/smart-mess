package com.smartmess.backend.service.intelligence.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContextBuilder;

@ExtendWith(MockitoExtension.class)
class MenuIntelligenceAnalysisServiceTest {

    @Mock
    private MenuIntelligenceContextBuilder contextBuilder;

    @Mock
    private MenuFeedbackAiProcessingService aiProcessingService;

    @Mock
    private MenuIntelligenceSuggestionPersistenceService persistenceService;

    private MenuIntelligenceAnalysisService analysisService;

    private Long messId;
    private Long analysisRunId;

    private LocalDate currentStart;
    private LocalDate currentEnd;
    private LocalDate previousStart;
    private LocalDate previousEnd;

    private MenuIntelligenceContext context;
    private ValidatedMenuFeedbackSuggestions suggestions;

    @BeforeEach
    void setUp() {
        messId = 1L;
        analysisRunId = 100L;

        currentStart = LocalDate.of(2026, 10, 25);
        currentEnd = LocalDate.of(2026, 10, 31);
        previousStart = LocalDate.of(2026, 10, 18);
        previousEnd = LocalDate.of(2026, 10, 24);

        context = org.mockito.Mockito.mock(MenuIntelligenceContext.class);
        suggestions = org.mockito.Mockito.mock(ValidatedMenuFeedbackSuggestions.class);

        analysisService = new MenuIntelligenceAnalysisService(
                contextBuilder,
                aiProcessingService,
                persistenceService
        );
    }

    @Test
    void shouldCompleteAnalysisSuccessfully() {
        when(persistenceService.startAnalysisRun(
                messId, currentStart, currentEnd
        )).thenReturn(analysisRunId);

        when(contextBuilder.buildWithComparison(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        )).thenReturn(context);

        when(aiProcessingService.process(context)).thenReturn(suggestions);

        boolean result = analysisService.analyzeMess(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );

        assertTrue(result);

        verify(persistenceService).startAnalysisRun(
                messId, currentStart, currentEnd
        );

        verify(contextBuilder).buildWithComparison(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );

        verify(aiProcessingService).process(context);

        verify(persistenceService).completeAnalysisRun(
                messId,
                analysisRunId,
                suggestions
        );
    }

    @Test
    void shouldMarkAnalysisRunFailedWhenAiProcessingFails() {
        when(persistenceService.startAnalysisRun(
                messId, currentStart, currentEnd
        )).thenReturn(analysisRunId);

        when(contextBuilder.buildWithComparison(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        )).thenReturn(context);

        when(aiProcessingService.process(context))
                .thenThrow(new RuntimeException("AI processing failed"));

        boolean result = analysisService.analyzeMess(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );

        org.junit.jupiter.api.Assertions.assertFalse(result);

        verify(persistenceService).markAnalysisRunFailed(
                messId, analysisRunId
        );

        org.mockito.Mockito.verify(
                persistenceService,
                org.mockito.Mockito.never()
        ).completeAnalysisRun(
                messId, analysisRunId, suggestions
        );
    }

    @Test
    void shouldMarkAnalysisRunFailedWhenContextBuildingFails() {
        when(persistenceService.startAnalysisRun(
                messId, currentStart, currentEnd
        )).thenReturn(analysisRunId);

        when(contextBuilder.buildWithComparison(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        )).thenThrow(new RuntimeException("Context building failed"));

        boolean result = analysisService.analyzeMess(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );

        org.junit.jupiter.api.Assertions.assertFalse(result);

        verify(persistenceService).markAnalysisRunFailed(
                messId, analysisRunId
        );

        org.mockito.Mockito.verifyNoInteractions(aiProcessingService);
    }

    @Test
    void shouldMarkAnalysisRunFailedWhenSuggestionPersistenceFails() {
        when(persistenceService.startAnalysisRun(
                messId, currentStart, currentEnd
        )).thenReturn(analysisRunId);

        when(contextBuilder.buildWithComparison(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        )).thenReturn(context);

        when(aiProcessingService.process(context)).thenReturn(suggestions);

        org.mockito.Mockito.doThrow(
                new RuntimeException("Suggestion persistence failed")
        ).when(persistenceService).completeAnalysisRun(
                messId, analysisRunId, suggestions
        );

        boolean result = analysisService.analyzeMess(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );

        org.junit.jupiter.api.Assertions.assertFalse(result);

        verify(persistenceService).markAnalysisRunFailed(
                messId, analysisRunId
        );
    }

    @Test
    void shouldNotMarkRunFailedWhenAnalysisRunCreationFails() {
        when(persistenceService.startAnalysisRun(
                messId, currentStart, currentEnd
        )).thenThrow(new RuntimeException("Could not create analysis run"));

        boolean result = analysisService.analyzeMess(
                messId,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );

        org.junit.jupiter.api.Assertions.assertFalse(result);

        org.mockito.Mockito.verify(
                persistenceService,
                org.mockito.Mockito.never()
        ).markAnalysisRunFailed(
                messId, analysisRunId
        );

        org.mockito.Mockito.verifyNoInteractions(contextBuilder, aiProcessingService);
    }
}