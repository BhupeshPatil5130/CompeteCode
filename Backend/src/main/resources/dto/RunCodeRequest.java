package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunCodeRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String language;

    private String stdin;
}
