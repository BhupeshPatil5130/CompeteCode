import { useEffect, useState, useCallback } from 'react';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import {
  Play, Sparkles, Send, Bot, CheckCircle, XCircle, Clock, ArrowLeft,
  Cpu, Activity, X, BookOpen, Code2, TrendingUp, StickyNote, ChevronDown,
  GitCompare, Lightbulb
} from 'lucide-react';
import toast from 'react-hot-toast';
import { problemApi, submissionApi, roadmapApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import CodeEditor from '../components/CodeEditor';
import AnimatedExplanation from '../components/AnimatedExplanation';
import DifficultyBadge from '../components/DifficultyBadge';

const LANGUAGES = [
  { id: 'java', label: 'Java' },
  { id: 'python', label: 'Python' },
  { id: 'cpp', label: 'C++' },
  { id: 'javascript', label: 'JavaScript' },
];

// ── Code Templates Library ───────────────────────────────────────────
const CODE_TEMPLATES = {
  java: [
    {
      name: 'BFS Template',
      code: `// BFS Template\nQueue<Integer> queue = new LinkedList<>();\nSet<Integer> visited = new HashSet<>();\nqueue.offer(start);\nvisited.add(start);\nwhile (!queue.isEmpty()) {\n    int node = queue.poll();\n    for (int neighbor : graph.get(node)) {\n        if (!visited.contains(neighbor)) {\n            visited.add(neighbor);\n            queue.offer(neighbor);\n        }\n    }\n}`,
    },
    {
      name: 'DFS Template',
      code: `// DFS Template\nprivate void dfs(int node, boolean[] visited, List<List<Integer>> graph) {\n    visited[node] = true;\n    for (int neighbor : graph.get(node)) {\n        if (!visited[neighbor]) dfs(neighbor, visited, graph);\n    }\n}`,
    },
    {
      name: 'Binary Search',
      code: `// Binary Search Template\nint left = 0, right = arr.length - 1;\nwhile (left <= right) {\n    int mid = left + (right - left) / 2;\n    if (arr[mid] == target) return mid;\n    else if (arr[mid] < target) left = mid + 1;\n    else right = mid - 1;\n}\nreturn -1;`,
    },
    {
      name: 'Two Pointers',
      code: `// Two Pointers Template\nint left = 0, right = arr.length - 1;\nwhile (left < right) {\n    if (condition) {\n        left++;\n    } else {\n        right--;\n    }\n}`,
    },
    {
      name: 'Sliding Window',
      code: `// Sliding Window Template\nint left = 0, maxLen = 0;\nMap<Character, Integer> freq = new HashMap<>();\nfor (int right = 0; right < s.length(); right++) {\n    freq.merge(s.charAt(right), 1, Integer::sum);\n    while (/* invalid condition */) {\n        freq.merge(s.charAt(left), -1, Integer::sum);\n        if (freq.get(s.charAt(left)) == 0) freq.remove(s.charAt(left));\n        left++;\n    }\n    maxLen = Math.max(maxLen, right - left + 1);\n}`,
    },
    {
      name: 'DP Template',
      code: `// Dynamic Programming Template\nint n = arr.length;\nint[] dp = new int[n + 1];\ndp[0] = baseCase;\nfor (int i = 1; i <= n; i++) {\n    dp[i] = Math.max(dp[i-1], /* recurrence */);\n}\nreturn dp[n];`,
    },
  ],
  python: [
    {
      name: 'BFS Template',
      code: `from collections import deque\n# BFS Template\nqueue = deque([start])\nvisited = {start}\nwhile queue:\n    node = queue.popleft()\n    for neighbor in graph[node]:\n        if neighbor not in visited:\n            visited.add(neighbor)\n            queue.append(neighbor)`,
    },
    {
      name: 'Binary Search',
      code: `# Binary Search Template\nleft, right = 0, len(arr) - 1\nwhile left <= right:\n    mid = (left + right) // 2\n    if arr[mid] == target:\n        return mid\n    elif arr[mid] < target:\n        left = mid + 1\n    else:\n        right = mid - 1\nreturn -1`,
    },
    {
      name: 'Two Pointers',
      code: `# Two Pointers Template\nleft, right = 0, len(arr) - 1\nwhile left < right:\n    if condition:\n        left += 1\n    else:\n        right -= 1`,
    },
    {
      name: 'DP Template',
      code: `# Dynamic Programming Template\nn = len(arr)\ndp = [0] * (n + 1)\ndp[0] = base_case\nfor i in range(1, n + 1):\n    dp[i] = max(dp[i-1], # recurrence\n    )\nreturn dp[n]`,
    },
  ],
  cpp: [
    {
      name: 'BFS Template',
      code: `// BFS Template\nqueue<int> q;\nunordered_set<int> visited;\nq.push(start);\nvisited.insert(start);\nwhile (!q.empty()) {\n    int node = q.front(); q.pop();\n    for (int neighbor : graph[node]) {\n        if (!visited.count(neighbor)) {\n            visited.insert(neighbor);\n            q.push(neighbor);\n        }\n    }\n}`,
    },
    {
      name: 'Binary Search',
      code: `// Binary Search Template\nint left = 0, right = arr.size() - 1;\nwhile (left <= right) {\n    int mid = left + (right - left) / 2;\n    if (arr[mid] == target) return mid;\n    else if (arr[mid] < target) left = mid + 1;\n    else right = mid - 1;\n}\nreturn -1;`,
    },
  ],
  javascript: [
    {
      name: 'BFS Template',
      code: `// BFS Template\nconst queue = [start];\nconst visited = new Set([start]);\nwhile (queue.length) {\n    const node = queue.shift();\n    for (const neighbor of graph[node] || []) {\n        if (!visited.has(neighbor)) {\n            visited.add(neighbor);\n            queue.push(neighbor);\n        }\n    }\n}`,
    },
    {
      name: 'Binary Search',
      code: `// Binary Search Template\nlet left = 0, right = arr.length - 1;\nwhile (left <= right) {\n    const mid = Math.floor((left + right) / 2);\n    if (arr[mid] === target) return mid;\n    else if (arr[mid] < target) left = mid + 1;\n    else right = mid - 1;\n}\nreturn -1;`,
    },
  ],
};

export default function ProblemIDE() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const battleId = searchParams.get('battleId');
  const roadmapId = searchParams.get('roadmapId');
  const { isAuthenticated } = useAuth();

  const [problem, setProblem] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [explainError, setExplainError] = useState('');
  const [language, setLanguage] = useState('java');
  const [code, setCode] = useState('');
  const [samples, setSamples] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);
  const [feedback, setFeedback] = useState('');
  const [showExplanation, setShowExplanation] = useState(false);
  const [explanation, setExplanation] = useState(null);
  const [loadingExplain, setLoadingExplain] = useState(false);
  const [stdin, setStdin] = useState('');
  const [consoleOutput, setConsoleOutput] = useState('');
  const [activeTab, setActiveTab] = useState('description');

  const [analyzingComplexity, setAnalyzingComplexity] = useState(false);
  const [complexityResult, setComplexityResult] = useState(null);
  const [showComplexityModal, setShowComplexityModal] = useState(false);

  // New feature states
  const [acceptanceRate, setAcceptanceRate] = useState(null);
  const [showTemplates, setShowTemplates] = useState(false);

  // Notes feature: per-problem markdown notes in localStorage
  const notesKey = `cc_notes_${id}`;
  const [notes, setNotes] = useState(() => {
    try { return localStorage.getItem(notesKey) || ''; } catch { return ''; }
  });
  const saveNotes = useCallback((val) => {
    setNotes(val);
    try { localStorage.setItem(notesKey, val); } catch {}
  }, [notesKey]);

  useEffect(() => {
    setLoadError('');
    problemApi.getById(id)
      .then(({ data }) => setProblem(data))
      .catch((err) => setLoadError(err.response?.data?.error || 'Failed to load problem'));
    problemApi.getSamples(id).then(({ data }) => {
      setSamples(data);
      if (data && data.length > 0) setStdin(data[0].input);
    }).catch(() => {});
    // Fetch acceptance rate
    problemApi.getAcceptanceRate(id).then(({ data }) => setAcceptanceRate(data)).catch(() => {});
  }, [id]);

  useEffect(() => {
    problemApi.getStarterCode(id, language).then(({ data }) => setCode(data.code || ''));
  }, [id, language]);

  const handleSubmit = async () => {
    if (!isAuthenticated) return;
    setSubmitting(true);
    setResult(null);
    setFeedback('');
    try {
      const { data } = await submissionApi.submit({
        problemId: Number(id),
        code,
        language,
        battleId: battleId ? Number(battleId) : null,
      });
      setResult(data);
      setActiveTab('submissions');
      if (data.status === 'ACCEPTED') toast.success(`✅ Accepted! ${data.testsPassed}/${data.testsTotal} tests passed`);
      else toast.error(`❌ ${data.status} — ${data.testsPassed}/${data.testsTotal} tests passed`);
      if (data.status === 'ACCEPTED' && roadmapId) {
        roadmapApi.markComplete(roadmapId, id).catch(() => {});
      }
      if (data.submissionId) {
        try {
          const fb = await submissionApi.getFeedback(data.submissionId);
          setFeedback(fb.data.feedback);
        } catch {
          setFeedback('');
        }
      }
    } catch (err) {
      setResult({ status: 'ERROR', errorMessage: err.response?.data?.error || 'Submission failed' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleExplain = async () => {
    setLoadingExplain(true);
    setExplainError('');
    try {
      const { data } = await problemApi.getExplanation(id);
      setExplanation(data);
      setShowExplanation(true);
    } catch (err) {
      setExplainError(err.response?.data?.error || 'Failed to load explanation');
    } finally {
      setLoadingExplain(false);
    }
  };

  const handleRun = async () => {
    setConsoleOutput('Running...\n');
    try {
      const { data } = await submissionApi.runCode({ code, language, stdin });
      if (data.compileOutput && data.compileOutput.trim()) {
        setConsoleOutput(`Compilation Error:\n${data.compileOutput}`);
      } else if (data.stdout) {
        setConsoleOutput(data.stdout);
      } else if (data.stderr) {
        setConsoleOutput(`Error:\n${data.stderr}`);
      } else {
        setConsoleOutput(`Status: ${data.statusDescription}`);
      }
    } catch (err) {
      setConsoleOutput(`An error occurred: ${err.message}`);
    }
  };

  const handleAnalyzeComplexity = async () => {
    setAnalyzingComplexity(true);
    setShowComplexityModal(true);
    setComplexityResult(null);
    try {
      const { data } = await problemApi.analyzeComplexity(id, code, language);
      setComplexityResult(data.analysis);
    } catch (err) {
      toast.error('Failed to analyze complexity');
      setShowComplexityModal(false);
    } finally {
      setAnalyzingComplexity(false);
    }
  };

  const applyTemplate = (template) => {
    setCode((prev) => prev + '\n\n' + template.code);
    setShowTemplates(false);
    toast.success(`Template "${template.name}" inserted`);
  };

  if (loadError) {
    return (
      <div className="p-8 text-center">
        <p className="text-red-400 mb-4 text-sm">{loadError}</p>
        <Link to="/problems" className="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-neutral-700 text-sm text-neutral-300 hover:bg-surface-hover transition-colors">
          <ArrowLeft className="w-4 h-4" /> Back to Problems
        </Link>
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="flex items-center justify-center h-[calc(100vh-3.5rem)]">
        <div className="text-neutral-500 text-sm animate-pulse">Loading problem...</div>
      </div>
    );
  }

  const tabs = [
    { id: 'description', label: 'Description', icon: BookOpen },
    { id: 'submissions', label: 'Results', icon: CheckCircle },
    { id: 'notes', label: 'Notes', icon: StickyNote },
  ];

  return (
    <div className="h-[calc(100vh-3.5rem)] flex flex-col lg:flex-row">
      {/* ── Left panel ──────────────────────────────── */}
      <div className="lg:w-[42%] flex flex-col border-r border-border overflow-hidden">
        {/* Problem header */}
        <div className="px-5 pt-5 pb-3 border-b border-border bg-surface-secondary">
          <div className="flex items-start justify-between gap-3 mb-2">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-3 mb-1.5">
                {battleId && (
                  <Link to={`/battleground/${battleId}`} className="flex items-center gap-1 text-xs text-amber-500 hover:text-amber-400 font-semibold bg-amber-500/10 px-2 py-0.5 rounded-md transition-colors">
                    <ArrowLeft className="w-3 h-3" /> Battle
                  </Link>
                )}
                <DifficultyBadge difficulty={problem.difficulty} />
                <span className="text-xs text-neutral-600">{problem.topic}</span>
              </div>
              <h1 className="text-lg font-bold text-white truncate">{problem.title}</h1>

              {/* Acceptance Rate */}
              {acceptanceRate && (
                <div className="flex items-center gap-3 mt-1.5 text-xs text-neutral-500">
                  <span className="flex items-center gap-1">
                    <TrendingUp className="w-3 h-3 text-emerald-500" />
                    <span className="text-emerald-400 font-medium">{acceptanceRate.acceptanceRate}%</span>
                    <span>acceptance</span>
                  </span>
                  <span className="text-neutral-700">·</span>
                  <span>{acceptanceRate.total} submissions</span>
                </div>
              )}
            </div>
            <button
              onClick={handleExplain}
              disabled={loadingExplain}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium bg-purple-500/10 text-purple-400 border border-purple-500/20 hover:bg-purple-500/15 transition-colors disabled:opacity-50 shrink-0"
            >
              <Sparkles className="w-3.5 h-3.5" />
              {loadingExplain ? 'Loading...' : 'Animate'}
            </button>
          </div>

          {explainError && (
            <div className="mt-2 p-2 rounded-md bg-red-500/10 border border-red-500/20 text-red-400 text-xs">
              {explainError}
            </div>
          )}

          {/* Tabs */}
          <div className="flex gap-4 mt-3">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`pb-2 text-xs font-medium transition-colors border-b-2 flex items-center gap-1 ${
                  activeTab === tab.id
                    ? 'text-amber-400 border-amber-400'
                    : 'text-neutral-500 border-transparent hover:text-neutral-300'
                }`}
              >
                <tab.icon className="w-3 h-3" />
                {tab.label}
                {tab.id === 'notes' && notes && (
                  <span className="w-1.5 h-1.5 rounded-full bg-amber-400 ml-0.5" />
                )}
              </button>
            ))}
          </div>
        </div>

        {/* Tab content */}
        <div className="flex-1 overflow-y-auto p-5">
          {/* Description tab */}
          {activeTab === 'description' && (
            <div className="space-y-5 text-sm">
              <div>
                <h3 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-2">Description</h3>
                <p className="text-neutral-300 whitespace-pre-wrap leading-relaxed">{problem.description}</p>
              </div>
              {problem.examples && (
                <div>
                  <h3 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-2">Examples</h3>
                  <pre className="bg-surface-tertiary border border-border rounded-lg p-3.5 text-xs font-mono text-neutral-300 overflow-x-auto leading-relaxed">{problem.examples}</pre>
                </div>
              )}
              {problem.constraints && (
                <div>
                  <h3 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-2">Constraints</h3>
                  <pre className="text-xs text-neutral-400 whitespace-pre-wrap leading-relaxed">{problem.constraints}</pre>
                </div>
              )}
              {samples.length > 0 && (
                <div>
                  <h3 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-2">Sample Tests</h3>
                  {samples.map((s, i) => (
                    <div key={i} className="bg-surface-tertiary border border-border rounded-lg p-3 mb-2 text-xs font-mono">
                      <div><span className="text-purple-400 font-semibold">Input:</span> <span className="text-neutral-300">{s.input}</span></div>
                      <div><span className="text-emerald-400 font-semibold">Output:</span> <span className="text-neutral-300">{s.expectedOutput}</span></div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Results tab */}
          {activeTab === 'submissions' && (
            <div className="space-y-4">
              {result ? (
                <motion.div
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  className={`p-4 rounded-xl border ${
                    result.status === 'ACCEPTED'
                      ? 'bg-emerald-500/5 border-emerald-500/20'
                      : 'bg-red-500/5 border-red-500/20'
                  }`}
                >
                  <div className="flex items-center gap-2 font-semibold text-sm mb-2">
                    {result.status === 'ACCEPTED' ? (
                      <CheckCircle className="w-4 h-4 text-emerald-400" />
                    ) : (
                      <XCircle className="w-4 h-4 text-red-400" />
                    )}
                    <span className={result.status === 'ACCEPTED' ? 'text-emerald-400' : 'text-red-400'}>
                      {result.status}
                    </span>
                    {result.score !== undefined && (
                      <span className="text-neutral-500 font-normal">— Score: {result.score}</span>
                    )}
                  </div>
                  <p className="text-xs text-neutral-500">
                    {result.testsPassed}/{result.testsTotal} tests passed
                    {result.executionTimeMs && ` · ${result.executionTimeMs}ms`}
                  </p>
                  {result.errorMessage && (
                    <pre className="mt-3 text-xs text-red-400 bg-red-500/5 border border-red-500/20 rounded-lg p-3 overflow-auto whitespace-pre-wrap">
                      {result.errorMessage}
                    </pre>
                  )}
                  {result.testResults?.map((t) => (
                    <div key={t.testNumber} className="text-xs mt-2 font-mono text-neutral-400">
                      Test {t.testNumber}: {t.passed ? <span className="text-emerald-400">✓ Pass</span> : <span className="text-red-400">✗ Fail</span>}
                      {t.error && <span className="text-red-400 ml-2">{t.error}</span>}
                    </div>
                  ))}
                </motion.div>
              ) : (
                <div className="py-12 text-center">
                  <Code2 className="w-8 h-8 text-neutral-700 mx-auto mb-2" />
                  <p className="text-neutral-600 text-sm">Submit your code to see results here.</p>
                </div>
              )}

              {feedback && (
                <div className="p-4 rounded-xl bg-purple-500/5 border border-purple-500/20">
                  <div className="flex items-center gap-2 text-purple-400 font-semibold text-sm mb-2">
                    <Bot className="w-4 h-4" /> AI Feedback
                  </div>
                  <p className="text-xs text-neutral-400 whitespace-pre-wrap leading-relaxed">{feedback}</p>
                </div>
              )}
            </div>
          )}

          {/* Notes tab ──────────────────────────── */}
          {activeTab === 'notes' && (
            <div className="space-y-3 h-full flex flex-col">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <StickyNote className="w-3.5 h-3.5 text-amber-400" />
                  <h3 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider">My Notes</h3>
                </div>
                <span className="text-[10px] text-neutral-600">Auto-saved locally</span>
              </div>
              <textarea
                value={notes}
                onChange={(e) => saveNotes(e.target.value)}
                placeholder="Write your notes, approach, or observations here...&#10;&#10;Supports plain text. Saved automatically to your browser."
                className="flex-1 min-h-[300px] w-full p-3.5 bg-surface-tertiary border border-border rounded-xl text-xs font-mono text-neutral-300 resize-none focus:outline-none focus:border-amber-500/30 placeholder-neutral-700 leading-relaxed"
              />
              {notes && (
                <button
                  onClick={() => saveNotes('')}
                  className="text-xs text-neutral-600 hover:text-red-400 transition-colors self-start"
                >
                  Clear notes
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* ── Right panel — Editor ────────────────────── */}
      <div className="flex-1 flex flex-col min-h-0">
        {/* Editor toolbar */}
        <div className="flex items-center justify-between px-4 py-2 border-b border-border bg-surface-secondary flex-wrap gap-2">
          <div className="flex items-center gap-2">
            <select
              value={language}
              onChange={(e) => setLanguage(e.target.value)}
              className="bg-surface-tertiary border border-border rounded-md px-2.5 py-1 text-xs text-neutral-300 focus:outline-none focus:border-amber-500/50 cursor-pointer"
            >
              {LANGUAGES.map((l) => (
                <option key={l.id} value={l.id}>{l.label}</option>
              ))}
            </select>

            {/* Templates dropdown */}
            <div className="relative">
              <button
                onClick={() => setShowTemplates(!showTemplates)}
                className="flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-medium border border-neutral-700 text-neutral-400 hover:bg-surface-hover transition-colors"
              >
                <Lightbulb className="w-3 h-3" /> Templates
                <ChevronDown className={`w-3 h-3 transition-transform ${showTemplates ? 'rotate-180' : ''}`} />
              </button>
              {showTemplates && (
                <div className="absolute top-full left-0 mt-1 z-50 bg-surface-elevated border border-border rounded-lg shadow-2xl min-w-48 overflow-hidden">
                  {(CODE_TEMPLATES[language] || []).map((t) => (
                    <button
                      key={t.name}
                      onClick={() => applyTemplate(t)}
                      className="w-full text-left px-3 py-2 text-xs text-neutral-300 hover:bg-surface-hover hover:text-white transition-colors"
                    >
                      <Code2 className="w-3 h-3 inline mr-2 text-amber-400" />
                      {t.name}
                    </button>
                  ))}
                  {(!CODE_TEMPLATES[language] || CODE_TEMPLATES[language].length === 0) && (
                    <p className="px-3 py-2 text-xs text-neutral-600">No templates for {language}</p>
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="flex gap-2">
            <button
              onClick={handleAnalyzeComplexity}
              disabled={analyzingComplexity}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold bg-purple-500/10 hover:bg-purple-500/20 text-purple-400 border border-purple-500/20 transition-colors disabled:opacity-50"
            >
              {analyzingComplexity ? <Activity className="w-3.5 h-3.5 animate-spin" /> : <Sparkles className="w-3.5 h-3.5" />}
              {analyzingComplexity ? 'Analyzing...' : 'Complexity'}
            </button>
            <button
              onClick={handleRun}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium border border-neutral-700 text-neutral-300 hover:bg-surface-hover transition-colors"
            >
              <Play className="w-3.5 h-3.5" /> Run
            </button>
            {isAuthenticated ? (
              <button
                onClick={handleSubmit}
                disabled={submitting}
                className="flex items-center gap-1.5 px-4 py-1.5 rounded-md text-xs font-semibold bg-emerald-500 hover:bg-emerald-600 text-white transition-colors disabled:opacity-50"
              >
                <Send className="w-3.5 h-3.5" />
                {submitting ? 'Checking...' : 'Submit'}
              </button>
            ) : (
              <Link
                to="/login"
                className="flex items-center gap-1.5 px-4 py-1.5 rounded-md text-xs font-semibold bg-amber-500 hover:bg-amber-600 text-black transition-colors"
              >
                Login to Submit
              </Link>
            )}
          </div>
        </div>

        {/* Submit loading bar */}
        {submitting && (
          <div className="h-0.5 bg-surface-tertiary overflow-hidden">
            <motion.div
              className="h-full bg-amber-500"
              initial={{ width: '0%' }}
              animate={{ width: '90%' }}
              transition={{ duration: 8, ease: 'linear' }}
            />
          </div>
        )}

        {/* Code editor */}
        <div className="flex-1 min-h-[200px]">
          <CodeEditor value={code} onChange={setCode} language={language} height="100%" />
        </div>

        {/* Console */}
        <div className="border-t border-border h-40 flex flex-col">
          <div className="px-4 py-1.5 text-[10px] text-neutral-600 border-b border-border bg-surface-secondary font-medium uppercase tracking-wider">
            Console
          </div>
          <div className="flex flex-1 min-h-0">
            <textarea
              value={stdin}
              onChange={(e) => setStdin(e.target.value)}
              placeholder="stdin input..."
              className="w-1/3 p-3 bg-surface-secondary border-r border-border text-xs font-mono text-neutral-300 resize-none focus:outline-none placeholder-neutral-700"
            />
            <pre className="flex-1 p-3 text-xs font-mono text-neutral-400 overflow-auto bg-surface">
              {consoleOutput || 'Output will appear here...'}
            </pre>
          </div>
        </div>
      </div>

      {/* Animated Explanation Overlay */}
      <AnimatePresence>
        {showExplanation && (
          <AnimatedExplanation explanation={explanation} onClose={() => setShowExplanation(false)} />
        )}
      </AnimatePresence>

      {/* Complexity Modal */}
      <AnimatePresence>
        {showComplexityModal && complexityResult && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={() => setShowComplexityModal(false)}>
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="bg-surface-elevated/95 backdrop-blur-xl border border-purple-500/30 rounded-2xl p-6 w-full max-w-lg shadow-[0_0_50px_rgba(168,85,247,0.15)] relative overflow-hidden"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="absolute top-0 right-0 w-32 h-32 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-purple-500/20 flex items-center justify-center border border-purple-500/30">
                    <Activity className="w-5 h-5 text-purple-400" />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-white tracking-wide">AI Complexity Analysis</h3>
                    <p className="text-xs text-neutral-400 uppercase tracking-widest mt-0.5">Static Code Evaluation</p>
                  </div>
                </div>
                <button onClick={() => setShowComplexityModal(false)} className="p-2 rounded-lg hover:bg-white/5 text-neutral-500 transition-colors">
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="grid grid-cols-2 gap-4 mb-5">
                <div className="bg-black/40 rounded-xl p-4 border border-white/5 shadow-inner relative overflow-hidden">
                  <div className="absolute top-0 left-0 w-1 h-full bg-amber-500" />
                  <p className="text-[10px] text-neutral-500 font-bold uppercase tracking-widest mb-1.5 flex items-center gap-1.5">
                    <Clock className="w-3 h-3 text-amber-500" /> Time Complexity
                  </p>
                  <p className="text-2xl font-mono font-bold text-amber-400">{complexityResult.timeComplexity}</p>
                </div>
                <div className="bg-black/40 rounded-xl p-4 border border-white/5 shadow-inner relative overflow-hidden">
                  <div className="absolute top-0 left-0 w-1 h-full bg-emerald-500" />
                  <p className="text-[10px] text-neutral-500 font-bold uppercase tracking-widest mb-1.5 flex items-center gap-1.5">
                    <Cpu className="w-3 h-3 text-emerald-500" /> Space Complexity
                  </p>
                  <p className="text-2xl font-mono font-bold text-emerald-400">{complexityResult.spaceComplexity}</p>
                </div>
              </div>

              <div className="bg-purple-500/5 rounded-xl p-4 border border-purple-500/20 shadow-inner">
                <p className="text-[10px] text-purple-400 font-bold uppercase tracking-widest mb-2 flex items-center gap-1.5">
                  <Sparkles className="w-3 h-3" /> AI Optimization Advice
                </p>
                <p className="text-sm text-neutral-300 leading-relaxed italic">
                  "{complexityResult.advice}"
                </p>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Click outside to close templates */}
      {showTemplates && (
        <div className="fixed inset-0 z-40" onClick={() => setShowTemplates(false)} />
      )}
    </div>
  );
}
