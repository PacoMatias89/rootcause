package com.rootcause.dto;

import jakarta.validation.constraints.*;

public record AnalyzeRequest(
        @NotBlank(message = "inputText must not be blank")
        @Size(max = 20000, message = "inputText must not exceed 20,000 characters")
        String inputText
) {
}
