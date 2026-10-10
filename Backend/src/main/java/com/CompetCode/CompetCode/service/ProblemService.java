package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.dto.AnimatedExplanationResponse;
import com.CompetCode.CompetCode.dto.ComplexityAnalysisResponse;
import com.CompetCode.CompetCode.entity.Problem;
import com.CompetCode.CompetCode.entity.SubmissionStatus;
import com.CompetCode.CompetCode.entity.TestCase;
import com.CompetCode.CompetCode.repository.ProblemRepository;
import com.CompetCode.CompetCode.repository.SubmissionRepository;
import com.CompetCode.CompetCode.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final GeminiService geminiService;
    private final SubmissionRepository submissionRepository;

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Problem getProblem(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
    }

    public List<TestCase> getSampleTestCases(Long problemId) {
        List<TestCase> samples = testCaseRepository.findByProblemIdAndIsHiddenFalse(problemId);
        if (samples.isEmpty()) {
            List<TestCase> all = testCaseRepository.findByProblemId(problemId);
            if (!all.isEmpty()) {
                TestCase first = all.get(0);
                first.setIsHidden(false);
                return List.of(first);
            }
        }
        return samples;
    }

    public AnimatedExplanationResponse getAnimatedExplanation(Long problemId) {
        Problem problem = getProblem(problemId);
        return geminiService.generateAnimatedExplanation(problem);
    }

    public ComplexityAnalysisResponse analyzeComplexity(Long problemId, String code, String language) {
        Problem problem = getProblem(problemId);
        return geminiService.analyzeComplexity(problem, code, language);
    }

    public Map<String, String> getStarterCode(Long problemId, String language) {
        Problem problem = getProblem(problemId);
        return switch (language.toLowerCase()) {
            case "python", "python3" -> Map.of("code", nullSafe(problem.getStarterCodePython()));
            case "cpp", "c++" -> Map.of("code", nullSafe(problem.getStarterCodeCpp()));
            case "javascript", "js" -> Map.of("code", nullSafe(problem.getStarterCodeJavascript()));
            default -> Map.of("code", nullSafe(problem.getStarterCodeJava()));
        };
    }

    private String nullSafe(String s) {
        return s != null ? s : "// Write your solution here\n";
    }

    public Map<String, Object> getAcceptanceRate(Long problemId) {
        long total = submissionRepository.countByProblemId(problemId);
        long accepted = submissionRepository.countByProblemIdAndStatus(problemId, SubmissionStatus.ACCEPTED);
        double rate = total > 0 ? Math.round((double) accepted / total * 1000.0) / 10.0 : 0.0;
        return Map.of(
            "total", total,
            "accepted", accepted,
            "acceptanceRate", rate
        );
    }
}
