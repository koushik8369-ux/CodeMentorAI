import React, { useState } from 'react';
import { Sparkles, Terminal, Code2, Play, Flame, BookOpen, Layers } from 'lucide-react';

interface InterviewSetupProps {
  onStart: (topic: string, difficulty: string, language: string) => void;
  isLoading: boolean;
  error?: string | null;
}

export const InterviewSetup: React.FC<InterviewSetupProps> = ({ onStart, isLoading, error }) => {
  const [topic, setTopic] = useState('Arrays');
  const [difficulty, setDifficulty] = useState('MEDIUM');
  const [language, setLanguage] = useState('JAVA');

  const topics = [
    'Arrays',
    'Strings',
    'Linked Lists',
    'Stacks & Queues',
    'Trees',
    'Searching',
    'Sorting',
    'Recursion',
    'Dynamic Programming',
  ];

  const difficulties = [
    { id: 'EASY', label: 'Easy', badge: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' },
    { id: 'MEDIUM', label: 'Medium', badge: 'bg-amber-500/10 text-amber-400 border-amber-500/20' },
    { id: 'HARD', label: 'Hard', badge: 'bg-rose-500/10 text-rose-400 border-rose-500/20' },
  ];

  const languages = [
    { id: 'JAVA', label: 'Java', ext: '.java' },
    { id: 'PYTHON', label: 'Python', ext: '.py' },
    { id: 'CPP', label: 'C++', ext: '.cpp' },
    { id: 'JAVASCRIPT', label: 'JavaScript', ext: '.js' },
  ];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isLoading) return;
    onStart(topic, difficulty, language);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-cyan-950/40 via-blue-950/30 to-slate-900 border border-cyan-500/20 rounded-2xl p-6 sm:p-8 relative overflow-hidden">
        <div className="absolute right-0 top-0 translate-x-10 -translate-y-10 w-64 h-64 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="flex items-center gap-3 text-cyan-400 mb-2">
          <Sparkles className="w-5 h-5" />
          <span className="text-xs uppercase tracking-wider font-semibold">Gemini 3.8 Flash Powered</span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
          AI Coding Interview Engine
        </h1>
        <p className="text-slate-400 mt-2 text-sm sm:text-base max-w-2xl leading-relaxed">
          Configure your simulation parameters. The backend AI generates a custom, production-grade algorithmic problem tailored to your language and target difficulty.
        </p>
      </div>

      {error && (
        <div className="bg-rose-950/30 border border-rose-500/30 rounded-xl p-4 text-rose-300 text-sm flex items-start gap-3">
          <span className="font-semibold">Error:</span> {error}
        </div>
      )}

      {/* Configuration Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 1. Topic Selection */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="flex items-center gap-2 text-white font-medium">
            <BookOpen className="w-5 h-5 text-cyan-400" />
            <span>Select Interview Topic</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {topics.map((t) => {
              const active = topic === t;
              return (
                <button
                  type="button"
                  key={t}
                  onClick={() => setTopic(t)}
                  className={`px-4 py-3 rounded-xl text-sm font-medium text-left transition-all border ${
                    active
                      ? 'bg-cyan-500/10 border-cyan-500/40 text-cyan-300 shadow-sm shadow-cyan-500/10'
                      : 'bg-slate-950/60 border-slate-800 text-slate-300 hover:border-slate-700 hover:text-white'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span>{t}</span>
                    {active && <div className="w-2 h-2 rounded-full bg-cyan-400" />}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* 2. Difficulty & Language Selection */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Difficulty */}
          <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 space-y-4">
            <div className="flex items-center gap-2 text-white font-medium">
              <Flame className="w-5 h-5 text-amber-400" />
              <span>Target Difficulty</span>
            </div>
            <div className="grid grid-cols-3 gap-3">
              {difficulties.map((d) => {
                const active = difficulty === d.id;
                return (
                  <button
                    type="button"
                    key={d.id}
                    onClick={() => setDifficulty(d.id)}
                    className={`py-3 px-3 rounded-xl text-center text-sm font-semibold transition-all border ${
                      active
                        ? `${d.badge} border-current ring-1 ring-current/20`
                        : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                    }`}
                  >
                    {d.label}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Programming Language */}
          <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 space-y-4">
            <div className="flex items-center gap-2 text-white font-medium">
              <Code2 className="w-5 h-5 text-indigo-400" />
              <span>Programming Language</span>
            </div>
            <div className="grid grid-cols-2 gap-3">
              {languages.map((l) => {
                const active = language === l.id;
                return (
                  <button
                    type="button"
                    key={l.id}
                    onClick={() => setLanguage(l.id)}
                    className={`py-3 px-4 rounded-xl text-left text-sm font-medium transition-all border flex items-center justify-between ${
                      active
                        ? 'bg-indigo-500/10 border-indigo-500/40 text-indigo-300'
                        : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                    }`}
                  >
                    <span>{l.label}</span>
                    <span className="text-xs font-mono text-slate-500">{l.ext}</span>
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        {/* Start Button */}
        <div className="pt-2 flex justify-end">
          <button
            type="submit"
            disabled={isLoading}
            className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-semibold text-base transition-all duration-200 shadow-lg shadow-cyan-500/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-3"
          >
            {isLoading ? (
              <>
                <div className="w-5 h-5 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                <span>Generating AI Problem...</span>
              </>
            ) : (
              <>
                <Play className="w-5 h-5 fill-current" />
                <span>Start Interview</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
