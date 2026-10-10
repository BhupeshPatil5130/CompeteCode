package com.CompetCode.CompetCode.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "battle_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    @Column(nullable = false)
    private String teamName;

    /**
     * CSS-friendly color identifier: "red", "blue", "green", "purple", "orange"
     */
    @Column(nullable = false)
    private String teamColor;

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    private Integer rank = 0;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BattleParticipant> members = new ArrayList<>();

    @CreationTimestamp
    private Instant createdAt;
}
