import React from 'react';
import {
  Trophy,
  CheckCircle2,
  Clock,
  Cpu,
  ThumbsUp,
  AlertTriangle,
  Lightbulb,
  ArrowRight,
  RotateCcw,
  BookOpen,
} from 'lucide-react';
import { Interview } from '../../types';

interface InterviewResultProps {
  interview: Interview;
  onNewInterview: () => void;
  onViewHistory: () => void;
}

export const InterviewResult: React.FC<InterviewResultProps> = ({
  interview,
  onNewInterview,
  onViewHistory,
}) => {
  const score = interview.score ?? 0;

  const getScoreColor = (val: number) => {
    if (val >= 85) return 'text-emerald-400 border-emerald-500/30 bg-emerald-500/10';
    if (val >= 70) return 'text-cyan-400 border-cyan-500/30 bg-cyan-500/10';
    if (val >= 50) return 'text-amber-400 border-amber-500/30 bg-amber-500/10';
    return 'text-rose-400 border-rose-500/30 bg-rose-500/10';
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Score Hero Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-cyan-950/40 to-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-8 flex flex-col sm:flex-row items-center justify-between gap-6 shadow-xl">
        <div className="space-y-2 text-center sm:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-cyan-500/10 border border-cyan-500/20 text-cyan-400">
            <Trophy className="w-3.5 h-3.5" />
            <span>Interview Evaluation Complete</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white">
            {interview.problemTitle}
          </h1>
          <div className="flex flex-wrap items-center justify-center sm:justify-start gap-3 text-xs sm:text-sm text-slate-400">
            <span>Topic: <strong className="text-slate-200">{interview.topic}</strong></span>
            <span>•</span>
            <span>Difficulty: <strong className="text-slate-200">{interview.difficulty}</strong></span>
            <span>•</span>
            <span>Language: <strong className="text-slate-200 font-mono">{interview.language}</strong></span>
          </div>
        </div>

        {/* Score Badge Gauge */}
        <div className="flex flex-col items-center">
          <div
            className={`w-28 h-28 rounded-2xl border-2 flex flex-col items-center justify-center font-bold shadow-lg ${getScoreColor(
              score
            )}`}
          >
            <span className="text-4xl tracking-tight">{score}</span>
            <span className="text-xs uppercase tracking-wider opacity-80">/ 100</span>
          </div>
        </div>
      </div>

      {/* Primary Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Correctness */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-2">
          <div className="flex items-center gap-2 text-emerald-400 font-medium text-sm">
            <CheckCircle2 className="w-4 h-4" />
            <span>Correctness & Logic Soundness</span>
          </div>
          <p className="text-slate-300 text-sm leading-relaxed">
            {interview.correctness || 'Algorithmic logic evaluated by AI mentor.'}
          </p>
        </div>

        {/* Code Quality */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-2">
          <div className="flex items-center gap-2 text-cyan-400 font-medium text-sm">
            <Trophy className="w-4 h-4" />
            <span>Code Quality & Idiomatic Style</span>
          </div>
          <p className="text-slate-300 text-sm leading-relaxed">
            {interview.codeQuality || 'Structure, naming, and readability analysis.'}
          </p>
        </div>

        {/* Time Complexity */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-2">
          <div className="flex items-center gap-2 text-amber-400 font-medium text-sm">
            <Clock className="w-4 h-4" />
            <span>Time Complexity</span>
          </div>
          <p className="text-slate-300 text-sm font-mono leading-relaxed">
            {interview.timeComplexity || 'O(n)'}
          </p>
        </div>

        {/* Space Complexity */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-2">
          <div className="flex items-center gap-2 text-indigo-400 font-medium text-sm">
            <Cpu className="w-4 h-4" />
            <span>Space Complexity</span>
          </div>
          <p className="text-slate-300 text-sm font-mono leading-relaxed">
            {interview.spaceComplexity || 'O(1)'}
          </p>
        </div>
      </div>

      {/* Strengths & Weaknesses */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Strengths */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-emerald-400 font-medium text-sm">
            <ThumbsUp className="w-4 h-4" />
            <span>Identified Strengths</span>
          </div>
          <ul className="space-y-2">
            {interview.strengths && interview.strengths.length > 0 ? (
              interview.strengths.map((str, idx) => (
                <li key={idx} className="flex items-start gap-2 text-xs sm:text-sm text-slate-300">
                  <span className="text-emerald-400 mt-1">•</span>
                  <span>{str}</span>
                </li>
              ))
            ) : (
              <li className="text-xs text-slate-400">Solid algorithmic formulation</li>
            )}
          </ul>
        </div>

        {/* Weaknesses */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-rose-400 font-medium text-sm">
            <AlertTriangle className="w-4 h-4" />
            <span>Areas to Watch</span>
          </div>
          <ul className="space-y-2">
            {interview.weaknesses && interview.weaknesses.length > 0 ? (
              interview.weaknesses.map((weak, idx) => (
                <li key={idx} className="flex items-start gap-2 text-xs sm:text-sm text-slate-300">
                  <span className="text-rose-400 mt-1">•</span>
                  <span>{weak}</span>
                </li>
              ))
            ) : (
              <li className="text-xs text-slate-400">Minor boundary condition considerations</li>
            )}
          </ul>
        </div>
      </div>

      {/* Recommendations & Improvement Topics */}
      {interview.recommendations && interview.recommendations.length > 0 && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-amber-400 font-medium text-sm">
            <Lightbulb className="w-4 h-4" />
            <span>Recommended Improvement Topics</span>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
            {interview.recommendations.map((rec, idx) => (
              <div
                key={idx}
                className="bg-slate-950/70 border border-slate-800/80 rounded-lg p-3 text-xs text-slate-300 flex items-start gap-2"
              >
                <span className="text-amber-400 font-bold">{idx + 1}.</span>
                <span>{rec}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Overall Feedback */}
      {interview.aiFeedback && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-2">
          <div className="flex items-center gap-2 text-cyan-400 font-medium text-sm">
            <BookOpen className="w-4 h-4" />
            <span>Mentor Feedback & Next Steps</span>
          </div>
          <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-line">
            {interview.aiFeedback}
          </p>
        </div>
      )}

      {/* Submitted Code Preview */}
      {interview.userCode && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-2">
          <h3 className="text-xs uppercase tracking-wider text-slate-400 font-semibold">
            Submitted Code ({interview.language})
          </h3>
          <pre className="p-4 bg-slate-950 rounded-lg border border-slate-800 text-xs text-cyan-200 font-mono overflow-x-auto leading-relaxed">
            {interview.userCode}
          </pre>
        </div>
      )}

      {/* Actions */}
      <div className="flex flex-wrap items-center justify-between gap-4 pt-4 border-t border-slate-800">
        <button
          onClick={onViewHistory}
          className="px-5 py-2.5 rounded-xl border border-slate-700 bg-slate-800/80 hover:bg-slate-700 text-slate-200 text-sm font-medium transition"
        >
          View Interview History
        </button>

        <button
          onClick={onNewInterview}
          className="px-6 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 text-sm font-semibold transition shadow-md shadow-cyan-500/20 flex items-center gap-2"
        >
          <RotateCcw className="w-4 h-4" />
          <span>Start New Interview</span>
        </button>
      </div>
    </div>
  );
};
