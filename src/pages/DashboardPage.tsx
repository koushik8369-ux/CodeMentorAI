import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Sparkles,
  TrendingUp,
  Award,
  CheckCircle2,
  Clock,
  ArrowRight,
  Flame,
  Calendar,
  Layers,
  ChevronRight,
  AlertCircle,
  Database,
} from 'lucide-react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api, HealthStatus } from '../services/api';
import { InterviewSession } from '../types';

// Structured baseline metrics for V1 preview (will connect to user telemetry API in V2)
const DEMO_METRICS = {
  readinessPercentage: 78,
  problemsSolved: 42,
  interviewsCompleted: 8,
  averageScore: 81,
  currentStreakDays: 6,
};

export const DashboardPage: React.FC = () => {
  const [interviews, setInterviews] = useState<InterviewSession[]>([]);
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 1. Fetch real health status from Spring Boot
    api.getHealth()
      .then((h) => setHealth(h))
      .catch(() => setHealth({ status: 'OFFLINE', service: 'CodeMentor AI (Spring Boot 8080)' }));

    // 2. Fetch real interview records from Spring Boot
    api.getInterviews()
      .then((data) => {
        setInterviews(data);
        setLoading(false);
      })
      .catch((err) => {
        console.warn('Could not load interviews from Spring Boot at localhost:8080', err);
        setLoading(false);
      });
  }, []);

  // Compute live values if interviews exist, otherwise display baseline demo metrics
  const completedCount = interviews.length > 0 ? interviews.length : DEMO_METRICS.interviewsCompleted;
  const avgScore = interviews.length > 0
    ? Math.round(interviews.reduce((acc, i) => acc + (i.score || 0), 0) / interviews.length)
    : DEMO_METRICS.averageScore;

  const skillScores = [
    { name: 'Java', score: 82 },
    { name: 'Data Structures', score: 71 },
    { name: 'Algorithms', score: 64 },
    { name: 'SQL', score: 84 },
    { name: 'Spring Boot', score: 73 },
  ];

  const recommendedPractice = [
    {
      id: 'binary-search',
      title: 'Binary Search',
      difficulty: 'Medium',
      topic: 'Algorithms',
      description: 'Search target element in sorted array with guaranteed O(log n) efficiency.',
    },
    {
      id: 'two-sum',
      title: 'HashMap Problems',
      difficulty: 'Medium',
      topic: 'Data Structures',
      description: 'Master fast lookups, collision strategies, and frequency tracking patterns.',
    },
    {
      id: 'spring-rest-service',
      title: 'Spring Boot REST APIs',
      difficulty: 'Hard',
      topic: 'Backend',
      description: 'Design production endpoints with validation, exception handlers, and pagination.',
    },
  ];

  return (
    <DashboardLayout>
      <div className="max-w-6xl mx-auto space-y-8">
        {/* Welcome Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-850 pb-6">
          <div>
            <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
              <span>CANDIDATE WORKSPACE</span>
              <span className="text-slate-600">·</span>
              <span className="text-slate-400">Spring Boot Backend: {health?.status || 'CHECKING...'}</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2">
              Welcome back 👋
            </h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Your preparation diagnostic is up-to-date. Next recommended focus: <span className="text-indigo-400 font-medium">Algorithms (O(log n) patterns)</span>.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/interview-setup"
              className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-xs font-semibold text-white hover:bg-indigo-500 transition-all shadow-sm shadow-indigo-500/20"
            >
              <Sparkles className="h-4 w-4" />
              <span>Start New Interview</span>
            </Link>
          </div>
        </div>

        {/* Primary Metric Stat Cards */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-[11px] font-mono text-slate-400 px-1">
            <span>Core Performance Metrics (V1 Candidate Diagnostic)</span>
            <span>Target: L5 Senior SWE</span>
          </div>

          <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
            {/* Metric 1 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
                <span>Interview Readiness</span>
                <TrendingUp className="h-4 w-4 text-emerald-400" />
              </div>
              <div className="text-2xl font-extrabold text-white font-mono">{DEMO_METRICS.readinessPercentage}%</div>
              <p className="text-[11px] text-emerald-400 font-mono">+4% from last mock</p>
            </div>

            {/* Metric 2 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
                <span>Problems Solved</span>
                <CheckCircle2 className="h-4 w-4 text-indigo-400" />
              </div>
              <div className="text-2xl font-extrabold text-white font-mono">{DEMO_METRICS.problemsSolved}</div>
              <p className="text-[11px] text-slate-400 font-mono">32 Medium · 10 Hard</p>
            </div>

            {/* Metric 3 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
                <span>Interviews Completed</span>
                <Award className="h-4 w-4 text-sky-400" />
              </div>
              <div className="text-2xl font-extrabold text-white font-mono">{completedCount}</div>
              <p className="text-[11px] text-slate-400 font-mono">Target: 12 sessions</p>
            </div>

            {/* Metric 4 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
                <span>Average Score</span>
                <TrendingUp className="h-4 w-4 text-indigo-400" />
              </div>
              <div className="text-2xl font-extrabold text-white font-mono">{avgScore}%</div>
              <p className="text-[11px] text-indigo-400 font-mono">Above 75th percentile</p>
            </div>

            {/* Metric 5 */}
            <div className="col-span-2 lg:col-span-1 rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
                <span>Current Streak</span>
                <Flame className="h-4 w-4 text-amber-400" />
              </div>
              <div className="text-2xl font-extrabold text-amber-400 font-mono">{DEMO_METRICS.currentStreakDays} days</div>
              <p className="text-[11px] text-slate-400 font-mono">Best: 14 days</p>
            </div>
          </div>
        </div>

        {/* Middle Two-Column Grid: Skill Performance & Recommended Practice */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Skill Performance Panel */}
          <div className="lg:col-span-6 rounded-xl border border-slate-800 bg-slate-900/50 p-6 flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h3 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
                  Skill Performance
                </h3>
                <span className="text-xs text-slate-400 font-mono">Target: &gt; 80%</span>
              </div>

              <div className="space-y-4 pt-1">
                {skillScores.map((skill) => (
                  <div key={skill.name} className="space-y-1.5">
                    <div className="flex items-center justify-between text-xs font-mono">
                      <span className="text-slate-300">{skill.name}</span>
                      <span className="text-white font-semibold">{skill.score}%</span>
                    </div>
                    {/* Visual Progress Bar */}
                    <div className="h-2 w-full rounded-full bg-slate-800 overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${
                          skill.score >= 80
                            ? 'bg-emerald-500'
                            : skill.score >= 70
                            ? 'bg-indigo-500'
                            : 'bg-amber-500'
                        }`}
                        style={{ width: `${skill.score}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs font-mono text-slate-400">
              <span>Diagnostic based on 42 problems</span>
              <Link to="/analytics" className="text-indigo-400 hover:underline">
                View Full Analytics →
              </Link>
            </div>
          </div>

          {/* Recommended Practice Cards */}
          <div className="lg:col-span-6 rounded-xl border border-slate-800 bg-slate-900/50 p-6 flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h3 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
                  Recommended Practice
                </h3>
                <span className="text-xs text-indigo-400 font-mono">Spring Boot & Algorithms</span>
              </div>

              <div className="space-y-3 pt-1">
                {recommendedPractice.map((rec) => (
                  <Link
                    key={rec.title}
                    to={rec.id === 'two-sum' ? '/problem/two-sum' : `/problem/${rec.id}`}
                    className="block p-3.5 rounded-lg border border-slate-800 bg-slate-900/70 hover:border-indigo-500/40 hover:bg-slate-900 transition-all group"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-semibold text-white group-hover:text-indigo-300 transition-colors">
                          {rec.title}
                        </span>
                        <span className="text-[11px] font-mono text-slate-400">·</span>
                        <span className="text-[11px] font-mono text-slate-400">{rec.topic}</span>
                      </div>
                      <span
                        className={`text-[10px] font-mono font-medium ${
                          rec.difficulty === 'Easy'
                            ? 'text-emerald-400'
                            : rec.difficulty === 'Medium'
                            ? 'text-amber-400'
                            : 'text-rose-400'
                        }`}
                      >
                        {rec.difficulty}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 mt-1 line-clamp-1">
                      {rec.description}
                    </p>
                  </Link>
                ))}
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs font-mono text-slate-400">
              <span>Updated after each evaluation</span>
              <Link to="/problems" className="text-indigo-400 hover:underline">
                Explore Problem Bank →
              </Link>
            </div>
          </div>
        </div>

        {/* Recent Interviews Table */}
        <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-6 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-850 pb-3">
            <div>
              <h3 className="text-base font-bold text-white">Recent Interviews</h3>
              <p className="text-xs text-slate-400">Endpoint: GET http://localhost:8080/api/interviews</p>
            </div>
            <Link
              to="/interview-setup"
              className="text-xs font-mono text-indigo-400 hover:underline flex items-center gap-1"
            >
              <span>Schedule New</span>
              <ChevronRight className="h-3.5 w-3.5" />
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 uppercase tracking-wider text-[11px]">
                  <th className="pb-3 font-semibold">Date</th>
                  <th className="pb-3 font-semibold">Role</th>
                  <th className="pb-3 font-semibold">Difficulty</th>
                  <th className="pb-3 font-semibold">Score</th>
                  <th className="pb-3 font-semibold text-right">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {interviews.length > 0 ? (
                  interviews.map((item) => (
                    <tr key={item.id} className="hover:bg-slate-900/60 transition-colors">
                      <td className="py-3 text-slate-400">{item.createdAt}</td>
                      <td className="py-3 font-sans font-medium text-white">{item.role}</td>
                      <td className="py-3">
                        <span
                          className={
                            item.difficulty === 'Easy'
                              ? 'text-emerald-400'
                              : item.difficulty === 'Medium'
                              ? 'text-amber-400'
                              : 'text-rose-400'
                          }
                        >
                          {item.difficulty}
                        </span>
                      </td>
                      <td className="py-3 font-semibold text-white">
                        {item.score ? `${item.score}%` : '—'}
                      </td>
                      <td className="py-3 text-right">
                        <span
                          className={`inline-block ${
                            item.status === 'Completed'
                              ? 'text-emerald-400'
                              : item.status === 'In Progress'
                              ? 'text-indigo-400'
                              : 'text-amber-400'
                          }`}
                        >
                          {item.status}
                        </span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="py-6 text-center text-slate-500">
                      {loading ? 'Querying Spring Boot /api/interviews...' : 'No interview sessions recorded yet.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};
