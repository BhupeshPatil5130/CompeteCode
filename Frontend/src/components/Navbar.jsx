import { Link, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  Code2, Map, Swords, Trophy, LogOut, User, Menu, X,
  LayoutGrid, ChevronDown, History, UserCircle, Sparkles, Zap
} from 'lucide-react';

const navItems = [
  { path: '/problems', label: 'Problems', icon: Code2 },
  { path: '/sheets', label: 'Sheets', icon: LayoutGrid },
  { path: '/roadmaps', label: 'Roadmaps', icon: Map },
  { path: '/battleground', label: 'Battle', icon: Swords },
  { path: '/speed-mode', label: 'Speed', icon: Zap },
  { path: '/story-mode', label: 'Story', icon: Sparkles },
  { path: '/leaderboard', label: 'Leaderboard', icon: Trophy },
];

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [userDropdown, setUserDropdown] = useState(false);

  return (
    <nav className="sticky top-0 z-50 bg-surface-secondary/95 backdrop-blur-md border-b border-border">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-14">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 group" onClick={() => setMobileOpen(false)}>
            <div className="w-8 h-8 rounded-lg bg-accent-amber/10 flex items-center justify-center group-hover:bg-accent-amber/20 transition-colors">
              <Code2 className="w-5 h-5 text-accent-amber" />
            </div>
            <span className="text-lg font-bold text-white">CompetCode</span>
          </Link>

          {/* Desktop Nav */}
          <div className="hidden md:flex items-center gap-0.5">
            {navItems.map(({ path, label, icon: Icon }) => {
              const isActive = location.pathname === path || location.pathname.startsWith(path + '/');
              return (
                <Link
                  key={path}
                  to={path}
                  className={`relative flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'text-accent-amber bg-accent-amber/10'
                      : 'text-neutral-400 hover:text-white hover:bg-surface-hover'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {label}
                </Link>
              );
            })}
          </div>

          {/* Right side */}
          <div className="flex items-center gap-2">
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setUserDropdown(!userDropdown)}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-tertiary border border-border hover:border-border-strong transition-colors"
                >
                  <div className="w-6 h-6 rounded-full bg-accent-amber/20 flex items-center justify-center">
                    <span className="text-xs font-bold text-accent-amber">
                      {user.username?.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <span className="hidden sm:block text-sm font-medium text-white">{user.username}</span>
                  <span className="hidden sm:block text-xs text-accent-amber font-mono">{user.rating}</span>
                  <ChevronDown className="w-3.5 h-3.5 text-neutral-500" />
                </button>

                {userDropdown && (
                  <>
                    <div className="fixed inset-0 z-40" onClick={() => setUserDropdown(false)} />
                    <div className="absolute right-0 top-full mt-1.5 w-48 bg-surface-elevated border border-border-strong rounded-lg shadow-lg z-50 py-1 overflow-hidden">
                      <Link
                        to="/profile"
                        onClick={() => setUserDropdown(false)}
                        className="flex items-center gap-2.5 px-3 py-2 text-sm text-neutral-300 hover:bg-surface-hover hover:text-white transition-colors"
                      >
                        <UserCircle className="w-4 h-4" /> Profile
                      </Link>
                      <Link
                        to="/submissions"
                        onClick={() => setUserDropdown(false)}
                        className="flex items-center gap-2.5 px-3 py-2 text-sm text-neutral-300 hover:bg-surface-hover hover:text-white transition-colors"
                      >
                        <History className="w-4 h-4" /> Submissions
                      </Link>
                      <Link
                        to="/battle-history"
                        onClick={() => setUserDropdown(false)}
                        className="flex items-center gap-2.5 px-3 py-2 text-sm text-neutral-300 hover:bg-surface-hover hover:text-white transition-colors"
                      >
                        <Swords className="w-4 h-4" /> Battle History
                      </Link>
                      <div className="border-t border-border my-1" />
                      <button
                        onClick={() => { logout(); setUserDropdown(false); }}
                        className="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-red-400 hover:bg-red-500/10 transition-colors"
                      >
                        <LogOut className="w-4 h-4" /> Sign Out
                      </button>
                    </div>
                  </>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login" className="btn-ghost text-sm">Log In</Link>
                <Link to="/register" className="btn-primary text-sm">Sign Up</Link>
              </div>
            )}

            {/* Mobile toggle */}
            <button
              onClick={() => setMobileOpen(!mobileOpen)}
              className="md:hidden p-2 rounded-md text-neutral-400 hover:text-white hover:bg-surface-hover transition-colors"
            >
              {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="md:hidden border-t border-border bg-surface-secondary">
          <div className="px-4 py-3 space-y-1">
            {navItems.map(({ path, label, icon: Icon }) => {
              const isActive = location.pathname === path || location.pathname.startsWith(path + '/');
              return (
                <Link
                  key={path}
                  to={path}
                  onClick={() => setMobileOpen(false)}
                  className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'text-accent-amber bg-accent-amber/10'
                      : 'text-neutral-400 hover:text-white hover:bg-surface-hover'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {label}
                </Link>
              );
            })}
          </div>
        </div>
      )}
    </nav>
  );
}
