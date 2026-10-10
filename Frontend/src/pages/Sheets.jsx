import { useEffect, useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { LayoutGrid, ChevronRight, BookOpen, Star, FileText } from 'lucide-react';
import { problemApi } from '../api/client';
import DifficultyBadge from '../components/DifficultyBadge';
import LoadingSpinner from '../components/LoadingSpinner';

const SHEET_META = {
  'Arrays & Hashing': { icon: '📊', color: 'border-blue-500/20 bg-blue-500/5' },
  'Strings': { icon: '🔤', color: 'border-violet-500/20 bg-violet-500/5' },
  'Linked Lists': { icon: '🔗', color: 'border-cyan-500/20 bg-cyan-500/5' },
  'Stacks & Queues': { icon: '📚', color: 'border-indigo-500/20 bg-indigo-500/5' },
  'Trees & BST': { icon: '🌲', color: 'border-emerald-500/20 bg-emerald-500/5' },
  'Graphs': { icon: '🕸️', color: 'border-orange-500/20 bg-orange-500/5' },
  'Dynamic Programming': { icon: '🧠', color: 'border-purple-500/20 bg-purple-500/5' },
  'Greedy & Sorting': { icon: '⚡', color: 'border-amber-500/20 bg-amber-500/5' },
  'Binary Search': { icon: '🔍', color: 'border-teal-500/20 bg-teal-500/5' },
  'Backtracking': { icon: '🔄', color: 'border-rose-500/20 bg-rose-500/5' },
};
const CREATOR_SHEETS = {
  "Striver's SDE Sheet": {
    'Day 1: Arrays': ['Set Matrix Zeroes', "Pascal's Triangle", 'Next Permutation', "Kadane's Algorithm", 'Sort an array of 0s, 1s and 2s', 'Stock Buy and Sell'],
    'Day 2: Arrays Part-II': ['Rotate Image', 'Merge Overlapping Subintervals', 'Merge two sorted Arrays without extra space', 'Find the duplicate in an array of N+1 integers', 'Repeat and Missing Number', 'Inversion of Array'],
    'Day 3: Arrays Part-III': ['Search in a 2D matrix', 'Pow(X,n)', 'Majority Element (>N/2 times)', 'Majority Element (>N/3 times)', 'Grid Unique Paths', 'Reverse Pairs'],
    'Day 4: Arrays Part-IV': ['Two Sum', '4-sum', 'Longest Consecutive Sequence', 'Largest Subarray with 0 sum', 'Count number of subarrays with given XOR', 'Longest Substring Without Repeating Characters'],
    'Day 5: Linked List': ['Reverse a LinkedList', 'Find the middle of LinkedList', 'Merge two sorted Linked List', 'Remove N-th node from back of LinkedList', 'Add two numbers as LinkedList', 'Delete a given Node when a node is given'],
    'Day 6: Linked List Part-II': ['Find intersection point of Y LinkedList', 'Detect a cycle in Linked List', 'Reverse a LinkedList in groups of size k', 'Check if a LinkedList is palindrome', 'Find the starting point of the Loop of LinkedList', 'Flattening of a LinkedList'],
    'Day 7: Linked List and Arrays': ['Rotate a LinkedList', 'Clone a Linked List with random and next pointer', '3 sum', 'Trapping rainwater', 'Remove Duplicate from Sorted array', 'Max consecutive ones'],
    'Day 8: Greedy Algorithm': ['N meetings in one room', 'Minimum number of platforms required', 'Job sequencing Problem', 'Fractional Knapsack Problem', 'Greedy algorithm to find minimum number of coins', 'Activity Selection'],
    'Day 9: Recursion': ['Subset Sums', 'Subset-II', 'Combination sum-1', 'Combination sum-2', 'Palindrome Partitioning', 'K-th permutation Sequence'],
    'Day 10: Recursion and Backtracking': ['Print all permutations of a string/array', 'N queens Problem', 'Sudoku Solver', 'M coloring Problem', 'Rat in a Maze', 'Word Break (print all ways)'],
    'Day 11: Binary Search': ['The N-th root of an integer', 'Matrix Median', 'Find the element that appears once in a sorted array, and the rest element appears twice', 'Search element in a sorted and rotated array/ find pivot where it is rotated', 'Median of 2 sorted arrays', 'K-th element of two sorted arrays', 'Allocate Minimum Number of Pages', 'Aggressive Cows'],
    'Day 12: Heaps': ['Kth Largest Element in an Array', 'K Closest Points to Origin', 'Top K Frequent Elements', 'Merge K Sorted Lists'],
    'Day 13: Stack and Queue': ['Implement Stack using Arrays', 'Implement Queue using Arrays', 'Implement Stack using Queue (using single queue)', 'Implement Queue using Stack (0(1) amortized method)', 'Check for balanced parentheses', 'Next Greater Element', 'Sort a Stack'],
    'Day 14: Stack and Queue Part-II': ['Next Smaller Element', 'LRU cache', 'LFU Cache', 'Largest rectangle in a histogram', 'Sliding Window maximum', 'Implement Min Stack', 'Rotten Orange', 'Stock span problem', 'Find the maximum of minimums of every window size', 'The Celebrity Problem'],
    'Day 15: String': ['Reverse Words in a String', 'Longest Palindrome in a string', 'Roman Number to Integer and vice versa', 'Implement ATOI/STRSTR', 'Longest Common Prefix', 'Rabin Karp'],
    'Day 16: String Part-II': ['Prefix function/Z-function', 'KMP algo / LPS(pi) array', 'Minimum characters needed to be inserted in the beginning to make it palindromic', 'Check for Anagrams', 'Count and Say', 'Compare version numbers'],
    'Day 17: Binary Tree': ['Inorder Traversal', 'Preorder Traversal', 'Postorder Traversal', 'LeftView Of Binary Tree', 'Bottom View of Binary Tree', 'Top View of Binary Tree', 'Preorder inorder postorder in a single traversal', 'Vertical order traversal', 'Root to node path in a Binary Tree', 'Max width of a Binary Tree'],
    'Day 18: Binary Tree part-II': ['Level order Traversal / Level order traversal in spiral form', 'Height of a Binary Tree', 'Diameter of Binary Tree', 'Check if the Binary tree is height-balanced or not', 'LCA in Binary Tree', 'Check if two trees are identical or not', 'Zig Zag Traversal of Binary Tree', 'Boundary Traversal of Binary Tree'],
    'Day 19: Binary Tree part-III': ['Maximum path sum', 'Construct Binary Tree from inorder and preorder', 'Construct Binary Tree from Inorder and Postorder', 'Symmetric Binary Tree', 'Flatten Binary Tree to LinkedList', 'Check if Binary Tree is the mirror of itself or not'],
    'Day 20: Binary Search Tree': ['Populate Next Right pointers of Tree', 'Search given Key in BST', 'Construct BST from given keys", "Construct BST from preorder traversal", "Check is a BT is BST or not", "Find LCA of two nodes in BST", "Find the inorder predecessor/successor of a given Key in BST'],
    'Day 21: Binary Search Tree Part-II': ['Floor in a BST', 'Ceil in a BST', 'Find K-th smallest element in BST', 'Find K-th largest element in BST', 'Find a pair with a given sum in BST', 'BST iterator', 'Size of largest BST in a Binary Tree'],
    'Day 22: Mixed Questions': ['Binary Tree to Double Linked List', 'Find median in a stream of running integers', 'K-th largest element in a stream', 'Distinct numbers in Window', 'K-th largest element in an unsorted array', 'Flood-fill Algorithm'],
    'Day 23: Graph': ['Clone a graph', 'DFS', 'BFS', 'Detect A cycle in Undirected Graph using BFS', 'Detect A cycle in Undirected Graph using DFS', 'Detect A cycle in a Directed Graph using DFS', 'Detect A cycle in a Directed Graph using BFS', 'Topological Sort BFS', 'Topological Sort DFS', 'Number of islands', 'Bipartite Check using BFS', 'Bipartite Check using DFS'],
    'Day 24: Graph Part-II': ["Strongly Connected Component(using KosaRaju's algo)", "Dijkstra's Algorithm", 'Bellman-Ford Algo', 'Floyd Warshall Algorithm', "MST using Prim's Algo", "MST using Kruskal's Algo"],
    'Day 25: Dynamic Programming': ['Max Product Subarray', 'Longest Increasing Subsequence', 'Longest Common Subsequence', '0-1 Knapsack', 'Edit Distance', 'Maximum sum increasing subsequence', 'Matrix Chain Multiplication'],
    'Day 26: Dynamic Programming Part-II': ['Maximum sum path in the matrix', 'Coin change', 'Subset Sum', 'Rod Cutting', 'Egg Dropping', 'Word Break', 'Palindrome Partitioning (MCM Variation)', 'Maximum profit in Job scheduling'],
  },
  "Apna College DSA": {
    'Array Fundamentals': ['Reverse String', 'Contains Duplicate', 'Longest Consecutive Sequence', 'Two Sum', 'Product of Array Except Self', 'Maximum Subarray', 'Search in Rotated Sorted Array', 'Find Minimum in Rotated Sorted Array'],
    'String Manipulation': ['Valid Anagram', 'Valid Palindrome', 'Longest Palindromic Substring', 'String to Integer (atoi)', 'Longest Repeating Character Replacement', 'Permutation in String'],
    'Stack Patterns': ['Valid Parentheses', 'Min Stack', 'Daily Temperatures', 'Evaluate Reverse Polish Notation', 'Generate Parentheses', 'Largest Rectangle in Histogram'],
    'Tree Traversals': ['Invert Binary Tree', 'Maximum Depth of Binary Tree', 'Same Tree', 'Subtree of Another Tree', 'Lowest Common Ancestor of a BST', 'Binary Tree Level Order Traversal', 'Validate Binary Search Tree'],
    'Graph Foundations': ['Number of Islands', 'Max Area of Island', 'Clone Graph', 'Walls and Gates', 'Rotting Oranges', 'Course Schedule'],
    'Dynamic Programming': ['Climbing Stairs', 'Min Cost Climbing Stairs', 'House Robber', 'House Robber II', 'Coin Change', 'Maximum Product Subarray'],
  },
  "NeetCode 150": {
    'Arrays & Hashing': ['Contains Duplicate', 'Valid Anagram', 'Two Sum', 'Group Anagrams', 'Top K Frequent Elements', 'Product of Array Except Self', 'Valid Sudoku', 'Encode and Decode Strings', 'Longest Consecutive Sequence'],
    'Two Pointers': ['Valid Palindrome', 'Two Sum II', '3Sum', 'Container With Most Water', 'Trapping Rain Water'],
    'Sliding Window': ['Best Time to Buy and Sell Stock', 'Longest Substring Without Repeating Characters', 'Longest Repeating Character Replacement', 'Permutation in String', 'Minimum Window Substring', 'Sliding Window Maximum'],
    'Stack': ['Valid Parentheses', 'Min Stack', 'Evaluate Reverse Polish Notation', 'Generate Parentheses', 'Daily Temperatures', 'Car Fleet', 'Largest Rectangle in Histogram'],
    'Binary Search': ['Binary Search', 'Search a 2D Matrix', 'Koko Eating Bananas', 'Find Minimum in Rotated Sorted Array', 'Search in Rotated Sorted Array', 'Time Based Key-Value Store', 'Median of Two Sorted Arrays'],
    'Linked List': ['Reverse Linked List', 'Merge Two Sorted Lists', 'Reorder List', 'Remove Nth Node From End of List', 'Copy List with Random Pointer', 'Add Two Numbers', 'Linked List Cycle', 'Find the Duplicate Number', 'LRU Cache', 'Merge K Sorted Lists', 'Reverse Nodes in k-Group'],
    'Trees': ['Invert Binary Tree', 'Maximum Depth of Binary Tree', 'Diameter of Binary Tree', 'Balanced Binary Tree', 'Same Tree', 'Subtree of Another Tree', 'Lowest Common Ancestor of a BST', 'Binary Tree Level Order Traversal', 'Binary Tree Right Side View', 'Count Good Nodes in Binary Tree', 'Validate Binary Search Tree', 'Kth Smallest Element in a BST', 'Construct Binary Tree from Preorder and Inorder Traversal', 'Binary Tree Maximum Path Sum', 'Serialize and Deserialize Binary Tree'],
  }
};
export default function Sheets() {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);
  const [activeTab, setActiveTab] = useState('Topics');

  useEffect(() => {
    problemApi.getAll()
      .then(({ data }) => setProblems(data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  // Compute the sheets based on active tab
  const sheets = useMemo(() => {
    if (activeTab === 'Topics') {
      const map = {};
      problems.forEach((p) => {
        const topic = p.topic || 'Other';
        if (!map[topic]) map[topic] = [];
        map[topic].push(p);
      });
      return Object.keys(SHEET_META)
        .filter((k) => map[k])
        .map((topic) => ({
          topic,
          problems: map[topic],
          icon: SHEET_META[topic].icon,
        }));
    } else {
      // Creator sheets logic
      const layout = CREATOR_SHEETS[activeTab];
      if (!layout) return [];
      
      const result = [];
      Object.keys(layout).forEach((groupName) => {
        const targetTitles = layout[groupName];
        // Find problems matching the titles
        const groupProblems = targetTitles
          .map(title => problems.find(p => p.title === title))
          .filter(p => p !== undefined);
        
        if (groupProblems.length > 0) {
          result.push({
            topic: groupName,
            problems: groupProblems,
            icon: '📌'
          });
        }
      });
      return result;
    }
  }, [problems, activeTab]);

  if (loading) return <LoadingSpinner text="Loading sheets..." />;

  const tabs = [
    { id: 'Topics', label: 'Topic-wise', icon: LayoutGrid },
    { id: "Striver's SDE Sheet", label: "Striver's SDE", icon: Star },
    { id: "Apna College DSA", label: "Apna College", icon: BookOpen },
    { id: "NeetCode 150", label: "NeetCode 150", icon: FileText },
  ];

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2 mb-1">
          <LayoutGrid className="w-6 h-6 text-amber-400" /> DSA Curriculum Sheets
        </h1>
        <p className="text-sm text-neutral-500">Master Data Structures and Algorithms with curated sheets.</p>
      </div>

      {/* Tabs */}
      <div className="flex overflow-x-auto gap-2 pb-2 mb-6 scrollbar-none">
        {tabs.map((tab) => {
          const isActive = activeTab === tab.id;
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => { setActiveTab(tab.id); setExpanded(null); }}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium transition-all whitespace-nowrap border ${
                isActive 
                  ? 'bg-amber-500/10 border-amber-500/30 text-amber-400 shadow-[0_0_15px_rgba(245,158,11,0.1)]' 
                  : 'bg-surface-secondary border-border text-neutral-400 hover:text-white hover:border-neutral-600'
              }`}
            >
              <Icon className="w-4 h-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      <div className="space-y-3">
        <AnimatePresence mode="popLayout">
          {sheets.map((sheet, i) => {
            const isExpanded = expanded === sheet.topic;
            const easyCount = sheet.problems.filter(p => p.difficulty === 'EASY').length;
            const medCount = sheet.problems.filter(p => p.difficulty === 'MEDIUM').length;
            const hardCount = sheet.problems.filter(p => p.difficulty === 'HARD').length;

            return (
              <motion.div
                key={sheet.topic + activeTab}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.98 }}
                transition={{ delay: i * 0.04 }}
              >
                {/* Sheet header */}
                <button
                  onClick={() => setExpanded(isExpanded ? null : sheet.topic)}
                  className={`w-full flex items-center gap-4 p-4 rounded-xl border transition-all text-left ${
                    isExpanded
                      ? 'bg-surface-tertiary border-neutral-700'
                      : 'bg-surface-secondary border-border hover:border-neutral-600'
                  }`}
                >
                  <div className="text-2xl">{sheet.icon}</div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-sm font-semibold text-white">{sheet.topic}</h3>
                    <div className="flex items-center gap-3 mt-0.5">
                      <span className="text-xs text-neutral-500">{sheet.problems.length} problems</span>
                      <span className="text-[10px] text-emerald-500">{easyCount}E</span>
                      <span className="text-[10px] text-amber-500">{medCount}M</span>
                      <span className="text-[10px] text-orange-500">{hardCount}H</span>
                    </div>
                  </div>
                  <ChevronRight className={`w-4 h-4 text-neutral-600 transition-transform ${isExpanded ? 'rotate-90' : ''}`} />
                </button>

                {/* Expanded problems */}
                {isExpanded && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    className="mt-1 ml-4 border-l-2 border-border pl-4 space-y-1"
                  >
                    {sheet.problems.map((problem, j) => (
                      <motion.div
                        key={problem.id}
                        initial={{ opacity: 0, x: -8 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: j * 0.03 }}
                      >
                        <Link
                          to={`/problems/${problem.id}`}
                          className="flex items-center justify-between p-3 rounded-lg hover:bg-surface-hover transition-colors group"
                        >
                          <span className="text-sm text-neutral-300 group-hover:text-amber-400 transition-colors">
                            {problem.title}
                          </span>
                          <DifficultyBadge difficulty={problem.difficulty} />
                        </Link>
                      </motion.div>
                    ))}
                  </motion.div>
                )}
              </motion.div>
            );
          })}
        </AnimatePresence>
      </div>
    </div>
  );
}
