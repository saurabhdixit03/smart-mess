package com.smartmess.backend.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.smartmess.backend.config.MenuIntelligenceProperties;
import com.smartmess.backend.service.intelligence.DishFeedbackAiContext;
import com.smartmess.backend.service.intelligence.DishIntelligenceSummary;
import com.smartmess.backend.service.intelligence.FeedbackCommentProcessor;
import com.smartmess.backend.service.intelligence.FeedbackCommentSanitizer;
import com.smartmess.backend.service.intelligence.MenuFeedbackAiContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceObservation;
import com.smartmess.backend.service.intelligence.ProcessedFeedbackComment;
import com.smartmess.backend.repository.projection.OverallMealFeedbackProjection;

@Component
public class MenuFeedbackAiContextMapper {

    private final FeedbackCommentSanitizer feedbackCommentSanitizer;
    private final FeedbackCommentProcessor feedbackCommentProcessor;
    private final MenuIntelligenceProperties properties;

    public MenuFeedbackAiContextMapper(
            FeedbackCommentSanitizer feedbackCommentSanitizer,
            FeedbackCommentProcessor feedbackCommentProcessor,
            MenuIntelligenceProperties properties) {

        this.feedbackCommentSanitizer = feedbackCommentSanitizer;
        this.feedbackCommentProcessor = feedbackCommentProcessor;
        this.properties = properties;
    }

    public MenuFeedbackAiContext toAiContext(
            MenuIntelligenceContext context) {

        List<DishFeedbackAiContext> dishFeedback =
                context.dishSummaries()
                        .stream()
                        .filter(summary -> summary.ratingCount()
                                >= properties.getDishRatingThreshold())
                        .map(summary -> toDishFeedbackAiContext(summary, context))
                        .toList();

        List<ProcessedFeedbackComment> sanitizedOverallComments =
                feedbackCommentSanitizer.sanitize(
                        context.overallMealComments()
                );

        long overallCommentCount = sanitizedOverallComments.stream()
                .mapToLong(ProcessedFeedbackComment::occurrences)
                .sum();

        List<ProcessedFeedbackComment> overallMealComments =
                overallCommentCount >= properties.getOverallCommentThreshold()
                        ? sanitizedOverallComments
                        : List.of();

        List<ProcessedFeedbackComment> currentPeriodOverallComments =
                getCurrentPeriodOverallComments(context);

        return new MenuFeedbackAiContext(
                context.currentPeriodStart(),
                context.currentPeriodEnd(),
                dishFeedback,
                overallMealComments,
                currentPeriodOverallComments
        );
    }

    private DishFeedbackAiContext toDishFeedbackAiContext(
            DishIntelligenceSummary summary,
            MenuIntelligenceContext context) {

        List<ProcessedFeedbackComment> sanitizedComments =
                feedbackCommentSanitizer.sanitize(summary.comments());

        List<ProcessedFeedbackComment> currentPeriodComments =
                getCurrentPeriodDishComments(summary.dishId(), context);

        return new DishFeedbackAiContext(
                summary.dishName(),
                summary.ratingCount(),
                summary.averageRating(),
                summary.oneStarCount(),
                summary.twoStarCount(),
                summary.threeStarCount(),
                summary.fourStarCount(),
                summary.fiveStarCount(),
                summary.lowRatingPercentage(),
                summary.threeStarOrBelowPercentage(),
                sanitizedComments,
                summary.currentPeriodRatingCount(),
                summary.currentPeriodAverageRating(),
                summary.previousPeriodRatingCount(),
                summary.previousPeriodAverageRating(),
                summary.ratingTrend(),
                currentPeriodComments
        );
    }

    private List<ProcessedFeedbackComment> getCurrentPeriodDishComments(
            Long dishId,
            MenuIntelligenceContext context) {

        if (context.currentPeriodStart() == null
                || context.currentPeriodEnd() == null) {
            return List.of();
        }

        List<String> comments = context.observations()
                .stream()
                .filter(observation -> observation.dishId().equals(dishId))
                .filter(observation -> isInCurrentPeriod(
                        observation.menuDate(),
                        context
                ))
                .map(MenuIntelligenceObservation::comment)
                .filter(comment -> comment != null && !comment.isBlank())
                .toList();

        return feedbackCommentSanitizer.sanitize(
                feedbackCommentProcessor.process(comments)
        );
    }

    private List<ProcessedFeedbackComment> getCurrentPeriodOverallComments(
            MenuIntelligenceContext context) {

        if (context.currentPeriodStart() == null
                || context.currentPeriodEnd() == null) {
            return List.of();
        }

        List<String> comments = context.overallMealFeedbackObservations()
                .stream()
                .filter(observation -> isInCurrentPeriod(
                        observation.getMenuDate(),
                        context
                ))
                .map(OverallMealFeedbackProjection::getComment)
                .filter(comment -> comment != null && !comment.isBlank())
                .toList();

        List<ProcessedFeedbackComment> processedComments =
                feedbackCommentSanitizer.sanitize(
                        feedbackCommentProcessor.process(comments)
                );

        long commentCount = processedComments.stream()
                .mapToLong(ProcessedFeedbackComment::occurrences)
                .sum();

        return commentCount >= properties.getOverallCommentThreshold()
                ? processedComments
                : List.of();
    }

    private boolean isInCurrentPeriod(
            java.time.LocalDate menuDate,
            MenuIntelligenceContext context) {

        return menuDate != null
                && !menuDate.isBefore(context.currentPeriodStart())
                && !menuDate.isAfter(context.currentPeriodEnd());
    }
}