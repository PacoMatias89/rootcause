package com.rootcause.model;

import java.util.Objects;

public record AnalysisRequestContext(
        String originalText,
        String normalizedText
) {
    public AnalysisRequestContext {
        originalText = Objects.requireNonNullElse(originalText, "");
        normalizedText = Objects.requireNonNullElse(normalizedText, "");
    }
}