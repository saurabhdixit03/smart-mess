
package com.smartmess.backend.service.intelligence;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class DishIntelligenceCalculator {

    private final FeedbackCommentProcessor feedbackCommentProcessor;

    public DishIntelligenceCalculator(
            FeedbackCommentProcessor feedbackCommentProcessor) {
        this.feedbackCommentProcessor = feedbackCommentProcessor;
    }

    public List<DishIntelligenceSummary> calculate(
            List<MenuIntelligenceObservation> observations) {

        return calculate(observations, null, null);
    }

    public List<DishIntelligenceSummary> calculate(
            List<MenuIntelligenceObservation> observations,
            LocalDate periodStart,
            LocalDate periodEnd) {

        List<MenuIntelligenceObservation> filteredObservations =
                observations.stream()
                        .filter(observation ->
                                isWithinPeriod(observation, periodStart, periodEnd))
                        .toList();

        Map<Long, List<MenuIntelligenceObservation>> observationsByDish =
                filteredObservations.stream()
                        .collect(Collectors.groupingBy(
                                MenuIntelligenceObservation::dishId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return observationsByDish.values()
                .stream()
                .map(this::buildSummary)
                .sorted(Comparator.comparing(
                        DishIntelligenceSummary::dishName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    public List<DishIntelligenceSummary> calculateWithComparison(
            List<MenuIntelligenceObservation> observations,
            LocalDate currentStart,
            LocalDate currentEnd,
            LocalDate previousStart,
            LocalDate previousEnd) {

        Map<Long, List<MenuIntelligenceObservation>> allByDish =
                observations.stream()
                        .filter(observation -> observation.menuDate() != null)
                        .collect(Collectors.groupingBy(
                                MenuIntelligenceObservation::dishId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return allByDish.values().stream()
                .map(dishObservations -> buildComparisonSummary(
                        dishObservations,
                        currentStart,
                        currentEnd,
                        previousStart,
                        previousEnd
                ))
                .sorted(Comparator.comparing(
                        DishIntelligenceSummary::dishName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    private DishIntelligenceSummary buildComparisonSummary(
            List<MenuIntelligenceObservation> observations,
            LocalDate currentStart,
            LocalDate currentEnd,
            LocalDate previousStart,
            LocalDate previousEnd) {

        DishIntelligenceSummary base = buildSummary(observations);

        List<MenuIntelligenceObservation> current =
                filterByPeriod(observations, currentStart, currentEnd);

        List<MenuIntelligenceObservation> previous =
                filterByPeriod(observations, previousStart, previousEnd);

        long currentCount = current.size();
        long previousCount = previous.size();

        double currentAverage = calculateAverage(current);
        double previousAverage = calculateAverage(previous);

        String trend = determineTrend(
                currentAverage,
                previousAverage,
                currentCount,
                previousCount
        );

        return new DishIntelligenceSummary(
                base.dishId(),
                base.dishName(),
                base.ratingCount(),
                base.averageRating(),
                base.oneStarCount(),
                base.twoStarCount(),
                base.threeStarCount(),
                base.fourStarCount(),
                base.fiveStarCount(),
                base.lowRatingPercentage(),
                base.threeStarOrBelowPercentage(),
                base.comments(),
                currentCount,
                currentAverage,
                previousCount,
                previousAverage,
                trend
        );
    }

    private List<MenuIntelligenceObservation> filterByPeriod(
            List<MenuIntelligenceObservation> observations,
            LocalDate start,
            LocalDate end) {

        return observations.stream()
                .filter(observation ->
                        !observation.menuDate().isBefore(start)
                                && !observation.menuDate().isAfter(end))
                .toList();
    }

    private double calculateAverage(
            List<MenuIntelligenceObservation> observations) {

        return observations.stream()
                .mapToInt(MenuIntelligenceObservation::rating)
                .average()
                .orElse(0.0);
    }

    private String determineTrend(
            double currentAverage,
            double previousAverage,
            long currentCount,
            long previousCount) {

        if (currentCount == 0 || previousCount == 0) {
            return "INSUFFICIENT_DATA";
        }

        double difference = currentAverage - previousAverage;

        if (difference >= 0.25) {
            return "IMPROVED";
        }

        if (difference <= -0.25) {
            return "DECLINED";
        }

        return "STABLE";
    }

    private boolean isWithinPeriod(
            MenuIntelligenceObservation observation,
            LocalDate periodStart,
            LocalDate periodEnd) {

        if (periodStart == null && periodEnd == null) {
            return true;
        }

        if (observation.menuDate() == null) {
            return false;
        }

        return (periodStart == null
                        || !observation.menuDate().isBefore(periodStart))
                && (periodEnd == null
                        || !observation.menuDate().isAfter(periodEnd));
    }

    private DishIntelligenceSummary buildSummary(
            List<MenuIntelligenceObservation> observations) {

        MenuIntelligenceObservation first = observations.get(0);

        long oneStarCount = countRatings(observations, 1);
        long twoStarCount = countRatings(observations, 2);
        long threeStarCount = countRatings(observations, 3);
        long fourStarCount = countRatings(observations, 4);
        long fiveStarCount = countRatings(observations, 5);

        long ratingCount = observations.size();

        double averageRating = calculateAverage(observations);

        double lowRatingPercentage =
                ((oneStarCount + twoStarCount) * 100.0) / ratingCount;

        double threeStarOrBelowPercentage =
                ((oneStarCount + twoStarCount + threeStarCount) * 100.0)
                        / ratingCount;

        List<String> rawComments = observations.stream()
                .map(MenuIntelligenceObservation::comment)
                .toList();

        List<ProcessedFeedbackComment> processedComments =
                feedbackCommentProcessor.process(rawComments);

        return new DishIntelligenceSummary(
                first.dishId(),
                first.dishName(),
                ratingCount,
                averageRating,
                oneStarCount,
                twoStarCount,
                threeStarCount,
                fourStarCount,
                fiveStarCount,
                lowRatingPercentage,
                threeStarOrBelowPercentage,
                processedComments,
                0L,
                0.0,
                0L,
                0.0,
                "NOT_CALCULATED"
        );
    }

    private long countRatings(
            List<MenuIntelligenceObservation> observations,
            int rating) {

        return observations.stream()
                .filter(observation -> observation.rating() == rating)
                .count();
    }
}
