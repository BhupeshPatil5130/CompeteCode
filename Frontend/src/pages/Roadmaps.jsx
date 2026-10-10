import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Map } from 'lucide-react';
import { roadmapApi } from '../api/client';
import LoadingSpinner from '../components/LoadingSpinner';

export default function Roadmaps() {
  const [roadmaps, setRoadmaps] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    roadmapApi.getAll()
      .then(({ data }) => setRoadmaps(data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner text="Loading roadmaps..." />;

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2 mb-1">
          <Map className="w-6 h-6 text-amber-400" /> Learning Roadmaps
        </h1>
        <p className="text-sm text-neutral-500">Structured learning paths with curated problem sets and progress tracking</p>
      </div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {roadmaps.map((roadmap, i) => {
          const total = roadmap.items?.length || 0;
          return (
            <motion.div
              key={roadmap.id}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05 }}
            >
              <Link
                to={`/roadmaps/${roadmap.id}`}
                className="block bg-surface-secondary border border-border rounded-xl p-5 hover:border-neutral-600 transition-all group"
              >
                <div className="text-3xl mb-3">{roadmap.icon || '📚'}</div>
                <h3 className="text-sm font-semibold text-white group-hover:text-amber-400 transition-colors mb-1">
                  {roadmap.title}
                </h3>
                <p className="text-xs text-neutral-500 leading-relaxed mb-3">{roadmap.description}</p>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] text-neutral-600 uppercase tracking-wider font-medium">{total} problems</span>
                  <div className="w-16 h-1 rounded-full bg-surface-tertiary overflow-hidden">
                    <div className="h-full rounded-full bg-amber-500/50" style={{ width: '0%' }} />
                  </div>
                </div>
              </Link>
            </motion.div>
          );
        })}
      </div>
    </div>
  );
}
