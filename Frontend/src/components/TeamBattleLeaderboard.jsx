import { motion, AnimatePresence } from 'framer-motion';
import { Users, Trophy, Crown, Shield } from 'lucide-react';

const TEAM_COLORS = {
  red: {
    bg: 'bg-red-500/10',
    border: 'border-red-500/30',
    text: 'text-red-400',
    bar: 'bg-red-500',
    badge: 'bg-red-500/20 text-red-300',
    glow: 'shadow-[0_0_20px_rgba(239,68,68,0.15)]',
  },
  blue: {
    bg: 'bg-blue-500/10',
    border: 'border-blue-500/30',
    text: 'text-blue-400',
    bar: 'bg-blue-500',
    badge: 'bg-blue-500/20 text-blue-300',
    glow: 'shadow-[0_0_20px_rgba(59,130,246,0.15)]',
  },
  green: {
    bg: 'bg-emerald-500/10',
    border: 'border-emerald-500/30',
    text: 'text-emerald-400',
    bar: 'bg-emerald-500',
    badge: 'bg-emerald-500/20 text-emerald-300',
    glow: 'shadow-[0_0_20px_rgba(16,185,129,0.15)]',
  },
  purple: {
    bg: 'bg-purple-500/10',
    border: 'border-purple-500/30',
    text: 'text-purple-400',
    bar: 'bg-purple-500',
    badge: 'bg-purple-500/20 text-purple-300',
    glow: 'shadow-[0_0_20px_rgba(168,85,247,0.15)]',
  },
  orange: {
    bg: 'bg-orange-500/10',
    border: 'border-orange-500/30',
    text: 'text-orange-400',
    bar: 'bg-orange-500',
    badge: 'bg-orange-500/20 text-orange-300',
    glow: 'shadow-[0_0_20px_rgba(249,115,22,0.15)]',
  },
  cyan: {
    bg: 'bg-cyan-500/10',
    border: 'border-cyan-500/30',
    text: 'text-cyan-400',
    bar: 'bg-cyan-500',
    badge: 'bg-cyan-500/20 text-cyan-300',
    glow: 'shadow-[0_0_20px_rgba(6,182,212,0.15)]',
  },
  pink: {
    bg: 'bg-pink-500/10',
    border: 'border-pink-500/30',
    text: 'text-pink-400',
    bar: 'bg-pink-500',
    badge: 'bg-pink-500/20 text-pink-300',
    glow: 'shadow-[0_0_20px_rgba(236,72,153,0.15)]',
  },
  yellow: {
    bg: 'bg-yellow-500/10',
    border: 'border-yellow-500/30',
    text: 'text-yellow-400',
    bar: 'bg-yellow-500',
    badge: 'bg-yellow-500/20 text-yellow-300',
    glow: 'shadow-[0_0_20px_rgba(234,179,8,0.15)]',
  },
};

const DEFAULT_COLOR = {
  bg: 'bg-neutral-500/10',
  border: 'border-neutral-500/30',
  text: 'text-neutral-400',
  bar: 'bg-neutral-500',
  badge: 'bg-neutral-500/20 text-neutral-300',
  glow: '',
};

function getColor(teamColor) {
  return TEAM_COLORS[teamColor?.toLowerCase()] || DEFAULT_COLOR;
}

export default function TeamBattleLeaderboard({ teams = [], currentUserId, onJoinTeam, canJoinTeams = false }) {
  const maxScore = Math.max(...teams.map((t) => t.score || 0), 1);

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2 mb-4">
        <Trophy className="w-4 h-4 text-amber-400" />
        <h2 className="text-sm font-semibold text-white">Team Standings</h2>
      </div>

      <AnimatePresence>
        {teams.map((team, i) => {
          const c = getColor(team.teamColor);
          const myTeam = team.members?.some((m) => m.userId === currentUserId);
          const pct = maxScore > 0 ? Math.round((team.score / maxScore) * 100) : 0;

          return (
            <motion.div
              key={team.teamId}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.08 }}
              className={`rounded-xl border p-4 ${c.bg} ${c.border} ${i === 0 ? c.glow : ''} relative overflow-hidden`}
            >
              {/* Rank ribbon */}
              {i === 0 && (
                <div className={`absolute top-0 right-0 px-2 py-0.5 text-[10px] font-bold uppercase tracking-widest ${c.badge} rounded-bl-lg`}>
                  🥇 Leading
                </div>
              )}

              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold ${c.badge}`}>
                    {i + 1}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className={`font-bold text-sm ${c.text}`}>{team.teamName}</span>
                      {myTeam && (
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-400 font-semibold border border-amber-500/20">
                          You
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-1 mt-0.5">
                      <Users className="w-3 h-3 text-neutral-500" />
                      <span className="text-xs text-neutral-500">{team.members?.length || 0} members</span>
                    </div>
                  </div>
                </div>

                <div className="text-right">
                  <p className={`font-mono text-lg font-bold ${c.text}`}>{team.score || 0}</p>
                  <p className="text-[10px] text-neutral-500">pts</p>
                </div>
              </div>

              {/* Score progress bar */}
              <div className="h-1.5 bg-black/30 rounded-full overflow-hidden mb-3">
                <motion.div
                  className={`h-full ${c.bar} rounded-full`}
                  initial={{ width: 0 }}
                  animate={{ width: `${pct}%` }}
                  transition={{ duration: 0.8, ease: 'easeOut', delay: i * 0.1 }}
                />
              </div>

              {/* Members list */}
              {team.members && team.members.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mb-3">
                  {team.members.map((m) => (
                    <div key={m.userId} className={`flex items-center gap-1 px-2 py-0.5 rounded-md text-xs ${c.badge}`}>
                      {m.userId === currentUserId && <Crown className="w-2.5 h-2.5" />}
                      <span>{m.username}</span>
                      <span className="text-[10px] opacity-70">({m.score || 0})</span>
                    </div>
                  ))}
                </div>
              )}

              {/* Join button */}
              {canJoinTeams && !myTeam && onJoinTeam && (
                <button
                  onClick={() => onJoinTeam(team.teamId)}
                  className={`w-full py-1.5 rounded-lg text-xs font-semibold border ${c.border} ${c.text} hover:${c.bg} transition-colors`}
                >
                  <Shield className="w-3 h-3 inline mr-1" />
                  Join {team.teamName}
                </button>
              )}
            </motion.div>
          );
        })}
      </AnimatePresence>

      {teams.length === 0 && (
        <div className="py-8 text-center">
          <Users className="w-8 h-8 text-neutral-700 mx-auto mb-2" />
          <p className="text-neutral-600 text-sm">No teams yet</p>
        </div>
      )}
    </div>
  );
}
