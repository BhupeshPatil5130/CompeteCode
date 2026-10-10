package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.dto.SubmissionResponse;
import com.CompetCode.CompetCode.dto.SubmitCodeRequest;
import com.CompetCode.CompetCode.dto.TestResultDto;
import com.CompetCode.CompetCode.dto.RunCodeRequest;
import com.CompetCode.CompetCode.dto.RunCodeResponse;
import com.CompetCode.CompetCode.entity.*;
import com.CompetCode.CompetCode.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final BattleParticipantRepository battleParticipantRepository;
    private final CodeExecutionService codeExecutionService;
    private final GeminiService geminiService;
    private final RatingService ratingService;
    private final BattleService battleService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public SubmissionResponse submit(Long userId, SubmitCodeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        List<TestCase> testCases = testCaseRepository.findByProblemId(problem.getId());
        if (testCases.isEmpty()) {
            List<TestCase> generated = geminiService.generateTestCases(problem, 5);
            if (!generated.isEmpty()) {
                testCaseRepository.saveAll(generated);
                testCases = testCaseRepository.findByProblemId(problem.getId());
            }
        }
        if (testCases.isEmpty()) {
            throw new IllegalStateException("Problem has no test cases configured");
        }

        boolean hadAcceptedBefore = submissionRepository.existsByUserIdAndProblemIdAndStatus(
                userId, problem.getId(), SubmissionStatus.ACCEPTED);

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .code(request.getCode())
                .language(request.getLanguage())
                .status(SubmissionStatus.RUNNING)
                .testsTotal(testCases.size())
                .build();
        submission = submissionRepository.save(submission);

        List<TestResultDto> results = new ArrayList<>();
        int passed = 0;
        long totalTime = 0;

        for (int i = 0; i < testCases.size(); i++) {
            if (i > 0) {
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
            TestResultDto result = codeExecutionService.runTestCase(
                    request.getCode(), request.getLanguage(), testCases.get(i), i + 1,
                    problem.getExecutionKey());
            results.add(result);
            if (result.isPassed()) passed++;
            if (result.getExecutionTimeMs() != null) totalTime += result.getExecutionTimeMs();
        }

        SubmissionStatus status = determineStatus(passed, testCases.size(), results);
        int score = ratingService.calculateSubmissionScore(
                passed, testCases.size(), totalTime, problem.getTimeLimitMs());

        submission.setStatus(status);
        submission.setTestsPassed(passed);
        submission.setScore(score);
        submission.setExecutionTimeMs(totalTime);
        try {
            submission.setTestResultsJson(objectMapper.writeValueAsString(results));
        } catch (Exception ignored) {}

        if (status == SubmissionStatus.RUNTIME_ERROR || status == SubmissionStatus.COMPILE_ERROR) {
            submission.setErrorMessage(results.stream()
                    .filter(r -> r.getError() != null)
                    .map(TestResultDto::getError)
                    .findFirst().orElse(null));
        }

        submissionRepository.save(submission);

        if (status == SubmissionStatus.ACCEPTED && !hadAcceptedBefore) {
            user.setProblemsSolved(user.getProblemsSolved() + 1);
            userRepository.save(user);
        }

        if (request.getBattleId() != null) {
            updateBattleScore(request.getBattleId(), userId, submission);
        }

        return SubmissionResponse.builder()
                .submissionId(submission.getId())
                .status(status)
                .score(score)
                .testsPassed(passed)
                .testsTotal(testCases.size())
                .executionTimeMs(totalTime)
                .errorMessage(submission.getErrorMessage())
                .testResults(results.stream()
                        .map(r -> TestResultDto.builder()
                                .testNumber(r.getTestNumber())
                                .passed(r.isPassed())
                                .input(r.getInput())
                                .expectedOutput(r.getExpectedOutput())
                                .actualOutput(r.getActualOutput())
                                .error(r.getError())
                                .executionTimeMs(r.getExecutionTimeMs())
                                .build())
                        .toList())
                .build();
    }

    public RunCodeResponse runCode(RunCodeRequest request) {
        return codeExecutionService.runRawCode(request.getCode(), request.getLanguage(), request.getStdin());
    }

    public String getAiFeedback(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        if (!submission.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not your submission");
        }
        return geminiService.analyzeCode(
                submission.getCode(),
                submission.getLanguage(),
                submission.getProblem(),
                submission.getErrorMessage());
    }

    private void updateBattleScore(Long battleId, Long userId, Submission submission) {
        battleParticipantRepository.findByBattleIdAndUserId(battleId, userId)
                .ifPresent(participant -> {
                    Long problemId = submission.getProblem().getId();
                    Integer currentBest = participant.getProblemScores().getOrDefault(problemId, 0);
                    if (submission.getScore() > currentBest) {
                        participant.getProblemScores().put(problemId, submission.getScore());
                        int totalScore = participant.getProblemScores().values().stream().mapToInt(Integer::intValue).sum();
                        participant.setScore(totalScore);
                        battleParticipantRepository.save(participant);
                        battleService.broadcastBattleUpdate(participant.getBattle());
                    }
                });
    }

    private SubmissionStatus determineStatus(int passed, int total, List<TestResultDto> results) {
        if (total == 0) return SubmissionStatus.WRONG_ANSWER;
        if (passed == total) return SubmissionStatus.ACCEPTED;
        boolean hasCompile = results.stream().anyMatch(r ->
                r.getError() != null && r.getError().toLowerCase().contains("compile"));
        if (hasCompile) return SubmissionStatus.COMPILE_ERROR;
        boolean hasRuntime = results.stream().anyMatch(r ->
                r.getError() != null && !r.getError().isBlank());
        if (hasRuntime && passed == 0) return SubmissionStatus.RUNTIME_ERROR;
        return SubmissionStatus.WRONG_ANSWER;
    }
}
