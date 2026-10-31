package com.smartmess.backend.service.intelligence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DishIntelligenceCalculator {

    public List<DishIntelligenceSummary> calculate(
            List<MenuIntelligenceObservation> observations) {

        Map<Long, List<MenuIntelligenceObservation>> observationsByDish =
                observations.stream()
                        .collect(
                                java.util.stream.Collectors.groupingBy(
                                        MenuIntelligenceObservation::dishId,
                                        LinkedHashMap::new,
                                        java.util.stream.Collectors.toList()
                                )
                        );

        return observationsByDish.values()
                .stream()
                .map(this::buildSummary)
                .sorted(
                        Comparator.comparing(
                                DishIntelligenceSummary::dishName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
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

        double averageRating = observations.stream()
                .mapToInt(MenuIntelligenceObservation::rating)
                .average()
                .orElse(0.0);

        double lowRatingPercentage =
                ratingCount == 0
                        ? 0.0
                        : ((oneStarCount + twoStarCount) * 100.0)
                                / ratingCount;
        
        double threeStarOrBelowPercentage =
                ratingCount == 0
                        ? 0.0
                        : ((oneStarCount
                                + twoStarCount
                                + threeStarCount) * 100.0)
                                / ratingCount;

        List<String> comments = observations.stream()
                .map(MenuIntelligenceObservation::comment)
                .filter(comment -> comment != null && !comment.isBlank())
                .toList();

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
                comments
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