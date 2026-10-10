package com.CompetCode.CompetCode.dto;

import java.util.List;

public record AnimatedExplanationResponse(
        String problemTitle,
        String summary,
        List<AnimationStep> steps
) {}
