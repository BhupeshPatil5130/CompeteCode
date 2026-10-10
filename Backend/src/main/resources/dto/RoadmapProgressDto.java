package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapProgressDto {
    private Long roadmapId;
    private String title;
    private int totalProblems;
    private int completedProblems;
    private double progressPercent;
    private List<RoadmapItemDto> items;
}
