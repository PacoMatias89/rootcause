package com.rootcause.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AnalysisResult(
        UUID analysisId,
        OffsetDateTime analyzedAt,
        ErrorCategory category,
        Severity severity,
        String probableCause,
        List<String> detectedPatterns,
        List<String> recommendedActions,
        BigDecimal confidence
) {

    public AnalysisResult{
        detectedPatterns = detectedPatterns == null ? List.of() : List.copyOf(detectedPatterns);
        recommendedActions = recommendedActions == null ? List.of() : List.copyOf(recommendedActions);
    }


}
