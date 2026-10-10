package com.CompetCode.CompetCode.dto;

import com.CompetCode.CompetCode.entity.Battle;
import com.CompetCode.CompetCode.entity.BattleMode;
import com.CompetCode.CompetCode.entity.BattleStatus;
import com.CompetCode.CompetCode.entity.Problem;

import java.time.Instant;
import java.util.List;

public record BattleResponse(
        Long id,
        String roomCode,
        List<Problem> problems,
        UserSummaryDto host,
        BattleStatus status,
        BattleMode battleMode,
        Integer durationMinutes,
        Integer maxParticipants,
        Integer teamsCount,
        Integer teamSize,
        Integer participantCount,
        Instant startedAt,
        Instant endsAt,
        Instant completedAt,
        Instant createdAt,
        List<BattleTeamResponse> teams
) {
    public static BattleResponse from(Battle battle, int participantCount) {
        return new BattleResponse(
                battle.getId(),
                battle.getRoomCode(),
                battle.getProblems(),
                UserSummaryDto.from(battle.getHost()),
                battle.getStatus(),
                battle.getBattleMode(),
                battle.getDurationMinutes(),
                battle.getMaxParticipants(),
                battle.getTeamsCount(),
                battle.getTeamSize(),
                participantCount,
                battle.getStartedAt(),
                battle.getEndsAt(),
                battle.getCompletedAt(),
                battle.getCreatedAt(),
                null
        );
    }
}
