package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotBlank;

public record RunCodeRequest(
        @NotBlank String code,
        @NotBlank String language,
        String stdin
) {}
