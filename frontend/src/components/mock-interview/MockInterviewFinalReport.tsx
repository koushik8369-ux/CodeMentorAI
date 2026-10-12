import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Trophy,
  CheckCircle2,
  AlertTriangle,
  Lightbulb,
  BarChart3,
  RotateCcw,
  ShieldCheck,
  BrainCircuit,
  MessageSquare,
  Sparkles,
} from 'lucide-react';
import { MockInterview } from '../../types';

interface MockInterviewFinalReportProps {
  interview: MockInterview;
  onRestart: () => void;
}

export const MockInterviewFinalReport: React.FC<MockInterviewFinalReportProps> = ({
  interview,
  onRestart,
}) => {
  const navigate = useNavigate();

  const parseList = (str?: string): string[] => {
    if (!str) return [];
    try {
      const parsed = JSON.parse(str);
      return Array.isArray(parsed) ? parsed : [str];
    } catch {
      return str.split(';').map((s) => s.trim()).filter(Boolean);
    }
  };

  const strengths = parseList(interview.strengths);
  const weaknesses = parseList(interview.weaknesses);
  const recommendations = parseList(interview.recommendations);

  const getReadinessBadge = (level?: string) => {
    switch (level) {
      case 'STRONG CANDIDATE':
        return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
      case 'INTERVIEW READY':
        return 'bg-cyan-500/20 text-cyan-300 border-cyan-500/40';
      case 'DEVELOPING':
        return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
      default:
        return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
    }
  };

  const metrics = [
    {
      title: 'Technical Knowledge',
      score: interview.technicalKnowledge ?? interview.overallScore ?? 80,
      icon: BrainCircuit,
      color: 'text-indigo-400',
      border: 'border-indigo-500/30',
      bg: 'bg-indigo-500/5',
    },
    {
      title: 'Problem Solving',
      score: interview.problemSolving ?? interview.overallScore ?? 80,
      icon: ShieldCheck,
      color: 'text-cyan-400',
      border: 'border-cyan-500/30',
      bg: 'bg-cyan-500/5',
    },
    {
      title: 'Communication',
      score: interview.communication ?? interview.overallScore ?? 80,
      icon: MessageSquare,
      color: 'text-emerald-400',
      border: 'border-emerald-500/30',
      bg: 'bg-emerald-500/5',
    },
    {
      title: 'Confidence & Structure',
      score: interview.confidence ?? interview.overallScore ?? 80,
      icon: Trophy,
      color: 'text-amber-400',
      border: 'border-amber-500/30',
      bg: 'bg-amber-500/5',
    },
  ];

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      {/* Top Banner: Score & Readiness */}
      <div className="bg-gradient-to-br from-slate-900 via-slate-900/95 to-slate-950 border border-cyan-500/30 rounded-3xl p-6 sm:p-10 shadow-2xl relative overflow-hidden">
        <div className="absolute right-0 top-0 translate-x-12 -translate-y-12 w-72 h-72 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="flex flex-col md:flex-row items-center justify-between gap-6 relative z-10">
          <div className="space-y-3 text-center md:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 text-cyan-300 border border-cyan-500/20 text-xs font-semibold uppercase tracking-wider">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Hiring Committee Verdict</span>
            </div>
            <h1 className="text-2xl sm:text-3xl lg:text-4xl font-extrabold text-white tracking-tight">
              Mock Interview Final Report
            </h1>
            <p className="text-slate-400 text-sm max-w-xl">
              Completed {interview.totalRounds}-round {interview.interviewType.toLowerCase()} interview on <strong className="text-white">{interview.topic}</strong> ({interview.difficulty.toLowerCase()} difficulty).
            </p>
          </div>

          <div className="flex flex-col items-center justify-center bg-slate-950/80 border border-slate-800 rounded-2xl p-6 min-w-[200px] text-center shadow-inner">
            <span className="text-xs uppercase tracking-wider font-semibold text-slate-400">
              Composite Score
            </span>
            <div className="text-5xl font-extrabold text-cyan-400 font-mono my-2 tracking-tight">
              {interview.overallScore ?? 80}
              <span className="text-sm font-sans text-slate-500">/100</span>
            </div>
            <div className={`px-3 py-1 rounded-lg text-xs font-mono font-bold border uppercase tracking-wider ${getReadinessBadge(interview.readinessLevel)}`}>
              {interview.readinessLevel || 'INTERVIEW READY'}
            </div>
          </div>
        </div>
      </div>

      {/* 4 Performance Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {metrics.map((m, idx) => {
          const Icon = m.icon;
          return (
            <div
              key={idx}
              className={`bg-slate-900 border ${m.border} ${m.bg} rounded-2xl p-5 space-y-3 transition hover:border-slate-700`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  {m.title}
                </span>
                <div className={`p-2 rounded-xl bg-slate-950 border border-slate-800 ${m.color}`}>
                  <Icon className="w-4 h-4" />
                </div>
              </div>

              <div className="space-y-2">
                <div className="text-2xl font-bold font-mono text-white">
                  {m.score}%
                </div>
                <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full rounded-full bg-cyan-400 transition-all duration-500"
                    style={{ width: `${Math.min(100, Math.max(10, m.score))}%` }}
                  />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Committee Narrative Feedback */}
      {interview.overallFeedback && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-7 space-y-3 shadow-lg">
          <span className="text-xs uppercase font-bold tracking-wider text-cyan-400">
            Hiring Committee Comprehensive Evaluation
          </span>
          <p className="text-slate-200 text-sm sm:text-base leading-relaxed">
            {interview.overallFeedback}
          </p>
        </div>
      )}

      {/* Strengths, Weaknesses, Recommendations */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Strengths */}
        <div className="bg-slate-900 border border-emerald-500/20 rounded-2xl p-5 sm:p-6 space-y-3 shadow-lg">
          <div className="flex items-center gap-2 text-emerald-400 font-bold text-xs uppercase tracking-wider">
            <CheckCircle2 className="w-4 h-4" />
            <span>Demonstrated Strengths</span>
          </div>
          <ul className="space-y-2 text-xs sm:text-sm text-slate-300">
            {strengths.length > 0 ? (
              strengths.map((s, idx) => (
                <li key={idx} className="flex items-start gap-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 mt-2 flex-shrink-0" />
                  <span>{s}</span>
                </li>
              ))
            ) : (
              <li className="text-slate-500 italic">Solid baseline performance</li>
            )}
          </ul>
        </div>

        {/* Weaknesses */}
        <div className="bg-slate-900 border border-rose-500/20 rounded-2xl p-5 sm:p-6 space-y-3 shadow-lg">
          <div className="flex items-center gap-2 text-rose-400 font-bold text-xs uppercase tracking-wider">
            <AlertTriangle className="w-4 h-4" />
            <span>Growth Areas</span>
          </div>
          <ul className="space-y-2 text-xs sm:text-sm text-slate-300">
            {weaknesses.length > 0 ? (
              weaknesses.map((w, idx) => (
                <li key={idx} className="flex items-start gap-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-rose-400 mt-2 flex-shrink-0" />
                  <span>{w}</span>
                </li>
              ))
            ) : (
              <li className="text-slate-500 italic">No significant gaps detected</li>
            )}
          </ul>
        </div>

        {/* Recommendations */}
        <div className="bg-slate-900 border border-cyan-500/20 rounded-2xl p-5 sm:p-6 space-y-3 shadow-lg">
          <div className="flex items-center gap-2 text-cyan-400 font-bold text-xs uppercase tracking-wider">
            <Lightbulb className="w-4 h-4" />
            <span>Next Steps & Drills</span>
          </div>
          <ul className="space-y-2 text-xs sm:text-sm text-slate-300">
            {recommendations.length > 0 ? (
              recommendations.map((r, idx) => (
                <li key={idx} className="flex items-start gap-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 mt-2 flex-shrink-0" />
                  <span>{r}</span>
                </li>
              ))
            ) : (
              <li className="text-slate-500 italic">Continue general algorithm mock practice</li>
            )}
          </ul>
        </div>
      </div>

      {/* Question-by-Question Breakdown Table */}
      {interview.questions && interview.questions.length > 0 && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4 shadow-xl">
          <h3 className="text-white font-bold text-base sm:text-lg">
            Round-by-Round Performance Transcript
          </h3>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-3 px-4 w-16">Round</th>
                  <th className="py-3 px-4">Question & Evaluation</th>
                  <th className="py-3 px-4 text-center w-24">Score</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {interview.questions.map((q) => (
                  <tr key={q.id} className="hover:bg-slate-800/30 transition">
                    <td className="py-4 px-4 font-mono font-bold text-cyan-400 align-top">
                      R{q.roundNumber}
                    </td>
                    <td className="py-4 px-4 space-y-2 align-top">
                      <div className="font-medium text-white text-sm">{q.question}</div>
                      {q.userAnswer && (
                        <div className="text-xs text-slate-400 italic pl-3 border-l border-slate-700">
                          &ldquo;{q.userAnswer}&rdquo;
                        </div>
                      )}
                      {q.feedback && (
                        <div className="text-xs text-slate-300 pt-1">
                          <strong className="text-cyan-400">Feedback:</strong> {q.feedback}
                        </div>
                      )}
                      {q.followUpQuestion && (
                        <div className="text-xs text-amber-300/90 pt-1">
                          <strong>Follow-up:</strong> {q.followUpQuestion}
                        </div>
                      )}
                    </td>
                    <td className="py-4 px-4 text-center font-mono font-bold align-top">
                      <span className="px-2.5 py-1 rounded-lg text-xs bg-slate-950 border border-slate-800 text-white">
                        {q.score ?? '—'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-4 border-t border-slate-800">
        <button
          onClick={() => navigate('/analytics')}
          className="w-full sm:w-auto px-6 py-3 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-700 font-semibold text-sm transition flex items-center justify-center gap-2"
        >
          <BarChart3 className="w-4 h-4" />
          <span>View Detailed Analytics</span>
        </button>

        <button
          onClick={onRestart}
          className="w-full sm:w-auto px-6 py-3 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-sm transition flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/20"
        >
          <RotateCcw className="w-4 h-4" />
          <span>Start Another Interview</span>
        </button>
      </div>
    </div>
  );
};
