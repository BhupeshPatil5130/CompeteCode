package com.CompetCode.CompetCode.controller;

import com.CompetCode.CompetCode.dto.AnalyzeComplexityRequest;
import com.CompetCode.CompetCode.entity.Problem;
import com.CompetCode.CompetCode.entity.TestCase;
import com.CompetCode.CompetCode.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<Problem>> getAll() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Problem> getById(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblem(id));
    }

    @GetMapping("/{id}/samples")
    public ResponseEntity<List<TestCase>> getSamples(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getSampleTestCases(id));
    }

    @GetMapping("/{id}/starter-code")
    public ResponseEntity<Map<String, String>> getStarterCode(
            @PathVariable Long id,
            @RequestParam(defaultValue = "java") String language) {
        return ResponseEntity.ok(problemService.getStarterCode(id, language));
    }

    @GetMapping("/{id}/explain")
    public ResponseEntity<?> getExplanation(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getAnimatedExplanation(id));
    }

    @PostMapping("/{id}/analyze-complexity")
    public ResponseEntity<?> analyzeComplexity(
            @PathVariable Long id,
            @RequestBody AnalyzeComplexityRequest request) {
        var analysis = problemService.analyzeComplexity(id, request.getCode(), request.getLanguage());
        return ResponseEntity.ok(Map.of(
            "analysis", analysis,
            "timeComplexity", analysis.getTimeComplexity(),
            "spaceComplexity", analysis.getSpaceComplexity(),
            "advice", analysis.getAdvice()
        ));
    }

    @GetMapping("/{id}/acceptance-rate")
    public ResponseEntity<Map<String, Object>> getAcceptanceRate(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getAcceptanceRate(id));
    }
}
