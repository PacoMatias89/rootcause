package com.rootcause.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Public API response returned after a successful analysis request.
 *
 * <p>This DTO represents the structured diagnostic result exposed by the REST API.
 * It intentionally keeps {@code category} and {@code severity} as strings to preserve
 * the public contract already defined for the application.</p>
 *
 * @param analysisId identifier of the persisted analysis
 * @param analyzedAt timestamp when the analysis was produced
 * @param category detected error category
 * @param severity detected severity level
 * @param probableCause most likely technical cause inferred by the rule engine
 * @param detectedPatterns relevant patterns found in the input text
 * @param recommendedSteps ordered list of recommended next actions
 * @param confidence confidence score of the selected diagnosis
 */

public record AnalyzeResponse(
        UUID analysisId,
        OffsetDateTime analyzedAt,
        String category,
        String severity,
        String probableCause,
        List<String> detectedPatterns,
        List<String> recommendedSteps,
        BigDecimal confidence,
        String ruleCode,
        Integer rawInputLength,
        Integer matchedRuleCount
) {
}