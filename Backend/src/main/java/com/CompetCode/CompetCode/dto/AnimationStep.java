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
public class AnimationStep {
    private int step;
    private String title;
    private String description;
    private String visualType;
    private String visualData;
    private int durationMs;

    // Real execution dry-run fields
    private Map<String, Integer> pointers;       // e.g. {"i": 0, "left": 0, "right": 3}
    private List<Integer> highlights;            // e.g. [0, 1]
    private Map<String, String> vars;            // e.g. {"target": "9", "map": "{2: 0}"}
    private List<Integer> codeLines;             // executing 1-based line number(s)
    private List<String> code;                   // code lines if provided in step

    private Integer line;                        // optional single line
    private StepState state;                     // optional state snapshot
    private List<Operation> operations;          // optional operations

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StepState {
        private Map<String, Object> variables;
        private List<Object> array;
        private List<Integer> activeIndices;
        private Map<String, Integer> pointers;
        private Map<String, Object> dataStructures;
        private int[] activeCell;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Operation {
        private String type;
        private String target;
        private Object value;
        private Object prevValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CodeLine {
        private int line;
        private String text;
    }
}
