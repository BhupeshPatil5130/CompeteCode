package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private Long userId;
    private String username;
    private Integer rating;
    private Integer problemsSolved;
    private Integer battlesWon;
    private Integer rank;
}
