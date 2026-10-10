import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Swords, Plus, Users, X, Clock, Shield, Zap } from 'lucide-react';
import toast from 'react-hot-toast';
import { battleApi, problemApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

const MODE_COLORS = {
  SOLO: 'text-amber-400 bg-amber-500/10 border-amber-500/20',
  TEAM: 'text-purple-400 bg-purple-500/10 border-purple-500/20',
};

export default function BattleGround() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [activeBattles, setActiveBattles] = useState([]);
  const [problems, setProblems] = useState([]);
  const [showCreate, setShowCreate] = useState(false);
  const [roomCode, setRoomCode] = useState('');
  const [createForm, setCreateForm] = useState({
    problemIds: [],
    durationMinutes: 30,
    battleMode: 'SOLO',
    teamsCount: 2,
    teamSize: 5,
  });
  const [selectedTopic, setSelectedTopic] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    battleApi.getActive().then(({ data }) => setActiveBattles(data)).catch(() => {});
    problemApi.getAll().then(({ data }) => setProblems(data)).catch(() => {});
  }, []);

  const handleCreate = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    if (createForm.problemIds.length === 0) {
      toast.error('Select at least one problem');
      return;
    }
    setLoading(true);
    try {
      const { data } = await battleApi.create({
        problemIds: createForm.problemIds.map(Number),
        durationMinutes: createForm.durationMinutes,
        battleMode: createForm.battleMode,
        teamsCount: createForm.battleMode === 'TEAM' ? createForm.teamsCount : undefined,
        teamSize: createForm.battleMode === 'TEAM' ? createForm.teamSize : undefined,
      });
      navigate(`/battleground/${data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to create battle');
    } finally {
      setLoading(false);
    }
  };

  const handleJoin = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    if (!roomCode.trim()) { toast.error('Enter a room code'); return; }
    setLoading(true);
    try {
      const { data } = await battleApi.join(roomCode.toUpperCase());
      navigate(`/battleground/${data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to join battle');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2 mb-1">
            <Swords className="w-6 h-6 text-amber-400" /> BattleGround
          </h1>
          <p className="text-sm text-neutral-500">Compete live — highest score wins rating points</p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-amber-500 hover:bg-amber-600 text-black text-sm font-semibold transition-all hover:-translate-y-0.5"
        >
          <Plus className="w-4 h-4" /> Create Battle
        </button>
      </div>

      {/* Join with code */}
      <div className="bg-surface-secondary border border-border rounded-xl p-5 mb-8">
        <h3 className="text-sm font-semibold text-white mb-3">Join with Room Code</h3>
        <div className="flex gap-3">
          <input
            value={roomCode}
            onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
            onKeyDown={(e) => e.key === 'Enter' && handleJoin()}
            placeholder="Enter room code (e.g. ABC12345)"
            className="flex-1 px-3.5 py-2.5 rounded-lg bg-surface-tertiary border border-border text-sm text-white font-mono uppercase placeholder-neutral-600 focus:outline-none focus:border-amber-500/50 transition-colors"
          />
          <button
            onClick={handleJoin}
            disabled={loading || !roomCode}
            className="px-5 py-2.5 rounded-lg border border-neutral-700 text-sm text-neutral-300 font-medium hover:bg-surface-hover transition-colors disabled:opacity-50"
          >
            {loading ? 'Joining...' : 'Join'}
          </button>
        </div>
      </div>

      {/* Active battles */}
      <div className="flex items-center gap-3 mb-4">
        <h2 className="text-sm font-semibold text-neutral-400 uppercase tracking-wider">Open Battles</h2>
        <span className="text-xs px-2 py-0.5 rounded-full bg-surface-tertiary text-neutral-500 border border-border">
          {activeBattles.length}
        </span>
      </div>

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
        {activeBattles.length === 0 ? (
          <div className="col-span-full py-12 text-center">
            <Swords className="w-8 h-8 text-neutral-700 mx-auto mb-2" />
            <p className="text-neutral-600 text-sm">No open battles. Create one to start competing!</p>
          </div>
        ) : (
          activeBattles.map((battle, i) => (
            <motion.div
              key={battle.id}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05 }}
            >
              <Link
                to={`/battleground/${battle.id}`}
                className="block bg-surface-secondary border border-border rounded-xl p-4 hover:border-neutral-600 transition-all group"
              >
                <div className="flex items-center justify-between mb-3">
                  <span className="font-mono text-amber-400 text-sm font-semibold">{battle.roomCode}</span>
                  <div className="flex items-center gap-1.5">
                    {battle.battleMode === 'TEAM' && (
                      <span className="text-[10px] px-2 py-0.5 rounded-full font-semibold uppercase bg-purple-500/10 text-purple-400 border border-purple-500/20">
                        Team
                      </span>
                    )}
                    <span className={`text-[10px] px-2 py-0.5 rounded-full font-semibold uppercase ${
                      battle.status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                      'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                    }`}>{battle.status}</span>
                  </div>
                </div>
                <p className="text-sm font-medium text-neutral-200 group-hover:text-amber-400 transition-colors mb-2">
                  {battle.problems?.length > 1
                    ? `${battle.problems.length} Questions (${battle.problems[0]?.topic || 'Mixed'})`
                    : battle.problems?.[0]?.title || 'No questions'}
                </p>
                <div className="flex items-center gap-4 text-xs text-neutral-500">
                  <span className="flex items-center gap-1">
                    <Users className="w-3 h-3" /> {battle.participantCount ?? 0}
                    {battle.battleMode === 'TEAM' && battle.teamsCount && (
                      <span className="text-neutral-600">· {battle.teamsCount} teams</span>
                    )}
                  </span>
                  <span className="flex items-center gap-1">
                    <Clock className="w-3 h-3" /> {battle.durationMinutes || 30}min
                  </span>
                </div>
              </Link>
            </motion.div>
          ))
        )}
      </div>

      {/* Create Modal */}
      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={() => setShowCreate(false)}>
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-surface-elevated border border-border-strong rounded-xl p-6 w-full max-w-lg shadow-2xl max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-bold text-white">Create Battle</h3>
              <button onClick={() => setShowCreate(false)} className="p-1.5 rounded-md hover:bg-surface-hover text-neutral-500 transition-colors">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-4">
              {/* Battle Mode toggle */}
              <div>
                <label className="block text-xs font-medium text-neutral-400 mb-2">Battle Mode</label>
                <div className="grid grid-cols-2 gap-2">
                  {['SOLO', 'TEAM'].map((mode) => (
                    <button
                      key={mode}
                      onClick={() => setCreateForm({ ...createForm, battleMode: mode })}
                      className={`py-2.5 rounded-lg text-sm font-semibold border transition-all flex items-center justify-center gap-2 ${
                        createForm.battleMode === mode
                          ? mode === 'TEAM'
                            ? 'bg-purple-500/15 border-purple-500/50 text-purple-400'
                            : 'bg-amber-500/15 border-amber-500/50 text-amber-400'
                          : 'border-border text-neutral-500 hover:border-neutral-600'
                      }`}
                    >
                      {mode === 'SOLO' ? <Zap className="w-4 h-4" /> : <Shield className="w-4 h-4" />}
                      {mode === 'SOLO' ? 'Solo Battle' : 'Team Battle'}
                    </button>
                  ))}
                </div>
                <p className="text-[11px] text-neutral-600 mt-1.5">
                  {createForm.battleMode === 'TEAM'
                    ? '🛡 Teams compete — team score = sum of all member scores'
                    : '⚡ Everyone competes individually — highest score wins'}
                </p>
              </div>

              {/* Team settings (only for TEAM mode) */}
              {createForm.battleMode === 'TEAM' && (
                <div className="grid grid-cols-2 gap-3 p-3 bg-purple-500/5 border border-purple-500/20 rounded-lg">
                  <div>
                    <label className="block text-xs font-medium text-neutral-400 mb-1.5">Number of Teams</label>
                    <select
                      value={createForm.teamsCount}
                      onChange={(e) => setCreateForm({ ...createForm, teamsCount: Number(e.target.value) })}
                      className="w-full px-3 py-2 rounded-lg bg-surface-secondary border border-border text-sm text-neutral-300 focus:outline-none focus:border-purple-500/50"
                    >
                      {[2, 3, 4, 5, 6, 7, 8].map((n) => (
                        <option key={n} value={n}>{n} Teams</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-neutral-400 mb-1.5">Players per Team</label>
                    <select
                      value={createForm.teamSize}
                      onChange={(e) => setCreateForm({ ...createForm, teamSize: Number(e.target.value) })}
                      className="w-full px-3 py-2 rounded-lg bg-surface-secondary border border-border text-sm text-neutral-300 focus:outline-none focus:border-purple-500/50"
                    >
                      {[2, 3, 4, 5, 6, 8, 10].map((n) => (
                        <option key={n} value={n}>{n} Players</option>
                      ))}
                    </select>
                  </div>
                </div>
              )}

              {/* Topic filter */}
              <div>
                <label className="block text-xs font-medium text-neutral-400 mb-1.5">Filter by Topic</label>
                <select
                  value={selectedTopic}
                  onChange={(e) => setSelectedTopic(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-lg bg-surface-secondary border border-border text-sm text-neutral-300 focus:outline-none focus:border-amber-500/50 cursor-pointer"
                >
                  <option value="">All Topics</option>
                  {[...new Set(problems.map((p) => p.topic).filter(Boolean))].map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>

              {/* Problems list */}
              <div>
                <label className="block text-xs font-medium text-neutral-400 mb-1.5">
                  Select Problems ({createForm.problemIds.length} selected)
                </label>
                <div className="max-h-48 overflow-y-auto border border-border rounded-lg bg-surface-secondary p-2 space-y-1">
                  {problems
                    .filter((p) => !selectedTopic || p.topic === selectedTopic)
                    .map((p) => (
                      <label key={p.id} className="flex items-center gap-3 p-2 rounded hover:bg-surface-tertiary cursor-pointer transition-colors">
                        <input
                          type="checkbox"
                          checked={createForm.problemIds.includes(p.id)}
                          onChange={(e) => {
                            const newIds = e.target.checked
                              ? [...createForm.problemIds, p.id]
                              : createForm.problemIds.filter((id) => id !== p.id);
                            setCreateForm({ ...createForm, problemIds: newIds });
                          }}
                          className="w-4 h-4 rounded border-neutral-600 text-amber-500 focus:ring-amber-500 bg-surface-tertiary"
                        />
                        <span className="text-sm text-neutral-200 flex-1">{p.title}</span>
                        <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${
                          p.difficulty === 'EASY' ? 'text-emerald-400 bg-emerald-400/10' :
                          p.difficulty === 'MEDIUM' ? 'text-amber-400 bg-amber-400/10' :
                          'text-red-400 bg-red-400/10'
                        }`}>{p.difficulty}</span>
                      </label>
                    ))}
                </div>
              </div>

              {/* Duration */}
              <div>
                <label className="block text-xs font-medium text-neutral-400 mb-1.5">Duration (minutes)</label>
                <div className="flex gap-2">
                  {[15, 30, 45, 60, 90].map((d) => (
                    <button
                      key={d}
                      onClick={() => setCreateForm({ ...createForm, durationMinutes: d })}
                      className={`flex-1 py-2 rounded-lg text-xs font-semibold border transition-all ${
                        createForm.durationMinutes === d
                          ? 'bg-amber-500/15 border-amber-500/50 text-amber-400'
                          : 'border-border text-neutral-500 hover:border-neutral-600'
                      }`}
                    >
                      {d}m
                    </button>
                  ))}
                </div>
              </div>

              <button
                onClick={handleCreate}
                disabled={loading || createForm.problemIds.length === 0}
                className="w-full py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-semibold text-sm transition-all disabled:opacity-50"
              >
                {loading ? 'Creating...' : `Create ${createForm.battleMode === 'TEAM' ? 'Team ' : ''}Battle & Enter`}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
}
