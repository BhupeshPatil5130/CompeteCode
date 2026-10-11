import { useState, useEffect, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Play, Pause, SkipForward, SkipBack, Sparkles, X,
  RotateCcw, ChevronDown, Code2, Database, Variable,
  Maximize2, ArrowRight, CheckCircle2
} from 'lucide-react';

/* ─────────────────────────────────────────────
   HashMap visualizer & parser
───────────────────────────────────────────── */
function parseMapString(mapStr) {
  if (!mapStr) return [];
  if (typeof mapStr === 'object') return Object.entries(mapStr);
  const clean = String(mapStr).trim().replace(/^\{|\}$/g, '').trim();
  if (!clean) return [];
  return clean.split(/,\s*/).map(pair => {
    const parts = pair.split(/[:=]/).map(s => s.trim().replace(/^['"]|['"]$/g, ''));
    return parts.length === 2 ? parts : [parts[0], ''];
  }).filter(([k]) => Boolean(k));
}

function HashMapVisual({ mapData }) {
  const entries = parseMapString(mapData);
  return (
    <div className="bg-neutral-900/90 border border-neutral-800 rounded-xl p-3.5 flex flex-col gap-2 min-w-[220px]">
      <div className="flex items-center gap-1.5 text-xs font-mono font-bold text-emerald-400 uppercase tracking-wider">
        <Database className="w-3.5 h-3.5" />
        <span>Hash Map (Key → Index)</span>
      </div>
      {entries.length === 0 ? (
        <div className="text-center py-4 border border-dashed border-neutral-800 rounded-lg text-neutral-600 font-mono text-xs">
          empty map {}
        </div>
      ) : (
        <div className="flex flex-wrap gap-2 max-h-36 overflow-y-auto pr-1">
          <AnimatePresence>
            {entries.map(([k, v], idx) => (
              <motion.div
                key={`${k}-${v}-${idx}`}
                initial={{ scale: 0.8, opacity: 0, y: -6 }}
                animate={{ scale: 1, opacity: 1, y: 0 }}
                exit={{ scale: 0.8, opacity: 0 }}
                className="flex items-center gap-2 bg-neutral-950 border border-emerald-500/30 px-3 py-1.5 rounded-lg shadow-sm"
              >
                <span className="font-mono text-xs font-bold text-emerald-300">{k}</span>
                <span className="text-neutral-600 text-xs">→</span>
                <span className="font-mono text-xs font-semibold text-white">{v}</span>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────
   Data structure visualizers
───────────────────────────────────────────── */

function ArrayVisualizer({ data, highlights = [], pointers = {} }) {
  let arr = [];
  try {
    arr = typeof data === 'string' ? JSON.parse(data) : data;
  } catch {
    arr = String(data).split(/[\s,]+/).filter(Boolean);
  }
  if (!Array.isArray(arr)) arr = [];

  const hl = Array.isArray(highlights) ? highlights : [];
  const ptrs = (pointers && typeof pointers === 'object') ? pointers : {};

  return (
    <div className="flex flex-col items-center gap-3 w-full py-4">
      <div className="flex gap-2.5 sm:gap-3 flex-wrap justify-center items-end min-h-[100px]">
        {arr.map((item, i) => {
          const isHighlighted = hl.includes(i);
          const activePointers = Object.entries(ptrs).filter(([, idx]) => Number(idx) === i).map(([k]) => k);

          return (
            <div key={i} className="flex flex-col items-center gap-1.5">
              {/* Pointer labels */}
              <div className="h-6 flex items-center justify-center">
                {activePointers.length > 0 && (
                  <motion.div
                    initial={{ y: -8, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    className="flex items-center gap-1 bg-amber-400 text-black px-2 py-0.5 rounded text-[11px] font-mono font-black shadow-md shadow-amber-400/20"
                  >
                    <span>{activePointers.join(', ')}</span>
                    <span className="text-[9px]">↓</span>
                  </motion.div>
                )}
              </div>

              {/* Cell */}
              <motion.div
                key={`${i}-${item}`}
                initial={{ scale: 0.85, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ type: 'spring', stiffness: 350, damping: 25 }}
                className={`w-14 h-14 sm:w-16 sm:h-16 flex items-center justify-center rounded-2xl font-mono text-lg font-black transition-all duration-300 select-none ${
                  isHighlighted
                    ? 'bg-amber-400/20 border-2 border-amber-400 text-amber-200 shadow-xl shadow-amber-400/25 ring-2 ring-amber-400/30'
                    : 'bg-neutral-800/90 border border-neutral-700/80 text-white shadow-sm'
                }`}
              >
                {String(item)}
              </motion.div>

              {/* 0-based Index */}
              <span className="text-xs text-neutral-500 font-mono font-medium">[{i}]</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function BarsVisualizer({ data, highlights = [], pointers = {} }) {
  let arr = [];
  try { arr = typeof data === 'string' ? JSON.parse(data) : data; } catch { arr = []; }
  if (!Array.isArray(arr)) arr = [];
  const maxVal = Math.max(...arr.map(Number), 1);

  const hl = Array.isArray(highlights) ? highlights : [];
  const ptrs = (pointers && typeof pointers === 'object') ? pointers : {};

  return (
    <div className="flex items-end justify-center gap-3 h-44 w-full py-2">
      {arr.map((val, i) => {
        const heightPct = Math.max(12, (Number(val) / maxVal) * 100);
        const isHighlighted = hl.includes(i);
        const activePointers = Object.entries(ptrs).filter(([, idx]) => Number(idx) === i).map(([k]) => k);

        return (
          <div key={i} className="flex flex-col items-center gap-1.5 h-full justify-end">
            {activePointers.length > 0 && (
              <span className="bg-amber-400 text-black text-[10px] font-mono font-bold px-1.5 rounded mb-1">
                {activePointers.join(', ')}
              </span>
            )}
            <motion.div
              style={{ height: `${heightPct}%` }}
              animate={{ height: `${heightPct}%` }}
              className={`w-9 rounded-t-lg flex items-center justify-center text-xs font-mono font-bold transition-all ${
                isHighlighted ? 'bg-amber-400 text-black shadow-lg shadow-amber-400/30' : 'bg-neutral-700 text-neutral-300'
              }`}
            >
              {val}
            </motion.div>
            <span className="text-[10px] text-neutral-500 font-mono">[{i}]</span>
          </div>
        );
      })}
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
    <div className="flex flex-col items-center gap-3 py-3 w-full">
      {levels.map((level, li) => (
        <div key={li} className="flex justify-center" style={{ gap: `${Math.max(10, 80 / (li + 1))}px` }}>
          {level.map((node, ni) => (
            <motion.div
              key={`${li}-${ni}`}
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              className={`w-11 h-11 flex items-center justify-center rounded-full font-mono text-xs font-bold ${
                String(node) === 'null' || String(node) === 'N'
                  ? 'border border-dashed border-neutral-700 text-neutral-700'
                  : li === 0
                    ? 'bg-amber-400/20 border-2 border-amber-400 text-amber-200 shadow-md'
                    : 'bg-purple-500/20 border border-purple-500/40 text-purple-200'
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

function MatrixVisualizer({ data }) {
  let matrix = [];
  try {
    const parsed = typeof data === 'string' ? JSON.parse(data) : data;
    matrix = Array.isArray(parsed) ? parsed : [];
  } catch {
    matrix = [];
  }

  return (
    <div className="flex flex-col items-center gap-1 py-2">
      {matrix.map((row, ri) => (
        <div key={ri} className="flex gap-1">
          {(Array.isArray(row) ? row : [row]).map((cell, ci) => (
            <div
              key={`${ri}-${ci}`}
              className="w-10 h-10 flex items-center justify-center rounded-lg bg-neutral-800 border border-neutral-700 text-xs font-mono font-bold text-white"
            >
              {String(cell)}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}

function LinkedListVisualizer({ data }) {
  let nodes = [];
  try { nodes = typeof data === 'string' ? JSON.parse(data) : data; } catch { nodes = String(data).split(/[\s,]+/).filter(Boolean); }

  return (
    <div className="flex items-center gap-2 flex-wrap justify-center py-3">
      {nodes.map((node, i) => (
        <div key={i} className="flex items-center gap-2">
          <div className="px-3.5 py-2 rounded-xl bg-purple-500/15 border border-purple-500/30 font-mono text-sm font-bold text-purple-300">
            {String(node)}
          </div>
          {i < nodes.length - 1 && <span className="text-neutral-500 text-lg font-bold">→</span>}
        </div>
      ))}
      <span className="text-neutral-600 font-mono text-xs">null</span>
    </div>
  );
}

function StackVisualizer({ data }) {
  let items = [];
  try { items = typeof data === 'string' ? JSON.parse(data) : data; } catch { items = String(data).split(/[\s,]+/).filter(Boolean); }

  return (
    <div className="flex flex-col-reverse items-center gap-1.5 max-w-[200px] mx-auto py-2">
      {items.map((item, i) => (
        <div
          key={i}
          className={`w-36 py-2 text-center rounded-lg font-mono text-sm font-bold ${
            i === items.length - 1
              ? 'bg-amber-400/20 border border-amber-400 text-amber-200'
              : 'bg-neutral-800 border border-neutral-700 text-neutral-300'
          }`}
        >
          {String(item)}
        </div>
      ))}
      <div className="w-36 border-t-2 border-neutral-600 pt-1 text-center font-mono text-[10px] text-neutral-500 uppercase tracking-widest">
        Stack
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Step visual dispatcher
───────────────────────────────────────────── */
function StepCanvas({ step }) {
  const { visualType, visualData, highlights, pointers } = step;

  switch (visualType) {
    case 'bars':
      return <BarsVisualizer data={visualData} highlights={highlights} pointers={pointers} />;
    case 'tree':
      return <TreeVisualizer data={visualData} />;
    case 'matrix':
      return <MatrixVisualizer data={visualData} />;
    case 'linkedlist':
      return <LinkedListVisualizer data={visualData} />;
    case 'stack':
      return <StackVisualizer data={visualData} />;
    case 'pointer':
      return (
        <div className="flex flex-col items-center gap-2 py-4">
          <span className="text-3xl">👆</span>
          <span className="font-mono text-base font-bold text-amber-400">{visualData}</span>
        </div>
      );
    case 'text':
      return (
        <div className="p-4 bg-neutral-900 border border-neutral-800 rounded-xl text-center max-w-md mx-auto my-3">
          <p className="font-mono text-sm text-amber-300">{visualData}</p>
        </div>
      );
    case 'array':
    case 'two-pointer':
    default:
      return <ArrayVisualizer data={visualData} highlights={highlights} pointers={pointers} />;
  }
}

/* ─────────────────────────────────────────────
   Live Code panel with active line highlighting
───────────────────────────────────────────── */
function LiveCodePanel({ code = [], activeLines = [] }) {
  const activeRef = useRef(null);

  useEffect(() => {
    activeRef.current?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
  }, [activeLines]);

  if (!code || code.length === 0) return null;

  return (
    <div className="bg-neutral-950 border border-neutral-800 rounded-xl overflow-hidden flex flex-col h-full shadow-inner">
      <div className="px-3.5 py-2.5 border-b border-neutral-800/80 bg-neutral-900/60 flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs font-mono font-bold text-sky-400">
          <Code2 className="w-3.5 h-3.5" />
          <span>Algorithm Code</span>
        </div>
        <span className="text-[10px] text-neutral-500 font-mono">Real-time Exec</span>
      </div>
      <div className="overflow-y-auto font-mono text-xs p-2.5 flex-1 space-y-0.5">
        {code.map((lineText, idx) => {
          const lineNum = idx + 1;
          const isActive = activeLines && activeLines.includes(lineNum);
          return (
            <div
              key={lineNum}
              ref={isActive ? activeRef : null}
              className={`flex items-start gap-2.5 px-2 py-1 rounded transition-colors duration-150 ${
                isActive
                  ? 'bg-amber-400/20 text-amber-200 border-l-2 border-amber-400 font-bold'
                  : 'text-neutral-400 hover:text-neutral-200'
              }`}
            >
              <span className={`select-none w-5 text-right shrink-0 ${isActive ? 'text-amber-400' : 'text-neutral-600'}`}>
                {lineNum}
              </span>
              {isActive && <span className="text-amber-400 select-none">▶</span>}
              <span className="whitespace-pre-wrap">{lineText}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Main Big Screen Visualizer
───────────────────────────────────────────── */
const SPEEDS = [0.5, 1, 1.5, 2];

export default function AnimatedExplanation({ explanation, onClose }) {
  const [currentStep, setCurrentStep] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [speed, setSpeed] = useState(1);
  const progressRef = useRef(null);

  const steps = explanation?.steps || [];
  const step = steps[currentStep];
  const isFirst = currentStep === 0;
  const isLast = currentStep === steps.length - 1;

  // Extract code (either from step, or from step 1, or fallback)
  const codeLinesSource = step?.code?.length
    ? step.code
    : steps[0]?.code?.length
      ? steps[0].code
      : explanation?.code?.map(c => typeof c === 'string' ? c : c.text) || [];

  const activeLines = step?.codeLines || (step?.line ? [step.line] : []);

  // Check if there is HashMap data in vars or state
  const hasMapVar = step?.vars?.map || step?.state?.dataStructures?.map || null;

  /* Auto-play loop */
  useEffect(() => {
    if (!playing || !step) return;
    const ms = (step.durationMs || 3000) / speed;
    const timer = setTimeout(() => {
      if (!isLast) setCurrentStep(s => s + 1);
      else setPlaying(false);
    }, ms);
    return () => clearTimeout(timer);
  }, [playing, currentStep, step, isLast, speed]);

  const restart = useCallback(() => {
    setCurrentStep(0);
    setPlaying(false);
  }, []);

  const scrub = useCallback((e) => {
    if (!progressRef.current || steps.length <= 1) return;
    const rect = progressRef.current.getBoundingClientRect();
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    setCurrentStep(Math.round(pct * (steps.length - 1)));
  }, [steps.length]);

  if (!explanation) return null;

  if (!steps.length) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4" onClick={onClose}>
        <div className="bg-neutral-900 border border-neutral-700 rounded-2xl p-8 text-center max-w-md w-full" onClick={e => e.stopPropagation()}>
          <p className="text-neutral-400 mb-5 text-sm">No animation trace steps available for this problem.</p>
          <button onClick={onClose} className="px-5 py-2 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-sm font-semibold text-white transition-colors">
            Close
          </button>
        </div>
      </div>
    );
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-2 sm:p-4"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.96, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.96, opacity: 0 }}
        transition={{ type: 'spring', stiffness: 320, damping: 28 }}
        className="bg-neutral-900/95 border border-neutral-700/80 rounded-2xl w-[98vw] max-w-7xl h-[94vh] shadow-2xl flex flex-col overflow-hidden text-neutral-100"
        onClick={e => e.stopPropagation()}
      >
        {/* ── Top Header ── */}
        <div className="px-5 py-3.5 border-b border-neutral-800 bg-neutral-950/60 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-amber-400/15 flex items-center justify-center border border-amber-400/30">
              <Sparkles className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-black text-white tracking-tight">{explanation.problemTitle}</h2>
                {explanation.exampleInput && (
                  <span className="hidden md:inline-block bg-neutral-800 border border-neutral-700 px-2 py-0.5 rounded text-[11px] font-mono text-neutral-400">
                    Input: {explanation.exampleInput}
                  </span>
                )}
              </div>
              <p className="text-xs text-neutral-400 leading-tight mt-0.5">{explanation.summary}</p>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            {/* Speed selection */}
            <div className="relative group">
              <button className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-neutral-700 bg-neutral-800/80 text-xs font-mono font-bold text-neutral-300 hover:text-white transition-colors">
                {speed}× <ChevronDown className="w-3.5 h-3.5 text-neutral-400" />
              </button>
              <div className="absolute right-0 top-full mt-1 bg-neutral-800 border border-neutral-700 rounded-xl py-1 z-30 hidden group-hover:block shadow-2xl">
                {SPEEDS.map(s => (
                  <button
                    key={s}
                    onClick={() => setSpeed(s)}
                    className={`block w-full text-left px-4 py-1.5 text-xs font-mono transition-colors ${
                      speed === s ? 'text-amber-400 font-bold bg-amber-400/10' : 'text-neutral-300 hover:bg-neutral-700'
                    }`}
                  >
                    {s}× Speed
                  </button>
                ))}
              </div>
            </div>

            <button onClick={restart} title="Restart from Step 1" className="p-2 rounded-lg hover:bg-neutral-800 text-neutral-400 hover:text-white transition-colors">
              <RotateCcw className="w-4 h-4" />
            </button>
            <button onClick={onClose} title="Close Visualizer" className="p-2 rounded-lg hover:bg-neutral-800 text-neutral-400 hover:text-white transition-colors">
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* ── Main Workspace: Side-by-Side (Visualizer + Code + Vars) ── */}
        <div className="flex-1 min-h-0 grid grid-cols-1 lg:grid-cols-12 gap-4 p-4 overflow-y-auto">
          {/* LEFT/CENTER: Big Canvas & Data Structures (8 cols) */}
          <div className="lg:col-span-8 flex flex-col gap-3 min-h-0">
            {/* Step Banner */}
            <div className="bg-neutral-950/70 border border-neutral-800 rounded-xl px-4 py-3 shrink-0">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono font-bold text-amber-400 uppercase tracking-widest bg-amber-400/10 px-2 py-0.5 rounded">
                  Step {step.step} of {steps.length}
                </span>
                {explanation.exampleOutput && (
                  <span className="text-[11px] font-mono text-neutral-500">
                    Target Output: <strong className="text-emerald-400">{explanation.exampleOutput}</strong>
                  </span>
                )}
              </div>
              <h3 className="text-lg font-black text-white mt-1">{step.title}</h3>
              <p className="text-sm text-neutral-300 mt-0.5 leading-relaxed">{step.description}</p>
            </div>

            {/* Big Animation Canvas */}
            <div className="bg-neutral-950 border border-neutral-800/90 rounded-2xl p-4 flex-1 flex flex-col items-center justify-center min-h-[220px] shadow-inner relative overflow-hidden">
              <div className="absolute top-2.5 left-3 text-[10px] font-mono uppercase text-neutral-600 tracking-wider">
                Interactive State View ({step.visualType})
              </div>
              <AnimatePresence mode="wait">
                <motion.div
                  key={currentStep}
                  initial={{ opacity: 0, scale: 0.97 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.97 }}
                  transition={{ duration: 0.18 }}
                  className="w-full flex items-center justify-center"
                >
                  <StepCanvas step={step} />
                </motion.div>
              </AnimatePresence>
            </div>

            {/* Bottom Row: HashMap Visualizer + Variables Watch (Both visible simultaneously!) */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 shrink-0">
              {/* HashMap (Always rendered when map data exists) */}
              {hasMapVar ? (
                <HashMapVisual mapData={hasMapVar} />
              ) : (
                <div className="bg-neutral-900/60 border border-neutral-800/70 rounded-xl p-3 flex flex-col justify-center">
                  <div className="flex items-center gap-1.5 text-xs font-mono font-bold text-neutral-500 uppercase tracking-wider mb-1">
                    <Database className="w-3.5 h-3.5" />
                    <span>Memory / Auxiliary</span>
                  </div>
                  <span className="text-xs text-neutral-500 font-mono">Constant O(1) space maintained</span>
                </div>
              )}

              {/* Variables Panel */}
              <div className="bg-neutral-900/90 border border-neutral-800 rounded-xl p-3.5 flex flex-col gap-2">
                <div className="flex items-center gap-1.5 text-xs font-mono font-bold text-sky-400 uppercase tracking-wider">
                  <Variable className="w-3.5 h-3.5" />
                  <span>Variables Watch</span>
                </div>
                <div className="flex flex-wrap gap-2 max-h-36 overflow-y-auto pr-1">
                  {step.vars && Object.entries(step.vars).filter(([k]) => k !== 'map').length > 0 ? (
                    Object.entries(step.vars).filter(([k]) => k !== 'map').map(([k, v]) => (
                      <div key={k} className="flex items-center gap-1.5 bg-neutral-950 border border-neutral-800 px-2.5 py-1 rounded-lg">
                        <span className="font-mono text-xs text-sky-300 font-bold">{k}:</span>
                        <motion.span
                          key={String(v)}
                          initial={{ scale: 1.15, color: '#f59e0b' }}
                          animate={{ scale: 1, color: '#ffffff' }}
                          className="font-mono text-xs font-black"
                        >
                          {String(v)}
                        </motion.span>
                      </div>
                    ))
                  ) : (
                    <span className="text-xs text-neutral-500 font-mono">No local vars at this step</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* RIGHT: Live Code Panel with Active Line Highlighting (4 cols) */}
          <div className="lg:col-span-4 flex flex-col min-h-0">
            {codeLinesSource.length > 0 ? (
              <LiveCodePanel code={codeLinesSource} activeLines={activeLines} />
            ) : (
              <div className="bg-neutral-950 border border-neutral-800 rounded-xl p-6 text-center text-neutral-500 font-mono text-xs flex items-center justify-center h-full">
                No code lines generated for this step
              </div>
            )}
          </div>
        </div>

        {/* ── Bottom Controls: Timeline & Playback ── */}
        <div className="px-6 py-4 border-t border-neutral-800 bg-neutral-950/80 space-y-3 shrink-0">
          {/* Progress timeline scrub bar */}
          <div
            ref={progressRef}
            onClick={scrub}
            className="w-full h-3 bg-neutral-800/90 rounded-full cursor-pointer relative group"
          >
            <motion.div
              className="h-full bg-amber-400 rounded-full"
              animate={{ width: `${steps.length > 1 ? (currentStep / (steps.length - 1)) * 100 : 100}%` }}
              transition={{ duration: 0.15 }}
            />
            <div
              className="absolute top-1/2 -translate-y-1/2 w-4 h-4 bg-amber-400 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-opacity ring-2 ring-white"
              style={{
                left: `${steps.length > 1 ? (currentStep / (steps.length - 1)) * 100 : 100}%`,
                transform: 'translate(-50%, -50%)',
              }}
            />
          </div>

          {/* Bottom Bar Controls */}
          <div className="flex items-center justify-between">
            {/* Step navigation dots */}
            <div className="flex gap-1.5 flex-wrap max-w-xs items-center">
              {steps.map((_, i) => (
                <button
                  key={i}
                  onClick={() => { setCurrentStep(i); setPlaying(false); }}
                  title={`Jump to Step ${i + 1}`}
                  className={`h-2 rounded-full transition-all duration-200 cursor-pointer ${
                    i === currentStep
                      ? 'w-7 bg-amber-400'
                      : i < currentStep
                        ? 'w-3 bg-amber-600/70 hover:bg-amber-500'
                        : 'w-3 bg-neutral-700 hover:bg-neutral-600'
                  }`}
                />
              ))}
            </div>

            {/* Playback Buttons */}
            <div className="flex items-center gap-2.5">
              <button
                onClick={() => { setCurrentStep(s => Math.max(s - 1, 0)); setPlaying(false); }}
                disabled={isFirst}
                className="flex items-center gap-1 px-3.5 py-1.5 rounded-xl border border-neutral-700 bg-neutral-800/80 text-neutral-300 hover:text-white hover:bg-neutral-700 transition-colors disabled:opacity-30 disabled:pointer-events-none text-xs font-semibold"
              >
                <SkipBack className="w-3.5 h-3.5" /> Prev
              </button>

              <button
                onClick={() => setPlaying(p => !p)}
                className="flex items-center gap-1.5 px-5 py-2 rounded-xl bg-amber-400 hover:bg-amber-300 text-black text-xs font-black transition-all shadow-lg shadow-amber-400/20 active:scale-95"
              >
                {playing ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4 fill-black" />}
                <span>{playing ? 'Pause' : 'Play'}</span>
              </button>

              <button
                onClick={() => { setCurrentStep(s => Math.min(s + 1, steps.length - 1)); setPlaying(false); }}
                disabled={isLast}
                className="flex items-center gap-1 px-3.5 py-1.5 rounded-xl border border-neutral-700 bg-neutral-800/80 text-neutral-300 hover:text-white hover:bg-neutral-700 transition-colors disabled:opacity-30 disabled:pointer-events-none text-xs font-semibold"
              >
                Next <SkipForward className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
