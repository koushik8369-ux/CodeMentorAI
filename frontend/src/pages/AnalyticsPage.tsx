import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api } from '../services/api';
import {
  AnalyticsOverview,
  TopicAnalytics,
  DifficultyAnalytics,
  LanguageAnalytics,
  PerformanceTrend,
  AiInsights,
} from '../types';
import { SummaryCards } from '../components/analytics/SummaryCards';
import { PerformanceTrendChart } from '../components/analytics/PerformanceTrendChart';
import { TopicPerformanceTable } from '../components/analytics/TopicPerformanceTable';
import { DifficultyLanguageCard } from '../components/analytics/DifficultyLanguageCard';
import { AiImprovementPlan } from '../components/analytics/AiImprovementPlan';
import { AnalyticsEmptyState } from '../components/analytics/AnalyticsEmptyState';
import { BarChart3, AlertCircle, RefreshCw } from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [topics, setTopics] = useState<TopicAnalytics[]>([]);
  const [difficulties, setDifficulties] = useState<DifficultyAnalytics[]>([]);
  const [languages, setLanguages] = useState<LanguageAnalytics[]>([]);
  const [trend, setTrend] = useState<PerformanceTrend[]>([]);
  const [aiInsights, setAiInsights] = useState<AiInsights | null>(null);

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isAiLoading, setIsAiLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    loadAnalytics();
  }, []);

  const loadAnalytics = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const [overviewData, topicsData, diffData, langData, trendData] = await Promise.all([
        api.getAnalyticsOverview(),
        api.getTopicAnalytics(),
        api.getDifficultyAnalytics(),
        api.getLanguageAnalytics(),
        api.getPerformanceTrend(),
      ]);

      setOverview(overviewData);
      setTopics(topicsData);
      setDifficulties(diffData);
      setLanguages(langData);
      setTrend(trendData);

      // If user has completed interviews, also trigger AI insights in background
      if (overviewData.completedInterviews > 0) {
        loadAiInsights();
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load performance analytics.');
    } finally {
      setIsLoading(false);
    }
  };

  const loadAiInsights = async () => {
    setIsAiLoading(true);
    try {
      const insights = await api.getAiInsights();
      setAiInsights(insights);
    } catch (err) {
      console.warn('AI Insights retrieval failed', err);
    } finally {
      setIsAiLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-8">
        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase tracking-wider mb-1">
              <BarChart3 className="w-4 h-4" />
              <span>Performance Intelligence</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Interview Analytics & AI Coaching
            </h1>
            <p className="text-slate-400 text-sm mt-1">
              Data-backed evaluation of your algorithmic interview readiness and personalized improvement trajectory.
            </p>
          </div>

          <button
            onClick={loadAnalytics}
            disabled={isLoading}
            className="self-start sm:self-auto px-4 py-2 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 text-xs font-semibold transition flex items-center gap-2 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-cyan-400' : ''}`} />
            <span>Refresh Analytics</span>
          </button>
        </div>

        {/* Error notification */}
        {errorMessage && (
          <div className="bg-rose-950/30 border border-rose-500/30 rounded-2xl p-4 text-rose-300 text-sm flex items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-400" />
              <span>{errorMessage}</span>
            </div>
            <button
              onClick={loadAnalytics}
              className="text-xs font-semibold underline hover:text-white"
            >
              Retry
            </button>
          </div>
        )}

        {/* Loading state skeleton */}
        {isLoading ? (
          <div className="py-24 flex flex-col items-center justify-center space-y-3 text-slate-400">
            <div className="w-10 h-10 border-2 border-cyan-400 border-t-transparent rounded-full animate-spin" />
            <span className="text-sm font-medium">Aggregating PostgreSQL interview statistics...</span>
          </div>
        ) : !overview || overview.completedInterviews === 0 ? (
          /* Empty state when 0 completed interviews */
          <AnalyticsEmptyState />
        ) : (
          /* Full Analytics Dashboard */
          <div className="space-y-8">
            {/* Top 4 Summary Cards + Strongest/Weakest Areas */}
            <SummaryCards overview={overview} />

            {/* Performance Trend Chart */}
            <PerformanceTrendChart trend={trend} />

            {/* Topic Performance Table */}
            <TopicPerformanceTable topics={topics} />

            {/* Difficulty & Language Segment Breakdown */}
            <DifficultyLanguageCard
              difficulties={difficulties}
              languages={languages}
            />

            {/* Gemini Personalized AI Improvement Plan */}
            <AiImprovementPlan
              insights={aiInsights}
              isLoading={isAiLoading}
              onRefresh={loadAiInsights}
              hasInterviews={overview.completedInterviews > 0}
            />
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};
