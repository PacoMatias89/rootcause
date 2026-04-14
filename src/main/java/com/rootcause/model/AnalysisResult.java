package com.rootcause.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Internal immutable result produced by the analysis engine.
 *
 * <p>This record represents the full domain-level outcome of an analysis execution.
 * It contains the generated diagnosis, the supporting diagnostic details, the final
 * confidence score, and the metadata needed for persistence and historical queries.</p>
 *
 * <p>The result is used internally by the service and mapping layers before being
 * transformed into public API response DTOs.</p>
 *
 * <p>Its canonical constructor guarantees that list-based fields are never {@code null}
 * and are always stored as immutable copies.</p>
 *
 * @param analysisId unique identifier of the analysis
 * @param analyzedAt timestamp when the analysis was executed
 * @param category detected error category
 * @param severity detected severity level
 * @param probableCause most likely technical cause inferred by the rule engine
 * @param detectedPatterns patterns detected in the analyzed input
 * @param recommendedSteps recommended remediation steps for the detected issue
 * @param confidence confidence score of the selected diagnosis
 * @param ruleCode internal code of the rule selected as the best match
 * @param rawInputLength length of the sanitized input text used during analysis
 * @param matchedRuleCount total number of rules that matched with a positive score
 */
public record AnalysisResult(
        UUID analysisId,
        OffsetDateTime analyzedAt,
        ErrorCategory category,
        Severity severity,
        String probableCause,
        List<String> detectedPatterns,
        List<String> recommendedSteps,
        BigDecimal confidence,
        String ruleCode,
        Integer rawInputLength,
        Integer matchedRuleCount
) {

    /**
     * Canonical constructor with defensive handling for list-based fields.
     *
     * @param analysisId unique identifier of the analysis
     * @param analyzedAt timestamp when the analysis was executed
     * @param category detected error category
     * @param severity detected severity level
     * @param probableCause most likely technical cause inferred by the rule engine
     * @param detectedPatterns detected patterns, converted to an immutable empty list when {@code null}
     * @param recommendedSteps recommended steps, converted to an immutable empty list when {@code null}
     * @param confidence confidence score of the selected diagnosis
     * @param ruleCode internal code of the selected rule
     * @param rawInputLength length of the sanitized input text
     * @param matchedRuleCount number of rules that matched with a positive score
     */
    public AnalysisResult {
        detectedPatterns = detectedPatterns == null ? List.of() : List.copyOf(detectedPatterns);
        recommendedSteps = recommendedSteps == null ? List.of() : List.copyOf(recommendedSteps);
    }
}