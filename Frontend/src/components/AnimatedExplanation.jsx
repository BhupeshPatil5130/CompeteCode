import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Play, Pause, SkipForward, Sparkles, X, SkipBack } from 'lucide-react';

function ArrayVisual({ data }) {
  let arr;
  try {
    arr = JSON.parse(data);
  } catch {
    arr = data.split(/[\s,]+/).filter(Boolean);
  }

  return (
    <div className="flex gap-1.5 flex-wrap justify-center">
      {arr.map((item, i) => (
        <motion.div
          key={i}
          initial={{ scale: 0, y: 16 }}
          animate={{ scale: 1, y: 0 }}
          transition={{ delay: i * 0.08, type: 'spring', stiffness: 300 }}
          className="w-12 h-12 flex items-center justify-center rounded-lg bg-amber-500/10 border border-amber-500/20 font-mono text-sm font-bold text-amber-300"
        >
          {String(item)}
        </motion.div>
      ))}
    </div>
  );
}

function TreeVisual({ data }) {
  let nodes;
  try { nodes = JSON.parse(data); } catch { nodes = data.split(/[\s,]+/).filter(Boolean); }

  // Simple binary tree layout
  const levels = [];
  let idx = 0;
  let levelSize = 1;
  while (idx < nodes.length) {
    levels.push(nodes.slice(idx, idx + levelSize));
    idx += levelSize;
    levelSize *= 2;
  }

  return (
    <div className="flex flex-col items-center gap-3">
      {levels.map((level, li) => (
        <div key={li} className="flex justify-center gap-2" style={{ gap: `${Math.max(8, 48 / (li + 1))}px` }}>
          {level.map((node, ni) => (
            <motion.div
              key={`${li}-${ni}`}
              initial={{ scale: 0, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: (li * 0.2) + (ni * 0.1), type: 'spring' }}
              className={`w-10 h-10 flex items-center justify-center rounded-full font-mono text-xs font-bold ${
                node === 'null' ? 'border border-dashed border-neutral-700 text-neutral-700' :
                li === 0 ? 'bg-amber-500/20 border border-amber-500/30 text-amber-300' :
                'bg-purple-500/10 border border-purple-500/20 text-purple-300'
              }`}
            >
              {node === 'null' ? '∅' : String(node)}
            </motion.div>
          ))}
        </div>
      ))}
    </div>
  );
}

function MatrixVisual({ data }) {
  let matrix;
  try { matrix = JSON.parse(data); } catch {
    matrix = data.split('\n').map(row => row.split(/[\s,]+/).filter(Boolean));
  }

  return (
    <div className="flex flex-col items-center gap-0.5">
      {matrix.map((row, ri) => (
        <div key={ri} className="flex gap-0.5">
          {(Array.isArray(row) ? row : [row]).map((cell, ci) => (
            <motion.div
              key={`${ri}-${ci}`}
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: (ri * 0.05) + (ci * 0.05) }}
              className={`w-9 h-9 flex items-center justify-center rounded text-xs font-mono font-bold ${
                String(cell) === '1' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/20' :
                String(cell) === '0' ? 'bg-surface-tertiary text-neutral-600 border border-border' :
                'bg-amber-500/10 text-amber-300 border border-amber-500/20'
              }`}
            >
              {String(cell)}
            </motion.div>
          ))}
        </div>
      ))}
    </div>
  );
}

function StackVisual({ data }) {
  let items = [];
  try { items = JSON.parse(data); } catch { items = data.split(/[\s,]+/).filter(Boolean); }

  return (
    <div className="flex flex-col-reverse items-center gap-1">
      {items.map((item, i) => (
        <motion.div
          key={i}
          initial={{ x: -30, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ delay: i * 0.15, type: 'spring' }}
          className={`w-28 py-2 text-center rounded-md font-mono text-sm font-bold ${
            i === items.length - 1
              ? 'bg-amber-500/15 border border-amber-500/30 text-amber-300'
              : 'bg-surface-tertiary border border-border text-neutral-400'
          }`}
        >
          {String(item)}
        </motion.div>
      ))}
      <div className="w-28 border-t-2 border-neutral-700 mt-1 text-center">
        <span className="text-[10px] text-neutral-600 uppercase tracking-wider">stack</span>
      </div>
    </div>
  );
}

function LinkedListVisual({ data }) {
  let nodes = [];
  try { nodes = JSON.parse(data); } catch { nodes = data.split(/[\s,]+/).filter(Boolean); }

  return (
    <div className="flex items-center gap-1 flex-wrap justify-center">
      {nodes.map((node, i) => (
        <motion.div
          key={i}
          initial={{ opacity: 0, x: -12 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: i * 0.12 }}
          className="flex items-center gap-1"
        >
          <div className="px-3 py-2 rounded-md bg-purple-500/10 border border-purple-500/20 font-mono text-sm font-bold text-purple-300">
            {String(node)}
          </div>
          {i < nodes.length - 1 && (
            <span className="text-neutral-600 text-lg">→</span>
          )}
        </motion.div>
      ))}
      <span className="text-neutral-700 text-xs ml-1">null</span>
    </div>
  );
}

function PointerVisual({ data }) {
  return (
    <div className="flex flex-col items-center gap-3">
      <motion.div
        animate={{ y: [0, -8, 0] }}
        transition={{ repeat: Infinity, duration: 1.5 }}
        className="text-3xl"
      >
        👆
      </motion.div>
      <p className="text-amber-400 font-mono text-sm">{data}</p>
    </div>
  );
}

function TextVisual({ data }) {
  return (
    <motion.p
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="text-base text-center text-neutral-300 leading-relaxed max-w-md mx-auto"
    >
      {data}
    </motion.p>
  );
}

function StepVisual({ step }) {
  switch (step.visualType) {
    case 'array':
    case 'highlight':
      return <ArrayVisual data={step.visualData} />;
    case 'tree':
      return <TreeVisual data={step.visualData} />;
    case 'matrix':
    case 'grid':
      return <MatrixVisual data={step.visualData} />;
    case 'stack':
      return <StackVisual data={step.visualData} />;
    case 'linkedlist':
    case 'linked_list':
      return <LinkedListVisual data={step.visualData} />;
    case 'pointer':
      return <PointerVisual data={step.visualData} />;
    case 'text':
    default:
      if (step.visualData && step.visualData !== step.description) {
        return <TextVisual data={step.visualData} />;
      }
      return null;
  }
}

export default function AnimatedExplanation({ explanation, onClose }) {
  const [currentStep, setCurrentStep] = useState(0);
  const [playing, setPlaying] = useState(false);

  const steps = explanation?.steps || [];
  const step = steps[currentStep];

  useEffect(() => {
    if (!playing || !step) return;
    const timer = setTimeout(() => {
      if (currentStep < steps.length - 1) {
        setCurrentStep((s) => s + 1);
      } else {
        setPlaying(false);
      }
    }, step.durationMs || 3000);
    return () => clearTimeout(timer);
  }, [playing, currentStep, step, steps.length]);

  if (!explanation) return null;

  if (!steps.length) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
        onClick={onClose}
      >
        <div className="bg-surface-elevated border border-border-strong rounded-xl p-8 text-center" onClick={(e) => e.stopPropagation()}>
          <p className="text-neutral-500 mb-4 text-sm">No animation available for this problem.</p>
          <button onClick={onClose} className="px-4 py-2 rounded-lg border border-neutral-700 text-sm text-neutral-300 hover:bg-surface-hover transition-colors">
            Close
          </button>
        </div>
      </motion.div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.95, y: 16 }}
        animate={{ scale: 1, y: 0 }}
        className="bg-surface-elevated border border-border-strong rounded-xl w-full max-w-2xl overflow-hidden shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="px-5 py-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-purple-500/10 flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-purple-400" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">{explanation.problemTitle}</h2>
              <p className="text-xs text-neutral-500">{explanation.summary}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-md hover:bg-surface-hover text-neutral-500 transition-colors">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Step content */}
        <div className="p-8 min-h-[260px] flex flex-col items-center justify-center">
          <AnimatePresence mode="wait">
            {step && (
              <motion.div
                key={currentStep}
                initial={{ opacity: 0, x: 24 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -24 }}
                className="w-full space-y-5"
              >
                <div className="text-center">
                  <span className="text-[10px] font-mono text-neutral-600 uppercase tracking-widest">
                    Step {step.step} / {steps.length}
                  </span>
                  <h3 className="text-xl font-bold text-white mt-1">{step.title}</h3>
                  <p className="text-sm text-neutral-400 mt-1.5 max-w-md mx-auto">{step.description}</p>
                </div>
                <StepVisual step={step} />
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Controls */}
        <div className="px-5 py-3 border-t border-border flex items-center justify-between bg-surface-secondary">
          {/* Progress dots */}
          <div className="flex gap-1">
            {steps.map((_, i) => (
              <button
                key={i}
                onClick={() => setCurrentStep(i)}
                className={`h-1.5 rounded-full transition-all cursor-pointer ${
                  i === currentStep ? 'w-6 bg-amber-400' : i < currentStep ? 'w-3 bg-amber-600' : 'w-3 bg-neutral-700'
                }`}
              />
            ))}
          </div>

          {/* Buttons */}
          <div className="flex gap-2">
            <button
              onClick={() => setCurrentStep((s) => Math.max(s - 1, 0))}
              disabled={currentStep <= 0}
              className="flex items-center gap-1 px-3 py-1.5 rounded-md text-xs font-medium border border-neutral-700 text-neutral-300 hover:bg-surface-hover transition-colors disabled:opacity-30"
            >
              <SkipBack className="w-3.5 h-3.5" />
            </button>
            <button
              onClick={() => setPlaying(!playing)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium border border-neutral-700 text-neutral-300 hover:bg-surface-hover transition-colors"
            >
              {playing ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
              {playing ? 'Pause' : 'Play'}
            </button>
            <button
              onClick={() => setCurrentStep((s) => Math.min(s + 1, steps.length - 1))}
              disabled={currentStep >= steps.length - 1}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold bg-amber-500 hover:bg-amber-600 text-black transition-colors disabled:opacity-30"
            >
              Next <SkipForward className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
}
