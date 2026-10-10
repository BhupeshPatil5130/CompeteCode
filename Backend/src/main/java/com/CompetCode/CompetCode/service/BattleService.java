package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.dto.BattleParticipantResponse;
import com.CompetCode.CompetCode.dto.BattleResponse;
import com.CompetCode.CompetCode.dto.BattleTeamResponse;
import com.CompetCode.CompetCode.dto.CreateBattleRequest;
import com.CompetCode.CompetCode.entity.*;
import com.CompetCode.CompetCode.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleService {

    private final BattleRepository battleRepository;
    private final BattleParticipantRepository participantRepository;
    private final BattleTeamRepository teamRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final RatingService ratingService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${battle.default-duration-minutes:30}")
    private int defaultDuration;

    @Value("${battle.max-participants:50}")
    private int defaultMaxParticipants;

    // Team color palette (cycles if more than 8 teams)
    private static final List<String[]> TEAM_COLORS = List.of(
        new String[]{"Team Red",    "red"},
        new String[]{"Team Blue",   "blue"},
        new String[]{"Team Green",  "green"},
        new String[]{"Team Purple", "purple"},
        new String[]{"Team Orange", "orange"},
        new String[]{"Team Cyan",   "cyan"},
        new String[]{"Team Pink",   "pink"},
        new String[]{"Team Yellow", "yellow"}
    );

    @Transactional
    public Battle createBattle(Long hostId, CreateBattleRequest request) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Problem> problems = problemRepository.findAllById(request.getProblemIds());
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("Problems not found");
        }

        BattleMode mode = BattleMode.SOLO;
        if ("TEAM".equalsIgnoreCase(request.getBattleMode())) {
            mode = BattleMode.TEAM;
        }

        int teamsCount = (request.getTeamsCount() != null && request.getTeamsCount() >= 2)
                ? Math.min(request.getTeamsCount(), 8) : 2;
        int teamSize = (request.getTeamSize() != null && request.getTeamSize() >= 1)
                ? Math.min(request.getTeamSize(), 20) : 5;

        String roomCode = generateRoomCode();

        Battle battle = Battle.builder()
                .roomCode(roomCode)
                .problems(problems)
                .host(host)
                .status(BattleStatus.WAITING)
                .battleMode(mode)
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : defaultDuration)
                .maxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : defaultMaxParticipants)
                .teamsCount(teamsCount)
                .teamSize(teamSize)
                .build();

        battle = battleRepository.save(battle);

        // Create team slots for TEAM mode
        if (mode == BattleMode.TEAM) {
            for (int i = 0; i < teamsCount; i++) {
                String[] colorInfo = TEAM_COLORS.get(i % TEAM_COLORS.size());
                BattleTeam team = BattleTeam.builder()
                        .battle(battle)
                        .teamName(colorInfo[0])
                        .teamColor(colorInfo[1])
                        .score(0)
                        .rank(0)
                        .build();
                teamRepository.save(team);
            }
        }

        // Add host as first participant
        List<BattleTeam> teams = teamRepository.findByBattleId(battle.getId());
        BattleTeam hostTeam = (mode == BattleMode.TEAM && !teams.isEmpty()) ? teams.get(0) : null;

        BattleParticipant hostParticipant = BattleParticipant.builder()
                .battle(battle)
                .user(host)
                .isReady(true)
                .team(hostTeam)
                .build();
        participantRepository.save(hostParticipant);

        broadcastBattleUpdate(battle);
        return battle;
    }

    @Transactional
    public Battle joinBattle(String roomCode, Long userId) {
        Battle battle = battleRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Battle room not found"));

        if (battle.getStatus() != BattleStatus.WAITING && battle.getStatus() != BattleStatus.ACTIVE) {
            throw new IllegalStateException("Battle is not joinable");
        }

        List<BattleParticipant> currentParticipants = participantRepository.findByBattleId(battle.getId());
        if (currentParticipants.size() >= battle.getMaxParticipants()) {
            throw new IllegalStateException("Battle is full");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (participantRepository.findByBattleIdAndUserId(battle.getId(), userId).isPresent()) {
            return battle;
        }

        // Auto-assign to team with fewest members for TEAM mode
        BattleTeam assignedTeam = null;
        if (battle.getBattleMode() == BattleMode.TEAM) {
            assignedTeam = findSmallestTeam(battle);
        }

        BattleParticipant participant = BattleParticipant.builder()
                .battle(battle)
                .user(user)
                .team(assignedTeam)
                .build();
        participantRepository.save(participant);

        broadcastBattleUpdate(battle);
        return battleRepository.findById(battle.getId()).orElse(battle);
    }

    @Transactional
    public Battle joinTeam(Long battleId, Long teamId, Long userId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));
        BattleTeam team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (!team.getBattle().getId().equals(battleId)) {
            throw new IllegalArgumentException("Team does not belong to this battle");
        }

        // Check team capacity
        long teamMemberCount = participantRepository.findByBattleId(battleId).stream()
                .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(teamId))
                .count();
        if (teamMemberCount >= battle.getTeamSize()) {
            throw new IllegalStateException("Team is full");
        }

        BattleParticipant participant = participantRepository
                .findByBattleIdAndUserId(battleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("You must join the battle first"));

        participant.setTeam(team);
        participantRepository.save(participant);

        broadcastBattleUpdate(battle);
        return battle;
    }

    @Transactional
    public Battle startBattle(Long battleId, Long hostId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));

        if (!battle.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Only host can start the battle");
        }

        battle.setStatus(BattleStatus.ACTIVE);
        battle.setStartedAt(Instant.now());
        battle.setEndsAt(Instant.now().plus(battle.getDurationMinutes(), ChronoUnit.MINUTES));
        battleRepository.save(battle);

        broadcastBattleUpdate(battle);
        return battle;
    }

    @Transactional
    public Battle completeBattle(Long battleId, Long requesterId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));

        if (!battle.getHost().getId().equals(requesterId)) {
            throw new IllegalArgumentException("Only host can complete the battle");
        }
        if (battle.getStatus() != BattleStatus.ACTIVE) {
            throw new IllegalStateException("Battle is not active");
        }

        if (battle.getBattleMode() == BattleMode.TEAM) {
            completeTeamBattle(battle);
        } else {
            completeSoloBattle(battle);
        }

        battle.setStatus(BattleStatus.COMPLETED);
        battle.setCompletedAt(Instant.now());
        battleRepository.save(battle);

        broadcastBattleUpdate(battle);
        return battle;
    }

    private void completeSoloBattle(Battle battle) {
        List<BattleParticipant> participants = participantRepository
                .findByBattleIdOrderByScoreDesc(battle.getId());

        int rank = 1;
        List<User> users = new ArrayList<>();
        List<Integer> ranks = new ArrayList<>();

        for (BattleParticipant p : participants) {
            p.setRank(rank++);
            users.add(p.getUser());
            ranks.add(p.getRank());
            participantRepository.save(p);
        }

        if (users.size() >= 2) {
            ratingService.updateRatingsAfterBattle(users, ranks);
            userRepository.saveAll(users);
        }
    }

    private void completeTeamBattle(Battle battle) {
        List<BattleTeam> teams = teamRepository.findByBattleIdOrderByScoreDesc(battle.getId());
        List<BattleParticipant> allParticipants = participantRepository.findByBattleId(battle.getId());

        // Recalculate each team's total score from member scores
        for (BattleTeam team : teams) {
            int teamTotal = allParticipants.stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(team.getId()))
                    .mapToInt(p -> p.getScore() != null ? p.getScore() : 0)
                    .sum();
            team.setScore(teamTotal);
            teamRepository.save(team);
        }

        // Re-sort teams by score after recalculation
        teams = teamRepository.findByBattleIdOrderByScoreDesc(battle.getId());

        // Assign team ranks
        int teamRank = 1;
        for (BattleTeam team : teams) {
            team.setRank(teamRank++);
            teamRepository.save(team);
        }

        // Update individual participant ranks within their team
        for (BattleTeam team : teams) {
            List<BattleParticipant> teamMembers = allParticipants.stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(team.getId()))
                    .sorted((a, b) -> (b.getScore() != null ? b.getScore() : 0) - (a.getScore() != null ? a.getScore() : 0))
                    .collect(Collectors.toList());

            int memberRank = 1;
            for (BattleParticipant p : teamMembers) {
                p.setRank(memberRank++);
                participantRepository.save(p);
            }
        }

        // ELO update: treat each team's average rating as a single "player"
        // Build representative user lists and ranks per team
        List<User> teamRepUsers = new ArrayList<>();
        List<Integer> teamRanks = new ArrayList<>();

        for (BattleTeam team : teams) {
            List<BattleParticipant> teamMembers = allParticipants.stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(team.getId()))
                    .collect(Collectors.toList());

            if (!teamMembers.isEmpty()) {
                // Use a virtual "average" user — update all real members by the team's Elo delta
                int avgRating = (int) teamMembers.stream()
                        .mapToInt(p -> p.getUser().getRating())
                        .average().orElse(1200);

                // Create virtual user for rating calc
                User virtual = new User();
                virtual.setRating(avgRating);
                virtual.setBattlesWon(0);
                virtual.setProblemsSolved(0);
                teamRepUsers.add(virtual);
                teamRanks.add(team.getRank());
            }
        }

        if (teamRepUsers.size() >= 2) {
            // Calculate Elo deltas using virtual users
            int n = teamRepUsers.size();
            double[] expected = new double[n];
            double[] actual = new double[n];
            for (int i = 0; i < n; i++) {
                actual[i] = (double)(n - teamRanks.get(i)) / Math.max(1, n - 1);
                expected[i] = 0;
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        expected[i] += 1.0 / (1.0 + Math.pow(10,
                                (teamRepUsers.get(j).getRating() - teamRepUsers.get(i).getRating()) / 400.0));
                    }
                }
                if (n > 1) expected[i] /= (n - 1);
            }

            // Apply same delta to all real team members
            for (int i = 0; i < teams.size() && i < n; i++) {
                final int teamIdx = i;
                final int delta = (int) Math.round(32 * (actual[teamIdx] - expected[teamIdx]));
                final boolean teamWon = teamRanks.get(i) == 1;
                final Long capturedTeamId = teams.get(i).getId();

                List<BattleParticipant> teamMembers = allParticipants.stream()
                        .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(capturedTeamId))
                        .collect(Collectors.toList());

                for (BattleParticipant member : teamMembers) {
                    User u = member.getUser();
                    u.setRating(Math.max(100, u.getRating() + delta));
                    if (teamWon) u.setBattlesWon(u.getBattlesWon() + 1);
                    userRepository.save(u);
                }
            }
        }
    }

    public List<Battle> getActiveBattles() {
        return battleRepository.findByStatus(BattleStatus.WAITING);
    }

    public List<BattleResponse> getActiveBattleResponses() {
        return getActiveBattles().stream().map(this::toResponse).toList();
    }

    public BattleResponse toResponse(Battle battle) {
        int count = participantRepository.findByBattleId(battle.getId()).size();
        BattleResponse response = BattleResponse.from(battle, count);

        // Include teams for TEAM mode battles
        if (battle.getBattleMode() == BattleMode.TEAM) {
            List<BattleTeam> teams = teamRepository.findByBattleIdOrderByScoreDesc(battle.getId());
            List<BattleParticipant> allParticipants = participantRepository.findByBattleId(battle.getId());

            List<BattleTeamResponse> teamResponses = teams.stream().map(team -> {
                List<BattleParticipantResponse> members = allParticipants.stream()
                        .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(team.getId()))
                        .map(p -> BattleParticipantResponse.builder()
                                .userId(p.getUser().getId())
                                .username(p.getUser().getUsername())
                                .score(p.getScore())
                                .rank(p.getRank())
                                .teamId(team.getId())
                                .teamName(team.getTeamName())
                                .teamColor(team.getTeamColor())
                                .build())
                        .collect(Collectors.toList());
                return BattleTeamResponse.builder()
                        .teamId(team.getId())
                        .teamName(team.getTeamName())
                        .teamColor(team.getTeamColor())
                        .score(team.getScore())
                        .rank(team.getRank())
                        .members(members)
                        .build();
            }).collect(Collectors.toList());
            response.setTeams(teamResponses);
        }

        return response;
    }

    public List<BattleParticipantResponse> getLeaderboardResponses(Long battleId) {
        return getLeaderboard(battleId).stream()
                .map(p -> BattleParticipantResponse.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername())
                        .score(p.getScore())
                        .rank(p.getRank())
                        .teamId(p.getTeam() != null ? p.getTeam().getId() : null)
                        .teamName(p.getTeam() != null ? p.getTeam().getTeamName() : null)
                        .teamColor(p.getTeam() != null ? p.getTeam().getTeamColor() : null)
                        .build())
                .toList();
    }

    public List<BattleTeamResponse> getTeamLeaderboard(Long battleId) {
        List<BattleTeam> teams = teamRepository.findByBattleIdOrderByScoreDesc(battleId);
        List<BattleParticipant> allParticipants = participantRepository.findByBattleId(battleId);

        return teams.stream().map(team -> {
            List<BattleParticipantResponse> members = allParticipants.stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(team.getId()))
                    .sorted((a, b) -> (b.getScore() != null ? b.getScore() : 0) - (a.getScore() != null ? a.getScore() : 0))
                    .map(p -> BattleParticipantResponse.builder()
                            .userId(p.getUser().getId())
                            .username(p.getUser().getUsername())
                            .score(p.getScore())
                            .rank(p.getRank())
                            .teamId(team.getId())
                            .teamName(team.getTeamName())
                            .teamColor(team.getTeamColor())
                            .build())
                    .collect(Collectors.toList());
            return BattleTeamResponse.builder()
                    .teamId(team.getId())
                    .teamName(team.getTeamName())
                    .teamColor(team.getTeamColor())
                    .score(team.getScore())
                    .rank(team.getRank())
                    .members(members)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Battle getBattle(Long id) {
        return battleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));
    }

    public Battle getBattleByRoomCode(String roomCode) {
        return battleRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));
    }

    public List<BattleParticipant> getLeaderboard(Long battleId) {
        return participantRepository.findByBattleIdOrderByScoreDesc(battleId);
    }

    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BattleTeam findSmallestTeam(Battle battle) {
        List<BattleTeam> teams = teamRepository.findByBattleId(battle.getId());
        if (teams.isEmpty()) return null;

        List<BattleParticipant> participants = participantRepository.findByBattleId(battle.getId());

        return teams.stream().min((a, b) -> {
            long aCount = participants.stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(a.getId()))
                    .count();
            long bCount = participants.stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(b.getId()))
                    .count();
            return Long.compare(aCount, bCount);
        }).orElse(teams.get(0));
    }

    public void broadcastBattleUpdate(Battle battle) {
        List<BattleParticipant> participants = participantRepository.findByBattleId(battle.getId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("battleId", battle.getId());
        payload.put("roomCode", battle.getRoomCode());
        payload.put("status", battle.getStatus());
        payload.put("battleMode", battle.getBattleMode());
        payload.put("endsAt", battle.getEndsAt());
        payload.put("participants", participants.stream().map(p -> {
            Map<String, Object> pm = new HashMap<>();
            pm.put("userId", p.getUser().getId());
            pm.put("username", p.getUser().getUsername());
            pm.put("score", p.getScore());
            pm.put("rank", p.getRank() != null ? p.getRank() : 0);
            if (p.getTeam() != null) {
                pm.put("teamId", p.getTeam().getId());
                pm.put("teamName", p.getTeam().getTeamName());
                pm.put("teamColor", p.getTeam().getTeamColor());
            }
            return pm;
        }).collect(Collectors.toList()));

        // Include team leaderboard for TEAM mode
        if (battle.getBattleMode() == BattleMode.TEAM) {
            List<BattleTeam> teams = teamRepository.findByBattleIdOrderByScoreDesc(battle.getId());
            payload.put("teams", teams.stream().map(t -> Map.of(
                    "teamId", t.getId(),
                    "teamName", t.getTeamName(),
                    "teamColor", t.getTeamColor(),
                    "score", t.getScore(),
                    "rank", t.getRank() != null ? t.getRank() : 0
            )).collect(Collectors.toList()));
        }

        messagingTemplate.convertAndSend("/topic/battle/" + battle.getId(), (Object) payload);
    }
}
