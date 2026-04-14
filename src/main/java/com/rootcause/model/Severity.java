package com.rootcause.model;

/**
 * Enumeration of the severity levels supported by the RootCause analysis engine.
 *
 * <p>Each value represents the estimated operational or technical impact of the
 * diagnosed issue.</p>
 *
 * <p>Severity is used internally by the rule engine to classify the importance
 * of a detected failure and also to break ties when multiple rules produce the
 * same score.</p>
 *
 * <p>The severity values are later exposed through the public API as strings.</p>
 */
public enum Severity {

    /**
     * Low-impact issue with limited urgency.
     */
    LOW,

    /**
     * Moderate-impact issue that should be addressed but is not the most critical.
     */
    MEDIUM,

    /**
     * High-impact issue that may significantly affect the application or user flow.
     */
    HIGH,

    /**
     * Critical issue that may block core functionality or prevent the system from operating.
     */
    CRITICAL
}