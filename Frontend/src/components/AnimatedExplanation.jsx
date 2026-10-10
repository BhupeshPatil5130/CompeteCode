/**
 * DSA Visualizer Engine — AnimatedExplanation.jsx
 *
 * Full-page interactive dry-run visualizer modal with animated array blocks,
 * gliding pointer transitions, Hash Map memory table, live variable inspector,
 * synchronized auto-scrolling algorithm code highlighting, and keyboard navigation.
 */

import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Play, Pause, SkipForward, SkipBack, Sparkles, X,
  RotateCcw, Gauge, Terminal, ChevronRight, Database,
  CheckCircle2, Activity, Layers, CornerDownRight,
  Keyboard, Trophy
} from 'lucide-react';

// ── Color palette ─────────────────────────────────────────────────────────────
const POINTER_COLORS = [
  { bg: 'bg-amber-500/20', border: 'border-amber-500', text: 'text-amber-400', borderTop: 'border-t-amber-500', hex: '#f59e0b' },
  { bg: 'bg-sky-500/20', border: 'border-sky-500', text: 'text-sky-400', borderTop: 'border-t-sky-500', hex: '#0ea5e9' },
  { bg: 'bg-emerald-500/20', border: 'border-emerald-500', text: 'text-emerald-400', borderTop: 'border-t-emerald-500', hex: '#10b981' },
  { bg: 'bg-purple-500/20', border: 'border-purple-500', text: 'text-purple-400', borderTop: 'border-t-purple-500', hex: '#a855f7' },
  { bg: 'bg-rose-500/20', border: 'border-rose-500', text: 'text-rose-400', borderTop: 'border-t-rose-500', hex: '#f43f5e' },
];

const SPEEDS = [
  { label: '0.5×', ms: 5500 },
  { label: '1×', ms: 3000 },
  { label: '1.5×', ms: 2000 },
  { label: '2×', ms: 1200 },
];

// ── Safe JSON parse ───────────────────────────────────────────────────────────
function parseData(raw) {
  if (!raw) return null;
  if (typeof raw === 'object') return raw;
  try { return JSON.parse(raw); } catch { return raw; }
}

// ── Resolve Array Data with fallbacks ────────────────────────────────────────
function resolveArrayData(data, fallbackExample) {
  if (data) {
    const parsed = parseData(data);
    if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    if (typeof parsed === 'string' && parsed.trim().length > 0) {
      const match = parsed.match(/\[([^\]]+)\]/);
      if (match) {
        return match[1].split(/[\s,]+/).map((s) => s.trim().replace(/^["']|["']$/g, '')).filter(Boolean);
      }
      const parts = parsed.split(/[\s,]+/).filter(Boolean);
      if (parts.length > 0) return parts;
    }
  }
  if (fallbackExample) {
    const match = String(fallbackExample).match(/\[([^\]]+)\]/);
    if (match) {
      return match[1].split(/[\s,]+/).map((s) => s.trim().replace(/^["']|["']$/g, '')).filter(Boolean);
    }
  }
  return [];
}

// ── Parse Map entries from vars ──────────────────────────────────────────────
function parseMapEntries(raw) {
  if (!raw) return [];
  if (typeof raw === 'object') {
    return Object.entries(raw).map(([key, val]) => ({ key: String(key), val: String(val) }));
  }
  const str = String(raw).trim();
  const inner = str.replace(/^\{|\}$/g, '').trim();
  if (!inner) return [];
  const entries = [];
  const pairs = inner.split(/,\s*/);
  for (const pair of pairs) {
    const parts = pair.split(/[:=]/);
    if (parts.length === 2) {
      entries.push({ key: parts[0].trim(), val: parts[1].trim() });
    }
  }
  return entries;
}

// ── Array / Two-Pointer / Blocks Visualizer ───────────────────────────────────
function ArrayVisual({ data, pointers = {}, highlights = [], fill = [], visualType, fallbackExample }) {
  const safePointers = pointers || {};
  const safeHighlights = highlights || [];
  const safeFill = fill || [];
  const arr = resolveArrayData(data, fallbackExample);
  const ptrNames = Object.keys(safePointers);
  const nums = arr.map(Number).filter((n) => !isNaN(n));
  const maxVal = nums.length > 0 ? Math.max(...nums, 1) : 1;
  const isBars = visualType === 'bars' || (nums.length === arr.length && arr.length > 0 && nums.some((n) => n > 25));

  const isMultipleHighlights = safeHighlights.length > 1;

  // Check if any pointer is at the end boundary (arr.length)
  const hasEndPointer = ptrNames.some((k) => safePointers[k] === arr.length);
  const endPtrs = ptrNames.filter((k) => safePointers[k] === arr.length);

  return (
    <div className="w-full flex flex-col items-center gap-4 py-2">
      {/* ── Cell array mode (default) ── */}
      {!isBars && (
        <div className="flex flex-col items-center gap-2 w-full overflow-x-auto px-4 py-2">
          <div className="flex items-center justify-center gap-3 sm:gap-4 flex-wrap pt-12 pb-3 min-w-fit mx-auto">
            {arr.map((item, i) => {
              const isHl = safeHighlights.includes(i);
              const herePtrs = ptrNames.filter((k) => safePointers[k] === i);
              const isPointerHere = herePtrs.length > 0;
              const isSuccessMatch = isHl && isMultipleHighlights;

              return (
                <div key={`cell-${i}`} className="relative flex flex-col items-center">
                  {/* Floating Animated Pointer Badges (Side-by-side if multiple pointers at same index) */}
                  {isPointerHere && (
                    <div className="absolute -top-12 z-30 flex items-center justify-center gap-1 pointer-events-none">
                      {herePtrs.map((ptrName) => {
                        const ci = ptrNames.indexOf(ptrName);
                        const color = POINTER_COLORS[ci % POINTER_COLORS.length];
                        return (
                          <motion.div
                            key={ptrName}
                            layoutId={`ptr-${ptrName}`}
                            transition={{ type: 'spring', stiffness: 380, damping: 26 }}
                            className="flex flex-col items-center"
                          >
                            <span className={`px-2 py-0.5 rounded-full text-[10px] font-mono font-bold shadow-lg border backdrop-blur-md ${color.bg} ${color.border} ${color.text} flex items-center gap-0.5 whitespace-nowrap`}>
                              <span>↑</span>
                              <span>{ptrName}</span>
                            </span>
                            <motion.div
                              animate={{ y: [0, 3, 0] }}
                              transition={{ repeat: Infinity, duration: 1.1, ease: 'easeInOut' }}
                              className={`w-0 h-0 border-l-[4px] border-r-[4px] border-t-[5px] border-l-transparent border-r-transparent ${color.borderTop}`}
                            />
                          </motion.div>
                        );
                      })}
                    </div>
                  )}

                  {/* Array Cell / Block */}
                  <motion.div
                    layout
                    transition={{ type: 'spring', stiffness: 280, damping: 24 }}
                    animate={
                      isSuccessMatch
                        ? { scale: [1, 1.12, 1.06], y: -6 }
                        : isHl
                          ? { scale: [1, 1.08, 1.04], y: -4 }
                          : isPointerHere
                            ? { scale: 1.03, y: -2 }
                            : { scale: 1, y: 0 }
                    }
                    className={`relative w-16 h-16 sm:w-20 sm:h-20 flex flex-col items-center justify-center rounded-2xl font-mono border-2 transition-colors duration-300 shadow-xl select-none ${isSuccessMatch
                        ? 'bg-gradient-to-b from-emerald-500/30 to-emerald-900/30 border-emerald-400 text-emerald-100 ring-4 ring-emerald-400/30 shadow-[0_0_30px_rgba(16,185,129,0.4)]'
                        : isHl
                          ? 'bg-gradient-to-b from-amber-500/30 to-amber-900/30 border-amber-400 text-amber-100 ring-4 ring-amber-400/25 shadow-[0_0_25px_rgba(245,158,11,0.35)]'
                          : isPointerHere
                            ? 'bg-neutral-800/90 border-sky-500/80 text-sky-200 ring-2 ring-sky-500/20'
                            : 'bg-neutral-900/90 border-neutral-700/80 text-neutral-200 hover:border-neutral-600'
                      }`}
                  >
                    {/* Top status indicator if matched or active */}
                    {isSuccessMatch && (
                      <span className="absolute -top-2.5 px-2 py-0.5 rounded-full text-[9px] font-mono font-bold bg-emerald-500 text-neutral-950 uppercase tracking-wider shadow-md">
                        Match
                      </span>
                    )}
                    {isHl && !isSuccessMatch && (
                      <span className="absolute -top-2.5 px-2 py-0.5 rounded-full text-[9px] font-mono font-bold bg-amber-500 text-neutral-950 uppercase tracking-wider shadow-md">
                        Active
                      </span>
                    )}

                    {/* Value */}
                    <span className="text-xl sm:text-2xl font-bold tracking-tight">
                      {String(item)}
                    </span>
                  </motion.div>

                  {/* 0-based Index Label */}
                  <span className="text-[11px] font-mono text-neutral-500 mt-2">
                    idx {i}
                  </span>
                </div>
              );
            })}

            {/* End sentinel block if pointer is past array boundary */}
            {hasEndPointer && (
              <div className="relative flex flex-col items-center opacity-80">
                <div className="absolute -top-12 z-30 flex items-center justify-center gap-1 pointer-events-none">
                  {endPtrs.map((ptrName) => {
                    const ci = ptrNames.indexOf(ptrName);
                    const color = POINTER_COLORS[ci % POINTER_COLORS.length];
                    return (
                      <motion.div
                        key={ptrName}
                        layoutId={`ptr-${ptrName}`}
                        transition={{ type: 'spring', stiffness: 380, damping: 26 }}
                        className="flex flex-col items-center"
                      >
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-mono font-bold shadow-lg border backdrop-blur-md ${color.bg} ${color.border} ${color.text} flex items-center gap-0.5 whitespace-nowrap`}>
                          <span>↑</span>
                          <span>{ptrName}</span>
                        </span>
                        <motion.div
                          animate={{ y: [0, 3, 0] }}
                          transition={{ repeat: Infinity, duration: 1.1, ease: 'easeInOut' }}
                          className={`w-0 h-0 border-l-[4px] border-r-[4px] border-t-[5px] border-l-transparent border-r-transparent ${color.borderTop}`}
                        />
                      </motion.div>
                    );
                  })}
                </div>
                <div className="w-16 h-16 sm:w-20 sm:h-20 flex flex-col items-center justify-center rounded-2xl font-mono border-2 border-dashed border-neutral-700 bg-neutral-950/60 text-neutral-500 text-xs font-semibold">
                  <span>END</span>
                </div>
                <span className="text-[11px] font-mono text-neutral-600 mt-2">
                  idx {arr.length}
                </span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── Bar chart mode ── */}
      {isBars && (
        <div className="flex items-end gap-2.5 h-44 overflow-x-auto p-4 min-w-fit mx-auto">
          {arr.map((item, i) => {
            const num = Number(item);
            const heightPct = isNaN(num) ? 15 : Math.max(8, (num / maxVal) * 100);
            const isHl = safeHighlights.includes(i);
            const herePtrs = ptrNames.filter((k) => safePointers[k] === i);
            const isFill = safeFill.includes(i);
            return (
              <motion.div key={i} className="flex flex-col items-center gap-1.5"
                initial={{ scaleY: 0 }} animate={{ scaleY: 1 }}
                style={{ transformOrigin: 'bottom' }}
                transition={{ delay: i * 0.04, type: 'spring', stiffness: 220 }}
              >
                <span className="text-[10px] font-mono text-neutral-400 font-bold">{item}</span>
                <div className={`w-12 rounded-t-xl border-b-2 transition-all duration-300 ${isHl ? 'bg-amber-500/50 border-amber-400 ring-2 ring-amber-400/30' :
                    isFill ? 'bg-sky-500/35 border-sky-400' :
                      herePtrs.length > 0
                        ? `${POINTER_COLORS[ptrNames.indexOf(herePtrs[0]) % POINTER_COLORS.length].bg} border-2 ${POINTER_COLORS[ptrNames.indexOf(herePtrs[0]) % POINTER_COLORS.length].border}`
                        : 'bg-neutral-800 border-neutral-700'
                  }`} style={{ height: `${heightPct}%` }} />
                {herePtrs.map((name) => (
                  <motion.span key={name}
                    initial={{ opacity: 0, y: -4 }} animate={{ opacity: 1, y: 0 }}
                    className={`text-[10px] font-mono font-bold ${POINTER_COLORS[ptrNames.indexOf(name) % POINTER_COLORS.length].text}`}
                  >↑{name}</motion.span>
                ))}
                <span className="text-[9px] font-mono text-neutral-600">{i}</span>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}

// ── Hash Map / Key-Value Memory Table ───────────────────────────────────────
function HashMapVisual({ vars, currentComplement }) {
  if (!vars) return null;
  const mapRaw = vars.map || vars.hashMap || vars.table || vars.seen || vars.freq || vars.count || vars.dp || vars.memo;
  const hasMapKey = Boolean(
    vars.map !== undefined || vars.hashMap !== undefined || vars.table !== undefined ||
    vars.seen !== undefined || vars.freq !== undefined || vars.count !== undefined ||
    vars.dp !== undefined || vars.memo !== undefined
  );
  const entries = parseMapEntries(mapRaw);

  if (!hasMapKey && entries.length === 0) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex flex-col items-center gap-2.5 p-3.5 rounded-2xl bg-neutral-900/90 border border-purple-500/25 shadow-xl max-w-xl mx-auto w-full"
    >
      <div className="flex items-center gap-2 text-xs font-mono text-purple-300 font-semibold uppercase tracking-wider">
        <Database className="w-3.5 h-3.5 text-purple-400" />
        <span>Hash Map (Seen Values ➔ Stored Indices)</span>
      </div>

      {entries.length === 0 ? (
        <div className="py-2 text-[11px] font-mono text-neutral-500 italic">
          Map is empty `{ }` (no values stored yet)
        </div>
      ) : (
        <div className="flex flex-wrap gap-2.5 justify-center items-center py-1">
          {entries.map(({ key, val }) => {
            const isTargetComplement = currentComplement && String(currentComplement) === String(key);
            return (
              <motion.div
                key={key}
                layout
                initial={{ scale: 0, opacity: 0 }}
                animate={isTargetComplement ? { scale: [1, 1.1, 1.05], opacity: 1 } : { scale: 1, opacity: 1 }}
                transition={{ type: 'spring', stiffness: 300, damping: 22 }}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-mono text-xs border transition-all ${isTargetComplement
                    ? 'bg-emerald-500/20 border-emerald-400 text-emerald-200 ring-2 ring-emerald-400/40 shadow-[0_0_15px_rgba(16,185,129,0.35)]'
                    : 'bg-neutral-800/90 border-neutral-700 text-neutral-300'
                  }`}
              >
                <span className="text-[10px] text-neutral-500 uppercase font-bold">val</span>
                <span className={`font-bold ${isTargetComplement ? 'text-emerald-300 text-sm' : 'text-amber-300'}`}>{key}</span>
                <span className="text-neutral-500">➔</span>
                <span className="text-[10px] text-neutral-500 uppercase font-bold">idx</span>
                <span className="font-bold text-sky-300">{val}</span>
                {isTargetComplement && (
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 ml-0.5" />
                )}
              </motion.div>
            );
          })}
        </div>
      )}
    </motion.div>
  );
}

// ── Operation Banner / Live Status ──────────────────────────────────────────
function OperationBanner({ step }) {
  if (!step) return null;
  const vars = step.vars || {};
  const keys = Object.keys(vars);
  if (keys.length === 0) return null;

  // Show up to 5 most relevant variables in a compact inline banner
  const displayKeys = keys.slice(0, 6);

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.96 }}
      animate={{ opacity: 1, scale: 1 }}
      className="flex items-center justify-center gap-2 sm:gap-4 flex-wrap px-4 py-2.5 rounded-xl bg-neutral-900/90 border border-neutral-800 shadow-lg font-mono text-xs max-w-3xl mx-auto"
    >
      <Activity className="w-4 h-4 text-amber-400 shrink-0" />
      {displayKeys.map((key, idx) => {
        const val = vars[key];
        // Skip map/hashMap entries in the banner (shown in HashMapVisual)
        if (key === 'map' || key === 'hashMap' || key === 'table') return null;
        const isResult = key === 'result' || key === 'ans' || key === 'answer';
        return (
          <span key={key} className="text-neutral-300 flex items-center gap-1">
            {idx > 0 && <span className="text-neutral-600">•</span>}
            <span className="text-neutral-500">{key}:</span>
            <span className={`font-bold ${isResult ? 'text-emerald-400' :
                key === 'i' || key === 'left' || key === 'right' || key === 'mid' ? 'text-sky-400' :
                  key === 'complement' || key === 'target' ? 'text-amber-400' :
                    'text-purple-300'
              }`}>{String(val)}</span>
            {isResult && <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 ml-0.5" />}
          </span>
        );
      })}
    </motion.div>
  );
}

// ── Tree Visualizer ───────────────────────────────────────────────────────────
function TreeVisual({ data, highlights = [] }) {
  const safeHighlights = highlights || [];
  const parsed = parseData(data);
  const nodes = Array.isArray(parsed) ? parsed
    : typeof parsed === 'string' ? parsed.split(/[\s,]+/).filter(Boolean)
      : [];

  const levels = [];
  let idx = 0, sz = 1;
  while (idx < nodes.length) { levels.push(nodes.slice(idx, idx + sz)); idx += sz; sz *= 2; }

  return (
    <div className="flex flex-col items-center gap-6 w-full overflow-x-auto py-2">
      {levels.map((level, li) => (
        <div key={li} className="flex flex-col items-center gap-1">
          <div className="flex justify-center" style={{ gap: `${Math.max(14, 60 / (li + 1))}px` }}>
            {level.map((node, ni) => {
              const flat = levels.slice(0, li).reduce((s, l) => s + l.length, 0) + ni;
              const isHl = safeHighlights.includes(flat) || safeHighlights.includes(ni);
              const isNull = node === 'null' || node === null;
              return (
                <motion.div key={`${li}-${ni}`}
                  initial={{ scale: 0, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
                  transition={{ delay: li * 0.1 + ni * 0.05, type: 'spring' }}
                  className={`w-12 h-12 flex flex-col items-center justify-center rounded-full font-mono text-sm font-bold transition-all shadow-md ${isNull ? 'border border-dashed border-neutral-700 text-neutral-650' :
                      isHl ? 'bg-amber-500/25 border-2 border-amber-400 text-amber-300 shadow-[0_0_20px_rgba(245,158,11,0.35)]' :
                        li === 0 ? 'bg-amber-500/15 border border-amber-500/30 text-amber-300' :
                          'bg-purple-500/10 border border-purple-500/25 text-purple-300'
                    }`}
                >
                  <span>{isNull ? '∅' : String(node)}</span>
                </motion.div>
              );
            })}
          </div>
          <span className="text-[9px] font-mono text-neutral-600">level {li}</span>
        </div>
      ))}
    </div>
  );
}

// ── Matrix Visualizer ─────────────────────────────────────────────────────────
function MatrixVisual({ data, highlights = [] }) {
  const safeHighlights = highlights || [];
  const parsed = parseData(data);
  const matrix = Array.isArray(parsed) ? parsed
    : String(data || '').split('\n').map((r) => r.split(/[\s,]+/).filter(Boolean));

  return (
    <div className="flex flex-col items-center gap-2 overflow-x-auto py-2">
      {matrix.map((row, ri) => (
        <div key={ri} className="flex items-center gap-2">
          <span className="text-[10px] font-mono text-neutral-600 w-4 text-right">r{ri}</span>
          <div className="flex gap-1.5">
            {(Array.isArray(row) ? row : [row]).map((cell, ci) => {
              const isHl = safeHighlights.some(([r, c]) => r === ri && c === ci);
              return (
                <motion.div key={`${ri}-${ci}`}
                  initial={{ scale: 0 }} animate={{ scale: 1 }}
                  transition={{ delay: ri * 0.04 + ci * 0.04 }}
                  className={`w-12 h-12 flex items-center justify-center rounded-xl text-sm font-mono font-bold transition-all shadow-md ${isHl ? 'bg-amber-500/30 text-amber-300 border-2 border-amber-400 shadow-[0_0_20px_rgba(245,158,11,0.3)]' :
                      String(cell) === '1' ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/25' :
                        String(cell) === '0' ? 'bg-neutral-800 text-neutral-600 border border-neutral-700' :
                          'bg-amber-500/10 text-amber-300 border border-amber-500/20'
                    }`}
                >{String(cell)}</motion.div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
}

// ── Stack Visualizer ──────────────────────────────────────────────────────────
function StackVisual({ data }) {
  const parsed = parseData(data);
  const items = Array.isArray(parsed) ? parsed : String(data || '').split(/[\s,]+/).filter(Boolean);
  return (
    <div className="flex flex-col items-center gap-2 py-2">
      <div className="w-48 border-l-2 border-r-2 border-b-2 border-neutral-700 rounded-b-2xl p-2.5 flex flex-col-reverse items-center gap-1.5 min-h-[140px] justify-start bg-neutral-950/40 shadow-inner">
        {items.length === 0 ? (
          <span className="text-neutral-600 text-xs font-mono py-8">Empty Stack</span>
        ) : (
          items.map((item, i) => (
            <motion.div key={i}
              initial={{ y: -30, opacity: 0 }} animate={{ y: 0, opacity: 1 }}
              transition={{ delay: i * 0.08, type: 'spring', stiffness: 280 }}
              className={`w-full py-2.5 text-center rounded-xl font-mono text-sm font-bold shadow-lg transition-all ${i === items.length - 1
                  ? 'bg-amber-500/20 border-2 border-amber-500 text-amber-300 shadow-[0_0_20px_rgba(245,158,11,0.3)]'
                  : 'bg-neutral-800/90 border border-neutral-700 text-neutral-300'
                }`}
            >
              {String(item)}
              {i === items.length - 1 && <span className="text-[8px] ml-2 text-amber-400/80 uppercase tracking-widest font-bold">top</span>}
            </motion.div>
          ))
        )}
      </div>
      <span className="text-[10px] font-mono text-neutral-500 uppercase tracking-wider font-bold">Stack Memory</span>
    </div>
  );
}

// ── Linked List Visualizer ────────────────────────────────────────────────────
function LinkedListVisual({ data, highlights = [], pointers = {} }) {
  const safeHighlights = highlights || [];
  const safePointers = pointers || {};
  const parsed = parseData(data);
  const nodes = Array.isArray(parsed) ? parsed : String(data || '').split(/[\s,]+/).filter(Boolean);
  const ptrNames = Object.keys(safePointers);

  return (
    <div className="flex items-center gap-2.5 flex-wrap justify-center py-2">
      {nodes.map((node, i) => {
        const isHl = safeHighlights.includes(i);
        const here = ptrNames.filter((k) => safePointers[k] === i);
        return (
          <motion.div key={i} initial={{ opacity: 0, x: -16 }} animate={{ opacity: 1, x: 0 }}
            transition={{ delay: i * 0.09 }} className="flex items-center gap-2"
          >
            <div className="flex flex-col items-center gap-1">
              <div className={`px-5 py-3.5 rounded-2xl font-mono text-sm font-bold border-2 transition-all shadow-lg ${isHl ? 'bg-amber-500/25 border-amber-400 text-amber-300 ring-2 ring-amber-400/25'
                  : 'bg-purple-500/10 border-purple-500/30 text-purple-300'
                }`}>{String(node)}</div>
              {here.length > 0 && (
                <div className="flex gap-1">
                  {here.map((n) => (
                    <span key={n} className={`text-[10px] font-mono font-bold ${POINTER_COLORS[ptrNames.indexOf(n) % POINTER_COLORS.length].text}`}>
                      ↑{n}
                    </span>
                  ))}
                </div>
              )}
            </div>
            {i < nodes.length - 1 && <span className="text-neutral-600 text-xl font-bold">→</span>}
          </motion.div>
        );
      })}
      <span className="text-neutral-600 text-sm ml-1 font-mono">→ null</span>
    </div>
  );
}

// ── Variables Panel ───────────────────────────────────────────────────────────
function VarsPanel({ vars }) {
  if (!vars || Object.keys(vars).length === 0) return null;
  return (
    <div className="flex flex-wrap gap-2 justify-start">
      {Object.entries(vars).map(([k, v]) => (
        <motion.div key={k} initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
          className="px-3 py-1.5 rounded-xl bg-neutral-900/90 border border-neutral-700/80 font-mono text-xs shadow-md flex items-center gap-1.5"
        >
          <span className="text-neutral-400 font-medium">{k}</span>
          <span className="text-neutral-600">=</span>
          <span className="text-emerald-400 font-bold">{String(v)}</span>
        </motion.div>
      ))}
    </div>
  );
}

// ── Code Panel with Auto-scrolling Active Line Highlight ──────────────────────
function CodePanel({ code, codeLines = [] }) {
  if (!code || code.length === 0) return null;
  const lines = Array.isArray(code) ? code : String(code).split('\n');
  const scrollRef = useRef(null);
  const activeLineRef = useRef(null);

  useEffect(() => {
    if (activeLineRef.current && scrollRef.current) {
      activeLineRef.current.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }, [codeLines]);

  return (
    <div className="w-full rounded-2xl overflow-hidden border border-neutral-800 bg-[#09090b] shadow-xl flex flex-col">
      <div className="px-4 py-2.5 border-b border-neutral-800/80 bg-neutral-900/80 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-2">
          <Terminal className="w-3.5 h-3.5 text-amber-400" />
          <span className="text-[11px] text-neutral-300 font-mono font-semibold tracking-wider uppercase">Algorithm Execution</span>
        </div>
        {codeLines.length > 0 && (
          <span className="text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-amber-500/15 border border-amber-500/30 text-amber-400 font-bold">
            Line {codeLines.join(', ')}
          </span>
        )}
      </div>
      <div ref={scrollRef} className="p-3.5 overflow-x-auto max-h-56 overflow-y-auto font-mono text-xs">
        {lines.map((line, i) => {
          const ln = i + 1;
          const act = codeLines.includes(ln);
          return (
            <motion.div
              key={i}
              ref={act ? activeLineRef : null}
              animate={act ? { backgroundColor: 'rgba(245,158,11,0.14)' } : { backgroundColor: 'transparent' }}
              className={`flex items-center gap-3 px-2 py-1 rounded transition-colors ${act ? 'border-l-2 border-l-amber-400 pl-2' : 'border-l-2 border-l-transparent pl-2'
                }`}
            >
              <span className={`select-none w-6 text-right shrink-0 text-[11px] font-mono ${act ? 'text-amber-400 font-bold' : 'text-neutral-650'}`}>
                {act ? '▶' : ln}
              </span>
              <span className={`whitespace-pre font-mono ${act ? 'text-amber-200 font-bold' : 'text-neutral-400'}`}>
                {line}
              </span>
            </motion.div>
          );
        })}
      </div>
    </div>
  );
}

// ── Main Step Visualizer Component ────────────────────────────────────────────
function StepVisual({ step, persistentCode, fallbackExample }) {
  if (!step) return null;
  const pointers = step.pointers || {};
  const highlights = step.highlights || [];
  const vars = step.vars || {};
  const codeLines = step.codeLines || [];
  const code = step.code && step.code.length > 0 ? step.code : persistentCode;

  const visual = (() => {
    switch ((step.visualType || '').toLowerCase()) {
      case 'array': case 'two-pointer': case 'two_pointer': case 'highlight':
        return (
          <ArrayVisual
            data={step.visualData}
            pointers={pointers}
            highlights={highlights}
            visualType={step.visualType}
            fallbackExample={fallbackExample}
          />
        );
      case 'bars':
        return (
          <ArrayVisual
            data={step.visualData}
            pointers={pointers}
            highlights={highlights}
            fill={step.fill || []}
            visualType="bars"
            fallbackExample={fallbackExample}
          />
        );
      case 'tree':
        return <TreeVisual data={step.visualData} highlights={highlights} />;
      case 'matrix': case 'grid':
        return <MatrixVisual data={step.visualData} highlights={highlights} />;
      case 'stack':
        return <StackVisual data={step.visualData} />;
      case 'linkedlist': case 'linked_list':
        return <LinkedListVisual data={step.visualData} highlights={highlights} pointers={pointers} />;
      case 'pointer':
        return (
          <motion.div className="flex flex-col items-center gap-3 py-4">
            <motion.span animate={{ y: [0, -8, 0] }} transition={{ repeat: Infinity, duration: 1.4 }} className="text-4xl">👆</motion.span>
            <p className="text-amber-400 font-mono text-base px-6 py-3 bg-amber-500/10 rounded-2xl border border-amber-500/20">{step.visualData}</p>
          </motion.div>
        );
      case 'text': default:
        return (
          <ArrayVisual
            data={step.visualData}
            pointers={pointers}
            highlights={highlights}
            visualType="array"
            fallbackExample={fallbackExample}
          />
        );
    }
  })();

  return (
    <div className="space-y-6 w-full max-w-5xl mx-auto">
      {/* 1. Operation Banner (live calculation details) */}
      <OperationBanner step={step} />

      {/* 2. Visual Structure (Array Blocks with Gliding Pointers) */}
      {visual}

      {/* 3. Hash Map / State memory if available */}
      <HashMapVisual vars={vars} currentComplement={vars.complement} />

      {/* 4. Live Variables + Code Panel Split Area */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-start pt-2">
        {/* Left: Code Walkthrough */}
        <div className={Object.keys(vars).length > 0 ? 'lg:col-span-8' : 'lg:col-span-12'}>
          {code && <CodePanel code={code} codeLines={codeLines} />}
        </div>

        {/* Right: Live Variables State */}
        {Object.keys(vars).length > 0 && (
          <div className="lg:col-span-4 rounded-2xl border border-neutral-800 bg-[#09090b] p-3.5 shadow-xl flex flex-col gap-3">
            <div className="flex items-center gap-2 border-b border-neutral-800/80 pb-2">
              <Layers className="w-3.5 h-3.5 text-emerald-400" />
              <span className="text-[11px] text-neutral-300 font-mono font-semibold uppercase tracking-wider">Live State</span>
            </div>
            <VarsPanel vars={vars} />
          </div>
        )}
      </div>
    </div>
  );
}

// ── Root Component ────────────────────────────────────────────────────────────
export default function AnimatedExplanation({ explanation, onClose }) {
  const [current, setCurrent] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [speedIdx, setSpeedIdx] = useState(1);

  const steps = explanation?.steps || [];
  const step = steps[current];
  const dur = SPEEDS[speedIdx].ms;
  const progress = steps.length > 0 ? ((current + 1) / steps.length) * 100 : 0;
  const isFinalStep = steps.length > 0 && current === steps.length - 1;

  // Persist code across steps so it doesn't vanish on later steps
  const persistentCode = steps.find((s) => s.code && s.code.length > 0)?.code || null;

  // Auto-play timer
  useEffect(() => {
    if (!playing || !step) return;
    const t = setTimeout(() => {
      if (current < steps.length - 1) setCurrent((c) => c + 1);
      else setPlaying(false);
    }, step.durationMs || dur);
    return () => clearTimeout(t);
  }, [playing, current, step, steps.length, dur]);

  useEffect(() => { setCurrent(0); setPlaying(false); }, [explanation]);

  // Keyboard navigation shortcuts
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (['INPUT', 'TEXTAREA'].includes(document.activeElement?.tagName)) return;
      if (e.key === ' ' || e.code === 'Space') {
        e.preventDefault();
        setPlaying((p) => !p);
      } else if (e.key === 'ArrowRight') {
        e.preventDefault();
        setPlaying(false);
        setCurrent((c) => Math.min(c + 1, steps.length - 1));
      } else if (e.key === 'ArrowLeft') {
        e.preventDefault();
        setPlaying(false);
        setCurrent((c) => Math.max(c - 1, 0));
      } else if (e.key === 'Escape') {
        onClose?.();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [steps.length, onClose]);

  if (!explanation) return null;

  if (!steps.length) return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
      onClick={onClose}
    >
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-10 text-center shadow-2xl" onClick={(e) => e.stopPropagation()}>
        <Sparkles className="w-12 h-12 text-purple-400 mx-auto mb-4 opacity-40" />
        <p className="text-neutral-400 text-sm mb-6">No animation steps available.</p>
        <button onClick={onClose} className="px-5 py-2.5 rounded-xl border border-neutral-700 text-sm text-neutral-300 hover:bg-neutral-800 transition-colors">
          Close
        </button>
      </div>
    </motion.div>
  );

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-2 sm:p-4 md:p-6"
      onClick={onClose}
    >
      {/* ── LARGE modal — 96vw × 94vh ── */}
      <motion.div
        initial={{ scale: 0.94, y: 25, opacity: 0 }}
        animate={{ scale: 1, y: 0, opacity: 1 }}
        exit={{ scale: 0.94, y: 25, opacity: 0 }}
        transition={{ type: 'spring', stiffness: 280, damping: 26 }}
        className="bg-[#0c0c0e] border border-neutral-800 rounded-2xl w-full max-w-[96vw] h-[94vh] flex flex-col overflow-hidden shadow-[0_0_120px_rgba(0,0,0,0.8)]"
        onClick={(e) => e.stopPropagation()}
      >
        {/* ── TOP BAR ── */}
        <div className="px-6 py-3.5 border-b border-neutral-800 bg-neutral-900/70 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-4 min-w-0">
            <div className="w-9 h-9 rounded-xl bg-purple-500/15 flex items-center justify-center border border-purple-500/20 shrink-0">
              <Sparkles className="w-4.5 h-4.5 text-purple-400" />
            </div>
            <div className="min-w-0">
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-white truncate">{explanation.problemTitle}</h2>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-mono font-bold bg-purple-500/15 text-purple-300 border border-purple-500/30">
                  AI Dry-Run Engine
                </span>
              </div>
              <p className="text-[11px] text-neutral-500 truncate">{explanation.summary}</p>
            </div>

            {/* Example trace badge */}
            {explanation.exampleInput && (
              <div className="hidden lg:flex items-center gap-2 px-3 py-1.5 rounded-xl bg-neutral-800/60 border border-neutral-700/80 shrink-0 max-w-md overflow-hidden">
                <span className="text-[10px] text-neutral-400 uppercase tracking-wider font-bold shrink-0">Trace on:</span>
                <code className="text-[11px] text-amber-400 font-mono truncate">{explanation.exampleInput}</code>
                {explanation.exampleOutput && (
                  <>
                    <ChevronRight className="w-3.5 h-3.5 text-neutral-600 shrink-0" />
                    <code className="text-[11px] text-emerald-400 font-mono truncate">{explanation.exampleOutput}</code>
                  </>
                )}
              </div>
            )}
          </div>

          <div className="flex items-center gap-2.5 shrink-0">
            {/* Keyboard shortcuts tooltip */}
            <div className="hidden xl:flex items-center gap-1 px-2.5 py-1 rounded-lg bg-neutral-850 border border-neutral-800 text-[10px] text-neutral-400 font-mono">
              <Keyboard className="w-3 h-3 text-neutral-500" />
              <span>Space: Play/Pause • ←/→: Step</span>
            </div>

            {/* Speed control */}
            <div className="flex items-center bg-neutral-800 rounded-xl p-0.5 border border-neutral-700 gap-0.5">
              <Gauge className="w-3.5 h-3.5 text-neutral-500 ml-2" />
              {SPEEDS.map((s, i) => (
                <button key={s.label} onClick={() => setSpeedIdx(i)}
                  className={`text-[10px] font-mono px-2.5 py-1 rounded-lg transition-colors font-semibold ${speedIdx === i ? 'bg-amber-500/25 text-amber-300' : 'text-neutral-400 hover:text-neutral-200'
                    }`}
                >{s.label}</button>
              ))}
            </div>

            {/* Close button */}
            <button onClick={onClose}
              className="p-2 rounded-xl hover:bg-neutral-800 text-neutral-400 hover:text-white transition-colors border border-transparent hover:border-neutral-700"
            ><X className="w-4.5 h-4.5" /></button>
          </div>
        </div>

        {/* ── PROGRESS BAR ── */}
        <div className="h-1 bg-neutral-800/80 shrink-0">
          <motion.div className="h-full bg-gradient-to-r from-purple-500 via-amber-500 to-emerald-500"
            animate={{ width: `${progress}%` }} transition={{ duration: 0.35, ease: 'easeOut' }}
          />
        </div>

        {/* ── MAIN CONTENT — Split Stage & Step Navigator ── */}
        <div className="flex-1 min-h-0 flex flex-col lg:flex-row overflow-hidden">

          {/* Left: Visualization & Code Canvas */}
          <div className="flex-1 flex flex-col min-h-0 overflow-y-auto">
            <AnimatePresence mode="wait">
              {step && (
                <motion.div key={current}
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  transition={{ duration: 0.22 }}
                  className="flex-1 p-6 md:p-8 space-y-6"
                >
                  {/* Step label + title + desc */}
                  <div className="text-center max-w-3xl mx-auto">
                    <div className="flex items-center justify-center gap-2 mb-2">
                      <span className="text-[10px] font-mono text-neutral-400 uppercase tracking-widest font-bold px-2.5 py-0.5 rounded-full bg-neutral-800/80 border border-neutral-700">
                        Step {step.step} of {steps.length}
                      </span>
                      {playing && (
                        <motion.div animate={{ opacity: [1, 0.2, 1] }} transition={{ repeat: Infinity, duration: 0.8 }}
                          className="w-2.5 h-2.5 rounded-full bg-amber-400 shadow-[0_0_10px_rgba(245,158,11,0.6)]"
                        />
                      )}
                      {isFinalStep && (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-mono font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 flex items-center gap-1">
                          <Trophy className="w-3 h-3 text-emerald-400" />
                          Final Answer
                        </span>
                      )}
                    </div>
                    <h3 className="text-2xl font-bold text-white mb-2 tracking-tight">{step.title}</h3>
                    <p className="text-sm text-neutral-300 leading-relaxed">{step.description}</p>
                  </div>

                  {/* Visual components */}
                  <StepVisual
                    step={step}
                    persistentCode={persistentCode}
                    fallbackExample={explanation.exampleInput}
                  />
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Right: Steps Timeline Sidebar */}
          <div className="hidden lg:flex flex-col w-72 border-l border-neutral-800 overflow-y-auto bg-neutral-900/40 shrink-0">
            <div className="px-4 py-3 border-b border-neutral-800 text-[11px] text-neutral-400 uppercase tracking-wider font-bold flex items-center justify-between">
              <span>Steps Timeline</span>
              <span className="text-[10px] font-mono text-neutral-500">{current + 1}/{steps.length}</span>
            </div>
            {steps.map((s, i) => (
              <button key={i}
                onClick={() => { setCurrent(i); setPlaying(false); }}
                className={`text-left px-4 py-3.5 border-b border-neutral-800/60 transition-all ${i === current
                    ? 'bg-amber-500/15 border-l-4 border-l-amber-500 text-white'
                    : i < current
                      ? 'text-neutral-400 hover:bg-neutral-800/50'
                      : 'text-neutral-600 hover:bg-neutral-800/30'
                  }`}
              >
                <div className="flex items-center gap-2.5">
                  <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 ${i < current ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' :
                      i === current ? 'bg-amber-500 text-neutral-950 font-extrabold shadow-sm' :
                        'bg-neutral-800 text-neutral-500'
                    }`}>
                    {i < current ? '✓' : i + 1}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="text-xs font-semibold truncate">{s.title}</p>
                    <p className="text-[10px] text-neutral-500 truncate mt-0.5">{s.description}</p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* ── BOTTOM CONTROLS ── */}
        <div className="px-6 py-3.5 border-t border-neutral-800 bg-neutral-900/80 shrink-0 flex items-center justify-between gap-4">

          {/* Mobile progress indicators */}
          <div className="flex gap-1 flex-wrap max-w-xs lg:hidden">
            {steps.map((_, i) => (
              <button key={i} onClick={() => { setCurrent(i); setPlaying(false); }}
                className={`h-1.5 rounded-full transition-all duration-300 ${i === current ? 'w-6 bg-amber-400' : i < current ? 'w-2.5 bg-emerald-500' : 'w-2.5 bg-neutral-700'
                  }`} title={`Step ${i + 1}`}
              />
            ))}
          </div>

          {/* Desktop step status */}
          <span className="text-xs text-neutral-400 font-mono hidden lg:block font-medium">
            Step {current + 1} of {steps.length} • {Math.round(progress)}% complete
          </span>

          {/* Playback action buttons */}
          <div className="flex items-center gap-2 shrink-0">
            <button onClick={() => { setCurrent(0); setPlaying(false); }} disabled={current === 0} title="Reset to start (or press ←)"
              className="p-2.5 rounded-xl border border-neutral-700 text-neutral-400 hover:bg-neutral-800 hover:text-white transition-colors disabled:opacity-30 disabled:hover:bg-transparent"
            ><RotateCcw className="w-4 h-4" /></button>

            <button onClick={() => { setCurrent((c) => Math.max(c - 1, 0)); setPlaying(false); }}
              disabled={current <= 0} title="Previous step (← Arrow)"
              className="p-2.5 rounded-xl border border-neutral-700 text-neutral-300 hover:bg-neutral-800 transition-colors disabled:opacity-30 disabled:hover:bg-transparent"
            ><SkipBack className="w-4 h-4" /></button>

            <button onClick={() => setPlaying((p) => !p)}
              title="Play/Pause (Space)"
              className={`flex items-center gap-2 px-6 py-2.5 rounded-xl text-sm font-bold transition-all shadow-lg ${playing
                  ? 'bg-amber-500/20 border border-amber-500/50 text-amber-300 hover:bg-amber-500/30'
                  : 'bg-gradient-to-r from-amber-500 to-amber-600 text-neutral-950 hover:brightness-110'
                }`}
            >
              {playing ? <Pause className="w-4 h-4 fill-current" /> : <Play className="w-4 h-4 fill-current" />}
              <span>{playing ? 'Pause' : 'Play Animation'}</span>
            </button>

            <button onClick={() => { setCurrent((c) => Math.min(c + 1, steps.length - 1)); setPlaying(false); }}
              disabled={current >= steps.length - 1} title="Next step (→ Arrow)"
              className="p-2.5 rounded-xl border border-neutral-700 text-neutral-300 hover:bg-neutral-800 transition-colors disabled:opacity-30 disabled:hover:bg-transparent"
            ><SkipForward className="w-4 h-4" /></button>
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
}
