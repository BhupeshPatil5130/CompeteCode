package com.CompetCode.CompetCode.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "battles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomCode;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "battle_problems",
        joinColumns = @JoinColumn(name = "battle_id"),
        inverseJoinColumns = @JoinColumn(name = "problem_id")
    )
    @Builder.Default
    private List<Problem> problems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BattleStatus status = BattleStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BattleMode battleMode = BattleMode.SOLO;

    @Builder.Default
    private Integer durationMinutes = 30;

    @Builder.Default
    private Integer maxParticipants = 50;

    /** Number of teams (only for TEAM mode) */
    @Builder.Default
    private Integer teamsCount = 2;

    /** Max players per team (only for TEAM mode) */
    @Builder.Default
    private Integer teamSize = 5;

    private Instant startedAt;
    private Instant endsAt;
    private Instant completedAt;

    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<BattleParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<BattleTeam> teams = new ArrayList<>();

    @CreationTimestamp
    private Instant createdAt;
}
