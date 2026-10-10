package com.CompetCode.CompetCode.dto;

public record LeaderboardEntry(
        Long userId,
        String username,
        Integer rating,
        Integer problemsSolved,
        Integer battlesWon,
        Integer rank
) {}
