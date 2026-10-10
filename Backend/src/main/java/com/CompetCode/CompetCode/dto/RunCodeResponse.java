package com.CompetCode.CompetCode.dto;

public record RunCodeResponse(
        String stdout,
        String stderr,
        String compileOutput,
        int statusId,
        String statusDescription,
        Double time,
        Integer memory
) {}
