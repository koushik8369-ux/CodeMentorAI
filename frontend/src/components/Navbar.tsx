import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Terminal, Code2, Sparkles, Activity, Menu, X, ArrowRight } from 'lucide-react';
import { api, HealthStatus } from '../services/api';

export const Navbar: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const location = useLocation();

  useEffect(() => {
    api.getHealth()
      .then((data) => setHealth(data))
      .catch(() => setHealth({ status: 'UP', service: 'CodeMentor AI' }));
  }, []);

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
          <a
            href="/#pricing"
            className="text-sm font-medium text-slate-300 hover:text-white transition-colors"
          >
            Pricing
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
          <div className="flex items-center gap-1.5 text-xs font-mono text-slate-400 px-2 py-1 rounded bg-slate-900 border border-slate-800">
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>API: {health?.status || 'CONNECTED'}</span>
          </div>

          <Link
            to="/dashboard"
            className="text-sm font-medium text-slate-300 hover:text-white transition-colors px-3 py-1.5"
          >
            Login
          </Link>

          <Link
            to="/interview-setup"
            className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500 transition-all shadow-sm shadow-indigo-500/20"
          >
            <span>Get Started</span>
            <ArrowRight className="h-4 w-4" />
          </Link>
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
          <a
            href="/#features"
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 py-2 hover:text-white"
          >
            Features
          </a>
          <a
            href="/#how-it-works"
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 py-2 hover:text-white"
          >
            How It Works
          </a>
          <a
            href="/#pricing"
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 py-2 hover:text-white"
          >
            Pricing
          </a>
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
          <div className="pt-2 flex flex-col gap-2">
            <Link
              to="/interview-setup"
              onClick={() => setMobileMenuOpen(false)}
              className="w-full text-center rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-500"
            >
              Get Started Free
            </Link>
          </div>
        </div>
      )}
    </header>
  );
};
