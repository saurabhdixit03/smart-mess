package com.smartmess.backend.service.intelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeedbackCommentSanitizerTest {

    private FeedbackCommentSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new FeedbackCommentSanitizer();
    }

    @Test
    void sanitizeShouldMaskEmailAddresses() {

        List<ProcessedFeedbackComment> input = List.of(
                new ProcessedFeedbackComment(
                        "Please contact me at saurabh@example.com",
                        1L
                )
        );

        List<ProcessedFeedbackComment> result =
                sanitizer.sanitize(input);

        assertEquals(1, result.size());
        assertEquals(
                "Please contact me at [EMAIL]",
                result.get(0).text()
        );
    }

    @Test
    void sanitizeShouldMaskIndianMobileNumbers() {

        List<ProcessedFeedbackComment> input = List.of(
                new ProcessedFeedbackComment(
                        "Call me at 9876543210",
                        1L
                ),
                new ProcessedFeedbackComment(
                        "My number is +91 9876543210",
                        1L
                )
        );

        List<ProcessedFeedbackComment> result =
                sanitizer.sanitize(input);

        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(
                comment -> comment.text().contains("[PHONE]")
        ));

        assertTrue(result.stream().noneMatch(
                comment -> comment.text().contains("9876543210")
        ));
    }

    @Test
    void sanitizeShouldCombineCommentsThatBecomeIdentical() {

        List<ProcessedFeedbackComment> input = List.of(
                new ProcessedFeedbackComment(
                        "Contact saurabh@example.com",
                        2L
                ),
                new ProcessedFeedbackComment(
                        "Contact another@example.com",
                        3L
                )
        );

        List<ProcessedFeedbackComment> result =
                sanitizer.sanitize(input);

        assertEquals(1, result.size());
        assertEquals(
                "Contact [EMAIL]",
                result.get(0).text()
        );
        assertEquals(5L, result.get(0).occurrences());
    }

    @Test
    void sanitizeShouldNormalizeWhitespace() {

        List<ProcessedFeedbackComment> input = List.of(
                new ProcessedFeedbackComment(
                        "  Food   was    good  ",
                        2L
                )
        );

        List<ProcessedFeedbackComment> result =
                sanitizer.sanitize(input);

        assertEquals(
                "Food was good",
                result.get(0).text()
        );
    }

    @Test
    void sanitizeShouldHandleNullAndEmptyInput() {

        assertTrue(sanitizer.sanitize(null).isEmpty());
        assertTrue(sanitizer.sanitize(List.of()).isEmpty());
    }
}