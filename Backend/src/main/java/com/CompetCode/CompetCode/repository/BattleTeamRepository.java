package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.BattleTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BattleTeamRepository extends JpaRepository<BattleTeam, Long> {
    List<BattleTeam> findByBattleIdOrderByScoreDesc(Long battleId);
    List<BattleTeam> findByBattleId(Long battleId);
}
