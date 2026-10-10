package com.CompetCode.CompetCode.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateBattleRequest {
    @NotEmpty
    private List<Long> problemIds;
    private Integer durationMinutes;
    private Integer maxParticipants;
    /** SOLO or TEAM — defaults to SOLO if not provided */
    private String battleMode;
    /** Number of teams (TEAM mode only, default 2) */
    private Integer teamsCount;
    /** Max players per team (TEAM mode only, default 5) */
    private Integer teamSize;
}
