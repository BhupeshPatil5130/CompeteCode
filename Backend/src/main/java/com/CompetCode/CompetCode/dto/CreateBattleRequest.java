package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateBattleRequest(
        @NotEmpty List<Long> problemIds,
        Integer durationMinutes,
        Integer maxParticipants,
        /** SOLO or TEAM — defaults to SOLO if not provided */
        String battleMode,
        /** Number of teams (TEAM mode only, default 2) */
        Integer teamsCount,
        /** Max players per team (TEAM mode only, default 5) */
        Integer teamSize
) {}
