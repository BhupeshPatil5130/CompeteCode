package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimationStep {
    private int step;
    private String title;
    private String description;
    private String visualType;
    private String visualData;
    private int durationMs;
}
