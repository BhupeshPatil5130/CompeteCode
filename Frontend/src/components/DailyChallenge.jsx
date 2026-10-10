import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Clock, Flame, ArrowRight, Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';
import { problemApi } from '../api/client';
import DifficultyBadge from './DifficultyBadge';

export default function DailyChallenge() {
  const [problem, setProblem] = useState(null);
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    // Pick a "daily" problem based on date seed
    problemApi.getAll().then(({ data }) => {
      if (data.length > 0) {
        const today = new Date();
        const dayOfYear = Math.floor((today - new Date(today.getFullYear(), 0, 0)) / 86400000);
        const idx = dayOfYear % data.length;
        setProblem(data[idx]);
      }
    });
  }, []);

  useEffect(() => {
    const updateTimer = () => {
      const now = new Date();
      const midnight = new Date(now);
      midnight.setHours(24, 0, 0, 0);
      const diff = midnight - now;
      const hours = Math.floor(diff / 3600000);
      const mins = Math.floor((diff % 3600000) / 60000);
      const secs = Math.floor((diff % 60000) / 1000);
      setTimeLeft(`${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`);
    };
    updateTimer();
    const timer = setInterval(updateTimer, 1000);
    return () => clearInterval(timer);
  }, []);

  if (!problem) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-gradient-to-r from-amber-500/5 via-orange-500/5 to-red-500/5 border border-amber-500/15 rounded-xl p-5"
    >
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex-1 min-w-[200px]">
          <div className="flex items-center gap-2 mb-2">
            <div className="w-8 h-8 rounded-lg bg-amber-500/15 flex items-center justify-center">
              <Flame className="w-4 h-4 text-amber-400" />
            </div>
            <span className="text-[10px] font-bold text-amber-400 uppercase tracking-widest">Daily Challenge</span>
          </div>
          <h3 className="text-lg font-bold text-white mb-1.5">{problem.title}</h3>
          <div className="flex items-center gap-3 mb-3">
            <DifficultyBadge difficulty={problem.difficulty} />
            <span className="text-xs text-neutral-500">{problem.topic}</span>
          </div>
          <p className="text-sm text-neutral-400 line-clamp-2 leading-relaxed">
            {problem.description?.slice(0, 150)}...
          </p>
        </div>

        <div className="flex flex-col items-end gap-3">
          <div className="flex items-center gap-1.5 text-xs text-neutral-500">
            <Clock className="w-3.5 h-3.5" />
            <span className="font-mono">{timeLeft}</span>
            <span>remaining</span>
          </div>
          <Link
            to={`/problems/${problem.id}`}
            className="flex items-center gap-2 px-5 py-2 rounded-lg bg-amber-500 hover:bg-amber-600 text-black text-sm font-semibold transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-amber-500/20"
          >
            Solve Now <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </div>
    </motion.div>
  );
}
