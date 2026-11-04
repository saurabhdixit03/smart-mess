package com.smartmess.backend.service.intelligence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class FeedbackCommentProcessor {

    public List<ProcessedFeedbackComment> process(
            List<String> comments) {

        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        Map<String, Long> commentCounts = comments.stream()
                .filter(comment -> comment != null)
                .map(String::strip)
                .map(comment -> comment.replaceAll("\\s+", " "))
                .filter(comment -> !comment.isBlank())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        return commentCounts.entrySet()
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
}