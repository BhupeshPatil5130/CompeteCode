import re
import os

titles = [
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

    # Apna College extra
    "Valid Palindrome", "Valid Anagram", "Group Anagrams", "Top K Frequent Elements", "Valid Sudoku", "Encode and Decode Strings", "Longest Consecutive Sequence",
    "Valid Parentheses", "Min Stack", "Evaluate Reverse Polish Notation", "Generate Parentheses", "Daily Temperatures", "Car Fleet", "Largest Rectangle in Histogram",
    "Binary Search", "Search a 2D Matrix", "Koko Eating Bananas", "Find Minimum in Rotated Sorted Array", "Search in Rotated Sorted Array", "Time Based Key-Value Store", "Median of Two Sorted Arrays",
    "Best Time to Buy and Sell Stock", "Longest Repeating Character Replacement", "Permutation in String", "Minimum Window Substring", "Sliding Window Maximum",
    "Valid Palindrome", "Two Sum II", "3Sum", "Container With Most Water", "Trapping Rain Water",
    "Reverse Linked List", "Merge Two Sorted Lists", "Reorder List", "Remove Nth Node From End of List", "Copy List with Random Pointer", "Add Two Numbers", "Linked List Cycle", "Find the Duplicate Number", "LRU Cache", "Merge K Sorted Lists", "Reverse Nodes in k-Group",
    "Invert Binary Tree", "Maximum Depth of Binary Tree", "Diameter of Binary Tree", "Balanced Binary Tree", "Same Tree", "Subtree of Another Tree", "Lowest Common Ancestor of a BST", "Binary Tree Level Order Traversal", "Binary Tree Right Side View", "Count Good Nodes in Binary Tree", "Validate Binary Search Tree", "Kth Smallest Element in a BST", "Construct Binary Tree from Preorder and Inorder Traversal", "Binary Tree Maximum Path Sum", "Serialize and Deserialize Binary Tree",
    "Kth Largest Element in a Stream", "Last Stone Weight", "K Closest Points to Origin", "Kth Largest Element in an Array", "Task Scheduler", "Design Twitter", "Find Median from Data Stream",
    "Number of Islands", "Max Area of Island", "Clone Graph", "Walls and Gates", "Rotting Oranges", "Pacific Atlantic Water Flow", "Surrounded Regions", "Course Schedule", "Course Schedule II", "Graph Valid Tree", "Number of Connected Components in an Undirected Graph", "Redundant Connection",
    "Combination Sum", "Permutations", "Subsets II", "Word Search", "Palindrome Partitioning", "Letter Combinations of a Phone Number", "N-Queens",
    "Climbing Stairs", "Min Cost Climbing Stairs", "House Robber", "House Robber II", "Longest Palindromic Substring", "Palindromic Substrings", "Decode Ways", "Coin Change", "Maximum Product Subarray", "Word Break", "Longest Increasing Subsequence", "Partition Equal Subset Sum",
    "Unique Paths", "Longest Common Subsequence", "Best Time to Buy and Sell Stock with Cooldown", "Coin Change II", "Target Sum", "Interleaving String", "Longest Increasing Path in a Matrix", "Distinct Subsequences", "Edit Distance", "Burst Balloons", "Regular Expression Matching",
    "Maximum Subarray", "Jump Game", "Jump Game II", "Gas Station", "Hand of Straights", "Merge Triplets to Form Target Triplet", "Partition Labels", "Valid Parenthesis String",
    "Insert Interval", "Merge Intervals", "Non-overlapping Intervals", "Meeting Rooms", "Meeting Rooms II", "Minimum Interval to Include Each Query",
    "Single Number", "Number of 1 Bits", "Counting Bits", "Reverse Bits", "Missing Number", "Sum of Two Integers", "Reverse Integer"
]

unique_titles = list(set(titles))

java_code = f"""
    private void seedBulkProblems() {{
        String[] titles = {{
            {", ".join([f'"{title}"' for title in unique_titles])}
        }};

        for (String title : titles) {{
            if (!problemRepository.existsByTitle(title)) {{
                Problem p = Problem.builder()
                        .title(title)
                        .description("This is a dynamically seeded problem from the famous curated DSA sheets. Solve this problem to master its underlying pattern!\\n\\n**Problem Statement**:\\nSolve the '" + title + "' problem.")
                        .constraints("1 <= N <= 10^5\\nTime Complexity must be optimal.")
                        .examples("Input: Standard generic input\\nOutput: Generic answer")
                        .difficulty(Difficulty.MEDIUM)
                        .topic("Miscellaneous Patterns")
                        .executionKey(title.toUpperCase().replaceAll("[^A-Z0-9]+", "_"))
                        .starterCodeJava("class Solution {\\n    public void solve() {\\n        // Your code here\\n    }\\n}")
                        .starterCodePython("def solve():\\n    # Your code here\\n    pass")
                        .starterCodeCpp("class Solution {\\npublic:\\n    void solve() {\\n        // Your code here\\n    }\\n};")
                        .starterCodeJavascript("function solve() {\\n    // Your code here\\n}")
                        .build();
                problemRepository.save(p);
            }}
        }}
    }}
"""

with open("seedBulk.txt", "w") as f:
    f.write(java_code)
