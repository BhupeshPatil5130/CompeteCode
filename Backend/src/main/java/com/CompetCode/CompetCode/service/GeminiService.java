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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean hasValidApiKey() {
        return apiKey != null
                && !apiKey.isBlank()
                && !apiKey.toLowerCase().contains("your_")
                && !apiKey.equalsIgnoreCase("dummy")
                && !apiKey.equalsIgnoreCase("placeholder");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. TEST CASE GENERATION
    // ─────────────────────────────────────────────────────────────────────────

    public List<TestCase> generateTestCases(Problem problem, int count) {
        if (!hasValidApiKey()) {
            return fallbackTestCases(problem);
        }

        String prompt = """
                You are a senior competitive programming test case engineer.
                Generate exactly %d high-quality hidden test cases for problem: "%s".
                Problem Description: %s
                Constraints: %s
                Examples: %s

                Requirements:
                1. Match the exact stdin format and expected stdout format for competitive programming.
                2. Include standard cases, edge cases (min/max boundary, single element, duplicates, negative values).
                3. Return ONLY a valid JSON array of objects with keys "input" and "expectedOutput".
                No markdown, no explanation:
                [
                  {"input": "...", "expectedOutput": "..."}
                ]
                """.formatted(count, problem.getTitle(), problem.getDescription(),
                problem.getConstraints() != null ? problem.getConstraints() : "",
                problem.getExamples() != null ? problem.getExamples() : "");

        try {
            String response = callGemini(prompt, true);
            String json = extractJsonArray(response);
            JsonNode array = objectMapper.readTree(json);

            List<TestCase> cases = new ArrayList<>();
            if (array.isArray()) {
                for (JsonNode node : array) {
                    String input = node.path("input").asText("").trim();
                    String expected = node.path("expectedOutput").asText("").trim();
                    if (!input.isEmpty() && !expected.isEmpty()) {
                        cases.add(TestCase.builder()
                                .problem(problem)
                                .input(input)
                                .expectedOutput(expected)
                                .isHidden(true)
                                .isAiGenerated(true)
                                .weight(1)
                                .build());
                    }
                }
            }
            if (!cases.isEmpty()) {
                return cases;
            }
        } catch (Exception e) {
            log.warn("Gemini test case generation failed: {}, falling back to deterministic cases", e.getMessage());
        }
        return fallbackTestCases(problem);
    }

    private List<TestCase> fallbackTestCases(Problem problem) {
        List<TestCase> cases = new ArrayList<>();

        if (problem.getTestCases() != null) {
            for (TestCase tc : problem.getTestCases()) {
                if (Boolean.TRUE.equals(tc.getIsHidden()) && tc.getInput() != null && !tc.getInput().isBlank()) {
                    cases.add(tc);
                }
            }
        }
        if (!cases.isEmpty()) return cases;

        // Parse examples from problem definition
        if (problem.getExamples() != null && !problem.getExamples().isBlank()) {
            List<TestCase> parsed = parseExamplesIntoTestCases(problem);
            if (!parsed.isEmpty()) return parsed;
        }

        return generateDeterministicTestCases(problem);
    }

    private List<TestCase> parseExamplesIntoTestCases(Problem problem) {
        List<TestCase> list = new ArrayList<>();
        String ex = problem.getExamples();
        Pattern p = Pattern.compile("(?i)input:\\s*(.+?)\\s*output:\\s*([^\\n]+)", Pattern.DOTALL);
        Matcher m = p.matcher(ex);

        while (m.find()) {
            String in = m.group(1).trim();
            String out = m.group(2).trim();
            in = in.replaceAll("(?i)^[a-zA-Z0-9_]+\\s*=\\s*", "").trim();
            out = out.replaceAll("(?i)^[a-zA-Z0-9_]+\\s*=\\s*", "").trim();
            list.add(TestCase.builder()
                    .problem(problem)
                    .input(in)
                    .expectedOutput(out)
                    .isHidden(true)
                    .isAiGenerated(false)
                    .weight(1)
                    .build());
        }
        return list;
    }

    private List<TestCase> generateDeterministicTestCases(Problem problem) {
        String key = problem.getExecutionKey() != null ? problem.getExecutionKey().toUpperCase() : "";
        List<TestCase> list = new ArrayList<>();

        if (key.equals("TWO_SUM")) {
            list.add(TestCase.builder().problem(problem).input("2 7 11 15\n9").expectedOutput("0 1").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("3 2 4\n6").expectedOutput("1 2").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("3 3\n6").expectedOutput("0 1").isHidden(true).weight(1).build());
        } else if (key.equals("CONTAINS_DUPLICATE")) {
            list.add(TestCase.builder().problem(problem).input("1 2 3 1").expectedOutput("true").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("1 2 3 4").expectedOutput("false").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("1 1 1 3 3 4 3 2 4 2").expectedOutput("true").isHidden(true).weight(1).build());
        } else if (key.equals("VALID_ANAGRAM")) {
            list.add(TestCase.builder().problem(problem).input("anagram\nnagaram").expectedOutput("true").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("rat\ncar").expectedOutput("false").isHidden(true).weight(1).build());
        } else if (key.equals("VALID_PARENTHESES")) {
            list.add(TestCase.builder().problem(problem).input("()[]{}").expectedOutput("true").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("(]").expectedOutput("false").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("([)]").expectedOutput("false").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("{[]}").expectedOutput("true").isHidden(true).weight(1).build());
        } else if (key.equals("BINARY_SEARCH")) {
            list.add(TestCase.builder().problem(problem).input("-1 0 3 5 9 12\n9").expectedOutput("4").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("-1 0 3 5 9 12\n2").expectedOutput("-1").isHidden(true).weight(1).build());
        } else if (key.equals("MAX_SUBARRAY")) {
            list.add(TestCase.builder().problem(problem).input("-2 1 -3 4 -1 2 1 -5 4").expectedOutput("6").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("1").expectedOutput("1").isHidden(true).weight(1).build());
            list.add(TestCase.builder().problem(problem).input("5 4 -1 7 8").expectedOutput("23").isHidden(true).weight(1).build());
        } else {
            String defaultInput = problem.getExamples() != null && !problem.getExamples().isBlank() ? "1 2 3" : "0";
            list.add(TestCase.builder().problem(problem).input(defaultInput).expectedOutput("0").isHidden(true).weight(1).build());
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ANIMATED EXPLANATION GENERATION
    // ─────────────────────────────────────────────────────────────────────────

    public AnimatedExplanationResponse generateAnimatedExplanation(Problem problem) {
        if (!hasValidApiKey()) {
            log.info("No valid Gemini API key configured, using high-fidelity deterministic trace for {}", problem.getTitle());
            return generateDeterministicTrace(problem);
        }

        String prompt = """
                You are a world-class DSA visualization engine.
                Perform a step-by-step DRY RUN on the FIRST example of this problem.

                Problem: %s
                Description: %s
                Examples: %s
                Constraints: %s

                CRITICAL INSTRUCTIONS:
                1. "code": An array of strings representing the clean optimal Java or standard algorithm code.
                2. "visualType": MUST be one of:
                   - "array": 1D array iteration
                   - "two-pointer": two-pointer inward or sliding window
                   - "bars": height-based comparison (visualData = JSON array of heights)
                   - "tree": binary tree (BFS JSON array e.g. ["3","9","20","null","null","15","7"])
                   - "matrix": 2D grid (2D JSON array)
                   - "stack": stack operations (visualData = JSON array of items bottom to top)
                   - "linkedlist": linked list nodes (JSON array of values)
                   - "pointer": focused string or value
                3. "visualData": ALWAYS provide the state of the data structure at this step as a valid JSON array or string. NEVER empty!
                4. "pointers": 0-based index map (e.g. {"i": 0} or {"left": 0, "right": 3}).
                5. "highlights": 0-based indices being inspected/compared at this step (e.g. [0, 1]).
                6. "vars": Map of key-value pairs showing real variable values at this step (e.g. {"target": "9", "sum": "8"}). For HashMaps, include "map": "{2: 0, 7: 1}".
                7. "codeLines": 1-based line numbers in "code" executing at this step (e.g. [3, 4]).
                8. Generate between 8 to 16 detailed, authentic steps covering EVERY iteration and state change.

                Return ONLY this JSON schema:
                {
                  "summary": "One-line clear summary of the optimal algorithm approach",
                  "algorithmName": "e.g. Two Pointers / Hash Map",
                  "algorithmCategory": "e.g. Arrays & Hashing",
                  "exampleInput": "the exact example input used",
                  "exampleOutput": "the expected return output",
                  "code": [
                    "public int[] twoSum(int[] nums, int target) {",
                    "    Map<Integer, Integer> map = new HashMap<>();",
                    "    for (int i = 0; i < nums.length; i++) {",
                    "        int complement = target - nums[i];",
                    "        if (map.containsKey(complement)) return new int[] { map.get(complement), i };",
                    "        map.put(nums[i], i);",
                    "    }",
                    "    return new int[] {};",
                    "}"
                  ],
                  "steps": [
                    {
                      "step": 1,
                      "title": "1. Initialize Data Structures",
                      "description": "Initialize pointers and hash table.",
                      "visualType": "array",
                      "visualData": "[2, 7, 11, 15]",
                      "pointers": {"i": 0},
                      "highlights": [0],
                      "vars": {"target": "9", "map": "{}"},
                      "codeLines": [1, 2],
                      "durationMs": 2500
                    }
                  ]
                }
                """.formatted(
                problem.getTitle(),
                problem.getDescription(),
                problem.getExamples() != null ? problem.getExamples() : "No examples provided",
                problem.getConstraints() != null ? problem.getConstraints() : "");

        try {
            String response = callGemini(prompt, true);
            String json = extractJsonObject(response);
            JsonNode root = objectMapper.readTree(json);

            String exampleInputStr = root.path("exampleInput").asText("");
            String fallbackData = extractFallbackVisualData(exampleInputStr, problem);

            // Extract code at root
            List<String> rootCode = new ArrayList<>();
            if (root.has("code") && root.get("code").isArray()) {
                root.get("code").forEach(c -> rootCode.add(c.asText()));
            }

            List<AnimationStep> steps = new ArrayList<>();
            JsonNode stepsNode = root.path("steps");
            if (stepsNode.isArray()) {
                for (JsonNode node : stepsNode) {
                    JsonNode vNode = node.get("visualData");
                    String visualDataStr = "";
                    if (vNode != null && !vNode.isNull()) {
                        visualDataStr = vNode.isTextual() ? vNode.asText() : vNode.toString();
                    }

                    if (visualDataStr.isBlank() || visualDataStr.equals("[]")) {
                        if (!steps.isEmpty() && steps.get(steps.size() - 1).getVisualData() != null && !steps.get(steps.size() - 1).getVisualData().isBlank()) {
                            visualDataStr = steps.get(steps.size() - 1).getVisualData();
                        } else if (!fallbackData.isBlank()) {
                            visualDataStr = fallbackData;
                        }
                    }

                    String visualType = node.path("visualType").asText("array");
                    if (visualType.equalsIgnoreCase("text") && !fallbackData.isBlank()) {
                        visualType = "array";
                    }

                    List<String> stepCode = new ArrayList<>();
                    if (node.has("code") && node.get("code").isArray()) {
                        node.get("code").forEach(c -> stepCode.add(c.asText()));
                    } else if (!rootCode.isEmpty()) {
                        stepCode.addAll(rootCode);
                    }

                    AnimationStep.AnimationStepBuilder builder = AnimationStep.builder()
                            .step(node.path("step").asInt(steps.size() + 1))
                            .title(node.path("title").asText("Step " + (steps.size() + 1)))
                            .description(node.path("description").asText(""))
                            .visualType(visualType)
                            .visualData(visualDataStr)
                            .durationMs(node.path("durationMs").asInt(2600))
                            .code(stepCode);

                    if (node.has("pointers") && node.get("pointers").isObject()) {
                        Map<String, Integer> ptrs = new LinkedHashMap<>();
                        node.get("pointers").fields().forEachRemaining(e -> {
                            try { ptrs.put(e.getKey(), e.getValue().asInt()); } catch (Exception ignored) {}
                        });
                        builder.pointers(ptrs);
                    }

                    if (node.has("highlights") && node.get("highlights").isArray()) {
                        List<Integer> hl = new ArrayList<>();
                        node.get("highlights").forEach(n -> hl.add(n.asInt()));
                        builder.highlights(hl);
                    }

                    if (node.has("vars") && node.get("vars").isObject()) {
                        Map<String, String> vars = new LinkedHashMap<>();
                        node.get("vars").fields().forEachRemaining(e -> vars.put(e.getKey(), e.getValue().asText()));
                        builder.vars(vars);
                    }

                    if (node.has("codeLines") && node.get("codeLines").isArray()) {
                        List<Integer> cl = new ArrayList<>();
                        node.get("codeLines").forEach(n -> cl.add(n.asInt()));
                        builder.codeLines(cl);
                    }

                    steps.add(builder.build());
                }
            }

            if (steps.isEmpty()) {
                return generateDeterministicTrace(problem);
            }

            // Ensure first step code is populated to all if root was missing
            if (rootCode.isEmpty() && !steps.isEmpty() && steps.get(0).getCode() != null && !steps.get(0).getCode().isEmpty()) {
                rootCode.addAll(steps.get(0).getCode());
                for (AnimationStep s : steps) {
                    if (s.getCode() == null || s.getCode().isEmpty()) {
                        s.setCode(rootCode);
                    }
                }
            }

            List<AnimationStep.CodeLine> codeLineObjects = new ArrayList<>();
            for (int i = 0; i < rootCode.size(); i++) {
                codeLineObjects.add(AnimationStep.CodeLine.builder().line(i + 1).text(rootCode.get(i)).build());
            }

            return AnimatedExplanationResponse.builder()
                    .problemTitle(problem.getTitle())
                    .summary(root.path("summary").asText("Optimal algorithmic solution."))
                    .exampleInput(root.path("exampleInput").asText(exampleInputStr))
                    .exampleOutput(root.path("exampleOutput").asText(""))
                    .algorithmName(root.path("algorithmName").asText("Optimal DSA Execution"))
                    .algorithmCategory(root.path("algorithmCategory").asText(problem.getTopic() != null ? problem.getTopic() : "Algorithms"))
                    .code(codeLineObjects)
                    .steps(steps)
                    .complexity(AnimatedExplanationResponse.Complexity.builder()
                            .time(root.path("timeComplexity").asText("O(N)"))
                            .space(root.path("spaceComplexity").asText("O(1)"))
                            .build())
                    .build();

        } catch (Exception e) {
            log.warn("Gemini animated explanation failed ({}): falling back to high-fidelity deterministic trace", e.getMessage());
            return generateDeterministicTrace(problem);
        }
    }

    private String extractFallbackVisualData(String exampleInputStr, Problem problem) {
        Matcher m = Pattern.compile("\\[[^\\]]+\\]").matcher(exampleInputStr);
        if (m.find()) return m.group();
        if (problem.getExamples() != null) {
            Matcher m2 = Pattern.compile("\\[[^\\]]+\\]").matcher(problem.getExamples());
            if (m2.find()) return m2.group();
        }
        return "[1, 2, 3, 4, 5]";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. COMPLEXITY ANALYSIS & CODE ADVICE
    // ─────────────────────────────────────────────────────────────────────────

    public ComplexityAnalysisResponse analyzeComplexity(Problem problem, String code, String language) {
        if (!hasValidApiKey()) {
            return fallbackComplexity(problem, code);
        }

        String prompt = """
                Analyze the time and space complexity of this %s code for problem "%s".
                Return ONLY valid JSON:
                {
                  "timeComplexity": "e.g., O(N) or O(N log N)",
                  "spaceComplexity": "e.g., O(1) or O(N)",
                  "advice": "Actionable optimization or trade-off advice under 40 words."
                }

                Code:
                %s
                """.formatted(language, problem.getTitle(), code);

        try {
            String response = callGemini(prompt, true);
            String json = extractJsonObject(response);
            JsonNode root = objectMapper.readTree(json);

            return ComplexityAnalysisResponse.builder()
                    .timeComplexity(root.path("timeComplexity").asText("O(N)"))
                    .spaceComplexity(root.path("spaceComplexity").asText("O(1)"))
                    .advice(root.path("advice").asText("Code execution complexity is optimal."))
                    .build();
        } catch (Exception e) {
            log.warn("Gemini complexity analysis failed: {}", e.getMessage());
            return fallbackComplexity(problem, code);
        }
    }

    private ComplexityAnalysisResponse fallbackComplexity(Problem problem, String code) {
        String time = "O(N)";
        String space = "O(1)";
        String advice = "Optimal algorithmic complexity.";

        if (code != null) {
            String c = code.toLowerCase();
            // Nested loop heuristic
            if ((c.contains("for (") || c.contains("for(")) && (c.indexOf("for", c.indexOf("for") + 4) != -1)) {
                time = "O(N^2)";
                advice = "Consider using a HashMap or two-pointer approach to reduce time complexity to O(N).";
            } else if (c.contains("binarysearch") || c.contains("/ 2") || c.contains("mid =") || c.contains(">> 1")) {
                time = "O(log N)";
                advice = "Efficient logarithmic search space reduction.";
            } else if (c.contains("sort(") || c.contains("arrays.sort") || c.contains("collections.sort")) {
                time = "O(N log N)";
                advice = "Sorting dominates runtime. Check if counting sort or linear scan is applicable.";
            }

            if (c.contains("hashmap") || c.contains("hashset") || c.contains("new int[") || c.contains("new arraylist")) {
                space = "O(N)";
            }
        }

        return ComplexityAnalysisResponse.builder()
                .timeComplexity(time)
                .spaceComplexity(space)
                .advice(advice)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. CODE FEEDBACK
    // ─────────────────────────────────────────────────────────────────────────

    public String analyzeCode(String code, String language, Problem problem, String errorOutput) {
        if (!hasValidApiKey()) {
            return "Set GEMINI_API_KEY in the environment for AI-powered code evaluation.";
        }

        String prompt = """
                You are a senior algorithmic engineering mentor.
                Evaluate this %s code submission for problem "%s".
                Problem Description: %s
                Code:
                %s
                Error/Output: %s

                Provide concise, actionable review (under 180 words):
                1. Correctness analysis.
                2. Time & space complexity.
                3. One key optimization or edge case tip.
                """.formatted(language, problem.getTitle(), problem.getDescription(), code,
                errorOutput != null && !errorOutput.isBlank() ? errorOutput : "No runtime errors");

        try {
            return callGemini(prompt, false);
        } catch (Exception e) {
            return "Could not generate AI feedback: " + e.getMessage();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. GEMINI API CLIENT WITH RETRIES & FALLBACK
    // ─────────────────────────────────────────────────────────────────────────

    private String callGemini(String prompt, boolean jsonMode) throws Exception {
        String cleanKey = apiKey != null ? apiKey.trim() : "";
        String primaryUrl = apiUrl != null && !apiUrl.isBlank() ? apiUrl.trim() :
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

        List<String> targetUrls = new ArrayList<>();
        targetUrls.add(primaryUrl);

        if (primaryUrl.contains("gemini-2.0-flash")) {
            targetUrls.add(primaryUrl.replace("gemini-2.0-flash", "gemini-1.5-flash"));
        } else if (primaryUrl.contains("gemini-1.5-flash")) {
            targetUrls.add(primaryUrl.replace("gemini-1.5-flash", "gemini-2.0-flash"));
        }

        Exception lastEx = null;

        for (String baseUri : targetUrls) {
            try {
                String targetUrl = baseUri;
                if (!targetUrl.contains("key=") && !cleanKey.isEmpty()) {
                    targetUrl += (targetUrl.contains("?") ? "&" : "?") + "key=" + cleanKey;
                }

                Map<String, Object> body = new HashMap<>();
                body.put("contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )));

                if (jsonMode) {
                    body.put("generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "temperature", 0.2,
                            "maxOutputTokens", 8192
                    ));
                } else {
                    body.put("generationConfig", Map.of(
                            "temperature", 0.3,
                            "maxOutputTokens", 4096
                    ));
                }

                RestClient client = RestClient.builder().build();

                String response = client.post()
                        .uri(targetUrl)
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", cleanKey)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                if (response == null || response.isBlank()) continue;

                JsonNode root = objectMapper.readTree(response);
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && !parts.isEmpty()) {
                        String text = parts.get(0).path("text").asText();
                        if (text != null && !text.isBlank()) {
                            return cleanMarkdownCodeBlocks(text);
                        }
                    }
                }

                if (root.has("error")) {
                    String msg = root.path("error").path("message").asText("Gemini Error");
                    throw new RuntimeException(msg);
                }
            } catch (Exception e) {
                log.warn("Gemini call to {} failed: {}", baseUri, e.getMessage());
                lastEx = e;
            }
        }

        throw (lastEx != null ? lastEx : new RuntimeException("Gemini generation failed"));
    }

    private String cleanMarkdownCodeBlocks(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
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

    // ─────────────────────────────────────────────────────────────────────────
    // 6. HIGH-FIDELITY DETERMINISTIC TRACE ENGINE (BUILT-IN TRACES)
    // ─────────────────────────────────────────────────────────────────────────

    private AnimatedExplanationResponse generateDeterministicTrace(Problem problem) {
        String title = problem.getTitle() != null ? problem.getTitle().toLowerCase().trim() : "";
        String key = problem.getExecutionKey() != null ? problem.getExecutionKey().toUpperCase().trim() : "";

        if (key.equals("TWO_SUM") || title.equals("two sum")) {
            return generateTwoSumTrace(problem);
        } else if (key.equals("THREE_SUM") || key.equals("3SUM") || title.contains("3 sum") || title.contains("3sum") || title.contains("three sum")) {
            return generateThreeSumTrace(problem);
        } else if (key.equals("CONTAINER_WITH_MOST_WATER") || key.equals("CONTAINER_WATER") || title.contains("container with most water")) {
            return generateContainerWaterTrace(problem);
        } else if (key.equals("TRAPPING_RAIN_WATER") || title.contains("trapping rain")) {
            return generateTrappingRainWaterTrace(problem);
        } else if (key.equals("MAX_SUBARRAY") || key.equals("KADANE") || title.contains("maximum subarray") || title.contains("kadane")) {
            return generateMaxSubarrayTrace(problem);
        } else if (key.equals("BEST_TIME_STOCK") || key.equals("BEST_TIME_BUY") || title.contains("best time to buy and sell stock") || title.contains("stock buy and sell")) {
            return generateBestTimeToBuySellStockTrace(problem);
        } else if (key.equals("CONTAINS_DUPLICATE") || title.equals("contains duplicate")) {
            return generateContainsDuplicateTrace(problem);
        } else if (key.equals("VALID_ANAGRAM") || title.contains("valid anagram") || title.contains("check for anagrams")) {
            return generateValidAnagramTrace(problem);
        } else if (key.equals("GROUP_ANAGRAMS") || title.contains("group anagrams")) {
            return generateGroupAnagramsTrace(problem);
        } else if (key.equals("TOP_K_FREQUENT") || title.contains("top k frequent")) {
            return generateTopKFrequentTrace(problem);
        } else if (key.equals("VALID_PALINDROME") || title.equals("valid palindrome")) {
            return generateValidPalindromeTrace(problem);
        } else if (key.equals("LONGEST_PALINDROME") || title.contains("longest palindromic substring") || title.contains("longest palindrome in a string")) {
            return generateLongestPalindromeTrace(problem);
        } else if (key.equals("VALID_PARENTHESES") || title.contains("valid parentheses") || title.contains("balanced parentheses")) {
            return generateValidParenthesesTrace(problem);
        } else if (key.equals("MIN_STACK") || title.contains("min stack")) {
            return generateMinStackTrace(problem);
        } else if (key.equals("BINARY_SEARCH") || title.equals("binary search")) {
            return generateBinarySearchTrace(problem);
        } else if (key.equals("CLIMBING_STAIRS") || key.equals("CLIMB_STAIRS") || title.contains("climbing stairs")) {
            return generateClimbingStairsTrace(problem);
        } else if (key.equals("COIN_CHANGE") || title.contains("coin change")) {
            return generateCoinChangeTrace(problem);
        } else if (key.equals("HOUSE_ROBBER") || title.contains("house robber")) {
            return generateHouseRobberTrace(problem);
        } else if (key.equals("REVERSE_LINKED_LIST") || title.contains("reverse a linkedlist") || title.contains("reverse linked list")) {
            return generateLinkedListTrace(problem);
        } else if (key.equals("MERGE_TWO_SORTED_LISTS") || title.contains("merge two sorted lists") || title.contains("merge two sorted linked list")) {
            return generateMergeTwoSortedListsTrace(problem);
        } else if (key.equals("MAX_DEPTH") || title.contains("maximum depth of binary tree") || title.contains("height of a binary tree")) {
            return generateTreeTrace(problem);
        } else if (key.equals("INVERT_BINARY_TREE") || title.contains("invert binary tree")) {
            return generateInvertTreeTrace(problem);
        } else if (key.equals("NUMBER_OF_ISLANDS") || key.equals("NUM_ISLANDS") || title.contains("number of islands")) {
            return generateMatrixTrace(problem);
        } else if (key.equals("MERGE_INTERVALS") || title.contains("merge intervals") || title.contains("merge overlapping subintervals")) {
            return generateMergeIntervalsTrace(problem);
        } else if (key.equals("SORT_COLORS") || title.contains("sort an array of 0s, 1s and 2s") || title.contains("sort colors")) {
            return generateSortColorsTrace(problem);
        } else if (key.equals("LONGEST_SUBSTRING") || title.contains("longest substring without")) {
            return generateLongestSubstringTrace(problem);
        } else {
            return generateDynamicTrace(problem);
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

    private AnimatedExplanationResponse generateThreeSumTrace(Problem problem) {
        List<String> code = List.of(
            "public List<List<Integer>> threeSum(int[] nums) {",
            "    Arrays.sort(nums);",
            "    List<List<Integer>> res = new ArrayList<>();",
            "    for (int i = 0; i < nums.length - 2; i++) {",
            "        if (i > 0 && nums[i] == nums[i-1]) continue;",
            "        int left = i + 1, right = nums.length - 1;",
            "        while (left < right) {",
            "            int sum = nums[i] + nums[left] + nums[right];",
            "            if (sum == 0) { res.add(Arrays.asList(nums[i], nums[left++], nums[right--])); }",
            "            else if (sum < 0) left++;",
            "            else right--;",
            "        }",
            "    }",
            "    return res;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Sort Input Array")
                .description("Sort nums in ascending order: [-4, -1, -1, 0, 1, 2]. Sorting enables O(N) two-pointer scans.")
                .visualType("array").visualData("[-4, -1, -1, 0, 1, 2]")
                .pointers(Map.of("i", 0)).highlights(List.of())
                .vars(Map.of("sorted", "true")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. i = 0 (nums[0] = -4)")
                .description("Fix first element nums[0] = -4. Set left = 1 (-1), right = 5 (2).")
                .visualType("two-pointer").visualData("[-4, -1, -1, 0, 1, 2]")
                .pointers(Map.of("i", 0, "left", 1, "right", 5)).highlights(List.of(0, 1, 5))
                .vars(Map.of("nums[i]", "-4", "nums[left]", "-1", "nums[right]", "2", "sum", "-3")).codeLines(List.of(4, 6, 8)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Sum = -3 < 0: Increment left")
                .description("sum = -3 < 0. Need larger sum -> increment left pointer to 2 (-1).")
                .visualType("two-pointer").visualData("[-4, -1, -1, 0, 1, 2]")
                .pointers(Map.of("i", 0, "left", 2, "right", 5)).highlights(List.of(0, 2, 5))
                .vars(Map.of("sum", "-3", "action", "left++")).codeLines(List.of(10)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Advance to i = 1 (nums[1] = -1)")
                .description("Fix nums[1] = -1. Set left = 2 (-1), right = 5 (2).")
                .visualType("two-pointer").visualData("[-4, -1, -1, 0, 1, 2]")
                .pointers(Map.of("i", 1, "left", 2, "right", 5)).highlights(List.of(1, 2, 5))
                .vars(Map.of("nums[i]", "-1", "nums[left]", "-1", "nums[right]", "2")).codeLines(List.of(4, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Triplet Found! [-1, -1, 2]")
                .description("sum = (-1) + (-1) + 2 = 0! Match found! Record triplet [-1, -1, 2]. Advance left=3, right=4.")
                .visualType("two-pointer").visualData("[-4, -1, -1, 0, 1, 2]")
                .pointers(Map.of("i", 1, "left", 2, "right", 5)).highlights(List.of(1, 2, 5))
                .vars(Map.of("triplet", "[-1, -1, 2]", "sum", "0", "status", "FOUND")).codeLines(List.of(8, 9)).code(code).durationMs(2600).build(),

            AnimationStep.builder().step(6).title("6. Check Next Pair: left = 3 (0), right = 4 (1)")
                .description("sum = (-1) + 0 + 1 = 0! Another valid triplet found: [-1, 0, 1]!")
                .visualType("two-pointer").visualData("[-4, -1, -1, 0, 1, 2]")
                .pointers(Map.of("i", 1, "left", 3, "right", 4)).highlights(List.of(1, 3, 4))
                .vars(Map.of("triplet", "[-1, 0, 1]", "sum", "0", "status", "FOUND")).codeLines(List.of(8, 9)).code(code).durationMs(2600).build(),

            AnimationStep.builder().step(7).title("7. Return Solution Triplets")
                .description("Result: [[-1, -1, 2], [-1, 0, 1]]. Completed in O(N^2) time and O(1) extra space.")
                .visualType("two-pointer").visualData("[-4, -1, -1, 0, 1, 2]")
                .highlights(List.of(1, 2, 3, 4, 5))
                .vars(Map.of("res", "[[-1,-1,2], [-1,0,1]]", "status", "COMPLETE")).codeLines(List.of(14)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Sort array and two-pointer scan for each fixed element in O(N^2) time.")
            .exampleInput("nums = [-1, 0, 1, 2, -1, -4]")
            .exampleOutput("[[-1, -1, 2], [-1, 0, 1]]")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateMaxSubarrayTrace(Problem problem) {
        List<String> code = List.of(
            "public int maxSubArray(int[] nums) {",
            "    int currentSum = nums[0];",
            "    int maxSum = nums[0];",
            "    for (int i = 1; i < nums.length; i++) {",
            "        currentSum = Math.max(nums[i], currentSum + nums[i]);",
            "        maxSum = Math.max(maxSum, currentSum);",
            "    }",
            "    return maxSum;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Kadane's Invariants")
                .description("Initialize currentSum = nums[0] = -2, maxSum = -2.")
                .visualType("array").visualData("[-2, 1, -3, 4, -1, 2, 1, -5, 4]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("currentSum", "-2", "maxSum", "-2")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. i = 1 (nums[1] = 1): Reset or Extend?")
                .description("max(1, -2 + 1 = -1) = 1. Start fresh subarray at index 1! Update maxSum = 1.")
                .visualType("array").visualData("[-2, 1, -3, 4, -1, 2, 1, -5, 4]")
                .pointers(Map.of("i", 1)).highlights(List.of(1))
                .vars(Map.of("nums[i]", "1", "currentSum", "1", "maxSum", "1")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. i = 2 (nums[2] = -3)")
                .description("currentSum = max(-3, 1 - 3 = -2) = -2. maxSum remains 1.")
                .visualType("array").visualData("[-2, 1, -3, 4, -1, 2, 1, -5, 4]")
                .pointers(Map.of("i", 2)).highlights(List.of(2))
                .vars(Map.of("nums[i]", "-3", "currentSum", "-2", "maxSum", "1")).codeLines(List.of(5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. i = 3 (nums[3] = 4): New Best Start!")
                .description("max(4, -2 + 4 = 2) = 4. Start new subarray! Update maxSum = 4.")
                .visualType("array").visualData("[-2, 1, -3, 4, -1, 2, 1, -5, 4]")
                .pointers(Map.of("i", 3)).highlights(List.of(3))
                .vars(Map.of("nums[i]", "4", "currentSum", "4", "maxSum", "4")).codeLines(List.of(5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. i = 4 (nums[4] = -1) & i = 5 (nums[5] = 2)")
                .description("currentSum = 4 - 1 = 3. Next step: currentSum = 3 + 2 = 5! Update maxSum = 5.")
                .visualType("array").visualData("[-2, 1, -3, 4, -1, 2, 1, -5, 4]")
                .pointers(Map.of("i", 5)).highlights(List.of(3, 4, 5))
                .vars(Map.of("currentSum", "5", "maxSum", "5")).codeLines(List.of(5, 6)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(6).title("6. i = 6 (nums[6] = 1): Peak Subarray Sum = 6")
                .description("currentSum = 5 + 1 = 6. Update maxSum = 6! Best subarray is [4, -1, 2, 1].")
                .visualType("array").visualData("[-2, 1, -3, 4, -1, 2, 1, -5, 4]")
                .pointers(Map.of("i", 6)).highlights(List.of(3, 4, 5, 6))
                .vars(Map.of("currentSum", "6", "maxSum", "6", "status", "PEAK")).codeLines(List.of(5, 6)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(7).title("7. Return Optimal Sum = 6")
                .description("Remaining iterations do not exceed 6. Maximum subarray sum is 6 in O(N) time and O(1) space.")
                .visualType("array").visualData("[-2, 1, -3, 4, -1, 2, 1, -5, 4]")
                .highlights(List.of(3, 4, 5, 6))
                .vars(Map.of("result", "6", "subarray", "[4, -1, 2, 1]", "time", "O(N)", "space", "O(1)")).codeLines(List.of(8)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Kadane's Algorithm linear scan tracking max subarray sum in O(N) time.")
            .exampleInput("nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]")
            .exampleOutput("6")
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
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 0, "right", 8)).highlights(List.of(0, 8))
                .vars(Map.of("left", "0", "right", "8", "maxArea", "0")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Iteration 1: Compute Area for (0, 8)")
                .description("width = 8 - 0 = 8, h = min(1, 7) = 1. area = 8 * 1 = 8. Update maxArea = 8.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 0, "right", 8)).highlights(List.of(0, 8))
                .vars(Map.of("width", "8", "minHeight", "1", "area", "8", "maxArea", "8")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Advance Shorter Boundary (left)")
                .description("height[0] (1) < height[8] (7). Increment left from 0 to 1.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("left", "1", "right", "8", "maxArea", "8")).codeLines(List.of(8)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Iteration 2: Compute Area for (1, 8)")
                .description("width = 8 - 1 = 7, h = min(8, 7) = 7. area = 7 * 7 = 49!")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("width", "7", "minHeight", "7", "area", "49", "maxArea", "8")).codeLines(List.of(5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Update Maximum Area: maxArea = 49")
                .description("New best area! maxArea = max(8, 49) = 49.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 8)).highlights(List.of(1, 8))
                .vars(Map.of("maxArea", "49", "status", "NEW_MAX")).codeLines(List.of(7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Decrement Shorter Boundary (right)")
                .description("height[8] (7) <= height[1] (8). Decrement right from 8 to 7.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 7)).highlights(List.of(1, 7))
                .vars(Map.of("left", "1", "right", "7", "maxArea", "49")).codeLines(List.of(9)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Iteration 3: Check (1, 7)")
                .description("width = 7 - 1 = 6, h = min(8, 3) = 3. area = 18 < 49. maxArea remains 49.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 7)).highlights(List.of(1, 7))
                .vars(Map.of("width", "6", "minHeight", "3", "area", "18", "maxArea", "49")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(8).title("8. Decrement Shorter Boundary to index 6")
                .description("height[7] (3) < height[1] (8). Decrement right from 7 to 6.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 6)).highlights(List.of(1, 6))
                .vars(Map.of("left", "1", "right", "6", "maxArea", "49")).codeLines(List.of(9)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(9).title("9. Iteration 4: Check (1, 6)")
                .description("width = 6 - 1 = 5, h = min(8, 8) = 8. area = 5 * 8 = 40 < 49. maxArea remains 49.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
                .pointers(Map.of("left", 1, "right", 6)).highlights(List.of(1, 6))
                .vars(Map.of("width", "5", "minHeight", "8", "area", "40", "maxArea", "49")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(10).title("10. Termination & Return maxArea = 49")
                .description("All remaining windows yield smaller widths. Optimal capacity is 49. Complete in O(N) time and O(1) space.")
                .visualType("bars").visualData("[1, 8, 6, 2, 5, 4, 8, 3, 7]")
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

    private AnimatedExplanationResponse generateTrappingRainWaterTrace(Problem problem) {
        List<String> code = List.of(
            "public int trap(int[] height) {",
            "    int left = 0, right = height.length - 1;",
            "    int leftMax = 0, rightMax = 0, water = 0;",
            "    while (left < right) {",
            "        if (height[left] < height[right]) {",
            "            if (height[left] >= leftMax) leftMax = height[left];",
            "            else water += leftMax - height[left];",
            "            left++;",
            "        } else {",
            "            if (height[right] >= rightMax) rightMax = height[right];",
            "            else water += rightMax - height[right];",
            "            right--;",
            "        }",
            "    }",
            "    return water;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Invariants & Pointers")
                .description("Initialize left = 0, right = 11, leftMax = 0, rightMax = 0, water = 0.")
                .visualType("bars").visualData("[0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]")
                .pointers(Map.of("left", 0, "right", 11)).highlights(List.of(0, 11))
                .vars(Map.of("water", "0", "leftMax", "0", "rightMax", "0")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Step 1: height[0] = 0 < height[11] = 1")
                .description("leftMax updated to 0. Advance left pointer to 1.")
                .visualType("bars").visualData("[0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]")
                .pointers(Map.of("left", 1, "right", 11)).highlights(List.of(1, 11))
                .vars(Map.of("leftMax", "0", "water", "0")).codeLines(List.of(5, 6, 8)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Step 2: height[1] = 1 >= leftMax (0)")
                .description("Update leftMax = 1. Advance left to index 2 (height 0).")
                .visualType("bars").visualData("[0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]")
                .pointers(Map.of("left", 2, "right", 11)).highlights(List.of(2, 11))
                .vars(Map.of("leftMax", "1", "water", "0")).codeLines(List.of(6, 8)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Trap Water at Index 2!")
                .description("height[2] (0) < leftMax (1). Trap water: 1 - 0 = 1 unit! Total water = 1.")
                .visualType("bars").visualData("[0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]")
                .pointers(Map.of("left", 2, "right", 11)).highlights(List.of(2))
                .vars(Map.of("trappedHere", "1", "water", "1", "leftMax", "1")).codeLines(List.of(7, 8)).code(code).durationMs(2500).build(),

            AnimationStep.builder().step(5).title("5. Pointers Converge through Center Peaks")
                .description("Inward scan traps water across troughs at indices 4, 5, 6, 9. Total trapped water reaches 6.")
                .visualType("bars").visualData("[0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]")
                .pointers(Map.of("left", 7, "right", 8)).highlights(List.of(7, 8))
                .vars(Map.of("leftMax", "3", "rightMax", "2", "water", "6")).codeLines(List.of(4, 5)).code(code).durationMs(2500).build(),

            AnimationStep.builder().step(6).title("6. Complete Execution & Return water = 6")
                .description("Total water trapped is 6 units. Complete in optimal O(N) time and O(1) space.")
                .visualType("bars").visualData("[0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]")
                .highlights(List.of(7))
                .vars(Map.of("result", "6", "status", "OPTIMAL", "time", "O(N)", "space", "O(1)")).codeLines(List.of(15)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Two-pointer inward traversal computing trapped water in O(N) time and O(1) space.")
            .exampleInput("height = [0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]")
            .exampleOutput("6")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateGroupAnagramsTrace(Problem problem) {
        List<String> code = List.of(
            "public List<List<String>> groupAnagrams(String[] strs) {",
            "    Map<String, List<String>> map = new HashMap<>();",
            "    for (String s : strs) {",
            "        char[] ca = s.toCharArray();",
            "        Arrays.sort(ca);",
            "        String key = String.valueOf(ca);",
            "        map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);",
            "    }",
            "    return new ArrayList<>(map.values());",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Categorization Map")
                .description("Initialize empty HashMap to group strings by their sorted character signature.")
                .visualType("array").visualData("[\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
                .pointers(Map.of("i", 0)).highlights(List.of())
                .vars(Map.of("map", "{}")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Process \"eat\" -> key: \"aet\"")
                .description("Sort chars of \"eat\" -> \"aet\". Insert \"eat\" under bucket \"aet\".")
                .visualType("array").visualData("[\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("word", "eat", "key", "aet", "map", "{aet: [eat]}")).codeLines(List.of(3, 4, 5, 6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Process \"tea\" -> key: \"aet\"")
                .description("Sort \"tea\" -> \"aet\". Bucket exists! Add \"tea\" to \"aet\" group: [\"eat\", \"tea\"].")
                .visualType("array").visualData("[\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
                .pointers(Map.of("i", 1)).highlights(List.of(1))
                .vars(Map.of("word", "tea", "key", "aet", "map", "{aet: [eat, tea]}")).codeLines(List.of(6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Process \"tan\" -> key: \"ant\"")
                .description("Sort \"tan\" -> \"ant\". Create new group for \"ant\": [\"tan\"].")
                .visualType("array").visualData("[\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
                .pointers(Map.of("i", 2)).highlights(List.of(2))
                .vars(Map.of("word", "tan", "key", "ant", "map", "{aet: [eat, tea], ant: [tan]}")).codeLines(List.of(6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Process \"ate\" -> key: \"aet\"")
                .description("Sort \"ate\" -> \"aet\". Add to \"aet\" group: [\"eat\", \"tea\", \"ate\"].")
                .visualType("array").visualData("[\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
                .pointers(Map.of("i", 3)).highlights(List.of(3))
                .vars(Map.of("word", "ate", "key", "aet", "map", "{aet: [eat, tea, ate]}")).codeLines(List.of(6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Process \"nat\" and \"bat\"")
                .description("\"nat\" joins \"ant\". \"bat\" creates [\"bat\"]. All strings grouped.")
                .visualType("array").visualData("[\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
                .pointers(Map.of("i", 5)).highlights(List.of(4, 5))
                .vars(Map.of("status", "ALL_GROUPED", "map", "{aet: 3, ant: 2, abt: 1}")).codeLines(List.of(6, 7)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(7).title("7. Return Grouped Anagrams")
                .description("Return map.values(): [[\"bat\"], [\"nat\", \"tan\"], [\"ate\", \"eat\", \"tea\"]]. Time: O(N * K log K).")
                .visualType("array").visualData("[\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
                .highlights(List.of(0, 1, 2, 3, 4, 5))
                .vars(Map.of("groups", "3", "status", "SUCCESS")).codeLines(List.of(9)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Group anagrams using sorted character keys in HashMap.")
            .exampleInput("strs = [\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]")
            .exampleOutput("[[\"bat\"], [\"nat\", \"tan\"], [\"ate\", \"eat\", \"tea\"]]")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateTopKFrequentTrace(Problem problem) {
        List<String> code = List.of(
            "public int[] topKFrequent(int[] nums, int k) {",
            "    Map<Integer, Integer> count = new HashMap<>();",
            "    for (int n : nums) count.put(n, count.getOrDefault(n, 0) + 1);",
            "    PriorityQueue<Integer> heap = new PriorityQueue<>((a, b) -> count.get(a) - count.get(b));",
            "    for (int n : count.keySet()) {",
            "        heap.add(n);",
            "        if (heap.size() > k) heap.poll();",
            "    }",
            "    int[] res = new int[k];",
            "    for (int i = k - 1; i >= 0; i--) res[i] = heap.poll();",
            "    return res;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Frequency Counting with Map")
                .description("nums = [1, 1, 1, 2, 2, 3], k = 2. Count frequency of each number.")
                .visualType("array").visualData("[1, 1, 1, 2, 2, 3]")
                .pointers(Map.of("i", 0)).highlights(List.of(0, 1, 2))
                .vars(Map.of("map", "{1: 3, 2: 2, 3: 1}")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Maintain Min-Heap of Size k = 2")
                .description("Insert keys into Min-Heap. Size limited to k = 2 to keep only highest frequencies.")
                .visualType("array").visualData("[1, 2, 3]")
                .pointers(Map.of("k", 2)).highlights(List.of(0, 1))
                .vars(Map.of("heap", "[2, 1]", "k", "2")).codeLines(List.of(4, 5, 6, 7)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(3).title("3. Return Top 2 Elements [1, 2]")
                .description("Extract top k frequent numbers: [1, 2]. Completed in O(N log K) time and O(N) space.")
                .visualType("array").visualData("[1, 2]")
                .highlights(List.of(0, 1))
                .vars(Map.of("result", "[1, 2]", "status", "SUCCESS")).codeLines(List.of(9, 10, 11)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Frequency map with min-heap tracking top K elements in O(N log K) time.")
            .exampleInput("nums = [1, 1, 1, 2, 2, 3], k = 2")
            .exampleOutput("[1, 2]")
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

    private AnimatedExplanationResponse generateInvertTreeTrace(Problem problem) {
        List<String> code = List.of(
            "public TreeNode invertTree(TreeNode root) {",
            "    if (root == null) return null;",
            "    TreeNode temp = root.left;",
            "    root.left = root.right;",
            "    root.right = temp;",
            "    invertTree(root.left);",
            "    invertTree(root.right);",
            "    return root;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Inspect Root (4)")
                .description("Start at root 4. Subtrees: left = 2, right = 7.")
                .visualType("tree").visualData("[\"4\",\"2\",\"7\",\"1\",\"3\",\"6\",\"9\"]")
                .vars(Map.of("root", "4", "left", "2", "right", "7")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Swap Root Children")
                .description("Swap root.left and root.right. Left becomes 7, right becomes 2.")
                .visualType("tree").visualData("[\"4\",\"7\",\"2\",\"6\",\"9\",\"1\",\"3\"]")
                .vars(Map.of("newLeft", "7", "newRight", "2")).codeLines(List.of(3, 4, 5)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(3).title("3. Recursively Invert Subtrees")
                .description("Invert left subtree (7) and right subtree (2). All child leaves swapped.")
                .visualType("tree").visualData("[\"4\",\"7\",\"2\",\"9\",\"6\",\"3\",\"1\"]")
                .vars(Map.of("status", "INVERTED")).codeLines(List.of(6, 7)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(4).title("4. Return Inverted Root")
                .description("Tree fully mirrored: [4, 7, 2, 9, 6, 3, 1] in O(N) time and O(H) space.")
                .visualType("tree").visualData("[\"4\",\"7\",\"2\",\"9\",\"6\",\"3\",\"1\"]")
                .vars(Map.of("result", "[4,7,2,9,6,3,1]")).codeLines(List.of(8)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Recursive DFS mirror inversion in O(N) time and O(H) call stack space.")
            .exampleInput("root = [4, 2, 7, 1, 3, 6, 9]")
            .exampleOutput("[4, 7, 2, 9, 6, 3, 1]")
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

    private AnimatedExplanationResponse generateMergeTwoSortedListsTrace(Problem problem) {
        List<String> code = List.of(
            "public ListNode mergeTwoLists(ListNode list1, ListNode list2) {",
            "    ListNode dummy = new ListNode(0);",
            "    ListNode curr = dummy;",
            "    while (list1 != null && list2 != null) {",
            "        if (list1.val <= list2.val) { curr.next = list1; list1 = list1.next; }",
            "        else { curr.next = list2; list2 = list2.next; }",
            "        curr = curr.next;",
            "    }",
            "    curr.next = list1 != null ? list1 : list2;",
            "    return dummy.next;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Dummy Node")
                .description("list1 = [1, 2, 4], list2 = [1, 3, 4]. Init dummy head node and curr pointer.")
                .visualType("linkedlist").visualData("[\"dummy\"]")
                .vars(Map.of("list1", "1", "list2", "1")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Compare 1 vs 1 -> Append list1 (1)")
                .description("list1.val <= list2.val. Append 1 to result list. Advance list1 to 2.")
                .visualType("linkedlist").visualData("[\"1\"]")
                .vars(Map.of("appended", "1", "nextList1", "2")).codeLines(List.of(4, 5, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Compare 2 vs 1 -> Append list2 (1)")
                .description("list2.val (1) < list1.val (2). Append 1 to result list. Advance list2 to 3.")
                .visualType("linkedlist").visualData("[\"1\", \"1\"]")
                .vars(Map.of("appended", "1", "nextList2", "3")).codeLines(List.of(6, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Compare 2 vs 3 -> Append list1 (2)")
                .description("list1.val (2) <= list2.val (3). Append 2. Advance list1 to 4.")
                .visualType("linkedlist").visualData("[\"1\", \"1\", \"2\"]")
                .vars(Map.of("appended", "2")).codeLines(List.of(5, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Complete Merge [1, 1, 2, 3, 4, 4]")
                .description("Remaining nodes merged. Return dummy.next: [1, 1, 2, 3, 4, 4] in O(N+M) time.")
                .visualType("linkedlist").visualData("[\"1\", \"1\", \"2\", \"3\", \"4\", \"4\"]")
                .vars(Map.of("result", "[1, 1, 2, 3, 4, 4]", "status", "SUCCESS")).codeLines(List.of(9, 10)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Two-pointer linear splice merging sorted lists in O(N+M) time and O(1) extra space.")
            .exampleInput("list1 = [1, 2, 4], list2 = [1, 3, 4]")
            .exampleOutput("[1, 1, 2, 3, 4, 4]")
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

    private AnimatedExplanationResponse generateMergeIntervalsTrace(Problem problem) {
        List<String> code = List.of(
            "public int[][] merge(int[][] intervals) {",
            "    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));",
            "    List<int[]> merged = new ArrayList<>();",
            "    for (int[] interval : intervals) {",
            "        if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < interval[0]) {",
            "            merged.add(interval);",
            "        } else {",
            "            merged.get(merged.size() - 1)[1] = Math.max(merged.get(merged.size() - 1)[1], interval[1]);",
            "        }",
            "    }",
            "    return merged.toArray(new int[merged.size()][]);",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Sort Intervals by Start Time")
                .description("intervals = [[1,3],[2,6],[8,10],[15,18]]. Sort intervals by start time.")
                .visualType("matrix").visualData("[[1,3],[2,6],[8,10],[15,18]]")
                .vars(Map.of("status", "SORTED")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Insert First Interval [1, 3]")
                .description("merged list is empty. Add [1, 3] to merged.")
                .visualType("matrix").visualData("[[1,3]]")
                .vars(Map.of("currentMerged", "[[1,3]]")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Compare [1, 3] with [2, 6] -> Overlap!")
                .description("interval[0] (2) <= lastMerged[1] (3). Overlap detected! Merge into [1, max(3, 6)] = [1, 6].")
                .visualType("matrix").visualData("[[1,6]]")
                .vars(Map.of("mergedInterval", "[1,6]")).codeLines(List.of(7, 8)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(4).title("4. Compare [1, 6] with [8, 10] -> No Overlap")
                .description("8 > 6. Disjoint interval! Append [8, 10] as new non-overlapping range.")
                .visualType("matrix").visualData("[[1,6],[8,10]]")
                .vars(Map.of("currentMerged", "[[1,6],[8,10]]")).codeLines(List.of(5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Compare [8, 10] with [15, 18] -> Append")
                .description("15 > 10. Disjoint interval! Append [15, 18]. All intervals processed.")
                .visualType("matrix").visualData("[[1,6],[8,10],[15,18]]")
                .vars(Map.of("result", "[[1,6],[8,10],[15,18]]", "status", "SUCCESS")).codeLines(List.of(5, 6, 11)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Sort by start times and single-pass interval consolidation in O(N log N) time.")
            .exampleInput("intervals = [[1,3],[2,6],[8,10],[15,18]]")
            .exampleOutput("[[1,6],[8,10],[15,18]]")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateSortColorsTrace(Problem problem) {
        List<String> code = List.of(
            "public void sortColors(int[] nums) {",
            "    int low = 0, mid = 0, high = nums.length - 1;",
            "    while (mid <= high) {",
            "        if (nums[mid] == 0) { swap(nums, low++, mid++); }",
            "        else if (nums[mid] == 1) { mid++; }",
            "        else { swap(nums, mid, high--); }",
            "    }",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize 3-Way Partition Pointers")
                .description("Dutch National Flag algorithm: low = 0, mid = 0, high = 5. nums = [2, 0, 2, 1, 1, 0].")
                .visualType("array").visualData("[2, 0, 2, 1, 1, 0]")
                .pointers(Map.of("low", 0, "mid", 0, "high", 5)).highlights(List.of(0, 5))
                .vars(Map.of("low", "0", "mid", "0", "high", "5")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. nums[mid] == 2 -> Swap with high (0)")
                .description("nums[0] is 2. Swap nums[mid=0] with nums[high=5]. Decrement high to 4.")
                .visualType("array").visualData("[0, 0, 2, 1, 1, 2]")
                .pointers(Map.of("low", 0, "mid", 0, "high", 4)).highlights(List.of(0, 5))
                .vars(Map.of("action", "swap(0, 5)", "high", "4")).codeLines(List.of(6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. nums[mid] == 0 -> Swap with low (0)")
                .description("nums[mid=0] is 0. Swap with low (0). Increment low=1, mid=1.")
                .visualType("array").visualData("[0, 0, 2, 1, 1, 2]")
                .pointers(Map.of("low", 1, "mid", 1, "high", 4)).highlights(List.of(1))
                .vars(Map.of("low", "1", "mid", "1")).codeLines(List.of(4)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Complete In-Place Partitioning")
                .description("All 0s moved to left, 1s in center, 2s on right: [0, 0, 1, 1, 2, 2]. Completed in O(N) time and O(1) space.")
                .visualType("array").visualData("[0, 0, 1, 1, 2, 2]")
                .highlights(List.of(0, 1, 2, 3, 4, 5))
                .vars(Map.of("result", "[0, 0, 1, 1, 2, 2]", "status", "SORTED")).codeLines(List.of(7)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Dutch National Flag 3-way partition in O(N) time and O(1) space.")
            .exampleInput("nums = [2, 0, 2, 1, 1, 0]")
            .exampleOutput("[0, 0, 1, 1, 2, 2]")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateBinarySearchTrace(Problem problem) {
        List<String> code = List.of(
            "public int search(int[] nums, int target) {",
            "    int low = 0, high = nums.length - 1;",
            "    while (low <= high) {",
            "        int mid = low + (high - low) / 2;",
            "        if (nums[mid] == target) return mid;",
            "        if (nums[mid] < target) low = mid + 1;",
            "        else high = mid - 1;",
            "    }",
            "    return -1;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Search Range")
                .description("nums = [-1, 0, 3, 5, 9, 12], target = 9. low = 0, high = 5.")
                .visualType("array").visualData("[-1, 0, 3, 5, 9, 12]")
                .pointers(Map.of("low", 0, "high", 5)).highlights(List.of(0, 5))
                .vars(Map.of("low", "0", "high", "5", "target", "9")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Iteration 1: Calculate mid = 2 (nums[2] = 3)")
                .description("mid = 0 + (5 - 0) / 2 = 2. nums[2] = 3. Compare 3 with target 9.")
                .visualType("array").visualData("[-1, 0, 3, 5, 9, 12]")
                .pointers(Map.of("low", 0, "mid", 2, "high", 5)).highlights(List.of(2))
                .vars(Map.of("mid", "2", "nums[mid]", "3", "target", "9")).codeLines(List.of(4, 5)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(3).title("3. Discard Left Half: low = mid + 1 = 3")
                .description("nums[2] (3) < target (9). Target must be in right half. Set low = 3.")
                .visualType("array").visualData("[-1, 0, 3, 5, 9, 12]")
                .pointers(Map.of("low", 3, "high", 5)).highlights(List.of(3, 4, 5))
                .vars(Map.of("low", "3", "high", "5")).codeLines(List.of(6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Iteration 2: Calculate mid = 4 (nums[4] = 9)")
                .description("mid = 3 + (5 - 3) / 2 = 4. nums[4] = 9.")
                .visualType("array").visualData("[-1, 0, 3, 5, 9, 12]")
                .pointers(Map.of("low", 3, "mid", 4, "high", 5)).highlights(List.of(4))
                .vars(Map.of("mid", "4", "nums[mid]", "9", "target", "9")).codeLines(List.of(4, 5)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(5).title("5. Target Found at Index 4!")
                .description("nums[4] == target (9). Match found! Return index 4 in O(log N) time.")
                .visualType("array").visualData("[-1, 0, 3, 5, 9, 12]")
                .pointers(Map.of("mid", 4)).highlights(List.of(4))
                .vars(Map.of("result", "4", "time", "O(log N)", "status", "SUCCESS")).codeLines(List.of(5)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Binary search halving search space in O(log N) time.")
            .exampleInput("nums = [-1, 0, 3, 5, 9, 12], target = 9")
            .exampleOutput("4")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateBestTimeToBuySellStockTrace(Problem problem) {
        List<String> code = List.of(
            "public int maxProfit(int[] prices) {",
            "    int minPrice = prices[0], maxProfit = 0;",
            "    for (int p : prices) {",
            "        minPrice = Math.min(minPrice, p);",
            "        maxProfit = Math.max(maxProfit, p - minPrice);",
            "    }",
            "    return maxProfit;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Invariants")
                .description("prices = [7, 1, 5, 3, 6, 4]. Init minPrice = 7, maxProfit = 0.")
                .visualType("array").visualData("[7, 1, 5, 3, 6, 4]")
                .pointers(Map.of("day", 0)).highlights(List.of(0))
                .vars(Map.of("minPrice", "7", "maxProfit", "0")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Day 1: Price Drops to 1")
                .description("Price 1 < minPrice (7). Update minPrice = 1. Profit = 0.")
                .visualType("array").visualData("[7, 1, 5, 3, 6, 4]")
                .pointers(Map.of("day", 1)).highlights(List.of(1))
                .vars(Map.of("price", "1", "minPrice", "1", "maxProfit", "0")).codeLines(List.of(4, 5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Day 2: Price = 5")
                .description("Profit = 5 - 1 = 4. Update maxProfit = 4.")
                .visualType("array").visualData("[7, 1, 5, 3, 6, 4]")
                .pointers(Map.of("day", 2)).highlights(List.of(1, 2))
                .vars(Map.of("price", "5", "minPrice", "1", "profit", "4", "maxProfit", "4")).codeLines(List.of(4, 5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Day 4: Price Rises to 6 (Peak)")
                .description("Profit = 6 - 1 = 5 > 4. Update maxProfit = 5!")
                .visualType("array").visualData("[7, 1, 5, 3, 6, 4]")
                .pointers(Map.of("day", 4)).highlights(List.of(1, 4))
                .vars(Map.of("buyDay", "1", "sellDay", "4", "profit", "5", "maxProfit", "5")).codeLines(List.of(4, 5)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(5).title("5. Return Maximum Profit = 5")
                .description("Optimal strategy: Buy on day 1 (price 1), sell on day 4 (price 6). Return 5.")
                .visualType("array").visualData("[7, 1, 5, 3, 6, 4]")
                .highlights(List.of(1, 4))
                .vars(Map.of("result", "5", "status", "OPTIMAL", "time", "O(N)", "space", "O(1)")).codeLines(List.of(7)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("One-pass tracking of min price and max profit in O(N) time.")
            .exampleInput("prices = [7, 1, 5, 3, 6, 4]")
            .exampleOutput("5")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateContainsDuplicateTrace(Problem problem) {
        List<String> code = List.of(
            "public boolean containsDuplicate(int[] nums) {",
            "    Set<Integer> set = new HashSet<>();",
            "    for (int num : nums) {",
            "        if (set.contains(num)) return true;",
            "        set.add(num);",
            "    }",
            "    return false;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Seen Set")
                .description("Initialize empty HashSet to store visited numbers for O(1) duplicate checks.")
                .visualType("array").visualData("[1, 2, 3, 1]")
                .pointers(Map.of("i", 0)).highlights(List.of())
                .vars(Map.of("set", "{}", "num", "-")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(2).title("2. Process num = 1 (index 0)")
                .description("Read nums[0] = 1. set.contains(1) -> false. Insert 1 into set.")
                .visualType("array").visualData("[1, 2, 3, 1]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("num", "1", "inSet", "false", "set", "{1}")).codeLines(List.of(3, 4, 5)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(3).title("3. Process num = 2 (index 1)")
                .description("Read nums[1] = 2. set.contains(2) -> false. Insert 2 into set.")
                .visualType("array").visualData("[1, 2, 3, 1]")
                .pointers(Map.of("i", 1)).highlights(List.of(1))
                .vars(Map.of("num", "2", "inSet", "false", "set", "{1, 2}")).codeLines(List.of(3, 4, 5)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(4).title("4. Process num = 3 (index 2)")
                .description("Read nums[2] = 3. set.contains(3) -> false. Insert 3 into set.")
                .visualType("array").visualData("[1, 2, 3, 1]")
                .pointers(Map.of("i", 2)).highlights(List.of(2))
                .vars(Map.of("num", "3", "inSet", "false", "set", "{1, 2, 3}")).codeLines(List.of(3, 4, 5)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(5).title("5. Process num = 1 (index 3) - DUPLICATE!")
                .description("Read nums[3] = 1. set.contains(1) -> TRUE! Number 1 already exists in set.")
                .visualType("array").visualData("[1, 2, 3, 1]")
                .pointers(Map.of("i", 3)).highlights(List.of(0, 3))
                .vars(Map.of("num", "1", "inSet", "true", "duplicateFound", "true")).codeLines(List.of(4)).code(code).durationMs(2500).build(),
            AnimationStep.builder().step(6).title("6. Return true")
                .description("Duplicate detected. Return true. Completed in O(N) time and O(N) space.")
                .visualType("array").visualData("[1, 2, 3, 1]")
                .highlights(List.of(0, 3))
                .vars(Map.of("result", "true", "status", "SUCCESS")).codeLines(List.of(4)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Linear scan using HashSet lookup in O(N) time.")
            .exampleInput("nums = [1, 2, 3, 1]")
            .exampleOutput("true")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateValidAnagramTrace(Problem problem) {
        List<String> code = List.of(
            "public boolean isAnagram(String s, String t) {",
            "    if (s.length() != t.length()) return false;",
            "    int[] counts = new int[26];",
            "    for (char c : s.toCharArray()) counts[c - 'a']++;",
            "    for (char c : t.toCharArray()) {",
            "        if (--counts[c - 'a'] < 0) return false;",
            "    }",
            "    return true;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Validate String Lengths")
                .description("Check lengths: s.length() (7) == t.length() (7). Initialize frequency buffer counts[26].")
                .visualType("pointer").visualData("s=\"anagram\", t=\"nagaram\"")
                .vars(Map.of("s", "anagram", "t", "nagaram", "counts", "[0...0]")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(2).title("2. Count Frequencies for s (\"anagram\")")
                .description("Increment frequency for every char in s: a:3, n:1, g:1, r:1, m:1.")
                .visualType("pointer").visualData("{\"a\":3,\"n\":1,\"g\":1,\"r\":1,\"m\":1}")
                .vars(Map.of("counts", "{a:3, n:1, g:1, r:1, m:1}")).codeLines(List.of(4)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(3).title("3. Decrement Frequencies for t (\"nagaram\")")
                .description("Scan t: decrement each character count. Inspecting 'n', 'a', 'g' -> all counts remain >= 0.")
                .visualType("pointer").visualData("{\"a\":2,\"n\":0,\"g\":0,\"r\":1,\"m\":1}")
                .vars(Map.of("processedChars", "\"nag\"", "allCountsValid", "true")).codeLines(List.of(5, 6)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(4).title("4. Process Remaining Chars (\"aram\")")
                .description("Decrement for remaining chars 'a', 'r', 'a', 'm'. All counts balance perfectly to 0.")
                .visualType("pointer").visualData("{\"a\":0,\"n\":0,\"g\":0,\"r\":0,\"m\":0}")
                .vars(Map.of("finalCounts", "all zero", "status", "BALANCED")).codeLines(List.of(5, 6)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(5).title("5. Return true")
                .description("All character counts balanced. 'nagaram' is a valid anagram of 'anagram'. Return true.")
                .visualType("pointer").visualData("true")
                .vars(Map.of("result", "true", "time", "O(N)", "space", "O(1)")).codeLines(List.of(8)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Character frequency balancing in O(N) time and O(1) extra space.")
            .exampleInput("s = \"anagram\", t = \"nagaram\"")
            .exampleOutput("true")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateValidPalindromeTrace(Problem problem) {
        List<String> code = List.of(
            "public boolean isPalindrome(String s) {",
            "    int left = 0, right = s.length() - 1;",
            "    while (left < right) {",
            "        while (left < right && !Character.isLetterOrDigit(s.charAt(left))) left++;",
            "        while (left < right && !Character.isLetterOrDigit(s.charAt(right))) right--;",
            "        if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) return false;",
            "        left++; right--;",
            "    }",
            "    return true;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Two Pointers")
                .description("Set left pointer at start (index 0) and right pointer at end of string.")
                .visualType("pointer").visualData("A man, a plan, a canal: Panama")
                .pointers(Map.of("left", 0, "right", 29))
                .vars(Map.of("left", "0", "right", "29", "s[left]", "A", "s[right]", "a")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(2).title("2. Compare Outer Chars ('A' vs 'a')")
                .description("Convert to lowercase: 'a' == 'a'. Characters match! Advance left to 1, decrement right to 28.")
                .visualType("pointer").visualData("A man, a plan, a canal: Panama")
                .pointers(Map.of("left", 1, "right", 28))
                .vars(Map.of("left", "1", "right", "28", "matched", "true")).codeLines(List.of(6, 7)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(3).title("3. Skip Non-Alphanumeric Characters")
                .description("Skip space at index 1 -> left = 2 ('m'). Skip space at 28 -> right = 27 ('m').")
                .visualType("pointer").visualData("A man, a plan, a canal: Panama")
                .pointers(Map.of("left", 2, "right", 27))
                .vars(Map.of("left", "2", "right", "27", "s[left]", "m", "s[right]", "m")).codeLines(List.of(4, 5)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(4).title("4. Compare Center Substrings")
                .description("Pointers advance comparing 'a', 'n', 'a', 'p', 'l', 'a', 'n'... all alphanumeric characters match.")
                .visualType("pointer").visualData("A man, a plan, a canal: Panama")
                .pointers(Map.of("left", 14, "right", 15))
                .vars(Map.of("status", "INWARD_CONVERGENCE", "matches", "ALL_VALID")).codeLines(List.of(6, 7)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(5).title("5. Pointers Meet & Return true")
                .description("Pointers cross without any mismatch. The string is a valid palindrome. Return true in O(N) time.")
                .visualType("pointer").visualData("true")
                .vars(Map.of("result", "true", "time", "O(N)", "space", "O(1)")).codeLines(List.of(9)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Two-pointer inward convergence in O(N) time and O(1) space.")
            .exampleInput("s = \"A man, a plan, a canal: Panama\"")
            .exampleOutput("true")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateLongestPalindromeTrace(Problem problem) {
        List<String> code = List.of(
            "public String longestPalindrome(String s) {",
            "    if (s == null || s.length() < 1) return \"\";",
            "    int start = 0, end = 0;",
            "    for (int i = 0; i < s.length(); i++) {",
            "        int len1 = expandAroundCenter(s, i, i);",
            "        int len2 = expandAroundCenter(s, i, i + 1);",
            "        int len = Math.max(len1, len2);",
            "        if (len > end - start) {",
            "            start = i - (len - 1) / 2;",
            "            end = i + len / 2;",
            "        }",
            "    }",
            "    return s.substring(start, end + 1);",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Invariants for \"babad\"")
                .description("Input string s = \"babad\". Initialize start = 0, end = 0. Iterate through all possible centers.")
                .visualType("pointer").visualData("babad")
                .pointers(Map.of("center", 0)).highlights(List.of(0))
                .vars(Map.of("s", "babad", "start", "0", "end", "0", "longest", "\"b\""))
                .codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Center i = 0 (char 'b')")
                .description("Odd center at index 0 ('b'). Cannot expand outward. Palindrome is \"b\" (length 1).")
                .visualType("pointer").visualData("babad")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("center", "0", "len", "1", "longest", "\"b\""))
                .codeLines(List.of(4, 5, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Center i = 1 (char 'a') - Expand!")
                .description("Odd center at index 1 ('a'). Expand outward: left = 0 ('b') and right = 2 ('b').")
                .visualType("pointer").visualData("babad")
                .pointers(Map.of("left", 0, "center", 1, "right", 2)).highlights(List.of(0, 1, 2))
                .vars(Map.of("s[left]", "b", "s[right]", "b", "match", "true"))
                .codeLines(List.of(5)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(4).title("4. Palindrome \"bab\" Found (Length 3)")
                .description("Characters match ('b' == 'b')! Palindrome is \"bab\" (length 3 > 1). Update start = 0, end = 2.")
                .visualType("pointer").visualData("babad")
                .pointers(Map.of("start", 0, "end", 2)).highlights(List.of(0, 1, 2))
                .vars(Map.of("start", "0", "end", "2", "longest", "\"bab\"", "maxLen", "3"))
                .codeLines(List.of(8, 9, 10)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(5).title("5. Center i = 2 (char 'b') - Expand to \"aba\"")
                .description("Expand around index 2 ('b'): left = 1 ('a'), right = 3 ('a'). Match 'a' == 'a' yields \"aba\" (length 3 <= current max).")
                .visualType("pointer").visualData("babad")
                .pointers(Map.of("left", 1, "center", 2, "right", 3)).highlights(List.of(1, 2, 3))
                .vars(Map.of("candidate", "\"aba\"", "len", "3", "longest", "\"bab\""))
                .codeLines(List.of(5, 7)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(6).title("6. Center i = 3 (char 'a') - Mismatch")
                .description("Expand around index 3: left = 2 ('b'), right = 4 ('d'). 'b' != 'd' (mismatch).")
                .visualType("pointer").visualData("babad")
                .pointers(Map.of("left", 2, "center", 3, "right", 4)).highlights(List.of(2, 4))
                .vars(Map.of("s[2]", "b", "s[4]", "d", "match", "false"))
                .codeLines(List.of(5)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(7).title("7. Return Longest Palindromic Substring \"bab\"")
                .description("All centers inspected. Return s.substring(0, 3) = \"bab\" in O(N^2) time and O(1) space.")
                .visualType("pointer").visualData("bab")
                .highlights(List.of(0, 1, 2))
                .vars(Map.of("result", "\"bab\"", "status", "SUCCESS", "time", "O(N^2)", "space", "O(1)"))
                .codeLines(List.of(13)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Expand around center in O(N^2) time and O(1) space to find longest palindromic substring.")
            .exampleInput("s = \"babad\"")
            .exampleOutput("\"bab\"")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateValidParenthesesTrace(Problem problem) {
        List<String> code = List.of(
            "public boolean isValid(String s) {",
            "    Stack<Character> stack = new Stack<>();",
            "    for (char c : s.toCharArray()) {",
            "        if (c == '(' || c == '{' || c == '[') stack.push(c);",
            "        else if (stack.isEmpty() || !matches(stack.pop(), c)) return false;",
            "    }",
            "    return stack.isEmpty();",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Bracket Stack")
                .description("Initialize empty Stack<Character> for tracking opening brackets.")
                .visualType("stack").visualData("[]")
                .vars(Map.of("s", "()[]{}", "stack", "[]")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(2).title("2. Push '(' and Match ')'")
                .description("Encounter '(': push to stack. Next char ')': pop '(' which matches closing ')'.")
                .visualType("stack").visualData("[\"(\"]")
                .vars(Map.of("read", "\")\"", "popped", "\"(\"", "matched", "true")).codeLines(List.of(4, 5)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(3).title("3. Push '[' and Match ']'")
                .description("Encounter '[': push to stack. Next char ']': pop '[' which matches closing ']'.")
                .visualType("stack").visualData("[\"[\"]")
                .vars(Map.of("read", "\"]\"", "popped", "\"[\"", "matched", "true")).codeLines(List.of(4, 5)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(4).title("4. Push '{' and Match '}'")
                .description("Encounter '{': push to stack. Next char '}': pop '{' which matches closing '}'.")
                .visualType("stack").visualData("[\"{\"]")
                .vars(Map.of("read", "\"}\"", "popped", "\"{\"", "matched", "true")).codeLines(List.of(4, 5)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(5).title("5. Stack is Empty -> Return true")
                .description("All brackets matched and stack is empty. String is valid. Return true in O(N) time.")
                .visualType("stack").visualData("[]")
                .vars(Map.of("stackEmpty", "true", "result", "true")).codeLines(List.of(7)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("LIFO Stack matching of brackets in O(N) time and O(N) space.")
            .exampleInput("s = \"()[]{}\"")
            .exampleOutput("true")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateMinStackTrace(Problem problem) {
        List<String> code = List.of(
            "class MinStack {",
            "    private Stack<Integer> stack = new Stack<>();",
            "    private Stack<Integer> minStack = new Stack<>();",
            "    public void push(int val) {",
            "        stack.push(val);",
            "        if (minStack.isEmpty() || val <= minStack.peek()) minStack.push(val);",
            "    }",
            "    public void pop() {",
            "        if (stack.pop().equals(minStack.peek())) minStack.pop();",
            "    }",
            "    public int getMin() { return minStack.peek(); }",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Main and Min Stack")
                .description("Create primary stack and parallel minStack maintaining minimum at each depth.")
                .visualType("stack").visualData("[]")
                .vars(Map.of("stack", "[]", "minStack", "[]")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Push -2")
                .description("stack.push(-2). minStack.push(-2). Current min = -2.")
                .visualType("stack").visualData("[-2]")
                .vars(Map.of("pushed", "-2", "currentMin", "-2")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. Push 0")
                .description("stack.push(0). 0 > -2 so minStack does not update. Current min remains -2.")
                .visualType("stack").visualData("[-2, 0]")
                .vars(Map.of("pushed", "0", "currentMin", "-2")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. Push -3")
                .description("stack.push(-3). -3 <= -2 -> minStack.push(-3). New min = -3!")
                .visualType("stack").visualData("[-2, 0, -3]")
                .vars(Map.of("pushed", "-3", "currentMin", "-3")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Pop Top Element (-3)")
                .description("Popped -3. Since it was the minimum, minStack also pops. Current min reverts to -2.")
                .visualType("stack").visualData("[-2, 0]")
                .vars(Map.of("popped", "-3", "currentMin", "-2")).codeLines(List.of(8, 9)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(6).title("6. getMin() Returns -2")
                .description("minStack.peek() returns -2 in guaranteed O(1) time.")
                .visualType("stack").visualData("[-2, 0]")
                .vars(Map.of("min", "-2", "time", "O(1)")).codeLines(List.of(11)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Two synchronized stacks supporting O(1) push, pop, and getMin.")
            .exampleInput("push(-2) -> push(0) -> push(-3) -> getMin() -> pop() -> getMin()")
            .exampleOutput("-3, -2")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateClimbingStairsTrace(Problem problem) {
        List<String> code = List.of(
            "public int climbStairs(int n) {",
            "    if (n <= 2) return n;",
            "    int prev2 = 1, prev1 = 2;",
            "    for (int i = 3; i <= n; i++) {",
            "        int curr = prev1 + prev2;",
            "        prev2 = prev1;",
            "        prev1 = curr;",
            "    }",
            "    return prev1;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Base Cases (Step 1 & Step 2)")
                .description("n = 5 stairs. Base cases: 1 stair = 1 way. 2 stairs = 2 ways. prev2 = 1, prev1 = 2.")
                .visualType("array").visualData("[1, 2, 0, 0, 0]")
                .pointers(Map.of("step", 2)).highlights(List.of(0, 1))
                .vars(Map.of("step1", "1", "step2", "2")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(2).title("2. Step 3: ways = 1 + 2 = 3")
                .description("Step 3 can be reached from step 2 (1 step) or step 1 (2 steps). curr = 2 + 1 = 3.")
                .visualType("array").visualData("[1, 2, 3, 0, 0]")
                .pointers(Map.of("step", 3)).highlights(List.of(2))
                .vars(Map.of("i", "3", "curr", "3", "prev2", "2", "prev1", "3")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(3).title("3. Step 4: ways = 2 + 3 = 5")
                .description("Step 4 can be reached from step 3 or step 2. curr = 3 + 2 = 5.")
                .visualType("array").visualData("[1, 2, 3, 5, 0]")
                .pointers(Map.of("step", 4)).highlights(List.of(3))
                .vars(Map.of("i", "4", "curr", "5", "prev2", "3", "prev1", "5")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(4).title("4. Step 5: ways = 3 + 5 = 8")
                .description("Step 5 can be reached from step 4 or step 3. curr = 5 + 3 = 8.")
                .visualType("array").visualData("[1, 2, 3, 5, 8]")
                .pointers(Map.of("step", 5)).highlights(List.of(4))
                .vars(Map.of("i", "5", "curr", "8", "finalWays", "8")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(5).title("5. Return 8 Distinct Ways")
                .description("Total distinct ways to climb 5 stairs is 8 (Fibonacci sequence). Return 8.")
                .visualType("array").visualData("[1, 2, 3, 5, 8]")
                .highlights(List.of(4))
                .vars(Map.of("result", "8", "time", "O(N)", "space", "O(1)")).codeLines(List.of(9)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Dynamic programming Fibonacci state transition in O(N) time and O(1) space.")
            .exampleInput("n = 5")
            .exampleOutput("8")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateCoinChangeTrace(Problem problem) {
        List<String> code = List.of(
            "public int coinChange(int[] coins, int amount) {",
            "    int[] dp = new int[amount + 1];",
            "    Arrays.fill(dp, amount + 1);",
            "    dp[0] = 0;",
            "    for (int i = 1; i <= amount; i++) {",
            "        for (int coin : coins) {",
            "            if (coin <= i) dp[i] = Math.min(dp[i], dp[i - coin] + 1);",
            "        }",
            "    }",
            "    return dp[amount] > amount ? -1 : dp[amount];",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize DP Table")
                .description("coins = [1, 2, 5], amount = 11. dp[0] = 0, all others initialized to infinity.")
                .visualType("array").visualData("[0, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12]")
                .pointers(Map.of("amount", 0)).highlights(List.of(0))
                .vars(Map.of("dp[0]", "0", "target", "11")).codeLines(List.of(1, 2, 3, 4)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. Compute Amounts 1 to 5")
                .description("dp[1]=1 (coin 1), dp[2]=1 (coin 2), dp[3]=2 (2+1), dp[4]=2 (2+2), dp[5]=1 (coin 5).")
                .visualType("array").visualData("[0, 1, 1, 2, 2, 1, 12, 12, 12, 12, 12, 12]")
                .pointers(Map.of("amount", 5)).highlights(List.of(5))
                .vars(Map.of("dp[5]", "1")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(3).title("3. Compute Amount 6 to 10")
                .description("dp[6] = 1 + dp[5] = 2. dp[10] = 1 + dp[5] = 2 (two 5-coins).")
                .visualType("array").visualData("[0, 1, 1, 2, 2, 1, 2, 2, 3, 3, 2, 12]")
                .pointers(Map.of("amount", 10)).highlights(List.of(10))
                .vars(Map.of("dp[10]", "2")).codeLines(List.of(5, 6, 7)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(4).title("4. Compute Target Amount 11: min(dp[10]+1, dp[6]+1, dp[9]+1) = 3")
                .description("dp[11] = dp[11 - 5] + 1 = dp[6] + 1 = 3 (coins 5 + 5 + 1 = 11). Return 3.")
                .visualType("array").visualData("[0, 1, 1, 2, 2, 1, 2, 2, 3, 3, 2, 3]")
                .pointers(Map.of("amount", 11)).highlights(List.of(11))
                .vars(Map.of("dp[11]", "3", "status", "OPTIMAL", "time", "O(amount * coins)")).codeLines(List.of(10)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Bottom-up dynamic programming computing min coins in O(amount * |coins|) time.")
            .exampleInput("coins = [1, 2, 5], amount = 11")
            .exampleOutput("3")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateHouseRobberTrace(Problem problem) {
        List<String> code = List.of(
            "public int rob(int[] nums) {",
            "    int rob1 = 0, rob2 = 0;",
            "    for (int n : nums) {",
            "        int temp = Math.max(n + rob1, rob2);",
            "        rob1 = rob2;",
            "        rob2 = temp;",
            "    }",
            "    return rob2;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize DP Variables")
                .description("nums = [2, 7, 9, 3, 1]. rob1 = 0 (two houses back), rob2 = 0 (previous house).")
                .visualType("array").visualData("[2, 7, 9, 3, 1]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("rob1", "0", "rob2", "0")).codeLines(List.of(1, 2)).code(code).durationMs(2400).build(),

            AnimationStep.builder().step(2).title("2. House 0 (Value 2)")
                .description("max(2 + rob1(0), rob2(0)) = 2. rob1 = 0, rob2 = 2.")
                .visualType("array").visualData("[2, 7, 9, 3, 1]")
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("rob1", "0", "rob2", "2")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(3).title("3. House 1 (Value 7)")
                .description("max(7 + rob1(0), rob2(2)) = 7. rob1 = 2, rob2 = 7.")
                .visualType("array").visualData("[2, 7, 9, 3, 1]")
                .pointers(Map.of("i", 1)).highlights(List.of(1))
                .vars(Map.of("rob1", "2", "rob2", "7")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(4).title("4. House 2 (Value 9)")
                .description("max(9 + rob1(2) = 11, rob2(7)) = 11. rob1 = 7, rob2 = 11!")
                .visualType("array").visualData("[2, 7, 9, 3, 1]")
                .pointers(Map.of("i", 2)).highlights(List.of(0, 2))
                .vars(Map.of("rob1", "7", "rob2", "11")).codeLines(List.of(4, 5, 6)).code(code).durationMs(2200).build(),

            AnimationStep.builder().step(5).title("5. Complete Scan -> Return 12")
                .description("House 3 (3+7=10 < 11 -> rob2=11). House 4 (1+11=12 > 11 -> rob2=12). Return 12.")
                .visualType("array").visualData("[2, 7, 9, 3, 1]")
                .highlights(List.of(0, 2, 4))
                .vars(Map.of("result", "12", "houses", "[2, 9, 1]", "time", "O(N)", "space", "O(1)")).codeLines(List.of(8)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("O(1) space dynamic programming calculating max non-adjacent loot in O(N) time.")
            .exampleInput("nums = [2, 7, 9, 3, 1]")
            .exampleOutput("12")
            .steps(steps)
            .build();
    }

    private AnimatedExplanationResponse generateLongestSubstringTrace(Problem problem) {
        List<String> code = List.of(
            "public int lengthOfLongestSubstring(String s) {",
            "    Set<Character> set = new HashSet<>();",
            "    int left = 0, maxLen = 0;",
            "    for (int right = 0; right < s.length(); right++) {",
            "        while (set.contains(s.charAt(right))) set.remove(s.charAt(left++));",
            "        set.add(s.charAt(right));",
            "        maxLen = Math.max(maxLen, right - left + 1);",
            "    }",
            "    return maxLen;",
            "}"
        );

        List<AnimationStep> steps = List.of(
            AnimationStep.builder().step(1).title("1. Initialize Sliding Window")
                .description("s = \"abcabcbb\". Init left = 0, right = 0, maxLen = 0, set = {}.")
                .visualType("pointer").visualData("abcabcbb")
                .pointers(Map.of("left", 0, "right", 0))
                .vars(Map.of("left", "0", "right", "0", "maxLen", "0")).codeLines(List.of(1, 2, 3)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(2).title("2. Expand Window to \"abc\"")
                .description("Characters 'a', 'b', 'c' are unique. Insert into set. Window: [0..2] \"abc\", maxLen = 3.")
                .visualType("pointer").visualData("abcabcbb")
                .pointers(Map.of("left", 0, "right", 2))
                .vars(Map.of("window", "\"abc\"", "windowLen", "3", "maxLen", "3")).codeLines(List.of(4, 6, 7)).code(code).durationMs(2400).build(),
            AnimationStep.builder().step(3).title("3. Duplicate 'a' Encountered (right = 3)")
                .description("char 'a' is already in set. Advance left pointer: remove s[0] ('a'), left becomes 1.")
                .visualType("pointer").visualData("abcabcbb")
                .pointers(Map.of("left", 1, "right", 3))
                .vars(Map.of("removed", "'a'", "left", "1", "window", "\"bca\"")).codeLines(List.of(5, 6)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(4).title("4. Slide Through Remaining Chars")
                .description("Window continues sliding through s. Any duplicate triggers left contraction.")
                .visualType("pointer").visualData("abcabcbb")
                .pointers(Map.of("left", 5, "right", 7))
                .vars(Map.of("status", "SLIDING", "maxLen", "3")).codeLines(List.of(4, 7)).code(code).durationMs(2200).build(),
            AnimationStep.builder().step(5).title("5. Return Maximum Substring Length = 3")
                .description("Longest substring without repeating characters is \"abc\" of length 3. Completed in O(N) time.")
                .visualType("pointer").visualData("3")
                .vars(Map.of("result", "3", "status", "SUCCESS", "time", "O(N)", "space", "O(min(M,N))")).codeLines(List.of(9)).code(code).durationMs(3000).build()
        );

        return AnimatedExplanationResponse.builder()
            .problemTitle(problem.getTitle())
            .summary("Sliding window with Set in O(N) time.")
            .exampleInput("s = \"abcabcbb\"")
            .exampleOutput("3")
            .steps(steps)
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. INTELLIGENT DYNAMIC TRACE FOR ANY DSA PROBLEM
    // ─────────────────────────────────────────────────────────────────────────

    private AnimatedExplanationResponse generateDynamicTrace(Problem problem) {
        String title = problem.getTitle() != null ? problem.getTitle() : "Algorithm Problem";
        String topic = problem.getTopic() != null ? problem.getTopic() : "Data Structures & Algorithms";

        String exampleInput = "";
        String exampleOutput = "";
        if (problem.getExamples() != null && !problem.getExamples().isBlank()) {
            for (String line : problem.getExamples().split("\n")) {
                line = line.trim();
                if (line.toLowerCase().startsWith("input:") && exampleInput.isEmpty()) {
                    exampleInput = line.substring(6).trim();
                } else if (line.toLowerCase().startsWith("output:") && exampleOutput.isEmpty()) {
                    exampleOutput = line.substring(7).trim();
                }
            }
        }
        if (exampleInput.isEmpty()) exampleInput = "Sample Input for " + title;
        if (exampleOutput.isEmpty()) exampleOutput = "Expected Result";

        List<String> code = new ArrayList<>();
        if (problem.getStarterCodeJava() != null && !problem.getStarterCodeJava().isBlank()) {
            for (String l : problem.getStarterCodeJava().split("\n")) {
                if (!l.trim().isEmpty()) code.add(l);
            }
        }
        if (code.isEmpty()) {
            code = List.of(
                "class Solution {",
                "    public Object solve(" + exampleInput + ") {",
                "        // Optimal resolution for " + title,
                "        return " + exampleOutput + ";",
                "    }",
                "}"
            );
        }

        // Determine data structure and visualType
        String visualType = "array";
        String visualData = "[]";
        List<String> items = new ArrayList<>();

        if (topic.toLowerCase().contains("tree")) {
            visualType = "tree";
            visualData = "[\"root\",\"left\",\"right\"]";
        } else if (topic.toLowerCase().contains("list") || topic.toLowerCase().contains("linked")) {
            visualType = "linkedlist";
            visualData = "[\"1\",\"2\",\"3\",\"4\"]";
        } else if (topic.toLowerCase().contains("matrix") || topic.toLowerCase().contains("grid")) {
            visualType = "matrix";
            visualData = "[[1,0],[0,1]]";
        } else if (topic.toLowerCase().contains("stack")) {
            visualType = "stack";
            visualData = "[]";
        } else {
            Matcher m = Pattern.compile("\\[([^\\]]+)\\]").matcher(exampleInput);
            if (m.find()) {
                visualData = "[" + m.group(1).trim() + "]";
                visualType = "array";
                for (String part : m.group(1).split(",")) {
                    items.add(part.trim());
                }
            } else {
                visualType = "pointer";
                visualData = exampleInput;
            }
        }

        List<AnimationStep> steps = new ArrayList<>();
        int stepNum = 1;

        steps.add(AnimationStep.builder().step(stepNum++).title("1. Initialize Invariants for " + title)
                .description("Prepare initial algorithmic invariants and load input: " + exampleInput)
                .visualType(visualType).visualData(visualData)
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("input", exampleInput, "target", exampleOutput))
                .codeLines(List.of(1, 2)).code(code).durationMs(2400).build());

        steps.add(AnimationStep.builder().step(stepNum++).title("2. Verify Boundary Constraints")
                .description("Inspect starting values, base conditions, and boundary checks.")
                .visualType(visualType).visualData(visualData)
                .pointers(Map.of("i", 0)).highlights(List.of(0))
                .vars(Map.of("status", "VALIDATING_BASE_CONDITIONS")).codeLines(List.of(2, 3)).code(code).durationMs(2200).build());

        if (!items.isEmpty()) {
            for (int i = 0; i < Math.min(items.size(), 4); i++) {
                steps.add(AnimationStep.builder().step(stepNum++).title((stepNum - 1) + ". Process Element at Index " + i + " (" + items.get(i) + ")")
                        .description("Evaluate element " + items.get(i) + " against algorithmic condition and state transition.")
                        .visualType(visualType).visualData(visualData)
                        .pointers(Map.of("i", i)).highlights(List.of(i))
                        .vars(Map.of("i", String.valueOf(i), "currentVal", items.get(i), "status", "EVALUATING"))
                        .codeLines(List.of(3, 4)).code(code).durationMs(2200).build());
            }
        } else {
            steps.add(AnimationStep.builder().step(stepNum++).title("3. Iterative State Transition")
                    .description("Execute main loop transition, evaluating state parameters.")
                    .visualType(visualType).visualData(visualData)
                    .pointers(Map.of("idx", 0)).highlights(List.of(0))
                    .vars(Map.of("state", "ITERATING")).codeLines(List.of(3, 4)).code(code).durationMs(2200).build());
            steps.add(AnimationStep.builder().step(stepNum++).title("4. Converging on Target Solution")
                    .description("Invariants satisfied, preparing accumulator and final return state.")
                    .visualType(visualType).visualData(visualData)
                    .pointers(Map.of("idx", 1)).highlights(List.of(0))
                    .vars(Map.of("state", "CONVERGING", "candidate", exampleOutput)).codeLines(List.of(4)).code(code).durationMs(2200).build());
        }

        steps.add(AnimationStep.builder().step(stepNum).title(stepNum + ". Return Evaluated Solution: " + exampleOutput)
                .description("Execution complete. Return solution " + exampleOutput + " for " + title + " in optimal time.")
                .visualType(visualType).visualData(visualData)
                .highlights(List.of(0))
                .vars(Map.of("result", exampleOutput, "status", "SUCCESS", "time", "O(N)", "space", "O(1)"))
                .codeLines(List.of(code.size() > 2 ? code.size() - 1 : code.size())).code(code).durationMs(3000).build());

        return AnimatedExplanationResponse.builder()
            .problemTitle(title)
            .summary("Optimal algorithmic resolution for " + title + " in " + topic + ".")
            .exampleInput(exampleInput)
            .exampleOutput(exampleOutput)
            .steps(steps)
            .build();
    }
}
