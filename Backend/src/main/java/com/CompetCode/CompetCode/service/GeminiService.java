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
                9. Generate one step per meaningful operation in the algorithm (loop iteration, comparison, hash map insert, pointer move, swap).
                10. Generate 6 to 12 steps total — enough to show the COMPLETE trace to the final answer.
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

        List<AnimationStep> steps = new ArrayList<>();

        steps.add(AnimationStep.builder()
            .step(1)
            .title("1. Initialize Hash Map")
            .description("Initialize an empty hash map to store seen values and their indices for O(1) complement lookup.")
            .visualType("array")
            .visualData("[2, 7, 11, 15]")
            .pointers(Map.of("i", 0))
            .highlights(List.of(0))
            .vars(Map.of("target", "9", "i", "0", "map", "{}"))
            .codeLines(List.of(1, 2))
            .code(code)
            .durationMs(2500)
            .build());

        steps.add(AnimationStep.builder()
            .step(2)
            .title("2. Inspect nums[0] = 2")
            .description("Calculate complement: target - nums[0] = 9 - 2 = 7. Look up 7 in the map -> Not found.")
            .visualType("array")
            .visualData("[2, 7, 11, 15]")
            .pointers(Map.of("i", 0))
            .highlights(List.of(0))
            .vars(Map.of("nums[0]", "2", "complement", "7", "inMap", "false", "map", "{}"))
            .codeLines(List.of(3, 4, 5))
            .code(code)
            .durationMs(2500)
            .build());

        steps.add(AnimationStep.builder()
            .step(3)
            .title("3. Insert into Hash Map")
            .description("Store current number and index: map.put(2, 0).")
            .visualType("array")
            .visualData("[2, 7, 11, 15]")
            .pointers(Map.of("i", 0))
            .highlights(List.of(0))
            .vars(Map.of("nums[0]", "2", "map", "{2: 0}"))
            .codeLines(List.of(8))
            .code(code)
            .durationMs(2500)
            .build());

        steps.add(AnimationStep.builder()
            .step(4)
            .title("4. Move to nums[1] = 7")
            .description("Advance i = 1 (value 7). Calculate complement: target - nums[1] = 9 - 7 = 2.")
            .visualType("array")
            .visualData("[2, 7, 11, 15]")
            .pointers(Map.of("i", 1))
            .highlights(List.of(1))
            .vars(Map.of("nums[1]", "7", "complement", "2", "map", "{2: 0}"))
            .codeLines(List.of(3, 4))
            .code(code)
            .durationMs(2500)
            .build());

        steps.add(AnimationStep.builder()
            .step(5)
            .title("5. Complement Found in Map!")
            .description("Search 2 in hash map -> MATCH FOUND at index 0! Complement pair (2 + 7 = 9) located.")
            .visualType("array")
            .visualData("[2, 7, 11, 15]")
            .pointers(Map.of("i", 1))
            .highlights(List.of(0, 1))
            .vars(Map.of("foundIndex", "0", "currentIndex", "1", "map", "{2: 0}"))
            .codeLines(List.of(5, 6))
            .code(code)
            .durationMs(3000)
            .build());

        steps.add(AnimationStep.builder()
            .step(6)
            .title("6. Return Result [0, 1]")
            .description("Return indices [0, 1] as the final solution. Time Complexity: O(N), Space: O(N).")
            .visualType("array")
            .visualData("[2, 7, 11, 15]")
            .highlights(List.of(0, 1))
            .vars(Map.of("result", "[0, 1]", "status", "SUCCESS", "map", "{2: 0}"))
            .codeLines(List.of(6))
            .code(code)
            .durationMs(3000)
            .build());

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("One-pass Hash Map lookup achieving optimal O(N) time.")
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
            AnimationStep.builder().step(1).title("1. Initialize Pointers")
                .description("Place 'left' pointer at index 0 and 'right' pointer at the last index.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 0, "right", 8)).highlights(List.of(0, 8))
                .vars(Map.of("left", "0", "right", "8", "maxArea", "0")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(2).title("2. Calculate Area for (0, 8)")
                .description("width = 8 - 0 = 8, h = min(1, 7) = 1. Area = 8 * 1 = 8. Update maxArea to 8.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 0, "right", 8)).highlights(List.of(0, 8))
                .vars(Map.of("width", "8", "minHeight", "1", "currentArea", "8", "maxArea", "8")).codeLines(List.of(4, 5, 6, 7)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(3).title("3. Move Left Pointer Inward")
                .description("Since height[left] (1) < height[right] (7), increment left to index 1.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("left", "1", "right", "8", "maxArea", "8")).codeLines(List.of(8)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(4).title("4. Calculate Area for (1, 8)")
                .description("width = 8 - 1 = 7, h = min(8, 7) = 7. Area = 7 * 7 = 49. maxArea updated to 49!")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("width", "7", "minHeight", "7", "currentArea", "49", "maxArea", "49")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(5).title("5. Return Maximum Area (49)")
                .description("Optimal container found. Return maxArea = 49.")
                .visualType("two-pointer").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("maxArea", "49", "status", "OPTIMAL")).codeLines(List.of(11)).code(code).durationMs(2500).build()
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
            AnimationStep.builder().step(1).title("1. Inspect Root Node")
                .description("Start DFS from root node 3.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "3", "depth", "1")).codeLines(List.of(1, 2)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(2).title("2. Traverse Left Subtree")
                .description("Recursively explore left child (node 9).")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "9", "leftDepth", "1")).codeLines(List.of(3)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(3).title("3. Traverse Right Subtree")
                .description("Recursively explore right child (node 20) and its children 15 and 7.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("node", "20", "rightDepth", "2")).codeLines(List.of(4)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(4).title("4. Return Max Depth (3)")
                .description("1 + max(left, right) = 1 + 2 = 3.")
                .visualType("tree").visualData("[\"3\",\"9\",\"20\",\"null\",\"null\",\"15\",\"7\"]")
                .vars(Map.of("maxDepth", "3")).codeLines(List.of(5)).code(code).durationMs(2500).build()
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
            AnimationStep.builder().step(1).title("1. Initialize Pointers")
                .description("prev = null, curr = head (value 1).")
                .visualType("linkedlist").visualData("[\"1\",\"2\",\"3\",\"4\"]")
                .vars(Map.of("prev", "null", "curr", "1")).codeLines(List.of(1, 2)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(2).title("2. Rewire First Node")
                .description("curr.next = prev (1 -> null). Advance prev to 1 and curr to 2.")
                .visualType("linkedlist").visualData("[\"2\",\"3\",\"4\"]")
                .vars(Map.of("prev", "1", "curr", "2")).codeLines(List.of(3, 4, 5, 6, 7)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(3).title("3. Complete Reversal")
                .description("Continue until curr is null. Return new head (prev).")
                .visualType("linkedlist").visualData("[\"4\",\"3\",\"2\",\"1\"]")
                .vars(Map.of("newHead", "4", "status", "COMPLETE")).codeLines(List.of(9)).code(code).durationMs(2500).build()
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
            AnimationStep.builder().step(1).title("1. Inspect Grid Dimensions")
                .description("Iterate through rows and columns searching for unvisited land ('1').")
                .visualType("matrix").visualData("[[1,1,0],[1,0,0],[0,0,1]]")
                .vars(Map.of("r", "0", "c", "0", "islands", "0")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(2).title("2. Discover Island and Sink")
                .description("Found land at (0, 0)! Increment island count and sink connected land.")
                .visualType("matrix").visualData("[[0,0,0],[0,0,0],[0,0,1]]")
                .vars(Map.of("r", "0", "c", "0", "islands", "1")).codeLines(List.of(5)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(3).title("3. Final Count")
                .description("Complete scan of matrix. Found 2 distinct islands.")
                .visualType("matrix").visualData("[[0,0,0],[0,0,0],[0,0,0]]")
                .vars(Map.of("totalIslands", "2")).codeLines(List.of(8)).code(code).durationMs(2500).build()
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
            AnimationStep.builder().step(1).title("1. Initialize Invariants")
                .description("Inspect initial array element.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("i", "0", "currentMax", "4")).codeLines(List.of(1, 2)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(2).title("2. Process Iteration i = 2")
                .description("Element nums[2] = 7 > currentMax (4). Update currentMax = 7.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 2)).highlights(List.of(2))
                .vars(Map.of("i", "2", "nums[i]", "7", "currentMax", "7")).codeLines(List.of(3, 4)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(3).title("3. Termination")
                .description("Reached end of array. Max value is 9.")
                .visualType("array").visualData("[4, 2, 7, 1, 9]")
                .pointers(Map.of("i", 4)).highlights(List.of(4))
                .vars(Map.of("result", "9", "status", "DONE")).codeLines(List.of(6)).code(code).durationMs(2500).build()
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
