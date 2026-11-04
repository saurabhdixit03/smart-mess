package com.smartmess.backend.service.intelligence;

public record ProcessedFeedbackComment(
        String text,
        long occurrences
) {
}