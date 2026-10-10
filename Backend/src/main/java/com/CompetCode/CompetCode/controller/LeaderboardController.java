package com.CompetCode.CompetCode.controller;

import com.CompetCode.CompetCode.dto.LeaderboardEntry;
import com.CompetCode.CompetCode.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final RoadmapService roadmapService;

    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntry> entries = roadmapService.getGlobalLeaderboard().stream()
                .map(e -> {
                    e.setRank(rank.getAndIncrement());
                    return e;
                })
                .toList();
        return ResponseEntity.ok(entries);
    }
}
