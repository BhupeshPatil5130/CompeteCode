import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { History, CheckCircle, XCircle, AlertCircle, Filter } from 'lucide-react';
import { submissionApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

const STATUS_FILTERS = ['All', 'ACCEPTED', 'WRONG_ANSWER', 'ERROR'];

export default function SubmissionHistory() {
  const { isAuthenticated } = useAuth();
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('All');

  useEffect(() => {
    if (isAuthenticated) {
      submissionApi.getMy()
        .then(({ data }) => setSubmissions(Array.isArray(data) ? data : []))
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="max-w-md mx-auto px-4 py-20 text-center">
        <History className="w-10 h-10 text-neutral-700 mx-auto mb-3" />
        <h2 className="text-xl font-bold text-white mb-2">Sign in to view submissions</h2>
        <p className="text-sm text-neutral-500 mb-6">Track all your problem submissions and results.</p>
        <Link
          to="/login"
          className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-semibold text-sm transition-all"
        >
          Sign In
        </Link>
      </div>
    );
  }

  if (loading) return <LoadingSpinner text="Loading submissions..." />;

  const filtered = statusFilter === 'All'
    ? submissions
    : submissions.filter((s) => s.status === statusFilter);

  const getStatusIcon = (status) => {
    if (status === 'ACCEPTED') return <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />;
    if (status === 'WRONG_ANSWER') return <XCircle className="w-3.5 h-3.5 text-red-400" />;
    return <AlertCircle className="w-3.5 h-3.5 text-amber-400" />;
  };

  const getStatusColor = (status) => {
    if (status === 'ACCEPTED') return 'text-emerald-400';
    if (status === 'WRONG_ANSWER') return 'text-red-400';
    return 'text-amber-400';
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2 mb-1">
          <History className="w-6 h-6 text-amber-400" /> Submissions
        </h1>
        <p className="text-sm text-neutral-500">{submissions.length} total submissions</p>
      </div>

      {/* Filter pills */}
      <div className="flex gap-1.5 mb-6">
        {STATUS_FILTERS.map((s) => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`px-3 py-1 rounded-md text-xs font-medium transition-colors ${
              statusFilter === s
                ? 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                : 'bg-surface-secondary text-neutral-500 border border-border hover:text-neutral-300'
            }`}
          >
            {s === 'All' ? 'All' : s.replace('_', ' ')}
          </button>
        ))}
      </div>

      {/* Submissions table */}
      {filtered.length === 0 ? (
        <div className="py-16 text-center">
          <History className="w-8 h-8 text-neutral-700 mx-auto mb-2" />
          <p className="text-neutral-600 text-sm">
            {submissions.length === 0
              ? 'No submissions yet. Start solving problems!'
              : 'No submissions match this filter.'}
          </p>
          {submissions.length === 0 && (
            <Link to="/problems" className="inline-block mt-4 text-sm text-amber-400 hover:text-amber-300 font-medium transition-colors">
              Browse Problems →
            </Link>
          )}
        </div>
      ) : (
        <div className="bg-surface-secondary border border-border rounded-xl overflow-hidden">
          <div className="grid grid-cols-12 gap-4 px-5 py-2.5 text-[10px] text-neutral-600 uppercase tracking-wider font-medium border-b border-border">
            <div className="col-span-1">Status</div>
            <div className="col-span-4">Problem</div>
            <div className="col-span-2">Language</div>
            <div className="col-span-2">Score</div>
            <div className="col-span-3 text-right">Submitted</div>
          </div>

          {filtered.map((sub, i) => (
            <motion.div
              key={sub.id || i}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: i * 0.02 }}
              className="grid grid-cols-12 gap-4 px-5 py-3 items-center border-b border-border last:border-0 hover:bg-surface-hover transition-colors"
            >
              <div className="col-span-1">
                {getStatusIcon(sub.status)}
              </div>
              <div className="col-span-4">
                <Link
                  to={`/problems/${sub.problemId}`}
                  className="text-sm text-neutral-200 hover:text-amber-400 transition-colors"
                >
                  {sub.problemTitle || `Problem #${sub.problemId}`}
                </Link>
              </div>
              <div className="col-span-2">
                <span className="text-xs text-neutral-500 font-mono">{sub.language}</span>
              </div>
              <div className="col-span-2">
                <span className={`text-xs font-semibold ${getStatusColor(sub.status)}`}>
                  {sub.score ?? '—'}
                </span>
              </div>
              <div className="col-span-3 text-right">
                <span className="text-xs text-neutral-600">
                  {sub.createdAt ? new Date(sub.createdAt).toLocaleDateString() : '—'}
                </span>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
