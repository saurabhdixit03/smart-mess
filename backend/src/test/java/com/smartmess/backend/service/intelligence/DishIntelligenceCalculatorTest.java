
package com.smartmess.backend.service.intelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DishIntelligenceCalculatorTest {

    private DishIntelligenceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DishIntelligenceCalculator(
                new FeedbackCommentProcessor()
        );
    }

    @Test
    void calculateShouldAggregateRatingsByDish() {

        List<MenuIntelligenceObservation> observations = List.of(
                observation(1L, "Paneer Masala", 1, "Too spicy"),
                observation(2L, "Paneer Masala", 2, "Too oily"),
                observation(3L, "Paneer Masala", 3, null),
                observation(4L, "Paneer Masala", 4, "Good taste"),
                observation(5L, "Paneer Masala", 5, "Excellent"),
                observation(6L, "Aloo Masala", 5, "Very good")
        );

        List<DishIntelligenceSummary> summaries =
                calculator.calculate(observations);

        assertEquals(2, summaries.size());
        assertEquals("Aloo Masala", summaries.get(0).dishName());
        assertEquals("Paneer Masala", summaries.get(1).dishName());

        DishIntelligenceSummary paneer = summaries.get(1);

        assertEquals(5L, paneer.ratingCount());
        assertEquals(3.0, paneer.averageRating(), 0.001);

        assertEquals(1L, paneer.oneStarCount());
        assertEquals(1L, paneer.twoStarCount());
        assertEquals(1L, paneer.threeStarCount());
        assertEquals(1L, paneer.fourStarCount());
        assertEquals(1L, paneer.fiveStarCount());

        assertEquals(40.0, paneer.lowRatingPercentage(), 0.001);
        assertEquals(60.0, paneer.threeStarOrBelowPercentage(), 0.001);
    }

    @Test
    void calculateShouldFilterObservationsWithinDateRange() {

        List<MenuIntelligenceObservation> observations = List.of(
                observation(1L, "Paneer Masala", 2, "Too oily",
                        LocalDate.of(2026, 10, 19)),
                observation(2L, "Paneer Masala", 4, "Good taste",
                        LocalDate.of(2026, 10, 22)),
                observation(3L, "Paneer Masala", 5, "Excellent",
                        LocalDate.of(2026, 10, 25)),
                observation(4L, "Paneer Masala", 1, "Too spicy",
                        LocalDate.of(2026, 10, 26))
        );

        List<DishIntelligenceSummary> summaries =
                calculator.calculate(
                        observations,
                        LocalDate.of(2026, 10, 20),
                        LocalDate.of(2026, 10, 25)
                );

        assertEquals(1, summaries.size());

        DishIntelligenceSummary summary = summaries.get(0);

        assertEquals(2L, summary.ratingCount());
        assertEquals(4.5, summary.averageRating(), 0.001);
        assertEquals(0L, summary.oneStarCount());
        assertEquals(1L, summary.fourStarCount());
        assertEquals(1L, summary.fiveStarCount());
    }

    @Test
    void calculateShouldExcludeObservationsWithoutMenuDateForPeriod() {

        List<MenuIntelligenceObservation> observations = List.of(
                observation(1L, "Dal Fry", 4, "Good taste",
                        LocalDate.of(2026, 10, 22)),
                observation(2L, "Dal Fry", 2, "Too spicy", null)
        );

        List<DishIntelligenceSummary> summaries =
                calculator.calculate(
                        observations,
                        LocalDate.of(2026, 10, 20),
                        LocalDate.of(2026, 10, 25)
                );

        assertEquals(1, summaries.size());
        assertEquals(1L, summaries.get(0).ratingCount());
        assertEquals(4.0, summaries.get(0).averageRating(), 0.001);
    }

    @Test
    void calculateShouldExcludeNullAndBlankComments() {

        List<MenuIntelligenceObservation> observations = List.of(
                observation(1L, "Dal Fry", 4, "Good taste"),
                observation(2L, "Dal Fry", 3, null),
                observation(3L, "Dal Fry", 5, "   ")
        );

        List<DishIntelligenceSummary> summaries =
                calculator.calculate(observations);

        assertEquals(1, summaries.size());

        assertEquals(
                List.of(new ProcessedFeedbackComment("Good taste", 1L)),
                summaries.get(0).comments()
        );
    }

    @Test
    void calculateShouldReturnEmptyListWhenNoObservationsExist() {

        List<DishIntelligenceSummary> summaries =
                calculator.calculate(List.of());

        assertTrue(summaries.isEmpty());
    }

    private MenuIntelligenceObservation observation(
            Long feedbackId,
            String dishName,
            Integer rating,
            String comment) {

        return observation(
                feedbackId,
                dishName,
                rating,
                comment,
                null
        );
    }

    private MenuIntelligenceObservation observation(
            Long feedbackId,
            String dishName,
            Integer rating,
            String comment,
            LocalDate menuDate) {

        return new MenuIntelligenceObservation(
                feedbackId,
                100L,
                menuDate,
                null,
                dishName.equals("Paneer Masala") ? 10L
                        : dishName.equals("Aloo Masala") ? 20L : 30L,
                dishName,
                rating,
                comment
        );
    }
}
