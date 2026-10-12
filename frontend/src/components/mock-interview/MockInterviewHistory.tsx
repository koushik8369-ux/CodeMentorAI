import React from 'react';
import { Calendar, Trophy, ArrowRight, BrainCircuit, Play } from 'lucide-react';
import { MockInterview } from '../../types';

interface MockInterviewHistoryProps {
  interviews: MockInterview[];
  onSelectInterview: (interview: MockInterview) => void;
  onNewInterview: () => void;
}

export const MockInterviewHistory: React.FC<MockInterviewHistoryProps> = ({
  interviews,
  onSelectInterview,
  onNewInterview,
}) => {
  const getReadinessBadge = (level?: string) => {
    switch (level) {
      case 'STRONG CANDIDATE':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
      case 'INTERVIEW READY':
        return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20';
      case 'DEVELOPING':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/20';
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
    }
  };

  if (!interviews || interviews.length === 0) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 sm:p-12 text-center max-w-xl mx-auto space-y-5">
        <div className="w-14 h-14 rounded-2xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 flex items-center justify-center mx-auto">
          <BrainCircuit className="w-7 h-7" />
        </div>
        <div className="space-y-1">
          <h3 className="text-xl font-bold text-white">No Mock Interviews Completed Yet</h3>
          <p className="text-slate-400 text-sm">
            Launch your first interactive multi-round technical interview loop with Gemini.
          </p>
        </div>
        <button
          onClick={onNewInterview}
          className="px-6 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs sm:text-sm transition inline-flex items-center gap-2 shadow-lg shadow-cyan-500/20"
        >
          <Play className="w-4 h-4 fill-current" />
          <span>Start First Mock Interview</span>
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4 max-w-4xl mx-auto">
      <div className="flex items-center justify-between pb-2">
        <h3 className="text-white font-bold text-lg">Your Past Mock Interviews</h3>
        <button
          onClick={onNewInterview}
          className="px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs transition flex items-center gap-1.5 shadow-md shadow-cyan-500/20"
        >
          <Play className="w-3.5 h-3.5 fill-current" />
          <span>New Interview</span>
        </button>
      </div>

      <div className="space-y-3">
        {interviews.map((item) => (
          <div
            key={item.id}
            onClick={() => onSelectInterview(item)}
            className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-2xl p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition cursor-pointer group"
          >
            <div className="space-y-2">
              <div className="flex flex-wrap items-center gap-2">
                <span className="font-bold text-white text-base group-hover:text-cyan-400 transition">
                  {item.topic}
                </span>
                <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-cyan-500/10 text-cyan-300 border border-cyan-500/20">
                  {item.interviewType}
                </span>
                <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                  {item.difficulty}
                </span>
                <span className="px-2 py-0.5 rounded text-[10px] font-mono text-slate-400 bg-slate-950 border border-slate-800">
                  {item.totalRounds} Rounds
                </span>
              </div>

              <div className="flex items-center gap-3 text-xs text-slate-400">
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5" />
                  <span>{item.startedAt ? item.startedAt.slice(0, 10) : 'Recent'}</span>
                </span>
                <span>•</span>
                <span className={item.status === 'COMPLETED' ? 'text-emerald-400' : 'text-amber-400'}>
                  {item.status === 'COMPLETED' ? 'Completed' : `In Progress (Round ${item.currentRound})`}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-4 self-end sm:self-auto">
              {item.overallScore !== undefined && item.overallScore !== null ? (
                <div className="text-right">
                  <div className="text-xl font-extrabold font-mono text-white">
                    {item.overallScore}
                    <span className="text-xs text-slate-400">/100</span>
                  </div>
                  {item.readinessLevel && (
                    <span className={`inline-block px-2 py-0.5 rounded text-[10px] font-semibold border ${getReadinessBadge(item.readinessLevel)}`}>
                      {item.readinessLevel}
                    </span>
                  )}
                </div>
              ) : (
                <span className="text-xs text-slate-400 italic">In Progress</span>
              )}

              <div className="p-2 rounded-xl bg-slate-950 border border-slate-800 text-slate-400 group-hover:text-cyan-400 group-hover:border-slate-700 transition">
                <ArrowRight className="w-4 h-4" />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
