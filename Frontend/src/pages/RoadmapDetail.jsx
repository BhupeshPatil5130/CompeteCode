import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { CheckCircle, Circle, ArrowLeft } from 'lucide-react';
import { roadmapApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import DifficultyBadge from '../components/DifficultyBadge';
import LoadingSpinner from '../components/LoadingSpinner';

export default function RoadmapDetail() {
  const { id } = useParams();
  const { isAuthenticated } = useAuth();
  const [progress, setProgress] = useState(null);
  const [roadmap, setRoadmap] = useState(null);

  useEffect(() => {
    roadmapApi.getById(id).then(({ data }) => setRoadmap(data));
    if (isAuthenticated) {
      roadmapApi.getProgress(id).then(({ data }) => setProgress(data)).catch(() => {});
    }
  }, [id, isAuthenticated]);

  if (!roadmap) return <LoadingSpinner text="Loading roadmap..." />;

  const items = progress?.items || roadmap.items?.map((item) => ({
    problemId: item.problem?.id,
    problemTitle: item.problem?.title,
    difficulty: item.problem?.difficulty,
    sectionTitle: item.sectionTitle,
    completed: false,
  })) || [];

  const completedCount = progress?.completedProblems ?? items.filter((i) => i.completed).length;
  const totalCount = progress?.totalProblems ?? items.length;
  const percent = progress?.progressPercent ?? (totalCount ? (completedCount / totalCount) * 100 : 0);

  // Group items by section
  const sections = {};
  items.forEach((item) => {
    const section = item.sectionTitle || 'General';
    if (!sections[section]) sections[section] = [];
    sections[section].push(item);
  });

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      {/* Back link */}
      <Link to="/roadmaps" className="inline-flex items-center gap-1.5 text-xs text-neutral-500 hover:text-neutral-300 transition-colors mb-6">
        <ArrowLeft className="w-3.5 h-3.5" /> All Roadmaps
      </Link>

      {/* Header */}
      <div className="mb-8">
        <div className="text-4xl mb-2">{roadmap.icon}</div>
        <h1 className="text-2xl font-bold text-white mb-1">{roadmap.title}</h1>
        <p className="text-sm text-neutral-500">{roadmap.description}</p>

        {/* Progress bar */}
        <div className="mt-5">
          <div className="flex justify-between text-xs mb-1.5">
            <span className="text-neutral-500">Progress</span>
            <span className="text-amber-400 font-semibold">{completedCount}/{totalCount} ({percent.toFixed(0)}%)</span>
          </div>
          <div className="h-1.5 rounded-full bg-surface-tertiary overflow-hidden">
            <motion.div
              className="h-full rounded-full bg-gradient-to-r from-amber-500 to-orange-500"
              initial={{ width: 0 }}
              animate={{ width: `${percent}%` }}
              transition={{ duration: 0.8, ease: 'easeOut' }}
            />
          </div>
        </div>
      </div>

      {/* Sections */}
      <div className="space-y-6">
        {Object.entries(sections).map(([sectionTitle, sectionItems]) => (
          <div key={sectionTitle}>
            <h3 className="text-[10px] font-semibold text-neutral-500 uppercase tracking-wider mb-2 pl-1">
              {sectionTitle}
            </h3>
            <div className="space-y-1.5">
              {sectionItems.map((item, idx) => (
                <motion.div
                  key={item.problemId || idx}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: idx * 0.03 }}
                >
                  <Link
                    to={`/problems/${item.problemId}?roadmapId=${id}`}
                    className="flex items-center gap-3 p-3 rounded-lg bg-surface-secondary border border-border hover:border-neutral-600 transition-all group"
                  >
                    {item.completed ? (
                      <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0" />
                    ) : (
                      <Circle className="w-4 h-4 text-neutral-700 shrink-0" />
                    )}
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-neutral-200 group-hover:text-amber-400 transition-colors truncate">
                        {item.problemTitle}
                      </p>
                    </div>
                    {item.difficulty && <DifficultyBadge difficulty={item.difficulty} />}
                  </Link>
                </motion.div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
