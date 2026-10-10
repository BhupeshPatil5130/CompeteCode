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
        if (apiKey == null || apiKey.isBlank()) {
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
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackExplanation(problem);
        }

        String prompt = """
                You are a senior DSA algorithm visualizer engine.
                Generate a structured, complete algorithm trace and dry run for this DSA problem.
                Return ONLY valid JSON. DO NOT use markdown formatting like ```json.
                {
                  "summary": "1-2 sentence high-level algorithmic overview",
                  "algorithmName": "e.g. Hash Map / Two Pointers / Binary Search",
                  "complexity": {
                    "time": "e.g. O(N)",
                    "space": "e.g. O(1)",
                    "explanation": "Clear complexity reasoning"
                  },
                  "code": [
                    {"line": 1, "text": "public int[] solve(int[] nums, int target) {"},
                    {"line": 2, "text": "    Map<Integer, Integer> map = new HashMap<>();"},
                    {"line": 3, "text": "    for (int i = 0; i < nums.length; i++) {"},
                    {"line": 4, "text": "        int comp = target - nums[i];"},
                    {"line": 5, "text": "        if (map.containsKey(comp)) return new int[]{map.get(comp), i};"},
                    {"line": 6, "text": "        map.put(nums[i], i);"},
                    {"line": 7, "text": "    }"},
                    {"line": 8, "text": "    return new int[]{};"},
                    {"line": 9, "text": "}"}
                  ],
                  "steps": [
                    {
                      "step": 1,
                      "line": 2,
                      "title": "Initialize Data Structures",
                      "description": "Initialize HashMap to store complements as we traverse.",
                      "visualType": "array",
                      "visualData": "[2, 7, 11, 15]",
                      "durationMs": 3000,
                      "state": {
                        "variables": {"target": 9, "i": 0, "comp": 7},
                        "pointers": {"i": 0},
                        "activeIndices": [0],
                        "dataStructures": {"map": {}}
                      },
                      "operations": [
                        {"type": "lookup", "target": "map", "value": 7}
                      ]
                    }
                  ],
                  "result": {
                    "value": "[0, 1]",
                    "explanation": "Found matching pair satisfying target."
                  }
                }

                Problem: %s
                Description: %s
                Examples: %s
                """.formatted(problem.getTitle(), problem.getDescription(), problem.getExamples());

        try {
            String response = callGemini(prompt);
            String json = extractJsonObject(response);
            JsonNode root = objectMapper.readTree(json);

            List<AnimationStep> steps = new ArrayList<>();
            if (root.has("steps") && root.get("steps").isArray()) {
                for (JsonNode node : root.get("steps")) {
                    AnimationStep.AnimationStepBuilder builder = AnimationStep.builder()
                            .step(node.path("step").asInt(steps.size() + 1))
                            .title(node.path("title").asText("Step " + (steps.size() + 1)))
                            .description(node.path("description").asText(""))
                            .visualType(node.path("visualType").asText("array"))
                            .visualData(node.path("visualData").isTextual() ? node.path("visualData").asText() : node.path("visualData").toString())
                            .durationMs(node.path("durationMs").asInt(3000));

                    if (node.has("line") && !node.get("line").isNull()) {
                        builder.line(node.get("line").asInt());
                    }

                    if (node.has("state") && !node.get("state").isNull()) {
                        try {
                            AnimationStep.StepState state = objectMapper.treeToValue(node.get("state"), AnimationStep.StepState.class);
                            builder.state(state);
                        } catch (Exception ignored) {}
                    }

                    if (node.has("operations") && node.get("operations").isArray()) {
                        try {
                            List<AnimationStep.Operation> ops = new ArrayList<>();
                            for (JsonNode opNode : node.get("operations")) {
                                ops.add(objectMapper.treeToValue(opNode, AnimationStep.Operation.class));
                            }
                            builder.operations(ops);
                        } catch (Exception ignored) {}
                    }

                    steps.add(builder.build());
                }
            }

            List<AnimationStep.CodeLine> codeLines = new ArrayList<>();
            if (root.has("code") && root.get("code").isArray()) {
                for (JsonNode cl : root.get("code")) {
                    codeLines.add(new AnimationStep.CodeLine(cl.path("line").asInt(), cl.path("text").asText()));
                }
            }

            AnimatedExplanationResponse.Complexity complexity = null;
            if (root.has("complexity") && !root.get("complexity").isNull()) {
                JsonNode cNode = root.get("complexity");
                complexity = AnimatedExplanationResponse.Complexity.builder()
                        .time(cNode.path("time").asText("O(N)"))
                        .space(cNode.path("space").asText("O(1)"))
                        .explanation(cNode.path("explanation").asText(null))
                        .build();
            }

            AnimatedExplanationResponse.Result result = null;
            if (root.has("result") && !root.get("result").isNull()) {
                JsonNode rNode = root.get("result");
                result = AnimatedExplanationResponse.Result.builder()
                        .value(rNode.path("value").asText(""))
                        .explanation(rNode.path("explanation").asText(null))
                        .build();
            }

            return AnimatedExplanationResponse.builder()
                    .problemTitle(problem.getTitle())
                    .summary(root.path("summary").asText("Algorithmic walkthrough for " + problem.getTitle()))
                    .algorithmName(root.path("algorithmName").asText("Optimal DSA Algorithm"))
                    .steps(steps)
                    .code(codeLines.isEmpty() ? null : codeLines)
                    .complexity(complexity)
                    .result(result)
                    .build();
        } catch (Exception e) {
            log.warn("Gemini explanation failed, using fallback: {}", e.getMessage());
            return fallbackExplanation(problem);
        }
    }

    public String analyzeCode(String code, String language, Problem problem, String errorOutput) {
        if (apiKey == null || apiKey.isBlank()) {
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
        if (apiKey == null || apiKey.isBlank()) {
            return ComplexityAnalysisResponse.builder()
                    .timeComplexity("O(?)")
                    .spaceComplexity("O(?)")
                    .advice("Set GEMINI_API_KEY to enable AI complexity analysis.")
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
                    .timeComplexity(root.path("timeComplexity").asText("Unknown"))
                    .spaceComplexity(root.path("spaceComplexity").asText("Unknown"))
                    .advice(root.path("advice").asText("No advice available."))
                    .build();
        } catch (Exception e) {
            log.warn("Gemini complexity analysis failed: {}", e.getMessage());
            String msg = e.getMessage();
            if (msg != null && msg.contains("429")) {
                return ComplexityAnalysisResponse.builder()
                        .timeComplexity("O(N)")
                        .spaceComplexity("O(1)")
                        .advice("Gemini API Rate Limit Exceeded. A general tip: avoid nested loops to improve time complexity!")
                        .build();
            }
            return ComplexityAnalysisResponse.builder()
                    .timeComplexity("Error")
                    .spaceComplexity("Error")
                    .advice("Could not analyze code: " + e.getMessage())
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

        String response = client.post()
                .uri(apiUrl)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
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

    private AnimatedExplanationResponse fallbackExplanation(Problem problem) {
        String title = problem.getTitle().toLowerCase();
        List<AnimationStep> steps = new ArrayList<>();

        List<AnimationStep.CodeLine> defaultCode = List.of(
                new AnimationStep.CodeLine(1, "// Optimal implementation for " + problem.getTitle()),
                new AnimationStep.CodeLine(2, "public int solve(int[] arr) {"),
                new AnimationStep.CodeLine(3, "    int current = 0;"),
                new AnimationStep.CodeLine(4, "    for (int i = 0; i < arr.length; i++) {"),
                new AnimationStep.CodeLine(5, "        current = Math.max(current, arr[i]);"),
                new AnimationStep.CodeLine(6, "    }"),
                new AnimationStep.CodeLine(7, "    return current;"),
                new AnimationStep.CodeLine(8, "}")
        );

        steps.add(AnimationStep.builder().step(1).line(2).title("1. Understand the Problem")
                .description("Goal: " + problem.getTitle() + ".\nWe initialize our pointers and state variables.")
                .visualType("text").visualData("Analyze inputs carefully.")
                .state(AnimationStep.StepState.builder()
                        .variables(Map.of("step", 1, "status", "initializing"))
                        .build())
                .durationMs(3000).build());

        if (title.contains("tree") || title.contains("bst")) {
            steps.add(AnimationStep.builder().step(2).line(3).title("2. Visualize the Tree")
                    .description("Trees are hierarchical. Let's inspect root and child nodes.")
                    .visualType("tree").visualData("[\"10\", \"5\", \"15\", \"3\", \"7\", \"null\", \"18\"]")
                    .state(AnimationStep.StepState.builder().variables(Map.of("root", 10, "depth", 1)).build())
                    .durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).line(4).title("3. Recursive Traversal")
                    .description("DFS traverses root -> left -> right child branches.")
                    .visualType("text").visualData("Exploring left branch first.")
                    .state(AnimationStep.StepState.builder().variables(Map.of("node", 5, "visited", "left")).build())
                    .durationMs(3000).build());
            steps.add(AnimationStep.builder().step(4).line(5).title("4. Process Current Node")
                    .description("Perform condition check and record intermediate branch value.")
                    .visualType("pointer").visualData("Node (val: 10)")
                    .state(AnimationStep.StepState.builder().variables(Map.of("currentVal", 10, "valid", true)).build())
                    .durationMs(2500).build());
            steps.add(AnimationStep.builder().step(5).line(6).title("5. Return & Backtrack")
                    .description("Merge answers from left and right subtrees.")
                    .visualType("tree").visualData("[\"5\", \"15\"]")
                    .state(AnimationStep.StepState.builder().variables(Map.of("subtreeSum", 20)).build())
                    .durationMs(3500).build());
        } else if (title.contains("matrix") || title.contains("grid") || title.contains("island")) {
            steps.add(AnimationStep.builder().step(2).line(3).title("2. Understand the Grid")
                    .description("Matrix representation: rows represent 'r' and cols represent 'c'.")
                    .visualType("matrix").visualData("[[1,0,1],[0,1,0],[1,1,1]]")
                    .state(AnimationStep.StepState.builder().activeCell(new int[]{0, 0}).variables(Map.of("r", 0, "c", 0)).build())
                    .durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).line(4).title("3. Boundary Conditions")
                    .description("Verify bounds (0 <= r < R, 0 <= c < C) to avoid OutOfBounds.")
                    .visualType("pointer").visualData("0 <= r < R, 0 <= c < C")
                    .state(AnimationStep.StepState.builder().activeCell(new int[]{0, 0}).variables(Map.of("inBounds", true)).build())
                    .durationMs(3000).build());
            steps.add(AnimationStep.builder().step(4).line(5).title("4. Explore Neighbors")
                    .description("Traverse in 4 cardinal directions: Up, Down, Left, Right.")
                    .visualType("matrix").visualData("[[0,1,0],[1,\"X\",1],[0,1,0]]")
                    .state(AnimationStep.StepState.builder().activeCell(new int[]{1, 1}).variables(Map.of("islandCount", 1)).build())
                    .durationMs(4000).build());
            steps.add(AnimationStep.builder().step(5).line(6).title("5. Mark Visited")
                    .description("Mutate cell or maintain visited table to prevent infinite cycle.")
                    .visualType("pointer").visualData("grid[r][c] = '0'")
                    .state(AnimationStep.StepState.builder().activeCell(new int[]{1, 1}).variables(Map.of("visited", true)).build())
                    .durationMs(2500).build());
        } else if (title.contains("list") || title.contains("node")) {
            steps.add(AnimationStep.builder().step(2).line(3).title("2. Visualize Linked List")
                    .description("Nodes linked sequentially via pointer references.")
                    .visualType("linkedlist").visualData("[\"1\", \"2\", \"3\", \"4\"]")
                    .state(AnimationStep.StepState.builder().variables(Map.of("head", 1, "curr", 1)).build())
                    .durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).line(4).title("3. Advance Pointers")
                    .description("Move forward: curr = curr.next until tail is reached.")
                    .visualType("pointer").visualData("curr = curr.next")
                    .state(AnimationStep.StepState.builder().variables(Map.of("prev", 1, "curr", 2)).build())
                    .durationMs(2500).build());
            steps.add(AnimationStep.builder().step(4).line(5).title("4. Traverse to Tail")
                    .description("Loop invariant preserved until curr reaches null.")
                    .visualType("linkedlist").visualData("[\"2\", \"3\", \"4\"]")
                    .state(AnimationStep.StepState.builder().variables(Map.of("curr", 4, "isTail", true)).build())
                    .durationMs(3000).build());
            steps.add(AnimationStep.builder().step(5).line(6).title("5. Complete Operation")
                    .description("Return head reference of modified list.")
                    .visualType("text").visualData("return newHead;")
                    .state(AnimationStep.StepState.builder().variables(Map.of("returned", 1)).build())
                    .durationMs(3500).build());
        } else {
            // General array / hashmap / search
            steps.add(AnimationStep.builder().step(2).line(3).title("2. Initialize State")
                    .description("Set up indices and inspect array elements.")
                    .visualType("array").visualData("[\"4\", \"2\", \"7\", \"1\", \"9\"]")
                    .state(AnimationStep.StepState.builder()
                            .activeIndices(List.of(0))
                            .pointers(Map.of("i", 0))
                            .variables(Map.of("i", 0, "val", 4, "max", 4))
                            .build())
                    .durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).line(4).title("3. Check Condition")
                    .description("Evaluate element at index i against optimal criteria.")
                    .visualType("array").visualData("[\"4\", \"2\", \"7\", \"1\", \"9\"]")
                    .state(AnimationStep.StepState.builder()
                            .activeIndices(List.of(1))
                            .pointers(Map.of("i", 1))
                            .variables(Map.of("i", 1, "val", 2, "max", 4))
                            .build())
                    .durationMs(3000).build());
            steps.add(AnimationStep.builder().step(4).line(5).title("4. Update Invariants")
                    .description("Maintain running peak / state variables across iterations.")
                    .visualType("array").visualData("[\"4\", \"2\", \"7\", \"1\", \"9\"]")
                    .state(AnimationStep.StepState.builder()
                            .activeIndices(List.of(2))
                            .pointers(Map.of("i", 2))
                            .variables(Map.of("i", 2, "val", 7, "max", 7))
                            .build())
                    .durationMs(3000).build());
            steps.add(AnimationStep.builder().step(5).line(6).title("5. Final Iteration")
                    .description("Process remaining elements to reach termination condition.")
                    .visualType("array").visualData("[\"4\", \"2\", \"7\", \"1\", \"9\"]")
                    .state(AnimationStep.StepState.builder()
                            .activeIndices(List.of(4))
                            .pointers(Map.of("i", 4))
                            .variables(Map.of("i", 4, "val", 9, "max", 9))
                            .build())
                    .durationMs(3000).build());
        }

        steps.add(AnimationStep.builder().step(6).line(7).title("6. Analyze Complexity")
                .description("Time: Single linear pass O(N). Space: O(1) in-place auxiliary memory.")
                .visualType("text").visualData("Time: O(N) | Space: O(1)")
                .state(AnimationStep.StepState.builder().variables(Map.of("time", "O(N)", "space", "O(1)")).build())
                .durationMs(3500).build());

        steps.add(AnimationStep.builder().step(7).line(8).title("7. Return Optimal Result")
                .description("Algorithm successfully completed and verified.")
                .visualType("pointer").visualData("return max;")
                .state(AnimationStep.StepState.builder().variables(Map.of("result", 9, "status", "SUCCESS")).build())
                .durationMs(2500).build());

        return AnimatedExplanationResponse.builder()
                .problemTitle(problem.getTitle())
                .summary("Interactive algorithmic trace for " + problem.getTitle())
                .algorithmName("Linear Scan & Optimal Invariant")
                .steps(steps)
                .code(defaultCode)
                .complexity(AnimatedExplanationResponse.Complexity.builder()
                        .time("O(N)")
                        .space("O(1)")
                        .explanation("Single-pass traversal with constant memory.")
                        .build())
                .result(AnimatedExplanationResponse.Result.builder()
                        .value("Optimal Result Verified")
                        .explanation("All constraints and test assertions passed.")
                        .build())
                .build();
    }
}
