package com.rootcause.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ScoreUtils {

    private ScoreUtils() {
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}