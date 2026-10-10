import { useEffect, useState, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Zap, Timer, Trophy, RefreshCw, ChevronRight, Star, X, Play } from 'lucide-react';
import toast from 'react-hot-toast';
import { problemApi, submissionApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import CodeEditor from '../components/CodeEditor';

const SPEED_DURATION = 60; // seconds

export default function SpeedMode() {
  const { isAuthenticated } = useAuth();
  const [problems, setProblems] = useState([]);
  const [currentProblem, setCurrentProblem] = useState(null);
  const [code, setCode] = useState('');
  const [language] = useState('java');
  const [phase, setPhase] = useState('intro'); // intro | playing | result | gameover
  const [timeLeft, setTimeLeft] = useState(SPEED_DURATION);
  const [score, setScore] = useState(0);
  const [streak, setStreak] = useState(0);
  const [solved, setSolved] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [lastResult, setLastResult] = useState(null);
  const timerRef = useRef(null);

  useEffect(() => {
    problemApi.getAll()
      .then(({ data }) => {
        // Only use EASY problems for Speed Mode
        setProblems(data.filter((p) => p.difficulty === 'EASY'));
      })
      .catch(() => {});
    return () => clearInterval(timerRef.current);
  }, []);

  const getRandomProblem = useCallback((exclude = []) => {
    const available = problems.filter((p) => !exclude.includes(p.id));
    if (available.length === 0) return problems[Math.floor(Math.random() * problems.length)];
    return available[Math.floor(Math.random() * available.length)];
  }, [problems]);

  const startGame = () => {
    if (problems.length === 0) { toast.error('No easy problems available'); return; }
    const first = getRandomProblem();
    setCurrentProblem(first);
    setTimeLeft(SPEED_DURATION);
    setScore(0);
    setStreak(0);
    setSolved([]);
    setCode('');
    setLastResult(null);
    setPhase('playing');

    problemApi.getStarterCode(first.id, language).then(({ data }) => setCode(data.code || ''));

    timerRef.current = setInterval(() => {
      setTimeLeft((t) => {
        if (t <= 1) {
          clearInterval(timerRef.current);
          setPhase('gameover');
          return 0;
        }
        return t - 1;
      });
    }, 1000);
  };

  const handleSkip = () => {
    setStreak(0);
    const next = getRandomProblem(solved);
    setCurrentProblem(next);
    setCode('');
    problemApi.getStarterCode(next.id, language).then(({ data }) => setCode(data.code || ''));
    toast('Skipped! (-10 pts)', { icon: '⏭' });
    setScore((s) => Math.max(0, s - 10));
  };

  const handleSubmit = async () => {
    if (!currentProblem) return;
    setSubmitting(true);
    try {
      const { data } = await submissionApi.submit({
        problemId: currentProblem.id,
        code,
        language,
      });
      setLastResult(data);
      if (data.status === 'ACCEPTED') {
        const bonus = Math.max(10, Math.floor(timeLeft / 5));
        const streakBonus = streak >= 2 ? streak * 20 : 0;
        const gained = 100 + bonus + streakBonus;
        setScore((s) => s + gained);
        setStreak((s) => s + 1);
        setSolved((prev) => [...prev, currentProblem.id]);
        toast.success(`+${gained} pts! ${streakBonus > 0 ? `🔥 Streak x${streak + 1}` : ''}`);

        // Load next problem
        setTimeout(() => {
          const next = getRandomProblem([...solved, currentProblem.id]);
          setCurrentProblem(next);
          setCode('');
          setLastResult(null);
          problemApi.getStarterCode(next.id, language).then(({ data }) => setCode(data.code || ''));
        }, 1500);
      } else {
        setStreak(0);
        toast.error(`Wrong answer — try again!`);
      }
    } catch (err) {
      toast.error('Submission failed');
    } finally {
      setSubmitting(false);
    }
  };

  const timerColor = timeLeft > 30 ? 'text-emerald-400' : timeLeft > 10 ? 'text-amber-400' : 'text-red-400';
  const timerPct = (timeLeft / SPEED_DURATION) * 100;

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2 mb-1">
            <Zap className="w-6 h-6 text-amber-400" /> Speed Mode
          </h1>
          <p className="text-sm text-neutral-500">Solve as many EASY problems as possible in {SPEED_DURATION} seconds</p>
        </div>
        {phase === 'playing' && (
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-xs text-neutral-500 mb-0.5">Score</p>
              <p className="text-2xl font-bold text-amber-400">{score}</p>
            </div>
            {streak >= 2 && (
              <div className="px-3 py-1.5 rounded-lg bg-orange-500/20 border border-orange-500/30 text-orange-400 text-sm font-bold flex items-center gap-1">
                🔥 x{streak}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Intro screen */}
      {phase === 'intro' && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-lg mx-auto text-center py-16"
        >
          <div className="w-20 h-20 rounded-2xl bg-amber-500/20 flex items-center justify-center mx-auto mb-6 border border-amber-500/30">
            <Zap className="w-10 h-10 text-amber-400" />
          </div>
          <h2 className="text-2xl font-bold text-white mb-3">Ready to Speed Code?</h2>
          <div className="space-y-2 mb-8 text-sm text-neutral-400">
            <p>⏱ <strong className="text-neutral-300">60 seconds</strong> on the clock</p>
            <p>✅ Each correct solution = <strong className="text-neutral-300">100+ points</strong></p>
            <p>🔥 <strong className="text-neutral-300">Streak bonuses</strong> for consecutive solves</p>
            <p>⚡ All problems are <strong className="text-emerald-400">EASY</strong> difficulty</p>
            <p>⏭ Skip anytime — costs <strong className="text-red-400">10 points</strong></p>
          </div>
          {!isAuthenticated && (
            <p className="text-xs text-amber-500 mb-4">⚠ Login to save your score</p>
          )}
          <button
            onClick={startGame}
            disabled={problems.length === 0}
            className="px-8 py-3 rounded-xl bg-amber-500 hover:bg-amber-600 text-black font-bold text-lg transition-all hover:scale-105 disabled:opacity-50 flex items-center gap-2 mx-auto"
          >
            <Play className="w-5 h-5" /> Start Speed Mode
          </button>
        </motion.div>
      )}

      {/* Playing screen */}
      {phase === 'playing' && currentProblem && (
        <div className="space-y-4">
          {/* Timer bar */}
          <div className="relative">
            <div className="h-2 bg-surface-tertiary rounded-full overflow-hidden">
              <motion.div
                className={`h-full rounded-full ${timeLeft > 30 ? 'bg-emerald-500' : timeLeft > 10 ? 'bg-amber-500' : 'bg-red-500'}`}
                style={{ width: `${timerPct}%` }}
                transition={{ duration: 0.5 }}
              />
            </div>
            <div className="absolute right-0 -top-6 flex items-center gap-1.5">
              <Timer className="w-4 h-4 text-neutral-500" />
              <span className={`text-lg font-bold font-mono ${timerColor}`}>{timeLeft}s</span>
            </div>
          </div>

          <div className="grid lg:grid-cols-2 gap-4">
            {/* Problem panel */}
            <div className="bg-surface-secondary border border-border rounded-xl p-5">
              <div className="flex items-center justify-between mb-3">
                <div>
                  <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-semibold">EASY</span>
                  <span className="text-xs text-neutral-600 ml-2">#{solved.length + 1}</span>
                </div>
                <button
                  onClick={handleSkip}
                  className="text-xs text-neutral-500 hover:text-neutral-300 flex items-center gap-1 transition-colors"
                >
                  <ChevronRight className="w-3 h-3" /> Skip (-10pts)
                </button>
              </div>
              <h2 className="text-lg font-bold text-white mb-3">{currentProblem.title}</h2>
              <p className="text-sm text-neutral-400 leading-relaxed whitespace-pre-wrap">
                {currentProblem.description?.substring(0, 300)}{currentProblem.description?.length > 300 ? '...' : ''}
              </p>

              {lastResult && (
                <div className={`mt-4 p-3 rounded-lg text-sm ${
                  lastResult.status === 'ACCEPTED'
                    ? 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400'
                    : 'bg-red-500/10 border border-red-500/20 text-red-400'
                }`}>
                  {lastResult.status} · {lastResult.testsPassed}/{lastResult.testsTotal} tests
                </div>
              )}
            </div>

            {/* Editor panel */}
            <div className="flex flex-col gap-2">
              <div className="flex-1 border border-border rounded-xl overflow-hidden min-h-[300px]">
                <CodeEditor value={code} onChange={setCode} language={language} height="300px" />
              </div>
              <button
                onClick={handleSubmit}
                disabled={submitting}
                className="w-full py-3 rounded-xl bg-amber-500 hover:bg-amber-600 text-black font-bold text-sm transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                <Zap className="w-4 h-4" />
                {submitting ? 'Checking...' : 'Submit Solution'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Game over screen */}
      {phase === 'gameover' && (
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="max-w-md mx-auto text-center py-12"
        >
          <div className="w-20 h-20 rounded-2xl bg-amber-500/20 flex items-center justify-center mx-auto mb-6 border border-amber-500/30">
            <Trophy className="w-10 h-10 text-amber-400" />
          </div>
          <h2 className="text-3xl font-bold text-white mb-2">Time's Up!</h2>
          <p className="text-5xl font-bold text-amber-400 mb-1">{score}</p>
          <p className="text-neutral-500 text-sm mb-6">points in {SPEED_DURATION}s</p>

          <div className="grid grid-cols-3 gap-3 mb-8">
            {[
              { label: 'Solved', value: solved.length, icon: '✅' },
              { label: 'Best Streak', value: streak, icon: '🔥' },
              { label: 'Avg pts', value: solved.length > 0 ? Math.round(score / solved.length) : 0, icon: '⚡' },
            ].map((s) => (
              <div key={s.label} className="bg-surface-secondary border border-border rounded-xl p-3">
                <p className="text-xl mb-1">{s.icon}</p>
                <p className="text-xl font-bold text-white">{s.value}</p>
                <p className="text-xs text-neutral-500">{s.label}</p>
              </div>
            ))}
          </div>

          <button
            onClick={startGame}
            className="px-6 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-black font-bold transition-all flex items-center gap-2 mx-auto"
          >
            <RefreshCw className="w-4 h-4" /> Play Again
          </button>
        </motion.div>
      )}
    </div>
  );
}
