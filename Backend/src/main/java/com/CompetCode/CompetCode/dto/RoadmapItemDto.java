package com.CompetCode.CompetCode.dto;

public record RoadmapItemDto(
        Long problemId,
        String problemTitle,
        String difficulty,
        String sectionTitle,
        boolean completed
) {}
