package com.smartmess.backend.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.response.DishFeedbackAnalyticsResponse;
import com.smartmess.backend.dto.response.DishIntelligenceResponse;
import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceAnalysisResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceOverviewResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceSuggestionResponse;
import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.MenuIntelligenceAnalysisRun;
import com.smartmess.backend.entity.MenuIntelligenceSuggestion;
import com.smartmess.backend.enums.AnalysisRunStatus;
import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.repository.DishRepository;
import com.smartmess.backend.repository.MealFeedbackRepository;
import com.smartmess.backend.repository.MenuIntelligenceAnalysisRunRepository;
import com.smartmess.backend.repository.MenuIntelligenceSuggestionRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MenuIntelligenceService;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContextBuilder;

@Service
public class MenuIntelligenceServiceImpl
        implements MenuIntelligenceService {

    private final DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private final MealFeedbackRepository mealFeedbackRepository;
    private final DishRepository dishRepository;
    private final CustomerSecurity customerSecurity;
    private final MenuIntelligenceContextBuilder contextBuilder;
    private final MenuIntelligenceAnalysisRunRepository analysisRunRepository;
    private final MenuIntelligenceSuggestionRepository suggestionRepository;

    public MenuIntelligenceServiceImpl(
            DishFeedbackRatingRepository dishFeedbackRatingRepository,
            MealFeedbackRepository mealFeedbackRepository,
            DishRepository dishRepository,
            CustomerSecurity customerSecurity,
            MenuIntelligenceContextBuilder contextBuilder,
            MenuIntelligenceAnalysisRunRepository analysisRunRepository,
            MenuIntelligenceSuggestionRepository suggestionRepository) {

        this.dishFeedbackRatingRepository = dishFeedbackRatingRepository;
        this.mealFeedbackRepository = mealFeedbackRepository;
        this.dishRepository = dishRepository;
        this.customerSecurity = customerSecurity;
        this.contextBuilder = contextBuilder;
        this.analysisRunRepository = analysisRunRepository;
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishRatingAnalyticsResponse> getDishRatingAnalytics() {
        Long messId = customerSecurity.getCurrentMessId();

        return dishFeedbackRatingRepository.getDishRatingAnalytics(messId)
                .stream()
                .map(projection -> new DishRatingAnalyticsResponse(
                        projection.getDishId(),
                        projection.getDishName(),
                        projection.getTotalRatings(),
                        projection.getAverageRating(),
                        projection.getOneStarCount(),
                        projection.getTwoStarCount(),
                        projection.getThreeStarCount(),
                        projection.getFourStarCount(),
                        projection.getFiveStarCount()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishFeedbackAnalyticsResponse> getDishFeedbackAnalytics() {
        Long messId = customerSecurity.getCurrentMessId();

        return mealFeedbackRepository.getDishFeedbackAnalytics(messId)
                .stream()
                .map(projection -> new DishFeedbackAnalyticsResponse(
                        projection.getDishId(),
                        projection.getDishName(),
                        projection.getTotalComments()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishIntelligenceResponse> getDishIntelligence() {
        Long messId = customerSecurity.getCurrentMessId();

        Map<Long, DishRatingAnalyticsResponse> ratingsByDishId =
                new HashMap<>();

        getDishRatingAnalytics().forEach(rating ->
                ratingsByDishId.put(rating.dishId(), rating));

        Map<Long, DishFeedbackAnalyticsResponse> feedbackByDishId =
                new HashMap<>();

        getDishFeedbackAnalytics().forEach(feedback ->
                feedbackByDishId.put(feedback.dishId(), feedback));

        Map<Long, DishIntelligenceResponse.AiSuggestion> suggestionsByDishId =
                new HashMap<>();

        suggestionRepository.findLatestSuggestionsByMessId(messId)
                .forEach(suggestion -> {
                    if (suggestion.getDish() != null) {
                        Long dishId = suggestion.getDish().getDishId();

                        suggestionsByDishId.putIfAbsent(
                                dishId,
                                mapToAiSuggestion(suggestion));
                    }
                });

        return dishRepository.findByMess_MessIdOrderByNameAsc(messId)
                .stream()
                .map(dish -> mapToDishIntelligence(
                        dish,
                        ratingsByDishId.get(dish.getDishId()),
                        feedbackByDishId.get(dish.getDishId()),
                        suggestionsByDishId.get(dish.getDishId())))
                .toList();
    }

    private DishIntelligenceResponse mapToDishIntelligence(
            Dish dish,
            DishRatingAnalyticsResponse rating,
            DishFeedbackAnalyticsResponse feedback,
            DishIntelligenceResponse.AiSuggestion aiSuggestion) {

        return new DishIntelligenceResponse(
                dish.getDishId(),
                dish.getName(),
                dish.isActive(),
                rating == null ? 0L : rating.totalRatings(),
                rating == null ? null : rating.averageRating(),
                rating == null ? 0L : rating.oneStarCount(),
                rating == null ? 0L : rating.twoStarCount(),
                rating == null ? 0L : rating.threeStarCount(),
                rating == null ? 0L : rating.fourStarCount(),
                rating == null ? 0L : rating.fiveStarCount(),
                feedback == null ? 0L : feedback.totalComments(),
                aiSuggestion);
    }

    private DishIntelligenceResponse.AiSuggestion mapToAiSuggestion(
            MenuIntelligenceSuggestion suggestion) {

        return new DishIntelligenceResponse.AiSuggestion(
                suggestion.getObservation(),
                suggestion.getRecommendation(),
                suggestion.getPriority());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuIntelligenceContext buildIntelligenceContext() {
        Long messId = customerSecurity.getCurrentMessId();
        return contextBuilder.build(messId);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuIntelligenceOverviewResponse getIntelligenceOverview() {
        Long messId = customerSecurity.getCurrentMessId();

        MenuIntelligenceAnalysisRun latestSuccessfulRun =
                analysisRunRepository
                        .findFirstByMess_MessIdAndStatusOrderByAnalysisRunIdDesc(
                                messId,
                                AnalysisRunStatus.SUCCESS)
                        .orElse(null);

        if (latestSuccessfulRun == null) {
            return new MenuIntelligenceOverviewResponse(null, List.of());
        }

        MenuIntelligenceAnalysisResponse analysisResponse =
                new MenuIntelligenceAnalysisResponse(
                        latestSuccessfulRun.getPeriodStart(),
                        latestSuccessfulRun.getPeriodEnd(),
                        latestSuccessfulRun.getCompletedAt(),
                        latestSuccessfulRun.getSuggestionCount());

        List<MenuIntelligenceSuggestionResponse> suggestions =
                suggestionRepository.findLatestSuggestionsByMessId(messId)
                        .stream()
                        .map(this::mapToSuggestionResponse)
                        .toList();

        return new MenuIntelligenceOverviewResponse(
                analysisResponse,
                suggestions);
    }

    private MenuIntelligenceSuggestionResponse mapToSuggestionResponse(
            MenuIntelligenceSuggestion suggestion) {

        String dishName = suggestion.getDish() == null
                ? null
                : suggestion.getDish().getName();

        return new MenuIntelligenceSuggestionResponse(
                dishName,
                suggestion.getObservation(),
                suggestion.getRecommendation(),
                suggestion.getPriority());
    }
}
