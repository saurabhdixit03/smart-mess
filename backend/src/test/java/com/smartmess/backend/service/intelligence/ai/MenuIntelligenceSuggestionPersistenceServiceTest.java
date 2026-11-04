package com.smartmess.backend.service.intelligence.ai;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.MenuIntelligenceAnalysisRun;
import com.smartmess.backend.entity.MenuIntelligenceSuggestion;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.enums.AnalysisRunStatus;
import com.smartmess.backend.enums.DishCategory;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.repository.DishRepository;
import com.smartmess.backend.repository.MenuIntelligenceAnalysisRunRepository;
import com.smartmess.backend.repository.MenuIntelligenceSuggestionRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.intelligence.ai.MenuIntelligenceSuggestionPersistenceService;
import com.smartmess.backend.service.intelligence.ai.ValidatedDishSuggestion;
import com.smartmess.backend.service.intelligence.ai.ValidatedMenuFeedbackSuggestions;
import com.smartmess.backend.service.intelligence.ai.dto.OverallMealSuggestion;
import com.smartmess.backend.enums.SuggestionPriority;

@ExtendWith(MockitoExtension.class)
class MenuIntelligenceSuggestionPersistenceServiceTest {

    @Mock
    private MessRepository messRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private MenuIntelligenceAnalysisRunRepository analysisRunRepository;

    @Mock
    private MenuIntelligenceSuggestionRepository suggestionRepository;

    @InjectMocks
    private MenuIntelligenceSuggestionPersistenceService persistenceService;

    private Mess mess;
    private Dish dish;
    private MenuIntelligenceAnalysisRun run;

    private final Long messId = 10L;
    private final Long dishId = 20L;
    private final Long runId = 30L;

    private final LocalDate periodStart = LocalDate.of(2026, 10, 25);
    private final LocalDate periodEnd = LocalDate.of(2026, 10, 31);

    @BeforeEach
    void setUp() {
        mess = new Mess();
        mess.setMessId(messId);

        dish = new Dish();
        dish.setDishId(dishId);
        dish.setMess(mess);
        dish.setName("Paneer Masala");
        dish.setNormalizedName("paneer masala");
        dish.setCategory(DishCategory.SABJI);

        run = new MenuIntelligenceAnalysisRun();
        run.setAnalysisRunId(runId);
        run.setMess(mess);
        run.setPeriodStart(periodStart);
        run.setPeriodEnd(periodEnd);
        run.setStatus(AnalysisRunStatus.IN_PROGRESS);
        run.setStartedAt(LocalDateTime.now());
        run.setSuggestionCount(0);
    }

    @Test
    void shouldStartNewAnalysisRun() {
        when(analysisRunRepository.findByMessAndPeriodForUpdate(
                messId, periodStart, periodEnd))
                .thenReturn(Optional.empty());

        when(messRepository.findById(messId))
                .thenReturn(Optional.of(mess));

        when(analysisRunRepository.save(any(MenuIntelligenceAnalysisRun.class)))
                .thenAnswer(invocation -> {
                    MenuIntelligenceAnalysisRun saved = invocation.getArgument(0);
                    saved.setAnalysisRunId(runId);
                    return saved;
                });

        Long result = persistenceService.startAnalysisRun(
                messId, periodStart, periodEnd);

        assertEquals(runId, result);
        verify(analysisRunRepository).save(any(MenuIntelligenceAnalysisRun.class));
    }

    @Test
    void shouldRejectDuplicateSuccessfulRun() {
        run.setStatus(AnalysisRunStatus.SUCCESS);

        when(analysisRunRepository.findByMessAndPeriodForUpdate(
                messId, periodStart, periodEnd))
                .thenReturn(Optional.of(run));

        assertThrows(
                IllegalStateException.class,
                () -> persistenceService.startAnalysisRun(
                        messId, periodStart, periodEnd)
        );

        verify(messRepository, never()).findById(any());
    }

    @Test
    void shouldSaveDishSpecificSuggestion() {
        when(analysisRunRepository.findById(runId))
                .thenReturn(Optional.of(run));

        when(dishRepository.findByDishIdAndMess_MessId(dishId, messId))
                .thenReturn(Optional.of(dish));

        ValidatedDishSuggestion dishSuggestion =
                new ValidatedDishSuggestion(
                        dishId,
                        "Paneer Masala",
                        "Customers report inconsistent gravy quality.",
                        "Maintain consistent gravy thickness.",
                        SuggestionPriority.HIGH
                );

        ValidatedMenuFeedbackSuggestions suggestions =
                new ValidatedMenuFeedbackSuggestions(
                        List.of(dishSuggestion),
                        List.of()
                );

        when(suggestionRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        persistenceService.completeAnalysisRun(
                messId, runId, suggestions);

        verify(suggestionRepository).saveAll(argThat(saved -> {
            List<?> records = (List<?>) saved;

            if (records.size() != 1) {
                return false;
            }

            MenuIntelligenceSuggestion suggestion =
                    (MenuIntelligenceSuggestion) records.get(0);

            return suggestion.getMess() == mess
                    && suggestion.getDish() == dish
                    && suggestion.getAnalysisRun() == run
                    && suggestion.getPriority() == SuggestionPriority.HIGH;
        }));

        assertEquals(AnalysisRunStatus.SUCCESS, run.getStatus());
        assertEquals(1, run.getSuggestionCount());
        assertNotNull(run.getCompletedAt());
    }

    @Test
    void shouldSaveOverallSuggestionWithoutDish() {
        when(analysisRunRepository.findById(runId))
                .thenReturn(Optional.of(run));

        OverallMealSuggestion overallSuggestion =
                new OverallMealSuggestion(
                        "Customers frequently report excessive oil.",
                        "Review oil usage during meal preparation.",
                        SuggestionPriority.MEDIUM
                );

        ValidatedMenuFeedbackSuggestions suggestions =
                new ValidatedMenuFeedbackSuggestions(
                        List.of(),
                        List.of(overallSuggestion)
                );

        when(suggestionRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        persistenceService.completeAnalysisRun(
                messId, runId, suggestions);

        verify(suggestionRepository).saveAll(argThat(saved -> {
            List<?> records = (List<?>) saved;

            if (records.size() != 1) {
                return false;
            }

            MenuIntelligenceSuggestion suggestion =
                    (MenuIntelligenceSuggestion) records.get(0);

            return suggestion.getDish() == null
                    && suggestion.getAnalysisRun() == run
                    && suggestion.getMess() == mess;
        }));

        assertEquals(AnalysisRunStatus.SUCCESS, run.getStatus());
        assertEquals(1, run.getSuggestionCount());
    }

    @Test
    void shouldCompleteRunWhenNoSuggestionsExist() {
        when(analysisRunRepository.findById(runId))
                .thenReturn(Optional.of(run));

        ValidatedMenuFeedbackSuggestions suggestions =
                new ValidatedMenuFeedbackSuggestions(
                        List.of(),
                        List.of()
                );

        persistenceService.completeAnalysisRun(
                messId, runId, suggestions);

        verify(suggestionRepository).saveAll(List.of());

        assertEquals(AnalysisRunStatus.SUCCESS, run.getStatus());
        assertEquals(0, run.getSuggestionCount());
        assertNotNull(run.getCompletedAt());
    }

    @Test
    void shouldRejectDishBelongingToAnotherMess() {
        when(analysisRunRepository.findById(runId))
                .thenReturn(Optional.of(run));

        when(dishRepository.findByDishIdAndMess_MessId(dishId, messId))
                .thenReturn(Optional.empty());

        ValidatedDishSuggestion dishSuggestion =
                new ValidatedDishSuggestion(
                        dishId,
                        "Paneer Masala",
                        "Observation",
                        "Recommendation",
                        SuggestionPriority.HIGH
                );

        ValidatedMenuFeedbackSuggestions suggestions =
                new ValidatedMenuFeedbackSuggestions(
                        List.of(dishSuggestion),
                        List.of()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> persistenceService.completeAnalysisRun(
                        messId, runId, suggestions)
        );

        verify(suggestionRepository, never()).saveAll(any());
    }

    @Test
    void shouldMarkAnalysisRunFailed() {
        when(analysisRunRepository.findById(runId))
                .thenReturn(Optional.of(run));

        persistenceService.markAnalysisRunFailed(messId, runId);

        assertEquals(AnalysisRunStatus.FAILED, run.getStatus());
        assertNotNull(run.getCompletedAt());

        verify(analysisRunRepository).save(run);
    }

    @Test
    void shouldRejectCompletionOfNonInProgressRun() {
        run.setStatus(AnalysisRunStatus.SUCCESS);

        when(analysisRunRepository.findById(runId))
                .thenReturn(Optional.of(run));

        ValidatedMenuFeedbackSuggestions suggestions =
                new ValidatedMenuFeedbackSuggestions(
                        List.of(),
                        List.of()
                );

        assertThrows(
                IllegalStateException.class,
                () -> persistenceService.completeAnalysisRun(
                        messId, runId, suggestions)
        );

        verify(suggestionRepository, never()).saveAll(any());
    }

    @Test
    void shouldRejectRunBelongingToAnotherMess() {
        when(analysisRunRepository.findById(runId))
                .thenReturn(Optional.of(run));

        assertThrows(
                IllegalArgumentException.class,
                () -> persistenceService.markAnalysisRunFailed(
                        999L, runId)
        );

        verify(analysisRunRepository, never()).save(any());
    }

    @Test
    void shouldReuseFailedAnalysisRun() {
        run.setStatus(AnalysisRunStatus.FAILED);
        run.setCompletedAt(LocalDateTime.now());

        when(analysisRunRepository.findByMessAndPeriodForUpdate(
                messId, periodStart, periodEnd))
                .thenReturn(Optional.of(run));

        when(analysisRunRepository.save(any(MenuIntelligenceAnalysisRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long result = persistenceService.startAnalysisRun(
                messId, periodStart, periodEnd);

        assertEquals(runId, result);
        assertEquals(AnalysisRunStatus.IN_PROGRESS, run.getStatus());
        assertNull(run.getCompletedAt());
        assertEquals(0, run.getSuggestionCount());
        assertNotNull(run.getStartedAt());

        verify(suggestionRepository)
                .deleteAllByAnalysisRun_AnalysisRunId(runId);

        verify(analysisRunRepository)
                .save(run);

        verify(messRepository, never()).findById(any());
    }

    @Test
    void shouldRejectDuplicateInProgressRun() {
        run.setStatus(AnalysisRunStatus.IN_PROGRESS);

        when(analysisRunRepository.findByMessAndPeriodForUpdate(
                messId, periodStart, periodEnd))
                .thenReturn(Optional.of(run));

        assertThrows(
                IllegalStateException.class,
                () -> persistenceService.startAnalysisRun(
                        messId, periodStart, periodEnd)
        );

        verify(analysisRunRepository, never())
                .save(any(MenuIntelligenceAnalysisRun.class));

        verify(suggestionRepository, never())
                .deleteAllByAnalysisRun_AnalysisRunId(anyLong());

        verify(messRepository, never()).findById(any());
    }
}