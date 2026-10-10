package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.Submission;
import com.CompetCode.CompetCode.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<Submission> findByProblemIdOrderBySubmittedAtDesc(Long problemId);
    List<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(Long userId, Long problemId);
    boolean existsByUserIdAndProblemIdAndStatus(Long userId, Long problemId, SubmissionStatus status);
    List<Submission> findByUserIdAndStatus(Long userId, SubmissionStatus status);
    long countByProblemId(Long problemId);
    long countByProblemIdAndStatus(Long problemId, SubmissionStatus status);
}

