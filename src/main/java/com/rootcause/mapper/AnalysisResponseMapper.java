package com.rootcause.mapper;

import com.rootcause.dto.AnalyzeResponse;
import com.rootcause.model.AnalysisResult;
import org.springframework.stereotype.Component;

@Component
public class AnalysisResponseMapper {

    public AnalyzeResponse toResponse(AnalysisResult result) {
        return new AnalyzeResponse(
                result.analysisId(),
                result.analyzedAt(),
                result.category().name(),
                result.severity().name(),
                result.probableCause(),
                result.detectedPatterns(),
                result.recommendedSteps(),
                result.confidence()
        );
    }
}