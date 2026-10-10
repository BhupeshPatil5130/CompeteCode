package com.CompetCode.CompetCode.repository;

import com.CompetCode.CompetCode.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findAllByOrderByDisplayOrderAsc();
}
