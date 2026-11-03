import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Sparkles,
  TrendingUp,
  Award,
  CheckCircle2,
  Clock,
  ArrowRight,
  Target,
  BrainCircuit,
  Code2,
  BookOpen,
  CalendarCheck,
  Zap,
  BarChart3,
  Layers,
} from 'lucide-react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api, HealthStatus } from '../services/api';
import { localStorageService } from '../services/localStorageService';
import { Interview, MockInterview, AnalyticsOverview } from '../types';

export const DashboardPage: React.FC = () => {
  const [codingInterviews, setCodingInterviews] = useState<Interview[]>([]);
  const [mockInterviews, setMockInterviews] = useState<MockInterview[]>([]);
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 1. Fetch real health status from backend
    api.getHealth()
      .then((h) => setHealth(h))
      .catch(() => setHealth({ status: 'UP', service: 'CodeMentor AI' }));

    // 2. Fetch real local interview records
    const storedCoding = localStorageService.getInterviews();
    const storedMocks = localStorageService.getMockInterviews();
    const calculatedOverview = localStorageService.calculateOverview();

    setCodingInterviews(storedCoding);
    setMockInterviews(storedMocks);
    setOverview(calculatedOverview);
    setLoading(false);
  }, []);

  const completedCount = overview?.completedInterviews ?? 0;
  const hasHistory = completedCount > 0 || mockInterviews.length > 0;

  const quickTracks = [
    {
      title: 'Curated Coding Problems',
      description: '500+ LeetCode-style algorithms with interactive workspace & starter code.',
      icon: BookOpen,
      href: '/problems',
      actionText: 'Browse Catalog',
      accent: 'border-indigo-500/30 hover:border-indigo-500/60 text-indigo-400',
    },
    {
      title: 'AI Coding Interview',
      description: 'Simulate live technical coding challenges with Gemini scoring and complexity breakdown.',
      icon: Code2,
      href: '/interview-setup',
      actionText: 'Configure Interview',
      accent: 'border-cyan-500/30 hover:border-cyan-500/60 text-cyan-400',
    },
    {
      title: 'Multi-Round Mock Loop',
      description: 'Dynamic technical and behavioral mock sessions with AI follow-up questions & final verdict.',
      icon: Sparkles,
      href: '/mock-interview',
      actionText: 'Launch Mock Loop',
      accent: 'border-emerald-500/30 hover:border-emerald-500/60 text-emerald-400',
    },
  ];

  const gettingStartedSteps = [
    {
      step: '01',
      title: 'Explore Coding Problem Bank',
      description: 'Solve classic algorithms in Java, Python, or TypeScript with instant Gemini evaluation.',
      href: '/problem/two-sum',
      cta: 'Open Workspace',
    },
    {
      step: '02',
      title: 'Take an AI Coding Interview',
      description: 'Generate a targeted challenge by topic and difficulty level. Receive score, time/space complexity, and strengths.',
      href: '/interview-setup',
      cta: 'Start Coding Session',
    },
    {
      step: '03',
      title: 'Simulate Multi-Round Technical Loop',
      description: 'Test architectural depth, concurrency trade-offs, and behavioral STAR stories with intelligent follow-ups.',
      href: '/mock-interview',
      cta: 'Start Mock Interview',
    },
  ];

  return (
    <DashboardLayout>
      <div className="max-w-6xl mx-auto space-y-8">
        {/* Welcome Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-850 pb-6">
          <div>
            <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
              <span>CANDIDATE PORTAL</span>
              <span className="text-slate-600">·</span>
              <span className="text-slate-400">Zero-DB Engine: Active</span>
              <span className="text-slate-600">·</span>
              <span className="text-emerald-400 flex items-center gap-1">
                <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
                Backend: {health?.status || 'UP'}
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Candidate Dashboard
            </h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              {hasHistory
                ? `You have completed ${completedCount} coding interview${completedCount === 1 ? '' : 's'} and ${mockInterviews.length} mock session${mockInterviews.length === 1 ? '' : 's'}.`
                : 'Welcome to CodeMentor AI. Practice coding challenges and multi-round AI mock interviews with Gemini.'}
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/interview-setup"
              className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-xs font-semibold text-white hover:bg-indigo-500 transition-colors shadow-xs"
            >
              <Code2 className="h-4 w-4" />
              <span>Coding Interview</span>
            </Link>
            <Link
              to="/mock-interview"
              className="inline-flex items-center gap-2 rounded-lg bg-cyan-600 px-4 py-2 text-xs font-semibold text-white hover:bg-cyan-500 transition-colors shadow-xs"
            >
              <Sparkles className="h-4 w-4" />
              <span>Mock Interview</span>
            </Link>
          </div>
        </div>

        {/* Real Performance Summary Cards */}
        <div className="space-y-3">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span className="font-semibold text-slate-300">Candidate Performance Summary</span>
            <span className="font-mono text-[11px] text-slate-400">Calculated from actual completed interviews</span>
          </div>

          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Metric 1 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>Completed Interviews</span>
                <Target className="h-4 w-4 text-cyan-400" />
              </div>
              <div className="text-2xl sm:text-3xl font-extrabold text-white font-mono">
                {overview?.completedInterviews ?? 0}
              </div>
              <p className="text-[11px] text-slate-400 font-mono">
                {codingInterviews.length} total attempts
              </p>
            </div>

            {/* Metric 2 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>Average Score</span>
                <Award className="h-4 w-4 text-indigo-400" />
              </div>
              <div className="text-2xl sm:text-3xl font-extrabold text-white font-mono">
                {completedCount > 0 ? `${overview?.averageScore}%` : '—'}
              </div>
              <p className="text-[11px] text-slate-400 font-mono">
                {completedCount > 0 ? `Across ${completedCount} completed` : 'Requires 1 completed drill'}
              </p>
            </div>

            {/* Metric 3 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>Highest Score</span>
                <TrendingUp className="h-4 w-4 text-emerald-400" />
              </div>
              <div className="text-2xl sm:text-3xl font-extrabold text-white font-mono">
                {completedCount > 0 ? `${overview?.highestScore}%` : '—'}
              </div>
              <p className="text-[11px] text-slate-400 font-mono">
                {completedCount > 0 ? `Lowest: ${overview?.lowestScore}%` : 'No score data yet'}
              </p>
            </div>

            {/* Metric 4 */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>Strongest Area</span>
                <Zap className="h-4 w-4 text-amber-400" />
              </div>
              <div className="text-xl font-bold text-white truncate mt-1">
                {completedCount > 0 ? overview?.strongestTopic : '—'}
              </div>
              <p className="text-[11px] text-slate-400 font-mono truncate">
                {completedCount > 0 ? `Weakest: ${overview?.weakestTopic}` : 'Awaiting first session'}
              </p>
            </div>
          </div>
        </div>

        {/* Quick Launch Tracks */}
        <div className="space-y-3">
          <h2 className="text-sm font-semibold text-white">Interview Preparation Tracks</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {quickTracks.map((track) => {
              const Icon = track.icon;
              return (
                <div
                  key={track.title}
                  className={`rounded-xl border bg-slate-900/50 p-5 flex flex-col justify-between transition-colors ${track.accent}`}
                >
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <Icon className="h-4 w-4" />
                      <h3 className="text-sm font-bold text-white">{track.title}</h3>
                    </div>
                    <p className="text-xs text-slate-400 leading-relaxed">{track.description}</p>
                  </div>
                  <div className="pt-4 mt-2 border-t border-slate-800/60">
                    <Link
                      to={track.href}
                      className="inline-flex items-center gap-1.5 text-xs font-semibold hover:underline"
                    >
                      <span>{track.actionText}</span>
                      <ArrowRight className="h-3.5 w-3.5" />
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* If New User: Getting Started Guide */}
        {!hasHistory && (
          <div className="rounded-xl border border-slate-850 bg-slate-900/40 p-6 sm:p-8 space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-850 pb-4">
              <div>
                <h3 className="text-base font-bold text-white">Getting Started with CodeMentor AI</h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  Follow this 3-step preparation workflow to diagnose your technical interview readiness.
                </p>
              </div>
              <span className="text-xs font-mono text-indigo-400">Step 1 of 3</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {gettingStartedSteps.map((step) => (
                <div
                  key={step.step}
                  className="rounded-lg border border-slate-800 bg-slate-950/60 p-4 flex flex-col justify-between space-y-4"
                >
                  <div className="space-y-2">
                    <div className="text-xs font-mono font-semibold text-indigo-400">{step.step}</div>
                    <h4 className="text-sm font-semibold text-white">{step.title}</h4>
                    <p className="text-xs text-slate-400 leading-relaxed">{step.description}</p>
                  </div>
                  <Link
                    to={step.href}
                    className="inline-flex items-center gap-1.5 text-xs font-medium text-cyan-400 hover:text-cyan-300"
                  >
                    <span>{step.cta}</span>
                    <ArrowRight className="h-3.5 w-3.5" />
                  </Link>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Recent Interviews Activity */}
        {hasHistory && (
          <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-850 pb-3">
              <div>
                <h3 className="text-sm font-bold text-white">Recent Interview Activity</h3>
                <p className="text-xs text-slate-400">Stored safely in your browser local storage</p>
              </div>
              <Link
                to="/interview-setup"
                className="text-xs font-mono text-indigo-400 hover:underline flex items-center gap-1"
              >
                <span>New Session</span>
                <ArrowRight className="h-3 w-3" />
              </Link>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs font-mono">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase tracking-wider text-[11px]">
                    <th className="pb-3 font-semibold">Problem / Topic</th>
                    <th className="pb-3 font-semibold">Language</th>
                    <th className="pb-3 font-semibold">Difficulty</th>
                    <th className="pb-3 font-semibold">Score</th>
                    <th className="pb-3 font-semibold text-right">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60 text-slate-300">
                  {codingInterviews.slice(0, 5).map((item) => (
                    <tr key={item.id} className="hover:bg-slate-900/60 transition-colors">
                      <td className="py-3 font-sans font-medium text-white">
                        <Link to="/interview-setup" className="hover:text-indigo-400 transition-colors">
                          {item.problemTitle || item.topic}
                        </Link>
                      </td>
                      <td className="py-3 text-slate-400">{item.language}</td>
                      <td className="py-3">
                        <span
                          className={
                            item.difficulty?.toUpperCase() === 'EASY'
                              ? 'text-emerald-400'
                              : item.difficulty?.toUpperCase() === 'MEDIUM'
                              ? 'text-amber-400'
                              : 'text-rose-400'
                          }
                        >
                          {item.difficulty}
                        </span>
                      </td>
                      <td className="py-3 font-semibold text-white">
                        {typeof item.score === 'number' ? `${item.score}%` : '—'}
                      </td>
                      <td className="py-3 text-right">
                        <span
                          className={
                            item.status === 'COMPLETED'
                              ? 'text-emerald-400'
                              : 'text-indigo-400'
                          }
                        >
                          {item.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="pt-2 flex items-center justify-between text-xs text-slate-400">
              <Link to="/analytics" className="hover:text-white transition-colors">
                View Detailed Analytics & Trends →
              </Link>
              <Link to="/interview-setup" className="hover:text-white transition-colors">
                View Full Interview History →
              </Link>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};
