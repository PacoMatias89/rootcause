package com.rootcause.model;

import java.util.List;

public record RuleMatch(
        String ruleCode,
        ErrorCategory category,
        Severity severity,
        double score,
        String probableCause,
        List<String> detectedPatterns,
        List<String> recommendedSteps
) {
    public RuleMatch {
        detectedPatterns = detectedPatterns == null ? List.of() : List.copyOf(detectedPatterns);
        recommendedSteps = recommendedSteps == null ? List.of() : List.copyOf(recommendedSteps);
    }
}