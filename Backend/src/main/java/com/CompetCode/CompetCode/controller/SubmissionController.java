package com.CompetCode.CompetCode.controller;

import com.CompetCode.CompetCode.dto.SubmissionResponse;
import com.CompetCode.CompetCode.dto.SubmitCodeRequest;
import com.CompetCode.CompetCode.dto.RunCodeRequest;
import com.CompetCode.CompetCode.dto.RunCodeResponse;
import com.CompetCode.CompetCode.entity.Submission;
import com.CompetCode.CompetCode.entity.SubmissionStatus;
import com.CompetCode.CompetCode.repository.SubmissionRepository;
import com.CompetCode.CompetCode.security.JwtUtil;
import com.CompetCode.CompetCode.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SubmitCodeRequest request) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(submissionService.submit(userId, request));
    }

    @PostMapping("/run")
    public ResponseEntity<RunCodeResponse> runCode(
            @Valid @RequestBody RunCodeRequest request) {
        return ResponseEntity.ok(submissionService.runCode(request));
    }

    @GetMapping("/{id}/feedback")
    public ResponseEntity<Map<String, String>> getFeedback(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = extractUserId(authHeader);
        String feedback = submissionService.getAiFeedback(userId, id);
        return ResponseEntity.ok(Map.of("feedback", feedback));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMySubmissions(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        List<Submission> submissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        List<Map<String, Object>> result = submissions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("problemId", s.getProblem().getId());
            map.put("problemTitle", s.getProblem().getTitle());
            map.put("language", s.getLanguage());
            map.put("status", s.getStatus().name());
            map.put("score", s.getScore());
            map.put("createdAt", s.getSubmittedAt() != null ? s.getSubmittedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my/stats")
    public ResponseEntity<Map<String, Object>> getMyStats(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        List<Submission> accepted = submissionRepository.findByUserIdAndStatus(userId, SubmissionStatus.ACCEPTED);

        // Count unique problems solved by difficulty
        long easySolved = accepted.stream()
                .filter(s -> s.getProblem().getDifficulty().name().equals("EASY"))
                .map(s -> s.getProblem().getId())
                .distinct()
                .count();
        long mediumSolved = accepted.stream()
                .filter(s -> s.getProblem().getDifficulty().name().equals("MEDIUM"))
                .map(s -> s.getProblem().getId())
                .distinct()
                .count();
        long hardSolved = accepted.stream()
                .filter(s -> s.getProblem().getDifficulty().name().equals("HARD"))
                .map(s -> s.getProblem().getId())
                .distinct()
                .count();
        long totalSolved = accepted.stream()
                .map(s -> s.getProblem().getId())
                .distinct()
                .count();

        // Calculate real streak
        java.util.List<java.time.LocalDate> dates = accepted.stream()
                .filter(s -> s.getSubmittedAt() != null)
                .map(s -> s.getSubmittedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                .distinct()
                .sorted(java.util.Comparator.reverseOrder())
                .collect(Collectors.toList());

        int streak = 0;
        java.time.LocalDate current = java.time.LocalDate.now();
        
        for (java.time.LocalDate d : dates) {
            if (d.equals(current)) {
                streak++;
                current = current.minusDays(1);
            } else if (d.equals(java.time.LocalDate.now().minusDays(1)) && streak == 0) {
                streak++;
                current = d.minusDays(1);
            } else {
                break;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSolved", totalSolved);
        stats.put("easySolved", easySolved);
        stats.put("mediumSolved", mediumSolved);
        stats.put("hardSolved", hardSolved);
        stats.put("streak", streak);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/my/heatmap")
    public ResponseEntity<Map<String, Integer>> getMyHeatmap(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        List<Submission> submissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        Map<String, Integer> heatmap = new HashMap<>();
        for (Submission s : submissions) {
            if (s.getSubmittedAt() != null) {
                String date = s.getSubmittedAt().atZone(java.time.ZoneId.systemDefault()).format(formatter);
                heatmap.put(date, heatmap.getOrDefault(date, 0) + 1);
            }
        }
        
        return ResponseEntity.ok(heatmap);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }
}
