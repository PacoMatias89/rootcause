package com.rootcause.model;

import java.util.List;
/**
 * Internal model containing aggregated statistics for persisted analyses.
 *
 * @param totalAnalyses total number of stored analyses
 * @param byCategory grouped counts by category
 * @param bySeverity grouped counts by severity
 */
public record AnalysisStats(
        long totalAnalyses,
        List<AnalysisCount> byCategory,
        List<AnalysisCount> bySeverity
) {
    /**
     * Creates an immutable statistics model.
     *
     * @param totalAnalyses total number of stored analyses
     * @param byCategory grouped counts by category
     * @param bySeverity grouped counts by severity
     * @throws IllegalArgumentException when {@code totalAnalyses} is negative
     */

    public AnalysisStats{
        if (totalAnalyses < 0) {
            throw new IllegalArgumentException("totalAnalyses must be greater than or equal to 0");
        }

        byCategory = bySeverity == null ? List.of() : List.copyOf(byCategory);
        bySeverity = bySeverity == null ? List.of() : List.copyOf(bySeverity);
    }
}
