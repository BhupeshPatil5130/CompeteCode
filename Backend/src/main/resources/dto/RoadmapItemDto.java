package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapItemDto {
    private Long problemId;
    private String problemTitle;
    private String difficulty;
    private String sectionTitle;
    private boolean completed;
}
