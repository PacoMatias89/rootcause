package com.rootcause.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for score-related operations used by the analysis engine.
 *
 * <p>This class centralizes small numeric helpers related to rule scoring,
 * such as constraining a score to an allowed range and converting a numeric
 * score into the public decimal format used by the application.</p>
 *
 * <p>The class is not intended to be instantiated.</p>
 */
public final class ScoreUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ScoreUtils() {
    }

    /**
     * Restricts the provided value so it stays within the given minimum and maximum bounds.
     *
     * @param value value to constrain
     * @param min lower allowed bound
     * @param max upper allowed bound
     * @return constrained value within the inclusive range [{@code min}, {@code max}]
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Converts a {@code double} score into a {@link BigDecimal} rounded to two decimal places.
     *
     * <p>The rounding mode used is {@link RoundingMode#HALF_UP}, which is appropriate for
     * the confidence values exposed by the API.</p>
     *
     * @param value numeric score to convert
     * @return score represented as a {@link BigDecimal} with scale 2
     */
    public static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}