package com.CompetCode.CompetCode.config;

import com.CompetCode.CompetCode.entity.*;
import com.CompetCode.CompetCode.repository.ProblemRepository;
import com.CompetCode.CompetCode.repository.RoadmapRepository;
import com.CompetCode.CompetCode.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final RoadmapRepository roadmapRepository;

    @Override
    public void run(String... args) {
        if (problemRepository.count() == 0) {
            seedAllData();
        }
        seedBulkProblems();
        backfillExecutionKeys();
    }

    private void seedAllData() {
        List<Problem> allProblems = new ArrayList<>();
        List<TestCase> allTestCases = new ArrayList<>();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 1: ARRAYS & HASHING
        // ═══════════════════════════════════════════════════════════════

        Problem twoSum = Problem.builder()
                .title("Two Sum")
                .description("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\nYou can return the answer in any order.")
                .constraints("2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9\nOnly one valid answer exists.")
                .examples("Input: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: Because nums[0] + nums[1] == 9, we return [0, 1].\n\nInput: nums = [3,2,4], target = 6\nOutput: [1,2]")
                .difficulty(Difficulty.EASY)
                .topic("Arrays & Hashing")
                .executionKey("TWO_SUM")
                .starterCodeJava("class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Your code here\n        return new int[]{};\n    }\n}")
                .starterCodePython("def two_sum(nums, target):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function twoSum(nums, target) {\n    // Your code here\n}")
                .build();

        Problem containsDuplicate = Problem.builder()
                .title("Contains Duplicate")
                .description("Given an integer array nums, return true if any value appears at least twice in the array, and return false if every element is distinct.")
                .constraints("1 <= nums.length <= 10^5\n-10^9 <= nums[i] <= 10^9")
                .examples("Input: nums = [1,2,3,1]\nOutput: true\n\nInput: nums = [1,2,3,4]\nOutput: false")
                .difficulty(Difficulty.EASY)
                .topic("Arrays & Hashing")
                .executionKey("CONTAINS_DUPLICATE")
                .starterCodeJava("class Solution {\n    public boolean containsDuplicate(int[] nums) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def contains_duplicate(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool containsDuplicate(vector<int>& nums) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function containsDuplicate(nums) {\n    // Your code here\n}")
                .build();

        Problem validAnagram = Problem.builder()
                .title("Valid Anagram")
                .description("Given two strings s and t, return true if t is an anagram of s, and false otherwise.\n\nAn Anagram is a word or phrase formed by rearranging the letters of a different word or phrase, typically using all the original letters exactly once.")
                .constraints("1 <= s.length, t.length <= 5 * 10^4\ns and t consist of lowercase English letters.")
                .examples("Input: s = \"anagram\", t = \"nagaram\"\nOutput: true\n\nInput: s = \"rat\", t = \"car\"\nOutput: false")
                .difficulty(Difficulty.EASY)
                .topic("Arrays & Hashing")
                .executionKey("VALID_ANAGRAM")
                .starterCodeJava("class Solution {\n    public boolean isAnagram(String s, String t) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def is_anagram(s, t):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool isAnagram(string s, string t) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function isAnagram(s, t) {\n    // Your code here\n}")
                .build();

        Problem groupAnagrams = Problem.builder()
                .title("Group Anagrams")
                .description("Given an array of strings strs, group the anagrams together. You can return the answer in any order.\n\nAn Anagram is a word or phrase formed by rearranging the letters of a different word or phrase, using all the original letters exactly once.")
                .constraints("1 <= strs.length <= 10^4\n0 <= strs[i].length <= 100\nstrs[i] consists of lowercase English letters.")
                .examples("Input: strs = [\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]\nOutput: [[\"bat\"],[\"nat\",\"tan\"],[\"ate\",\"eat\",\"tea\"]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Arrays & Hashing")
                .executionKey("GROUP_ANAGRAMS")
                .starterCodeJava("class Solution {\n    public List<List<String>> groupAnagrams(String[] strs) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def group_anagrams(strs):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<string>> groupAnagrams(vector<string>& strs) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function groupAnagrams(strs) {\n    // Your code here\n}")
                .build();

        Problem topKFrequent = Problem.builder()
                .title("Top K Frequent Elements")
                .description("Given an integer array nums and an integer k, return the k most frequent elements. You may return the answer in any order.")
                .constraints("1 <= nums.length <= 10^5\n-10^4 <= nums[i] <= 10^4\nk is in the range [1, the number of unique elements in the array].")
                .examples("Input: nums = [1,1,1,2,2,3], k = 2\nOutput: [1,2]\n\nInput: nums = [1], k = 1\nOutput: [1]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Arrays & Hashing")
                .executionKey("TOP_K_FREQUENT")
                .starterCodeJava("class Solution {\n    public int[] topKFrequent(int[] nums, int k) {\n        // Your code here\n        return new int[]{};\n    }\n}")
                .starterCodePython("def top_k_frequent(nums, k):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<int> topKFrequent(vector<int>& nums, int k) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function topKFrequent(nums, k) {\n    // Your code here\n}")
                .build();

        Problem productExceptSelf = Problem.builder()
                .title("Product of Array Except Self")
                .description("Given an integer array nums, return an array answer such that answer[i] is equal to the product of all the elements of nums except nums[i].\n\nThe product of any prefix or suffix of nums is guaranteed to fit in a 32-bit integer.\n\nYou must write an algorithm that runs in O(n) time and without using the division operation.")
                .constraints("2 <= nums.length <= 10^5\n-30 <= nums[i] <= 30\nThe product of any prefix or suffix of nums is guaranteed to fit in a 32-bit integer.")
                .examples("Input: nums = [1,2,3,4]\nOutput: [24,12,8,6]\n\nInput: nums = [-1,1,0,-3,3]\nOutput: [0,0,9,0,0]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Arrays & Hashing")
                .executionKey("PRODUCT_EXCEPT_SELF")
                .starterCodeJava("class Solution {\n    public int[] productExceptSelf(int[] nums) {\n        // Your code here\n        return new int[]{};\n    }\n}")
                .starterCodePython("def product_except_self(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<int> productExceptSelf(vector<int>& nums) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function productExceptSelf(nums) {\n    // Your code here\n}")
                .build();

        Problem longestConsecutive = Problem.builder()
                .title("Longest Consecutive Sequence")
                .description("Given an unsorted array of integers nums, return the length of the longest consecutive elements sequence.\n\nYou must write an algorithm that runs in O(n) time.")
                .constraints("0 <= nums.length <= 10^5\n-10^9 <= nums[i] <= 10^9")
                .examples("Input: nums = [100,4,200,1,3,2]\nOutput: 4\nExplanation: The longest consecutive sequence is [1, 2, 3, 4]. Length = 4.\n\nInput: nums = [0,3,7,2,5,8,4,6,0,1]\nOutput: 9")
                .difficulty(Difficulty.MEDIUM)
                .topic("Arrays & Hashing")
                .executionKey("LONGEST_CONSECUTIVE")
                .starterCodeJava("class Solution {\n    public int longestConsecutive(int[] nums) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def longest_consecutive(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int longestConsecutive(vector<int>& nums) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function longestConsecutive(nums) {\n    // Your code here\n}")
                .build();

        Problem subarraySum = Problem.builder()
                .title("Subarray Sum Equals K")
                .description("Given an array of integers nums and an integer k, return the total number of subarrays whose sum equals to k.\n\nA subarray is a contiguous non-empty sequence of elements within an array.")
                .constraints("1 <= nums.length <= 2 * 10^4\n-1000 <= nums[i] <= 1000\n-10^7 <= k <= 10^7")
                .examples("Input: nums = [1,1,1], k = 2\nOutput: 2\n\nInput: nums = [1,2,3], k = 3\nOutput: 2")
                .difficulty(Difficulty.MEDIUM)
                .topic("Arrays & Hashing")
                .executionKey("SUBARRAY_SUM")
                .starterCodeJava("class Solution {\n    public int subarraySum(int[] nums, int k) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def subarray_sum(nums, k):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int subarraySum(vector<int>& nums, int k) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function subarraySum(nums, k) {\n    // Your code here\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 2: STRINGS
        // ═══════════════════════════════════════════════════════════════

        Problem reverseString = Problem.builder()
                .title("Reverse String")
                .description("Write a function that reverses a string. The input string is given as an array of characters s.\n\nYou must do this by modifying the input array in-place with O(1) extra memory.")
                .constraints("1 <= s.length <= 10^5\ns[i] is a printable ascii character.")
                .examples("Input: s = [\"h\",\"e\",\"l\",\"l\",\"o\"]\nOutput: [\"o\",\"l\",\"l\",\"e\",\"h\"]\n\nInput: s = [\"H\",\"a\",\"n\",\"n\",\"a\",\"h\"]\nOutput: [\"h\",\"a\",\"n\",\"n\",\"a\",\"H\"]")
                .difficulty(Difficulty.EASY)
                .topic("Strings")
                .executionKey("REVERSE_STRING")
                .starterCodeJava("class Solution {\n    public void reverseString(char[] s) {\n        // Your code here\n    }\n}")
                .starterCodePython("def reverse_string(s):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    void reverseString(vector<char>& s) {\n        // Your code here\n    }\n};")
                .starterCodeJavascript("function reverseString(s) {\n    // Your code here\n}")
                .build();

        Problem validPalindrome = Problem.builder()
                .title("Valid Palindrome")
                .description("A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward. Alphanumeric characters include letters and numbers.\n\nGiven a string s, return true if it is a palindrome, or false otherwise.")
                .constraints("1 <= s.length <= 2 * 10^5\ns consists only of printable ASCII characters.")
                .examples("Input: s = \"A man, a plan, a canal: Panama\"\nOutput: true\nExplanation: \"amanaplanacanalpanama\" is a palindrome.\n\nInput: s = \"race a car\"\nOutput: false")
                .difficulty(Difficulty.EASY)
                .topic("Strings")
                .executionKey("VALID_PALINDROME")
                .starterCodeJava("class Solution {\n    public boolean isPalindrome(String s) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def is_palindrome(s):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool isPalindrome(string s) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function isPalindrome(s) {\n    // Your code here\n}")
                .build();

        Problem longestPalindrome = Problem.builder()
                .title("Longest Palindromic Substring")
                .description("Given a string s, return the longest palindromic substring in s.")
                .constraints("1 <= s.length <= 1000\ns consist of only digits and English letters.")
                .examples("Input: s = \"babad\"\nOutput: \"bab\"\nExplanation: \"aba\" is also a valid answer.\n\nInput: s = \"cbbd\"\nOutput: \"bb\"")
                .difficulty(Difficulty.MEDIUM)
                .topic("Strings")
                .executionKey("LONGEST_PALINDROME")
                .starterCodeJava("class Solution {\n    public String longestPalindrome(String s) {\n        // Your code here\n        return \"\";\n    }\n}")
                .starterCodePython("def longest_palindrome(s):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    string longestPalindrome(string s) {\n        // Your code here\n        return \"\";\n    }\n};")
                .starterCodeJavascript("function longestPalindrome(s) {\n    // Your code here\n}")
                .build();

        Problem longestSubstring = Problem.builder()
                .title("Longest Substring Without Repeating Characters")
                .description("Given a string s, find the length of the longest substring without repeating characters.")
                .constraints("0 <= s.length <= 5 * 10^4\ns consists of English letters, digits, symbols and spaces.")
                .examples("Input: s = \"abcabcbb\"\nOutput: 3\nExplanation: The answer is \"abc\", with length 3.\n\nInput: s = \"bbbbb\"\nOutput: 1\n\nInput: s = \"pwwkew\"\nOutput: 3")
                .difficulty(Difficulty.MEDIUM)
                .topic("Strings")
                .executionKey("LONGEST_SUBSTRING")
                .starterCodeJava("class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def length_of_longest_substring(s):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int lengthOfLongestSubstring(string s) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function lengthOfLongestSubstring(s) {\n    // Your code here\n}")
                .build();

        Problem stringToInteger = Problem.builder()
                .title("String to Integer (atoi)")
                .description("Implement the myAtoi(string s) function, which converts a string to a 32-bit signed integer.\n\nThe algorithm:\n1. Read and ignore leading whitespace.\n2. Check the next character for '-' or '+' sign.\n3. Read characters until non-digit or end of input.\n4. Convert to integer and clamp to [-2^31, 2^31 - 1].")
                .constraints("0 <= s.length <= 200\ns consists of English letters, digits, ' ', '+', '-', '.'")
                .examples("Input: s = \"42\"\nOutput: 42\n\nInput: s = \"   -42\"\nOutput: -42\n\nInput: s = \"4193 with words\"\nOutput: 4193")
                .difficulty(Difficulty.MEDIUM)
                .topic("Strings")
                .executionKey("STRING_TO_INT")
                .starterCodeJava("class Solution {\n    public int myAtoi(String s) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def my_atoi(s):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int myAtoi(string s) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function myAtoi(s) {\n    // Your code here\n}")
                .build();

        Problem minWindow = Problem.builder()
                .title("Minimum Window Substring")
                .description("Given two strings s and t of lengths m and n respectively, return the minimum window substring of s such that every character in t (including duplicates) is included in the window. If there is no such substring, return the empty string \"\".")
                .constraints("m == s.length\nn == t.length\n1 <= m, n <= 10^5\ns and t consist of uppercase and lowercase English letters.")
                .examples("Input: s = \"ADOBECODEBANC\", t = \"ABC\"\nOutput: \"BANC\"\nExplanation: The minimum window substring is \"BANC\".\n\nInput: s = \"a\", t = \"a\"\nOutput: \"a\"")
                .difficulty(Difficulty.HARD)
                .topic("Strings")
                .executionKey("MIN_WINDOW")
                .starterCodeJava("class Solution {\n    public String minWindow(String s, String t) {\n        // Your code here\n        return \"\";\n    }\n}")
                .starterCodePython("def min_window(s, t):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    string minWindow(string s, string t) {\n        // Your code here\n        return \"\";\n    }\n};")
                .starterCodeJavascript("function minWindow(s, t) {\n    // Your code here\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 3: LINKED LISTS
        // ═══════════════════════════════════════════════════════════════

        Problem reverseLinkedList = Problem.builder()
                .title("Reverse Linked List")
                .description("Given the head of a singly linked list, reverse the list, and return the reversed list.")
                .constraints("The number of nodes in the list is [0, 5000].\n-5000 <= Node.val <= 5000")
                .examples("Input: head = [1,2,3,4,5]\nOutput: [5,4,3,2,1]\n\nInput: head = [1,2]\nOutput: [2,1]")
                .difficulty(Difficulty.EASY)
                .topic("Linked Lists")
                .executionKey("REVERSE_LINKED_LIST")
                .starterCodeJava("class Solution {\n    public ListNode reverseList(ListNode head) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("def reverse_list(head):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    ListNode* reverseList(ListNode* head) {\n        // Your code here\n        return nullptr;\n    }\n};")
                .starterCodeJavascript("function reverseList(head) {\n    // Your code here\n}")
                .build();

        Problem mergeTwoLists = Problem.builder()
                .title("Merge Two Sorted Lists")
                .description("You are given the heads of two sorted linked lists list1 and list2.\n\nMerge the two lists into one sorted list. The list should be made by splicing together the nodes of the first two lists.\n\nReturn the head of the merged linked list.")
                .constraints("The number of nodes in both lists is in the range [0, 50].\n-100 <= Node.val <= 100\nBoth lists are sorted in non-decreasing order.")
                .examples("Input: list1 = [1,2,4], list2 = [1,3,4]\nOutput: [1,1,2,3,4,4]\n\nInput: list1 = [], list2 = []\nOutput: []")
                .difficulty(Difficulty.EASY)
                .topic("Linked Lists")
                .executionKey("MERGE_TWO_LISTS")
                .starterCodeJava("class Solution {\n    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("def merge_two_lists(list1, list2):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    ListNode* mergeTwoLists(ListNode* list1, ListNode* list2) {\n        // Your code here\n        return nullptr;\n    }\n};")
                .starterCodeJavascript("function mergeTwoLists(list1, list2) {\n    // Your code here\n}")
                .build();

        Problem linkedListCycle = Problem.builder()
                .title("Linked List Cycle")
                .description("Given head, the head of a linked list, determine if the linked list has a cycle in it.\n\nThere is a cycle in a linked list if there is some node in the list that can be reached again by continuously following the next pointer. Internally, pos is used to denote the index of the node that tail's next pointer is connected to. Note that pos is not passed as a parameter.\n\nReturn true if there is a cycle in the linked list. Otherwise, return false.")
                .constraints("The number of the nodes in the list is in the range [0, 10^4].\n-10^5 <= Node.val <= 10^5\npos is -1 or a valid index in the linked-list.")
                .examples("Input: head = [3,2,0,-4], pos = 1\nOutput: true\nExplanation: There is a cycle, where tail connects to the 1st node.\n\nInput: head = [1], pos = -1\nOutput: false")
                .difficulty(Difficulty.EASY)
                .topic("Linked Lists")
                .executionKey("LINKED_LIST_CYCLE")
                .starterCodeJava("class Solution {\n    public boolean hasCycle(ListNode head) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def has_cycle(head):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool hasCycle(ListNode *head) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function hasCycle(head) {\n    // Your code here\n}")
                .build();

        Problem removeNthFromEnd = Problem.builder()
                .title("Remove Nth Node From End of List")
                .description("Given the head of a linked list, remove the nth node from the end of the list and return its head.")
                .constraints("The number of nodes in the list is sz.\n1 <= sz <= 30\n0 <= Node.val <= 100\n1 <= n <= sz")
                .examples("Input: head = [1,2,3,4,5], n = 2\nOutput: [1,2,3,5]\n\nInput: head = [1], n = 1\nOutput: []")
                .difficulty(Difficulty.MEDIUM)
                .topic("Linked Lists")
                .executionKey("REMOVE_NTH_FROM_END")
                .starterCodeJava("class Solution {\n    public ListNode removeNthFromEnd(ListNode head, int n) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("def remove_nth_from_end(head, n):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    ListNode* removeNthFromEnd(ListNode* head, int n) {\n        // Your code here\n        return nullptr;\n    }\n};")
                .starterCodeJavascript("function removeNthFromEnd(head, n) {\n    // Your code here\n}")
                .build();

        Problem mergeKLists = Problem.builder()
                .title("Merge K Sorted Lists")
                .description("You are given an array of k linked-lists lists, each linked-list is sorted in ascending order.\n\nMerge all the linked-lists into one sorted linked-list and return it.")
                .constraints("k == lists.length\n0 <= k <= 10^4\n0 <= lists[i].length <= 500\n-10^4 <= lists[i][j] <= 10^4\nlists[i] is sorted in ascending order.\nThe sum of lists[i].length will not exceed 10^4.")
                .examples("Input: lists = [[1,4,5],[1,3,4],[2,6]]\nOutput: [1,1,2,3,4,4,5,6]\n\nInput: lists = []\nOutput: []")
                .difficulty(Difficulty.HARD)
                .topic("Linked Lists")
                .executionKey("MERGE_K_LISTS")
                .starterCodeJava("class Solution {\n    public ListNode mergeKLists(ListNode[] lists) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("def merge_k_lists(lists):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    ListNode* mergeKLists(vector<ListNode*>& lists) {\n        // Your code here\n        return nullptr;\n    }\n};")
                .starterCodeJavascript("function mergeKLists(lists) {\n    // Your code here\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 4: STACKS & QUEUES
        // ═══════════════════════════════════════════════════════════════

        Problem validParentheses = Problem.builder()
                .title("Valid Parentheses")
                .description("Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\nAn input string is valid if:\n1. Open brackets must be closed by the same type of brackets.\n2. Open brackets must be closed in the correct order.\n3. Every close bracket has a corresponding open bracket of the same type.")
                .constraints("1 <= s.length <= 10^4\ns consists of parentheses only '()[]{}'.")
                .examples("Input: s = \"()\"\nOutput: true\n\nInput: s = \"()[]{}\"\nOutput: true\n\nInput: s = \"(]\"\nOutput: false")
                .difficulty(Difficulty.EASY)
                .topic("Stacks & Queues")
                .executionKey("VALID_PARENTHESES")
                .starterCodeJava("class Solution {\n    public boolean isValid(String s) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def is_valid(s):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool isValid(string s) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function isValid(s) {\n    // Your code here\n}")
                .build();

        Problem minStack = Problem.builder()
                .title("Min Stack")
                .description("Design a stack that supports push, pop, top, and retrieving the minimum element in constant time.\n\nImplement the MinStack class:\n- MinStack() initializes the stack object.\n- void push(int val) pushes the element val onto the stack.\n- void pop() removes the element on the top of the stack.\n- int top() gets the top element of the stack.\n- int getMin() retrieves the minimum element in the stack.")
                .constraints("-2^31 <= val <= 2^31 - 1\nMethods pop, top and getMin operations will always be called on non-empty stacks.\nAt most 3 * 10^4 calls will be made to push, pop, top, and getMin.")
                .examples("Input:\n[\"MinStack\",\"push\",\"push\",\"push\",\"getMin\",\"pop\",\"top\",\"getMin\"]\n[[],[-2],[0],[-3],[],[],[],[]]\nOutput: [null,null,null,null,-3,null,0,-2]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Stacks & Queues")
                .executionKey("MIN_STACK")
                .starterCodeJava("class MinStack {\n    public MinStack() {\n        // Your code here\n    }\n    public void push(int val) {\n    }\n    public void pop() {\n    }\n    public int top() {\n        return 0;\n    }\n    public int getMin() {\n        return 0;\n    }\n}")
                .starterCodePython("class MinStack:\n    def __init__(self):\n        pass\n    def push(self, val):\n        pass\n    def pop(self):\n        pass\n    def top(self):\n        pass\n    def get_min(self):\n        pass")
                .starterCodeCpp("class MinStack {\npublic:\n    MinStack() {}\n    void push(int val) {}\n    void pop() {}\n    int top() { return 0; }\n    int getMin() { return 0; }\n};")
                .starterCodeJavascript("class MinStack {\n    constructor() {}\n    push(val) {}\n    pop() {}\n    top() {}\n    getMin() {}\n}")
                .build();

        Problem dailyTemperatures = Problem.builder()
                .title("Daily Temperatures")
                .description("Given an array of integers temperatures represents the daily temperatures, return an array answer such that answer[i] is the number of days you have to wait after the ith day to get a warmer temperature. If there is no future day for which this is possible, keep answer[i] == 0 instead.")
                .constraints("1 <= temperatures.length <= 10^5\n30 <= temperatures[i] <= 100")
                .examples("Input: temperatures = [73,74,75,71,69,72,76,73]\nOutput: [1,1,4,2,1,1,0,0]\n\nInput: temperatures = [30,40,50,60]\nOutput: [1,1,1,0]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Stacks & Queues")
                .executionKey("DAILY_TEMPERATURES")
                .starterCodeJava("class Solution {\n    public int[] dailyTemperatures(int[] temperatures) {\n        // Your code here\n        return new int[]{};\n    }\n}")
                .starterCodePython("def daily_temperatures(temperatures):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<int> dailyTemperatures(vector<int>& temperatures) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function dailyTemperatures(temperatures) {\n    // Your code here\n}")
                .build();

        Problem evalRPN = Problem.builder()
                .title("Evaluate Reverse Polish Notation")
                .description("You are given an array of strings tokens that represents an arithmetic expression in Reverse Polish Notation.\n\nEvaluate the expression. Return an integer that represents the value of the expression.\n\nNote that: division truncates toward zero.")
                .constraints("1 <= tokens.length <= 10^4\ntokens[i] is an operator: +, -, *, / or an integer in range [-200, 200].")
                .examples("Input: tokens = [\"2\",\"1\",\"+\",\"3\",\"*\"]\nOutput: 9\nExplanation: ((2 + 1) * 3) = 9\n\nInput: tokens = [\"4\",\"13\",\"5\",\"/\",\"+\"]\nOutput: 6")
                .difficulty(Difficulty.MEDIUM)
                .topic("Stacks & Queues")
                .executionKey("EVAL_RPN")
                .starterCodeJava("class Solution {\n    public int evalRPN(String[] tokens) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def eval_rpn(tokens):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int evalRPN(vector<string>& tokens) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function evalRPN(tokens) {\n    // Your code here\n}")
                .build();

        Problem largestRectangle = Problem.builder()
                .title("Largest Rectangle in Histogram")
                .description("Given an array of integers heights representing the histogram's bar height where the width of each bar is 1, return the area of the largest rectangle in the histogram.")
                .constraints("1 <= heights.length <= 10^5\n0 <= heights[i] <= 10^4")
                .examples("Input: heights = [2,1,5,6,2,3]\nOutput: 10\nExplanation: The largest rectangle is shown in the third and fourth bars with height 5 and width 2 = 10.\n\nInput: heights = [2,4]\nOutput: 4")
                .difficulty(Difficulty.HARD)
                .topic("Stacks & Queues")
                .executionKey("LARGEST_RECTANGLE")
                .starterCodeJava("class Solution {\n    public int largestRectangleArea(int[] heights) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def largest_rectangle_area(heights):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int largestRectangleArea(vector<int>& heights) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function largestRectangleArea(heights) {\n    // Your code here\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 5: TREES & BST
        // ═══════════════════════════════════════════════════════════════

        Problem invertTree = Problem.builder()
                .title("Invert Binary Tree")
                .description("Given the root of a binary tree, invert the tree, and return its root.\n\nInverting a binary tree means swapping every left child with its right child.")
                .constraints("The number of nodes in the tree is in the range [0, 100].\n-100 <= Node.val <= 100")
                .examples("Input: root = [4,2,7,1,3,6,9]\nOutput: [4,7,2,9,6,3,1]\n\nInput: root = [2,1,3]\nOutput: [2,3,1]")
                .difficulty(Difficulty.EASY)
                .topic("Trees & BST")
                .executionKey("INVERT_TREE")
                .starterCodeJava("class Solution {\n    public TreeNode invertTree(TreeNode root) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("def invert_tree(root):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    TreeNode* invertTree(TreeNode* root) {\n        // Your code here\n        return nullptr;\n    }\n};")
                .starterCodeJavascript("function invertTree(root) {\n    // Your code here\n}")
                .build();

        Problem maxDepth = Problem.builder()
                .title("Maximum Depth of Binary Tree")
                .description("Given the root of a binary tree, return its maximum depth.\n\nA binary tree's maximum depth is the number of nodes along the longest path from the root node down to the farthest leaf node.")
                .constraints("The number of nodes in the tree is in the range [0, 10^4].\n-100 <= Node.val <= 100")
                .examples("Input: root = [3,9,20,null,null,15,7]\nOutput: 3\n\nInput: root = [1,null,2]\nOutput: 2")
                .difficulty(Difficulty.EASY)
                .topic("Trees & BST")
                .executionKey("MAX_DEPTH")
                .starterCodeJava("class Solution {\n    public int maxDepth(TreeNode root) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def max_depth(root):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int maxDepth(TreeNode* root) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function maxDepth(root) {\n    // Your code here\n}")
                .build();

        Problem isSameTree = Problem.builder()
                .title("Same Tree")
                .description("Given the roots of two binary trees p and q, write a function to check if they are the same or not.\n\nTwo binary trees are considered the same if they are structurally identical, and the nodes have the same value.")
                .constraints("The number of nodes in both trees is in the range [0, 100].\n-10^4 <= Node.val <= 10^4")
                .examples("Input: p = [1,2,3], q = [1,2,3]\nOutput: true\n\nInput: p = [1,2], q = [1,null,2]\nOutput: false")
                .difficulty(Difficulty.EASY)
                .topic("Trees & BST")
                .executionKey("SAME_TREE")
                .starterCodeJava("class Solution {\n    public boolean isSameTree(TreeNode p, TreeNode q) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def is_same_tree(p, q):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool isSameTree(TreeNode* p, TreeNode* q) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function isSameTree(p, q) {\n    // Your code here\n}")
                .build();

        Problem levelOrder = Problem.builder()
                .title("Binary Tree Level Order Traversal")
                .description("Given the root of a binary tree, return the level order traversal of its nodes' values. (i.e., from left to right, level by level).")
                .constraints("The number of nodes in the tree is in the range [0, 2000].\n-1000 <= Node.val <= 1000")
                .examples("Input: root = [3,9,20,null,null,15,7]\nOutput: [[3],[9,20],[15,7]]\n\nInput: root = [1]\nOutput: [[1]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Trees & BST")
                .executionKey("LEVEL_ORDER")
                .starterCodeJava("class Solution {\n    public List<List<Integer>> levelOrder(TreeNode root) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def level_order(root):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<int>> levelOrder(TreeNode* root) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function levelOrder(root) {\n    // Your code here\n}")
                .build();

        Problem validateBST = Problem.builder()
                .title("Validate Binary Search Tree")
                .description("Given the root of a binary tree, determine if it is a valid binary search tree (BST).\n\nA valid BST is defined as follows:\n- The left subtree of a node contains only nodes with keys less than the node's key.\n- The right subtree of a node contains only nodes with keys greater than the node's key.\n- Both the left and right subtrees must also be binary search trees.")
                .constraints("The number of nodes in the tree is in the range [1, 10^4].\n-2^31 <= Node.val <= 2^31 - 1")
                .examples("Input: root = [2,1,3]\nOutput: true\n\nInput: root = [5,1,4,null,null,3,6]\nOutput: false")
                .difficulty(Difficulty.MEDIUM)
                .topic("Trees & BST")
                .executionKey("VALIDATE_BST")
                .starterCodeJava("class Solution {\n    public boolean isValidBST(TreeNode root) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def is_valid_bst(root):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool isValidBST(TreeNode* root) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function isValidBST(root) {\n    // Your code here\n}")
                .build();

        Problem lowestCommonAncestor = Problem.builder()
                .title("Lowest Common Ancestor of a BST")
                .description("Given a binary search tree (BST), find the lowest common ancestor (LCA) node of two given nodes in the BST.\n\nThe lowest common ancestor is defined between two nodes p and q as the lowest node in T that has both p and q as descendants (where we allow a node to be a descendant of itself).")
                .constraints("The number of nodes in the tree is in the range [2, 10^5].\n-10^9 <= Node.val <= 10^9\nAll Node.val are unique.\np != q\np and q will exist in the BST.")
                .examples("Input: root = [6,2,8,0,4,7,9,null,null,3,5], p = 2, q = 8\nOutput: 6\n\nInput: root = [6,2,8,0,4,7,9,null,null,3,5], p = 2, q = 4\nOutput: 2")
                .difficulty(Difficulty.MEDIUM)
                .topic("Trees & BST")
                .executionKey("LCA_BST")
                .starterCodeJava("class Solution {\n    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("def lowest_common_ancestor(root, p, q):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    TreeNode* lowestCommonAncestor(TreeNode* root, TreeNode* p, TreeNode* q) {\n        // Your code here\n        return nullptr;\n    }\n};")
                .starterCodeJavascript("function lowestCommonAncestor(root, p, q) {\n    // Your code here\n}")
                .build();

        Problem serializeTree = Problem.builder()
                .title("Serialize and Deserialize Binary Tree")
                .description("Serialization is the process of converting a data structure or object into a sequence of bits so that it can be stored in a file or memory buffer.\n\nDesign an algorithm to serialize and deserialize a binary tree. There is no restriction on how your serialization/deserialization algorithm should work.")
                .constraints("The number of nodes in the tree is in the range [0, 10^4].\n-1000 <= Node.val <= 1000")
                .examples("Input: root = [1,2,3,null,null,4,5]\nOutput: [1,2,3,null,null,4,5]")
                .difficulty(Difficulty.HARD)
                .topic("Trees & BST")
                .executionKey("SERIALIZE_TREE")
                .starterCodeJava("public class Codec {\n    public String serialize(TreeNode root) {\n        // Your code here\n        return \"\";\n    }\n    public TreeNode deserialize(String data) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("class Codec:\n    def serialize(self, root):\n        pass\n    def deserialize(self, data):\n        pass")
                .starterCodeCpp("class Codec {\npublic:\n    string serialize(TreeNode* root) { return \"\"; }\n    TreeNode* deserialize(string data) { return nullptr; }\n};")
                .starterCodeJavascript("class Codec {\n    serialize(root) {}\n    deserialize(data) {}\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 6: GRAPHS
        // ═══════════════════════════════════════════════════════════════

        Problem numIslands = Problem.builder()
                .title("Number of Islands")
                .description("Given an m x n 2D binary grid which represents a map of '1's (land) and '0's (water), return the number of islands.\n\nAn island is surrounded by water and is formed by connecting adjacent lands horizontally or vertically. You may assume all four edges of the grid are all surrounded by water.")
                .constraints("m == grid.length\nn == grid[i].length\n1 <= m, n <= 300\ngrid[i][j] is '0' or '1'.")
                .examples("Input: grid = [\n  [\"1\",\"1\",\"1\",\"1\",\"0\"],\n  [\"1\",\"1\",\"0\",\"1\",\"0\"],\n  [\"1\",\"1\",\"0\",\"0\",\"0\"],\n  [\"0\",\"0\",\"0\",\"0\",\"0\"]\n]\nOutput: 1\n\nInput: grid = [\n  [\"1\",\"1\",\"0\",\"0\",\"0\"],\n  [\"1\",\"1\",\"0\",\"0\",\"0\"],\n  [\"0\",\"0\",\"1\",\"0\",\"0\"],\n  [\"0\",\"0\",\"0\",\"1\",\"1\"]\n]\nOutput: 3")
                .difficulty(Difficulty.MEDIUM)
                .topic("Graphs")
                .executionKey("NUM_ISLANDS")
                .starterCodeJava("class Solution {\n    public int numIslands(char[][] grid) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def num_islands(grid):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int numIslands(vector<vector<char>>& grid) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function numIslands(grid) {\n    // Your code here\n}")
                .build();

        Problem cloneGraph = Problem.builder()
                .title("Clone Graph")
                .description("Given a reference of a node in a connected undirected graph. Return a deep copy (clone) of the graph.\n\nEach node in the graph contains a value (int) and a list of its neighbors.")
                .constraints("The number of nodes in the graph is in the range [0, 100].\n1 <= Node.val <= 100\nNode.val is unique for each node.\nThere are no repeated edges and no self-loops.\nThe Graph is connected and all nodes can be visited starting from the given node.")
                .examples("Input: adjList = [[2,4],[1,3],[2,4],[1,3]]\nOutput: [[2,4],[1,3],[2,4],[1,3]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Graphs")
                .executionKey("CLONE_GRAPH")
                .starterCodeJava("class Solution {\n    public Node cloneGraph(Node node) {\n        // Your code here\n        return null;\n    }\n}")
                .starterCodePython("def clone_graph(node):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    Node* cloneGraph(Node* node) {\n        // Your code here\n        return nullptr;\n    }\n};")
                .starterCodeJavascript("function cloneGraph(node) {\n    // Your code here\n}")
                .build();

        Problem courseSchedule = Problem.builder()
                .title("Course Schedule")
                .description("There are a total of numCourses courses you have to take, labeled from 0 to numCourses - 1. You are given an array prerequisites where prerequisites[i] = [ai, bi] indicates that you must take course bi first if you want to take course ai.\n\nReturn true if you can finish all courses. Otherwise, return false.")
                .constraints("1 <= numCourses <= 2000\n0 <= prerequisites.length <= 5000\nprerequisites[i].length == 2\n0 <= ai, bi < numCourses\nAll the pairs prerequisites[i] are unique.")
                .examples("Input: numCourses = 2, prerequisites = [[1,0]]\nOutput: true\n\nInput: numCourses = 2, prerequisites = [[1,0],[0,1]]\nOutput: false")
                .difficulty(Difficulty.MEDIUM)
                .topic("Graphs")
                .executionKey("COURSE_SCHEDULE")
                .starterCodeJava("class Solution {\n    public boolean canFinish(int numCourses, int[][] prerequisites) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def can_finish(num_courses, prerequisites):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool canFinish(int numCourses, vector<vector<int>>& prerequisites) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function canFinish(numCourses, prerequisites) {\n    // Your code here\n}")
                .build();

        Problem pacificAtlantic = Problem.builder()
                .title("Pacific Atlantic Water Flow")
                .description("There is an m x n rectangular island that borders both the Pacific Ocean and Atlantic Ocean. The Pacific Ocean touches the island's left and top edges, and the Atlantic Ocean touches the island's right and bottom edges.\n\nThe island receives a lot of rain, and the rain water can flow to neighboring cells directly north, south, east, and west if the neighboring cell's height is less than or equal to the current cell's height.\n\nReturn a 2D list of grid coordinates result where result[i] = [ri, ci] denotes that rain water can flow from cell (ri, ci) to both the Pacific and Atlantic oceans.")
                .constraints("m == heights.length\nn == heights[r].length\n1 <= m, n <= 200\n0 <= heights[r][c] <= 10^5")
                .examples("Input: heights = [[1,2,2,3,5],[3,2,3,4,4],[2,4,5,3,1],[6,7,1,4,5],[5,1,1,2,4]]\nOutput: [[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Graphs")
                .executionKey("PACIFIC_ATLANTIC")
                .starterCodeJava("class Solution {\n    public List<List<Integer>> pacificAtlantic(int[][] heights) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def pacific_atlantic(heights):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<int>> pacificAtlantic(vector<vector<int>>& heights) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function pacificAtlantic(heights) {\n    // Your code here\n}")
                .build();

        Problem networkDelay = Problem.builder()
                .title("Network Delay Time")
                .description("You are given a network of n nodes, labeled from 1 to n. You are also given times, a list of travel times as directed edges times[i] = (ui, vi, wi), where ui is the source node, vi is the target node, and wi is the time it takes for a signal to travel from source to target.\n\nWe will send a signal from a given node k. Return the minimum time it takes for all the n nodes to receive the signal. If it is impossible for all the n nodes to receive the signal, return -1.")
                .constraints("1 <= k <= n <= 100\n1 <= times.length <= 6000\ntimes[i].length == 3\n1 <= ui, vi <= n\nui != vi\n0 <= wi <= 100\nAll the pairs (ui, vi) are unique.")
                .examples("Input: times = [[2,1,1],[2,3,1],[3,4,1]], n = 4, k = 2\nOutput: 2\n\nInput: times = [[1,2,1]], n = 2, k = 2\nOutput: -1")
                .difficulty(Difficulty.MEDIUM)
                .topic("Graphs")
                .executionKey("NETWORK_DELAY")
                .starterCodeJava("class Solution {\n    public int networkDelayTime(int[][] times, int n, int k) {\n        // Your code here\n        return -1;\n    }\n}")
                .starterCodePython("def network_delay_time(times, n, k):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int networkDelayTime(vector<vector<int>>& times, int n, int k) {\n        // Your code here\n        return -1;\n    }\n};")
                .starterCodeJavascript("function networkDelayTime(times, n, k) {\n    // Your code here\n}")
                .build();

        Problem alienDictionary = Problem.builder()
                .title("Word Ladder")
                .description("A transformation sequence from word beginWord to word endWord using a dictionary wordList is a sequence of words beginWord -> s1 -> s2 -> ... -> sk such that:\n- Every adjacent pair of words differs by a single letter.\n- Every si for 1 <= i <= k is in wordList.\n- sk == endWord\n\nGiven two words, beginWord and endWord, and a dictionary wordList, return the number of words in the shortest transformation sequence from beginWord to endWord, or 0 if no such sequence exists.")
                .constraints("1 <= beginWord.length <= 10\nendWord.length == beginWord.length\n1 <= wordList.length <= 5000\nwordList[i].length == beginWord.length\nAll words consist of lowercase English letters.\nAll the words are unique.")
                .examples("Input: beginWord = \"hit\", endWord = \"cog\", wordList = [\"hot\",\"dot\",\"dog\",\"lot\",\"log\",\"cog\"]\nOutput: 5\nExplanation: hit -> hot -> dot -> dog -> cog")
                .difficulty(Difficulty.HARD)
                .topic("Graphs")
                .executionKey("WORD_LADDER")
                .starterCodeJava("class Solution {\n    public int ladderLength(String beginWord, String endWord, List<String> wordList) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def ladder_length(begin_word, end_word, word_list):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int ladderLength(string beginWord, string endWord, vector<string>& wordList) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function ladderLength(beginWord, endWord, wordList) {\n    // Your code here\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 7: DYNAMIC PROGRAMMING
        // ═══════════════════════════════════════════════════════════════

        Problem climbStairs = Problem.builder()
                .title("Climbing Stairs")
                .description("You are climbing a staircase. It takes n steps to reach the top.\n\nEach time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?")
                .constraints("1 <= n <= 45")
                .examples("Input: n = 2\nOutput: 2\nExplanation: 1. 1+1  2. 2\n\nInput: n = 3\nOutput: 3\nExplanation: 1. 1+1+1  2. 1+2  3. 2+1")
                .difficulty(Difficulty.EASY)
                .topic("Dynamic Programming")
                .executionKey("CLIMB_STAIRS")
                .starterCodeJava("class Solution {\n    public int climbStairs(int n) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def climb_stairs(n):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int climbStairs(int n) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function climbStairs(n) {\n    // Your code here\n}")
                .build();

        Problem maxSubarray = Problem.builder()
                .title("Maximum Subarray")
                .description("Given an integer array nums, find the subarray with the largest sum, and return its sum.\n\nA subarray is a contiguous non-empty sequence of elements within an array.")
                .constraints("1 <= nums.length <= 10^5\n-10^4 <= nums[i] <= 10^4")
                .examples("Input: nums = [-2,1,-3,4,-1,2,1,-5,4]\nOutput: 6\nExplanation: The subarray [4,-1,2,1] has the largest sum 6.\n\nInput: nums = [1]\nOutput: 1")
                .difficulty(Difficulty.MEDIUM)
                .topic("Dynamic Programming")
                .executionKey("MAX_SUBARRAY")
                .starterCodeJava("class Solution {\n    public int maxSubArray(int[] nums) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def max_sub_array(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int maxSubArray(vector<int>& nums) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function maxSubArray(nums) {\n    // Your code here\n}")
                .build();

        Problem coinChange = Problem.builder()
                .title("Coin Change")
                .description("You are given an integer array coins representing coins of different denominations and an integer amount representing a total amount of money.\n\nReturn the fewest number of coins that you need to make up that amount. If that amount of money cannot be made up by any combination of the coins, return -1.\n\nYou may assume that you have an infinite number of each kind of coin.")
                .constraints("1 <= coins.length <= 12\n1 <= coins[i] <= 2^31 - 1\n0 <= amount <= 10^4")
                .examples("Input: coins = [1,5,10,25], amount = 30\nOutput: 2\nExplanation: 25 + 5 = 30\n\nInput: coins = [2], amount = 3\nOutput: -1")
                .difficulty(Difficulty.MEDIUM)
                .topic("Dynamic Programming")
                .executionKey("COIN_CHANGE")
                .starterCodeJava("class Solution {\n    public int coinChange(int[] coins, int amount) {\n        // Your code here\n        return -1;\n    }\n}")
                .starterCodePython("def coin_change(coins, amount):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int coinChange(vector<int>& coins, int amount) {\n        // Your code here\n        return -1;\n    }\n};")
                .starterCodeJavascript("function coinChange(coins, amount) {\n    // Your code here\n}")
                .build();

        Problem longestIncreasingSubsequence = Problem.builder()
                .title("Longest Increasing Subsequence")
                .description("Given an integer array nums, return the length of the longest strictly increasing subsequence.")
                .constraints("1 <= nums.length <= 2500\n-10^4 <= nums[i] <= 10^4")
                .examples("Input: nums = [10,9,2,5,3,7,101,18]\nOutput: 4\nExplanation: The LIS is [2,3,7,101], length = 4.\n\nInput: nums = [0,1,0,3,2,3]\nOutput: 4")
                .difficulty(Difficulty.MEDIUM)
                .topic("Dynamic Programming")
                .executionKey("LIS")
                .starterCodeJava("class Solution {\n    public int lengthOfLIS(int[] nums) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def length_of_lis(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int lengthOfLIS(vector<int>& nums) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function lengthOfLIS(nums) {\n    // Your code here\n}")
                .build();

        Problem houseRobber = Problem.builder()
                .title("House Robber")
                .description("You are a professional robber planning to rob houses along a street. Each house has a certain amount of money stashed, the only constraint stopping you from robbing each of them is that adjacent houses have security systems connected and it will automatically contact the police if two adjacent houses were broken into on the same night.\n\nGiven an integer array nums representing the amount of money of each house, return the maximum amount of money you can rob tonight without alerting the police.")
                .constraints("1 <= nums.length <= 100\n0 <= nums[i] <= 400")
                .examples("Input: nums = [1,2,3,1]\nOutput: 4\nExplanation: Rob house 1 (1) and house 3 (3) = 4.\n\nInput: nums = [2,7,9,3,1]\nOutput: 12")
                .difficulty(Difficulty.MEDIUM)
                .topic("Dynamic Programming")
                .executionKey("HOUSE_ROBBER")
                .starterCodeJava("class Solution {\n    public int rob(int[] nums) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def rob(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int rob(vector<int>& nums) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function rob(nums) {\n    // Your code here\n}")
                .build();

        Problem uniquePaths = Problem.builder()
                .title("Unique Paths")
                .description("There is a robot on an m x n grid. The robot is initially located at the top-left corner (i.e., grid[0][0]). The robot tries to move to the bottom-right corner (i.e., grid[m - 1][n - 1]). The robot can only move either down or right at any point in time.\n\nGiven the two integers m and n, return the number of possible unique paths that the robot can take to reach the bottom-right corner.")
                .constraints("1 <= m, n <= 100")
                .examples("Input: m = 3, n = 7\nOutput: 28\n\nInput: m = 3, n = 2\nOutput: 3")
                .difficulty(Difficulty.MEDIUM)
                .topic("Dynamic Programming")
                .executionKey("UNIQUE_PATHS")
                .starterCodeJava("class Solution {\n    public int uniquePaths(int m, int n) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def unique_paths(m, n):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int uniquePaths(int m, int n) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function uniquePaths(m, n) {\n    // Your code here\n}")
                .build();

        Problem wordBreak = Problem.builder()
                .title("Word Break")
                .description("Given a string s and a dictionary of strings wordDict, return true if s can be segmented into a space-separated sequence of one or more dictionary words.\n\nNote that the same word in the dictionary may be reused multiple times in the segmentation.")
                .constraints("1 <= s.length <= 300\n1 <= wordDict.length <= 1000\n1 <= wordDict[i].length <= 20\ns and wordDict[i] consist of only lowercase English letters.\nAll the strings of wordDict are unique.")
                .examples("Input: s = \"leetcode\", wordDict = [\"leet\",\"code\"]\nOutput: true\n\nInput: s = \"applepenapple\", wordDict = [\"apple\",\"pen\"]\nOutput: true")
                .difficulty(Difficulty.MEDIUM)
                .topic("Dynamic Programming")
                .executionKey("WORD_BREAK")
                .starterCodeJava("class Solution {\n    public boolean wordBreak(String s, List<String> wordDict) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def word_break(s, word_dict):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool wordBreak(string s, vector<string>& wordDict) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function wordBreak(s, wordDict) {\n    // Your code here\n}")
                .build();

        Problem editDistance = Problem.builder()
                .title("Edit Distance")
                .description("Given two strings word1 and word2, return the minimum number of operations required to convert word1 to word2.\n\nYou have the following three operations permitted on a word:\n- Insert a character\n- Delete a character\n- Replace a character")
                .constraints("0 <= word1.length, word2.length <= 500\nword1 and word2 consist of lowercase English letters.")
                .examples("Input: word1 = \"horse\", word2 = \"ros\"\nOutput: 3\nExplanation: horse -> rorse -> rose -> ros\n\nInput: word1 = \"intention\", word2 = \"execution\"\nOutput: 5")
                .difficulty(Difficulty.MEDIUM)
                .topic("Dynamic Programming")
                .executionKey("EDIT_DISTANCE")
                .starterCodeJava("class Solution {\n    public int minDistance(String word1, String word2) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def min_distance(word1, word2):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int minDistance(string word1, string word2) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function minDistance(word1, word2) {\n    // Your code here\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SHEET 8: GREEDY & SORTING
        // ═══════════════════════════════════════════════════════════════

        Problem bestTimeToBuy = Problem.builder()
                .title("Best Time to Buy and Sell Stock")
                .description("You are given an array prices where prices[i] is the price of a given stock on the ith day.\n\nYou want to maximize your profit by choosing a single day to buy one stock and choosing a different day in the future to sell that stock.\n\nReturn the maximum profit you can achieve from this transaction. If you cannot achieve any profit, return 0.")
                .constraints("1 <= prices.length <= 10^5\n0 <= prices[i] <= 10^4")
                .examples("Input: prices = [7,1,5,3,6,4]\nOutput: 5\nExplanation: Buy on day 2 (price = 1) and sell on day 5 (price = 6), profit = 5.\n\nInput: prices = [7,6,4,3,1]\nOutput: 0")
                .difficulty(Difficulty.EASY)
                .topic("Greedy & Sorting")
                .executionKey("BEST_TIME_BUY")
                .starterCodeJava("class Solution {\n    public int maxProfit(int[] prices) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def max_profit(prices):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int maxProfit(vector<int>& prices) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function maxProfit(prices) {\n    // Your code here\n}")
                .build();

        Problem jumpGame = Problem.builder()
                .title("Jump Game")
                .description("You are given an integer array nums. You are initially positioned at the array's first index, and each element in the array represents your maximum jump length at that position.\n\nReturn true if you can reach the last index, or false otherwise.")
                .constraints("1 <= nums.length <= 10^4\n0 <= nums[i] <= 10^5")
                .examples("Input: nums = [2,3,1,1,4]\nOutput: true\nExplanation: Jump 1 step from index 0 to 1, then 3 steps to the last index.\n\nInput: nums = [3,2,1,0,4]\nOutput: false")
                .difficulty(Difficulty.MEDIUM)
                .topic("Greedy & Sorting")
                .executionKey("JUMP_GAME")
                .starterCodeJava("class Solution {\n    public boolean canJump(int[] nums) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def can_jump(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool canJump(vector<int>& nums) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function canJump(nums) {\n    // Your code here\n}")
                .build();

        Problem mergeIntervals = Problem.builder()
                .title("Merge Intervals")
                .description("Given an array of intervals where intervals[i] = [starti, endi], merge all overlapping intervals, and return an array of the non-overlapping intervals that cover all the intervals in the input.")
                .constraints("1 <= intervals.length <= 10^4\nintervals[i].length == 2\n0 <= starti <= endi <= 10^4")
                .examples("Input: intervals = [[1,3],[2,6],[8,10],[15,18]]\nOutput: [[1,6],[8,10],[15,18]]\nExplanation: Intervals [1,3] and [2,6] overlap, merged to [1,6].\n\nInput: intervals = [[1,4],[4,5]]\nOutput: [[1,5]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Greedy & Sorting")
                .executionKey("MERGE_INTERVALS")
                .starterCodeJava("class Solution {\n    public int[][] merge(int[][] intervals) {\n        // Your code here\n        return new int[][]{};\n    }\n}")
                .starterCodePython("def merge(intervals):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<int>> merge(vector<vector<int>>& intervals) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function merge(intervals) {\n    // Your code here\n}")
                .build();

        Problem meetingRooms = Problem.builder()
                .title("Non-Overlapping Intervals")
                .description("Given an array of intervals intervals where intervals[i] = [starti, endi], return the minimum number of intervals you need to remove to make the rest of the intervals non-overlapping.\n\nNote that intervals which only touch at a point are non-overlapping. For example, [1, 2] and [2, 3] are non-overlapping.")
                .constraints("1 <= intervals.length <= 10^5\nintervals[i].length == 2\n-5 * 10^4 <= starti < endi <= 5 * 10^4")
                .examples("Input: intervals = [[1,2],[2,3],[3,4],[1,3]]\nOutput: 1\nExplanation: [1,3] can be removed.\n\nInput: intervals = [[1,2],[1,2],[1,2]]\nOutput: 2")
                .difficulty(Difficulty.MEDIUM)
                .topic("Greedy & Sorting")
                .executionKey("NON_OVERLAPPING")
                .starterCodeJava("class Solution {\n    public int eraseOverlapIntervals(int[][] intervals) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def erase_overlap_intervals(intervals):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int eraseOverlapIntervals(vector<vector<int>>& intervals) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function eraseOverlapIntervals(intervals) {\n    // Your code here\n}")
                .build();

        Problem taskScheduler = Problem.builder()
                .title("Task Scheduler")
                .description("You are given an array of CPU tasks, each represented by letters A to Z, and a cooling time n. Each cycle or interval allows the completion of one task. Tasks can be completed in any order, but there's a constraint: identical tasks must be separated by at least n intervals due to cooling time.\n\nReturn the minimum number of intervals the CPU will take to finish all the given tasks.")
                .constraints("1 <= tasks.length <= 10^4\ntasks[i] is an uppercase English letter.\n0 <= n <= 100")
                .examples("Input: tasks = [\"A\",\"A\",\"A\",\"B\",\"B\",\"B\"], n = 2\nOutput: 8\nExplanation: A -> B -> idle -> A -> B -> idle -> A -> B\n\nInput: tasks = [\"A\",\"C\",\"A\",\"B\",\"D\",\"B\"], n = 1\nOutput: 6")
                .difficulty(Difficulty.MEDIUM)
                .topic("Greedy & Sorting")
                .executionKey("TASK_SCHEDULER")
                .starterCodeJava("class Solution {\n    public int leastInterval(char[] tasks, int n) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def least_interval(tasks, n):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int leastInterval(vector<char>& tasks, int n) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function leastInterval(tasks, n) {\n    // Your code here\n}")
                .build();

        // ═══════════════════════════════════════════════════════════════
        // SAVE ALL PROBLEMS
        // ═══════════════════════════════════════════════════════════════

        // Sheet 1: Arrays & Hashing
        twoSum = problemRepository.save(twoSum);
        containsDuplicate = problemRepository.save(containsDuplicate);
        validAnagram = problemRepository.save(validAnagram);
        groupAnagrams = problemRepository.save(groupAnagrams);
        topKFrequent = problemRepository.save(topKFrequent);
        productExceptSelf = problemRepository.save(productExceptSelf);
        longestConsecutive = problemRepository.save(longestConsecutive);
        subarraySum = problemRepository.save(subarraySum);

        // Sheet 2: Strings
        reverseString = problemRepository.save(reverseString);
        validPalindrome = problemRepository.save(validPalindrome);
        longestPalindrome = problemRepository.save(longestPalindrome);
        longestSubstring = problemRepository.save(longestSubstring);
        stringToInteger = problemRepository.save(stringToInteger);
        minWindow = problemRepository.save(minWindow);

        // Sheet 3: Linked Lists
        reverseLinkedList = problemRepository.save(reverseLinkedList);
        mergeTwoLists = problemRepository.save(mergeTwoLists);
        linkedListCycle = problemRepository.save(linkedListCycle);
        removeNthFromEnd = problemRepository.save(removeNthFromEnd);
        mergeKLists = problemRepository.save(mergeKLists);

        // Sheet 4: Stacks & Queues
        validParentheses = problemRepository.save(validParentheses);
        minStack = problemRepository.save(minStack);
        dailyTemperatures = problemRepository.save(dailyTemperatures);
        evalRPN = problemRepository.save(evalRPN);
        largestRectangle = problemRepository.save(largestRectangle);

        // Sheet 5: Trees & BST
        invertTree = problemRepository.save(invertTree);
        maxDepth = problemRepository.save(maxDepth);
        isSameTree = problemRepository.save(isSameTree);
        levelOrder = problemRepository.save(levelOrder);
        validateBST = problemRepository.save(validateBST);
        lowestCommonAncestor = problemRepository.save(lowestCommonAncestor);
        serializeTree = problemRepository.save(serializeTree);

        // Sheet 6: Graphs
        numIslands = problemRepository.save(numIslands);
        cloneGraph = problemRepository.save(cloneGraph);
        courseSchedule = problemRepository.save(courseSchedule);
        pacificAtlantic = problemRepository.save(pacificAtlantic);
        networkDelay = problemRepository.save(networkDelay);
        alienDictionary = problemRepository.save(alienDictionary);

        // Sheet 7: Dynamic Programming
        climbStairs = problemRepository.save(climbStairs);
        maxSubarray = problemRepository.save(maxSubarray);
        coinChange = problemRepository.save(coinChange);
        longestIncreasingSubsequence = problemRepository.save(longestIncreasingSubsequence);
        houseRobber = problemRepository.save(houseRobber);
        uniquePaths = problemRepository.save(uniquePaths);
        wordBreak = problemRepository.save(wordBreak);
        editDistance = problemRepository.save(editDistance);

        // Sheet 8: Greedy & Sorting
        bestTimeToBuy = problemRepository.save(bestTimeToBuy);
        jumpGame = problemRepository.save(jumpGame);
        mergeIntervals = problemRepository.save(mergeIntervals);
        meetingRooms = problemRepository.save(meetingRooms);
        taskScheduler = problemRepository.save(taskScheduler);

        // ═══════════════════════════════════════════════════════════════
        // SEED TEST CASES
        // ═══════════════════════════════════════════════════════════════

        testCaseRepository.saveAll(List.of(
                // Two Sum
                TestCase.builder().problem(twoSum).input("2 7 11 15\n9").expectedOutput("0 1").isHidden(false).build(),
                TestCase.builder().problem(twoSum).input("3 2 4\n6").expectedOutput("1 2").isHidden(false).build(),
                TestCase.builder().problem(twoSum).input("3 3\n6").expectedOutput("0 1").isHidden(true).build(),
                // Contains Duplicate
                TestCase.builder().problem(containsDuplicate).input("1 2 3 1").expectedOutput("true").isHidden(false).build(),
                TestCase.builder().problem(containsDuplicate).input("1 2 3 4").expectedOutput("false").isHidden(false).build(),
                TestCase.builder().problem(containsDuplicate).input("1 1 1 3 3 4 3 2 4 2").expectedOutput("true").isHidden(true).build(),
                // Valid Anagram
                TestCase.builder().problem(validAnagram).input("anagram\nnagaram").expectedOutput("true").isHidden(false).build(),
                TestCase.builder().problem(validAnagram).input("rat\ncar").expectedOutput("false").isHidden(false).build(),
                // Reverse String
                TestCase.builder().problem(reverseString).input("hello").expectedOutput("olleh").isHidden(false).build(),
                TestCase.builder().problem(reverseString).input("Hannah").expectedOutput("hannaH").isHidden(true).build(),
                // Max Subarray
                TestCase.builder().problem(maxSubarray).input("-2 1 -3 4 -1 2 1 -5 4").expectedOutput("6").isHidden(false).build(),
                TestCase.builder().problem(maxSubarray).input("1").expectedOutput("1").isHidden(false).build(),
                TestCase.builder().problem(maxSubarray).input("5 4 -1 7 8").expectedOutput("23").isHidden(true).build(),
                // Valid Parentheses
                TestCase.builder().problem(validParentheses).input("()").expectedOutput("true").isHidden(false).build(),
                TestCase.builder().problem(validParentheses).input("()[]{}").expectedOutput("true").isHidden(false).build(),
                TestCase.builder().problem(validParentheses).input("(]").expectedOutput("false").isHidden(false).build(),
                // Climbing Stairs
                TestCase.builder().problem(climbStairs).input("2").expectedOutput("2").isHidden(false).build(),
                TestCase.builder().problem(climbStairs).input("3").expectedOutput("3").isHidden(false).build(),
                TestCase.builder().problem(climbStairs).input("5").expectedOutput("8").isHidden(true).build(),
                // Coin Change
                TestCase.builder().problem(coinChange).input("1 5 10 25\n30").expectedOutput("2").isHidden(false).build(),
                TestCase.builder().problem(coinChange).input("2\n3").expectedOutput("-1").isHidden(false).build(),
                // Best Time to Buy
                TestCase.builder().problem(bestTimeToBuy).input("7 1 5 3 6 4").expectedOutput("5").isHidden(false).build(),
                TestCase.builder().problem(bestTimeToBuy).input("7 6 4 3 1").expectedOutput("0").isHidden(false).build(),
                // Number of Islands
                TestCase.builder().problem(numIslands).input("1 1 1 1 0\n1 1 0 1 0\n1 1 0 0 0\n0 0 0 0 0").expectedOutput("1").isHidden(false).build(),
                TestCase.builder().problem(numIslands).input("1 1 0 0 0\n1 1 0 0 0\n0 0 1 0 0\n0 0 0 1 1").expectedOutput("3").isHidden(false).build(),
                // Merge Intervals
                TestCase.builder().problem(mergeIntervals).input("1 3\n2 6\n8 10\n15 18").expectedOutput("1 6\n8 10\n15 18").isHidden(false).build(),
                TestCase.builder().problem(mergeIntervals).input("1 4\n4 5").expectedOutput("1 5").isHidden(false).build(),
                // House Robber
                TestCase.builder().problem(houseRobber).input("1 2 3 1").expectedOutput("4").isHidden(false).build(),
                TestCase.builder().problem(houseRobber).input("2 7 9 3 1").expectedOutput("12").isHidden(false).build(),
                // Jump Game
                TestCase.builder().problem(jumpGame).input("2 3 1 1 4").expectedOutput("true").isHidden(false).build(),
                TestCase.builder().problem(jumpGame).input("3 2 1 0 4").expectedOutput("false").isHidden(false).build()
        ));

        // ═══════════════════════════════════════════════════════════════
        // SEED 8 ROADMAPS
        // ═══════════════════════════════════════════════════════════════

        Roadmap arraysRoadmap = Roadmap.builder()
                .title("Arrays & Hashing")
                .description("Master fundamental array techniques, hash maps, prefix sums, and sliding window patterns.")
                .icon("📊")
                .displayOrder(1)
                .build();

        Roadmap stringsRoadmap = Roadmap.builder()
                .title("Strings")
                .description("From palindromes to pattern matching — master string manipulation and two-pointer techniques.")
                .icon("🔤")
                .displayOrder(2)
                .build();

        Roadmap linkedListRoadmap = Roadmap.builder()
                .title("Linked Lists")
                .description("Learn reversal, fast/slow pointers, cycle detection, and merge operations on linked lists.")
                .icon("🔗")
                .displayOrder(3)
                .build();

        Roadmap stackQueueRoadmap = Roadmap.builder()
                .title("Stacks & Queues")
                .description("Conquer monotonic stacks, parentheses validation, and expression evaluation problems.")
                .icon("📚")
                .displayOrder(4)
                .build();

        Roadmap treeRoadmap = Roadmap.builder()
                .title("Trees & BST")
                .description("Traverse, validate, and construct binary trees. Master DFS, BFS, and BST properties.")
                .icon("🌲")
                .displayOrder(5)
                .build();

        Roadmap graphRoadmap = Roadmap.builder()
                .title("Graphs")
                .description("Navigate graph traversals, topological sort, shortest paths, and connected components.")
                .icon("🕸️")
                .displayOrder(6)
                .build();

        Roadmap dpRoadmap = Roadmap.builder()
                .title("Dynamic Programming")
                .description("Build intuition for optimal substructure, overlapping subproblems, and state transitions.")
                .icon("🧠")
                .displayOrder(7)
                .build();

        Roadmap greedyRoadmap = Roadmap.builder()
                .title("Greedy & Sorting")
                .description("Solve interval scheduling, stock trading, and optimization problems with greedy strategies.")
                .icon("⚡")
                .displayOrder(8)
                .build();

        arraysRoadmap = roadmapRepository.save(arraysRoadmap);
        stringsRoadmap = roadmapRepository.save(stringsRoadmap);
        linkedListRoadmap = roadmapRepository.save(linkedListRoadmap);
        stackQueueRoadmap = roadmapRepository.save(stackQueueRoadmap);
        treeRoadmap = roadmapRepository.save(treeRoadmap);
        graphRoadmap = roadmapRepository.save(graphRoadmap);
        dpRoadmap = roadmapRepository.save(dpRoadmap);
        greedyRoadmap = roadmapRepository.save(greedyRoadmap);

        // Arrays & Hashing roadmap items
        arraysRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(twoSum).sectionTitle("Basics").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(containsDuplicate).sectionTitle("Basics").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(validAnagram).sectionTitle("Hash Maps").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(groupAnagrams).sectionTitle("Hash Maps").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(topKFrequent).sectionTitle("Frequency").displayOrder(5).build(),
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(productExceptSelf).sectionTitle("Prefix").displayOrder(6).build(),
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(longestConsecutive).sectionTitle("Sets").displayOrder(7).build(),
                RoadmapItem.builder().roadmap(arraysRoadmap).problem(subarraySum).sectionTitle("Prefix Sums").displayOrder(8).build()
        ));

        // Strings roadmap items
        stringsRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(stringsRoadmap).problem(reverseString).sectionTitle("Basics").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(stringsRoadmap).problem(validPalindrome).sectionTitle("Two Pointers").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(stringsRoadmap).problem(longestPalindrome).sectionTitle("Expand Around Center").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(stringsRoadmap).problem(longestSubstring).sectionTitle("Sliding Window").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(stringsRoadmap).problem(stringToInteger).sectionTitle("Parsing").displayOrder(5).build(),
                RoadmapItem.builder().roadmap(stringsRoadmap).problem(minWindow).sectionTitle("Sliding Window").displayOrder(6).build()
        ));

        // Linked Lists roadmap items
        linkedListRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(linkedListRoadmap).problem(reverseLinkedList).sectionTitle("Basics").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(linkedListRoadmap).problem(mergeTwoLists).sectionTitle("Merge").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(linkedListRoadmap).problem(linkedListCycle).sectionTitle("Cycle Detection").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(linkedListRoadmap).problem(removeNthFromEnd).sectionTitle("Two Pointers").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(linkedListRoadmap).problem(mergeKLists).sectionTitle("Advanced").displayOrder(5).build()
        ));

        // Stacks & Queues roadmap items
        stackQueueRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(stackQueueRoadmap).problem(validParentheses).sectionTitle("Stack Basics").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(stackQueueRoadmap).problem(minStack).sectionTitle("Design").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(stackQueueRoadmap).problem(dailyTemperatures).sectionTitle("Monotonic Stack").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(stackQueueRoadmap).problem(evalRPN).sectionTitle("Expression Eval").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(stackQueueRoadmap).problem(largestRectangle).sectionTitle("Advanced Stack").displayOrder(5).build()
        ));

        // Trees & BST roadmap items
        treeRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(treeRoadmap).problem(invertTree).sectionTitle("Basic Traversal").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(treeRoadmap).problem(maxDepth).sectionTitle("Basic Traversal").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(treeRoadmap).problem(isSameTree).sectionTitle("Comparison").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(treeRoadmap).problem(levelOrder).sectionTitle("BFS").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(treeRoadmap).problem(validateBST).sectionTitle("BST Properties").displayOrder(5).build(),
                RoadmapItem.builder().roadmap(treeRoadmap).problem(lowestCommonAncestor).sectionTitle("BST Properties").displayOrder(6).build(),
                RoadmapItem.builder().roadmap(treeRoadmap).problem(serializeTree).sectionTitle("Serialization").displayOrder(7).build()
        ));

        // Graphs roadmap items
        graphRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(graphRoadmap).problem(numIslands).sectionTitle("DFS/BFS").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(graphRoadmap).problem(cloneGraph).sectionTitle("DFS/BFS").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(graphRoadmap).problem(courseSchedule).sectionTitle("Topological Sort").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(graphRoadmap).problem(pacificAtlantic).sectionTitle("Multi-Source BFS").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(graphRoadmap).problem(networkDelay).sectionTitle("Shortest Path").displayOrder(5).build(),
                RoadmapItem.builder().roadmap(graphRoadmap).problem(alienDictionary).sectionTitle("BFS").displayOrder(6).build()
        ));

        // Dynamic Programming roadmap items
        dpRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(dpRoadmap).problem(climbStairs).sectionTitle("1D DP").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(dpRoadmap).problem(maxSubarray).sectionTitle("1D DP").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(dpRoadmap).problem(houseRobber).sectionTitle("1D DP").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(dpRoadmap).problem(coinChange).sectionTitle("Unbounded Knapsack").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(dpRoadmap).problem(longestIncreasingSubsequence).sectionTitle("Subsequence").displayOrder(5).build(),
                RoadmapItem.builder().roadmap(dpRoadmap).problem(uniquePaths).sectionTitle("2D DP").displayOrder(6).build(),
                RoadmapItem.builder().roadmap(dpRoadmap).problem(wordBreak).sectionTitle("String DP").displayOrder(7).build(),
                RoadmapItem.builder().roadmap(dpRoadmap).problem(editDistance).sectionTitle("String DP").displayOrder(8).build()
        ));

        // Greedy & Sorting roadmap items
        greedyRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(greedyRoadmap).problem(bestTimeToBuy).sectionTitle("Greedy Basics").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(greedyRoadmap).problem(jumpGame).sectionTitle("Greedy").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(greedyRoadmap).problem(mergeIntervals).sectionTitle("Intervals").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(greedyRoadmap).problem(meetingRooms).sectionTitle("Intervals").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(greedyRoadmap).problem(taskScheduler).sectionTitle("Scheduling").displayOrder(5).build()
        ));

        // ═══════════════════════════════════════════════════════════════
        // SHEET 9: BINARY SEARCH
        // ═══════════════════════════════════════════════════════════════

        Problem binarySearch = Problem.builder()
                .title("Binary Search")
                .description("Given an array of integers nums which is sorted in ascending order, and an integer target, write a function to search target in nums. If target exists, then return its index. Otherwise, return -1.\n\nYou must write an algorithm with O(log n) runtime complexity.")
                .constraints("1 <= nums.length <= 10^4\n-10^4 < nums[i], target < 10^4\nAll the integers in nums are unique.\nnums is sorted in ascending order.")
                .examples("Input: nums = [-1,0,3,5,9,12], target = 9\nOutput: 4\n\nInput: nums = [-1,0,3,5,9,12], target = 2\nOutput: -1")
                .difficulty(Difficulty.EASY)
                .topic("Binary Search")
                .executionKey("BINARY_SEARCH")
                .starterCodeJava("class Solution {\n    public int search(int[] nums, int target) {\n        // Your code here\n        return -1;\n    }\n}")
                .starterCodePython("def search(nums, target):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int search(vector<int>& nums, int target) {\n        // Your code here\n        return -1;\n    }\n};")
                .starterCodeJavascript("function search(nums, target) {\n    // Your code here\n}")
                .build();
        allProblems.add(binarySearch);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(binarySearch).input("[-1,0,3,5,9,12]\n9").expectedOutput("4").isHidden(false).build(),
                TestCase.builder().problem(binarySearch).input("[-1,0,3,5,9,12]\n2").expectedOutput("-1").isHidden(false).build(),
                TestCase.builder().problem(binarySearch).input("[5]\n5").expectedOutput("0").isHidden(true).build()
        ));

        Problem searchMatrix = Problem.builder()
                .title("Search a 2D Matrix")
                .description("You are given an m x n integer matrix matrix with the following two properties:\n\n- Each row is sorted in non-decreasing order.\n- The first integer of each row is greater than the last integer of the previous row.\n\nGiven an integer target, return true if target is in matrix or false otherwise.\n\nYou must write an algorithm with O(log(m * n)) runtime complexity.")
                .constraints("m == matrix.length\nn == matrix[i].length\n1 <= m, n <= 100\n-10^4 <= matrix[i][j], target <= 10^4")
                .examples("Input: matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 3\nOutput: true\n\nInput: matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 13\nOutput: false")
                .difficulty(Difficulty.MEDIUM)
                .topic("Binary Search")
                .executionKey("SEARCH_MATRIX")
                .starterCodeJava("class Solution {\n    public boolean searchMatrix(int[][] matrix, int target) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def search_matrix(matrix, target):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool searchMatrix(vector<vector<int>>& matrix, int target) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function searchMatrix(matrix, target) {\n    // Your code here\n}")
                .build();
        allProblems.add(searchMatrix);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(searchMatrix).input("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]\n3").expectedOutput("true").isHidden(false).build(),
                TestCase.builder().problem(searchMatrix).input("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]\n13").expectedOutput("false").isHidden(false).build()
        ));

        Problem kokoEatingBananas = Problem.builder()
                .title("Koko Eating Bananas")
                .description("Koko loves to eat bananas. There are n piles of bananas, the ith pile has piles[i] bananas. The guards have gone and will come back in h hours.\n\nKoko can decide her bananas-per-hour eating speed of k. Each hour, she chooses some pile of bananas and eats k bananas from that pile. If the pile has less than k bananas, she eats all of them instead and will not eat any more bananas during this hour.\n\nReturn the minimum integer k such that she can eat all the bananas within h hours.")
                .constraints("1 <= piles.length <= 10^4\npiles.length <= h <= 10^9\n1 <= piles[i] <= 10^9")
                .examples("Input: piles = [3,6,7,11], h = 8\nOutput: 4\n\nInput: piles = [30,11,23,4,20], h = 5\nOutput: 30")
                .difficulty(Difficulty.MEDIUM)
                .topic("Binary Search")
                .executionKey("KOKO_BANANAS")
                .starterCodeJava("class Solution {\n    public int minEatingSpeed(int[] piles, int h) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def min_eating_speed(piles, h):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int minEatingSpeed(vector<int>& piles, int h) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function minEatingSpeed(piles, h) {\n    // Your code here\n}")
                .build();
        allProblems.add(kokoEatingBananas);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(kokoEatingBananas).input("[3,6,7,11]\n8").expectedOutput("4").isHidden(false).build(),
                TestCase.builder().problem(kokoEatingBananas).input("[30,11,23,4,20]\n5").expectedOutput("30").isHidden(false).build()
        ));

        Problem findMinRotated = Problem.builder()
                .title("Find Minimum in Rotated Sorted Array")
                .description("Suppose an array of length n sorted in ascending order is rotated between 1 and n times. For example, the array nums = [0,1,2,4,5,6,7] might become [4,5,6,7,0,1,2].\n\nGiven the sorted rotated array nums of unique elements, return the minimum element of this array.\n\nYou must write an algorithm that runs in O(log n) time.")
                .constraints("n == nums.length\n1 <= n <= 5000\n-5000 <= nums[i] <= 5000\nAll values are unique.\nnums is sorted and rotated.")
                .examples("Input: nums = [3,4,5,1,2]\nOutput: 1\n\nInput: nums = [4,5,6,7,0,1,2]\nOutput: 0")
                .difficulty(Difficulty.MEDIUM)
                .topic("Binary Search")
                .executionKey("FIND_MIN_ROTATED")
                .starterCodeJava("class Solution {\n    public int findMin(int[] nums) {\n        // Your code here\n        return 0;\n    }\n}")
                .starterCodePython("def find_min(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int findMin(vector<int>& nums) {\n        // Your code here\n        return 0;\n    }\n};")
                .starterCodeJavascript("function findMin(nums) {\n    // Your code here\n}")
                .build();
        allProblems.add(findMinRotated);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(findMinRotated).input("[3,4,5,1,2]").expectedOutput("1").isHidden(false).build(),
                TestCase.builder().problem(findMinRotated).input("[4,5,6,7,0,1,2]").expectedOutput("0").isHidden(false).build()
        ));

        Problem searchRotated = Problem.builder()
                .title("Search in Rotated Sorted Array")
                .description("There is an integer array nums sorted in ascending order (with distinct values). Prior to being passed to your function, nums is possibly rotated at an unknown pivot index.\n\nGiven the array nums after the possible rotation and an integer target, return the index of target if it is in nums, or -1 if it is not in nums.\n\nYou must write an algorithm with O(log n) runtime complexity.")
                .constraints("1 <= nums.length <= 5000\n-10^4 <= nums[i] <= 10^4\nAll values are unique.\nnums is an ascending array that is possibly rotated.")
                .examples("Input: nums = [4,5,6,7,0,1,2], target = 0\nOutput: 4\n\nInput: nums = [4,5,6,7,0,1,2], target = 3\nOutput: -1")
                .difficulty(Difficulty.MEDIUM)
                .topic("Binary Search")
                .executionKey("SEARCH_ROTATED")
                .starterCodeJava("class Solution {\n    public int search(int[] nums, int target) {\n        // Your code here\n        return -1;\n    }\n}")
                .starterCodePython("def search(nums, target):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    int search(vector<int>& nums, int target) {\n        // Your code here\n        return -1;\n    }\n};")
                .starterCodeJavascript("function search(nums, target) {\n    // Your code here\n}")
                .build();
        allProblems.add(searchRotated);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(searchRotated).input("[4,5,6,7,0,1,2]\n0").expectedOutput("4").isHidden(false).build(),
                TestCase.builder().problem(searchRotated).input("[4,5,6,7,0,1,2]\n3").expectedOutput("-1").isHidden(false).build()
        ));

        Problem medianSortedArrays = Problem.builder()
                .title("Median of Two Sorted Arrays")
                .description("Given two sorted arrays nums1 and nums2 of size m and n respectively, return the median of the two sorted arrays.\n\nThe overall run time complexity should be O(log (m+n)).")
                .constraints("nums1.length == m\nnums2.length == n\n0 <= m <= 1000\n0 <= n <= 1000\n1 <= m + n <= 2000\n-10^6 <= nums1[i], nums2[i] <= 10^6")
                .examples("Input: nums1 = [1,3], nums2 = [2]\nOutput: 2.00000\n\nInput: nums1 = [1,2], nums2 = [3,4]\nOutput: 2.50000")
                .difficulty(Difficulty.HARD)
                .topic("Binary Search")
                .executionKey("MEDIAN_SORTED")
                .starterCodeJava("class Solution {\n    public double findMedianSortedArrays(int[] nums1, int[] nums2) {\n        // Your code here\n        return 0.0;\n    }\n}")
                .starterCodePython("def find_median_sorted_arrays(nums1, nums2):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    double findMedianSortedArrays(vector<int>& nums1, vector<int>& nums2) {\n        // Your code here\n        return 0.0;\n    }\n};")
                .starterCodeJavascript("function findMedianSortedArrays(nums1, nums2) {\n    // Your code here\n}")
                .build();
        allProblems.add(medianSortedArrays);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(medianSortedArrays).input("[1,3]\n[2]").expectedOutput("2.00000").isHidden(false).build(),
                TestCase.builder().problem(medianSortedArrays).input("[1,2]\n[3,4]").expectedOutput("2.50000").isHidden(false).build()
        ));

        Problem timeBasedKV = Problem.builder()
                .title("Time Based Key-Value Store")
                .description("Design a time-based key-value data structure that can store multiple values for the same key at different time stamps and retrieve the key's value at a certain timestamp.\n\nImplement the TimeMap class:\n- TimeMap() Initializes the object.\n- void set(String key, String value, int timestamp) Stores the key with the value at the given timestamp.\n- String get(String key, int timestamp) Returns a value such that set was called previously, with timestamp_prev <= timestamp.")
                .constraints("1 <= key.length, value.length <= 100\nkey and value consist of lowercase English letters and digits.\n1 <= timestamp <= 10^7\nAll timestamps of set are strictly increasing.\nAt most 2 * 10^5 calls will be made to set and get.")
                .examples("Input:\n[\"TimeMap\", \"set\", \"get\", \"get\", \"set\", \"get\"]\n[[], [\"foo\", \"bar\", 1], [\"foo\", 1], [\"foo\", 3], [\"foo\", \"bar2\", 4], [\"foo\", 4]]\nOutput: [null, null, \"bar\", \"bar\", null, \"bar2\"]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Binary Search")
                .executionKey("TIME_BASED_KV")
                .starterCodeJava("class TimeMap {\n    public TimeMap() {\n        // Your code here\n    }\n    \n    public void set(String key, String value, int timestamp) {\n        // Your code here\n    }\n    \n    public String get(String key, int timestamp) {\n        // Your code here\n        return \"\";\n    }\n}")
                .starterCodePython("class TimeMap:\n    def __init__(self):\n        pass\n    \n    def set(self, key, value, timestamp):\n        pass\n    \n    def get(self, key, timestamp):\n        pass")
                .starterCodeCpp("class TimeMap {\npublic:\n    TimeMap() {}\n    \n    void set(string key, string value, int timestamp) {\n        // Your code here\n    }\n    \n    string get(string key, int timestamp) {\n        // Your code here\n        return \"\";\n    }\n};")
                .starterCodeJavascript("class TimeMap {\n    constructor() {\n    }\n    \n    set(key, value, timestamp) {\n    }\n    \n    get(key, timestamp) {\n    }\n}")
                .build();
        allProblems.add(timeBasedKV);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(timeBasedKV).input("set foo bar 1\nget foo 1").expectedOutput("bar").isHidden(false).build(),
                TestCase.builder().problem(timeBasedKV).input("set foo bar 1\nget foo 3").expectedOutput("bar").isHidden(false).build()
        ));

        // ═══════════════════════════════════════════════════════════════
        // SHEET 10: BACKTRACKING
        // ═══════════════════════════════════════════════════════════════

        Problem subsets = Problem.builder()
                .title("Subsets")
                .description("Given an integer array nums of unique elements, return all possible subsets (the power set).\n\nThe solution set must not contain duplicate subsets. Return the solution in any order.")
                .constraints("1 <= nums.length <= 10\n-10 <= nums[i] <= 10\nAll the numbers of nums are unique.")
                .examples("Input: nums = [1,2,3]\nOutput: [[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]\n\nInput: nums = [0]\nOutput: [[],[0]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Backtracking")
                .executionKey("SUBSETS")
                .starterCodeJava("class Solution {\n    public List<List<Integer>> subsets(int[] nums) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def subsets(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<int>> subsets(vector<int>& nums) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function subsets(nums) {\n    // Your code here\n}")
                .build();
        allProblems.add(subsets);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(subsets).input("[1,2,3]").expectedOutput("[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]").isHidden(false).build(),
                TestCase.builder().problem(subsets).input("[0]").expectedOutput("[[],[0]]").isHidden(false).build()
        ));

        Problem permutations = Problem.builder()
                .title("Permutations")
                .description("Given an array nums of distinct integers, return all the possible permutations. You can return the answer in any order.")
                .constraints("1 <= nums.length <= 6\n-10 <= nums[i] <= 10\nAll the integers of nums are unique.")
                .examples("Input: nums = [1,2,3]\nOutput: [[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]\n\nInput: nums = [0,1]\nOutput: [[0,1],[1,0]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Backtracking")
                .executionKey("PERMUTATIONS")
                .starterCodeJava("class Solution {\n    public List<List<Integer>> permute(int[] nums) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def permute(nums):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<int>> permute(vector<int>& nums) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function permute(nums) {\n    // Your code here\n}")
                .build();
        allProblems.add(permutations);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(permutations).input("[1,2,3]").expectedOutput("[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]").isHidden(false).build(),
                TestCase.builder().problem(permutations).input("[0,1]").expectedOutput("[[0,1],[1,0]]").isHidden(false).build()
        ));

        Problem combinationSum = Problem.builder()
                .title("Combination Sum")
                .description("Given an array of distinct integers candidates and a target integer target, return a list of all unique combinations of candidates where the chosen numbers sum to target. You may return the combinations in any order.\n\nThe same number may be chosen from candidates an unlimited number of times.")
                .constraints("1 <= candidates.length <= 30\n2 <= candidates[i] <= 40\nAll elements are distinct.\n1 <= target <= 40")
                .examples("Input: candidates = [2,3,6,7], target = 7\nOutput: [[2,2,3],[7]]\n\nInput: candidates = [2,3,5], target = 8\nOutput: [[2,2,2,2],[2,3,3],[3,5]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Backtracking")
                .executionKey("COMBINATION_SUM")
                .starterCodeJava("class Solution {\n    public List<List<Integer>> combinationSum(int[] candidates, int target) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def combination_sum(candidates, target):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<int>> combinationSum(vector<int>& candidates, int target) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function combinationSum(candidates, target) {\n    // Your code here\n}")
                .build();
        allProblems.add(combinationSum);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(combinationSum).input("[2,3,6,7]\n7").expectedOutput("[[2,2,3],[7]]").isHidden(false).build(),
                TestCase.builder().problem(combinationSum).input("[2,3,5]\n8").expectedOutput("[[2,2,2,2],[2,3,3],[3,5]]").isHidden(false).build()
        ));

        Problem wordSearch = Problem.builder()
                .title("Word Search")
                .description("Given an m x n grid of characters board and a string word, return true if word exists in the grid.\n\nThe word can be constructed from letters of sequentially adjacent cells, where adjacent cells are horizontally or vertically neighboring. The same letter cell may not be used more than once.")
                .constraints("m == board.length\nn = board[i].length\n1 <= m, n <= 6\n1 <= word.length <= 15\nboard and word consists of only lowercase and uppercase English letters.")
                .examples("Input: board = [[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], word = \"ABCCED\"\nOutput: true\n\nInput: board = [[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], word = \"SEE\"\nOutput: true")
                .difficulty(Difficulty.MEDIUM)
                .topic("Backtracking")
                .executionKey("WORD_SEARCH")
                .starterCodeJava("class Solution {\n    public boolean exist(char[][] board, String word) {\n        // Your code here\n        return false;\n    }\n}")
                .starterCodePython("def exist(board, word):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    bool exist(vector<vector<char>>& board, string word) {\n        // Your code here\n        return false;\n    }\n};")
                .starterCodeJavascript("function exist(board, word) {\n    // Your code here\n}")
                .build();
        allProblems.add(wordSearch);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(wordSearch).input("[[A,B,C,E],[S,F,C,S],[A,D,E,E]]\nABCCED").expectedOutput("true").isHidden(false).build(),
                TestCase.builder().problem(wordSearch).input("[[A,B,C,E],[S,F,C,S],[A,D,E,E]]\nSEE").expectedOutput("true").isHidden(false).build()
        ));

        Problem nQueens = Problem.builder()
                .title("N-Queens")
                .description("The n-queens puzzle is the problem of placing n queens on an n x n chessboard such that no two queens attack each other.\n\nGiven an integer n, return all distinct solutions to the n-queens puzzle. You may return the answer in any order.\n\nEach solution contains a distinct board configuration of the n-queens placement, where 'Q' and '.' both indicate a queen and an empty space, respectively.")
                .constraints("1 <= n <= 9")
                .examples("Input: n = 4\nOutput: [[\".Q..\",\"...Q\",\"Q...\",\"..Q.\"],[\"..Q.\",\"Q...\",\"...Q\",\".Q..\"]]\n\nInput: n = 1\nOutput: [[\"Q\"]]")
                .difficulty(Difficulty.HARD)
                .topic("Backtracking")
                .executionKey("N_QUEENS")
                .starterCodeJava("class Solution {\n    public List<List<String>> solveNQueens(int n) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def solve_n_queens(n):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<string>> solveNQueens(int n) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function solveNQueens(n) {\n    // Your code here\n}")
                .build();
        allProblems.add(nQueens);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(nQueens).input("4").expectedOutput("[[\".Q..\",\"...Q\",\"Q...\",\"..Q.\"],[\"..Q.\",\"Q...\",\"...Q\",\".Q..\"]]").isHidden(false).build(),
                TestCase.builder().problem(nQueens).input("1").expectedOutput("[[\"Q\"]]").isHidden(false).build()
        ));

        Problem sudokuSolver = Problem.builder()
                .title("Sudoku Solver")
                .description("Write a program to solve a Sudoku puzzle by filling the empty cells.\n\nA sudoku solution must satisfy all of the following rules:\n1. Each of the digits 1-9 must occur exactly once in each row.\n2. Each of the digits 1-9 must occur exactly once in each column.\n3. Each of the digits 1-9 must occur exactly once in each of the 9 3x3 sub-boxes of the grid.\n\nThe '.' character indicates empty cells.")
                .constraints("board.length == 9\nboard[i].length == 9\nboard[i][j] is a digit or '.'.\nIt is guaranteed that the input board has only one solution.")
                .examples("Input: board = [[\"5\",\"3\",\".\",\".\",\"7\",\".\",\".\",\".\",\".\"],[\"6\",\".\",\".\",\"1\",\"9\",\"5\",\".\",\".\",\".\"],[\".\",\"9\",\"8\",\".\",\".\",\".\",\".\",\"6\",\".\"],[\"8\",\".\",\".\",\".\",\"6\",\".\",\".\",\".\",\"3\"],[\"4\",\".\",\".\",\"8\",\".\",\"3\",\".\",\".\",\"1\"],[\"7\",\".\",\".\",\".\",\"2\",\".\",\".\",\".\",\"6\"],[\".\",\"6\",\".\",\".\",\".\",\".\",\"2\",\"8\",\".\"],[\".\",\".\",\".\",\"4\",\"1\",\"9\",\".\",\".\",\"5\"],[\".\",\".\",\".\",\".\",\"8\",\".\",\".\",\"7\",\"9\"]]\nOutput: Solved board")
                .difficulty(Difficulty.HARD)
                .topic("Backtracking")
                .executionKey("SUDOKU_SOLVER")
                .starterCodeJava("class Solution {\n    public void solveSudoku(char[][] board) {\n        // Your code here\n    }\n}")
                .starterCodePython("def solve_sudoku(board):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    void solveSudoku(vector<vector<char>>& board) {\n        // Your code here\n    }\n};")
                .starterCodeJavascript("function solveSudoku(board) {\n    // Your code here\n}")
                .build();
        allProblems.add(sudokuSolver);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(sudokuSolver).input("[[5,3,.,.,7,.,.,.,.],[6,.,.,1,9,5,.,.,.],[.,9,8,.,.,.,.,6,.],[8,.,.,.,6,.,.,.,3],[4,.,.,8,.,3,.,.,1],[7,.,.,.,2,.,.,.,6],[.,6,.,.,.,.,2,8,.],[.,.,.,4,1,9,.,.,5],[.,.,.,.,8,.,.,7,9]]").expectedOutput("solved").isHidden(false).build()
        ));

        Problem letterCombinations = Problem.builder()
                .title("Letter Combinations of a Phone Number")
                .description("Given a string containing digits from 2-9 inclusive, return all possible letter combinations that the number could represent. Return the answer in any order.\n\nA mapping of digits to letters (just like on the telephone buttons) is given. Note that 1 does not map to any letters.")
                .constraints("0 <= digits.length <= 4\ndigits[i] is a digit in the range ['2', '9'].")
                .examples("Input: digits = \"23\"\nOutput: [\"ad\",\"ae\",\"af\",\"bd\",\"be\",\"bf\",\"cd\",\"ce\",\"cf\"]\n\nInput: digits = \"\"\nOutput: []")
                .difficulty(Difficulty.MEDIUM)
                .topic("Backtracking")
                .executionKey("LETTER_COMBOS")
                .starterCodeJava("class Solution {\n    public List<String> letterCombinations(String digits) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def letter_combinations(digits):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<string> letterCombinations(string digits) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function letterCombinations(digits) {\n    // Your code here\n}")
                .build();
        allProblems.add(letterCombinations);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(letterCombinations).input("23").expectedOutput("[\"ad\",\"ae\",\"af\",\"bd\",\"be\",\"bf\",\"cd\",\"ce\",\"cf\"]").isHidden(false).build(),
                TestCase.builder().problem(letterCombinations).input("").expectedOutput("[]").isHidden(false).build()
        ));

        Problem palindromePartitioning = Problem.builder()
                .title("Palindrome Partitioning")
                .description("Given a string s, partition s such that every substring of the partition is a palindrome. Return all possible palindrome partitioning of s.")
                .constraints("1 <= s.length <= 16\ns contains only lowercase English letters.")
                .examples("Input: s = \"aab\"\nOutput: [[\"a\",\"a\",\"b\"],[\"aa\",\"b\"]]\n\nInput: s = \"a\"\nOutput: [[\"a\"]]")
                .difficulty(Difficulty.MEDIUM)
                .topic("Backtracking")
                .executionKey("PALINDROME_PARTITION")
                .starterCodeJava("class Solution {\n    public List<List<String>> partition(String s) {\n        // Your code here\n        return new ArrayList<>();\n    }\n}")
                .starterCodePython("def partition(s):\n    # Your code here\n    pass")
                .starterCodeCpp("class Solution {\npublic:\n    vector<vector<string>> partition(string s) {\n        // Your code here\n        return {};\n    }\n};")
                .starterCodeJavascript("function partition(s) {\n    // Your code here\n}")
                .build();
        allProblems.add(palindromePartitioning);
        allTestCases.addAll(List.of(
                TestCase.builder().problem(palindromePartitioning).input("aab").expectedOutput("[[\"a\",\"a\",\"b\"],[\"aa\",\"b\"]]").isHidden(false).build(),
                TestCase.builder().problem(palindromePartitioning).input("a").expectedOutput("[[\"a\"]]").isHidden(false).build()
        ));

        // Save all problems and test cases
        problemRepository.saveAll(allProblems);
        testCaseRepository.saveAll(allTestCases);

        // ═══════════════════════════════════════════════════════════════
        // ROADMAPS — now 10 total
        // ═══════════════════════════════════════════════════════════════

        Roadmap binarySearchRoadmap = Roadmap.builder()
                .title("Binary Search")
                .description("Master binary search patterns — sorted arrays, rotated arrays, search spaces, and optimization problems.")
                .icon("🔍")
                .displayOrder(9)
                .build();

        Roadmap backtrackingRoadmap = Roadmap.builder()
                .title("Backtracking")
                .description("Generate subsets, permutations, combinations, and solve constraint satisfaction with recursive exploration.")
                .icon("🔄")
                .displayOrder(10)
                .build();

        binarySearchRoadmap = roadmapRepository.save(binarySearchRoadmap);
        backtrackingRoadmap = roadmapRepository.save(backtrackingRoadmap);

        // Binary Search roadmap items
        binarySearchRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(binarySearchRoadmap).problem(binarySearch).sectionTitle("Basics").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(binarySearchRoadmap).problem(searchMatrix).sectionTitle("2D Search").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(binarySearchRoadmap).problem(kokoEatingBananas).sectionTitle("Search Space").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(binarySearchRoadmap).problem(findMinRotated).sectionTitle("Rotated Array").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(binarySearchRoadmap).problem(searchRotated).sectionTitle("Rotated Array").displayOrder(5).build(),
                RoadmapItem.builder().roadmap(binarySearchRoadmap).problem(medianSortedArrays).sectionTitle("Advanced").displayOrder(6).build(),
                RoadmapItem.builder().roadmap(binarySearchRoadmap).problem(timeBasedKV).sectionTitle("Design").displayOrder(7).build()
        ));

        // Backtracking roadmap items
        backtrackingRoadmap.getItems().addAll(List.of(
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(subsets).sectionTitle("Subsets").displayOrder(1).build(),
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(permutations).sectionTitle("Permutations").displayOrder(2).build(),
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(combinationSum).sectionTitle("Combinations").displayOrder(3).build(),
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(letterCombinations).sectionTitle("Combinations").displayOrder(4).build(),
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(wordSearch).sectionTitle("Grid Search").displayOrder(5).build(),
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(palindromePartitioning).sectionTitle("Partitioning").displayOrder(6).build(),
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(nQueens).sectionTitle("Constraint Satisfaction").displayOrder(7).build(),
                RoadmapItem.builder().roadmap(backtrackingRoadmap).problem(sudokuSolver).sectionTitle("Constraint Satisfaction").displayOrder(8).build()
        ));

        roadmapRepository.saveAll(List.of(
                arraysRoadmap, stringsRoadmap, linkedListRoadmap, stackQueueRoadmap,
                treeRoadmap, graphRoadmap, dpRoadmap, greedyRoadmap,
                binarySearchRoadmap, backtrackingRoadmap
        ));
    }

    private void seedBulkProblems() {
        String[] titles = {
            "Set Matrix Zeroes", "Pascal's Triangle", "Next Permutation", "Kadane's Algorithm", "Sort an array of 0s, 1s and 2s", "Stock Buy and Sell",
            "Rotate Image", "Merge Overlapping Subintervals", "Merge two sorted Arrays without extra space", "Find the duplicate in an array of N+1 integers", "Repeat and Missing Number", "Inversion of Array",
            "Search in a 2D matrix", "Pow(X,n)", "Majority Element (>N/2 times)", "Majority Element (>N/3 times)", "Grid Unique Paths", "Reverse Pairs",
            "Two Sum", "4-sum", "Longest Consecutive Sequence", "Largest Subarray with 0 sum", "Count number of subarrays with given XOR", "Longest Substring Without Repeating Characters",
            "Reverse a LinkedList", "Find the middle of LinkedList", "Merge two sorted Linked List", "Remove N-th node from back of LinkedList", "Add two numbers as LinkedList", "Delete a given Node when a node is given",
            "Find intersection point of Y LinkedList", "Detect a cycle in Linked List", "Reverse a LinkedList in groups of size k", "Check if a LinkedList is palindrome", "Find the starting point of the Loop of LinkedList", "Flattening of a LinkedList",
            "Rotate a LinkedList", "Clone a Linked List with random and next pointer", "3 sum", "Trapping rainwater", "Remove Duplicate from Sorted array", "Max consecutive ones",
            "N meetings in one room", "Minimum number of platforms required", "Job sequencing Problem", "Fractional Knapsack Problem", "Greedy algorithm to find minimum number of coins", "Activity Selection",
            "Subset Sums", "Subset-II", "Combination sum-1", "Combination sum-2", "Palindrome Partitioning", "K-th permutation Sequence",
            "Print all permutations of a string/array", "N queens Problem", "Sudoku Solver", "M coloring Problem", "Rat in a Maze", "Word Break (print all ways)",
            "The N-th root of an integer", "Matrix Median", "Find the element that appears once in a sorted array, and the rest element appears twice", "Search element in a sorted and rotated array/ find pivot where it is rotated", "Median of 2 sorted arrays", "K-th element of two sorted arrays", "Allocate Minimum Number of Pages", "Aggressive Cows",
            "Implement Trie (Prefix Tree)", "Implement Trie II (Prefix Tree)", "Longest String with All Prefixes", "Number of Distinct Substrings in a String", "Power Set (this is very important)", "Maximum XOR of two numbers in an array", "Maximum XOR With an Element From Array",
            "Inorder Traversal", "Preorder Traversal", "Postorder Traversal", "LeftView Of Binary Tree", "Bottom View of Binary Tree", "Top View of Binary Tree", "Preorder inorder postorder in a single traversal", "Vertical order traversal", "Root to node path in a Binary Tree", "Max width of a Binary Tree",
            "Level order Traversal / Level order traversal in spiral form", "Height of a Binary Tree", "Diameter of Binary Tree", "Check if the Binary tree is height-balanced or not", "LCA in Binary Tree", "Check if two trees are identical or not", "Zig Zag Traversal of Binary Tree", "Boundary Traversal of Binary Tree",
            "Maximum path sum", "Construct Binary Tree from inorder and preorder", "Construct Binary Tree from Inorder and Postorder", "Symmetric Binary Tree", "Flatten Binary Tree to LinkedList", "Check if Binary Tree is the mirror of itself or not",
            "Populate Next Right pointers of Tree", "Search given Key in BST", "Construct BST from given keys", "Construct BST from preorder traversal", "Check is a BT is BST or not", "Find LCA of two nodes in BST", "Find the inorder predecessor/successor of a given Key in BST",
            "Floor in a BST", "Ceil in a BST", "Find K-th smallest element in BST", "Find K-th largest element in BST", "Find a pair with a given sum in BST", "BST iterator", "Size of largest BST in a Binary Tree",
            "Clone a graph", "DFS", "BFS", "Detect A cycle in Undirected Graph using BFS", "Detect A cycle in Undirected Graph using DFS", "Detect A cycle in a Directed Graph using DFS", "Detect A cycle in a Directed Graph using BFS", "Topological Sort BFS", "Topological Sort DFS", "Number of islands", "Bipartite Check using BFS", "Bipartite Check using DFS",
            "Strongly Connected Component(using KosaRaju's algo)", "Dijkstra's Algorithm", "Bellman-Ford Algo", "Floyd Warshall Algorithm", "MST using Prim's Algo", "MST using Kruskal's Algo",
            "Max Product Subarray", "Longest Increasing Subsequence", "Longest Common Subsequence", "0-1 Knapsack", "Edit Distance", "Maximum sum increasing subsequence", "Matrix Chain Multiplication",
            "Maximum sum path in the matrix", "Coin change", "Subset Sum", "Rod Cutting", "Egg Dropping", "Word Break", "Palindrome Partitioning (MCM Variation)", "Maximum profit in Job scheduling",
            "Implement Stack using Arrays", "Implement Queue using Arrays", "Implement Stack using Queue (using single queue)", "Implement Queue using Stack (0(1) amortized method)", "Check for balanced parentheses", "Next Greater Element", "Sort a Stack",
            "Next Smaller Element", "LRU cache", "LFU Cache", "Largest rectangle in a histogram", "Sliding Window maximum", "Implement Min Stack", "Rotten Orange", "Stock span problem", "Find the maximum of minimums of every window size", "The Celebrity Problem",
            "Reverse Words in a String", "Longest Palindrome in a string", "Roman Number to Integer and vice versa", "Implement ATOI/STRSTR", "Longest Common Prefix", "Rabin Karp",
            "Prefix function/Z-function", "KMP algo / LPS(pi) array", "Minimum characters needed to be inserted in the beginning to make it palindromic", "Check for Anagrams", "Count and Say", "Compare version numbers",
            
            "Valid Palindrome", "Valid Anagram", "Group Anagrams", "Top K Frequent Elements", "Valid Sudoku", "Encode and Decode Strings", "Longest Consecutive Sequence",
            "Valid Parentheses", "Min Stack", "Evaluate Reverse Polish Notation", "Generate Parentheses", "Daily Temperatures", "Car Fleet", "Largest Rectangle in Histogram",
            "Binary Search", "Search a 2D Matrix", "Koko Eating Bananas", "Find Minimum in Rotated Sorted Array", "Search in Rotated Sorted Array", "Time Based Key-Value Store", "Median of Two Sorted Arrays",
            "Best Time to Buy and Sell Stock", "Longest Repeating Character Replacement", "Permutation in String", "Minimum Window Substring", "Sliding Window Maximum",
            "Two Sum II", "3Sum", "Container With Most Water", "Trapping Rain Water",
            "Reverse Linked List", "Merge Two Sorted Lists", "Reorder List", "Remove Nth Node From End of List", "Copy List with Random Pointer", "Add Two Numbers", "Linked List Cycle", "Find the Duplicate Number", "LRU Cache", "Merge K Sorted Lists", "Reverse Nodes in k-Group",
            "Invert Binary Tree", "Maximum Depth of Binary Tree", "Diameter of Binary Tree", "Balanced Binary Tree", "Same Tree", "Subtree of Another Tree", "Lowest Common Ancestor of a BST", "Binary Tree Level Order Traversal", "Binary Tree Right Side View", "Count Good Nodes in Binary Tree", "Validate Binary Search Tree", "Kth Smallest Element in a BST", "Construct Binary Tree from Preorder and Inorder Traversal", "Binary Tree Maximum Path Sum", "Serialize and Deserialize Binary Tree",
            "Kth Largest Element in a Stream", "Last Stone Weight", "K Closest Points to Origin", "Kth Largest Element in an Array", "Task Scheduler", "Design Twitter", "Find Median from Data Stream",
            "Number of Islands", "Max Area of Island", "Clone Graph", "Walls and Gates", "Rotting Oranges", "Pacific Atlantic Water Flow", "Surrounded Regions", "Course Schedule", "Course Schedule II", "Graph Valid Tree", "Number of Connected Components in an Undirected Graph", "Redundant Connection",
            "Combination Sum", "Permutations", "Subsets II", "Word Search", "Letter Combinations of a Phone Number", "N-Queens",
            "Climbing Stairs", "Min Cost Climbing Stairs", "House Robber", "House Robber II", "Palindromic Substrings", "Decode Ways", "Coin Change", "Maximum Product Subarray", "Partition Equal Subset Sum",
            "Unique Paths", "Best Time to Buy and Sell Stock with Cooldown", "Coin Change II", "Target Sum", "Interleaving String", "Longest Increasing Path in a Matrix", "Distinct Subsequences", "Burst Balloons", "Regular Expression Matching",
            "Maximum Subarray", "Jump Game", "Jump Game II", "Gas Station", "Hand of Straights", "Merge Triplets to Form Target Triplet", "Partition Labels", "Valid Parenthesis String",
            "Insert Interval", "Merge Intervals", "Non-overlapping Intervals", "Meeting Rooms", "Meeting Rooms II", "Minimum Interval to Include Each Query",
            "Single Number", "Number of 1 Bits", "Counting Bits", "Reverse Bits", "Missing Number", "Sum of Two Integers", "Reverse Integer"
        };

        for (String title : titles) {
            if (!problemRepository.existsByTitle(title)) {
                Problem p = Problem.builder()
                        .title(title)
                        .description("This is a dynamically seeded problem from the famous curated DSA sheets. Solve this problem to master its underlying pattern!\n\n**Problem Statement**:\nSolve the '" + title + "' problem.")
                        .constraints("1 <= N <= 10^5\nTime Complexity must be optimal.")
                        .examples("Input: Standard generic input\nOutput: Generic answer")
                        .difficulty(Difficulty.MEDIUM)
                        .topic("Miscellaneous Patterns")
                        .executionKey((title.toUpperCase().replaceAll("[^A-Z0-9]+", "_")).substring(0, Math.min(50, title.toUpperCase().replaceAll("[^A-Z0-9]+", "_").length())))
                        .starterCodeJava("class Solution {\n    public void solve() {\n        // Your code here\n    }\n}")
                        .starterCodePython("def solve():\n    # Your code here\n    pass")
                        .starterCodeCpp("class Solution {\npublic:\n    void solve() {\n        // Your code here\n    }\n};")
                        .starterCodeJavascript("function solve() {\n    // Your code here\n}")
                        .build();
                problemRepository.save(p);
            }
        }
    }

    private void backfillExecutionKeys() {
        Map<String, String> keysByTitle = Map.ofEntries(
                Map.entry("Two Sum", "TWO_SUM"),
                Map.entry("Reverse String", "REVERSE_STRING"),
                Map.entry("Maximum Subarray", "MAX_SUBARRAY"),
                Map.entry("Contains Duplicate", "CONTAINS_DUPLICATE"),
                Map.entry("Valid Anagram", "VALID_ANAGRAM"),
                Map.entry("Group Anagrams", "GROUP_ANAGRAMS"),
                Map.entry("Top K Frequent Elements", "TOP_K_FREQUENT"),
                Map.entry("Product of Array Except Self", "PRODUCT_EXCEPT_SELF"),
                Map.entry("Longest Consecutive Sequence", "LONGEST_CONSECUTIVE"),
                Map.entry("Valid Palindrome", "VALID_PALINDROME"),
                Map.entry("Longest Palindromic Substring", "LONGEST_PALINDROME"),
                Map.entry("Valid Parentheses", "VALID_PARENTHESES"),
                Map.entry("Climbing Stairs", "CLIMB_STAIRS"),
                Map.entry("Coin Change", "COIN_CHANGE"),
                Map.entry("Number of Islands", "NUM_ISLANDS"),
                Map.entry("Best Time to Buy and Sell Stock", "BEST_TIME_BUY"),
                Map.entry("Merge Intervals", "MERGE_INTERVALS"),
                Map.entry("House Robber", "HOUSE_ROBBER"),
                Map.entry("Jump Game", "JUMP_GAME")
        );
        problemRepository.findAll().forEach(problem -> {
            if (problem.getExecutionKey() == null && keysByTitle.containsKey(problem.getTitle())) {
                problem.setExecutionKey(keysByTitle.get(problem.getTitle()));
                problemRepository.save(problem);
            }
        });
    }
}
