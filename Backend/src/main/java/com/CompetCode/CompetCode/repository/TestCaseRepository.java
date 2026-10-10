package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProblemId(Long problemId);
    List<TestCase> findByProblemIdAndIsHiddenFalse(Long problemId);
}
