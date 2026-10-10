package com.CompetCode.CompetCode.dto;

import com.CompetCode.CompetCode.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long submissionId;
    private SubmissionStatus status;
    private Integer score;
    private Integer testsPassed;
    private Integer testsTotal;
    private Long executionTimeMs;
    private String errorMessage;
    private List<TestResultDto> testResults;
}
