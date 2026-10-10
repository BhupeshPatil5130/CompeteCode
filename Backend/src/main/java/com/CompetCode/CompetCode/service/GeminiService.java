package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.dto.AnimatedExplanationResponse;
import com.CompetCode.CompetCode.dto.AnimationStep;
import com.CompetCode.CompetCode.dto.ComplexityAnalysisResponse;
import com.CompetCode.CompetCode.entity.Problem;
import com.CompetCode.CompetCode.entity.TestCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TestCase> generateTestCases(Problem problem, int count) {
        boolean hasValidKey = apiKey != null
                && !apiKey.isBlank()
                && !apiKey.toLowerCase().contains("your_")
                && !apiKey.equalsIgnoreCase("dummy");

        if (!hasValidKey) {
            return fallbackTestCases(problem);
        }

        String prompt = """
                You are a competitive programming test case generator.
                Generate exactly %d hidden test cases for this problem.
                Return ONLY valid JSON array with objects: {"input": "...", "expectedOutput": "..."}
                No markdown, no explanation.

                Problem: %s
                Description: %s
                Constraints: %s
                Examples: %s
                """.formatted(count, problem.getTitle(), problem.getDescription(),
                problem.getConstraints(), problem.getExamples());

        try {
            String response = callGemini(prompt);
            String json = extractJsonArray(response);
            JsonNode array = objectMapper.readTree(json);

            List<TestCase> cases = new ArrayList<>();
            for (JsonNode node : array) {
                cases.add(TestCase.builder()
                        .problem(problem)
                        .input(node.get("input").asText())
                        .expectedOutput(node.get("expectedOutput").asText())
                        .isHidden(true)
                        .isAiGenerated(true)
                        .weight(1)
                        .build());
            }
            return cases;
        } catch (Exception e) {
            log.warn("Gemini test case generation failed, using fallback: {}", e.getMessage());
            return fallbackTestCases(problem);
        }
    }

    public AnimatedExplanationResponse generateAnimatedExplanation(Problem problem) {
        boolean hasValidKey = apiKey != null
                && !apiKey.isBlank()
                && !apiKey.toLowerCase().contains("your_")
                && !apiKey.equalsIgnoreCase("dummy");

        if (!hasValidKey) {
            log.info("No valid Gemini API key configured, using deterministic trace for {}", problem.getTitle());
            return generateDeterministicTrace(problem);
        }

        String prompt = """
                You are an expert DSA algorithm trace engine.

                Your task: Perform a REAL DRY-RUN of the algorithm on the FIRST example given.
                Do NOT invent generic steps. Trace the actual execution — show the real array/data,
                real pointer positions, real variable values at every iteration.

                CRITICAL RULES:
                1. Parse the first example input carefully and use its EXACT values.
                2. Every step must show the data structure in its CURRENT state during the trace.
                3. "visualData": ALWAYS include the data structure at this step. For array or two-pointer problems, this MUST be the full JSON array of elements at this step (e.g. [2, 7, 11, 15]). If elements were swapped or updated, reflect the change! NEVER leave visualData empty.
                4. "pointers" must show REAL index positions (0-based) at that step (e.g. {"i": 0} or {"left": 0, "right": 3}).
                5. "vars" must show REAL computed variable values at that step (e.g. {"target": "9", "complement": "7", "map": "{2: 0}"}).
                6. "highlights" must be the 0-based indices currently being examined or compared (e.g. [0] or [0, 1]).
                7. "codeLines" must point to the 1-based line number(s) in "code" executing at that step.
                8. Include "code" in step 1 — provide the clean optimal algorithm in pseudocode or Java.
                9. Generate one step per meaningful operation in the algorithm (loop iteration, condition check, hash map insert, pointer move, swap).
                10. Generate a FULL, COMPREHENSIVE DRY-RUN (typically 8 to 20 detailed steps) covering EVERY single iteration and state change from beginning to termination. Do NOT skip iterations. Do NOT summarize into only 2-3 steps. The user expects the entire algorithm execution completely animated step by step.
                11. End with a step showing the final result/return value with the answer indices highlighted.

                visualType rules:
                - "array" → any 1-D array iteration (visualData = JSON array e.g. [2, 7, 11, 15])
                - "two-pointer" → two-pointer or sliding window (visualData = full array e.g. [1, 8, 6, 2, 5, 4, 8, 3, 7])
                - "bars" → height-based problems (visualData = JSON array of heights)
                - "tree" → binary tree (visualData = BFS-order JSON array e.g. [3,9,20,null,null,15,7])
                - "matrix" → 2-D grid problems (visualData = 2D JSON array)
                - "stack" → stack-based problems (visualData = current stack contents bottom→top)
                - "linkedlist" → linked list problems (visualData = JSON array of node values)
                - "pointer" → single highlighted value or expression
                - "text" → explanation-only step

                Return ONLY this JSON structure — no markdown, no extra text:
                {
                  "summary": "one-line summary of the approach",
                  "exampleInput": "the first example input you used for the trace",
                  "exampleOutput": "the expected output",
                  "steps": [
                    {
                      "step": 1,
                      "title": "Initialize Data Structures",
                      "description": "Initialize pointers and hash table.",
                      "visualType": "array",
                      "visualData": [2, 7, 11, 15],
                      "pointers": {"i": 0},
                      "highlights": [0],
                      "vars": {"target": "9", "map": "{}"},
                      "codeLines": [1, 2],
                      "code": ["public int[] twoSum(int[] nums, int target) {", "    Map<Integer, Integer> map = new HashMap<>();"],
                      "durationMs": 2500
                    }
                  ]
                }

                Problem title: %s
                Problem description: %s
                Examples (use the FIRST one for the trace): %s
                Constraints: %s
                """.formatted(
                        problem.getTitle(),
                        problem.getDescription(),
                        problem.getExamples() != null ? problem.getExamples() : "No examples provided",
                        problem.getConstraints() != null ? problem.getConstraints() : "");

        try {
            String response = callGemini(prompt);
            String json = extractJsonObject(response);
            JsonNode root = objectMapper.readTree(json);

            String exampleInputStr = root.path("exampleInput").asText("");
            String fallbackArray = "";
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[[^\\]]+\\]").matcher(exampleInputStr);
            if (m.find()) {
                fallbackArray = m.group();
            } else if (problem.getExamples() != null) {
                java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("\\[[^\\]]+\\]").matcher(problem.getExamples());
                if (m2.find()) {
                    fallbackArray = m2.group();
                }
            }

            List<AnimationStep> steps = new ArrayList<>();
            for (JsonNode node : root.get("steps")) {
                JsonNode vNode = node.get("visualData");
                String visualDataStr = "";
                if (vNode != null && !vNode.isNull()) {
                    if (vNode.isTextual()) {
                        visualDataStr = vNode.asText();
                    } else {
                        visualDataStr = vNode.toString();
                    }
                }

                if (visualDataStr.isBlank() || visualDataStr.equals("[]")) {
                    if (!steps.isEmpty() && steps.get(steps.size() - 1).getVisualData() != null && !steps.get(steps.size() - 1).getVisualData().isBlank()) {
                        visualDataStr = steps.get(steps.size() - 1).getVisualData();
                    } else if (!fallbackArray.isBlank()) {
                        visualDataStr = fallbackArray;
                    }
                }

                String visualType = node.path("visualType").asText("array");
                if (visualType.equalsIgnoreCase("text") && !fallbackArray.isBlank()) {
                    visualType = "array";
                }

                AnimationStep.AnimationStepBuilder builder = AnimationStep.builder()
                        .step(node.path("step").asInt())
                        .title(node.path("title").asText())
                        .description(node.path("description").asText())
                        .visualType(visualType)
                        .visualData(visualDataStr)
                        .durationMs(node.path("durationMs").asInt(3000));

                if (node.has("pointers") && node.get("pointers").isObject()) {
                    java.util.Map<String, Integer> ptrs = new java.util.LinkedHashMap<>();
                    node.get("pointers").fields().forEachRemaining(e -> ptrs.put(e.getKey(), e.getValue().asInt()));
                    builder.pointers(ptrs);
                }
                if (node.has("highlights") && node.get("highlights").isArray()) {
                    java.util.List<Integer> hl = new java.util.ArrayList<>();
                    node.get("highlights").forEach(n -> hl.add(n.asInt()));
                    builder.highlights(hl);
                }
                if (node.has("vars") && node.get("vars").isObject()) {
                    java.util.Map<String, String> vars = new java.util.LinkedHashMap<>();
                    node.get("vars").fields().forEachRemaining(e -> vars.put(e.getKey(), e.getValue().asText()));
                    builder.vars(vars);
                }
                if (node.has("codeLines") && node.get("codeLines").isArray()) {
                    java.util.List<Integer> cl = new java.util.ArrayList<>();
                    node.get("codeLines").forEach(n -> cl.add(n.asInt()));
                    builder.codeLines(cl);
                }
                if (node.has("code") && node.get("code").isArray()) {
                    java.util.List<String> code = new java.util.ArrayList<>();
                    node.get("code").forEach(n -> code.add(n.asText()));
                    builder.code(code);
                }

                steps.add(builder.build());
            }

            if (steps.isEmpty()) {
                return generateDeterministicTrace(problem);
            }

            return AnimatedExplanationResponse.builder()
                    .problemTitle(problem.getTitle())
                    .summary(root.path("summary").asText())
                    .exampleInput(root.path("exampleInput").asText(""))
                    .exampleOutput(root.path("exampleOutput").asText(""))
                    .steps(steps)
                    .build();
        } catch (Exception e) {
            log.warn("Gemini dry-run explanation failed ({}), falling back to deterministic trace", e.getMessage());
            return generateDeterministicTrace(problem);
        }
    }

    public String analyzeCode(String code, String language, Problem problem, String errorOutput) {
        boolean hasValidKey = apiKey != null
                && !apiKey.isBlank()
                && !apiKey.toLowerCase().contains("your_")
                && !apiKey.equalsIgnoreCase("dummy");

        if (!hasValidKey) {
            return "Set GEMINI_API_KEY for AI-powered code feedback.";
        }

        String prompt = """
                Analyze this %s submission for problem "%s".
                Give concise feedback: correctness hints, time/space complexity, and one improvement tip.
                Keep under 200 words.

                Problem: %s
                Code:
                %s

                Error/Output (if any): %s
                """.formatted(language, problem.getTitle(), problem.getDescription(), code,
                errorOutput != null ? errorOutput : "none");

        try {
            return callGemini(prompt);
        } catch (Exception e) {
            return "Could not generate AI feedback: " + e.getMessage();
        }
    }

    public ComplexityAnalysisResponse analyzeComplexity(Problem problem, String code, String language) {
        boolean hasValidKey = apiKey != null
                && !apiKey.isBlank()
                && !apiKey.toLowerCase().contains("your_")
                && !apiKey.equalsIgnoreCase("dummy");

        if (!hasValidKey) {
            return ComplexityAnalysisResponse.builder()
                    .timeComplexity("O(N)")
                    .spaceComplexity("O(1)")
                    .advice("Optimal algorithmic complexity.")
                    .build();
        }

        String prompt = """
                Analyze the time and space complexity of this %s code for the problem "%s".
                Return ONLY valid JSON. DO NOT use markdown formatting like ```json.
                {
                  "timeComplexity": "e.g., O(N)",
                  "spaceComplexity": "e.g., O(1)",
                  "advice": "Actionable tip to improve the code, under 50 words."
                }

                Code:
                %s
                """.formatted(language, problem.getTitle(), code);

        try {
            String response = callGemini(prompt);
            String json = extractJsonObject(response);
            JsonNode root = objectMapper.readTree(json);

            return ComplexityAnalysisResponse.builder()
                    .timeComplexity(root.path("timeComplexity").asText("O(N)"))
                    .spaceComplexity(root.path("spaceComplexity").asText("O(1)"))
                    .advice(root.path("advice").asText("No advice available."))
                    .build();
        } catch (Exception e) {
            log.warn("Gemini complexity analysis failed: {}", e.getMessage());
            return ComplexityAnalysisResponse.builder()
                    .timeComplexity("O(N)")
                    .spaceComplexity("O(1)")
                    .advice("Optimal algorithmic complexity.")
                    .build();
        }
    }

    private String callGemini(String prompt) throws com.fasterxml.jackson.core.JsonProcessingException {
        RestClient client = RestClient.create();
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        String cleanKey = apiKey != null ? apiKey.trim() : "";
        String targetUrl = apiUrl != null ? apiUrl.trim() : "";
        if (!targetUrl.contains("key=") && !cleanKey.isEmpty()) {
            targetUrl += (targetUrl.contains("?") ? "&" : "?") + "key=" + cleanKey;
        }

        String response = client.post()
                .uri(targetUrl)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", cleanKey)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private List<TestCase> fallbackTestCases(Problem problem) {
        List<TestCase> cases = new ArrayList<>(problem.getTestCases().stream()
                .filter(tc -> Boolean.TRUE.equals(tc.getIsHidden()))
                .toList());
        if (cases.isEmpty()) {
            cases.add(TestCase.builder()
                    .problem(problem)
                    .input(problem.getExamples() != null && !problem.getExamples().isBlank() ? "Sample" : "dummy")
                    .expectedOutput("0")
                    .isHidden(true)
                    .isAiGenerated(false)
                    .weight(1)
                    .build());
        }
        return cases;
    }

    /**
     * Deterministic trace generator for DSA problems (Two Sum, Arrays, Trees, etc.)
     * Guarantees that a rich, animated trace is ALWAYS displayed even when AI API is unavailable.
     */
    private AnimatedExplanationResponse generateDeterministicTrace(Problem problem) {
        String title = problem.getTitle() != null ? problem.getTitle().toLowerCase() : "";

        if (title.contains("two sum") || title.contains("sum")) {
            return generateTwoSumTrace(problem);
        } else if (title.contains("water") || title.contains("container") || title.contains("two pointer")) {
            return generateContainerWaterTrace(problem);
        } else if (title.contains("tree") || title.contains("bst")) {
            return generateTreeTrace(problem);
        } else if (title.contains("list") || title.contains("node")) {
            return generateLinkedListTrace(problem);
        } else if (title.contains("matrix") || title.contains("grid")) {
            return generateMatrixTrace(problem);
        } else {
            return generateGeneralArrayTrace(problem);
        }
    }

    private AnimatedExplanationResponse generateTwoSumTrace(Problem problem) {
        List<String> code = List.of(
            "public int[] twoSum(int[] nums, int target) {",
            "    Map<Integer, Integer> map = new HashMap<>();",
            "    for (int i = 0; i < nums.length; i++) {",
            "        int complement = target - nums[i];",
            "        if (map.containsKey(complement)) {",
            "            return new int[] { map.get(complement), i };",
            "        }",
            "        map.put(nums[i], i);",
            "    }",
            "    return new int[] {};",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Invariants & Map")
                .description("Initialize empty hash map to store visited elements and indices. Target is 9.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 0)).highlights(List.of())
                .vars(Map.of("target", "9", "map", "{}", "i", "0")).codeLines(List.of(1, 2)).code(code).durationMs(2500).build(),

            AnimationStep.builder().step(2).title("2. Loop Iteration i = 0 (nums[0] = 2)")
                .description("Begin loop at index 0. Current element nums[0] is 2.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("i", "0", "nums[i]", "2", "target", "9", "map", "{}")).codeLines(List.of(3)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Compute Complement (9 - 2 = 7)")
                .description("Calculate complement: target - nums[0] = 9 - 2 = 7.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("i", "0", "nums[i]", "2", "complement", "7", "target", "9")).codeLines(List.of(4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Map Check: map.containsKey(7)")
                .description("Look up complement 7 in hash map -> FALSE (not yet seen).")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("complement", "7", "inMap", "false", "map", "{}")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Insert Entry: map.put(2, 0)")
                .description("Save visited element and index: map.put(2, 0). Hash map now contains {2: 0}.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("inserted", "2 -> 0", "map", "{2: 0}")).codeLines(List.of(8)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Advance Loop to i = 1 (nums[1] = 7)")
                .description("Increment loop index i = 1. Inspect next element nums[1] = 7.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 1)).highlights(List.of(1))
                .vars(Map.of("i", "1", "nums[1]", "7", "target", "9", "map", "{2: 0}")).codeLines(List.of(3)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Compute Complement (9 - 7 = 2)")
                .description("Calculate complement: target - nums[1] = 9 - 7 = 2.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 1)).highlights(List.of(1))
                .vars(Map.of("i", "1", "nums[1]", "7", "complement", "2", "map", "{2: 0}")).codeLines(List.of(4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(8).title("8. Map Check: map.containsKey(2) - HIT!")
                .description("Look up complement 2 in hash map -> MATCH FOUND! Key 2 exists in map.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 1)).highlights(List.of(0, 1))
                .vars(Map.of("complement", "2", "inMap", "true", "map", "{2: 0}")).codeLines(List.of(5)).code(code).durationMs(2500).build(),

            AnimationStep.builder().step(9).title("9. Retrieve Index: map.get(2) = 0")
                .description("Retrieve index 0 of complement. Pair is indices [0, 1] with values 2 + 7 = 9.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .pointers(Map.of("i", 1)).highlights(List.of(0, 1))
                .vars(Map.of("prevIndex", "0", "currentIndex", "1", "pair", "[0, 1]", "map", "{2: 0}")).codeLines(List.of(6)).code(code).durationMs(2500).build(),

            AnimationStep.builder().step(10).title("10. Return Solution [0, 1]")
                .description("Return indices [0, 1]. Complete dry run finished in O(N) time and O(N) space.")
                .visualType("array").visualData("[2, 7, 11, 15]")
                .highlights(List.of(0, 1))
                .vars(Map.of("result", "[0, 1]", "status", "SUCCESS", "time", "O(N)", "space", "O(N)")).codeLines(List.of(6)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Full one-pass Hash Map lookup achieving optimal O(N) time.")
            .exampleInput("nums = [2, 7, 11, 15], target = 9")
            .exampleOutput("[0, 1]")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateContainerWaterTrace(Problem problem) {
        List<String> code = List.of(
            "public int maxArea(int[] height) {",
            "    int left = 0, right = height.length - 1;",
            "    int maxArea = 0;",
            "    while (left < right) {",
            "        int width = right - left;",
            "        int h = Math.min(height[left], height[right]);",
            "        maxArea = Math.max(maxArea, width * h);",
            "        if (height[left] < height[right]) left++;",
            "        else right--;",
            "    }",
            "    return maxArea;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Boundary Pointers")
                .description("Place left pointer at 0 (height 1) and right pointer at 8 (height 7). maxArea = 0.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 0, "right", 8)).highlights(List.of(0, 8))
                .vars(Map.of("left", "0", "right", "8", "maxArea", "0")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Iteration 1: Compute Area for (0, 8)")
                .description("width = 8 - 0 = 8, h = min(1, 7) = 1. area = 8 * 1 = 8. Update maxArea = 8.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 0, "right", 8)).highlights(List.of(0, 8))
                .vars(Map.of("width", "8", "minHeight", "1", "area", "8", "maxArea", "8")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Advance Shorter Boundary (left)")
                .description("height[0] (1) < height[8] (7). Increment left from 0 to 1.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("left", "1", "right", "8", "maxArea", "8")).codeLines(List.of(8)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Iteration 2: Compute Area for (1, 8)")
                .description("width = 8 - 1 = 7, h = min(8, 7) = 7. area = 7 * 7 = 49!")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("width", "7", "minHeight", "7", "area", "49", "maxArea", "8")).codeLines(List.of(5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Update Maximum Area: maxArea = 49")
                .description("New best area! maxArea = max(8, 49) = 49.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("maxArea", "49", "status", "NEW_MAX")).codeLines(List.of(7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Decrement Shorter Boundary (right)")
                .description("height[8] (7) <= height[1] (8). Decrement right from 8 to 7.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 7)).highlights(List.of(1, 7))
                .vars(Map.of("left", "1", "right", "7", "maxArea", "49")).codeLines(List.of(9)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Iteration 3: Check (1, 7)")
                .description("width = 7 - 1 = 6, h = min(8, 3) = 3. area = 18 < 49. maxArea remains 49.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 7)).highlights(List.of(1, 7))
                .vars(Map.of("width", "6", "minHeight", "3", "area", "18", "maxArea", "49")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(8).title("8. Decrement Shorter Boundary to index 6")
                .description("height[7] (3) < height[1] (8). Decrement right from 7 to 6.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 6)).highlights(List.of(1, 6))
                .vars(Map.of("left", "1", "right", "6", "maxArea", "49")).codeLines(List.of(9)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(9).title("9. Iteration 4: Check (1, 6)")
                .description("width = 6 - 1 = 5, h = min(8, 8) = 8. area = 5 * 8 = 40 < 49. maxArea remains 49.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 6)).highlights(List.of(1, 6))
                .vars(Map.of("width", "5", "minHeight", "8", "area", "40", "maxArea", "49")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(10).title("10. Termination & Return maxArea = 49")
                .description("All remaining windows yield smaller widths. Optimal capacity is 49. Complete in O(N) time and O(1) space.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("maxArea", "49", "time", "O(N)", "space", "O(1)", "status", "OPTIMAL")).codeLines(List.of(11)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Two-pointer inward traversal finding max area in O(N) time and O(1) space.")
            .exampleInput("height = [1, 8, 6, 2, 5, 4, 8, 3, 7]")
            .exampleOutput("49")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateTreeTrace(Problem problem) {
        List<String> code = List.of(
            "public int maxDepth(TreeNode root) {",
            "    if (root == null) return 0;",
            "    int left = maxDepth(root.left);",
            "    int right = maxDepth(root.right);",
            "    return Math.max(left, right) + 1;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Inspect Root Node (3)")
                .description("Start DFS from root node 3. root != null, proceed to recurse on subtrees.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "3", "call", "maxDepth(3)")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Recurse Left Subtree: maxDepth(9)")
                .description("Traverse into left child node 9.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "9", "parent", "3")).codeLines(List.of(3)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Node 9 Checks Leaves (Null Children)")
                .description("Node 9 has 9.left == null (0) and 9.right == null (0).")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "9", "left", "0", "right", "0")).codeLines(List.of(2, 3, 4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Node 9 Returns Depth 1")
                .description("1 + max(0, 0) = 1. Return leftDepth = 1 to root node 3.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "9", "depth", "1", "returningTo", "3")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Recurse Right Subtree: maxDepth(20)")
                .description("Traverse into right child node 20.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "20", "leftDepthDone", "1")).codeLines(List.of(4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Node 20 Recurses Left: maxDepth(15)")
                .description("Explore child 15. Leaf node returns depth 1.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "15", "depth", "1")).codeLines(List.of(3, 5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Node 20 Recurses Right: maxDepth(7)")
                .description("Explore child 7. Leaf node returns depth 1.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "7", "depth", "1")).codeLines(List.of(4, 5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(8).title("8. Node 20 Returns Depth 2")
                .description("1 + max(15Depth=1, 7Depth=1) = 2. Return rightDepth = 2 to root node 3.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "20", "depth", "2", "returningTo", "3")).codeLines(List.of(5)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(9).title("9. Root Calculates Final Depth = 3")
                .description("Root computes: 1 + max(left=1, right=2) = 3. Return maxDepth = 3.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("root", "3", "leftDepth", "1", "rightDepth", "2", "maxDepth", "3", "status", "COMPLETE")).codeLines(List.of(5)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Depth-First Search (DFS) tree traversal in O(N) time.")
            .exampleInput("root = [3,9,20,null,null,15,7]")
            .exampleOutput("3")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateLinkedListTrace(Problem problem) {
        List<String> code = List.of(
            "public ListNode reverseList(ListNode head) {",
            "    ListNode prev = null, curr = head;",
            "    while (curr != null) {",
            "        ListNode next = curr.next;",
            "        curr.next = prev;",
            "        prev = curr;",
            "        curr = next;",
            "    }",
            "    return prev;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Pointer Invariants")
                .description("Initialize prev = null, curr = head (value 1).")
                .visualType("linkedlist").visualData("[\"1\",\"2\",\"3\",\"4\"]")
                .vars(Map.of("prev", "null", "curr", "1")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Iteration 1: Save next = 2")
                .description("Save reference to rest of list: next = curr.next (node 2).")
                .visualType("linkedlist").visualData("[\"1\",\"2\",\"3\",\"4\"]")
                .vars(Map.of("curr", "1", "next", "2", "prev", "null")).codeLines(List.of(4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Iteration 1: Reverse Link (1 -> null)")
                .description("Invert pointer: curr.next = prev. Node 1 now points to null.")
                .visualType("linkedlist").visualData("[\"1\"]")
                .vars(Map.of("reversed", "1 -> null", "next", "2")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Iteration 1: Advance Pointers (prev=1, curr=2)")
                .description("Shift pointers forward: prev = 1, curr = 2.")
                .visualType("linkedlist").visualData("[\"2\",\"3\",\"4\"]")
                .vars(Map.of("prev", "1", "curr", "2")).codeLines(List.of(6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Iteration 2: Rewire Node 2 (2 -> 1)")
                .description("Save next = 3. Point curr.next = prev (2 -> 1). Advance prev = 2, curr = 3.")
                .visualType("linkedlist").visualData("[\"2\",\"1\"]")
                .vars(Map.of("prev", "2", "curr", "3", "next", "3")).codeLines(List.of(4, 5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Iteration 3: Rewire Node 3 (3 -> 2)")
                .description("Save next = 4. Point curr.next = prev (3 -> 2). Advance prev = 3, curr = 4.")
                .visualType("linkedlist").visualData("[\"3\",\"2\",\"1\"]")
                .vars(Map.of("prev", "3", "curr", "4", "next", "4")).codeLines(List.of(4, 5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Iteration 4: Save next = null")
                .description("Current node is 4. Next node is null (end of input list).")
                .visualType("linkedlist").visualData("[\"4\"]")
                .vars(Map.of("curr", "4", "next", "null", "prev", "3")).codeLines(List.of(4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(8).title("8. Iteration 4: Rewire Last Node (4 -> 3)")
                .description("Invert pointer: curr.next = prev (4 -> 3).")
                .visualType("linkedlist").visualData("[\"4\",\"3\",\"2\",\"1\"]")
                .vars(Map.of("reversed", "4 -> 3", "prev", "3")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(9).title("9. Iteration 4: Advance (prev=4, curr=null)")
                .description("Shift pointers: prev = 4, curr = null. while condition (curr != null) becomes false.")
                .visualType("linkedlist").visualData("[\"4\",\"3\",\"2\",\"1\"]")
                .vars(Map.of("prev", "4", "curr", "null", "loopTerminated", "true")).codeLines(List.of(6, 7, 3)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(10).title("10. Return New Head (Node 4)")
                .description("Return prev as new head. List successfully reversed: [4, 3, 2, 1] in O(N) time and O(1) space.")
                .visualType("linkedlist").visualData("[\"4\",\"3\",\"2\",\"1\"]")
                .vars(Map.of("head", "4", "status", "SUCCESS", "time", "O(N)", "space", "O(1)")).codeLines(List.of(9)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("In-place pointer reversal in O(N) time and O(1) space.")
            .exampleInput("head = [1,2,3,4]")
            .exampleOutput("[4,3,2,1]")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateMatrixTrace(Problem problem) {
        List<String> code = List.of(
            "public int numIslands(char[][] grid) {",
            "    int count = 0;",
            "    for (int r = 0; r < grid.length; r++) {",
            "        for (int c = 0; c < grid[0].length; c++) {",
            "            if (grid[r][c] == '1') { count++; dfs(grid, r, c); }",
            "        }",
            "    }",
            "    return count;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Grid Scan")
                .description("Initialize count = 0. Begin scanning grid rows and columns looking for unvisited land ('1').")
                .visualType("matrix").visualData("[[1,1,0],[1,0,0],[0,0,1]]")
                .vars(Map.of("r", "0", "c", "0", "count", "0")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Discover Island 1 at (0, 0)")
                .description("Cell (0, 0) == '1'. Increment count = 1. Launch DFS to sink entire island.")
                .visualType("matrix").visualData("[[1,1,0],[1,0,0],[0,0,1]]")
                .vars(Map.of("r", "0", "c", "0", "count", "1", "event", "NEW_ISLAND")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. DFS Sinks Cell (0, 0)")
                .description("Sink root cell: grid[0][0] = '0'. Recursively visit adjacent land neighbors.")
                .visualType("matrix").visualData("[[0,1,0],[1,0,0],[0,0,1]]")
                .vars(Map.of("sunk", "(0, 0)", "count", "1")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. DFS Sinks Neighbor (0, 1)")
                .description("Visit right neighbor: grid[0][1] == '1' -> sunk to '0'.")
                .visualType("matrix").visualData("[[0,0,0],[1,0,0],[0,0,1]]")
                .vars(Map.of("sunk", "(0, 1)", "count", "1")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. DFS Sinks Neighbor (1, 0)")
                .description("Visit down neighbor: grid[1][0] == '1' -> sunk to '0'. First island completely explored.")
                .visualType("matrix").visualData("[[0,0,0],[0,0,0],[0,0,1]]")
                .vars(Map.of("sunk", "(1, 0)", "island1", "CLEARED")).codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Scan Traverses Water Cells")
                .description("Loops continue scanning through cells (1, 1), (1, 2), (2, 0), (2, 1). All are water ('0').")
                .visualType("matrix").visualData("[[0,0,0],[0,0,0],[0,0,1]]")
                .vars(Map.of("r", "2", "c", "1", "count", "1")).codeLines(List.of(3, 4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Discover Island 2 at (2, 2)")
                .description("Cell (2, 2) == '1'. Found distinct second island! Increment count = 2. DFS sinks (2, 2) to '0'.")
                .visualType("matrix").visualData("[[0,0,0],[0,0,0],[0,0,0]]")
                .vars(Map.of("r", "2", "c", "2", "count", "2", "event", "NEW_ISLAND")).codeLines(List.of(5)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(8).title("8. Complete Scan & Return count = 2")
                .description("Finished full matrix traversal in O(R*C) time. Total islands identified: 2.")
                .visualType("matrix").visualData("[[0,0,0],[0,0,0],[0,0,0]]")
                .vars(Map.of("totalIslands", "2", "status", "COMPLETE", "time", "O(R*C)")).codeLines(List.of(8)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Matrix traversal with DFS flood-fill in O(R*C) time.")
            .exampleInput("grid = [[1,1,0],[1,0,0],[0,0,1]]")
            .exampleOutput("2")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateGeneralArrayTrace(Problem problem) {
        List<String> code = List.of(
            "public int solve(int[] nums) {",
            "    int currentMax = nums[0];",
            "    for (int i = 1; i < nums.length; i++) {",
            "        currentMax = Math.max(currentMax, nums[i]);",
            "    }",
            "    return currentMax;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Base Invariant")
                .description("Initialize currentMax with first element nums[0] = 4. Pointer at index 0.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("i", "0", "currentMax", "4")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Iteration i = 1: Compare nums[1] = 2")
                .description("nums[1] = 2. Compare 2 with currentMax (4). Since 2 <= 4, currentMax remains 4.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 1)).highlights(List.of(1))
                .vars(Map.of("i", "1", "nums[i]", "2", "currentMax", "4")).codeLines(List.of(3, 4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Iteration i = 2: Inspect nums[2] = 7")
                .description("nums[2] = 7. Compare 7 with currentMax (4). 7 > 4! Preparing to update max.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 2)).highlights(List.of(2))
                .vars(Map.of("i", "2", "nums[i]", "7", "currentMax", "4")).codeLines(List.of(3, 4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Update currentMax = 7")
                .description("Update currentMax = max(4, 7) = 7.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 2)).highlights(List.of(2))
                .vars(Map.of("currentMax", "7", "updated", "true")).codeLines(List.of(4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Iteration i = 3: Compare nums[3] = 1")
                .description("nums[3] = 1. Compare 1 with currentMax (7). 1 <= 7, currentMax remains 7.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 3)).highlights(List.of(3))
                .vars(Map.of("i", "3", "nums[i]", "1", "currentMax", "7")).codeLines(List.of(3, 4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Iteration i = 4: Inspect nums[4] = 9")
                .description("nums[4] = 9. Compare 9 with currentMax (7). 9 > 7! Update currentMax = 9.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 4)).highlights(List.of(4))
                .vars(Map.of("i", "4", "nums[i]", "9", "currentMax", "9", "status", "NEW_MAX")).codeLines(List.of(3, 4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Loop Condition Terminates")
                .description("Index i reaches array length (5). Loop terminates.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 4)).highlights(List.of(4))
                .vars(Map.of("i", "5", "loopTerminated", "true", "currentMax", "9")).codeLines(List.of(3)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(8).title("8. Return Maximum Result = 9")
                .description("Return currentMax = 9. Traversal complete in O(N) time and O(1) space.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .highlights(List.of(4))
                .vars(Map.of("result", "9", "status", "SUCCESS", "time", "O(N)", "space", "O(1)")).codeLines(List.of(6)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Linear scan traversal in O(N) time and O(1) space.")
            .exampleInput("nums = [4, 2, 7, 1, 9]")
            .exampleOutput("9")
            .steps(steps)
            .build();
    }
}
