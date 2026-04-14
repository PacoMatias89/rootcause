package com.rootcause.dto;

import java.util.List;

/**
 * Public API response used to represent a paginated analysis history result.
 *
 * <p>This DTO wraps the list of analysis summaries together with pagination metadata
 * so clients can navigate large result sets in a stable and explicit way.</p>
 *
 * @param items page content containing analysis summary responses
 * @param page zero-based page index returned by the API
 * @param size requested page size
 * @param totalElements total number of matching analyses
 * @param totalPages total number of available pages
 * @param first whether the current page is the first page
 * @param last whether the current page is the last page
 */

public record AnalysisPageResponse(
        List<AnalysisSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
