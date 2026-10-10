package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserIdAndRoadmapId(Long userId, Long roadmapId);
    Optional<UserProgress> findByUserIdAndRoadmapIdAndProblemId(Long userId, Long roadmapId, Long problemId);
    long countByUserIdAndRoadmapIdAndCompletedTrue(Long userId, Long roadmapId);
}
