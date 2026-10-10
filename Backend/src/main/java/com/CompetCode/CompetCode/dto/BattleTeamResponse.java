package com.CompetCode.CompetCode.dto;

import java.util.List;

public record BattleTeamResponse(
        Long teamId,
        String teamName,
        String teamColor,
        Integer score,
        Integer rank,
        List<BattleParticipantResponse> members
) {}
