package com.rootcause.dto;

import java.util.List;
/**
 * Public response DTO containing aggregated statistics for persisted analyses.
 *
 * @param totalAnalyses total number of stored analyses
 * @param byCategory grouped counts by category
 * @param bySeverity grouped counts by severity
 */
public record AnalysisStatsResponse(
        long totalAnalyses,
        List<AnalysisCountResponse> byCategory,
        List<AnalysisCountResponse> bySeverity
) {
}
