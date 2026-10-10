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
            return noApiKeyResponse(problem);
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
            // Helper fallback array from exampleInput e.g. "[2, 7, 11, 15]"
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

                // If visualData is blank, fallback to previous step or example input array
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

                // pointers map
                if (node.has("pointers") && node.get("pointers").isObject()) {
                    java.util.Map<String, Integer> ptrs = new java.util.LinkedHashMap<>();
                    node.get("pointers").fields().forEachRemaining(e -> ptrs.put(e.getKey(), e.getValue().asInt()));
                    builder.pointers(ptrs);
                }
                // highlights list
                if (node.has("highlights") && node.get("highlights").isArray()) {
                    java.util.List<Integer> hl = new java.util.ArrayList<>();
                    node.get("highlights").forEach(n -> hl.add(n.asInt()));
                    builder.highlights(hl);
                }
                // vars map
                if (node.has("vars") && node.get("vars").isObject()) {
                    java.util.Map<String, String> vars = new java.util.LinkedHashMap<>();
                    node.get("vars").fields().forEachRemaining(e -> vars.put(e.getKey(), e.getValue().asText()));
                    builder.vars(vars);
                }
                // codeLines list
                if (node.has("codeLines") && node.get("codeLines").isArray()) {
                    java.util.List<Integer> cl = new java.util.ArrayList<>();
                    node.get("codeLines").forEach(n -> cl.add(n.asInt()));
                    builder.codeLines(cl);
                }
                // code array
                if (node.has("code") && node.get("code").isArray()) {
                    java.util.List<String> code = new java.util.ArrayList<>();
                    node.get("code").forEach(n -> code.add(n.asText()));
                    builder.code(code);
                }

                steps.add(builder.build());
            }

            return AnimatedExplanationResponse.builder()
                    .problemTitle(problem.getTitle())
                    .summary(root.path("summary").asText())
                    .exampleInput(root.path("exampleInput").asText(""))
                    .exampleOutput(root.path("exampleOutput").asText(""))
                    .steps(steps)
                    .build();
        } catch (Exception e) {
            log.warn("Gemini dry-run explanation failed: {}", e.getMessage());
            return noApiKeyResponse(problem);
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

    /**
     * Shown when no Gemini API key is configured or the AI call fails entirely.
     * No hardcoded algorithm steps — user is informed to configure the key.
     */
    private AnimatedExplanationResponse noApiKeyResponse(Problem problem) {
        List<AnimationStep> steps = List.of(
            AnimationStep.builder()
                .step(1)
                .title("AI Animation Unavailable")
                .description("The Gemini AI key is not configured or the AI call failed. " +
                        "Set the GEMINI_API_KEY environment variable and restart the backend " +
                        "to enable live dry-run animations for " + problem.getTitle() + ".")
                .visualType("text")
                .visualData("export GEMINI_API_KEY=your_key_here")
                .durationMs(4000)
                .build()
        );
        return AnimatedExplanationResponse.builder()
                .problemTitle(problem.getTitle())
                .summary("Configure GEMINI_API_KEY to enable AI-powered algorithm dry-runs.")
                .steps(steps)
                .build();
    }
}
