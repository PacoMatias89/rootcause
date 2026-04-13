package com.rootcause.mapper;

import com.rootcause.entity.AnalysisRecordEntity;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalysisRecordMapper {

    private static final String LINE_SEPARATOR = "\n";

    public AnalysisRecordEntity toEntity(final String inputText, final AnalysisResult result) {
        final AnalysisRecordEntity entity = new AnalysisRecordEntity();
        entity.setId(result.analysisId());
        entity.setInputText(inputText);
        entity.setCategory(result.category().name());
        entity.setSeverity(result.severity().name());
        entity.setProbableCause(result.probableCause());
        entity.setDetectedPatterns(joinLines(result.detectedPatterns()));
        entity.setRecommendedSteps(joinLines(result.recommendedSteps()));
        entity.setConfidence(result.confidence());
        entity.setRuleCode(result.ruleCode());
        entity.setRawInputLength(result.rawInputLength());
        entity.setMatchedRuleCount(result.matchedRuleCount());
        entity.setAnalyzedAt(result.analyzedAt());
        return entity;
    }

    public AnalysisResult toModel(final AnalysisRecordEntity entity) {
        return new AnalysisResult(
                entity.getId(),
                entity.getAnalyzedAt(),
                ErrorCategory.valueOf(entity.getCategory()),
                Severity.valueOf(entity.getSeverity()),
                entity.getProbableCause(),
                splitLines(entity.getDetectedPatterns()),
                splitLines(entity.getRecommendedSteps()),
                entity.getConfidence(),
                entity.getRuleCode(),
                entity.getRawInputLength(),
                entity.getMatchedRuleCount()
        );
    }

    private String joinLines(final List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        return String.join(LINE_SEPARATOR, values);
    }

    private List<String> splitLines(final String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return value.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }
}