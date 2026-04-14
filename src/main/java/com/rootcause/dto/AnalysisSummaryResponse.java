package com.rootcause.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Public API response used to represent a stored analysis in summary form.
 *
 * <p>This DTO is intended for analysis history or listing endpoints where a compact
 * representation is preferred over the full detailed response.</p>
 *
 * <p>It contains the main classification data together with metadata that helps
 * understand how the analysis was produced, such as the matched rule code,
 * sanitized input length, and number of matched rules.</p>
 *
 * @param analysisId unique identifier of the persisted analysis
 * @param analyzedAt timestamp when the analysis was executed
 * @param category detected error category
 * @param severity detected severity level
 * @param probableCause most likely technical cause inferred by the analysis
 * @param confidence confidence score of the selected diagnosis
 * @param ruleCode internal code of the rule selected as the best match
 * @param rawInputLength length of the sanitized input text used during analysis
 * @param matchedRuleCount total number of rules that matched with a positive score
 */
public record AnalysisSummaryResponse(
        UUID analysisId,
        OffsetDateTime analyzedAt,
        String category,
        String severity,
        String probableCause,
        BigDecimal confidence,
        String ruleCode,
        Integer rawInputLength,
        Integer matchedRuleCount
) {
}