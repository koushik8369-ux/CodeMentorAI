import React from 'react';
import { Layers, Award, BarChart3 } from 'lucide-react';
import { TopicAnalytics } from '../../types';

interface TopicPerformanceTableProps {
  topics: TopicAnalytics[];
}

export const TopicPerformanceTable: React.FC<TopicPerformanceTableProps> = ({ topics }) => {
  if (!topics || topics.length === 0) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 text-center text-slate-400 text-sm">
        No topic metrics available yet.
      </div>
    );
  }

  const getScoreColor = (score: number) => {
    if (score >= 85) return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
    if (score >= 70) return 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20';
    if (score >= 50) return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
    return 'text-rose-400 bg-rose-500/10 border-rose-500/20';
  };

  const getBarColor = (score: number) => {
    if (score >= 85) return 'bg-emerald-500';
    if (score >= 70) return 'bg-cyan-500';
    if (score >= 50) return 'bg-amber-500';
    return 'bg-rose-500';
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            <BarChart3 className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-white font-semibold text-base">Topic Breakdown</h3>
            <p className="text-xs text-slate-400">Ranked by average algorithmic score</p>
          </div>
        </div>
        <span className="text-xs text-slate-400 font-mono">{topics.length} Topics Tested</span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
              <th className="py-3 px-4">Topic</th>
              <th className="py-3 px-4 text-center">Attempts</th>
              <th className="py-3 px-4">Average Score</th>
              <th className="py-3 px-4 text-right">Best Score</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {topics.map((t, idx) => (
              <tr key={idx} className="hover:bg-slate-800/30 transition">
                <td className="py-3.5 px-4">
                  <div className="font-medium text-white flex items-center gap-2">
                    <span className="text-slate-500 text-xs font-mono">{idx + 1}.</span>
                    <span>{t.topic}</span>
                  </div>
                </td>
                <td className="py-3.5 px-4 text-center font-mono text-slate-300">
                  {t.attempts}
                </td>
                <td className="py-3.5 px-4">
                  <div className="flex items-center gap-3">
                    <div className="w-24 sm:w-36 h-2 rounded-full bg-slate-800 overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${getBarColor(
                          t.averageScore
                        )}`}
                        style={{ width: `${Math.min(100, Math.max(5, t.averageScore))}%` }}
                      />
                    </div>
                    <span className="font-mono font-semibold text-slate-200 text-xs">
                      {t.averageScore.toFixed(1)}
                    </span>
                  </div>
                </td>
                <td className="py-3.5 px-4 text-right">
                  <span
                    className={`inline-block px-2.5 py-1 rounded-lg text-xs font-mono font-bold border ${getScoreColor(
                      t.bestScore
                    )}`}
                  >
                    {t.bestScore}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
