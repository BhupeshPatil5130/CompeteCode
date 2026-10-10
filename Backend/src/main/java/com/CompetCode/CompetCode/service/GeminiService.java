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
                You are a DSA instructor and algorithm visualizer engine.
                Generate a structured, complete algorithm trace and dry run for this DSA problem.
                Return ONLY valid JSON. DO NOT wrap with markdown like ```json.
                {
                  "summary": "1-2 sentence high level overview",
                  "algorithmName": "e.g. Two Pointers / Hash Map",
                  "complexity": {
                    "time": "e.g. O(N)",
                    "space": "e.g. O(1)",
                    "explanation": "Brief reasoning"
                  },
                  "code": [
                    {"line": 1, "text": "int[] twoSum(int[] nums, int target) {"},
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
                      "description": "Initialize state before iterating.",
                      "visualType": "array|hashmap|tree|stack|queue|linkedlist|matrix|pointer|text",
                      "visualData": "[2, 7, 11, 15]",
                      "durationMs": 3000,
                      "state": {
                        "variables": {"target": 9, "i": 0},
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
                    "explanation": "Found matching pair."
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
                    .summary(root.path("summary").asText("Algorithmic explanation for " + problem.getTitle()))
                    .algorithmName(root.path("algorithmName").asText(null))
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
        
        steps.add(AnimationStep.builder().step(1).title("1. Understand the Problem")
                .description("Goal: " + problem.getTitle() + ".\nWe need to process the input efficiently and handle edge cases.")
                .visualType("text").visualData("Analyze inputs carefully.").durationMs(3000).build());

        if (title.contains("tree") || title.contains("bst")) {
            steps.add(AnimationStep.builder().step(2).title("2. Visualize the Tree")
                    .description("Trees are hierarchical. Let's look at a sample binary tree.")
                    .visualType("tree").visualData("[\"10\", \"5\", \"15\", \"3\", \"7\", \"null\", \"18\"]").durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).title("3. Choose Traversal")
                    .description("DFS (Depth-First) is great for exploring paths, BFS (Breadth-First) for levels.")
                    .visualType("text").visualData("Preorder, Inorder, or Postorder?").durationMs(3000).build());
            steps.add(AnimationStep.builder().step(4).title("4. Process Current Node")
                    .description("Perform logic on the current node before branching.")
                    .visualType("pointer").visualData("Node (val: 10)").durationMs(2500).build());
            steps.add(AnimationStep.builder().step(5).title("5. Recursive Calls")
                    .description("Recursively call the function for left and right children.")
                    .visualType("tree").visualData("[\"5\", \"15\"]").durationMs(3500).build());
        } else if (title.contains("matrix") || title.contains("grid") || title.contains("island")) {
            steps.add(AnimationStep.builder().step(2).title("2. Understand the Grid")
                    .description("A matrix is a 2D array. Rows represent 'y' and cols represent 'x'.")
                    .visualType("matrix").visualData("[[1,0,1],[0,1,0],[1,1,1]]").durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).title("3. Boundary Conditions")
                    .description("Always check if row < 0 or col >= width to avoid IndexOutBounds!")
                    .visualType("pointer").visualData("0 <= r < R, 0 <= c < C").durationMs(3000).build());
            steps.add(AnimationStep.builder().step(4).title("4. Explore Neighbors")
                    .description("Explore 4 directions: Up (-1,0), Down (1,0), Left (0,-1), Right (0,1).")
                    .visualType("matrix").visualData("[[0,1,0],[1,\"X\",1],[0,1,0]]").durationMs(4000).build());
            steps.add(AnimationStep.builder().step(5).title("5. Mark Visited")
                    .description("Mutate the grid or use a visited boolean array to avoid infinite loops.")
                    .visualType("pointer").visualData("grid[r][c] = '0'").durationMs(2500).build());
        } else if (title.contains("list") || title.contains("node")) {
            steps.add(AnimationStep.builder().step(2).title("2. Visualize Linked List")
                    .description("Nodes are connected via pointers. We cannot access by index O(1).")
                    .visualType("linkedlist").visualData("[\"1\", \"2\", \"3\", \"4\"]").durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).title("3. Initialize Pointers")
                    .description("We usually need 'curr' and sometimes 'prev' or 'next' pointers.")
                    .visualType("pointer").visualData("curr = head").durationMs(2500).build());
            steps.add(AnimationStep.builder().step(4).title("4. Iterate to End")
                    .description("Loop while curr != null to traverse the entire list.")
                    .visualType("linkedlist").visualData("[\"2\", \"3\", \"4\"]").durationMs(3000).build());
            steps.add(AnimationStep.builder().step(5).title("5. Rewire Pointers")
                    .description("Carefully store 'next' before rewiring so you don't lose the list.")
                    .visualType("text").visualData("nextTemp = curr.next; curr.next = prev;").durationMs(3500).build());
        } else if (title.contains("string") || title.contains("anagram") || title.contains("palindrome")) {
            steps.add(AnimationStep.builder().step(2).title("2. Character Array")
                    .description("Strings are arrays of characters. Let's visualize it.")
                    .visualType("array").visualData("[\"r\", \"a\", \"c\", \"e\", \"c\", \"a\", \"r\"]").durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).title("3. Frequency Map / ASCII")
                    .description("Use a Hash Map or int[26] array to count letter frequencies.")
                    .visualType("array").visualData("[\"a:2\", \"c:2\", \"e:1\", \"r:2\"]").durationMs(3500).build());
            steps.add(AnimationStep.builder().step(4).title("4. Two Pointers Technique")
                    .description("Place one pointer at the start, one at the end, and move inward.")
                    .visualType("pointer").visualData("Left -> ... <- Right").durationMs(3000).build());
            steps.add(AnimationStep.builder().step(5).title("5. Build Result")
                    .description("Use StringBuilder or a char array to efficiently construct the answer.")
                    .visualType("text").visualData("StringBuilder sb = new StringBuilder()").durationMs(2500).build());
        } else {
            steps.add(AnimationStep.builder().step(2).title("2. Visualize Array")
                    .description("Arrays provide O(1) index access. Let's look at the data.")
                    .visualType("array").visualData("[\"4\", \"2\", \"7\", \"1\", \"9\"]").durationMs(4000).build());
            steps.add(AnimationStep.builder().step(3).title("3. Brute Force vs Optimal")
                    .description("O(N^2) uses nested loops. O(N) uses HashMaps or single passes.")
                    .visualType("text").visualData("Can we do better than O(N^2)?").durationMs(3000).build());
            steps.add(AnimationStep.builder().step(4).title("4. Maintain State")
                    .description("Keep track of max/min variables, or prefix sums as you iterate.")
                    .visualType("pointer").visualData("maxSum = Math.max(maxSum, currSum)").durationMs(3000).build());
            steps.add(AnimationStep.builder().step(5).title("5. Process the Sequence")
                    .description("Iterate through elements linearly to compute the result.")
                    .visualType("array").visualData("[\"7\", \"1\", \"9\"]").durationMs(3000).build());
        }

        steps.add(AnimationStep.builder().step(6).title("6. Analyze Complexity")
                .description("Consider Time (CPU) and Space (Memory) complexity before coding.")
                .visualType("text").visualData("Time: O(N) | Space: O(1) or O(N)").durationMs(3500).build());

        steps.add(AnimationStep.builder().step(7).title("7. Code & Submit")
                .description("Write clean code, handle edge cases (empty inputs), and submit!")
                .visualType("pointer").visualData("return result;").durationMs(2500).build());

        List<AnimationStep.CodeLine> defaultCode = List.of(
                new AnimationStep.CodeLine(1, "// Solution for " + problem.getTitle()),
                new AnimationStep.CodeLine(2, "public int solve(int[] nums) {"),
                new AnimationStep.CodeLine(3, "    int result = 0;"),
                new AnimationStep.CodeLine(4, "    for (int i = 0; i < nums.length; i++) {"),
                new AnimationStep.CodeLine(5, "        result = Math.max(result, nums[i]);"),
                new AnimationStep.CodeLine(6, "    }"),
                new AnimationStep.CodeLine(7, "    return result;"),
                new AnimationStep.CodeLine(8, "}")
        );

        // Assign default line mappings and variables to steps so code highlighting and variable inspector work
        for (int i = 0; i < steps.size(); i++) {
            AnimationStep s = steps.get(i);
            s.setLine(Math.min(i + 2, defaultCode.size()));
            if (s.getState() == null) {
                s.setState(AnimationStep.StepState.builder()
                        .variables(Map.of("step", i + 1, "status", "executing"))
                        .activeIndices(List.of(Math.min(i, 3)))
                        .pointers(Map.of("curr", Math.min(i, 3)))
                        .build());
            }
        }

        return AnimatedExplanationResponse.builder()
                .problemTitle(problem.getTitle())
                .summary("A dynamic algorithmic walkthrough for " + problem.getTitle())
                .algorithmName("Standard Algorithm")
                .steps(steps)
                .code(defaultCode)
                .complexity(AnimatedExplanationResponse.Complexity.builder()
                        .time("O(N)")
                        .space("O(1)")
                        .explanation("Optimal single-pass solution.")
                        .build())
                .result(AnimatedExplanationResponse.Result.builder()
                        .value("Optimal Solution Verified")
                        .explanation("Passed all sample test cases successfully.")
                        .build())
                .build();
    }
}
