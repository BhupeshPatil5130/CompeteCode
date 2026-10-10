package com.CompetCode.CompetCode.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    // Ordered execution trace steps
    private List<AnimationStep> steps;

    // Source code associated with algorithm
    private List<AnimationStep.CodeLine> code;

    // Time and Space complexity
    private Complexity complexity;

    // Final result
    private Result result;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Complexity {
        private String time;
        private String space;
        private String explanation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Result {
        private Object value;
        private String explanation;
    }
}
