import { useEffect, useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search, Filter, CheckCircle, Circle, Minus } from 'lucide-react';
import { problemApi } from '../api/client';
import DifficultyBadge from '../components/DifficultyBadge';
import LoadingSpinner from '../components/LoadingSpinner';

const TOPICS = [
  'All',
  'Arrays & Hashing',
  'Strings',
  'Linked Lists',
  'Stacks & Queues',
  'Trees & BST',
  'Graphs',
  'Dynamic Programming',
  'Greedy & Sorting',
  'Binary Search',
  'Backtracking',
];

const DIFFICULTIES = ['All', 'EASY', 'MEDIUM', 'HARD', 'EXPERT'];

export default function Problems() {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedTopic, setSelectedTopic] = useState('All');
  const [selectedDifficulty, setSelectedDifficulty] = useState('All');
  const [currentPage, setCurrentPage] = useState(1);
  const perPage = 15;

  useEffect(() => {
    problemApi.getAll()
      .then(({ data }) => setProblems(data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    return problems.filter((p) => {
      const matchSearch = !search || p.title.toLowerCase().includes(search.toLowerCase());
      const matchTopic = selectedTopic === 'All' || p.topic === selectedTopic;
      const matchDiff = selectedDifficulty === 'All' || p.difficulty === selectedDifficulty;
      return matchSearch && matchTopic && matchDiff;
    });
  }, [problems, search, selectedTopic, selectedDifficulty]);

  const totalPages = Math.ceil(filtered.length / perPage);
  const paginated = filtered.slice((currentPage - 1) * perPage, currentPage * perPage);

  useEffect(() => { setCurrentPage(1); }, [search, selectedTopic, selectedDifficulty]);

  if (loading) return <LoadingSpinner text="Loading problems..." />;

  const difficultyCount = (d) => problems.filter(p => p.difficulty === d).length;

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white mb-1">Problems</h1>
        <p className="text-sm text-neutral-500">{problems.length} problems · Solve with AI-powered test validation</p>
      </div>

      {/* Difficulty summary pills */}
      <div className="flex flex-wrap gap-3 mb-5">
        {['EASY', 'MEDIUM', 'HARD'].map((d) => (
          <div key={d} className="flex items-center gap-2 text-xs">
            <span className={`w-2 h-2 rounded-full ${d === 'EASY' ? 'bg-emerald-400' : d === 'MEDIUM' ? 'bg-amber-400' : 'bg-orange-400'}`} />
            <span className="text-neutral-500 capitalize">{d.toLowerCase()}</span>
            <span className="text-neutral-600 font-mono">{difficultyCount(d)}</span>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-600" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search problems..."
            className="w-full pl-9 pr-4 py-2 rounded-lg bg-surface-secondary border border-border text-sm text-white placeholder-neutral-600 focus:outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/20 transition-colors"
          />
        </div>
        <select
          value={selectedTopic}
          onChange={(e) => setSelectedTopic(e.target.value)}
          className="px-3 py-2 rounded-lg bg-surface-secondary border border-border text-sm text-neutral-300 focus:outline-none focus:border-amber-500/50 cursor-pointer"
        >
          {TOPICS.map((t) => <option key={t} value={t}>{t}</option>)}
        </select>
        <select
          value={selectedDifficulty}
          onChange={(e) => setSelectedDifficulty(e.target.value)}
          className="px-3 py-2 rounded-lg bg-surface-secondary border border-border text-sm text-neutral-300 focus:outline-none focus:border-amber-500/50 cursor-pointer"
        >
          {DIFFICULTIES.map((d) => (
            <option key={d} value={d}>{d === 'All' ? 'All Difficulties' : d.charAt(0) + d.slice(1).toLowerCase()}</option>
          ))}
        </select>
      </div>

      {/* Topic pills */}
      <div className="flex flex-wrap gap-1.5 mb-6">
        {TOPICS.map((t) => (
          <button
            key={t}
            onClick={() => setSelectedTopic(t)}
            className={`px-3 py-1 rounded-md text-xs font-medium transition-colors ${
              selectedTopic === t
                ? 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                : 'bg-surface-secondary text-neutral-500 border border-border hover:text-neutral-300 hover:border-neutral-700'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {/* Problem list */}
      {paginated.length === 0 ? (
        <div className="py-16 text-center">
          <p className="text-neutral-500 text-sm">No problems match your filters.</p>
        </div>
      ) : (
        <div className="border border-border rounded-xl overflow-hidden divide-y divide-border">
          {/* Table header */}
          <div className="grid grid-cols-12 gap-4 px-4 py-2.5 bg-surface-secondary text-xs text-neutral-600 uppercase tracking-wider font-medium">
            <div className="col-span-1">Status</div>
            <div className="col-span-5">Title</div>
            <div className="col-span-3">Topic</div>
            <div className="col-span-3 text-right">Difficulty</div>
          </div>

          {paginated.map((problem, i) => (
            <motion.div
              key={problem.id}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: i * 0.03 }}
            >
              <Link
                to={`/problems/${problem.id}`}
                className="grid grid-cols-12 gap-4 px-4 py-3 items-center hover:bg-surface-hover transition-colors group"
              >
                <div className="col-span-1">
                  <Circle className="w-4 h-4 text-neutral-700" />
                </div>
                <div className="col-span-5">
                  <p className="text-sm font-medium text-neutral-200 group-hover:text-amber-400 transition-colors">
                    {problem.title}
                  </p>
                </div>
                <div className="col-span-3">
                  <span className="text-xs text-neutral-500">{problem.topic}</span>
                </div>
                <div className="col-span-3 text-right">
                  <DifficultyBadge difficulty={problem.difficulty} />
                </div>
              </Link>
            </motion.div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between mt-6">
          <p className="text-xs text-neutral-600">
            Showing {(currentPage - 1) * perPage + 1}–{Math.min(currentPage * perPage, filtered.length)} of {filtered.length}
          </p>
          <div className="flex gap-1">
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => setCurrentPage(i + 1)}
                className={`w-8 h-8 rounded-md text-xs font-medium transition-colors ${
                  currentPage === i + 1
                    ? 'bg-amber-500 text-black'
                    : 'bg-surface-secondary text-neutral-400 border border-border hover:bg-surface-hover'
                }`}
              >
                {i + 1}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
