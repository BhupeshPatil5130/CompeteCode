import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Trophy, Medal, Search, Users, TrendingUp } from 'lucide-react';
import { leaderboardApi } from '../api/client';
import LoadingSpinner from '../components/LoadingSpinner';

export default function Leaderboard() {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    leaderboardApi.getGlobal()
      .then(({ data }) => setEntries(data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner text="Loading leaderboard..." />;

  const filtered = entries.filter(
    (e) => !search || e.username?.toLowerCase().includes(search.toLowerCase())
  );

  const top3 = entries.slice(0, 3);
  const rest = filtered.slice(entries.length >= 3 ? 0 : 0);

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2 mb-1">
          <Trophy className="w-6 h-6 text-amber-400" /> Leaderboard
        </h1>
        <p className="text-sm text-neutral-500">ELO-based ranking from battles and problem solving</p>
      </div>

      {/* Top 3 Podium */}
      {entries.length >= 3 && (
        <div className="grid grid-cols-3 gap-3 mb-8">
          {[1, 0, 2].map((idx) => {
            const e = top3[idx];
            if (!e) return null;
            const isGold = idx === 0;
            const isSilver = idx === 1;
            const colors = isGold
              ? 'border-amber-500/30 bg-amber-500/5'
              : isSilver
              ? 'border-neutral-400/30 bg-neutral-400/5'
              : 'border-orange-500/30 bg-orange-500/5';
            const textColor = isGold ? 'text-amber-400' : isSilver ? 'text-neutral-300' : 'text-orange-400';
            const order = idx === 0 ? 'order-2' : idx === 1 ? 'order-1' : 'order-3';

            return (
              <motion.div
                key={e.userId}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: idx * 0.1 }}
                className={`${order} flex flex-col items-center p-5 rounded-xl border ${colors} ${isGold ? 'pt-4' : 'pt-8'}`}
              >
                <div className={`w-12 h-12 rounded-full flex items-center justify-center text-lg font-bold mb-2 ${
                  isGold ? 'bg-amber-500/20 text-amber-400' : isSilver ? 'bg-neutral-400/20 text-neutral-300' : 'bg-orange-500/20 text-orange-400'
                }`}>
                  {e.username?.charAt(0).toUpperCase()}
                </div>
                <p className="font-semibold text-white text-sm">{e.username}</p>
                <p className={`text-xl font-bold font-mono mt-1 ${textColor}`}>{e.rating}</p>
                <p className="text-[10px] text-neutral-600 mt-0.5 uppercase tracking-wider">
                  {isGold ? '1st' : isSilver ? '2nd' : '3rd'} place
                </p>
              </motion.div>
            );
          })}
        </div>
      )}

      {/* Stats row */}
      <div className="grid grid-cols-3 gap-3 mb-6">
        <div className="bg-surface-secondary border border-border rounded-lg p-3 text-center">
          <Users className="w-4 h-4 text-neutral-500 mx-auto mb-1" />
          <p className="text-lg font-bold text-white">{entries.length}</p>
          <p className="text-[10px] text-neutral-600 uppercase">Players</p>
        </div>
        <div className="bg-surface-secondary border border-border rounded-lg p-3 text-center">
          <TrendingUp className="w-4 h-4 text-neutral-500 mx-auto mb-1" />
          <p className="text-lg font-bold text-white">
            {entries.length > 0 ? Math.round(entries.reduce((a, b) => a + (b.rating || 0), 0) / entries.length) : 0}
          </p>
          <p className="text-[10px] text-neutral-600 uppercase">Avg Rating</p>
        </div>
        <div className="bg-surface-secondary border border-border rounded-lg p-3 text-center">
          <Trophy className="w-4 h-4 text-neutral-500 mx-auto mb-1" />
          <p className="text-lg font-bold text-white">{entries[0]?.rating || '—'}</p>
          <p className="text-[10px] text-neutral-600 uppercase">Top Rating</p>
        </div>
      </div>

      {/* Search */}
      <div className="relative mb-4">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-600" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search players..."
          className="w-full pl-9 pr-4 py-2 rounded-lg bg-surface-secondary border border-border text-sm text-white placeholder-neutral-600 focus:outline-none focus:border-amber-500/50 transition-colors"
        />
      </div>

      {/* Table */}
      <div className="bg-surface-secondary border border-border rounded-xl overflow-hidden">
        <div className="grid grid-cols-12 gap-4 px-5 py-2.5 text-[10px] text-neutral-600 uppercase tracking-wider font-medium border-b border-border">
          <div className="col-span-1">Rank</div>
          <div className="col-span-4">Player</div>
          <div className="col-span-2 text-right">Rating</div>
          <div className="col-span-2 text-right">Solved</div>
          <div className="col-span-3 text-right">Battles Won</div>
        </div>

        {filtered.length === 0 ? (
          <p className="p-8 text-center text-neutral-600 text-sm">No players found.</p>
        ) : (
          filtered.map((entry, i) => (
            <motion.div
              key={entry.userId}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: i * 0.02 }}
              className={`grid grid-cols-12 gap-4 px-5 py-3 items-center border-b border-border last:border-0 hover:bg-surface-hover transition-colors ${
                i < 3 && !search ? 'bg-amber-500/[0.02]' : ''
              }`}
            >
              <div className="col-span-1">
                {i < 3 && !search ? (
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold ${
                    i === 0 ? 'bg-amber-500/20 text-amber-400' :
                    i === 1 ? 'bg-neutral-500/20 text-neutral-300' :
                    'bg-orange-500/20 text-orange-400'
                  }`}>
                    {i + 1}
                  </div>
                ) : (
                  <span className="text-xs text-neutral-600 font-mono">{entry.rank ?? i + 1}</span>
                )}
              </div>
              <div className="col-span-4">
                <span className="text-sm font-medium text-neutral-200">{entry.username}</span>
              </div>
              <div className="col-span-2 text-right font-mono text-sm text-amber-400">{entry.rating}</div>
              <div className="col-span-2 text-right text-sm text-neutral-500">{entry.problemsSolved}</div>
              <div className="col-span-3 text-right text-sm text-purple-400">{entry.battlesWon}</div>
            </motion.div>
          ))
        )}
      </div>
    </div>
  );
}
