import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Swords, Trophy, Clock, Users, Shield, Zap, CheckCircle, XCircle, ChevronRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { battleApi } from '../api/client';
import api from '../api/client';

export default function BattleHistory() {
  const { user, isAuthenticated } = useAuth();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ total: 0, wins: 0, teamWins: 0, avgScore: 0 });

  useEffect(() => {
    if (!isAuthenticated) return;
    // Fetch battle history from profile or battles endpoint
    api.get('/profile/battles').then(({ data }) => {
      setHistory(data.battles || []);
      const wins = (data.battles || []).filter((b) => b.myRank === 1).length;
      const teamWins = (data.battles || []).filter((b) => b.battleMode === 'TEAM' && b.myTeamRank === 1).length;
      const scores = (data.battles || []).map((b) => b.myScore || 0);
      const avg = scores.length > 0 ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) : 0;
      setStats({ total: data.battles?.length || 0, wins, teamWins, avgScore: avg });
    }).catch(() => {
      setHistory([]);
    }).finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-16 text-center">
        <Swords className="w-12 h-12 text-neutral-700 mx-auto mb-4" />
        <h1 className="text-xl font-bold text-white mb-2">Battle History</h1>
        <p className="text-neutral-500 text-sm mb-6">Sign in to see your battle history</p>
        <Link to="/login" className="px-6 py-2.5 rounded-lg bg-amber-500 text-black font-semibold text-sm hover:bg-amber-600 transition-colors">
          Sign In
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2 mb-1">
          <Swords className="w-6 h-6 text-amber-400" /> Battle History
        </h1>
        <p className="text-sm text-neutral-500">Your competitive coding battle record</p>
      </div>

      {/* Stats cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Total Battles', value: stats.total, icon: Swords, color: 'text-amber-400' },
          { label: 'Solo Wins', value: stats.wins, icon: Trophy, color: 'text-emerald-400' },
          { label: 'Team Wins', value: stats.teamWins, icon: Shield, color: 'text-purple-400' },
          { label: 'Avg Score', value: stats.avgScore, icon: Zap, color: 'text-blue-400' },
        ].map((s) => (
          <div key={s.label} className="bg-surface-secondary border border-border rounded-xl p-4">
            <s.icon className={`w-4 h-4 ${s.color} mb-2`} />
            <p className={`text-2xl font-bold ${s.color}`}>{s.value}</p>
            <p className="text-xs text-neutral-500 mt-0.5">{s.label}</p>
          </div>
        ))}
      </div>

      {/* History list */}
      {loading ? (
        <div className="space-y-3">
          {[1,2,3].map((i) => (
            <div key={i} className="bg-surface-secondary border border-border rounded-xl p-4 animate-pulse">
              <div className="h-4 bg-surface-tertiary rounded w-1/3 mb-2" />
              <div className="h-3 bg-surface-tertiary rounded w-1/2" />
            </div>
          ))}
        </div>
      ) : history.length === 0 ? (
        <div className="py-16 text-center">
          <Swords className="w-10 h-10 text-neutral-700 mx-auto mb-3" />
          <p className="text-neutral-500 text-sm mb-4">No battles yet — jump in and compete!</p>
          <Link to="/battleground" className="px-5 py-2 rounded-lg bg-amber-500 text-black font-semibold text-sm hover:bg-amber-600 transition-colors">
            Go to BattleGround
          </Link>
        </div>
      ) : (
        <div className="space-y-3">
          {history.map((battle, i) => {
            const won = battle.battleMode === 'TEAM' ? battle.myTeamRank === 1 : battle.myRank === 1;
            return (
              <motion.div
                key={battle.id || i}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
                className={`bg-surface-secondary border rounded-xl p-4 flex items-center justify-between hover:border-neutral-600 transition-all ${
                  won ? 'border-emerald-500/20 bg-emerald-500/3' : 'border-border'
                }`}
              >
                <div className="flex items-center gap-4">
                  <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${
                    won ? 'bg-emerald-500/20 text-emerald-400' : 'bg-surface-tertiary text-neutral-500'
                  }`}>
                    {won ? <Trophy className="w-5 h-5" /> : <XCircle className="w-5 h-5" />}
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-0.5">
                      <span className="text-sm font-semibold text-white font-mono">{battle.roomCode || 'BATTLE'}</span>
                      {battle.battleMode === 'TEAM' && (
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-purple-500/20 text-purple-400 font-medium border border-purple-500/20">Team</span>
                      )}
                      {won && <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-medium border border-emerald-500/20">Won</span>}
                    </div>
                    <div className="flex items-center gap-3 text-xs text-neutral-500">
                      <span className="flex items-center gap-1"><Users className="w-3 h-3" /> {battle.participantCount || '?'} players</span>
                      <span className="flex items-center gap-1"><Clock className="w-3 h-3" /> {battle.durationMinutes || 30}min</span>
                      {battle.completedAt && <span>{new Date(battle.completedAt).toLocaleDateString()}</span>}
                    </div>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-lg font-bold font-mono text-amber-400">{battle.myScore || 0}</p>
                  <p className="text-xs text-neutral-500">
                    Rank #{battle.battleMode === 'TEAM' ? battle.myTeamRank : battle.myRank || '?'}
                  </p>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}
