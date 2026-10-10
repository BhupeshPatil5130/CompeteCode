package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitCodeRequest(
        @NotNull Long problemId,
        @NotBlank String code,
        @NotBlank String language,
        Long battleId
) {}
