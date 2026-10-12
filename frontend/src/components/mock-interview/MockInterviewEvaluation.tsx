import React from 'react';
import { Award, CheckCircle2, AlertTriangle, Lightbulb, User, ArrowRight, CornerDownRight } from 'lucide-react';
import { MockInterviewQuestion } from '../../types';

interface MockInterviewEvaluationProps {
  question: MockInterviewQuestion;
  onProceed?: () => void;
  isLastRound?: boolean;
}

export const MockInterviewEvaluation: React.FC<MockInterviewEvaluationProps> = ({
  question,
  onProceed,
  isLastRound,
}) => {
  const parseList = (str?: string): string[] => {
    if (!str) return [];
    try {
      const parsed = JSON.parse(str);
      return Array.isArray(parsed) ? parsed : [str];
    } catch {
      return str.split(';').map((s) => s.trim()).filter(Boolean);
    }
  };

  const strengths = parseList(question.strengths);
  const weaknesses = parseList(question.weaknesses);

  const getScoreColor = (score?: number) => {
    if (!score) return 'text-slate-400 bg-slate-800 border-slate-700';
    if (score >= 85) return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
    if (score >= 70) return 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30';
    if (score >= 50) return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
    return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-7 space-y-6 shadow-xl">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-4">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Award className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-white font-bold text-base">Round {question.roundNumber} Evaluation</h3>
            <p className="text-xs text-slate-400">Interviewer diagnostic evaluation & feedback</p>
          </div>
        </div>

        {question.score !== undefined && (
          <div className={`px-3 py-1.5 rounded-xl font-mono text-sm font-bold border flex items-center gap-2 ${getScoreColor(question.score)}`}>
            <span>Round Score:</span>
            <span className="text-base">{question.score} / 100</span>
          </div>
        )}
      </div>

      {/* Candidate Answer recap */}
      {question.userAnswer && (
        <div className="bg-slate-950/60 border border-slate-800 rounded-xl p-4 space-y-1.5">
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
            <User className="w-3.5 h-3.5 text-cyan-400" />
            <span>Your Submitted Answer:</span>
          </div>
          <p className="text-slate-300 text-sm italic pl-5 border-l-2 border-cyan-500/40">
            &ldquo;{question.userAnswer}&rdquo;
          </p>
        </div>
      )}

      {/* 3 Detailed Criteria Pillars */}
      {(question.correctness || question.technicalDepth || question.communication) && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {question.correctness && (
            <div className="bg-slate-950/80 border border-slate-800/80 rounded-xl p-3.5 space-y-1">
              <span className="text-[10px] uppercase font-bold tracking-wider text-slate-400">
                Correctness & Accuracy
              </span>
              <p className="text-xs text-slate-300 leading-relaxed">{question.correctness}</p>
            </div>
          )}
          {question.technicalDepth && (
            <div className="bg-slate-950/80 border border-slate-800/80 rounded-xl p-3.5 space-y-1">
              <span className="text-[10px] uppercase font-bold tracking-wider text-slate-400">
                Technical Depth & Trade-offs
              </span>
              <p className="text-xs text-slate-300 leading-relaxed">{question.technicalDepth}</p>
            </div>
          )}
          {question.communication && (
            <div className="bg-slate-950/80 border border-slate-800/80 rounded-xl p-3.5 space-y-1">
              <span className="text-[10px] uppercase font-bold tracking-wider text-slate-400">
                Communication & Clarity
              </span>
              <p className="text-xs text-slate-300 leading-relaxed">{question.communication}</p>
            </div>
          )}
        </div>
      )}

      {/* Strengths & Weaknesses */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {strengths.length > 0 && (
          <div className="bg-slate-950/70 border border-emerald-500/20 rounded-xl p-4 space-y-2">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-400 uppercase tracking-wider">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Demonstrated Strengths</span>
            </div>
            <ul className="space-y-1.5">
              {strengths.map((s, idx) => (
                <li key={idx} className="text-xs text-slate-300 flex items-start gap-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 mt-1.5 flex-shrink-0" />
                  <span>{s}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {weaknesses.length > 0 && (
          <div className="bg-slate-950/70 border border-rose-500/20 rounded-xl p-4 space-y-2">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-rose-400 uppercase tracking-wider">
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>Gaps & Missed Considerations</span>
            </div>
            <ul className="space-y-1.5">
              {weaknesses.map((w, idx) => (
                <li key={idx} className="text-xs text-slate-300 flex items-start gap-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-rose-400 mt-1.5 flex-shrink-0" />
                  <span>{w}</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Mentor Feedback */}
      {question.feedback && (
        <div className="bg-slate-950/90 border border-indigo-500/20 rounded-xl p-4 space-y-1.5">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-indigo-400 uppercase tracking-wider">
            <Lightbulb className="w-3.5 h-3.5" />
            <span>Interviewer Feedback</span>
          </div>
          <p className="text-slate-200 text-xs sm:text-sm leading-relaxed">{question.feedback}</p>
        </div>
      )}

      {/* Follow-up Question Answer & Score if answered */}
      {question.followUpAnswer && (
        <div className="bg-amber-950/20 border border-amber-500/30 rounded-xl p-4 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-amber-400 uppercase tracking-wider">
              Follow-Up Evaluation
            </span>
            {question.followUpScore !== undefined && (
              <span className="text-xs font-mono font-bold text-amber-300">
                Score: {question.followUpScore} / 100
              </span>
            )}
          </div>
          <p className="text-xs text-slate-300 italic">&ldquo;{question.followUpAnswer}&rdquo;</p>
          {question.followUpFeedback && (
            <p className="text-xs text-amber-200/90 pt-1 border-t border-amber-500/20">
              {question.followUpFeedback}
            </p>
          )}
        </div>
      )}

      {onProceed && (
        <div className="pt-2 flex justify-end">
          <button
            onClick={onProceed}
            className="px-5 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold text-xs sm:text-sm transition flex items-center gap-2 shadow-lg shadow-cyan-500/20"
          >
            <span>{isLastRound ? 'Generate Final Report' : 'Proceed to Next Round'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  );
};
