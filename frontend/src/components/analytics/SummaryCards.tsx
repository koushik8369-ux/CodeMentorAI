import React from 'react';
import { Trophy, Target, TrendingUp, Award, Zap, AlertCircle } from 'lucide-react';
import { AnalyticsOverview } from '../../types';

interface SummaryCardsProps {
  overview: AnalyticsOverview;
}

export const SummaryCards: React.FC<SummaryCardsProps> = ({ overview }) => {
  const cards = [
    {
      title: 'Total Completed',
      value: overview.completedInterviews,
      suffix: '',
      icon: Target,
      color: 'text-cyan-400',
      border: 'border-cyan-500/20',
      bg: 'bg-cyan-500/5',
      subtitle: `${overview.totalInterviews} sessions registered`,
    },
    {
      title: 'Average Score',
      value: overview.averageScore,
      suffix: '/100',
      icon: Award,
      color: 'text-blue-400',
      border: 'border-blue-500/20',
      bg: 'bg-blue-500/5',
      subtitle: 'Across all completed drills',
    },
    {
      title: 'Highest Score',
      value: overview.highestScore,
      suffix: '/100',
      icon: Trophy,
      color: 'text-amber-400',
      border: 'border-amber-500/20',
      bg: 'bg-amber-500/5',
      subtitle: `Lowest: ${overview.lowestScore}/100`,
    },
    {
      title: 'Recent Average',
      value: overview.recentAverageScore,
      suffix: '/100',
      icon: TrendingUp,
      color: 'text-emerald-400',
      border: 'border-emerald-500/20',
      bg: 'bg-emerald-500/5',
      subtitle: 'Last 5 completed interviews',
    },
  ];

  return (
    <div className="space-y-4">
      {/* 4 Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <div
              key={idx}
              className={`bg-slate-900 border ${card.border} ${card.bg} rounded-2xl p-5 transition hover:border-slate-700 space-y-3`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs uppercase tracking-wider font-semibold text-slate-400">
                  {card.title}
                </span>
                <div className={`p-2 rounded-xl bg-slate-950 border border-slate-800 ${card.color}`}>
                  <Icon className="w-4 h-4" />
                </div>
              </div>

              <div>
                <div className="flex items-baseline gap-1">
                  <span className="text-3xl font-bold text-white tracking-tight">
                    {card.value}
                  </span>
                  {card.suffix && (
                    <span className="text-xs text-slate-400 font-mono">{card.suffix}</span>
                  )}
                </div>
                <p className="text-xs text-slate-400 mt-1">{card.subtitle}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Strongest & Weakest Area Highlight Banner */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-slate-900 border border-emerald-500/20 rounded-2xl p-4 flex items-center gap-4">
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <Zap className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">
              Strongest Topic
            </span>
            <div className="text-lg font-bold text-white">
              {overview.strongestTopic || 'N/A'}
            </div>
            <p className="text-xs text-slate-400">Highest composite score trajectory</p>
          </div>
        </div>

        <div className="bg-slate-900 border border-rose-500/20 rounded-2xl p-4 flex items-center gap-4">
          <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400 border border-rose-500/30">
            <AlertCircle className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs font-semibold text-rose-400 uppercase tracking-wider">
              Focus Growth Area
            </span>
            <div className="text-lg font-bold text-white">
              {overview.weakestTopic || 'N/A'}
            </div>
            <p className="text-xs text-slate-400">Recommended for targeted algorithm drills</p>
          </div>
        </div>
      </div>
    </div>
  );
};
