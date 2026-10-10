package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitCodeRequest {
    @NotNull
    private Long problemId;
    @NotBlank
    private String code;
    @NotBlank
    private String language;
    private Long battleId;
}
