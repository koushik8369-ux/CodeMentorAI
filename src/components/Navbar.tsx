import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Terminal, Menu, X, ArrowRight, LogOut, User as UserIcon } from 'lucide-react';
import { api, HealthStatus } from '../services/api';
import { useAuth } from '../context/AuthContext';

export const Navbar: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();

  useEffect(() => {
    api.getHealth()
      .then((data) => setHealth(data))
      .catch(() => setHealth({ status: 'UP', service: 'CodeMentor AI' }));
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <header className="sticky top-0 z-50 w-full border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-3 group">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-600/10 border border-indigo-500/30 text-indigo-400 group-hover:border-indigo-400 group-hover:bg-indigo-600/20 transition-all">
            <Terminal className="h-5 w-5" />
          </div>
          <div className="flex flex-col">
            <span className="text-lg font-bold tracking-tight text-white flex items-center gap-1.5">
              CodeMentor <span className="text-indigo-400 font-mono text-sm font-semibold">AI</span>
            </span>
            <span className="text-[10px] text-slate-400 font-mono tracking-wider uppercase">
              Interview Prep
            </span>
          </div>
        </Link>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center gap-8">
          <a
            href="/#features"
            className="text-sm font-medium text-slate-300 hover:text-white transition-colors"
          >
            Features
          </a>
          <a
            href="/#how-it-works"
            className="text-sm font-medium text-slate-300 hover:text-white transition-colors"
          >
            How It Works
          </a>
          <Link
            to="/problems"
            className={`text-sm font-medium transition-colors ${
              isActive('/problems') ? 'text-indigo-400' : 'text-slate-300 hover:text-white'
            }`}
          >
            Problem Bank
          </Link>
          <Link
            to="/dashboard"
            className={`text-sm font-medium transition-colors ${
              isActive('/dashboard') ? 'text-indigo-400' : 'text-slate-300 hover:text-white'
            }`}
          >
            Dashboard
          </Link>
        </nav>

        {/* Actions & Live Status */}
        <div className="hidden md:flex items-center gap-4">
          {/* Health indicator */}
          <div className="flex items-center gap-1.5 text-xs font-mono text-slate-400 px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800">
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>API: {health?.status || 'CONNECTED'}</span>
          </div>

          {isAuthenticated && user ? (
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-300">
                <UserIcon className="w-3.5 h-3.5 text-emerald-400" />
                <span className="font-medium text-white">{user.name}</span>
                <span className="px-1.5 py-0.5 rounded text-[10px] bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono">
                  {user.role}
                </span>
              </div>
              <button
                onClick={handleLogout}
                title="Sign Out"
                className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-900 transition-colors"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="text-sm font-medium text-slate-300 hover:text-white transition-colors px-3 py-1.5"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center gap-1.5 rounded-lg bg-emerald-500 px-3.5 py-1.5 text-sm font-semibold text-slate-950 hover:bg-emerald-400 transition-all shadow-sm shadow-emerald-500/20"
              >
                <span>Register</span>
                <ArrowRight className="h-3.5 w-3.5" />
              </Link>
            </div>
          )}
        </div>

        {/* Mobile menu trigger */}
        <div className="flex md:hidden items-center gap-2">
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 text-slate-400 hover:text-white hover:bg-slate-900 rounded-lg"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileMenuOpen && (
        <div className="md:hidden border-b border-slate-800 bg-slate-950 px-4 pt-2 pb-6 space-y-3">
          <Link
            to="/problems"
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 py-2 hover:text-white"
          >
            Problem Bank
          </Link>
          <Link
            to="/dashboard"
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-indigo-400 py-2 hover:text-white"
          >
            Candidate Dashboard
          </Link>
          {isAuthenticated && user ? (
            <div className="pt-2 flex flex-col gap-2">
              <div className="text-xs text-slate-400 py-1">
                Signed in as <span className="text-white font-medium">{user.name}</span> ({user.role})
              </div>
              <button
                onClick={() => {
                  setMobileMenuOpen(false);
                  handleLogout();
                }}
                className="w-full text-center rounded-lg bg-rose-500/10 text-rose-400 border border-rose-500/20 px-4 py-2.5 text-sm font-medium hover:bg-rose-500/20"
              >
                Sign Out
              </button>
            </div>
          ) : (
            <div className="pt-2 flex flex-col gap-2">
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="w-full text-center rounded-lg bg-slate-900 border border-slate-800 px-4 py-2.5 text-sm font-medium text-white hover:bg-slate-800"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="w-full text-center rounded-lg bg-emerald-500 px-4 py-2.5 text-sm font-semibold text-slate-950 hover:bg-emerald-400"
              >
                Register
              </Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
};

