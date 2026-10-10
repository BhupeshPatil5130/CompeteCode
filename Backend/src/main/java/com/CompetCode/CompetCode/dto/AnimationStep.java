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
    // Core step metadata
    private int step;
    private String title;
    private String description;

    // Visual configuration
    // Types: array | hashmap | tree | stack | queue | linkedlist | matrix | pointer | text | highlight
    private String visualType;
    private String visualData;
    private int durationMs;

    // Code execution tracking
    private Integer line;
    private List<CodeLine> codeLines;

    // Deterministic state snapshot
    private StepState state;

    // Operations performed at this step
    private List<Operation> operations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StepState {
        private Map<String, Object> variables;
        private List<Object> array;
        private List<Integer> activeIndices;
        private Map<String, Integer> pointers;        // e.g. {"i": 0, "j": 1, "left": 0, "right": 3}
        private Map<String, Object> dataStructures;   // e.g. {"map": {"2": 0}}
        private int[] activeCell;                      // [row, col] for matrix
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Operation {
        private String type;        // initialize | lookup | insert | compare | assign | swap | return
        private String target;      // target variable or structure
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
