package com.CompetCode.CompetCode.dto;

import com.CompetCode.CompetCode.entity.SubmissionStatus;

import java.util.List;

public record SubmissionResponse(
        Long submissionId,
        SubmissionStatus status,
        Integer score,
        Integer testsPassed,
        Integer testsTotal,
        Long executionTimeMs,
        String errorMessage,
        List<TestResultDto> testResults
) {}
