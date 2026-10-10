import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import CodeEditor from '../components/CodeEditor';
import { Bot, ChevronRight, ChevronLeft, Play, CheckCircle, Sparkles, BookOpen, ArrowLeft, Terminal, XCircle, Code2, Cpu } from 'lucide-react';
import Scene3D from '../components/Scene3D';
import toast from 'react-hot-toast';
import { submissionApi } from '../api/client';
import { QUESTS } from '../data/quests';

export default function StoryMode() {
  const [view, setView] = useState('menu'); // 'menu' | 'playing'
  const [selectedQuest, setSelectedQuest] = useState(null);
  
  const [chapterIndex, setChapterIndex] = useState(0);
  const [dialogueIndex, setDialogueIndex] = useState(0);
  const [language, setLanguage] = useState('java');
  const [code, setCode] = useState('');
  
  const [phase, setPhase] = useState('narrative');
  
  const [isExecuting, setIsExecuting] = useState(false);
  const [executionOutput, setExecutionOutput] = useState('');
  const [executionError, setExecutionError] = useState('');
  const [analysisError, setAnalysisError] = useState('');
  const [showErrorPopup, setShowErrorPopup] = useState(false);
  const [activeTab, setActiveTab] = useState('testcases');
  const [selectedTestCaseIndex, setSelectedTestCaseIndex] = useState(0);
  const [testResultStatus, setTestResultStatus] = useState(null);

  useEffect(() => {
    if (view === 'playing' && selectedQuest) {
      const currentChapter = selectedQuest.chapters[chapterIndex];
      setDialogueIndex(0);
      setCode(currentChapter.defaultCode[language]);
      setPhase('narrative');
    }
  }, [view, selectedQuest, chapterIndex, language]);

  const handleSelectQuest = (quest) => {
    setSelectedQuest(quest);
    setChapterIndex(0);
    setExecutionOutput('');
    setExecutionError('');
    setView('playing');
  };

  if (view === 'menu') {
    return (
      <div className="flex flex-col min-h-[calc(100vh-3.5rem)] relative overflow-hidden bg-[#0A0A0A]">
        <Scene3D />
        <div className="relative z-10 max-w-5xl mx-auto px-4 py-16 w-full">
          <div className="text-center mb-12">
            <div className="inline-flex items-center justify-center p-3 rounded-full bg-amber-500/10 mb-4 border border-amber-500/20">
              <Sparkles className="w-8 h-8 text-amber-500" />
            </div>
            <h1 className="text-4xl md:text-5xl font-extrabold text-white mb-4 tracking-tight">Logic Quest</h1>
            <p className="text-lg text-neutral-400 max-w-2xl mx-auto">
              Master the basics of programming logic through immersive, interactive storylines. Choose a quest to begin your journey.
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-6 mt-10">
            {QUESTS.map((quest, i) => (
              <motion.div
                key={quest.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.1 }}
                onClick={() => handleSelectQuest(quest)}
                className="group relative bg-surface-elevated/80 backdrop-blur-md border border-white/5 rounded-2xl p-6 cursor-pointer hover:border-amber-500/30 hover:shadow-[0_0_30px_rgba(245,158,11,0.1)] transition-all duration-500 overflow-hidden flex flex-col h-full"
              >
                <div className="absolute inset-0 bg-gradient-to-br from-amber-500/5 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
                
                {/* Glowing orb in corner */}
                <div className="absolute -top-10 -right-10 w-32 h-32 bg-amber-500/10 rounded-full blur-3xl group-hover:bg-amber-500/20 transition-colors duration-500 pointer-events-none" />

                <div className="relative z-10 flex-1 flex flex-col">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-black/50 rounded-lg border border-white/5 group-hover:border-amber-500/30 transition-colors">
                        <BookOpen className="w-4 h-4 text-amber-500" />
                      </div>
                      <h3 className="text-xl font-bold text-white group-hover:text-amber-400 transition-colors">{quest.title}</h3>
                    </div>
                    <span className={`px-3 py-1 text-[10px] font-bold uppercase tracking-widest rounded-md border ${quest.difficulty === 'Beginner' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-purple-500/10 text-purple-400 border-purple-500/20'}`}>
                      {quest.difficulty}
                    </span>
                  </div>
                  
                  <p className="text-sm text-neutral-400 mb-6 flex-1 leading-relaxed">
                    {quest.description}
                  </p>
                  
                  <div className="mt-auto pt-5 border-t border-white/5 flex items-center justify-between group-hover:border-amber-500/20 transition-colors">
                    <div className="flex items-center gap-2 text-[10px] text-neutral-500 font-mono uppercase tracking-widest">
                      <Cpu className="w-3.5 h-3.5 text-neutral-600 group-hover:text-amber-500/50 transition-colors" /> 
                      {quest.chapters.length} Chapters
                    </div>
                    
                    <div className="relative flex items-center justify-center overflow-hidden rounded-lg p-[1px] group/btn">
                      {/* Sweeping border gradient that appears on card hover */}
                      <div className="absolute inset-[-1000%] bg-[conic-gradient(from_90deg_at_50%_50%,transparent_0%,rgba(245,158,11,0.8)_50%,transparent_100%)] opacity-0 group-hover:opacity-100 animate-[spin_3s_linear_infinite] transition-opacity duration-500" />
                      
                      <div className="relative flex items-center gap-2 bg-neutral-900/90 backdrop-blur-sm px-4 py-2 rounded-[7px] text-[10px] font-bold text-amber-500 uppercase tracking-widest transition-colors shadow-inner">
                        <Play className="w-3 h-3 group-hover:scale-110 group-hover:text-amber-400 transition-all" />
                        Initiate
                      </div>
                    </div>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  const currentChapter = selectedQuest.chapters[chapterIndex];
  
  const currentDialogueArray = phase === 'success' ? currentChapter.successNarrative : currentChapter.narrative;

  const nextDialogue = () => {
    if (dialogueIndex < currentDialogueArray.length - 1) {
      setDialogueIndex(prev => prev + 1);
    } else if (phase === 'narrative') {
      setPhase('input');
    }
  };

  const prevDialogue = () => {
    if (dialogueIndex > 0) {
      setDialogueIndex(prev => prev - 1);
    }
  };

  const handleRun = async () => {
    setIsExecuting(true);
    setExecutionOutput('');
    setExecutionError('');
    setTestResultStatus(null);
    setActiveTab('result');

    let executeSuccess = false;

    try {
      const { data } = await submissionApi.runCode({ code, language });
      if (data.stderr || data.compileOutput) {
        setExecutionError(data.stderr || data.compileOutput);
        toast.error("Compilation or Runtime Error");
      } else {
        setExecutionOutput(data.stdout || 'Program exited with no output.');
        executeSuccess = true;
      }
    } catch (err) {
      setExecutionError(err.response?.data?.error || 'Execution failed on server.');
      toast.error("Execution Request Failed");
    } finally {
      setIsExecuting(false);
    }

    if (executeSuccess) {
      const codeClean = code.replace(/\s+/g, ' ');
      let allPassed = true;
      let failMessage = '';
      for (const val of currentChapter.validations) {
        if (!val.regex.test(code) && !val.regex.test(codeClean)) {
          allPassed = false;
          failMessage = val.message;
          break;
        }
      }

      if (!allPassed) {
        setTestResultStatus('failed');
        setAnalysisError(failMessage);
        setShowErrorPopup(true);
      } else {
        setTestResultStatus('accepted');
        toast.success("Logic execution successful!");
        setPhase('success');
        setDialogueIndex(0); // reset dialogue index to read success message
      }
    } else {
      setTestResultStatus('failed');
    }
  };

  const nextChapter = () => {
    if (chapterIndex < selectedQuest.chapters.length - 1) {
      setChapterIndex(prev => prev + 1);
    } else {
      toast.success(`You have completed "${selectedQuest.title}"!`);
      setView('menu');
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-3.5rem)] relative overflow-hidden bg-[#0A0A0A]">
      <Scene3D />
      
      {/* Left Side: Story & Narrative */}
      <div className="w-full lg:w-1/3 z-10 flex flex-col border-r border-border bg-black/60 backdrop-blur-md">
        <div className="p-4 border-b border-border flex items-center justify-between">
          <button onClick={() => setView('menu')} className="flex items-center gap-1 text-xs text-neutral-400 hover:text-white transition-colors">
            <ArrowLeft className="w-3 h-3" /> Back to Quests
          </button>
          <span className="px-3 py-1 bg-amber-500/10 border border-amber-500/20 text-amber-500 text-xs font-bold rounded-full shadow-[0_0_10px_rgba(245,158,11,0.1)]">
            {currentChapter.title}
          </span>
        </div>

        <div className="flex-1 p-6 flex flex-col justify-end space-y-6">
          {/* Character Avatar */}
          <motion.div 
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            className="flex flex-col items-center justify-center mb-10"
          >
            <div className="w-32 h-32 rounded-full bg-gradient-to-br from-neutral-800 to-black border-[4px] border-amber-500/30 flex items-center justify-center shadow-[0_0_50px_rgba(245,158,11,0.2)] mb-4 relative overflow-hidden">
              <Bot className="w-16 h-16 text-amber-400 z-10 animate-pulse" />
              <div className="absolute inset-0 bg-amber-500/10 blur-xl" />
            </div>
            <h3 className="text-amber-400 font-mono font-bold tracking-widest text-lg">C.O.D.E. BOT</h3>
          </motion.div>

          {/* Dialogue Box */}
          <div className={`border rounded-xl p-5 shadow-2xl relative min-h-[160px] flex flex-col transition-colors duration-500 ${phase === 'success' ? 'bg-emerald-900/30 border-emerald-500/50' : 'bg-surface-elevated border-border-strong'}`}>
            <div className={`absolute -top-3 left-6 w-0 h-0 border-l-8 border-r-8 border-b-8 border-transparent ${phase === 'success' ? 'border-b-emerald-500/50' : 'border-b-border-strong'}`} />
            <AnimatePresence mode="wait">
              <motion.p
                key={phase + dialogueIndex}
                initial={{ opacity: 0, y: 5 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -5 }}
                className={`text-lg leading-relaxed flex-1 ${phase === 'success' ? 'text-emerald-100 font-medium' : 'text-neutral-300'}`}
              >
                {currentDialogueArray[dialogueIndex]}
              </motion.p>
            </AnimatePresence>

            <div className="flex justify-between items-center mt-4">
              <span className="text-xs text-neutral-500 font-mono">
                MSG {dialogueIndex + 1}/{currentDialogueArray.length}
              </span>
              
              <div className="flex items-center gap-2">
                {dialogueIndex > 0 && (
                  <button 
                    onClick={prevDialogue}
                    className="flex items-center gap-1 text-xs font-bold text-neutral-400 hover:text-neutral-300 bg-white/5 hover:bg-white/10 px-3 py-1.5 rounded-lg transition-all border border-white/5 hover:border-white/10"
                  >
                    <ChevronLeft className="w-3 h-3" /> Prev
                  </button>
                )}
                
                {dialogueIndex < currentDialogueArray.length - 1 ? (
                  <button 
                    onClick={nextDialogue}
                    className="flex items-center gap-1 text-xs font-bold text-amber-400 hover:text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 px-3 py-1.5 rounded-lg transition-all border border-amber-500/20"
                  >
                    Next <ChevronRight className="w-3 h-3" />
                  </button>
                ) : phase === 'success' ? (
                  <button 
                    onClick={nextChapter}
                    className="flex items-center gap-1 text-xs font-bold text-black bg-amber-500 hover:bg-amber-400 px-3 py-1.5 rounded-lg transition-all shadow-[0_0_15px_rgba(245,158,11,0.4)]"
                  >
                    Proceed <ChevronRight className="w-3 h-3" />
                  </button>
                ) : (
                  <motion.div 
                    animate={{ boxShadow: ["0 0 0 rgba(16,185,129,0)", "0 0 15px rgba(16,185,129,0.3)", "0 0 0 rgba(16,185,129,0)"] }}
                    transition={{ duration: 2, repeat: Infinity }}
                    className="flex items-center gap-2 px-4 py-2 bg-emerald-500/10 rounded-lg border border-emerald-500/30 shadow-[inset_0_0_10px_rgba(16,185,129,0.1)]"
                  >
                    <Terminal className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
                    <span className="text-[10px] font-bold text-emerald-400 uppercase tracking-widest">
                      Terminal Ready
                    </span>
                  </motion.div>
                )}
              </div>
            </div>
          </div>

          {/* Current Task Box */}
          {phase === 'input' && (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="relative rounded-xl overflow-hidden group mt-2"
            >
              {/* Sweeping radar border effect */}
              <div className="absolute inset-[-100%] bg-[conic-gradient(from_0deg,transparent_0_300deg,rgba(245,158,11,0.5)_360deg)] animate-[spin_4s_linear_infinite] opacity-30" />
              
              <div className="relative m-[1px] bg-neutral-950/90 backdrop-blur-xl rounded-[11px] p-5 border border-amber-500/20 shadow-[0_0_20px_rgba(245,158,11,0.1)] h-full">
                <div className="absolute top-0 right-0 w-32 h-32 bg-amber-500/5 rounded-full blur-2xl pointer-events-none" />
                
                <h4 className="text-amber-500 text-[10px] font-bold uppercase tracking-[0.2em] mb-4 flex items-center gap-2">
                  <div className="relative flex items-center justify-center">
                    <span className="w-2 h-2 rounded-full bg-amber-500 animate-ping absolute opacity-75" />
                    <span className="w-2 h-2 rounded-full bg-amber-500 relative" />
                  </div>
                  System Directive
                </h4>
                
                <p className="text-white text-sm font-mono leading-relaxed border-l-[3px] border-amber-500/50 pl-4 mb-5 bg-gradient-to-r from-amber-500/5 to-transparent py-1">
                  {currentChapter.task}
                </p>

                <div className="bg-black/80 rounded-lg p-3.5 border border-white/5 flex items-start gap-3 shadow-inner">
                  <Sparkles className="w-4 h-4 text-amber-500/60 mt-0.5 flex-shrink-0 animate-pulse" />
                  <div>
                    <p className="text-[9px] uppercase font-bold tracking-widest text-neutral-500 mb-1">Tactical Hint</p>
                    <p className="text-xs text-neutral-400 italic leading-relaxed">{currentChapter.hint}</p>
                  </div>
                </div>
              </div>
            </motion.div>
          )}
        </div>
      </div>

      {/* Right Side: Code Editor */}
      <div className="w-full lg:w-2/3 flex flex-col z-10 bg-[#1e1e1e] relative">
        <div className="flex items-center justify-between px-4 py-2 border-b border-[#333] bg-[#252526]">
          <select
            value={language}
            onChange={(e) => setLanguage(e.target.value)}
            disabled={phase === 'success'}
            className="bg-[#333] border border-[#444] rounded-md px-3 py-1.5 text-sm text-neutral-300 focus:outline-none focus:border-amber-500 cursor-pointer disabled:opacity-50"
          >
            <option value="java">Java</option>
            <option value="c">C</option>
            <option value="cpp">C++</option>
            <option value="python">Python</option>
            <option value="javascript">JavaScript</option>
          </select>
          
          <div className="flex gap-3">
            <button
              onClick={handleRun}
              disabled={phase === 'success' || isExecuting}
              className="flex items-center gap-2 px-6 py-1.5 rounded-md text-sm font-bold bg-emerald-600 hover:bg-emerald-500 disabled:bg-neutral-700 disabled:text-neutral-500 text-white transition-colors"
            >
              <Play className={`w-4 h-4 ${isExecuting ? 'animate-pulse' : ''}`} /> {isExecuting ? 'Executing...' : 'Execute'}
            </button>
          </div>
        </div>

        <div className="flex-1 relative flex flex-col">
          <div className="flex-1 relative">
            <CodeEditor value={code} onChange={setCode} language={language} height="100%" />
          </div>
          
          {/* Bottom Panel */}
          <div className="h-64 bg-[#1e1e1e] border-t border-[#333] flex flex-col">
            {/* Tabs */}
            <div className="flex bg-[#252526] border-b border-[#333]">
              <button
                onClick={() => setActiveTab('testcases')}
                className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold uppercase tracking-wider transition-colors ${
                  activeTab === 'testcases' ? 'bg-[#1e1e1e] text-white border-t-2 border-t-amber-500' : 'text-neutral-400 hover:text-neutral-300 border-t-2 border-transparent'
                }`}
              >
                <CheckCircle className="w-4 h-4" /> Testcases
              </button>
              <button
                onClick={() => setActiveTab('result')}
                className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold uppercase tracking-wider transition-colors ${
                  activeTab === 'result' ? 'bg-[#1e1e1e] text-emerald-400 border-t-2 border-t-emerald-500' : 'text-neutral-400 hover:text-neutral-300 border-t-2 border-transparent'
                }`}
              >
                <Cpu className="w-4 h-4" /> Test Result
              </button>
              <button
                onClick={() => setActiveTab('console')}
                className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold uppercase tracking-wider transition-colors ${
                  activeTab === 'console' ? 'bg-[#1e1e1e] text-white border-t-2 border-t-amber-500' : 'text-neutral-400 hover:text-neutral-300 border-t-2 border-transparent'
                }`}
              >
                <Terminal className="w-4 h-4" /> Console Output
              </button>
            </div>

            {/* Tab Content */}
            <div className="flex-1 overflow-y-auto bg-[#1e1e1e]">
              {activeTab === 'testcases' && (
                <div className="p-4">
                  <div className="flex gap-2 mb-4">
                    {(currentChapter.testCases || [{name: 'Case 1', input: 'N/A', expectedOutput: 'N/A'}]).map((tc, idx) => (
                      <button
                        key={idx}
                        onClick={() => setSelectedTestCaseIndex(idx)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                          selectedTestCaseIndex === idx ? 'bg-surface-tertiary text-white' : 'bg-surface-secondary text-neutral-400 hover:text-neutral-300'
                        }`}
                      >
                        {tc.name || `Case ${idx + 1}`}
                      </button>
                    ))}
                  </div>
                  {(() => {
                    const tc = (currentChapter.testCases || [{name: 'Case 1', input: 'N/A', expectedOutput: 'N/A'}])[selectedTestCaseIndex] || {};
                    return (
                      <div className="space-y-4">
                        <div>
                          <p className="text-xs text-neutral-400 mb-1 font-semibold">Input:</p>
                          <div className="bg-[#252526] border border-[#333] rounded-lg p-3">
                            <pre className="text-sm font-mono text-neutral-200">{tc.input}</pre>
                          </div>
                        </div>
                        <div>
                          <p className="text-xs text-neutral-400 mb-1 font-semibold">Expected Output:</p>
                          <div className="bg-[#252526] border border-[#333] rounded-lg p-3">
                            <pre className="text-sm font-mono text-neutral-200">{tc.expectedOutput}</pre>
                          </div>
                        </div>
                      </div>
                    );
                  })()}
                </div>
              )}

              {activeTab === 'result' && (
                <div className="p-4">
                  {!testResultStatus && !isExecuting && (
                    <div className="text-neutral-600 italic font-mono text-sm">Run your code to see the test results.</div>
                  )}
                  {isExecuting && (
                    <div className="text-amber-500 animate-pulse font-mono text-sm">Evaluating logic on backend server...</div>
                  )}
                  {testResultStatus === 'accepted' && (
                    <div className="space-y-4">
                      <h2 className="text-2xl font-bold text-emerald-400">Accepted</h2>
                      <div className="text-sm text-neutral-300">Your solution successfully passed all the logic constraints for this chapter!</div>
                    </div>
                  )}
                  {testResultStatus === 'failed' && (
                    <div className="space-y-4">
                      <h2 className="text-2xl font-bold text-red-500">Wrong Answer</h2>
                      {executionError ? (
                        <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-3">
                          <pre className="text-sm font-mono text-red-400 whitespace-pre-wrap">{executionError}</pre>
                        </div>
                      ) : (
                        <div className="bg-[#252526] border border-[#333] rounded-lg p-3">
                          <p className="text-xs text-neutral-400 mb-2 font-semibold">Validation Failure:</p>
                          <p className="text-sm text-red-400 font-mono">{analysisError || "Actual output did not match expected requirements."}</p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'console' && (
                <div className="p-4 font-mono text-sm">
                  {!executionOutput && !executionError && !isExecuting && (
                    <div className="text-neutral-600 italic">No output yet. Write some code and hit Execute.</div>
                  )}
                  {isExecuting && (
                    <div className="text-amber-500 animate-pulse">Running code on backend server...</div>
                  )}
                  {executionOutput && (
                    <pre className="text-neutral-300 whitespace-pre-wrap">{executionOutput}</pre>
                  )}
                  {executionError && (
                    <pre className="text-red-400 whitespace-pre-wrap">{executionError}</pre>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Error Popup Modal */}
      <AnimatePresence>
        {showErrorPopup && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={() => setShowErrorPopup(false)}>
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-surface-elevated border border-red-500/30 rounded-xl p-6 w-full max-w-sm shadow-2xl relative"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex flex-col items-center text-center">
                <div className="w-12 h-12 rounded-full bg-red-500/10 flex items-center justify-center mb-4 border border-red-500/20">
                  <XCircle className="w-6 h-6 text-red-500" />
                </div>
                <h3 className="text-xl font-bold text-white mb-2">Analysis Failed</h3>
                <p className="text-neutral-400 text-sm mb-6">
                  {analysisError}
                </p>
                <button
                  onClick={() => setShowErrorPopup(false)}
                  className="w-full py-2.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 border border-red-500/20 text-red-400 font-semibold transition-colors"
                >
                  Close & Try Again
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
