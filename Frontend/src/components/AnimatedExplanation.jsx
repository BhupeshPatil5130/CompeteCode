import { useState, useEffect, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Play, Pause, SkipForward, SkipBack, Sparkles, X,
  RotateCcw, ChevronDown, Code2, BarChart3, Brain, Layers
} from 'lucide-react';

/* ─────────────────────────────────────────────
   Data-structure visualizers
───────────────────────────────────────────── */

function ArrayVisualizer({ data, activeIndices = [], pointers = {} }) {
  let arr = [];
  try { arr = typeof data === 'string' ? JSON.parse(data) : data; } catch { arr = String(data).split(/[\s,]+/).filter(Boolean); }
  if (!Array.isArray(arr)) arr = [];

  return (
    <div className="flex flex-col items-center gap-2">
      <div className="flex gap-1.5 flex-wrap justify-center">
        {arr.map((item, i) => {
          const isActive = activeIndices.includes(i);
          const ptrLabels = Object.entries(pointers).filter(([, idx]) => idx === i).map(([k]) => k);
          return (
            <div key={i} className="flex flex-col items-center gap-1">
              {ptrLabels.length > 0 && (
                <motion.div initial={{ opacity: 0, y: -6 }} animate={{ opacity: 1, y: 0 }}
                  className="text-[10px] font-mono font-bold text-amber-400 bg-amber-400/10 px-1.5 rounded">
                  {ptrLabels.join(',')}
                </motion.div>
              )}
              <motion.div
                key={`${i}-${item}`}
                initial={{ scale: 0.8, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ type: 'spring', stiffness: 300, delay: i * 0.04 }}
                className={`w-11 h-11 flex items-center justify-center rounded-lg font-mono text-sm font-bold transition-all duration-300 ${
                  isActive
                    ? 'bg-amber-500/30 border-2 border-amber-400 text-amber-200 shadow-lg shadow-amber-500/20'
                    : 'bg-neutral-800/60 border border-neutral-700 text-neutral-300'
                }`}
              >
                {String(item)}
              </motion.div>
              <span className="text-[10px] text-neutral-600 font-mono">{i}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function HashMapVisualizer({ data }) {
  let map = {};
  try { map = typeof data === 'string' ? JSON.parse(data) : data; } catch { map = {}; }
  const entries = Object.entries(map);

  return (
    <div className="flex flex-col gap-1.5 max-w-xs mx-auto">
      <div className="text-[10px] text-neutral-500 uppercase tracking-widest font-mono text-center mb-1">HashMap</div>
      {entries.length === 0 && (
        <div className="text-center text-neutral-600 text-xs font-mono py-3 border border-dashed border-neutral-700 rounded-lg">
          empty
        </div>
      )}
      <AnimatePresence>
        {entries.map(([k, v]) => (
          <motion.div
            key={k}
            initial={{ opacity: 0, x: -12, height: 0 }}
            animate={{ opacity: 1, x: 0, height: 'auto' }}
            exit={{ opacity: 0, x: 12, height: 0 }}
            className="flex items-center gap-2 bg-neutral-800/60 border border-neutral-700 rounded-lg px-3 py-2"
          >
            <span className="font-mono text-xs text-emerald-400 font-bold min-w-[60px]">{String(k)}</span>
            <span className="text-neutral-600">→</span>
            <span className="font-mono text-xs text-white">{String(v)}</span>
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  );
}

function TreeVisualizer({ data }) {
  let nodes = [];
  try { nodes = typeof data === 'string' ? JSON.parse(data) : data; } catch { nodes = String(data).split(/[\s,]+/).filter(Boolean); }
  if (!Array.isArray(nodes)) nodes = [];

  const levels = [];
  let idx = 0, levelSize = 1;
  while (idx < nodes.length) {
    levels.push(nodes.slice(idx, idx + levelSize));
    idx += levelSize; levelSize *= 2;
  }

  return (
    <div className="flex flex-col items-center gap-3">
      {levels.map((level, li) => (
        <div key={li} className="flex justify-center" style={{ gap: `${Math.max(8, 64 / (li + 1))}px` }}>
          {level.map((node, ni) => (
            <motion.div key={`${li}-${ni}`}
              initial={{ scale: 0, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: li * 0.15 + ni * 0.08, type: 'spring' }}
              className={`w-10 h-10 flex items-center justify-center rounded-full font-mono text-xs font-bold ${
                String(node) === 'null' || String(node) === 'N'
                  ? 'border border-dashed border-neutral-700 text-neutral-700'
                  : li === 0
                    ? 'bg-amber-500/20 border-2 border-amber-400/50 text-amber-300'
                    : 'bg-purple-500/15 border border-purple-500/30 text-purple-300'
              }`}
            >
              {String(node) === 'null' || String(node) === 'N' ? '∅' : String(node)}
            </motion.div>
          ))}
        </div>
      ))}
    </div>
  );
}

function StackVisualizer({ data }) {
  let items = [];
  try { items = typeof data === 'string' ? JSON.parse(data) : data; } catch { items = String(data).split(/[\s,]+/).filter(Boolean); }

  return (
    <div className="flex flex-col-reverse items-center gap-0.5 max-w-[160px] mx-auto">
      {items.map((item, i) => (
        <motion.div key={i}
          initial={{ x: -20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ delay: i * 0.1, type: 'spring' }}
          className={`w-36 py-2 text-center rounded-md font-mono text-sm font-bold transition-all ${
            i === items.length - 1
              ? 'bg-amber-500/20 border-2 border-amber-400/40 text-amber-200'
              : 'bg-neutral-800 border border-neutral-700 text-neutral-400'
          }`}
        >
          {String(item)}
          {i === items.length - 1 && <span className="ml-2 text-[10px] text-amber-500">← top</span>}
        </motion.div>
      ))}
      <div className="w-36 border-t-2 border-neutral-600 pt-1 text-center">
        <span className="text-[10px] text-neutral-600 font-mono uppercase tracking-widest">stack</span>
      </div>
    </div>
  );
}

function LinkedListVisualizer({ data }) {
  let nodes = [];
  try { nodes = typeof data === 'string' ? JSON.parse(data) : data; } catch { nodes = String(data).split(/[\s,]+/).filter(Boolean); }

  return (
    <div className="flex items-center gap-1 flex-wrap justify-center">
      {nodes.map((node, i) => (
        <motion.div key={i} initial={{ opacity: 0, x: -10 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.1 }}
          className="flex items-center gap-1">
          <div className="flex items-center gap-0 bg-neutral-800 border border-purple-500/30 rounded-lg overflow-hidden">
            <span className="px-3 py-2 font-mono text-sm font-bold text-purple-300">{String(node)}</span>
            <span className="px-2 py-2 text-xs text-neutral-600 border-l border-neutral-700">→</span>
          </div>
          {i < nodes.length - 1 && <span className="text-neutral-600 text-lg font-bold">→</span>}
        </motion.div>
      ))}
      <span className="text-neutral-600 font-mono text-xs ml-1">null</span>
    </div>
  );
}

function MatrixVisualizer({ data, activeCell }) {
  let matrix = [];
  try {
    const parsed = typeof data === 'string' ? JSON.parse(data) : data;
    matrix = Array.isArray(parsed) ? parsed : [];
  } catch {
    matrix = String(data).split('\n').map(row => row.split(/[\s,]+/).filter(Boolean));
  }

  return (
    <div className="flex flex-col items-center gap-0.5">
      {matrix.map((row, ri) => (
        <div key={ri} className="flex gap-0.5">
          {(Array.isArray(row) ? row : [row]).map((cell, ci) => {
            const isActive = activeCell && activeCell[0] === ri && activeCell[1] === ci;
            return (
              <motion.div key={`${ri}-${ci}`}
                initial={{ scale: 0 }} animate={{ scale: 1 }}
                transition={{ delay: ri * 0.04 + ci * 0.04 }}
                className={`w-9 h-9 flex items-center justify-center rounded text-xs font-mono font-bold transition-all duration-300 ${
                  isActive ? 'bg-amber-400/30 border-2 border-amber-400 text-amber-200' :
                  String(cell) === '1' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' :
                  String(cell) === '0' ? 'bg-neutral-800 text-neutral-600 border border-neutral-700' :
                  'bg-amber-500/10 text-amber-300 border border-amber-500/20'
                }`}
              >
                {String(cell)}
              </motion.div>
            );
          })}
        </div>
      ))}
    </div>
  );
}

function VariableInspector({ variables }) {
  if (!variables || Object.keys(variables).length === 0) return null;
  return (
    <div className="bg-neutral-900/60 border border-neutral-800 rounded-lg p-3">
      <div className="text-[10px] text-neutral-500 uppercase tracking-widest font-mono mb-2">Variables</div>
      <div className="grid grid-cols-2 gap-1.5">
        {Object.entries(variables).map(([k, v]) => (
          <motion.div key={k} layout
            className="flex items-center justify-between bg-neutral-800/80 rounded px-2 py-1 gap-2">
            <span className="font-mono text-xs text-sky-400">{k}</span>
            <motion.span
              key={String(v)}
              initial={{ scale: 1.2, color: '#f59e0b' }}
              animate={{ scale: 1, color: '#e5e7eb' }}
              className="font-mono text-xs font-bold"
            >
              {String(v ?? 'null')}
            </motion.span>
          </motion.div>
        ))}
      </div>
    </div>
  );
}

function CodePanel({ lines = [], activeLine }) {
  const activeRef = useRef(null);
  useEffect(() => {
    activeRef.current?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
  }, [activeLine]);

  if (!lines.length) return null;

  return (
    <div className="bg-neutral-950 border border-neutral-800 rounded-lg overflow-hidden">
      <div className="text-[10px] text-neutral-500 uppercase tracking-widest font-mono px-3 py-2 border-b border-neutral-800 flex items-center gap-2">
        <Code2 className="w-3 h-3" /> Code
      </div>
      <div className="overflow-y-auto max-h-52 font-mono text-xs">
        {lines.map((line) => {
          const isActive = line.line === activeLine;
          return (
            <div key={line.line}
              ref={isActive ? activeRef : null}
              className={`flex items-start gap-3 px-3 py-0.5 transition-all duration-200 ${
                isActive ? 'bg-amber-500/15 border-l-2 border-amber-400' : 'border-l-2 border-transparent'
              }`}
            >
              <span className={`select-none w-5 text-right shrink-0 ${isActive ? 'text-amber-400' : 'text-neutral-700'}`}>
                {line.line}
              </span>
              <span className={isActive ? 'text-white' : 'text-neutral-500'}>{line.text}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function ComplexityPanel({ complexity }) {
  if (!complexity) return null;
  return (
    <div className="bg-neutral-900/60 border border-neutral-800 rounded-lg p-3">
      <div className="text-[10px] text-neutral-500 uppercase tracking-widest font-mono mb-2 flex items-center gap-2">
        <BarChart3 className="w-3 h-3" /> Complexity
      </div>
      <div className="grid grid-cols-2 gap-2">
        <div className="bg-neutral-800/60 rounded-lg p-2 text-center">
          <div className="text-[10px] text-neutral-500 mb-1">Time</div>
          <div className="font-mono text-sm font-bold text-amber-400">{complexity.time || '—'}</div>
        </div>
        <div className="bg-neutral-800/60 rounded-lg p-2 text-center">
          <div className="text-[10px] text-neutral-500 mb-1">Space</div>
          <div className="font-mono text-sm font-bold text-sky-400">{complexity.space || '—'}</div>
        </div>
      </div>
      {complexity.explanation && (
        <p className="text-xs text-neutral-500 mt-2 leading-relaxed">{complexity.explanation}</p>
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────
   Step visual dispatcher
───────────────────────────────────────────── */
function StepVisual({ step }) {
  const { visualType, visualData, state } = step;
  const parsed = (() => { try { return typeof visualData === 'string' ? JSON.parse(visualData) : visualData; } catch { return visualData; } })();

  switch (visualType) {
    case 'array':
    case 'highlight':
      return <ArrayVisualizer data={parsed} activeIndices={state?.activeIndices || []} pointers={state?.pointers || {}} />;
    case 'hashmap':
    case 'hash_map':
    case 'map':
      return <HashMapVisualizer data={state?.dataStructures?.map || parsed || {}} />;
    case 'tree':
      return <TreeVisualizer data={parsed} />;
    case 'stack':
    case 'queue':
      return <StackVisualizer data={parsed} />;
    case 'linkedlist':
    case 'linked_list':
      return <LinkedListVisualizer data={parsed} />;
    case 'matrix':
    case 'grid':
      return <MatrixVisualizer data={parsed} activeCell={state?.activeCell} />;
    case 'text':
    default:
      if (visualData && visualData !== step.description) {
        return (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
            className="bg-neutral-900/60 border border-neutral-700 rounded-lg px-4 py-3 text-center max-w-sm mx-auto">
            <p className="font-mono text-xs text-amber-300 leading-relaxed">{visualData}</p>
          </motion.div>
        );
      }
      return null;
  }
}

/* ─────────────────────────────────────────────
   Main component
───────────────────────────────────────────── */
const SPEEDS = [0.5, 1, 1.5, 2];

export default function AnimatedExplanation({ explanation, onClose }) {
  const [currentStep, setCurrentStep] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [speed, setSpeed] = useState(1);
  const [activeTab, setActiveTab] = useState('visual'); // visual | code | complexity
  const progressRef = useRef(null);

  const steps = explanation?.steps || [];
  const step = steps[currentStep];
  const isFirst = currentStep === 0;
  const isLast = currentStep === steps.length - 1;

  /* Auto-play */
  useEffect(() => {
    if (!playing || !step) return;
    const ms = (step.durationMs || 3000) / speed;
    const timer = setTimeout(() => {
      if (!isLast) setCurrentStep(s => s + 1);
      else setPlaying(false);
    }, ms);
    return () => clearTimeout(timer);
  }, [playing, currentStep, step, isLast, speed]);

  const restart = useCallback(() => { setCurrentStep(0); setPlaying(false); }, []);

  const scrub = useCallback((e) => {
    if (!progressRef.current) return;
    const rect = progressRef.current.getBoundingClientRect();
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    setCurrentStep(Math.round(pct * (steps.length - 1)));
  }, [steps.length]);

  if (!explanation) return null;

  if (!steps.length) {
    return (
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4"
        onClick={onClose}>
        <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-8 text-center" onClick={e => e.stopPropagation()}>
          <p className="text-neutral-400 mb-4 text-sm">No animation steps available for this problem.</p>
          <button onClick={onClose} className="px-4 py-2 rounded-lg border border-neutral-700 text-sm text-neutral-300 hover:bg-neutral-800 transition-colors">Close</button>
        </div>
      </motion.div>
    );
  }

  const defaultFallbackCode = [
    { line: 1, text: `// Execution trace for: ${explanation.problemTitle || 'Algorithm'}` },
    { line: 2, text: 'public void executeStep() {' },
    { line: 3, text: '    // Processing iteration state' },
    { line: 4, text: '    updateDataStructures();' },
    { line: 5, text: '    verifyInvariants();' },
    { line: 6, text: '}' }
  ];

  const codeLinesSource = (step?.codeLines?.length ? step.codeLines : (explanation?.code?.length ? explanation.code : defaultFallbackCode));
  const activeCodeLine = step?.line ?? (currentStep + 1);

  const tabs = [
    { id: 'visual', label: 'Dry Run', icon: Brain },
    { id: 'code', label: 'Code', icon: Code2 },
    ...(explanation?.complexity ? [{ id: 'complexity', label: 'Complexity', icon: BarChart3 }] : []),
  ];

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-3"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.95, y: 20 }}
        animate={{ scale: 1, y: 0 }}
        transition={{ type: 'spring', stiffness: 300, damping: 30 }}
        className="bg-neutral-900 border border-neutral-700 rounded-2xl w-full max-w-3xl max-h-[92vh] overflow-hidden shadow-2xl flex flex-col"
        onClick={e => e.stopPropagation()}
      >
        {/* ── Header ── */}
        <div className="px-5 py-3.5 border-b border-neutral-800 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-purple-500/15 flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-purple-400" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white leading-tight">{explanation.problemTitle}</h2>
              <p className="text-xs text-neutral-500 leading-tight">{explanation.summary}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            {/* Speed selector */}
            <div className="relative group">
              <button className="flex items-center gap-1 px-2 py-1 rounded-md border border-neutral-700 text-xs text-neutral-400 hover:bg-neutral-800 transition-colors">
                {speed}× <ChevronDown className="w-3 h-3" />
              </button>
              <div className="absolute right-0 top-full mt-1 bg-neutral-800 border border-neutral-700 rounded-lg py-1 z-10 hidden group-hover:block">
                {SPEEDS.map(s => (
                  <button key={s} onClick={() => setSpeed(s)}
                    className={`block w-full text-left px-3 py-1 text-xs transition-colors ${speed === s ? 'text-amber-400' : 'text-neutral-300 hover:text-white'}`}>
                    {s}×
                  </button>
                ))}
              </div>
            </div>
            <button onClick={restart} className="p-1.5 rounded-md hover:bg-neutral-800 text-neutral-500 hover:text-neutral-300 transition-colors">
              <RotateCcw className="w-3.5 h-3.5" />
            </button>
            <button onClick={onClose} className="p-1.5 rounded-md hover:bg-neutral-800 text-neutral-500 hover:text-neutral-300 transition-colors">
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* ── Tabs (conditional) ── */}
        {tabs.length > 1 && (
          <div className="flex border-b border-neutral-800 shrink-0">
            {tabs.map(({ id, label, icon: Icon }) => (
              <button key={id} onClick={() => setActiveTab(id)}
                className={`flex items-center gap-1.5 px-4 py-2 text-xs font-medium transition-colors border-b-2 -mb-px ${
                  activeTab === id
                    ? 'border-amber-400 text-amber-400 bg-amber-400/5'
                    : 'border-transparent text-neutral-500 hover:text-neutral-300'
                }`}>
                <Icon className="w-3 h-3" />
                {label}
              </button>
            ))}
          </div>
        )}

        {/* ── Main Content ── */}
        <div className="flex-1 overflow-y-auto p-5 space-y-4">
          <AnimatePresence mode="wait">
            {step && (
              <motion.div key={currentStep}
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.18 }}
                className="space-y-4"
              >
                {/* Step header */}
                <div className="text-center">
                  <span className="text-[10px] font-mono text-neutral-600 uppercase tracking-widest">
                    Step {step.step} / {steps.length}
                  </span>
                  <h3 className="text-lg font-bold text-white mt-0.5">{step.title}</h3>
                  <p className="text-sm text-neutral-400 mt-1 max-w-lg mx-auto leading-relaxed">{step.description}</p>
                </div>

                {activeTab === 'visual' && (
                  <>
                    {/* Data structure visualization */}
                    <div className="bg-neutral-950/60 border border-neutral-800 rounded-xl p-6 flex items-center justify-center min-h-[120px]">
                      <StepVisual step={step} />
                    </div>

                    {/* HashMap if present in state */}
                    {step.state?.dataStructures?.map && (
                      <HashMapVisualizer data={step.state.dataStructures.map} />
                    )}

                    {/* Variable inspector */}
                    {step.state?.variables && Object.keys(step.state.variables).length > 0 && (
                      <VariableInspector variables={step.state.variables} />
                    )}

                    {/* Operation badge */}
                    {step.operations?.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 justify-center">
                        {step.operations.map((op, i) => (
                          <span key={i} className="text-[10px] font-mono bg-sky-500/10 border border-sky-500/20 text-sky-400 px-2 py-1 rounded">
                            {op.type}: {op.target}{op.value !== undefined ? ` = ${JSON.stringify(op.value)}` : ''}
                          </span>
                        ))}
                      </div>
                    )}
                  </>
                )}

                {activeTab === 'code' && (
                  <CodePanel lines={codeLinesSource} activeLine={activeCodeLine} />
                )}

                {activeTab === 'complexity' && (
                  <ComplexityPanel complexity={explanation.complexity} />
                )}
              </motion.div>
            )}
          </AnimatePresence>

          {/* Final result */}
          {isLast && explanation.result && (
            <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
              className="bg-emerald-500/10 border border-emerald-500/20 rounded-xl p-4 text-center">
              <div className="text-[10px] text-emerald-500 uppercase tracking-widest font-mono mb-1">Result</div>
              <div className="text-xl font-bold text-emerald-400 font-mono">
                {JSON.stringify(explanation.result.value ?? explanation.result)}
              </div>
              {explanation.result.explanation && (
                <p className="text-xs text-emerald-300/70 mt-1">{explanation.result.explanation}</p>
              )}
            </motion.div>
          )}
        </div>

        {/* ── Controls ── */}
        <div className="px-5 pb-4 pt-3 border-t border-neutral-800 space-y-3 shrink-0">
          {/* Scrub bar */}
          <div ref={progressRef} onClick={scrub}
            className="w-full h-2 bg-neutral-800 rounded-full cursor-pointer relative group">
            <motion.div
              className="h-full bg-amber-400 rounded-full"
              animate={{ width: `${steps.length > 1 ? (currentStep / (steps.length - 1)) * 100 : 100}%` }}
              transition={{ duration: 0.15 }}
            />
            <div className="absolute top-1/2 -translate-y-1/2 w-3.5 h-3.5 bg-amber-400 rounded-full shadow-md opacity-0 group-hover:opacity-100 transition-opacity"
              style={{ left: `${steps.length > 1 ? (currentStep / (steps.length - 1)) * 100 : 100}%`, transform: 'translate(-50%, -50%)' }} />
          </div>

          {/* Buttons row */}
          <div className="flex items-center justify-between">
            {/* Step dots (up to 20) */}
            <div className="flex gap-1 flex-wrap max-w-[200px]">
              {steps.slice(0, 20).map((_, i) => (
                <button key={i} onClick={() => { setCurrentStep(i); setPlaying(false); }}
                  className={`h-1.5 rounded-full transition-all duration-200 ${
                    i === currentStep ? 'w-5 bg-amber-400' : i < currentStep ? 'w-2 bg-amber-600/60' : 'w-2 bg-neutral-700'
                  }`}
                />
              ))}
              {steps.length > 20 && <span className="text-[10px] text-neutral-600 ml-1">+{steps.length - 20}</span>}
            </div>

            {/* Navigation buttons */}
            <div className="flex items-center gap-2">
              <button onClick={() => { setCurrentStep(s => Math.max(s - 1, 0)); setPlaying(false); }}
                disabled={isFirst}
                className="p-1.5 rounded-md border border-neutral-700 text-neutral-400 hover:text-white hover:bg-neutral-800 transition-colors disabled:opacity-30">
                <SkipBack className="w-3.5 h-3.5" />
              </button>
              <button onClick={() => setPlaying(p => !p)}
                className="flex items-center gap-1.5 px-4 py-1.5 rounded-lg bg-amber-500 hover:bg-amber-400 text-black text-xs font-bold transition-colors">
                {playing ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
                {playing ? 'Pause' : 'Play'}
              </button>
              <button onClick={() => { setCurrentStep(s => Math.min(s + 1, steps.length - 1)); setPlaying(false); }}
                disabled={isLast}
                className="p-1.5 rounded-md border border-neutral-700 text-neutral-400 hover:text-white hover:bg-neutral-800 transition-colors disabled:opacity-30">
                <SkipForward className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
}
