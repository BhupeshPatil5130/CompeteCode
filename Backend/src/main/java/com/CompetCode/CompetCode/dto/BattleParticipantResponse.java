package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleParticipantResponse {
    private Long userId;
    private String username;
    private Integer score;
    private Integer rank;
    private Long teamId;
    private String teamName;
    private String teamColor;
}
