package com.rootcause.mapper;

import com.rootcause.dto.AnalysisCountResponse;
import com.rootcause.dto.AnalysisStatsResponse;
import com.rootcause.model.AnalysisCount;
import com.rootcause.model.AnalysisStats;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper responsible for converting internal statistics models into public response DTOs.
 */
@Component
public class AnalysisStatsResponseMapper {

    /**
     * Converts an internal statistics model into the public API response.
     *
     * @param stats internal statistics model
     * @return public response DTO
     */
    public AnalysisStatsResponse toResponse(final AnalysisStats stats) {
        final List<AnalysisCountResponse> byCategory = stats.byCategory()
                .stream()
                .map(this::toCountResponse)
                .toList();

        final List<AnalysisCountResponse> bySeverity = stats.bySeverity()
                .stream()
                .map(this::toCountResponse)
                .toList();

        return new AnalysisStatsResponse(
                stats.totalAnalyses(),
                byCategory,
                bySeverity
        );
    }

    /**
     * Converts an internal grouped count into the public API grouped count DTO.
     *
     * @param count internal grouped count
     * @return public grouped count DTO
     */
    private AnalysisCountResponse toCountResponse(final AnalysisCount count) {
        return new AnalysisCountResponse(
                count.value(),
                count.count()
        );
    }
}