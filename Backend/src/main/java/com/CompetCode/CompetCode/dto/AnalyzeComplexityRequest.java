package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeComplexityRequest(
        @NotBlank(message = "Code is required") String code,
        @NotBlank(message = "Language is required") String language
) {}
