package com.CompetCode.CompetCode.dto;

import com.CompetCode.CompetCode.entity.Battle;
import com.CompetCode.CompetCode.entity.BattleMode;
import com.CompetCode.CompetCode.entity.BattleStatus;
import com.CompetCode.CompetCode.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResponse {
    private Long id;
    private String roomCode;
    private List<Problem> problems;
    private UserSummaryDto host;
    private BattleStatus status;
    private BattleMode battleMode;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private Integer teamsCount;
    private Integer teamSize;
    private Integer participantCount;
    private Instant startedAt;
    private Instant endsAt;
    private Instant completedAt;
    private Instant createdAt;
    private List<BattleTeamResponse> teams;

    public static BattleResponse from(Battle battle, int participantCount) {
        return BattleResponse.builder()
                .id(battle.getId())
                .roomCode(battle.getRoomCode())
                .problems(battle.getProblems())
                .host(UserSummaryDto.from(battle.getHost()))
                .status(battle.getStatus())
                .battleMode(battle.getBattleMode())
                .durationMinutes(battle.getDurationMinutes())
                .maxParticipants(battle.getMaxParticipants())
                .teamsCount(battle.getTeamsCount())
                .teamSize(battle.getTeamSize())
                .participantCount(participantCount)
                .startedAt(battle.getStartedAt())
                .endsAt(battle.getEndsAt())
                .completedAt(battle.getCompletedAt())
                .createdAt(battle.getCreatedAt())
                .build();
    }
}
