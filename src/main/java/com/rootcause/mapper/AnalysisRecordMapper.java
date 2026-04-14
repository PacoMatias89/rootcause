package com.rootcause.mapper;

import com.rootcause.entity.AnalysisRecordEntity;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper responsible for converting between persistence entities and internal analysis models.
 *
 * <p>This component isolates the transformation logic between {@link AnalysisRecordEntity},
 * which is used for database persistence, and {@link AnalysisResult}, which is the internal
 * domain model used by the service layer.</p>
 *
 * <p>It also handles the conversion of multi-value fields such as detected patterns and
 * recommended steps, storing them as newline-separated text in the database and restoring
 * them as lists when reading from persistence.</p>
 */
@Component
public class AnalysisRecordMapper {

    private static final String LINE_SEPARATOR = "\n";

    /**
     * Converts an internal analysis result into its persistence entity representation.
     *
     * <p>This method maps all persisted fields, including classification data, diagnostic
     * details, metadata, and analysis timestamp. List-based fields are serialized as
     * newline-separated strings.</p>
     *
     * @param inputText sanitized raw input text that was analyzed
     * @param result internal analysis result to persist
     * @return persistence entity ready to be stored in the database
     */
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

    /**
     * Converts a persistence entity into the internal analysis model.
     *
     * <p>Enum values are reconstructed from their stored string representations and
     * newline-separated text fields are expanded back into immutable lists.</p>
     *
     * @param entity persisted analysis record entity
     * @return internal domain model representation of the stored analysis
     */
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

    /**
     * Joins a list of text values into a single newline-separated string.
     *
     * <p>When the provided list is {@code null} or empty, an empty string is returned.</p>
     *
     * @param values values to join
     * @return newline-separated string representation
     */
    private String joinLines(final List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        return String.join(LINE_SEPARATOR, values);
    }

    /**
     * Splits a newline-separated text value into a list of non-blank trimmed lines.
     *
     * <p>When the provided value is {@code null} or blank, an empty list is returned.</p>
     *
     * @param value stored multiline text value
     * @return list of trimmed non-blank lines
     */
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