# Build an AI-Powered Algorithm Visualizer with Animated Dry Run

Act as a senior full-stack engineer, AI application architect, and UI/UX designer. Implement a **production-quality AI Algorithm Visualizer** in my existing application.

## 1. Core Feature

A user enters or selects a DSA question. The question is sent to my backend, which calls the Gemini API. Gemini analyzes the problem and generates a structured explanation that the frontend uses to render:

1. **Algorithm visualization** — visualize how the algorithm works step by step.
2. **Animated dry run** — show how variables, pointers, arrays, recursion, and data structures change at every step.
3. **Algorithm flowchart** — display the algorithm as a visual diagram with connected nodes and arrows.
4. **Code visualization** — show the relevant Java code and highlight the currently executing line.
5. **Execution state** — display variable values, the current iteration, and intermediate algorithm state.
6. **Explanation panel** — explain the current step in simple language.
7. **Complexity analysis** — show time and space complexity with a brief explanation.

The result should feel like a premium interactive educational product, not a static ChatGPT response.

## 2. Inspect My Existing Project First

Before writing code:

- Inspect the existing frontend, backend, folder structure, routing, components, and API conventions.
- Identify the current framework, state management, styling system, database, and Gemini integration, if present.
- Reuse the existing architecture, authentication, components, and dependencies wherever possible.
- Find the existing question submission feature and extend it instead of creating a duplicate feature.
- Do not rewrite unrelated functionality.
- Do not replace my existing CSS framework or introduce unnecessary dependencies.
- Check the existing `.env` setup and keep all API credentials secure.

Create a short implementation plan, then implement the complete feature.

## 3. Backend Architecture

Implement this flow:

**User Question → Frontend API Request → Backend → Gemini API → Structured Algorithm Response → Frontend Visualization Engine**

Requirements:

- The frontend sends the DSA problem statement and optional programming language to the backend.
- The backend calls Gemini using the existing configuration, or adds a secure server-side integration if one does not exist.
- Store the Gemini API key exclusively in backend environment variables.
- Never expose the API key in frontend code or browser responses.
- Validate incoming requests and handle missing questions, API failures, timeouts, malformed JSON, and rate limits.
- Validate Gemini's output against a defined schema before returning it to the frontend.
- Do not return raw HTML or JavaScript from Gemini for direct execution in the browser.
- Do not use `eval()`, `new Function()`, or unsafe HTML injection.

Keep the implementation consistent with my existing backend framework and API conventions.

## 4. Gemini Prompt and Response Format

Design a strong system prompt that makes Gemini behave as a DSA instructor and algorithm trace generator.

Gemini should return structured JSON containing:

- Problem summary
- Algorithm name and category
- Intuition
- Input and output examples
- Algorithm steps
- Language-specific code
- Flowchart nodes and edges
- Initial state
- Ordered execution trace
- Variable updates
- Array and data structure changes
- Current code line for each step
- Explanation for each step
- Final result
- Time and space complexity

Use a predictable schema similar to this illustrative example:

```json
{
  "problem": {
    "title": "Two Sum",
    "description": "Find two indices whose values sum to the target",
    "algorithm": "Hash Map",
    "language": "java"
  },
  "complexity": {
    "time": "O(n)",
    "space": "O(n)"
  },
  "code": [
    {
      "line": 1,
      "text": "Map<Integer, Integer> map = new HashMap<>();"
    }
  ],
  "flowchart": {
    "nodes": [
      {"id": "start", "label": "Start", "type": "start"},
      {"id": "loop", "label": "Iterate through array", "type": "process"},
      {"id": "end", "label": "Return result", "type": "end"}
    ],
    "edges": [
      {"from": "start", "to": "loop", "label": ""},
      {"from": "loop", "to": "end", "label": "Result found"}
    ]
  },
  "initialState": {
    "array": [2, 7, 11, 15],
    "variables": {"target": 9, "i": null}
  },
  "trace": [
    {
      "step": 1,
      "line": 1,
      "title": "Initialize the hash map",
      "explanation": "Create an empty map to store previously seen values.",
      "state": {
        "array": [2, 7, 11, 15],
        "variables": {"target": 9, "i": null},
        "dataStructures": {"map": {}}
      },
      "operations": [
        {"type": "initialize", "target": "map", "value": {}}
      ]
    }
  ],
  "result": {
    "value": [0, 1],
    "explanation": "The elements at indices 0 and 1 sum to 9."
  }
}
```

Treat this as an illustrative schema, not a complete fixed response. Define and validate the actual schema carefully. Every trace step must contain a complete, consistent snapshot of the state needed to render the animation.

Gemini must generate a logically correct trace for the selected problem and input. Do not fabricate variable updates, indices, return values, or complexity analysis.

## 5. Interactive Animation Engine

Build a reusable visualization engine that renders the trace returned by the backend.

### Playback controls

Include:

- Play
- Pause
- Next step
- Previous step
- Restart
- Playback speed control: 0.5×, 1×, 1.5×, 2×
- Progress bar
- Current step / total steps
- Scrubbing to any execution step

Playback must be deterministic. The same step should always display the same state, regardless of whether the user navigates forward, backward, or jumps directly to that step.

Use a single trace index as the source of truth for the displayed execution state. Avoid independent animation states becoming inconsistent.

### Animated data structures

For arrays:

- Render each array element in an individual cell.
- Highlight the active index.
- Display pointer labels such as `i`, `j`, `left`, and `right`.
- Animate comparisons, swaps, insertions, deletions, and updates.
- Display changed values clearly.
- Show array indices beneath the cells.

For hash maps:

- Display key-value entries.
- Animate lookups, insertions, and updates.
- Visually distinguish a found key from a missing key.

For stacks and queues:

- Render the structure with clear top/front/rear indicators.
- Animate push, pop, enqueue, and dequeue.

For linked lists:

- Show nodes connected by arrows.
- Animate pointer changes and node insertion/deletion.

For trees and graphs:

- Render nodes and edges.
- Highlight visited nodes and active edges.
- Display traversal order and relevant values.

For recursion and backtracking:

- Render a recursion tree or call stack.
- Show function arguments, local variables, and return values.
- Animate entering and exiting recursive calls.
- Show the current branch and backtracking operations.

Select the visualization appropriate to the algorithm. Do not show an array-only animation for every type of problem.

## 6. Code Execution Visualization

Create a code panel with syntax highlighting.

Requirements:

- Display line numbers.
- Highlight the exact code line associated with the current trace step.
- Scroll the active line into view when necessary.
- Keep code highlighting synchronized with playback.
- Show variable values in a dedicated variables panel.
- Show the current operation, such as comparison, assignment, swap, function call, or return.
- Support Java as the primary language and preserve the option to add other languages later.

The code, variables, explanation, and data structure visualization must all update from the same trace index.

## 7. Algorithm Flowchart and Diagrams

Create an interactive algorithm flowchart with:

- Start and end nodes.
- Process nodes.
- Decision nodes.
- True/false branches.
- Directed arrows.
- Labels on decision edges.
- Clear visual distinction between node types.
- Animated highlighting of the active node and edge.

The current flowchart node should correspond to the execution trace whenever possible.

Use an appropriate diagram library already present in the project. If none exists, consider React Flow for flowcharts and a suitable graph-rendering approach for graph algorithms.

Make large diagrams navigable using zoom, pan, and fit-to-view controls.

The flowchart must represent the actual algorithm, including loops and branching, rather than a decorative sequence of boxes.

## 8. User Interface and Visual Design

Design a clean, modern, premium educational interface.

Suggested layout:

**Top section**
- Problem title and difficulty, if available.
- Algorithm category.
- Language selector.
- Generate visualization button.

**Main workspace**
- Left panel: source code and active-line highlighting.
- Center panel: animated algorithm visualization.
- Right panel: variables, current step explanation, and data structure state.

**Lower section**
- Interactive flowchart.
- Step timeline.
- Complexity analysis.
- Final result and summary.

On smaller screens, turn the panels into responsive tabs or stacked sections instead of squeezing everything into narrow columns.

Visual requirements:

- Consistent spacing and typography.
- Clear visual hierarchy.
- Smooth but restrained animations.
- Readable labels and high contrast.
- Subtle transitions when values change.
- Loading skeletons during generation.
- Useful empty, success, and error states.
- Accessible controls and keyboard navigation where practical.

Use the project's existing design system. Avoid excessive gradients, glowing effects, decorative animations, and unnecessary visual clutter. Prioritize clarity and correctness.

## 9. Make the Dry Run Accurate

This is the most important requirement.

Do not implement an animation that merely changes colors or cycles through predefined steps without representing actual algorithm execution.

For each step, show:

1. What line of code is executing.
2. Which variables are involved.
3. The previous and current values.
4. What comparison or operation occurred.
5. How the data structure changed.
6. Why the algorithm takes this action.
7. What happens in the next step.

Use consistent snapshots and typed operations so the frontend can reconstruct the entire execution state.

Where practical, implement a deterministic local trace generator for supported algorithms. For Java code, do not attempt to execute arbitrary generated source code on the server merely to produce a trace.

For algorithms that cannot be traced reliably, clearly identify the limitation rather than displaying an invented execution.

Add validation that detects inconsistent traces, invalid indices, impossible state transitions, and incorrect final results for supported problems.

## 10. User Input and Example Testing

Allow users to:

- Enter a DSA question.
- Choose a supported language.
- Enter custom input values when the problem format permits.
- Generate the visualization.
- Replay the execution.
- Explore individual steps.
- Switch between the dry run, code, and flowchart views.

Use Two Sum as the initial demonstration with the input:

- Array: `[2, 7, 11, 15]`
- Target: `9`

The trace should correctly demonstrate the hash map lookup, complement calculation, insertion, successful match, and return of indices `[0, 1]`.

Also test at least one example from each supported visualization category, including sorting, binary search, recursion, trees, and graphs.

## 11. Performance and Reliability

- Use reusable React components and typed data structures where supported by the project.
- Keep playback smooth for traces containing hundreds of steps.
- Avoid rerendering unrelated components on every animation tick.
- Clean up animation frames and event listeners.
- Prevent duplicate generation requests.
- Handle malformed Gemini responses with a clear error message and retry option.
- Use bounded trace sizes and request timeouts.
- Keep API credentials and internal error details out of client responses.
- Add loading, error, and retry states.
- Make the implementation easy to extend with additional algorithms.

## 12. Suggested Component Structure

Adapt this to the existing codebase instead of creating unnecessary files.

- `AlgorithmVisualizer`
- `PlaybackControls`
- `CodePanel`
- `VariableInspector`
- `ArrayVisualizer`
- `HashMapVisualizer`
- `StackQueueVisualizer`
- `LinkedListVisualizer`
- `TreeVisualizer`
- `GraphVisualizer`
- `RecursionTreeVisualizer`
- `AlgorithmFlowchart`
- `StepExplanation`
- `ComplexityPanel`
- `useAlgorithmPlayback`
- Backend algorithm generation service
- Gemini prompt and response schema
- Trace validation utilities

## 13. Testing and Completion Criteria

After implementation:

- Run the existing frontend and backend tests.
- Run the build and resolve any errors introduced by the changes.
- Verify the frontend-to-backend-to-Gemini integration.
- Verify that the Gemini API key is never exposed in the frontend.
- Test playback in both directions, restarting, scrubbing, and speed changes.
- Confirm that all visualizations remain synchronized.
- Confirm that the flowchart represents actual control flow.
- Verify the Two Sum result and intermediate state transitions.
- Check mobile responsiveness and accessibility.
- Provide a concise summary of changed files, API endpoints, environment variables, and commands required to run the feature.

**Final instruction:** Implement the working feature in my existing repository. Do not stop after providing a plan, mockup, static UI, or pseudocode. Prioritize correct algorithm execution and trace consistency first, then deliver polished, synchronized animations and diagrams. If a requirement conflicts with the existing architecture, adapt it intelligently and explain the trade-off.

## Architecture Recommendation

Separate Gemini's explanation from the actual animation engine:

1. **User enters a DSA question** — problem statement and input.
2. **Backend + Gemini API** — generate structured algorithm steps, code, and explanations.
3. **Validate and construct execution trace** — verify state transitions and final results for supported algorithms.
4. **Synchronized visualization engine** — code highlighting, dry run, data structures, and flowchart.

Gemini can produce convincing explanations that contain incorrect intermediate states. For reliable educational visualizations, generate traces with deterministic algorithm implementations for supported problems and use Gemini for explanations and algorithm structure. This makes the feature more like an interactive VisuAlgo + AI coding tutor than a chatbot that returns code with a basic animation.
