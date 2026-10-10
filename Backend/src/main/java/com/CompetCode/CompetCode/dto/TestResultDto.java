package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDto {
    private int testNumber;
    private boolean passed;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String error;
    private Long executionTimeMs;
}
