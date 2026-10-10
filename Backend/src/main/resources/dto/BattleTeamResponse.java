package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleTeamResponse {
    private Long teamId;
    private String teamName;
    private String teamColor;
    private Integer score;
    private Integer rank;
    private List<BattleParticipantResponse> members;
}
