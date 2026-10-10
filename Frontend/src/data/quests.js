export const QUESTS = [
  {
    id: "q1",
    title: "The Core Reboot",
    difficulty: "Beginner",
    description: "Learn basic programming logic including variables, conditionals, and loops to restart the spaceship's core.",
    chapters: [
      {
        id: 1,
        title: "Chapter 1: The Awakening",
        narrative: [
          "Bzzt... System online.",
          "Hello, traveler. My name is C.O.D.E.-Bot.",
          "Our spaceship engine is completely out of power, and our main thrusters are offline.",
          "To reboot the core, we need to send a simple initiation signal to the console.",
          "Write a print statement that outputs exactly: \"Reboot Core\""
        ],
        successNarrative: [
          "Excellent! The initiation signal was received perfectly.",
          "The core is now humming back to life."
        ],
        task: "Print \"Reboot Core\" to the console.",
        hint: "Use System.out.println() in Java, printf() in C, cout in C++, console.log() in JS, or print() in Python.",
        validations: [
          { 
            regex: /(System\.out\.print(ln)?\s*\(\s*"Reboot Core"\s*\)|printf\s*\(\s*"Reboot Core(\\n)?"\s*\)|cout\s*<<\s*"Reboot Core"|console\.log\s*\(\s*"Reboot Core"\s*\)|print\s*\(\s*"Reboot Core"\s*\))/, 
            message: "Print statement must output exactly 'Reboot Core'." 
          }
        ],
        testCases: [
          { name: "Case 1", input: "No Input Required", expectedOutput: "Reboot Core" }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void main(String[] args) {\n    // Write your code here\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  // Write your code here\n  \n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nint main() {\n  // Write your code here\n  \n  return 0;\n}",
          javascript: "// Write your code here\n\n",
          python: "# Write your code here\n\n"
        }
      },
      {
        id: 2,
        title: "Chapter 2: Fuel Pumping",
        narrative: [
          "The core is rebooting, but we need fuel.",
          "Now we must manually pump fuel into the 5 reserve tanks.",
          "We cannot do this manually for every tank. We need a loop!",
          "Write a loop that iterates exactly 5 times."
        ],
        successNarrative: [
          "Fuel pumps are active!",
          "Great job using a loop to automate repetitive tasks."
        ],
        task: "Write a for/while loop that runs 5 times.",
        hint: "for(int i=0; i<5; i++) OR for i in range(5):",
        validations: [
          { 
            regex: /(for\s*\(\s*(int|let|var)?\s*[a-zA-Z_]\w*\s*=\s*0\s*;\s*[a-zA-Z_]\w*\s*<\s*5\s*;\s*[a-zA-Z_]\w*\+\+\s*\)|for\s+[a-zA-Z_]\w*\s+in\s+range\(\s*5\s*\)|while)/, 
            message: "You must write a loop that iterates 5 times." 
          }
        ],
        testCases: [
          { name: "Case 1", input: "No Input Required", expectedOutput: "(Structural Check) A loop iterating exactly 5 times must be present in the code." }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void main(String[] args) {\n    // Pump the 5 tanks using a loop\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  // Pump the 5 tanks using a loop\n  \n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nint main() {\n  // Pump the 5 tanks using a loop\n  \n  return 0;\n}",
          javascript: "// Pump the 5 tanks using a loop\n\n",
          python: "# Pump the 5 tanks using a loop\n\n"
        }
      },
      {
        id: 3,
        title: "Chapter 3: System Diagnostics",
        narrative: [
          "Fuel is pumping, but we must check the engine temperature.",
          "If the temperature exceeds 100 degrees, we must print 'WARNING'. Otherwise, print 'SAFE'.",
          "Assume a variable `temp = 120` is given. Write the if/else logic."
        ],
        successNarrative: [
          "Diagnostics complete.",
          "You have successfully mastered conditional logic!"
        ],
        task: "Write an if/else statement checking if temp > 100.",
        hint: "if (temp > 100) { ... } else { ... }",
        validations: [
          { regex: /if\s*\(?\s*temp\s*>\s*100\s*\)?\s*:?/, message: "Must check if temp is greater than 100." },
          { regex: /else\s*:?/, message: "Must include an else block." }
        ],
        testCases: [
          { name: "Case 1", input: "temp = 120", expectedOutput: "WARNING" }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void main(String[] args) {\n    int temp = 120;\n    // Write if/else logic here\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  int temp = 120;\n  // Write if/else logic here\n  \n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nint main() {\n  int temp = 120;\n  // Write if/else logic here\n  \n  return 0;\n}",
          javascript: "let temp = 120;\n// Write if/else logic here\n\n",
          python: "temp = 120\n# Write if/else logic here\n\n"
        }
      }
    ]
  },
  {
    id: "q2",
    title: "Memory Bank Restoration",
    difficulty: "Intermediate",
    description: "Learn how to store and access multiple data points using Arrays/Lists.",
    chapters: [
      {
        id: 1,
        title: "Chapter 1: Array Initialization",
        narrative: [
          "Warning! My memory banks are fragmented.",
          "I need you to allocate a continuous block of memory to store 5 sector addresses.",
          "Initialize an array (or list) named 'sectors' of size 5."
        ],
        successNarrative: [
          "Memory block allocated successfully.",
          "I can feel the continuous sectors aligning."
        ],
        task: "Declare an array/list named 'sectors' of size 5.",
        hint: "int[] sectors = new int[5]; OR sectors = [0]*5",
        validations: [
          { regex: /(sectors\s*=\s*(new\s+int\[5\]|\[.*?\]|\[0\]\s*\*\s*5)|int\s+sectors\[5\])/, message: "Declare an array/list named sectors." }
        ],
        testCases: [
          { name: "Case 1", input: "No Input", expectedOutput: "Array of size 5 initialized" }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void main(String[] args) {\n    // Initialize 'sectors' array\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  // Initialize 'sectors' array\n  \n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nint main() {\n  // Initialize 'sectors' array\n  \n  return 0;\n}",
          javascript: "// Initialize 'sectors' array\n\n",
          python: "# Initialize 'sectors' list\n\n"
        }
      },
      {
        id: 2,
        title: "Chapter 2: Sequential Access",
        narrative: [
          "Now that we have the 'sectors' array, we must populate it.",
          "Write a loop to populate the array with values 1, 2, 3, 4, 5 in order."
        ],
        successNarrative: [
          "Memory sectors populated.",
          "My data retrieval speeds have increased by 400%!"
        ],
        task: "Use a loop to set sectors[i] = i + 1.",
        hint: "Inside the loop: sectors[i] = i + 1;",
        validations: [
          { regex: /sectors\[[a-zA-Z_]\w*\]\s*=\s*[a-zA-Z_]\w*\s*\+\s*1/, message: "Must set sectors[i] = i + 1" }
        ],
        testCases: [
          { name: "Case 1", input: "Array sectors[5]", expectedOutput: "[1, 2, 3, 4, 5]" }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void main(String[] args) {\n    int[] sectors = new int[5];\n    // Loop and populate\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  int sectors[5];\n  // Loop and populate\n  \n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nint main() {\n  int sectors[5];\n  // Loop and populate\n  \n  return 0;\n}",
          javascript: "let sectors = new Array(5);\n// Loop and populate\n\n",
          python: "sectors = [0] * 5\n# Loop and populate\n\n"
        }
      }
    ]
  },
  {
    id: "q3",
    title: "Data Decryption Patterns",
    difficulty: "Advanced",
    description: "Master classic array manipulation patterns like Two-Pointer and Reversal.",
    chapters: [
      {
        id: 1,
        title: "Chapter 1: Reversing the Signal",
        narrative: [
          "Incoming transmission intercepted! But the signal is backwards.",
          "You must reverse the elements in the provided array.",
          "Use a loop to swap the elements from the ends to the middle!"
        ],
        successNarrative: [
          "Signal reversed successfully.",
          "The transmission reads: 'Reinforcements arriving in T-minus 10 minutes.'"
        ],
        task: "Reverse the elements in the given array.",
        hint: "Use a two pointer approach: swap array[left] with array[right].",
        validations: [
          { regex: /\[.*left.*\]\s*=\s*.*\[.*right.*\]/, message: "Must use left/right indices to swap." }
        ],
        testCases: [
          { name: "Case 1", input: "arr = [5, 4, 3, 2, 1]", expectedOutput: "[1, 2, 3, 4, 5]" }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void main(String[] args) {\n    int[] arr = {5, 4, 3, 2, 1};\n    int left = 0, right = arr.length - 1;\n    // Reverse the array\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  int arr[] = {5, 4, 3, 2, 1};\n  int left = 0, right = 4;\n  // Reverse the array\n  \n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nint main() {\n  int arr[] = {5, 4, 3, 2, 1};\n  int left = 0, right = 4;\n  // Reverse the array\n  \n  return 0;\n}",
          javascript: "let arr = [5, 4, 3, 2, 1];\nlet left = 0, right = arr.length - 1;\n// Reverse the array\n\n",
          python: "arr = [5, 4, 3, 2, 1]\nleft, right = 0, len(arr) - 1\n# Reverse the list\n\n"
        }
      }
    ]
  },
  {
    id: "q4",
    title: "The Linked Hierarchy",
    difficulty: "Intermediate",
    description: "Learn to build and traverse Linked Lists to establish secure communication nodes.",
    chapters: [
      {
        id: 1,
        title: "Chapter 1: Node Creation",
        narrative: [
          "Our communication nodes are disconnected.",
          "We must define a structure to link them together.",
          "Create a class/struct named 'Node' that contains an integer 'data' and a pointer/reference to the 'next' Node."
        ],
        successNarrative: [
          "Node structure defined.",
          "We now have the blueprint for our communication network."
        ],
        task: "Define a Node class/struct with 'data' and 'next' properties.",
        hint: "class Node { int data; Node next; }",
        validations: [
          { regex: /(class|struct)\s+Node/, message: "Must define a Node class or struct." },
          { regex: /(Node\s*\**\s*next|next\s*:\s*Node)/, message: "Node must have a 'next' property referencing another Node." }
        ],
        testCases: [
          { name: "Case 1", input: "No Input", expectedOutput: "Valid Node structure defined" }
        ],
        defaultCode: {
          java: "public class Main {\n  // Define Node class here\n  \n  public static void main(String[] args) {\n  }\n}",
          c: "#include <stdio.h>\n\n// Define Node struct here\n\nint main() {\n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\n// Define Node struct/class here\n\nint main() {\n  return 0;\n}",
          javascript: "// Define Node class here\n\n",
          python: "# Define Node class here\n\n"
        }
      },
      {
        id: 2,
        title: "Chapter 2: List Traversal",
        narrative: [
          "We have established a chain of nodes. Now we must trace the signal from the start to the end.",
          "Write a while loop that traverses the linked list until the current node is null.",
          "Assume 'curr' points to the head of the list."
        ],
        successNarrative: [
          "Signal traced successfully to the end of the line.",
          "Communication network is fully operational!"
        ],
        task: "Write a while loop to traverse 'curr' to the end.",
        hint: "while (curr != null) { curr = curr.next; }",
        validations: [
          { regex: /while\s*\(\s*curr\s*(!=|!==|is not)\s*(null|NULL|nullptr|None)\s*\)/, message: "Loop condition must check if curr is not null." },
          { regex: /curr\s*=\s*curr(\.|->)next/, message: "Must advance curr to curr.next inside the loop." }
        ],
        testCases: [
          { name: "Case 1", input: "Linked List: 1 -> 2 -> 3 -> null", expectedOutput: "curr ends up as null" }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void main(String[] args) {\n    Node curr = head; // Assume head is defined\n    // Traverse the list\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  struct Node* curr = head; // Assume head is defined\n  // Traverse the list\n  \n  return 0;\n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nint main() {\n  Node* curr = head; // Assume head is defined\n  // Traverse the list\n  \n  return 0;\n}",
          javascript: "let curr = head; // Assume head is defined\n// Traverse the list\n\n",
          python: "curr = head # Assume head is defined\n# Traverse the list\n\n"
        }
      }
    ]
  },
  {
    id: "q5",
    title: "The LIFO/FIFO Protocol",
    difficulty: "Intermediate",
    description: "Understand Stacks and Queues to process execution commands and data streams.",
    chapters: [
      {
        id: 1,
        title: "Chapter 1: Stack Operations",
        narrative: [
          "Commands are overflowing! We need a Last-In-First-Out (LIFO) structure.",
          "Initialize a Stack and 'push' the number 42 onto it."
        ],
        successNarrative: [
          "Command stacked securely.",
          "The system will execute the most recent commands first."
        ],
        task: "Create a Stack and push 42.",
        hint: "stack.push(42); or stack.append(42)",
        validations: [
          { regex: /(push|append|add)\(\s*42\s*\)/, message: "Must push the number 42 onto the stack." }
        ],
        testCases: [
          { name: "Case 1", input: "Empty Stack []", expectedOutput: "Stack [42]" }
        ],
        defaultCode: {
          java: "import java.util.Stack;\npublic class Main {\n  public static void main(String[] args) {\n    // Initialize stack and push 42\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  // Simulate stack push\n  \n  return 0;\n}",
          cpp: "#include <iostream>\n#include <stack>\nusing namespace std;\n\nint main() {\n  // Initialize stack and push 42\n  \n  return 0;\n}",
          javascript: "let stack = [];\n// Push 42 to stack\n\n",
          python: "stack = []\n# Append 42 to stack\n\n"
        }
      }
    ]
  },
  {
    id: "q6",
    title: "The Key-Value Encryption",
    difficulty: "Advanced",
    description: "Use Hash Maps to perform O(1) lookups and crack encryption algorithms.",
    chapters: [
      {
        id: 1,
        title: "Chapter 1: The Target Pair",
        narrative: [
          "We must find two power frequencies that sum to our target threshold.",
          "A brute-force double loop is too slow. We need O(n) time!",
          "Use a HashMap to store frequencies you have seen, and check if the complement (target - current) exists in the map."
        ],
        successNarrative: [
          "Frequencies matched!",
          "The O(n) algorithm executed flawlessly."
        ],
        task: "Check if the map contains the complement, then return the indices.",
        hint: "if (map.containsKey(complement)) return new int[]{map.get(complement), i};",
        validations: [
          { regex: /(containsKey|has|in|find|count)/, message: "Must use the map/dictionary to check for the complement." }
        ],
        testCases: [
          { name: "Case 1", input: "nums = [2, 7, 11, 15], target = 9", expectedOutput: "[0, 1]" },
          { name: "Case 2", input: "nums = [3, 2, 4], target = 6", expectedOutput: "[1, 2]" }
        ],
        defaultCode: {
          java: "import java.util.HashMap;\nimport java.util.Map;\npublic class Main {\n  public static void main(String[] args) {\n    // Implement O(n) Two-Sum logic\n    \n  }\n}",
          c: "#include <stdio.h>\n\nint main() {\n  // Implement O(n) Two-Sum logic\n  \n  return 0;\n}",
          cpp: "#include <iostream>\n#include <unordered_map>\nusing namespace std;\n\nint main() {\n  // Implement O(n) Two-Sum logic\n  \n  return 0;\n}",
          javascript: "let map = new Map();\n// Implement O(n) Two-Sum logic\n\n",
          python: "seen = {}\n# Implement O(n) Two-Sum logic\n\n"
        }
      }
    ]
  },
  {
    id: "q7",
    title: "The Branching Pathways",
    difficulty: "Advanced",
    description: "Navigate recursive binary trees to map out the ship's entire directory structure.",
    chapters: [
      {
        id: 1,
        title: "Chapter 1: Depth-First Search",
        narrative: [
          "We must scan the entire file system hierarchy, which is structured as a Binary Tree.",
          "Write a recursive DFS function that traverses the 'left' child, then the 'right' child.",
          "Don't forget the base case: if the node is null, return!"
        ],
        successNarrative: [
          "File system mapped completely.",
          "We have reached the root directory."
        ],
        task: "Write the recursive calls for a DFS traversal.",
        hint: "dfs(node.left); dfs(node.right);",
        validations: [
          { regex: /dfs\s*\(\s*.*(\.|->)left\s*\)/, message: "Must recursively call dfs on the left child." },
          { regex: /dfs\s*\(\s*.*(\.|->)right\s*\)/, message: "Must recursively call dfs on the right child." }
        ],
        testCases: [
          { name: "Case 1", input: "Root Node [1, 2, 3]", expectedOutput: "DFS visits all nodes 1, 2, 3" }
        ],
        defaultCode: {
          java: "public class Main {\n  public static void dfs(TreeNode node) {\n    if (node == null) return;\n    // Recursive calls here\n    \n  }\n}",
          c: "#include <stdio.h>\n\nvoid dfs(struct TreeNode* node) {\n  if (node == NULL) return;\n  // Recursive calls here\n  \n}",
          cpp: "#include <iostream>\nusing namespace std;\n\nvoid dfs(TreeNode* node) {\n  if (node == nullptr) return;\n  // Recursive calls here\n  \n}",
          javascript: "function dfs(node) {\n  if (!node) return;\n  // Recursive calls here\n  \n}",
          python: "def dfs(node):\n  if not node: return\n  # Recursive calls here\n  \n"
        }
      }
    ]
  }
];
