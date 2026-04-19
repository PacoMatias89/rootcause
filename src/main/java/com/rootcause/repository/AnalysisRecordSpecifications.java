package com.rootcause.repository;

import com.rootcause.entity.AnalysisRecordEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

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
}