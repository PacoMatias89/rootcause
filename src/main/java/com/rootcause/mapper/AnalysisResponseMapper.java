package com.rootcause.mapper;

import com.rootcause.dto.AnalysisSummaryResponse;
import com.rootcause.dto.AnalyzeResponse;
import com.rootcause.model.AnalysisResult;
import org.springframework.stereotype.Component;

/**
 * Mapper responsible for converting internal analysis results into public API response DTOs.
 *
 * <p>This component isolates the transformation from the internal {@link AnalysisResult}
 * model to the external response contracts exposed by the REST API.</p>
 *
 * <p>It currently supports two response shapes:</p>
 * <ul>
 *     <li>a full analysis response for detailed endpoints</li>
 *     <li>a summary response for history or listing endpoints</li>
 * </ul>
 *
 * <p>Enum-based values such as category and severity are exposed as their string names
 * to preserve the public API contract.</p>
 */
@Component
public class AnalysisResponseMapper {

    /**
     * Converts an internal analysis result into the full public analysis response.
     *
     * <p>This mapping is used for endpoints that return the complete diagnostic detail
     * of a single analysis, including detected patterns, recommended steps, and analysis metadata.</p>
     *
     * @param result internal analysis result
     * @return full public response DTO
     */
    public AnalyzeResponse toAnalyzeResponse(final AnalysisResult result) {
        return new AnalyzeResponse(
                result.analysisId(),
                result.analyzedAt(),
                result.category().name(),
                result.severity().name(),
                result.probableCause(),
                result.detectedPatterns(),
                result.recommendedSteps(),
                result.confidence(),
                result.ruleCode(),
                result.rawInputLength(),
                result.matchedRuleCount()
        );
    }

    /**
     * Converts an internal analysis result into the summary response used for listings.
     *
     * <p>This mapping is intended for endpoints that return collections of analyses,
     * where a compact representation is more appropriate than the full detailed response.</p>
     *
     * @param result internal analysis result
     * @return summary public response DTO
     */
    public AnalysisSummaryResponse toSummaryResponse(final AnalysisResult result) {
        return new AnalysisSummaryResponse(
                result.analysisId(),
                result.analyzedAt(),
                result.category().name(),
                result.severity().name(),
                result.probableCause(),
                result.confidence(),
                result.ruleCode(),
                result.rawInputLength(),
                result.matchedRuleCount()
        );
    }
}