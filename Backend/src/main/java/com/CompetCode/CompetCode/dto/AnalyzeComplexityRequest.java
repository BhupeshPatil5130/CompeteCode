package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalyzeComplexityRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Language is required")
    private String language;
}
