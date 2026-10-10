package com.CompetCode.CompetCode.dto;

public record TestResultDto(
        int testNumber,
        boolean passed,
        String input,
        String expectedOutput,
        String actualOutput,
        String error,
        Long executionTimeMs
) {}
