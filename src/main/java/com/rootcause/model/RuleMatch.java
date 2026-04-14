package com.rootcause.model;

import java.util.List;

/**
 * Internal immutable representation of a rule evaluation result.
 *
 * <p>A {@code RuleMatch} is produced by an {@code AnalysisRule} after evaluating a normalized
 * analysis context. It contains both the classification data and the supporting diagnostic
 * details used later to build the final analysis result.</p>
 *
 * <p>The canonical constructor applies defensive normalization to list fields so that
 * {@code detectedPatterns} and {@code recommendedSteps} are never {@code null} and remain
 * immutable.</p>
 *
 * @param ruleCode stable internal identifier of the matched rule
 * @param category category assigned by the rule
 * @param severity severity assigned by the rule
 * @param score matching score produced by the rule engine
 * @param probableCause probable cause inferred from the rule
 * @param detectedPatterns patterns detected in the input that support the match
 * @param recommendedSteps suggested technical actions for this kind of failure
 */

public record RuleMatch(
        String ruleCode,
        ErrorCategory category,
        Severity severity,
        double score,
        String probableCause,
        List<String> detectedPatterns,
        List<String> recommendedSteps
) {
    /**
     * Canonical constructor with defensive handling for list fields.
     *
     * @param ruleCode stable internal identifier of the matched rule
     * @param category category assigned by the rule
     * @param severity severity assigned by the rule
     * @param score matching score produced by the rule engine
     * @param probableCause probable cause inferred from the rule
     * @param detectedPatterns detected textual patterns, converted to an immutable empty list when null
     * @param recommendedSteps recommended actions, converted to an immutable empty list when null
     */
    public RuleMatch {
        detectedPatterns = detectedPatterns == null ? List.of() : List.copyOf(detectedPatterns);
        recommendedSteps = recommendedSteps == null ? List.of() : List.copyOf(recommendedSteps);
    }
}