package com.CompetCode.CompetCode.controller;

import com.CompetCode.CompetCode.dto.BattleParticipantResponse;
import com.CompetCode.CompetCode.dto.BattleResponse;
import com.CompetCode.CompetCode.dto.BattleTeamResponse;
import com.CompetCode.CompetCode.dto.CreateBattleRequest;
import com.CompetCode.CompetCode.entity.Battle;
import com.CompetCode.CompetCode.security.JwtUtil;
import com.CompetCode.CompetCode.service.BattleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;
    private final JwtUtil jwtUtil;

    // ── Mutation endpoints (require authentication) ───────────────────

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateBattleRequest request) {
        Long userId = resolveUserId(authHeader);
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(battleService.toResponse(battleService.createBattle(userId, request)));
    }

    @PostMapping("/join/{roomCode}")
    public ResponseEntity<?> join(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String roomCode) {
        Long userId = resolveUserId(authHeader);
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(battleService.toResponse(battleService.joinBattle(roomCode, userId)));
    }

    /**
     * Join a specific team within a battle (TEAM mode).
     */
    @PostMapping("/{id}/teams/{teamId}/join")
    public ResponseEntity<?> joinTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @PathVariable Long teamId) {
        Long userId = resolveUserId(authHeader);
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(battleService.toResponse(battleService.joinTeam(id, teamId, userId)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        Long userId = resolveUserId(authHeader);
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(battleService.toResponse(battleService.startBattle(id, userId)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        Long userId = resolveUserId(authHeader);
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(battleService.toResponse(battleService.completeBattle(id, userId)));
    }

    // ── Read-only endpoints (public or optionally authenticated) ──────

    @GetMapping("/active")
    public ResponseEntity<List<BattleResponse>> getActive() {
        return ResponseEntity.ok(battleService.getActiveBattleResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(battleService.toResponse(battleService.getBattle(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomCode}")
    public ResponseEntity<?> getByRoom(@PathVariable String roomCode) {
        try {
            return ResponseEntity.ok(battleService.toResponse(battleService.getBattleByRoomCode(roomCode)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<BattleParticipantResponse>> getLeaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(battleService.getLeaderboardResponses(id));
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<BattleTeamResponse>> getTeamLeaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(battleService.getTeamLeaderboard(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long id) {
        Battle battle = battleService.getBattle(id);
        return ResponseEntity.ok(Map.of(
                "status", battle.getStatus(),
                "battleMode", battle.getBattleMode(),
                "endsAt", battle.getEndsAt() != null ? battle.getEndsAt().toString() : "",
                "roomCode", battle.getRoomCode()
        ));
    }

    // ── Helper methods ─────────────────────────────────────────────────

    /**
     * Resolves the userId from an Authorization header OR from Spring Security context.
     * Returns null if neither is present/valid.
     */
    private Long resolveUserId(String authHeader) {
        // Try header first
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                return jwtUtil.extractUserId(authHeader.substring(7));
            } catch (Exception ignored) {}
        }
        // Fallback: try Spring Security context (set by JwtAuthFilter)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails ud) {
            // UserDetails doesn't store userId; try re-extracting from the token in context
            // At this point, if header was missing but filter set context, there's no easy way
            // to get userId without re-checking the header. Return null.
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Authentication required. Please log in."));
    }
}
