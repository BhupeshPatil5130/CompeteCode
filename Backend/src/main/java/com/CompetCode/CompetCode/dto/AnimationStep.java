package com.CompetCode.CompetCode.dto;

public record AnimationStep(
        int step,
        String title,
        String description,
        String visualType,
        String visualData,
        int durationMs
) {}
