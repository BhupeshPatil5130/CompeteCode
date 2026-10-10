package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.dto.TestResultDto;
import com.CompetCode.CompetCode.entity.TestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import com.CompetCode.CompetCode.dto.RunCodeResponse;

@Service
@Slf4j
public class CodeExecutionService {

    private static final Map<String, Integer> LANGUAGE_IDS = Map.of(
            "java", 62,
            "python", 71,
            "python3", 71,
            "cpp", 54,
            "c++", 54,
            "c", 50,
            "javascript", 63,
            "js", 63
    );

    private static final String OPEN_JUDGE0_URL = "https://ce.judge0.com";

    @Value("${judge0.api.url:https://ce.judge0.com}")
    private String judge0Url;

    @Value("${judge0.api.key:}")
    private String judge0Key;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestResultDto runTestCase(String code, String language, TestCase testCase,
                                     int testNumber, String executionKey) {
        long start = System.currentTimeMillis();

        try {
            Integer languageId = LANGUAGE_IDS.get(language.toLowerCase());
            if (languageId == null) {
                return failedResult(testCase, testNumber, start, "Unsupported language: " + language);
            }

            String preparedCode = prepareSourceCode(code, language, executionKey);
            if (preparedCode == null) {
                return failedResult(testCase, testNumber, start,
                        "No execution wrapper for this problem. Set executionKey on the problem.");
            }

            Map<String, Object> submission = new HashMap<>();
            submission.put("source_code", preparedCode);
            submission.put("language_id", languageId);
            submission.put("stdin", testCase.getInput());
            submission.put("expected_output", testCase.getExpectedOutput());

            var node = executeSubmission(submission);

            String status = node.path("status").path("description").asText("");
            String stdout = node.path("stdout").asText("").trim();
            String stderr = node.path("stderr").asText("");
            String compileOutput = node.path("compile_output").asText("");

            boolean passed = "Accepted".equals(status)
                    || stdout.equals(testCase.getExpectedOutput().trim());

            String error = null;
            if (!compileOutput.isBlank()) error = "Compile Error:\n" + compileOutput;
            else if (!stderr.isBlank()) error = stderr;

            return TestResultDto.builder()
                    .testNumber(testNumber)
                    .passed(passed)
                    .input(hiddenIfNeeded(testCase))
                    .expectedOutput(hiddenExpected(testCase))
                    .actualOutput(stdout.isEmpty() ? null : stdout)
                    .error(error)
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            log.error("Judge0 execution failed: {}", e.getMessage());
            return failedResult(testCase, testNumber, start, "Execution error: " + e.getMessage());
        }
    }

    public RunCodeResponse runRawCode(String code, String language, String stdin) {
        long start = System.currentTimeMillis();

        try {
            Integer languageId = LANGUAGE_IDS.get(language.toLowerCase());
            if (languageId == null) {
                return RunCodeResponse.builder()
                        .statusDescription("Error")
                        .stderr("Unsupported language: " + language)
                        .time(0.0)
                        .build();
            }

            // For raw run, ensure Java code has a main entry point
            String finalCode = code;
            if ("java".equalsIgnoreCase(language) && !hasMainEntryPoint(code)) {
                finalCode = wrapJavaWithGenericMain(code);
            }

            Map<String, Object> submission = new HashMap<>();
            submission.put("source_code", finalCode);
            submission.put("language_id", languageId);
            if (stdin != null && !stdin.isEmpty()) {
                submission.put("stdin", stdin);
            }

            var node = executeSubmission(submission);

            return RunCodeResponse.builder()
                    .stdout(node.path("stdout").asText(""))
                    .stderr(node.path("stderr").asText(""))
                    .compileOutput(node.path("compile_output").asText(""))
                    .statusId(node.path("status").path("id").asInt(0))
                    .statusDescription(node.path("status").path("description").asText(""))
                    .time(node.path("time").asDouble(0.0))
                    .memory(node.path("memory").asInt(0))
                    .build();
        } catch (Exception e) {
            log.error("Judge0 raw execution failed: {}", e.getMessage());
            return RunCodeResponse.builder()
                    .statusDescription("Execution Error")
                    .stderr("Execution error: " + e.getMessage())
                    .time((double)(System.currentTimeMillis() - start) / 1000.0)
                    .build();
        }
    }

    /**
     * Executes a submission against Judge0.
     * Automatically falls back to the open public Judge0 Community Edition (https://ce.judge0.com)
     * if the configured URL fails with 403 (unsubscribed), 401, or 429 rate limit.
     */
    private com.fasterxml.jackson.databind.JsonNode executeSubmission(Map<String, Object> submission) throws Exception {
        String primaryUrl = (judge0Url == null || judge0Url.isBlank()) ? OPEN_JUDGE0_URL : judge0Url.trim();
        boolean isPlaceholderKey = judge0Key == null
                || judge0Key.isBlank()
                || judge0Key.toLowerCase().contains("your_")
                || judge0Key.equalsIgnoreCase("dummy");

        // If configured with RapidAPI without a valid key, skip straight to open CE instance
        if (primaryUrl.contains("rapidapi.com") && isPlaceholderKey) {
            primaryUrl = OPEN_JUDGE0_URL;
        }

        try {
            return doJudge0Post(primaryUrl, submission, primaryUrl.equals(OPEN_JUDGE0_URL) ? null : judge0Key);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";
            // If primary failed due to subscription (403), auth (401), or rate limit (429), fallback to open CE
            if (!primaryUrl.equals(OPEN_JUDGE0_URL) && (msg.contains("403") || msg.contains("401") || msg.contains("429") || msg.contains("not subscribed"))) {
                log.warn("Judge0 call to {} failed ({}), retrying on public open instance {}", primaryUrl, msg, OPEN_JUDGE0_URL);
                return doJudge0Post(OPEN_JUDGE0_URL, submission, null);
            }
            throw ex;
        }
    }

    private com.fasterxml.jackson.databind.JsonNode doJudge0Post(String baseUrl, Map<String, Object> submission, String key) throws Exception {
        RestClient client = RestClient.create();
        var request = client.post()
                .uri(baseUrl + "/submissions?base64_encoded=false&wait=true")
                .header("Content-Type", "application/json");

        if (key != null && !key.isBlank() && !key.toLowerCase().contains("your_")) {
            if (baseUrl.contains("rapidapi.com")) {
                request = request.header("X-RapidAPI-Key", key)
                                 .header("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");
            } else {
                request = request.header("X-Auth-Token", key);
            }
        }

        String response = request.body(submission).retrieve().body(String.class);
        return objectMapper.readTree(response);
    }

    private String prepareSourceCode(String code, String language, String executionKey) {
        if (executionKey == null || executionKey.isBlank()) {
            // No key: only pass through if it already has a main
            return hasMainEntryPoint(code) ? code : null;
        }
        return switch (language.toLowerCase()) {
            case "java" -> wrapJava(code, executionKey);
            case "python", "python3" -> wrapPython(code, executionKey);
            case "cpp", "c++" -> wrapCpp(code, executionKey);
            default -> code;
        };
    }

    private boolean hasMainEntryPoint(String code) {
        return code.contains("public static void main")
                || code.contains("public class Main")
                || code.contains("int main(");
    }

    /**
     * Wraps a Solution class (without imports/public modifier) into a full compilable Java file.
     * Key fix: text blocks can't use \\s+ directly — we build the string programmatically.
     */
    private String wrapJava(String userCode, String key) {
        // Strip import lines and public modifier from class
        String withoutImports = userCode.replaceAll("(?m)^import\\s+[^;]+;\\s*\n?", "").trim();
        String solution = withoutImports.replaceAll("public\\s+class\\s+Solution", "class Solution");

        String mainBlock = buildJavaMain(key);
        if (mainBlock == null) {
            return hasMainEntryPoint(userCode) ? userCode : null;
        }
        return "import java.util.*;\nimport java.util.stream.*;\n\n" + solution + "\n\n" + mainBlock;
    }

    private String buildJavaMain(String key) {
        return switch (key) {
            case "TWO_SUM" ->
                "public class Main {\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        Scanner sc = new Scanner(System.in);\n" +
                "        String[] parts = sc.nextLine().trim().split(\"\\\\s+\");\n" +
                "        int[] nums = new int[parts.length];\n" +
                "        for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);\n" +
                "        int target = Integer.parseInt(sc.nextLine().trim());\n" +
                "        int[] result = new Solution().twoSum(nums, target);\n" +
                "        System.out.println(result[0] + \" \" + result[1]);\n" +
                "    }\n" +
                "}";
            case "REVERSE_STRING" ->
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        String input = new Scanner(System.in).nextLine();\n" +
                "        char[] chars = input.toCharArray();\n" +
                "        new Solution().reverseString(chars);\n" +
                "        System.out.println(new String(chars));\n" +
                "    }\n" +
                "}";
            case "MAX_SUBARRAY" ->
                "public class Main {\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        String[] parts = new Scanner(System.in).nextLine().trim().split(\"\\\\s+\");\n" +
                "        int[] nums = new int[parts.length];\n" +
                "        for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);\n" +
                "        System.out.println(new Solution().maxSubArray(nums));\n" +
                "    }\n" +
                "}";
            case "CONTAINS_DUPLICATE" ->
                "public class Main {\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        String[] parts = new Scanner(System.in).nextLine().trim().split(\"\\\\s+\");\n" +
                "        int[] nums = new int[parts.length];\n" +
                "        for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);\n" +
                "        System.out.println(new Solution().containsDuplicate(nums));\n" +
                "    }\n" +
                "}";
            case "VALID_ANAGRAM" ->
                "public class Main {\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        Scanner sc = new Scanner(System.in);\n" +
                "        String s = sc.nextLine().trim();\n" +
                "        String t = sc.nextLine().trim();\n" +
                "        System.out.println(new Solution().isAnagram(s, t));\n" +
                "    }\n" +
                "}";
            case "PALINDROME_NUMBER" ->
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int x = Integer.parseInt(new Scanner(System.in).nextLine().trim());\n" +
                "        System.out.println(new Solution().isPalindrome(x));\n" +
                "    }\n" +
                "}";
            case "ROMAN_TO_INT" ->
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s = new Scanner(System.in).nextLine().trim();\n" +
                "        System.out.println(new Solution().romanToInt(s));\n" +
                "    }\n" +
                "}";
            case "LONGEST_COMMON_PREFIX" ->
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        String line = new Scanner(System.in).nextLine().trim();\n" +
                "        String[] strs = line.split(\"\\\\s+\");\n" +
                "        System.out.println(new Solution().longestCommonPrefix(strs));\n" +
                "    }\n" +
                "}";
            case "VALID_PARENTHESES" ->
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s = new Scanner(System.in).nextLine().trim();\n" +
                "        System.out.println(new Solution().isValid(s));\n" +
                "    }\n" +
                "}";
            case "CLIMB_STAIRS" ->
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int n = Integer.parseInt(new Scanner(System.in).nextLine().trim());\n" +
                "        System.out.println(new Solution().climbStairs(n));\n" +
                "    }\n" +
                "}";
            case "FIB_NUMBER" ->
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int n = Integer.parseInt(new Scanner(System.in).nextLine().trim());\n" +
                "        System.out.println(new Solution().fib(n));\n" +
                "    }\n" +
                "}";
            case "BINARY_SEARCH" ->
                "public class Main {\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        Scanner sc = new Scanner(System.in);\n" +
                "        String[] parts = sc.nextLine().trim().split(\"\\\\s+\");\n" +
                "        int[] nums = new int[parts.length];\n" +
                "        for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);\n" +
                "        int target = Integer.parseInt(sc.nextLine().trim());\n" +
                "        System.out.println(new Solution().search(nums, target));\n" +
                "    }\n" +
                "}";
            default -> null;
        };
    }

    /**
     * Generic Java main wrapper for free-run mode when user code has no main.
     */
    private String wrapJavaWithGenericMain(String code) {
        String withoutImports = code.replaceAll("(?m)^import\\s+[^;]+;\\s*\n?", "").trim();
        String solution = withoutImports.replaceAll("public\\s+class\\s+Solution", "class Solution");
        return "import java.util.*;\n\n" + solution + "\n\n" +
               "public class Main {\n" +
               "    public static void main(String[] args) {\n" +
               "        System.out.println(\"Run your solution by calling Solution methods.\");\n" +
               "    }\n" +
               "}";
    }

    private String wrapPython(String userCode, String key) {
        String runner = switch (key) {
            case "TWO_SUM" ->
                "import sys\n" +
                "lines = sys.stdin.read().strip().split('\\n')\n" +
                "nums = list(map(int, lines[0].split()))\n" +
                "target = int(lines[1])\n" +
                "result = two_sum(nums, target)\n" +
                "print(*result)\n";
            case "REVERSE_STRING" ->
                "import sys\n" +
                "s = sys.stdin.read().strip()\n" +
                "result = reverse_string(list(s))\n" +
                "if isinstance(result, list):\n" +
                "    print(''.join(result))\n" +
                "else:\n" +
                "    print(result)\n";
            case "MAX_SUBARRAY" ->
                "import sys\n" +
                "nums = list(map(int, sys.stdin.read().strip().split()))\n" +
                "print(max_sub_array(nums))\n";
            case "CONTAINS_DUPLICATE" ->
                "import sys\n" +
                "nums = list(map(int, sys.stdin.read().strip().split()))\n" +
                "res = contains_duplicate(nums)\n" +
                "print(str(res).lower() if isinstance(res, bool) else res)\n";
            case "VALID_ANAGRAM" ->
                "import sys\n" +
                "lines = sys.stdin.read().strip().split('\\n')\n" +
                "s = lines[0].strip()\n" +
                "t = lines[1].strip() if len(lines) > 1 else ''\n" +
                "res = is_anagram(s, t)\n" +
                "print(str(res).lower() if isinstance(res, bool) else res)\n";
            case "PALINDROME_NUMBER" ->
                "import sys\n" +
                "x = int(sys.stdin.read().strip())\n" +
                "res = is_palindrome(x)\n" +
                "print(str(res).lower() if isinstance(res, bool) else res)\n";
            case "VALID_PARENTHESES" ->
                "import sys\n" +
                "s = sys.stdin.read().strip()\n" +
                "res = is_valid(s)\n" +
                "print(str(res).lower() if isinstance(res, bool) else res)\n";
            case "CLIMB_STAIRS" ->
                "import sys\n" +
                "n = int(sys.stdin.read().strip())\n" +
                "print(climb_stairs(n))\n";
            case "BINARY_SEARCH" ->
                "import sys\n" +
                "lines = sys.stdin.read().strip().split('\\n')\n" +
                "nums = list(map(int, lines[0].split()))\n" +
                "target = int(lines[1])\n" +
                "print(search(nums, target))\n";
            default -> null;
        };
        if (runner == null) return userCode;
        return userCode + "\n\n" + runner;
    }

    private String wrapCpp(String userCode, String key) {
        // For C++, code usually already has main; return as-is
        return userCode;
    }

    private TestResultDto failedResult(TestCase testCase, int testNumber, long start, String error) {
        return TestResultDto.builder()
                .testNumber(testNumber)
                .passed(false)
                .input(hiddenIfNeeded(testCase))
                .expectedOutput(hiddenExpected(testCase))
                .error(error)
                .executionTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    private String hiddenIfNeeded(TestCase testCase) {
        return Boolean.TRUE.equals(testCase.getIsHidden()) ? "[hidden]" : testCase.getInput();
    }

    private String hiddenExpected(TestCase testCase) {
        return Boolean.TRUE.equals(testCase.getIsHidden()) ? "[hidden]" : testCase.getExpectedOutput();
    }
}
