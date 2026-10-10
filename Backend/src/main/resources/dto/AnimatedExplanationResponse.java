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
public class AnimatedExplanationResponse {
    private String problemTitle;
    private String summary;
    private List<AnimationStep> steps;
}
