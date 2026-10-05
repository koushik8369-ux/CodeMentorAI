import React, { useState } from 'react';
import {
  BarChart3,
  TrendingUp,
  Clock,
  Target,
  AlertTriangle,
  Award,
  CheckCircle2,
  Calendar,
  ArrowUpRight,
  BookOpen,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { DashboardLayout } from '../layouts/DashboardLayout';

export const AnalyticsPage: React.FC = () => {
  const [timeRange, setTimeRange] = useState<'30d' | '90d' | 'all'>('30d');

  const topicPerformance = [
    { name: 'Arrays & Strings', accuracy: 88, solved: 18, total: 20 },
    { name: 'SQL & Database Design', accuracy: 84, solved: 12, total: 14 },
    { name: 'Hash Tables & Sets', accuracy: 80, solved: 15, total: 18 },
    { name: 'Spring Boot Architecture', accuracy: 74, solved: 8, total: 11 },
    { name: 'Binary Search & Sorts', accuracy: 68, solved: 9, total: 13 },
    { name: 'Trees & Graphs', accuracy: 62, solved: 7, total: 12 },
    { name: 'Dynamic Programming', accuracy: 48, solved: 4, total: 9 },
  ];

  const interviewScores = [
    { date: 'Sep 12', score: 68, label: 'L4 Diagnostic' },
    { date: 'Sep 18', score: 72, label: 'Algos & Trees' },
    { date: 'Sep 24', score: 79, label: 'System + Java' },
    { date: 'Sep 29', score: 81, label: 'Spring Boot' },
    { date: 'Oct 03', score: 84, label: 'Meta Mock Loop' },
  ];

  const weakAreas = [
    {
      topic: 'Dynamic Programming (2D Memoization)',
      accuracy: '48%',
      recommendation: 'Practice Grid Path & Knapsack variations before starting hard string transformations.',
      problemSlug: 'two-sum',
    },
    {
      topic: 'Graph Cycle Detection & Topological Sort',
      accuracy: '55%',
      recommendation: 'Review Kahn algorithm vs DFS post-order traversal for dependency resolution.',
      problemSlug: 'binary-search',
    },
    {
      topic: 'Java Concurrency & Thread Synchronization',
      accuracy: '60%',
      recommendation: 'Practice synchronized blocks, ReentrantLock, and thread pool starvation issues.',
      problemSlug: 'spring-rest-service',
    },
  ];

  return (
    <DashboardLayout>
      <div className="max-w-6xl mx-auto space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-850 pb-6">
          <div>
            <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
              <BarChart3 className="h-4 w-4" />
              <span>CANDIDATE TELEMETRY</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Performance Analytics
            </h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Data-driven insights assessing algorithmic accuracy, interview readiness, and skill gaps.
            </p>
          </div>

          <div className="flex items-center gap-1 bg-slate-900 p-1 rounded-lg border border-slate-800 text-xs font-mono">
            <button
              onClick={() => setTimeRange('30d')}
              className={`px-3 py-1.5 rounded transition-colors ${
                timeRange === '30d' ? 'bg-indigo-600 text-white font-semibold' : 'text-slate-400 hover:text-white'
              }`}
            >
              Last 30 Days
            </button>
            <button
              onClick={() => setTimeRange('90d')}
              className={`px-3 py-1.5 rounded transition-colors ${
                timeRange === '90d' ? 'bg-indigo-600 text-white font-semibold' : 'text-slate-400 hover:text-white'
              }`}
            >
              Last 90 Days
            </button>
            <button
              onClick={() => setTimeRange('all')}
              className={`px-3 py-1.5 rounded transition-colors ${
                timeRange === 'all' ? 'bg-indigo-600 text-white font-semibold' : 'text-slate-400 hover:text-white'
              }`}
            >
              All Time
            </button>
          </div>
        </div>

        {/* Top 4 KPI Metrics */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-5 space-y-1">
            <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
              <span>Overall Score</span>
              <Award className="h-4 w-4 text-indigo-400" />
            </div>
            <div className="text-3xl font-extrabold text-white font-mono">81%</div>
            <p className="text-xs text-emerald-400 font-mono">+13% trajectory</p>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-5 space-y-1">
            <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
              <span>Problem Accuracy</span>
              <Target className="h-4 w-4 text-emerald-400" />
            </div>
            <div className="text-3xl font-extrabold text-white font-mono">76.4%</div>
            <p className="text-xs text-slate-400 font-mono">42 of 55 attempted</p>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-5 space-y-1">
            <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
              <span>Average Coding Time</span>
              <Clock className="h-4 w-4 text-cyan-400" />
            </div>
            <div className="text-3xl font-extrabold text-white font-mono">24.8m</div>
            <p className="text-xs text-emerald-400 font-mono">-5.2m faster than baseline</p>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-5 space-y-1">
            <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
              <span>Mock Velocity</span>
              <TrendingUp className="h-4 w-4 text-amber-400" />
            </div>
            <div className="text-3xl font-extrabold text-white font-mono">2.4 / wk</div>
            <p className="text-xs text-slate-400 font-mono">Recommended: 2–3</p>
          </div>
        </div>

        {/* Charts Section: Interview Performance Trend & Topic Mastery */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Interview Performance Timeline */}
          <div className="lg:col-span-7 rounded-xl border border-slate-800 bg-slate-900/50 p-6 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h3 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
                  Interview Performance Trend
                </h3>
                <span className="text-xs font-mono text-emerald-400">Consistent Uptrend</span>
              </div>

              {/* Bar/Timeline Chart Simulation */}
              <div className="mt-8 space-y-4">
                <div className="h-48 flex items-end justify-between gap-4 pt-4 px-2 border-b border-slate-800">
                  {interviewScores.map((scoreItem) => (
                    <div key={scoreItem.date} className="flex-1 flex flex-col items-center gap-2 group">
                      <span className="text-xs font-mono text-slate-300 font-semibold group-hover:text-indigo-400">
                        {scoreItem.score}%
                      </span>
                      <div className="w-full bg-slate-800 rounded-t-sm h-full flex items-end">
                        <div
                          className="w-full bg-gradient-to-t from-indigo-700 to-indigo-500 rounded-t-sm group-hover:from-indigo-600 group-hover:to-cyan-400 transition-all"
                          style={{ height: `${scoreItem.score}%` }}
                        />
                      </div>
                      <span className="text-[11px] font-mono text-slate-400 mt-2">
                        {scoreItem.date}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs font-mono text-slate-400">
              <span>Evaluated against L5 Senior SWE Rubrics</span>
              <span className="text-indigo-400">8 Mock Sessions Total</span>
            </div>
          </div>

          {/* Topic Performance Bars */}
          <div className="lg:col-span-5 rounded-xl border border-slate-800 bg-slate-900/50 p-6 flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h3 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
                  Topic Performance
                </h3>
                <span className="text-xs font-mono text-slate-400">Accuracy</span>
              </div>

              <div className="space-y-3 pt-1">
                {topicPerformance.map((topic) => (
                  <div key={topic.name} className="space-y-1">
                    <div className="flex items-center justify-between text-xs font-mono">
                      <span className="text-slate-300 truncate max-w-[180px]">{topic.name}</span>
                      <span className="text-white font-semibold">{topic.accuracy}%</span>
                    </div>
                    <div className="h-1.5 w-full bg-slate-800 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          topic.accuracy >= 75
                            ? 'bg-emerald-500'
                            : topic.accuracy >= 60
                            ? 'bg-indigo-500'
                            : 'bg-rose-500'
                        }`}
                        style={{ width: `${topic.accuracy}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs font-mono text-slate-400">
              <span>Benchmark: FAANG Pass = 75%+</span>
              <span className="text-emerald-400">4 / 7 Passing</span>
            </div>
          </div>
        </div>

        {/* Weak Areas Diagnosis & Targeted Action */}
        <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-6 space-y-4">
          <div className="flex items-center gap-2 border-b border-slate-800 pb-3">
            <AlertTriangle className="h-5 w-5 text-amber-400" />
            <h3 className="text-base font-bold text-white">Identified Weak Areas & Recommended Focus</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {weakAreas.map((weak) => (
              <div
                key={weak.topic}
                className="rounded-lg border border-slate-800 bg-slate-900/80 p-4 space-y-3 flex flex-col justify-between"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-white font-mono">{weak.topic}</span>
                    <span className="text-xs font-mono text-rose-400">{weak.accuracy}</span>
                  </div>
                  <p className="text-xs text-slate-400 leading-relaxed">
                    {weak.recommendation}
                  </p>
                </div>

                <div className="pt-2 border-t border-slate-800/60">
                  <Link
                    to={`/problem/${weak.problemSlug}`}
                    className="inline-flex items-center gap-1.5 text-xs font-mono text-indigo-400 hover:text-indigo-300 font-semibold"
                  >
                    <span>Practice Drill Problem</span>
                    <ArrowUpRight className="h-3.5 w-3.5" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};
