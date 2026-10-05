import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  BookOpen,
  Search,
  SlidersHorizontal,
  ChevronRight,
  Sparkles,
  ArrowRight,
  Code2,
} from 'lucide-react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api } from '../services/api';
import { Problem, Difficulty } from '../types';

export const ProblemsListPage: React.FC = () => {
  const [problems, setProblems] = useState<Problem[]>([]);
  const [search, setSearch] = useState('');
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>('All');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getProblems()
      .then((data) => {
        setProblems(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load problems', err);
        setLoading(false);
      });
  }, []);

  const filteredProblems = problems.filter((p) => {
    const matchesSearch =
      p.title.toLowerCase().includes(search.toLowerCase()) ||
      p.topic.toLowerCase().includes(search.toLowerCase());
    const matchesDiff =
      selectedDifficulty === 'All' || p.difficulty === selectedDifficulty;
    return matchesSearch && matchesDiff;
  });

  return (
    <DashboardLayout>
      <div className="max-w-6xl mx-auto space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-850 pb-6">
          <div>
            <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
              <BookOpen className="h-4 w-4" />
              <span>CURATED CODING REPOSITORY</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Problem Bank
            </h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Explore battle-tested interview questions with interactive multi-language code workspaces.
            </p>
          </div>

          <Link
            to="/interview-setup"
            className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-xs font-semibold text-white hover:bg-indigo-500 shadow-sm"
          >
            <Sparkles className="h-4 w-4" />
            <span>Generate AI Challenge</span>
          </Link>
        </div>

        {/* Filter Controls */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          {/* Search Bar */}
          <div className="relative w-full sm:w-80">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search problems, topics, algorithms..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 rounded-lg border border-slate-800 bg-slate-900/90 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-indigo-500"
            />
          </div>

          {/* Difficulty Segmented Filter Tabs */}
          <div className="flex items-center gap-1 bg-slate-900 p-1 rounded-lg border border-slate-800 text-xs font-mono self-start sm:self-auto">
            {['All', 'Easy', 'Medium', 'Hard'].map((diff) => (
              <button
                key={diff}
                onClick={() => setSelectedDifficulty(diff)}
                className={`px-3 py-1.5 rounded transition-colors ${
                  selectedDifficulty === diff
                    ? 'bg-indigo-600 text-white font-semibold shadow-xs'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                {diff}
              </button>
            ))}
          </div>
        </div>

        {/* Problems List Table */}
        <div className="rounded-xl border border-slate-800 bg-slate-900/50 overflow-hidden">
          <table className="w-full text-left text-xs font-mono">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-950/60 text-slate-400 uppercase tracking-wider text-[11px]">
                <th className="py-3.5 px-4 font-semibold">Title</th>
                <th className="py-3.5 px-4 font-semibold">Topic</th>
                <th className="py-3.5 px-4 font-semibold">Difficulty</th>
                <th className="py-3.5 px-4 font-semibold">Acceptance</th>
                <th className="py-3.5 px-4 font-semibold text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {filteredProblems.length > 0 ? (
                filteredProblems.map((prob) => (
                  <tr key={prob.id} className="hover:bg-slate-900/70 transition-colors group">
                    <td className="py-4 px-4 font-sans font-semibold text-white group-hover:text-indigo-300">
                      {prob.title}
                    </td>
                    <td className="py-4 px-4 text-slate-400">{prob.topic}</td>
                    <td className="py-4 px-4">
                      <span
                        className={
                          prob.difficulty === 'Easy'
                            ? 'text-emerald-400'
                            : prob.difficulty === 'Medium'
                            ? 'text-amber-400'
                            : 'text-rose-400'
                        }
                      >
                        {prob.difficulty}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-slate-400">
                      {prob.acceptanceRate || '52.4%'}
                    </td>
                    <td className="py-4 px-4 text-right">
                      <Link
                        to={`/problem/${prob.id}`}
                        className="inline-flex items-center gap-1 text-xs text-indigo-400 hover:text-indigo-300 font-semibold"
                      >
                        <span>Solve</span>
                        <ChevronRight className="h-3.5 w-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-slate-500">
                    {loading ? 'Fetching problems from REST API...' : 'No matching problems found.'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </DashboardLayout>
  );
};
