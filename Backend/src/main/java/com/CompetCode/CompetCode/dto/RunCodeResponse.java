package com.CompetCode.CompetCode.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RunCodeResponse {
    private String stdout;
    private String stderr;
    private String compileOutput;
    private int statusId;
    private String statusDescription;
    private Double time;
    private Integer memory;
}
