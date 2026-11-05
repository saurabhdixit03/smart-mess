package com.smartmess.backend.service.intelligence.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartmess.backend.mapper.MenuFeedbackAiContextMapper;
import com.smartmess.backend.service.intelligence.MenuFeedbackAiContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.ProcessedFeedbackComment;
import com.smartmess.backend.service.intelligence.ai.MenuFeedbackAiProcessingService;
import com.smartmess.backend.service.intelligence.ai.MenuFeedbackAiResponseValidator;
import com.smartmess.backend.service.intelligence.ai.MenuFeedbackAiService;
import com.smartmess.backend.service.intelligence.ai.ValidatedMenuFeedbackSuggestions;
import com.smartmess.backend.service.intelligence.ai.dto.MenuFeedbackAiResponse;

@ExtendWith(MockitoExtension.class)
class MenuFeedbackAiProcessingServiceTest {

    @Mock
    private MenuFeedbackAiContextMapper contextMapper;

    @Mock
    private MenuFeedbackAiService aiService;

    @Mock
    private MenuFeedbackAiResponseValidator responseValidator;

    @InjectMocks
    private MenuFeedbackAiProcessingService processingService;

    private MenuIntelligenceContext sourceContext;
    private MenuFeedbackAiContext aiContext;

    @BeforeEach
    void setUp() {
        sourceContext = mock(MenuIntelligenceContext.class);

        aiContext = new MenuFeedbackAiContext(
                null,
                null,
                List.of(),
                List.of(),
                List.of()
        );

        when(contextMapper.toAiContext(sourceContext))
                .thenReturn(aiContext);
    }

    @Test
    void shouldCallAiAndValidateWhenEligibleDishFeedbackExists() {
        MenuFeedbackAiContext contextWithDishFeedback =
                new MenuFeedbackAiContext(
                        null,
                        null,
                        List.of(mock(
                                com.smartmess.backend.service.intelligence.DishFeedbackAiContext.class
                        )),
                        List.of(),
                        List.of()
                );

        MenuFeedbackAiResponse aiResponse =
                mock(MenuFeedbackAiResponse.class);

        ValidatedMenuFeedbackSuggestions expected =
                new ValidatedMenuFeedbackSuggestions(
                        List.of(),
                        List.of()
                );

        when(contextMapper.toAiContext(sourceContext))
                .thenReturn(contextWithDishFeedback);

        when(aiService.generateSuggestions(contextWithDishFeedback))
                .thenReturn(aiResponse);

        when(responseValidator.validate(
                aiResponse,
                sourceContext,
                contextWithDishFeedback
        )).thenReturn(expected);

        ValidatedMenuFeedbackSuggestions result =
                processingService.process(sourceContext);

        assertEquals(expected, result);

        verify(aiService).generateSuggestions(contextWithDishFeedback);
        verify(responseValidator).validate(
                aiResponse,
                sourceContext,
                contextWithDishFeedback
        );
    }

    @Test
    void shouldSkipAiWhenNoEligibleFeedbackExists() {
        ValidatedMenuFeedbackSuggestions result =
                processingService.process(sourceContext);

        assertEquals(
                new ValidatedMenuFeedbackSuggestions(
                        List.of(),
                        List.of()
                ),
                result
        );

        verifyNoInteractions(aiService, responseValidator);
    }

    @Test
    void shouldCallAiWhenOnlyOverallCommentsExist() {
        MenuFeedbackAiContext contextWithOverallComments =
                new MenuFeedbackAiContext(
                        null,
                        null,
                        List.of(),
                        List.of(new ProcessedFeedbackComment(
                                "Food was too spicy.",
                                5
                        )),
                        List.of()
                );

        MenuFeedbackAiResponse aiResponse =
                mock(MenuFeedbackAiResponse.class);

        ValidatedMenuFeedbackSuggestions expected =
                new ValidatedMenuFeedbackSuggestions(
                        List.of(),
                        List.of()
                );

        when(contextMapper.toAiContext(sourceContext))
                .thenReturn(contextWithOverallComments);

        when(aiService.generateSuggestions(contextWithOverallComments))
                .thenReturn(aiResponse);

        when(responseValidator.validate(
                aiResponse,
                sourceContext,
                contextWithOverallComments
        )).thenReturn(expected);

        ValidatedMenuFeedbackSuggestions result =
                processingService.process(sourceContext);

        assertEquals(expected, result);

        verify(aiService).generateSuggestions(
                contextWithOverallComments
        );

        verify(responseValidator).validate(
                aiResponse,
                sourceContext,
                contextWithOverallComments
        );
    }

    @Test
    void shouldPropagateValidationFailure() {
        MenuFeedbackAiContext contextWithDishFeedback =
                new MenuFeedbackAiContext(
                        null,
                        null,
                        List.of(mock(
                                com.smartmess.backend.service.intelligence.DishFeedbackAiContext.class
                        )),
                        List.of(),
                        List.of()
                );

        MenuFeedbackAiResponse aiResponse =
                mock(MenuFeedbackAiResponse.class);

        IllegalArgumentException validationException =
                new IllegalArgumentException(
                        "Invalid AI response."
                );

        when(contextMapper.toAiContext(sourceContext))
                .thenReturn(contextWithDishFeedback);

        when(aiService.generateSuggestions(contextWithDishFeedback))
                .thenReturn(aiResponse);

        when(responseValidator.validate(
                aiResponse,
                sourceContext,
                contextWithDishFeedback
        )).thenThrow(validationException);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> processingService.process(sourceContext)
        );

        assertEquals(
                "Invalid AI response.",
                thrown.getMessage()
        );

        verify(aiService).generateSuggestions(contextWithDishFeedback);
    }
}