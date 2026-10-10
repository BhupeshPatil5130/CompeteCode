import { Link } from 'react-router-dom';
import { Code2, ExternalLink, Heart } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="border-t border-border bg-surface-secondary mt-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          {/* Brand */}
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-2 mb-3">
              <div className="w-8 h-8 rounded-lg bg-amber-500/10 flex items-center justify-center">
                <Code2 className="w-5 h-5 text-amber-400" />
              </div>
              <span className="text-lg font-bold text-white">CompetCode</span>
            </div>
            <p className="text-sm text-neutral-500 leading-relaxed">
              Master DSA with AI-powered feedback, live battles, and structured learning paths.
            </p>
          </div>

          {/* Practice */}
          <div>
            <h4 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-3">Practice</h4>
            <ul className="space-y-2">
              <li><Link to="/problems" className="text-sm text-neutral-500 hover:text-white transition-colors">All Problems</Link></li>
              <li><Link to="/sheets" className="text-sm text-neutral-500 hover:text-white transition-colors">DSA Sheets</Link></li>
              <li><Link to="/roadmaps" className="text-sm text-neutral-500 hover:text-white transition-colors">Roadmaps</Link></li>
            </ul>
          </div>

          {/* Compete */}
          <div>
            <h4 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-3">Compete</h4>
            <ul className="space-y-2">
              <li><Link to="/battleground" className="text-sm text-neutral-500 hover:text-white transition-colors">BattleGround</Link></li>
              <li><Link to="/leaderboard" className="text-sm text-neutral-500 hover:text-white transition-colors">Leaderboard</Link></li>
            </ul>
          </div>

          {/* Account */}
          <div>
            <h4 className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-3">Account</h4>
            <ul className="space-y-2">
              <li><Link to="/profile" className="text-sm text-neutral-500 hover:text-white transition-colors">Profile</Link></li>
              <li><Link to="/submissions" className="text-sm text-neutral-500 hover:text-white transition-colors">Submissions</Link></li>
              <li><Link to="/register" className="text-sm text-neutral-500 hover:text-white transition-colors">Sign Up</Link></li>
            </ul>
          </div>
        </div>

        <div className="mt-10 pt-6 border-t border-border flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-xs text-neutral-600">© {new Date().getFullYear()} CompetCode. Built for competitive coders.</p>
          <div className="flex items-center gap-3">
            <a href="#" className="text-neutral-600 hover:text-neutral-400 transition-colors">
              <ExternalLink className="w-4 h-4" />
            </a>
            <a href="#" className="text-neutral-600 hover:text-neutral-400 transition-colors">
              <Heart className="w-4 h-4" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
