import React from 'react';
import { Target, CheckCircle2, Clock } from 'lucide-react';
import { MockInterview } from '../../types';

interface MockInterviewProgressProps {
  interview: MockInterview;
}

export const MockInterviewProgress: React.FC<MockInterviewProgressProps> = ({ interview }) => {
  const rounds = Array.from({ length: interview.totalRounds }, (_, i) => i + 1);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 sm:p-5 space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            <Target className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-white font-bold text-sm sm:text-base">
                Round {interview.currentRound} of {interview.totalRounds}
              </span>
              <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-cyan-500/10 text-cyan-300 border border-cyan-500/20">
                {interview.interviewType}
              </span>
              <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                {interview.difficulty}
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">Topic: <strong className="text-slate-200">{interview.topic}</strong></p>
          </div>
        </div>

        <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
          <Clock className="w-3.5 h-3.5" />
          <span>Active Session</span>
        </div>
      </div>

      {/* Round Stepper */}
      <div className="flex items-center gap-2 overflow-x-auto py-1">
        {rounds.map((r) => {
          const isDone = r < interview.currentRound || (interview.status === 'COMPLETED' && r <= interview.totalRounds);
          const isCurrent = r === interview.currentRound && interview.status !== 'COMPLETED';

          return (
            <div
              key={r}
              className={`flex-1 min-w-[36px] h-2.5 rounded-full transition-all duration-300 ${
                isDone
                  ? 'bg-emerald-500 shadow-sm shadow-emerald-500/20'
                  : isCurrent
                  ? 'bg-cyan-400 animate-pulse ring-2 ring-cyan-400/40'
                  : 'bg-slate-800'
              }`}
              title={`Round ${r}`}
            />
          );
        })}
      </div>
    </div>
  );
};
