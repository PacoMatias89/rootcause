package com.rootcause.repository;

import com.rootcause.entity.AnalysisRecordEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * Specification factory for {@link AnalysisRecordEntity} filtering.
 *
 * <p>This utility class centralizes the dynamic filtering rules used by the analysis
 * history endpoint. Blank or absent filter values are ignored so callers can combine
 * only the criteria they need.</p>
 */
public final class AnalysisRecordSpecifications {

    /**
     * Name of the entity field that stores the analysis category.
     */
    private static final String CATEGORY_FIELD = "category";

    /**
     * Name of the entity field that stores the analysis severity.
     */
    private static final String SEVERITY_FIELD = "severity";

    /**
     * Name of the entity field that stores the matched rule code.
     */
    private static final String RULE_CODE_FIELD = "ruleCode";

    /**
     * Name of the entity field that stores the analysis timestamp.
     */
    private static final String ANALYZED_AT_FIELD = "analyzedAt";

    /**
     * Name of the entity field that stores the original analyzed input text.
     */
    private static final String INPUT_TEXT_FIELD = "inputText";

    /**
     * Name of the entity field that stores the probable cause text.
     */
    private static final String PROBABLE_CAUSE_FIELD = "probableCause";

    /**
     * Name of the entity field that stores detected patterns as persisted text.
     */
    private static final String DETECTED_PATTERNS_FIELD = "detectedPatterns";

    /**
     * Name of the entity field that stores recommended steps as persisted text.
     */
    private static final String RECOMMENDED_STEPS_FIELD = "recommendedSteps";

    /**
     * Utility constructor.
     */
    private AnalysisRecordSpecifications() {
    }

    /**
     * Builds a category equality filter when a non-blank category is provided.
     *
     * @param category category filter value
     * @return specification for category equality, or an unrestricted specification when blank
     */
    public static Specification<AnalysisRecordEntity> hasCategory(final String category) {
        if (category == null || category.isBlank()) {
            return Specification.unrestricted();
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(CATEGORY_FIELD), category);
    }

    /**
     * Builds a severity equality filter when a non-blank severity is provided.
     *
     * @param severity severity filter value
     * @return specification for severity equality, or an unrestricted specification when blank
     */
    public static Specification<AnalysisRecordEntity> hasSeverity(final String severity) {
        if (severity == null || severity.isBlank()) {
            return Specification.unrestricted();
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(SEVERITY_FIELD), severity);
    }

    /**
     * Builds a rule-code equality filter when a non-blank rule code is provided.
     *
     * @param ruleCode rule-code filter value
     * @return specification for rule-code equality, or an unrestricted specification when blank
     */
    public static Specification<AnalysisRecordEntity> hasRuleCode(final String ruleCode) {
        if (ruleCode == null || ruleCode.isBlank()) {
            return Specification.unrestricted();
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(RULE_CODE_FIELD), ruleCode);
    }

    /**
     * Builds a lower-bound timestamp filter when a starting analysis date-time is provided.
     *
     * <p>The comparison is inclusive, so records whose {@code analyzedAt} value is exactly
     * equal to the provided bound are included in the result.</p>
     *
     * @param analyzedFrom lower bound for the analysis timestamp
     * @return specification for inclusive lower-bound filtering, or an unrestricted specification when absent
     */
    public static Specification<AnalysisRecordEntity> hasAnalyzedAtFrom(final OffsetDateTime analyzedFrom) {
        if (analyzedFrom == null) {
            return Specification.unrestricted();
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(ANALYZED_AT_FIELD), analyzedFrom);
    }

    /**
     * Builds an upper-bound timestamp filter when an ending analysis date-time is provided.
     *
     * <p>The comparison is inclusive, so records whose {@code analyzedAt} value is exactly
     * equal to the provided bound are included in the result.</p>
     *
     * @param analyzedTo upper bound for the analysis timestamp
     * @return specification for inclusive upper-bound filtering, or an unrestricted specification when absent
     */
    public static Specification<AnalysisRecordEntity> hasAnalyzedAtTo(final OffsetDateTime analyzedTo) {
        if (analyzedTo == null) {
            return Specification.unrestricted();
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(ANALYZED_AT_FIELD), analyzedTo);
    }

    /**
     * Builds a case-insensitive free-text search specification over the persisted textual
     * fields that are most useful for historical exploration.
     *
     * <p>The search is applied with partial matching over:</p>
     *
     * <ul>
     *     <li>{@code inputText}</li>
     *     <li>{@code probableCause}</li>
     *     <li>{@code detectedPatterns}</li>
     *     <li>{@code recommendedSteps}</li>
     *     <li>{@code ruleCode}</li>
     * </ul>
     *
     * <p>Blank values are treated as absent filters and therefore do not restrict the query.</p>
     *
     * @param search free-text search value
     * @return combined search specification, or an unrestricted specification when blank
     */
    public static Specification<AnalysisRecordEntity> matchesSearch(final String search) {
        if (search == null || search.isBlank()) {
            return Specification.unrestricted();
        }

        final String normalizedSearch = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get(INPUT_TEXT_FIELD)), normalizedSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(PROBABLE_CAUSE_FIELD)), normalizedSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(DETECTED_PATTERNS_FIELD)), normalizedSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(RECOMMENDED_STEPS_FIELD)), normalizedSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(RULE_CODE_FIELD)), normalizedSearch)
        );
    }
}