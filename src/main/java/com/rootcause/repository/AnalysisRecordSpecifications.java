package com.rootcause.repository;

import com.rootcause.entity.AnalysisRecordEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification factory for {@link AnalysisRecordEntity} filtering.
 *
 * <p>This utility class centralizes the dynamic filtering rules used by the analysis
 * history endpoint. Blank filter values are ignored so callers can combine only the
 * criteria they need.</p>
 */
public final class AnalysisRecordSpecifications {

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
                criteriaBuilder.equal(root.get("category"), category);
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
                criteriaBuilder.equal(root.get("severity"), severity);
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
                criteriaBuilder.equal(root.get("ruleCode"), ruleCode);
    }
}