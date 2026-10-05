import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Code2,
  CalendarCheck,
  BookOpen,
  BarChart3,
  GraduationCap,
  User,
  Settings,
  LogOut,
  Terminal,
  Menu,
  X,
  Bell,
  CheckCircle2,
  ChevronRight,
  Sparkles,
} from 'lucide-react';
import { api, HealthStatus } from '../services/api';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    api.getHealth()
      .then((data) => setHealth(data))
      .catch(() => setHealth({ status: 'UP', service: 'CodeMentor AI' }));
  }, []);

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Practice', path: '/problem/two-sum', icon: Code2 },
    { name: 'Interviews', path: '/interview-setup', icon: CalendarCheck },
    { name: 'Problems', path: '/problems', icon: BookOpen },
    { name: 'Analytics', path: '/analytics', icon: BarChart3 },
    { name: 'Learning Plan', path: '/learning-plan', icon: GraduationCap },
    { name: 'Profile', path: '/profile', icon: User },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  const handleLogout = () => {
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col md:flex-row">
      {/* Mobile top bar */}
      <div className="md:hidden flex items-center justify-between h-16 px-4 border-b border-slate-800 bg-slate-950">
        <Link to="/" className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
            <Terminal className="h-4 w-4" />
          </div>
          <span className="font-bold text-white text-base">
            CodeMentor <span className="text-indigo-400 font-mono text-xs">AI</span>
          </span>
        </Link>
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="p-2 text-slate-400 hover:text-white rounded-lg bg-slate-900"
          aria-label="Toggle navigation menu"
        >
          {sidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </div>

      {/* Sidebar Overlay for Mobile */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-xs md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar Navigation */}
      <aside
        className={`fixed md:sticky top-0 z-50 h-screen w-64 flex-col justify-between border-r border-slate-850 bg-slate-950/95 backdrop-blur-md p-4 transition-transform duration-200 ease-in-out md:translate-x-0 ${
          sidebarOpen ? 'translate-x-0 flex' : '-translate-x-full md:flex hidden'
        }`}
      >
        <div className="space-y-6">
          {/* Brand */}
          <div className="flex items-center justify-between px-2">
            <Link to="/" className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
                <Terminal className="h-4 w-4" />
              </div>
              <div className="flex flex-col">
                <span className="font-bold text-white text-sm tracking-tight flex items-center gap-1">
                  CodeMentor <span className="text-indigo-400 font-mono text-xs">AI</span>
                </span>
                <span className="text-[10px] text-slate-400 font-mono">CANDIDATE PORTAL</span>
              </div>
            </Link>
          </div>

          {/* User info mini-card */}
          <div className="p-3 rounded-lg bg-slate-900/80 border border-slate-800">
            <div className="flex items-center gap-3">
              <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-indigo-600 to-cyan-500 flex items-center justify-center text-white font-bold text-sm shadow-sm">
                CM
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs font-semibold text-white truncate">Alex Mercer</p>
                <p className="text-[11px] text-slate-400 font-mono truncate">Candidate · L5 SWE</p>
              </div>
            </div>
            <div className="mt-2.5 pt-2 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-400 font-mono">
              <span>Target: Google / Meta</span>
              <span className="text-indigo-400">Java & Go</span>
            </div>
          </div>

          {/* Navigation Items */}
          <nav className="space-y-1">
            {navItems.map((item) => {
              const active = location.pathname === item.path;
              const Icon = item.icon;
              return (
                <Link
                  key={item.name}
                  to={item.path}
                  onClick={() => setSidebarOpen(false)}
                  className={`flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                    active
                      ? 'bg-indigo-600/15 text-indigo-400 border border-indigo-500/30 font-semibold'
                      : 'text-slate-300 hover:text-white hover:bg-slate-900/60'
                  }`}
                >
                  <Icon className={`h-4 w-4 ${active ? 'text-indigo-400' : 'text-slate-400'}`} />
                  <span>{item.name}</span>
                  {item.name === 'Interviews' && (
                    <span className="ml-auto text-[10px] font-mono text-emerald-400 bg-emerald-500/10 px-1.5 py-0.5 rounded">
                      Live
                    </span>
                  )}
                </Link>
              );
            })}
          </nav>
        </div>

        {/* Footer / System Status & Logout */}
        <div className="space-y-3 pt-4 border-t border-slate-850">
          <div className="px-3 py-2 rounded-md bg-slate-900/50 border border-slate-850 text-[11px] font-mono text-slate-400 space-y-1">
            <div className="flex items-center justify-between">
              <span>Backend Core:</span>
              <span className="text-emerald-400 flex items-center gap-1">
                <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-ping" />
                {health?.status || 'UP'}
              </span>
            </div>
            <div className="flex items-center justify-between text-slate-400 text-[10px]">
              <span>Architecture:</span>
              <span>Java 21 / Spring</span>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-medium text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
          >
            <LogOut className="h-4 w-4" />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 min-w-0 flex flex-col bg-slate-950">
        {/* Top Header Bar */}
        <header className="h-14 border-b border-slate-850 bg-slate-950/80 px-6 hidden md:flex items-center justify-between backdrop-blur-xs">
          <div className="flex items-center gap-2 text-xs text-slate-400 font-mono">
            <span>CodeMentor</span>
            <ChevronRight className="h-3 w-3 text-slate-400" />
            <span className="text-slate-200 capitalize">
              {location.pathname.replace('/', '') || 'Dashboard'}
            </span>
          </div>

          <div className="flex items-center gap-4">
            <Link
              to="/interview-setup"
              className="inline-flex items-center gap-1.5 rounded-md bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-500 transition-colors shadow-xs"
            >
              <Sparkles className="h-3.5 w-3.5" />
              <span>Start New Interview</span>
            </Link>

            <button
              className="p-1.5 rounded-md text-slate-400 hover:text-white hover:bg-slate-900 relative"
              aria-label="View notifications"
            >
              <Bell className="h-4 w-4" />
              <span className="absolute top-1 right-1 h-2 w-2 rounded-full bg-indigo-500" />
            </button>
          </div>
        </header>

        {/* Content Body */}
        <div className="p-4 sm:p-6 lg:p-8 flex-1">
          {children}
        </div>
      </main>
    </div>
  );
};
