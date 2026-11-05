package com.smartmess.backend.service.intelligence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class FeedbackCommentSanitizer {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b"
            );

    private static final Pattern INDIAN_PHONE_PATTERN =
            Pattern.compile(
                    "(?<!\\d)(?:\\+91[\\s-]?|91[\\s-]?)?[6-9]\\d{9}(?!\\d)"
            );

    public List<ProcessedFeedbackComment> sanitize(
            List<ProcessedFeedbackComment> comments) {

        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        Map<String, Long> sanitizedCounts = comments.stream()
                .filter(comment -> comment != null)
                .map(comment -> Map.entry(
                        sanitizeText(comment.text()),
                        comment.occurrences()
                ))
                .filter(entry -> !entry.getKey().isBlank())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));

        return sanitizedCounts.entrySet()
                .stream()
                .map(entry -> new ProcessedFeedbackComment(
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(
                        Comparator.comparingLong(
                                ProcessedFeedbackComment::occurrences
                        ).reversed()
                        .thenComparing(
                                ProcessedFeedbackComment::text,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }

    private String sanitizeText(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        String sanitized = EMAIL_PATTERN.matcher(text)
                .replaceAll("[EMAIL]");

        sanitized = INDIAN_PHONE_PATTERN.matcher(sanitized)
                .replaceAll("[PHONE]");

        return sanitized.strip().replaceAll("\\s+", " ");
    }
}