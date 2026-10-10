package com.CompetCode.CompetCode.dto;

import java.util.List;

public record RoadmapProgressDto(
        Long roadmapId,
        String title,
        int totalProblems,
        int completedProblems,
        double progressPercent,
        List<RoadmapItemDto> items
) {}
