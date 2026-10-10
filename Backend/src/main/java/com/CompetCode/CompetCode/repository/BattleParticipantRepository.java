package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.BattleParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattleParticipantRepository extends JpaRepository<BattleParticipant, Long> {
    List<BattleParticipant> findByBattleIdOrderByScoreDesc(Long battleId);
    Optional<BattleParticipant> findByBattleIdAndUserId(Long battleId, Long userId);
    List<BattleParticipant> findByBattleId(Long battleId);
}
