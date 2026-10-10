package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.Difficulty;
import com.CompetCode.CompetCode.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByTopic(String topic);
    List<Problem> findByDifficulty(Difficulty difficulty);
    List<Problem> findByTitleContainingIgnoreCase(String title);
    boolean existsByTitle(String title);
}
