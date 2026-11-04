package com.smartmess.backend.service.intelligence.ai;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.smartmess.backend.mapper.MenuFeedbackAiContextMapper;
import com.smartmess.backend.service.intelligence.MenuFeedbackAiContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;

@Service
@ConditionalOnProperty(
        name = "spring.ai.model.chat",
        havingValue = "google-genai"
)
public class MenuFeedbackAiProcessingService {

    private final MenuFeedbackAiContextMapper contextMapper;
    private final MenuFeedbackAiService aiService;
    private final MenuFeedbackAiResponseValidator responseValidator;

    public MenuFeedbackAiProcessingService(
            MenuFeedbackAiContextMapper contextMapper,
            MenuFeedbackAiService aiService,
            MenuFeedbackAiResponseValidator responseValidator) {

        this.contextMapper = contextMapper;
        this.aiService = aiService;
        this.responseValidator = responseValidator;
    }

    public ValidatedMenuFeedbackSuggestions process(
            MenuIntelligenceContext sourceContext) {

        MenuFeedbackAiContext aiContext =
                contextMapper.toAiContext(sourceContext);

        boolean hasEligibleDishFeedback =
                aiContext.dishFeedback() != null
                        && !aiContext.dishFeedback().isEmpty();

        boolean hasEligibleOverallComments =
                hasComments(aiContext.overallMealComments())
                        || hasComments(
                                aiContext.currentPeriodOverallComments()
                        );

        if (!hasEligibleDishFeedback && !hasEligibleOverallComments) {
            return new ValidatedMenuFeedbackSuggestions(
                    List.of(),
                    List.of()
            );
        }

        var aiResponse = aiService.generateSuggestions(aiContext);

        return responseValidator.validate(
                aiResponse,
                sourceContext,
                aiContext
        );
    }

    private boolean hasComments(
            List<com.smartmess.backend.service.intelligence.ProcessedFeedbackComment> comments) {

        return comments != null && !comments.isEmpty();
    }
}