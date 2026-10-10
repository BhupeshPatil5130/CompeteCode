package com.CompetCode.CompetCode.controller;

import com.CompetCode.CompetCode.dto.RoadmapProgressDto;
import com.CompetCode.CompetCode.entity.Roadmap;
import com.CompetCode.CompetCode.security.JwtUtil;
import com.CompetCode.CompetCode.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final JwtUtil jwtUtil;

    @GetMapping("/api/roadmaps")
    public ResponseEntity<List<Roadmap>> getAll() {
        return ResponseEntity.ok(roadmapService.getAllRoadmaps());
    }

    @GetMapping("/api/roadmaps/{id}")
    public ResponseEntity<Roadmap> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roadmapService.getRoadmap(id));
    }

    @GetMapping("/api/roadmaps/{id}/progress")
    public ResponseEntity<RoadmapProgressDto> getProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        return ResponseEntity.ok(roadmapService.getProgress(userId, id));
    }

    @PostMapping("/api/roadmaps/{roadmapId}/problems/{problemId}/complete")
    public ResponseEntity<Void> markComplete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long roadmapId,
            @PathVariable Long problemId) {
        Long userId = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        roadmapService.markCompleted(userId, roadmapId, problemId);
        return ResponseEntity.ok().build();
    }
}
