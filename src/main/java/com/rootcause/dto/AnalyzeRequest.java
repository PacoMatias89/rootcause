package com.rootcause.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public API request used to submit raw technical input for analysis.
 *
 * <p>This DTO represents the input payload accepted by the analysis endpoint.
 * It contains the raw text that the system will inspect, such as an error message,
 * a log excerpt, or a stack trace.</p>
 *
 * <p>Validation rules applied to {@code inputText}:</p>
 * <ul>
 *     <li>it must not be blank</li>
 *     <li>it must not exceed 20,000 characters</li>
 * </ul>
 *
 * @param inputText raw technical text to be analyzed
 */
public record AnalyzeRequest(
        @NotBlank(message = "inputText must not be blank")
        @Size(max = 20000, message = "inputText must not exceed 20,000 characters")
        String inputText
) {
}