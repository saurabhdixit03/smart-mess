
package com.smartmess.backend.service.intelligence.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartmess.backend.service.intelligence.DishFeedbackAiContext;
import com.smartmess.backend.service.intelligence.DishIntelligenceSummary;
import com.smartmess.backend.service.intelligence.MenuFeedbackAiContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.ai.MenuFeedbackAiResponseValidator;
import com.smartmess.backend.service.intelligence.ai.ValidatedDishSuggestion;
import com.smartmess.backend.service.intelligence.ai.ValidatedMenuFeedbackSuggestions;
import com.smartmess.backend.service.intelligence.ai.dto.DishSuggestion;
import com.smartmess.backend.service.intelligence.ai.dto.MenuFeedbackAiResponse;
import com.smartmess.backend.service.intelligence.ai.dto.OverallMealSuggestion;
import com.smartmess.backend.enums.SuggestionPriority;

class MenuFeedbackAiResponseValidatorTest {

    private MenuFeedbackAiResponseValidator validator;

    private MenuIntelligenceContext sourceContext;
    private MenuFeedbackAiContext aiContext;

    @BeforeEach
    void setUp() {
        validator = new MenuFeedbackAiResponseValidator();

        DishIntelligenceSummary paneer = createDishSummary(
                12L, "Paneer Masala");

        DishIntelligenceSummary dal = createDishSummary(
                18L, "Dal Fry");

        sourceContext = createSourceContext(paneer, dal);

        aiContext = createAiContext(
                createDishAiContext("Paneer Masala"),
                createDishAiContext("Dal Fry"));
    }

    @Test
    void shouldMapValidDishSuggestionToCorrectDishId() {

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(new DishSuggestion(
                        "Paneer Masala",
                        "Customers frequently mention excessive oil.",
                        "Reduce oil during preparation.",
                        SuggestionPriority.HIGH)),
                List.of());

        ValidatedMenuFeedbackSuggestions result = validator.validate(
                response, sourceContext, aiContext);

        assertEquals(1, result.dishSuggestions().size());

        ValidatedDishSuggestion suggestion =
                result.dishSuggestions().get(0);

        assertEquals(12L, suggestion.dishId());
        assertEquals("Paneer Masala", suggestion.dishName());
        assertEquals(SuggestionPriority.HIGH, suggestion.priority());
    }

    @Test
    void shouldRejectUnknownDishName() {

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(new DishSuggestion(
                        "Paneer Butter Masala",
                        "Some observation",
                        "Some recommendation",
                        SuggestionPriority.MEDIUM)),
                List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(response, sourceContext, aiContext));
    }

    @Test
    void shouldRejectDishNotIncludedInAiContext() {

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(new DishSuggestion(
                        "Aloo Gobi",
                        "Some observation",
                        "Some recommendation",
                        SuggestionPriority.LOW)),
                List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(response, sourceContext, aiContext));
    }

    @Test
    void shouldRejectDuplicateSuggestionsForSameDish() {

        DishSuggestion first = new DishSuggestion(
                "Paneer Masala",
                "Too oily",
                "Reduce oil",
                SuggestionPriority.HIGH);

        DishSuggestion second = new DishSuggestion(
                "Paneer Masala",
                "Not spicy enough",
                "Adjust spices",
                SuggestionPriority.MEDIUM);

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(first, second),
                List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(response, sourceContext, aiContext));
    }

    @Test
    void shouldRejectAmbiguousDishNames() {

        DishIntelligenceSummary duplicatePaneer =
                createDishSummary(99L, "paneer   masala");

        MenuIntelligenceContext ambiguousContext =
                createSourceContext(
                        createDishSummary(12L, "Paneer Masala"),
                        duplicatePaneer,
                        createDishSummary(18L, "Dal Fry"));

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(new DishSuggestion(
                        "Paneer Masala",
                        "Some observation",
                        "Some recommendation",
                        SuggestionPriority.HIGH)),
                List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(
                        response, ambiguousContext, aiContext));
    }

    @Test
    void shouldRejectMissingDishObservation() {

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(new DishSuggestion(
                        "Paneer Masala",
                        " ",
                        "Reduce oil",
                        SuggestionPriority.HIGH)),
                List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(response, sourceContext, aiContext));
    }

    @Test
    void shouldRejectMissingDishRecommendation() {

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(new DishSuggestion(
                        "Paneer Masala",
                        "Too oily",
                        null,
                        SuggestionPriority.HIGH)),
                List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(response, sourceContext, aiContext));
    }

    @Test
    void shouldRejectMissingPriority() {

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(new DishSuggestion(
                        "Paneer Masala",
                        "Too oily",
                        "Reduce oil",
                        null)),
                List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(response, sourceContext, aiContext));
    }

    @Test
    void shouldRejectNullResponseAndNullSuggestionLists() {

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(null, sourceContext, aiContext));

        MenuFeedbackAiResponse nullDishList =
                new MenuFeedbackAiResponse(null, List.of());

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(
                        nullDishList, sourceContext, aiContext));

        MenuFeedbackAiResponse nullOverallList =
                new MenuFeedbackAiResponse(List.of(), null);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(
                        nullOverallList, sourceContext, aiContext));
    }

    @Test
    void shouldAcceptOverallMealSuggestions() {

        OverallMealSuggestion overallSuggestion =
                new OverallMealSuggestion(
                        "Customers mention inconsistent spice levels.",
                        "Standardize spice measurements.",
                        SuggestionPriority.MEDIUM);

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(),
                List.of(overallSuggestion));

        ValidatedMenuFeedbackSuggestions result = validator.validate(
                response, sourceContext, aiContext);

        assertEquals(0, result.dishSuggestions().size());
        assertEquals(1, result.overallMealSuggestions().size());

        assertEquals(
                SuggestionPriority.MEDIUM,
                result.overallMealSuggestions().get(0).priority());
    }

    @Test
    void shouldAcceptEmptySuggestionLists() {

        MenuFeedbackAiResponse response = new MenuFeedbackAiResponse(
                List.of(),
                List.of());

        ValidatedMenuFeedbackSuggestions result = validator.validate(
                response, sourceContext, aiContext);

        assertEquals(0, result.dishSuggestions().size());
        assertEquals(0, result.overallMealSuggestions().size());
    }

    private DishIntelligenceSummary createDishSummary(
            Long dishId,
            String dishName) {

        return new DishIntelligenceSummary(
                dishId,
                dishName,
                10L,
                4.0,
                0L,
                1L,
                2L,
                3L,
                4L,
                10.0,
                30.0,
                List.of(),
                2L,
                3.5,
                3L,
                4.0,
                "DECLINED");
    }

    private DishFeedbackAiContext createDishAiContext(String dishName) {

        return new DishFeedbackAiContext(
                dishName,
                10L,
                4.0,
                0L,
                1L,
                2L,
                3L,
                4L,
                10.0,
                30.0,
                List.of(),
                2L,
                3.5,
                3L,
                4.0,
                "DECLINED",
                List.of());
    }

    private MenuIntelligenceContext createSourceContext(
            DishIntelligenceSummary... summaries) {

        return new MenuIntelligenceContext(
                999L,
                List.of(),
                List.of(summaries),
                List.of(),
                List.of(),
                null,
                null);
    }

    private MenuFeedbackAiContext createAiContext(
            DishFeedbackAiContext... dishes) {

        return new MenuFeedbackAiContext(
                null,
                null,
                List.of(dishes),
                List.of(),
                List.of());
    }
}
