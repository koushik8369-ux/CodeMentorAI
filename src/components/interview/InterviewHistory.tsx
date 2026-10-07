import React from 'react';
import {
  History,
  Calendar,
  CheckCircle2,
  Clock,
  ChevronRight,
  Code2,
  Flame,
  Trophy,
} from 'lucide-react';
import { Interview } from '../../types';

interface InterviewHistoryProps {
  interviews: Interview[];
  isLoading: boolean;
  onSelectInterview: (interview: Interview) => void;
  onNewInterview: () => void;
}

export const InterviewHistory: React.FC<InterviewHistoryProps> = ({
  interviews,
  isLoading,
  onSelectInterview,
  onNewInterview,
}) => {
  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'Recently';
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  const getDifficultyBadge = (diff: string) => {
    switch (diff?.toUpperCase()) {
      case 'EASY':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'MEDIUM':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'HARD':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 rounded-2xl p-6">
        <div>
          <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase tracking-wider mb-1">
            <History className="w-4 h-4" />
            <span>Interview Records</span>
          </div>
          <h2 className="text-xl sm:text-2xl font-bold text-white">Previous Interview Attempts</h2>
          <p className="text-slate-400 text-sm mt-1">
            Review your AI-evaluated coding submissions, scores, and mentor feedback.
          </p>
        </div>

        <button
          onClick={onNewInterview}
          className="px-5 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold text-sm transition shadow-md shadow-cyan-500/20 self-start sm:self-auto"
        >
          Start New Interview
        </button>
      </div>

      {/* List */}
      {isLoading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400 space-y-3">
          <div className="w-8 h-8 border-2 border-cyan-400 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Loading interview history...</span>
        </div>
      ) : interviews.length === 0 ? (
        <div className="bg-slate-900/60 border border-dashed border-slate-800 rounded-2xl p-12 text-center space-y-4">
          <div className="w-12 h-12 rounded-full bg-slate-800 text-slate-400 flex items-center justify-center mx-auto">
            <History className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-white font-semibold text-base">No previous interviews found</h3>
            <p className="text-slate-400 text-sm mt-1">
              Start your first AI coding interview to receive detailed evaluations and track progress.
            </p>
          </div>
          <button
            onClick={onNewInterview}
            className="px-5 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 text-sm font-semibold transition"
          >
            Launch First Interview
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {interviews.map((item) => {
            const isCompleted = item.status === 'COMPLETED';
            return (
              <div
                key={item.id}
                onClick={() => onSelectInterview(item)}
                className="bg-slate-900 border border-slate-800 hover:border-slate-700 hover:bg-slate-850 rounded-xl p-5 transition cursor-pointer flex flex-col sm:flex-row sm:items-center justify-between gap-4 group"
              >
                <div className="space-y-1.5 flex-1 min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h4 className="text-white font-semibold text-base group-hover:text-cyan-300 transition truncate">
                      {item.problemTitle}
                    </h4>
                    <span className={`px-2 py-0.5 rounded text-xs font-semibold border ${getDifficultyBadge(item.difficulty)}`}>
                      {item.difficulty}
                    </span>
                    <span className="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700 font-mono">
                      {item.language}
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400">
                    <span>Topic: <strong className="text-slate-300">{item.topic}</strong></span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5" />
                      <span>{formatDate(item.completedAt || item.startedAt || item.createdAt)}</span>
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-4 self-end sm:self-auto">
                  {/* Status & Score */}
                  <div className="text-right">
                    {isCompleted ? (
                      <div className="flex items-center gap-1.5 text-cyan-400 font-bold text-lg">
                        <Trophy className="w-4 h-4 text-amber-400" />
                        <span>{item.score ?? 0}</span>
                        <span className="text-xs text-slate-400 font-normal">/100</span>
                      </div>
                    ) : (
                      <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 border border-amber-500/20 text-amber-400">
                        In Progress
                      </span>
                    )}
                    <div className="text-[11px] text-slate-500 mt-0.5">
                      {isCompleted ? 'Completed' : 'Resume workspace'}
                    </div>
                  </div>

                  <ChevronRight className="w-5 h-5 text-slate-500 group-hover:text-cyan-400 transition transform group-hover:translate-x-1" />
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
