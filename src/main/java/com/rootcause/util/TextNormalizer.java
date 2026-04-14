package com.rootcause.util;

import java.util.Locale;

/**
 * Utility class responsible for normalizing raw text before analysis.
 *
 * <p>This class centralizes the normalization logic applied to input text so that
 * rule evaluation operates on a more consistent representation.</p>
 *
 * <p>The current normalization process performs the following steps:</p>
 * <ul>
 *     <li>returns an empty string when the input is {@code null}</li>
 *     <li>normalizes line endings to {@code \n}</li>
 *     <li>converts the text to lowercase using {@link Locale#ROOT}</li>
 *     <li>removes leading and trailing whitespace</li>
 * </ul>
 *
 * <p>The class is not intended to be instantiated.</p>
 */
public final class TextNormalizer {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TextNormalizer() {
    }

    /**
     * Normalizes the provided text so it can be processed more consistently by the rule engine.
     *
     * @param text raw input text to normalize
     * @return normalized text, or an empty string when the input is {@code null}
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}