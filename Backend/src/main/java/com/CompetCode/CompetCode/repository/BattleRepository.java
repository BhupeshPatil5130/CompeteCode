package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.Battle;
import com.CompetCode.CompetCode.entity.BattleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {
    Optional<Battle> findByRoomCode(String roomCode);
    List<Battle> findByStatus(BattleStatus status);
}
