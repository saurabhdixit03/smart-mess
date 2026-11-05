
package com.smartmess.backend.service.intelligence.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.smartmess.backend.service.intelligence.DishFeedbackAiContext;
import com.smartmess.backend.service.intelligence.DishIntelligenceSummary;
import com.smartmess.backend.service.intelligence.MenuFeedbackAiContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.ai.dto.DishSuggestion;
import com.smartmess.backend.service.intelligence.ai.dto.MenuFeedbackAiResponse;
import com.smartmess.backend.service.intelligence.ai.dto.OverallMealSuggestion;

@Component
public class MenuFeedbackAiResponseValidator {

    public ValidatedMenuFeedbackSuggestions validate(
            MenuFeedbackAiResponse response,
            MenuIntelligenceContext sourceContext,
            MenuFeedbackAiContext aiContext) {

        if (response == null) {
            throw new IllegalArgumentException("AI response cannot be null.");
        }

        if (sourceContext == null || aiContext == null) {
            throw new IllegalArgumentException("AI context cannot be null.");
        }

        if (response.dishSuggestions() == null
                || response.overallMealSuggestions() == null) {
            throw new IllegalArgumentException(
                    "AI response suggestion lists cannot be null.");
        }

        Map<String, List<DishIntelligenceSummary>> sourceDishes =
                buildSourceDishMap(sourceContext);

        Set<String> eligibleDishNames = buildEligibleDishNames(aiContext);

        List<ValidatedDishSuggestion> validatedDishSuggestions =
                validateDishSuggestions(
                        response.dishSuggestions(),
                        sourceDishes,
                        eligibleDishNames);

        List<OverallMealSuggestion> validatedOverallSuggestions =
                validateOverallMealSuggestions(
                        response.overallMealSuggestions());

        return new ValidatedMenuFeedbackSuggestions(
                List.copyOf(validatedDishSuggestions),
                List.copyOf(validatedOverallSuggestions));
    }

    private Map<String, List<DishIntelligenceSummary>> buildSourceDishMap(
            MenuIntelligenceContext context) {

        Map<String, List<DishIntelligenceSummary>> result = new HashMap<>();

        for (DishIntelligenceSummary summary : context.dishSummaries()) {

            if (summary.dishId() == null || summary.dishName() == null
                    || summary.dishName().isBlank()) {
                throw new IllegalArgumentException(
                        "Source dish information is incomplete.");
            }

            String key = normalizeDishName(summary.dishName());

            result.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(summary);
        }

        return result;
    }

    private Set<String> buildEligibleDishNames(
            MenuFeedbackAiContext context) {

        if (context.dishFeedback() == null) {
            throw new IllegalArgumentException(
                    "AI dish feedback context cannot be null.");
        }

        Set<String> result = new HashSet<>();

        for (DishFeedbackAiContext dish : context.dishFeedback()) {

            if (dish.dishName() == null || dish.dishName().isBlank()) {
                throw new IllegalArgumentException(
                        "AI context contains an invalid dish name.");
            }

            String key = normalizeDishName(dish.dishName());

            if (!result.add(key)) {
                throw new IllegalArgumentException(
                        "AI context contains ambiguous dish names.");
            }
        }

        return result;
    }

    private List<ValidatedDishSuggestion> validateDishSuggestions(
            List<DishSuggestion> suggestions,
            Map<String, List<DishIntelligenceSummary>> sourceDishes,
            Set<String> eligibleDishNames) {

        List<ValidatedDishSuggestion> result = new ArrayList<>();
        Set<Long> processedDishIds = new HashSet<>();

        for (DishSuggestion suggestion : suggestions) {

            if (suggestion == null) {
                throw new IllegalArgumentException(
                        "AI returned a null dish suggestion.");
            }

            String dishName = validateText(
                    suggestion.dishName(), "Dish name");

            String key = normalizeDishName(dishName);

            if (!eligibleDishNames.contains(key)) {
                throw new IllegalArgumentException(
                        "AI suggested a dish that was not included in its input.");
            }

            List<DishIntelligenceSummary> matches =
                    sourceDishes.getOrDefault(key, List.of());

            if (matches.size() != 1) {
                throw new IllegalArgumentException(
                        "Dish name is unknown or ambiguous: " + dishName);
            }

            DishIntelligenceSummary sourceDish = matches.get(0);

            if (!processedDishIds.add(sourceDish.dishId())) {
                throw new IllegalArgumentException(
                        "AI returned duplicate suggestions for a dish.");
            }

            String observation = validateText(
                    suggestion.observation(), "Dish observation");

            String recommendation = validateText(
                    suggestion.recommendation(), "Dish recommendation");

            if (suggestion.priority() == null) {
                throw new IllegalArgumentException(
                        "Dish suggestion priority cannot be null.");
            }

            result.add(new ValidatedDishSuggestion(
                    sourceDish.dishId(),
                    sourceDish.dishName(),
                    observation,
                    recommendation,
                    suggestion.priority()));
        }

        return result;
    }

    private List<OverallMealSuggestion> validateOverallMealSuggestions(
            List<OverallMealSuggestion> suggestions) {

        List<OverallMealSuggestion> result = new ArrayList<>();

        for (OverallMealSuggestion suggestion : suggestions) {

            if (suggestion == null) {
                throw new IllegalArgumentException(
                        "AI returned a null overall meal suggestion.");
            }

            String observation = validateText(
                    suggestion.observation(), "Overall meal observation");

            String recommendation = validateText(
                    suggestion.recommendation(),
                    "Overall meal recommendation");

            if (suggestion.priority() == null) {
                throw new IllegalArgumentException(
                        "Overall meal suggestion priority cannot be null.");
            }

            result.add(new OverallMealSuggestion(
                    observation,
                    recommendation,
                    suggestion.priority()));
        }

        return result;
    }

    private String validateText(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty.");
        }

        return value.strip();
    }

    private String normalizeDishName(String dishName) {

        return dishName.strip()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
