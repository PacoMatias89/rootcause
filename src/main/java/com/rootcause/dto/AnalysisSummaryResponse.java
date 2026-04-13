package com.rootcause.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

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