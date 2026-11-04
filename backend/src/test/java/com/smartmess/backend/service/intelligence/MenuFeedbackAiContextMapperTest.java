package com.smartmess.backend.service.intelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartmess.backend.config.MenuIntelligenceProperties;
import com.smartmess.backend.mapper.MenuFeedbackAiContextMapper;
import com.smartmess.backend.repository.projection.OverallMealFeedbackProjection;

class MenuFeedbackAiContextMapperTest {

    private MenuFeedbackAiContextMapper mapper;

    private static final LocalDate PERIOD_START = LocalDate.of(2026, 10, 25);
    private static final LocalDate PERIOD_END = LocalDate.of(2026, 10, 31);

    @BeforeEach
    void setUp() {
        MenuIntelligenceProperties properties = new MenuIntelligenceProperties();

        mapper = new MenuFeedbackAiContextMapper(
                new FeedbackCommentSanitizer(),
                new FeedbackCommentProcessor(),
                properties
        );
    }

    @Test
    void toAiContextShouldMapDishStatisticsAndSanitizeComments() {
        DishIntelligenceSummary summary = new DishIntelligenceSummary(
                101L, "Paneer Masala", 5L, 3.0,
                1L, 1L, 1L, 1L, 1L, 40.0, 60.0,
                List.of(
                        new ProcessedFeedbackComment("Too spicy", 3L),
                        new ProcessedFeedbackComment("Contact saurabh@example.com", 1L)
                ),
                5L, 3.0, 5L, 2.0, "IMPROVED"
        );

        MenuIntelligenceContext internalContext = createContext(
                List.of(summary), List.of(), List.of(), null, null
        );

        MenuFeedbackAiContext result = mapper.toAiContext(internalContext);

        assertEquals(1, result.dishFeedback().size());

        DishFeedbackAiContext dish = result.dishFeedback().get(0);

        assertEquals("Paneer Masala", dish.dishName());
        assertEquals(5L, dish.ratingCount());
        assertEquals(3.0, dish.averageRating(), 0.001);
        assertEquals(1L, dish.oneStarCount());
        assertEquals(1L, dish.twoStarCount());
        assertEquals(1L, dish.threeStarCount());
        assertEquals(1L, dish.fourStarCount());
        assertEquals(1L, dish.fiveStarCount());
        assertEquals(40.0, dish.lowRatingPercentage(), 0.001);
        assertEquals(60.0, dish.threeStarOrBelowPercentage(), 0.001);

        assertEquals(
                List.of(
                        new ProcessedFeedbackComment("Too spicy", 3L),
                        new ProcessedFeedbackComment("Contact [EMAIL]", 1L)
                ),
                dish.comments()
        );

        assertEquals(5L, dish.currentPeriodRatingCount());
        assertEquals(3.0, dish.currentPeriodAverageRating(), 0.001);
        assertEquals(5L, dish.previousPeriodRatingCount());
        assertEquals(2.0, dish.previousPeriodAverageRating(), 0.001);
        assertEquals("IMPROVED", dish.ratingTrend());
    }

    @Test
    void toAiContextShouldKeepOverallCommentsSeparate() {
        DishIntelligenceSummary summary = new DishIntelligenceSummary(
                101L, "Dal Fry", 5L, 4.0,
                0L, 0L, 1L, 2L, 2L, 0.0, 20.0,
                List.of(new ProcessedFeedbackComment("Good taste", 2L)),
                5L, 4.2, 5L, 3.8, "IMPROVED"
        );

        MenuIntelligenceContext internalContext = createContext(
                List.of(summary),
                List.of(),
                List.of(
                        new ProcessedFeedbackComment("Meal was excellent", 3L),
                        new ProcessedFeedbackComment("Call me at 9876543210", 2L)
                ),
                null,
                null
        );

        MenuFeedbackAiContext result = mapper.toAiContext(internalContext);

        assertEquals(1, result.dishFeedback().size());
        assertEquals(
                List.of(new ProcessedFeedbackComment("Good taste", 2L)),
                result.dishFeedback().get(0).comments()
        );

        assertEquals(
                List.of(
                        new ProcessedFeedbackComment("Meal was excellent", 3L),
                        new ProcessedFeedbackComment("Call me at [PHONE]", 2L)
                ),
                result.overallMealComments()
        );
    }

    @Test
    void currentPeriodCommentsShouldUseMenuDateAndRemainSeparate() {
        DishIntelligenceSummary summary = createDishSummary(5L);

        List<MenuIntelligenceObservation> observations = List.of(
                observation(101L, "Too spicy", PERIOD_START),
                observation(101L, "Too spicy", PERIOD_END),
                observation(101L, "Old comment", PERIOD_START.minusDays(1)),
                observation(101L, "Future comment", PERIOD_END.plusDays(1))
        );

        List<OverallMealFeedbackProjection> overallObservations = List.of(
                overallObservation("Meal was good", PERIOD_START),
                overallObservation("Meal was good", PERIOD_START),
                overallObservation("Meal was good", PERIOD_START),
                overallObservation("Meal was good", PERIOD_END),
                overallObservation("Meal was good", PERIOD_END),
                overallObservation("Old overall comment", PERIOD_START.minusDays(1)),
                overallObservation("Future overall comment", PERIOD_END.plusDays(1))
        );

        MenuIntelligenceContext context = createContext(
                List.of(summary),
                observations,
                List.of(),
                PERIOD_START,
                PERIOD_END,
                overallObservations
        );

        MenuFeedbackAiContext result = mapper.toAiContext(context);

        assertEquals(
                List.of(new ProcessedFeedbackComment("Too spicy", 2L)),
                result.dishFeedback().get(0).currentPeriodComments()
        );

        assertEquals(
                List.of(new ProcessedFeedbackComment("Meal was good", 5L)),
                result.currentPeriodOverallComments()
        );

        assertEquals(PERIOD_START, result.currentPeriodStart());
        assertEquals(PERIOD_END, result.currentPeriodEnd());
    }

    @Test
    void aiContextDtosShouldNotExposeInternalIdentifiers() {
        Set<String> dishFields = Arrays.stream(DishFeedbackAiContext.class.getRecordComponents())
                .map(component -> component.getName())
                .collect(Collectors.toSet());

        Set<String> menuFields = Arrays.stream(MenuFeedbackAiContext.class.getRecordComponents())
                .map(component -> component.getName())
                .collect(Collectors.toSet());

        assertFalse(dishFields.contains("dishId"));
        assertFalse(menuFields.contains("messId"));
        assertFalse(dishFields.contains("menuId"));
        assertFalse(dishFields.contains("mealFeedbackId"));
        assertFalse(menuFields.contains("customerId"));
    }

    @Test
    void dishRatingThresholdShouldBeEnforcedAtFourFiveAndSixRatings() {
        for (long count : List.of(4L, 5L, 6L)) {
            MenuIntelligenceContext context = createContext(
                    List.of(createDishSummary(count)),
                    List.of(), List.of(), null, null
            );

            MenuFeedbackAiContext result = mapper.toAiContext(context);
            int expectedSize = count >= 5 ? 1 : 0;

            assertEquals(expectedSize, result.dishFeedback().size(),
                    "Unexpected result for dish rating count: " + count);
        }
    }

    @Test
    void overallCommentThresholdShouldBeEnforcedAtFourFiveAndSixOccurrences() {
        for (long count : List.of(4L, 5L, 6L)) {
            MenuIntelligenceContext context = createContext(
                    List.of(createDishSummary(5L)),
                    List.of(),
                    List.of(new ProcessedFeedbackComment("Meal was good", count)),
                    null,
                    null
            );

            MenuFeedbackAiContext result = mapper.toAiContext(context);
            int expectedSize = count >= 5 ? 1 : 0;

            assertEquals(expectedSize, result.overallMealComments().size(),
                    "Unexpected result for overall comment count: " + count);
        }
    }

    private MenuIntelligenceContext createContext(
            List<DishIntelligenceSummary> summaries,
            List<MenuIntelligenceObservation> observations,
            List<ProcessedFeedbackComment> overallComments,
            LocalDate periodStart,
            LocalDate periodEnd) {

        return createContext(
                summaries, observations, overallComments,
                periodStart, periodEnd, List.of()
        );
    }

    private MenuIntelligenceContext createContext(
            List<DishIntelligenceSummary> summaries,
            List<MenuIntelligenceObservation> observations,
            List<ProcessedFeedbackComment> overallComments,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<OverallMealFeedbackProjection> overallObservations) {

        return new MenuIntelligenceContext(
                999L,
                observations,
                summaries,
                overallComments,
                overallObservations,
                periodStart,
                periodEnd
        );
    }

    private MenuIntelligenceObservation observation(
            Long dishId, String comment, LocalDate menuDate) {

        return new MenuIntelligenceObservation(
                1L, 10L, menuDate, null,
                dishId, "Paneer Masala", 4, comment
        );
    }

    private OverallMealFeedbackProjection overallObservation(
            String comment, LocalDate menuDate) {

        return new OverallMealFeedbackProjection() {
            @Override
            public Long getMenuId() {
                return 10L;
            }

            @Override
            public LocalDate getMenuDate() {
                return menuDate;
            }

            @Override
            public com.smartmess.backend.enums.MealSession getMealSession() {
                return null;
            }

            @Override
            public String getComment() {
                return comment;
            }
        };
    }

    private DishIntelligenceSummary createDishSummary(long ratingCount) {
        return new DishIntelligenceSummary(
                101L, "Paneer Masala", ratingCount, 4.0,
                0L, 0L, 0L, ratingCount, 0L, 0.0, 0.0,
                List.of(new ProcessedFeedbackComment("Good taste", 2L)),
                5L, 4.0, 5L, 3.5, "IMPROVED"
        );
    }
}