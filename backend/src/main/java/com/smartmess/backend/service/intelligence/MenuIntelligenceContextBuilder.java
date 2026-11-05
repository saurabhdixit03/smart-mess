package com.smartmess.backend.service.intelligence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.repository.MealFeedbackRepository;
import com.smartmess.backend.repository.projection.DishFeedbackObservationProjection;
import com.smartmess.backend.repository.projection.OverallMealFeedbackProjection;

@Component
public class MenuIntelligenceContextBuilder {

    private final DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private final MealFeedbackRepository mealFeedbackRepository;
    private final DishIntelligenceCalculator dishIntelligenceCalculator;
    private final FeedbackCommentProcessor feedbackCommentProcessor;

    public MenuIntelligenceContextBuilder(
            DishFeedbackRatingRepository dishFeedbackRatingRepository,
            MealFeedbackRepository mealFeedbackRepository,
            DishIntelligenceCalculator dishIntelligenceCalculator,
            FeedbackCommentProcessor feedbackCommentProcessor) {

        this.dishFeedbackRatingRepository = dishFeedbackRatingRepository;
        this.mealFeedbackRepository = mealFeedbackRepository;
        this.dishIntelligenceCalculator = dishIntelligenceCalculator;
        this.feedbackCommentProcessor = feedbackCommentProcessor;
    }

    public MenuIntelligenceContext build(Long messId) {

        List<MenuIntelligenceObservation> observations = getObservations(messId);

        List<DishIntelligenceSummary> dishSummaries =
                dishIntelligenceCalculator.calculate(observations);

        return buildContext(messId, observations, dishSummaries, null, null);
    }

    public MenuIntelligenceContext buildWithComparison(
            Long messId,
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd,
            LocalDate previousPeriodStart,
            LocalDate previousPeriodEnd) {

        List<MenuIntelligenceObservation> observations = getObservations(messId);

        List<DishIntelligenceSummary> dishSummaries =
                dishIntelligenceCalculator.calculateWithComparison(
                        observations,
                        currentPeriodStart,
                        currentPeriodEnd,
                        previousPeriodStart,
                        previousPeriodEnd
                );

        return buildContext(
                messId,
                observations,
                dishSummaries,
                currentPeriodStart,
                currentPeriodEnd
        );
    }

    private List<MenuIntelligenceObservation> getObservations(Long messId) {

        List<DishFeedbackObservationProjection> projections =
                dishFeedbackRatingRepository.getDishFeedbackObservations(messId);

        return projections.stream()
                .map(projection -> new MenuIntelligenceObservation(
                        projection.getMealFeedbackId(),
                        projection.getMenuId(),
                        projection.getMenuDate(),
                        projection.getMealSession(),
                        projection.getDishId(),
                        projection.getDishName(),
                        projection.getRating(),
                        projection.getComment()
                ))
                .toList();
    }

    private MenuIntelligenceContext buildContext(
            Long messId,
            List<MenuIntelligenceObservation> observations,
            List<DishIntelligenceSummary> dishSummaries,
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd) {

        List<String> rawOverallComments =
                mealFeedbackRepository.getOverallMealComments(messId);

        List<ProcessedFeedbackComment> overallMealComments =
                feedbackCommentProcessor.process(rawOverallComments);

        List<OverallMealFeedbackProjection> overallMealFeedbackObservations =
                mealFeedbackRepository.getOverallMealFeedbackObservations(messId);

        return new MenuIntelligenceContext(
                messId,
                observations,
                dishSummaries,
                overallMealComments,
                overallMealFeedbackObservations,
                currentPeriodStart,
                currentPeriodEnd
        );
    }
}