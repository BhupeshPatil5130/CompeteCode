package com.CompetCode.CompetCode.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnimatedExplanationResponse {
    private String problemTitle;
    private String summary;
    private String algorithmName;
    private String algorithmCategory;

    // Ordered animation trace
    private List<AnimationStep> steps;

    // Shared code shown in the code panel (if not overridden per-step)
    private List<AnimationStep.CodeLine> code;

    // Complexity
    private Complexity complexity;

    // Final result
    private Result result;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Complexity {
        private String time;
        private String space;
        private String explanation;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Result {
        private Object value;
        private String explanation;
    }
}
