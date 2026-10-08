import React from 'react';
import { Flame, Code2 } from 'lucide-react';
import { DifficultyAnalytics, LanguageAnalytics } from '../../types';

interface DifficultyLanguageCardProps {
  difficulties: DifficultyAnalytics[];
  languages: LanguageAnalytics[];
}

export const DifficultyLanguageCard: React.FC<DifficultyLanguageCardProps> = ({
  difficulties,
  languages,
}) => {
  const getDifficultyMeta = (diff: string) => {
    switch (diff.toUpperCase()) {
      case 'EASY':
        return { label: 'Easy', badge: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' };
      case 'MEDIUM':
        return { label: 'Medium', badge: 'text-amber-400 bg-amber-500/10 border-amber-500/30' };
      case 'HARD':
        return { label: 'Hard', badge: 'text-rose-400 bg-rose-500/10 border-rose-500/30' };
      default:
        return { label: diff, badge: 'text-slate-400 bg-slate-800 border-slate-700' };
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {/* Difficulty Breakdown */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <Flame className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-white font-semibold text-base">Difficulty Performance</h3>
            <p className="text-xs text-slate-400">Scoring metrics segmented by difficulty tiers</p>
          </div>
        </div>

        <div className="space-y-3 pt-1">
          {difficulties.map((d, idx) => {
            const meta = getDifficultyMeta(d.difficulty);
            return (
              <div
                key={idx}
                className="bg-slate-950/70 border border-slate-800 rounded-xl p-3.5 flex items-center justify-between gap-4"
              >
                <div className="flex items-center gap-2.5">
                  <span className={`px-2.5 py-1 rounded-lg text-xs font-semibold border ${meta.badge}`}>
                    {meta.label}
                  </span>
                  <span className="text-xs text-slate-400">
                    {d.attempts} {d.attempts === 1 ? 'attempt' : 'attempts'}
                  </span>
                </div>

                <div className="flex items-center gap-4 text-right">
                  <div>
                    <span className="text-[10px] uppercase tracking-wider text-slate-500 block">
                      Avg Score
                    </span>
                    <span className="font-mono text-sm font-bold text-white">
                      {d.attempts > 0 ? `${d.averageScore.toFixed(1)}%` : '—'}
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] uppercase tracking-wider text-slate-500 block">
                      Best
                    </span>
                    <span className="font-mono text-sm font-bold text-cyan-400">
                      {d.attempts > 0 ? `${d.bestScore}/100` : '—'}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Language Breakdown */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Code2 className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-white font-semibold text-base">Programming Languages</h3>
            <p className="text-xs text-slate-400">Execution performance across language runtimes</p>
          </div>
        </div>

        <div className="space-y-3 pt-1">
          {languages.length === 0 ? (
            <div className="py-6 text-center text-xs text-slate-500">
              No language data recorded yet.
            </div>
          ) : (
            languages.map((l, idx) => (
              <div
                key={idx}
                className="bg-slate-950/70 border border-slate-800 rounded-xl p-3.5 flex items-center justify-between gap-4"
              >
                <div className="flex items-center gap-2.5">
                  <span className="px-2.5 py-1 rounded-lg text-xs font-mono font-semibold bg-indigo-500/10 text-indigo-300 border border-indigo-500/30">
                    {l.language}
                  </span>
                  <span className="text-xs text-slate-400">
                    {l.attempts} {l.attempts === 1 ? 'interview' : 'interviews'}
                  </span>
                </div>

                <div className="text-right">
                  <span className="text-[10px] uppercase tracking-wider text-slate-500 block">
                    Average Score
                  </span>
                  <span className="font-mono text-sm font-bold text-white">
                    {l.averageScore.toFixed(1)} / 100
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
