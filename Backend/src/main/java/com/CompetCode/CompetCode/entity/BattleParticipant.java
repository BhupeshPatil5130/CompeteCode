package com.CompetCode.CompetCode.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "battle_participants", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"battle_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    @JsonIgnore
    private Battle battle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The team this participant belongs to (null for SOLO battles) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    private BattleTeam team;

    @Builder.Default
    private Integer score = 0;

    private Integer rank;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "battle_participant_scores", joinColumns = @JoinColumn(name = "participant_id"))
    @MapKeyColumn(name = "problem_id")
    @Column(name = "score")
    @Builder.Default
    private Map<Long, Integer> problemScores = new HashMap<>();

    @Builder.Default
    private Boolean isReady = false;

    @CreationTimestamp
    private Instant joinedAt;
}
