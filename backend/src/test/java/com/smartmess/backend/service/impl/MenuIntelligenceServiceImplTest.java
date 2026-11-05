package com.smartmess.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartmess.backend.dto.response.DishIntelligenceResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceOverviewResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceSuggestionResponse;
import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.MenuIntelligenceAnalysisRun;
import com.smartmess.backend.entity.MenuIntelligenceSuggestion;
import com.smartmess.backend.enums.AnalysisRunStatus;
import com.smartmess.backend.enums.SuggestionPriority;
import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.repository.DishRepository;
import com.smartmess.backend.repository.MealFeedbackRepository;
import com.smartmess.backend.repository.MenuIntelligenceAnalysisRunRepository;
import com.smartmess.backend.repository.MenuIntelligenceSuggestionRepository;
import com.smartmess.backend.repository.projection.DishFeedbackAnalyticsProjection;
import com.smartmess.backend.repository.projection.DishRatingAnalyticsProjection;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContextBuilder;

class MenuIntelligenceServiceImplTest {

    private DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private MealFeedbackRepository mealFeedbackRepository;
    private DishRepository dishRepository;
    private CustomerSecurity customerSecurity;
    private MenuIntelligenceContextBuilder contextBuilder;
    private MenuIntelligenceAnalysisRunRepository analysisRunRepository;
    private MenuIntelligenceSuggestionRepository suggestionRepository;

    private MenuIntelligenceServiceImpl service;

    private final Long messId = 11L;

    @BeforeEach
    void setUp() {
        dishFeedbackRatingRepository = mock(DishFeedbackRatingRepository.class);
        mealFeedbackRepository = mock(MealFeedbackRepository.class);
        dishRepository = mock(DishRepository.class);
        customerSecurity = mock(CustomerSecurity.class);
        contextBuilder = mock(MenuIntelligenceContextBuilder.class);
        analysisRunRepository = mock(MenuIntelligenceAnalysisRunRepository.class);
        suggestionRepository = mock(MenuIntelligenceSuggestionRepository.class);

        service = new MenuIntelligenceServiceImpl(
                dishFeedbackRatingRepository,
                mealFeedbackRepository,
                dishRepository,
                customerSecurity,
                contextBuilder,
                analysisRunRepository,
                suggestionRepository
        );

        when(customerSecurity.getCurrentMessId()).thenReturn(messId);
    }

    @Test
    void shouldReturnLatestAnalysisAndSuggestions() {
        LocalDate periodStart = LocalDate.of(2026, 10, 25);
        LocalDate periodEnd = LocalDate.of(2026, 10, 31);
        LocalDateTime completedAt = LocalDateTime.of(2026, 10, 31, 23, 5);

        MenuIntelligenceAnalysisRun analysisRun = new MenuIntelligenceAnalysisRun();
        analysisRun.setPeriodStart(periodStart);
        analysisRun.setPeriodEnd(periodEnd);
        analysisRun.setCompletedAt(completedAt);
        analysisRun.setSuggestionCount(2);

        Dish dish = new Dish();
        dish.setName("Paneer Masala");

        MenuIntelligenceSuggestion dishSuggestion =
                new MenuIntelligenceSuggestion();
        dishSuggestion.setDish(dish);
        dishSuggestion.setObservation("Customers frequently report excessive oil.");
        dishSuggestion.setRecommendation("Consider reducing the oil quantity.");
        dishSuggestion.setPriority(SuggestionPriority.HIGH);

        MenuIntelligenceSuggestion overallSuggestion =
                new MenuIntelligenceSuggestion();
        overallSuggestion.setDish(null);
        overallSuggestion.setObservation("Overall meal satisfaction is declining.");
        overallSuggestion.setRecommendation("Review meal quality and consistency.");
        overallSuggestion.setPriority(SuggestionPriority.MEDIUM);

        when(analysisRunRepository
                .findFirstByMess_MessIdAndStatusOrderByAnalysisRunIdDesc(
                        messId, AnalysisRunStatus.SUCCESS))
                .thenReturn(Optional.of(analysisRun));

        when(suggestionRepository.findLatestSuggestionsByMessId(messId))
                .thenReturn(List.of(dishSuggestion, overallSuggestion));

        MenuIntelligenceOverviewResponse response =
                service.getIntelligenceOverview();

        assertNotNull(response);
        assertNotNull(response.analysis());

        assertEquals(periodStart, response.analysis().periodStart());
        assertEquals(periodEnd, response.analysis().periodEnd());
        assertEquals(completedAt, response.analysis().completedAt());
        assertEquals(2, response.analysis().suggestionCount());

        assertEquals(2, response.suggestions().size());

        MenuIntelligenceSuggestionResponse first =
                response.suggestions().get(0);

        assertEquals("Paneer Masala", first.dishName());
        assertEquals(
                "Customers frequently report excessive oil.",
                first.observation()
        );
        assertEquals(
                "Consider reducing the oil quantity.",
                first.recommendation()
        );
        assertEquals(SuggestionPriority.HIGH, first.priority());

        MenuIntelligenceSuggestionResponse second =
                response.suggestions().get(1);

        assertNull(second.dishName());
        assertEquals(
                "Overall meal satisfaction is declining.",
                second.observation()
        );
        assertEquals(SuggestionPriority.MEDIUM, second.priority());

        verify(customerSecurity).getCurrentMessId();

        verify(analysisRunRepository)
                .findFirstByMess_MessIdAndStatusOrderByAnalysisRunIdDesc(
                        messId, AnalysisRunStatus.SUCCESS
                );

        verify(suggestionRepository).findLatestSuggestionsByMessId(messId);
    }

    @Test
    void shouldReturnEmptyOverviewWhenNoSuccessfulAnalysisExists() {
        when(analysisRunRepository
                .findFirstByMess_MessIdAndStatusOrderByAnalysisRunIdDesc(
                        messId, AnalysisRunStatus.SUCCESS))
                .thenReturn(Optional.empty());

        MenuIntelligenceOverviewResponse response =
                service.getIntelligenceOverview();

        assertNotNull(response);
        assertNull(response.analysis());
        assertNotNull(response.suggestions());
        assertTrue(response.suggestions().isEmpty());

        verify(analysisRunRepository)
                .findFirstByMess_MessIdAndStatusOrderByAnalysisRunIdDesc(
                        messId, AnalysisRunStatus.SUCCESS
                );

        verify(suggestionRepository, never())
                .findLatestSuggestionsByMessId(anyLong());
    }

    @Test
    void shouldReturnAnalysisMetadataWhenNoSuggestionsExist() {
        MenuIntelligenceAnalysisRun analysisRun =
                new MenuIntelligenceAnalysisRun();

        analysisRun.setPeriodStart(LocalDate.of(2026, 10, 25));
        analysisRun.setPeriodEnd(LocalDate.of(2026, 10, 31));
        analysisRun.setCompletedAt(
                LocalDateTime.of(2026, 10, 31, 23, 5)
        );
        analysisRun.setSuggestionCount(0);

        when(analysisRunRepository
                .findFirstByMess_MessIdAndStatusOrderByAnalysisRunIdDesc(
                        messId, AnalysisRunStatus.SUCCESS))
                .thenReturn(Optional.of(analysisRun));

        when(suggestionRepository.findLatestSuggestionsByMessId(messId))
                .thenReturn(List.of());

        MenuIntelligenceOverviewResponse response =
                service.getIntelligenceOverview();

        assertNotNull(response);
        assertNotNull(response.analysis());
        assertEquals(0, response.analysis().suggestionCount());
        assertTrue(response.suggestions().isEmpty());

        verify(suggestionRepository).findLatestSuggestionsByMessId(messId);
    }

    @Test
    void shouldCombineDishRatingsCommentsAndAiSuggestionByDishId() {
        Dish paneer = new Dish();
        paneer.setDishId(101L);
        paneer.setName("Paneer Masala");
        paneer.setActive(true);

        Dish inactiveDish = new Dish();
        inactiveDish.setDishId(102L);
        inactiveDish.setName("Veg Kolhapuri");
        inactiveDish.setActive(false);

        DishRatingAnalyticsProjection rating =
                mock(DishRatingAnalyticsProjection.class);

        when(rating.getDishId()).thenReturn(101L);
        when(rating.getDishName()).thenReturn("Paneer Masala");
        when(rating.getTotalRatings()).thenReturn(10L);
        when(rating.getAverageRating()).thenReturn(4.2);
        when(rating.getOneStarCount()).thenReturn(0L);
        when(rating.getTwoStarCount()).thenReturn(1L);
        when(rating.getThreeStarCount()).thenReturn(1L);
        when(rating.getFourStarCount()).thenReturn(4L);
        when(rating.getFiveStarCount()).thenReturn(4L);

        DishFeedbackAnalyticsProjection feedback =
                mock(DishFeedbackAnalyticsProjection.class);

        when(feedback.getDishId()).thenReturn(101L);
        when(feedback.getDishName()).thenReturn("Paneer Masala");
        when(feedback.getTotalComments()).thenReturn(3L);

        MenuIntelligenceSuggestion aiSuggestion =
                new MenuIntelligenceSuggestion();

        aiSuggestion.setDish(paneer);
        aiSuggestion.setObservation("Customers report excessive oil.");
        aiSuggestion.setRecommendation("Reduce the oil quantity.");
        aiSuggestion.setPriority(SuggestionPriority.HIGH);

        when(dishRepository.findByMess_MessIdOrderByNameAsc(messId))
                .thenReturn(List.of(paneer, inactiveDish));

        when(dishFeedbackRatingRepository.getDishRatingAnalytics(messId))
                .thenReturn(List.of(rating));

        when(mealFeedbackRepository.getDishFeedbackAnalytics(messId))
                .thenReturn(List.of(feedback));

        when(suggestionRepository.findLatestSuggestionsByMessId(messId))
                .thenReturn(List.of(aiSuggestion));

        List<DishIntelligenceResponse> response =
                service.getDishIntelligence();

        assertEquals(2, response.size());

        DishIntelligenceResponse paneerResponse = response.get(0);

        assertEquals(101L, paneerResponse.dishId());
        assertEquals("Paneer Masala", paneerResponse.dishName());
        assertTrue(paneerResponse.active());
        assertEquals(10L, paneerResponse.totalRatings());
        assertEquals(4.2, paneerResponse.averageRating());
        assertEquals(0L, paneerResponse.oneStarCount());
        assertEquals(1L, paneerResponse.twoStarCount());
        assertEquals(1L, paneerResponse.threeStarCount());
        assertEquals(4L, paneerResponse.fourStarCount());
        assertEquals(4L, paneerResponse.fiveStarCount());
        assertEquals(3L, paneerResponse.totalComments());

        assertNotNull(paneerResponse.aiSuggestion());
        assertEquals(
                "Customers report excessive oil.",
                paneerResponse.aiSuggestion().observation()
        );
        assertEquals(
                "Reduce the oil quantity.",
                paneerResponse.aiSuggestion().recommendation()
        );
        assertEquals(
                SuggestionPriority.HIGH,
                paneerResponse.aiSuggestion().priority()
        );

        DishIntelligenceResponse inactiveResponse = response.get(1);

        assertEquals(102L, inactiveResponse.dishId());
        assertEquals("Veg Kolhapuri", inactiveResponse.dishName());
        assertFalse(inactiveResponse.active());
        assertEquals(0L, inactiveResponse.totalRatings());
        assertNull(inactiveResponse.averageRating());
        assertEquals(0L, inactiveResponse.oneStarCount());
        assertEquals(0L, inactiveResponse.totalComments());
        assertNull(inactiveResponse.aiSuggestion());

        verify(customerSecurity, times(3)).getCurrentMessId();
        verify(dishRepository).findByMess_MessIdOrderByNameAsc(messId);
        verify(dishFeedbackRatingRepository).getDishRatingAnalytics(messId);
        verify(mealFeedbackRepository).getDishFeedbackAnalytics(messId);
        verify(suggestionRepository).findLatestSuggestionsByMessId(messId);
    }

    @Test
    void shouldReturnEmptyDishIntelligenceWhenMessHasNoDishes() {
        when(dishRepository.findByMess_MessIdOrderByNameAsc(messId))
                .thenReturn(List.of());

        when(dishFeedbackRatingRepository.getDishRatingAnalytics(messId))
                .thenReturn(List.of());

        when(mealFeedbackRepository.getDishFeedbackAnalytics(messId))
                .thenReturn(List.of());

        when(suggestionRepository.findLatestSuggestionsByMessId(messId))
                .thenReturn(List.of());

        List<DishIntelligenceResponse> response =
                service.getDishIntelligence();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(dishRepository).findByMess_MessIdOrderByNameAsc(messId);
    }
}
