import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { User, Code2, Trophy, Swords, Flame, Target, TrendingUp } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { submissionApi } from '../api/client';

export default function Profile() {
  const { user, isAuthenticated } = useAuth();
  const [stats, setStats] = useState({ totalSolved: 0, easySolved: 0, mediumSolved: 0, hardSolved: 0, streak: 0 });
  const [heatmapData, setHeatmapData] = useState({});

  useEffect(() => {
    if (isAuthenticated) {
      submissionApi.getMyStats().then(({ data }) => {
        if (data) setStats(data);
      });
      submissionApi.getHeatmap().then(({ data }) => {
        if (data) setHeatmapData(data);
      });
    }
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="max-w-md mx-auto px-4 py-20 text-center">
        <div className="w-16 h-16 rounded-2xl bg-amber-500/10 flex items-center justify-center mx-auto mb-4">
          <User className="w-8 h-8 text-amber-400" />
        </div>
        <h2 className="text-xl font-bold text-white mb-2">Sign in to view profile</h2>
        <p className="text-sm text-neutral-500 mb-6">Track your progress, submissions, and rating.</p>
        <Link
          to="/login"
          className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-semibold text-sm transition-all"
        >
          Sign In
        </Link>
      </div>
    );
  }

  const diffData = [
    { label: 'Easy', count: stats.easySolved, color: 'bg-emerald-500', barColor: 'bg-emerald-500/20' },
    { label: 'Medium', count: stats.mediumSolved, color: 'bg-amber-500', barColor: 'bg-amber-500/20' },
    { label: 'Hard', count: stats.hardSolved, color: 'bg-orange-500', barColor: 'bg-orange-500/20' },
  ];

  const maxDiff = Math.max(stats.easySolved, stats.mediumSolved, stats.hardSolved, 1);

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Profile header */}
      <div className="bg-surface-secondary border border-border rounded-xl p-6 mb-6">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-amber-500/10 flex items-center justify-center shrink-0">
            <span className="text-2xl font-bold text-amber-400">
              {user.username?.charAt(0).toUpperCase()}
            </span>
          </div>
          <div>
            <h1 className="text-xl font-bold text-white">{user.username}</h1>
            <p className="text-sm text-neutral-500">{user.email}</p>
            <div className="flex items-center gap-3 mt-1.5">
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-amber-500/10 text-amber-400 text-xs font-semibold">
                <Trophy className="w-3 h-3" /> {user.rating} ELO
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
        {[
          { icon: Target, label: 'Solved', value: stats.totalSolved, color: 'text-emerald-400' },
          { icon: Flame, label: 'Streak', value: `${stats.streak}d`, color: 'text-orange-400' },
          { icon: TrendingUp, label: 'Rating', value: user.rating, color: 'text-amber-400' },
          { icon: Swords, label: 'Battles Won', value: user.battlesWon || 0, color: 'text-purple-400' },
        ].map(({ icon: Icon, label, value, color }) => (
          <div key={label} className="bg-surface-secondary border border-border rounded-xl p-4">
            <Icon className={`w-4 h-4 ${color} mb-2`} />
            <p className="text-2xl font-extrabold text-white tracking-tight">{value}</p>
            <p className="text-[10px] text-neutral-600 uppercase tracking-wider mt-0.5">{label}</p>
          </div>
        ))}
      </div>

      {/* Difficulty breakdown */}
      <div className="bg-surface-secondary border border-border rounded-xl p-6 mb-6">
        <h3 className="text-sm font-semibold text-white mb-4">Problems by Difficulty</h3>
        <div className="space-y-3">
          {diffData.map(({ label, count, color, barColor }) => (
            <div key={label}>
              <div className="flex items-center justify-between text-xs mb-1">
                <span className="text-neutral-400">{label}</span>
                <span className="text-white font-semibold">{count}</span>
              </div>
              <div className={`h-2 rounded-full ${barColor} overflow-hidden`}>
                <motion.div
                  className={`h-full rounded-full ${color}`}
                  initial={{ width: 0 }}
                  animate={{ width: `${(count / maxDiff) * 100}%` }}
                  transition={{ duration: 0.8, ease: 'easeOut', delay: 0.2 }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Activity heatmap */}
      <div className="bg-surface-secondary border border-border rounded-xl p-6">
        <h3 className="text-sm font-semibold text-white mb-4">Activity Heatmap</h3>
        <div className="grid grid-cols-[repeat(52,1fr)] gap-0.5">
          {Array.from({ length: 364 }, (_, i) => {
            const d = new Date();
            d.setDate(d.getDate() - (363 - i));
            const dateStr = d.toISOString().split('T')[0];
            const count = heatmapData[dateStr] || 0;
            
            let bg = 'bg-surface-tertiary';
            if (count >= 5) bg = 'bg-emerald-500';
            else if (count >= 3) bg = 'bg-emerald-500/60';
            else if (count === 2) bg = 'bg-emerald-500/30';
            else if (count === 1) bg = 'bg-emerald-500/10';
            
            return (
              <div 
                key={i} 
                className={`aspect-square rounded-[2px] ${bg} hover:ring-1 hover:ring-white/50 transition-all cursor-crosshair`}
                title={`${dateStr}: ${count} submissions`}
              />
            );
          })}
        </div>
        <div className="flex items-center gap-2 mt-3 justify-end">
          <span className="text-[10px] text-neutral-600">Less</span>
          {['bg-surface-tertiary', 'bg-emerald-500/10', 'bg-emerald-500/30', 'bg-emerald-500/60', 'bg-emerald-500'].map((c, i) => (
            <div key={i} className={`w-2.5 h-2.5 rounded-[2px] ${c}`} />
          ))}
          <span className="text-[10px] text-neutral-600">More</span>
        </div>
      </div>

      {/* Quick links */}
      <div className="mt-6 flex gap-3">
        <Link
          to="/submissions"
          className="flex-1 text-center py-3 rounded-xl bg-surface-secondary border border-border text-sm text-neutral-300 hover:text-white hover:border-neutral-600 transition-colors"
        >
          View Submissions
        </Link>
        <Link
          to="/problems"
          className="flex-1 text-center py-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-sm text-amber-400 hover:bg-amber-500/15 transition-colors"
        >
          Solve More Problems
        </Link>
      </div>
    </div>
  );
}
