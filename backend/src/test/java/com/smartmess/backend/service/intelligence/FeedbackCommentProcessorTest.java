package com.smartmess.backend.service.intelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeedbackCommentProcessorTest {

    private FeedbackCommentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new FeedbackCommentProcessor();
    }

    @Test
    void processShouldNormalizeAndConsolidateDuplicateComments() {

        List<String> comments = List.of(
                "Too spicy",
                " Too spicy ",
                "Too   spicy",
                "Oil was excessive",
                "Oil was excessive",
                "Good taste"
        );

        List<ProcessedFeedbackComment> result =
                processor.process(comments);

        assertEquals(3, result.size());

        assertEquals("Too spicy", result.get(0).text());
        assertEquals(3L, result.get(0).occurrences());

        assertEquals("Oil was excessive", result.get(1).text());
        assertEquals(2L, result.get(1).occurrences());

        assertEquals("Good taste", result.get(2).text());
        assertEquals(1L, result.get(2).occurrences());
    }

    @Test
    void processShouldIgnoreNullAndBlankComments() {

        List<String> comments = java.util.Arrays.asList(
                "Good taste",
                null,
                "",
                "   ",
                "\t",
                "Good taste"
        );

        List<ProcessedFeedbackComment> result =
                processor.process(comments);

        assertEquals(1, result.size());
        assertEquals("Good taste", result.get(0).text());
        assertEquals(2L, result.get(0).occurrences());
    }

    @Test
    void processShouldReturnEmptyListForNullOrEmptyInput() {

        assertTrue(processor.process(null).isEmpty());
        assertTrue(processor.process(List.of()).isEmpty());
    }

    @Test
    void processShouldKeepDifferentCaseCommentsSeparate() {

        List<String> comments = List.of(
                "Good taste",
                "good taste"
        );

        List<ProcessedFeedbackComment> result =
                processor.process(comments);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).occurrences());
        assertEquals(1L, result.get(1).occurrences());
    }
}