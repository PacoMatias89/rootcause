package com.rootcause.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AnalyzeResponse(
        UUID analysisId,
        OffsetDateTime analyzedAt,
        String category,
        String severity,
        String probableCause,
        List<String> detectedPatterns,
        List<String> recommendedSteps,
        BigDecimal confidence
) {
}
