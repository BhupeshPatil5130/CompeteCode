import { useEffect, useState, useRef } from 'react';

export default function StatsCard({ icon: Icon, label, value, suffix = '', delay = 0 }) {
  const [displayed, setDisplayed] = useState(0);
  const ref = useRef(null);
  const started = useRef(false);

  useEffect(() => {
    const num = parseInt(value, 10);
    if (isNaN(num) || num === 0 || started.current) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !started.current) {
          started.current = true;
          const duration = 1200;
          const steps = 40;
          const increment = num / steps;
          let current = 0;
          const timer = setInterval(() => {
            current += increment;
            if (current >= num) {
              setDisplayed(num);
              clearInterval(timer);
            } else {
              setDisplayed(Math.floor(current));
            }
          }, duration / steps);
        }
      },
      { threshold: 0.3 }
    );

    if (ref.current) observer.observe(ref.current);
    return () => observer.disconnect();
  }, [value]);

  return (
    <div
      ref={ref}
      style={{ animationDelay: `${delay}ms` }}
      className="bg-surface-secondary border border-border rounded-xl p-5 animate-fade-in opacity-0"
    >
      <div className="flex items-center gap-3 mb-3">
        {Icon && (
          <div className="w-9 h-9 rounded-lg bg-amber-500/10 flex items-center justify-center">
            <Icon className="w-4.5 h-4.5 text-amber-400" />
          </div>
        )}
        <span className="text-xs font-medium text-neutral-500 uppercase tracking-wider">{label}</span>
      </div>
      <p className="text-3xl font-extrabold text-white tracking-tight">
        {displayed.toLocaleString()}{suffix}
      </p>
    </div>
  );
}
