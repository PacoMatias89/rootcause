package com.rootcause.model;

import java.util.Objects;

/**
 * Immutable context object used during rule evaluation.
 *
 * <p>This record groups together the two text representations needed by the
 * rule engine during analysis:</p>
 * <ul>
 *     <li>the original input text received by the application</li>
 *     <li>the normalized version of that text prepared for rule matching</li>
 * </ul>
 *
 * <p>Its canonical constructor guarantees that neither field is {@code null}.
 * When a {@code null} value is provided, it is replaced with an empty string
 * so rule evaluation can operate safely without additional null checks.</p>
 *
 * @param originalText raw input text as received by the analysis flow
 * @param normalizedText normalized representation of the input text used for matching
 */
public record AnalysisRequestContext(
        String originalText,
        String normalizedText
) {

    /**
     * Canonical constructor with defensive null handling.
     *
     * @param originalText raw input text, replaced with an empty string when {@code null}
     * @param normalizedText normalized input text, replaced with an empty string when {@code null}
     */
    public AnalysisRequestContext {
        originalText = Objects.requireNonNullElse(originalText, "");
        normalizedText = Objects.requireNonNullElse(normalizedText, "");
    }
}