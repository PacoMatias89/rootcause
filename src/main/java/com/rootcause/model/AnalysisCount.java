package com.rootcause.model;
/**
 * Internal model representing a grouped analysis count.
 *
 * @param value grouped value, such as a category or severity name
 * @param count number of analyses associated with the grouped value
 */
public record AnalysisCount(
        String value,
        long count
) {
    /**
     * Creates an immutable grouped count.
     *
     * @param value grouped value
     * @param count grouped count
     * @throws NullPointerException when {@code value} is {@code null}
     * @throws IllegalArgumentException when {@code count} is negative
     */

    public AnalysisCount {
        if (value == null) {
            throw new NullPointerException("value must not be null");
        }
        if (count < 0) {
            throw new IllegalArgumentException("count must be greater than or equal to 0");
        }
    }
}
