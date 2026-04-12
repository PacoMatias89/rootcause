package com.rootcause.mapper;

import com.rootcause.entity.AnalysisRecordEntity;
import com.rootcause.model.AnalysisResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;


@Component
public class AnalysisRecordMapper {

    public AnalysisRecordEntity toEntity(String inputText, AnalysisResult result) {
        AnalysisRecordEntity entity = new AnalysisRecordEntity();
        entity.setId(result.analysisId());
        entity.setInputText(inputText);
        entity.setCategory(result.category().name());
        entity.setSeverity(result.severity().name());
        entity.setProbableCause(result.probableCause());
        entity.setDetectedPatterns(join(result.detectedPatterns()));
        entity.setRecommendedSteps(join(result.recommendedSteps()));
        entity.setConfidence(result.confidence());
        entity.setAnalyzedAt(result.analyzedAt());
        return entity;
    }

    public String join(List<String> values){
        if(values == null || values.isEmpty()){
            return "";
        }

        return  values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .reduce((left, right) -> left + " || " + right)
                .orElse("");
    }
}
