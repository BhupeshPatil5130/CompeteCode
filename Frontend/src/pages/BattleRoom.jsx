import { useEffect, useState, useCallback, useRef } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Copy, Play, Trophy, ExternalLink, Clock, Users, Shield, Zap, LogIn } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { battleApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import TeamBattleLeaderboard from '../components/TeamBattleLeaderboard';

export default function BattleRoom() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [battle, setBattle] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [teams, setTeams] = useState([]);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [copied, setCopied] = useState(false);
  const [activeTab, setActiveTab] = useState('individual');
  const [hasJoined, setHasJoined] = useState(false);

  const mapLeaderboard = (data) =>
    (data || []).map((entry, i) => ({
      userId: entry.userId,
      username: entry.username,
      score: entry.score ?? 0,
      rank: entry.rank ?? i + 1,
      teamId: entry.teamId,
      teamName: entry.teamName,
      teamColor: entry.teamColor,
    }));

  const refresh = useCallback(() => {
    battleApi.getById(id)
      .then(({ data }) => {
        setBattle(data);
        if (data.teams && data.teams.length > 0) {
          setTeams(data.teams);
        }
      })
      .catch((err) => {
        const msg = err.response?.data?.error || err.response?.data?.message || 'Failed to load battle';
        setError(msg);
      });
    battleApi.getLeaderboard(id)
      .then(({ data }) => setLeaderboard(mapLeaderboard(data)))
      .catch(() => {});
  }, [id]);

  // Initial load
  useEffect(() => {
    refresh();
    const interval = setInterval(refresh, 10000);
    return () => clearInterval(interval);
  }, [refresh]);

  // Auto-join the battle (only once, only if authenticated)
  useEffect(() => {
    if (!battle?.roomCode || !isAuthenticated || hasJoined) return;
    setHasJoined(true);
    battleApi.join(battle.roomCode)
      .then(() => {
        refresh(); // Refresh to show yourself in the participant list
      })
      .catch((err) => {
        // Ignore "already joined" (could be duplicate join attempt), but show real errors
        const status = err.response?.status;
        const msg = err.response?.data?.error || '';
        if (status === 401) {
          toast.error('Please log in to join battles');
        } else if (status !== 409 && !msg.toLowerCase().includes('already')) {
          // Don't show error for "already in battle"
          console.warn('Join attempt failed:', msg);
        }
      });
  }, [battle?.roomCode, isAuthenticated, hasJoined, refresh]);

  // WebSocket connection
  useEffect(() => {
    const wsUrl = `${window.location.origin}/ws/battle`;
    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/battle/${id}`, (message) => {
          try {
            const payload = JSON.parse(message.body);
            if (payload.participants) {
              setLeaderboard(mapLeaderboard(payload.participants));
            }
            if (payload.teams) {
              setTeams(payload.teams.map((t) => ({
                teamId: t.teamId,
                teamName: t.teamName,
                teamColor: t.teamColor,
                score: t.score ?? 0,
                rank: t.rank ?? 0,
                members: (payload.participants || []).filter((p) => p.teamId === t.teamId),
              })));
            }
            // Reflect status changes live
            if (payload.status) {
              setBattle((prev) => prev ? { ...prev, status: payload.status, endsAt: payload.endsAt } : prev);
            }
          } catch (e) {
            console.warn('WS parse error:', e);
          }
        });
      },
      onStompError: (frame) => {
        console.warn('STOMP error:', frame);
      },
    });
    client.activate();
    return () => {
      try { client.deactivate(); } catch {}
    };
  }, [id]);

  const handleStart = async () => {
    setActionError('');
    try {
      await battleApi.start(id);
      toast.success('Battle started!');
      refresh();
    } catch (err) {
      const msg = err.response?.data?.error || 'Failed to start battle';
      setActionError(msg);
      toast.error(msg);
    }
  };

  const handleComplete = async () => {
    setActionError('');
    try {
      await battleApi.complete(id);
      toast.success('Battle ended!');
      refresh();
    } catch (err) {
      const msg = err.response?.data?.error || 'Failed to complete battle';
      setActionError(msg);
      toast.error(msg);
    }
  };

  const handleJoinTeam = async (teamId) => {
    if (!isAuthenticated) {
      toast.error('Please log in to join a team');
      navigate('/login');
      return;
    }
    try {
      await battleApi.joinTeam(id, teamId);
      toast.success('Joined team!');
      refresh();
    } catch (err) {
      const msg = err.response?.data?.error || 'Failed to join team';
      toast.error(msg);
    }
  };

  const copyCode = () => {
    navigator.clipboard.writeText(battle?.roomCode || '').then(() => {
      setCopied(true);
      toast.success('Room code copied!');
      setTimeout(() => setCopied(false), 2000);
    }).catch(() => {
      toast.error('Failed to copy');
    });
  };

  // Error screen
  if (error) {
    return (
      <div className="max-w-lg mx-auto p-8 text-center">
        <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-6 mb-4">
          <p className="text-red-400 text-sm font-medium mb-1">Error loading battle</p>
          <p className="text-red-300/70 text-xs">{error}</p>
        </div>
        <div className="flex items-center justify-center gap-3">
          <button
            onClick={() => { setError(''); refresh(); }}
            className="px-4 py-2 rounded-lg border border-neutral-700 text-sm text-neutral-300 hover:bg-surface-hover transition-colors"
          >
            Retry
          </button>
          <Link to="/battleground" className="px-4 py-2 rounded-lg bg-amber-500 text-black text-sm font-semibold hover:bg-amber-600 transition-colors">
            Back to BattleGround
          </Link>
        </div>
      </div>
    );
  }

  // Loading screen
  if (!battle) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="w-8 h-8 border-2 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-neutral-500 text-sm">Loading battle room...</p>
        </div>
      </div>
    );
  }

  const isHost = isAuthenticated && user?.userId === battle.host?.id;
  const isTeamBattle = battle.battleMode === 'TEAM';
  const myTeam = isTeamBattle
    ? teams.find((t) => t.members?.some((m) => m.userId === user?.userId))
    : null;

  const tabs = isTeamBattle
    ? [
        { id: 'teams', label: 'Teams', icon: Shield },
        { id: 'individual', label: 'Individual', icon: Users },
      ]
    : [{ id: 'individual', label: 'Leaderboard', icon: Trophy }];

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Login banner for guests */}
      {!isAuthenticated && (
        <motion.div
          initial={{ opacity: 0, y: -8 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-4 flex items-center justify-between p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20"
        >
          <p className="text-sm text-amber-400">
            <span className="font-semibold">👀 Spectating</span> — Log in to join and compete!
          </p>
          <Link
            to={`/login?returnTo=/battleground/${id}`}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-500 text-black text-xs font-bold hover:bg-amber-600 transition-colors"
          >
            <LogIn className="w-3.5 h-3.5" /> Log In
          </Link>
        </motion.div>
      )}

      {actionError && (
        <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400 text-sm">
          {actionError}
        </div>
      )}

      {/* Battle info card */}
      <div className="bg-surface-secondary border border-border rounded-xl p-6 mb-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-2 flex-wrap">
              <span className={`text-[10px] px-2 py-0.5 rounded-full font-semibold uppercase ${
                battle.status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                battle.status === 'COMPLETED' ? 'bg-neutral-500/10 text-neutral-400 border border-neutral-500/20' :
                'bg-amber-500/10 text-amber-400 border border-amber-500/20'
              }`}>{battle.status}</span>

              {isTeamBattle ? (
                <span className="text-[10px] px-2 py-0.5 rounded-full font-semibold uppercase bg-purple-500/10 text-purple-400 border border-purple-500/20 flex items-center gap-1">
                  <Shield className="w-2.5 h-2.5" /> Team Battle · {battle.teamsCount} Teams
                </span>
              ) : (
                <span className="text-[10px] px-2 py-0.5 rounded-full font-semibold uppercase bg-amber-500/10 text-amber-400 border border-amber-500/20 flex items-center gap-1">
                  <Zap className="w-2.5 h-2.5" /> Solo Battle
                </span>
              )}

              <span className="text-xs text-neutral-600">
                Host: <span className="text-neutral-400">{battle.host?.username}</span>
              </span>
            </div>

            <h1 className="text-xl font-bold text-white mt-1">
              {battle.problems?.length > 1
                ? `${battle.problems.length} Questions`
                : battle.problems?.[0]?.title || 'Battle Room'}
            </h1>

            <div className="flex items-center gap-2 mt-2">
              <span className="font-mono text-amber-400 text-lg font-semibold">{battle.roomCode}</span>
              <button onClick={copyCode} className="p-1.5 rounded-md hover:bg-surface-hover text-neutral-500 hover:text-white transition-colors" title="Copy room code">
                <Copy className="w-4 h-4" />
              </button>
              {copied && <span className="text-xs text-emerald-400">Copied!</span>}
            </div>
          </div>

          {/* Host controls */}
          {isHost && (
            <div className="flex gap-2">
              {battle.status === 'WAITING' && (
                <button
                  onClick={handleStart}
                  className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white text-sm font-semibold transition-colors"
                >
                  <Play className="w-4 h-4" /> Start Battle
                </button>
              )}
              {battle.status === 'ACTIVE' && (
                <button
                  onClick={handleComplete}
                  className="px-4 py-2 rounded-lg border border-neutral-700 text-sm text-neutral-300 hover:bg-surface-hover transition-colors"
                >
                  End Battle
                </button>
              )}
            </div>
          )}
        </div>

        {/* Timer */}
        {battle.endsAt && battle.status === 'ACTIVE' && (
          <div className="flex items-center gap-1.5 mt-4 text-xs text-neutral-500">
            <Clock className="w-3.5 h-3.5 text-amber-500" />
            <span>Ends at <span className="text-neutral-300 font-medium">{new Date(battle.endsAt).toLocaleTimeString()}</span></span>
          </div>
        )}

        {/* My team badge */}
        {isTeamBattle && myTeam && (
          <div className="mt-3 inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-tertiary border border-border text-xs">
            <Users className="w-3 h-3 text-neutral-400" />
            <span className="text-neutral-400">Your team:</span>
            <span className="font-semibold text-white">{myTeam.teamName}</span>
            <span className="font-mono text-amber-400">{myTeam.score ?? 0} pts</span>
          </div>
        )}
      </div>

      {/* Problems */}
      {battle.problems && battle.problems.length > 0 && (
        <div className="mb-6">
          <h2 className="text-sm font-semibold text-neutral-400 uppercase tracking-wider mb-3">
            Problems ({battle.problems.length})
          </h2>
          <div className="grid md:grid-cols-2 gap-4">
            {battle.problems.map((p) => (
              <div key={p.id} className="bg-surface-secondary border border-border rounded-xl p-4 flex flex-col justify-between hover:border-neutral-600 transition-all">
                <div>
                  <h3 className="text-sm font-bold text-white mb-1">{p.title}</h3>
                  <div className="flex items-center gap-2 text-xs mb-4">
                    <span className={`px-2 py-0.5 rounded-full font-medium ${
                      p.difficulty === 'EASY' ? 'text-emerald-400 bg-emerald-400/10' :
                      p.difficulty === 'MEDIUM' ? 'text-amber-400 bg-amber-400/10' :
                      'text-red-400 bg-red-400/10'
                    }`}>{p.difficulty}</span>
                    <span className="text-neutral-600">{p.topic}</span>
                  </div>
                </div>
                {isAuthenticated ? (
                  <Link
                    to={`/problems/${p.id}?battleId=${battle.id}`}
                    className="flex items-center justify-center gap-1.5 w-full py-2 rounded-lg bg-amber-500/10 hover:bg-amber-500 hover:text-black border border-amber-500/20 text-amber-500 text-sm font-semibold transition-all"
                  >
                    <ExternalLink className="w-4 h-4" /> Solve Now
                  </Link>
                ) : (
                  <Link
                    to={`/login?returnTo=/battleground/${id}`}
                    className="flex items-center justify-center gap-1.5 w-full py-2 rounded-lg bg-neutral-700/50 border border-neutral-700 text-neutral-400 text-sm font-semibold transition-all hover:border-neutral-600"
                  >
                    <LogIn className="w-4 h-4" /> Log in to Solve
                  </Link>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Leaderboard Section */}
      <div className="bg-surface-secondary border border-border rounded-xl p-6">
        {/* Tab header */}
        {isTeamBattle && (
          <div className="flex gap-1 mb-5 bg-surface-tertiary rounded-lg p-1">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex-1 flex items-center justify-center gap-1.5 py-2 rounded-md text-xs font-semibold transition-colors ${
                  activeTab === tab.id
                    ? 'bg-surface-elevated text-white shadow'
                    : 'text-neutral-500 hover:text-neutral-300'
                }`}
              >
                <tab.icon className="w-3.5 h-3.5" />
                {tab.label}
              </button>
            ))}
          </div>
        )}

        {!isTeamBattle && (
          <h2 className="text-sm font-semibold text-white flex items-center gap-2 mb-4">
            <Trophy className="w-4 h-4 text-amber-400" /> Live Leaderboard
          </h2>
        )}

        {/* Teams leaderboard */}
        {isTeamBattle && activeTab === 'teams' && (
          <TeamBattleLeaderboard
            teams={teams}
            currentUserId={user?.userId}
            onJoinTeam={battle.status === 'WAITING' && isAuthenticated ? handleJoinTeam : null}
            canJoinTeams={battle.status === 'WAITING' && isAuthenticated}
          />
        )}

        {/* Individual leaderboard */}
        {activeTab === 'individual' && (
          leaderboard.length === 0 ? (
            <div className="py-8 text-center">
              <Users className="w-8 h-8 text-neutral-700 mx-auto mb-2" />
              <p className="text-neutral-600 text-sm">Waiting for participants to join...</p>
            </div>
          ) : (
            <div className="space-y-2">
              {leaderboard.map((entry, i) => (
                <motion.div
                  key={entry.userId || i}
                  initial={{ opacity: 0, x: -8 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: i * 0.04 }}
                  className={`flex items-center justify-between p-3.5 rounded-lg ${
                    i === 0 ? 'bg-amber-500/5 border border-amber-500/15' : 'bg-surface-tertiary border border-border'
                  } ${entry.userId === user?.userId ? 'ring-1 ring-amber-500/30' : ''}`}
                >
                  <div className="flex items-center gap-3">
                    <span className={`w-7 h-7 flex items-center justify-center rounded-full text-xs font-bold ${
                      i === 0 ? 'bg-amber-500/20 text-amber-400' :
                      i === 1 ? 'bg-neutral-500/20 text-neutral-300' :
                      i === 2 ? 'bg-orange-500/20 text-orange-400' :
                      'bg-surface-hover text-neutral-500'
                    }`}>
                      {entry.rank}
                    </span>
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="text-sm font-medium text-neutral-200">{entry.username}</span>
                        {entry.userId === user?.userId && (
                          <span className="text-[10px] px-1 py-0.5 rounded bg-amber-500/20 text-amber-400 font-semibold">You</span>
                        )}
                      </div>
                      {entry.teamName && (
                        <p className="text-[10px] text-neutral-600 mt-0.5">{entry.teamName}</p>
                      )}
                    </div>
                  </div>
                  <span className="font-mono text-sm text-amber-400 font-semibold">{entry.score} pts</span>
                </motion.div>
              ))}
            </div>
          )
        )}
      </div>
    </div>
  );
}
