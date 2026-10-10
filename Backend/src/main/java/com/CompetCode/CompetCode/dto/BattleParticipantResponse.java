package com.CompetCode.CompetCode.dto;

public record BattleParticipantResponse(
        Long userId,
        String username,
        Integer score,
        Integer rank,
        Long teamId,
        String teamName,
        String teamColor
) {}
