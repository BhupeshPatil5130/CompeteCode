import { Code2 } from 'lucide-react';

export default function LoadingSpinner({ text = 'Loading...' }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 gap-4">
      <div className="relative">
        <div className="w-10 h-10 rounded-xl bg-amber-500/10 flex items-center justify-center animate-pulse">
          <Code2 className="w-5 h-5 text-amber-400" />
        </div>
      </div>
      <p className="text-sm text-neutral-500 font-medium">{text}</p>
    </div>
  );
}
