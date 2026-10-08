import React from 'react';
import { Sparkles, ThumbsUp, AlertTriangle, Lightbulb, CheckCircle2, RefreshCw } from 'lucide-react';
import { AiInsights } from '../../types';

interface AiImprovementPlanProps {
  insights: AiInsights | null;
  isLoading: boolean;
  onRefresh: () => void;
  hasInterviews: boolean;
}

export const AiImprovementPlan: React.FC<AiImprovementPlanProps> = ({
  insights,
  isLoading,
  onRefresh,
  hasInterviews,
}) => {
  return (
    <div className="bg-gradient-to-br from-slate-900 via-slate-900/95 to-slate-950 border border-cyan-500/30 rounded-2xl p-6 sm:p-8 space-y-6 relative overflow-hidden shadow-xl">
      <div className="absolute right-0 top-0 translate-x-12 -translate-y-12 w-64 h-64 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                Personalized AI Improvement Plan
              </h2>
              <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-300 border border-cyan-500/40">
                Gemini 3.8 Flash
              </span>
            </div>
            <p className="text-slate-400 text-xs sm:text-sm mt-0.5">
              Targeted skill diagnostic and step-by-step interview readiness strategy
            </p>
          </div>
        </div>

        {hasInterviews && (
          <button
            onClick={onRefresh}
            disabled={isLoading}
            className="self-start sm:self-auto px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition flex items-center gap-2 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-cyan-400' : ''}`} />
            <span>{isLoading ? 'Synthesizing...' : 'Re-analyze with AI'}</span>
          </button>
        )}
      </div>

      {isLoading && !insights ? (
        <div className="py-12 flex flex-col items-center justify-center space-y-3 text-slate-400">
          <div className="w-8 h-8 border-2 border-cyan-400 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Synthesizing personalized diagnostic insights...</span>
        </div>
      ) : !insights ? (
        <div className="text-slate-400 text-sm py-4">
          Click &ldquo;Re-analyze with AI&rdquo; to generate your customized interview action plan.
        </div>
      ) : (
        <div className="space-y-6">
          {/* Overall Assessment */}
          <div className="bg-slate-950/80 border border-slate-800 rounded-xl p-5 space-y-2">
            <span className="text-xs uppercase tracking-wider text-cyan-400 font-semibold">
              Holistic Mentor Assessment
            </span>
            <p className="text-slate-200 text-sm sm:text-base leading-relaxed">
              {insights.overallAssessment}
            </p>
          </div>

          {/* Skill Tag Breakdown */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Strong Topics */}
            <div className="bg-slate-950/60 border border-emerald-500/20 rounded-xl p-4 space-y-2.5">
              <div className="flex items-center gap-2 text-emerald-400 font-semibold text-xs uppercase tracking-wider">
                <ThumbsUp className="w-3.5 h-3.5" />
                <span>Demonstrated Strengths</span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {insights.strongTopics && insights.strongTopics.length > 0 ? (
                  insights.strongTopics.map((st, idx) => (
                    <span
                      key={idx}
                      className="px-2.5 py-1 rounded-lg text-xs font-medium bg-emerald-500/10 text-emerald-300 border border-emerald-500/20"
                    >
                      {st}
                    </span>
                  ))
                ) : (
                  <span className="text-xs text-slate-500">None identified yet</span>
                )}
              </div>
            </div>

            {/* Weak Topics */}
            <div className="bg-slate-950/60 border border-rose-500/20 rounded-xl p-4 space-y-2.5">
              <div className="flex items-center gap-2 text-rose-400 font-semibold text-xs uppercase tracking-wider">
                <AlertTriangle className="w-3.5 h-3.5" />
                <span>Areas Needing Growth</span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {insights.weakTopics && insights.weakTopics.length > 0 ? (
                  insights.weakTopics.map((wt, idx) => (
                    <span
                      key={idx}
                      className="px-2.5 py-1 rounded-lg text-xs font-medium bg-rose-500/10 text-rose-300 border border-rose-500/20"
                    >
                      {wt}
                    </span>
                  ))
                ) : (
                  <span className="text-xs text-slate-500">None flagged</span>
                )}
              </div>
            </div>

            {/* Recommended Next Topics & Next Difficulty */}
            <div className="bg-slate-950/60 border border-cyan-500/20 rounded-xl p-4 space-y-2.5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-cyan-400 font-semibold text-xs uppercase tracking-wider">
                  <Lightbulb className="w-3.5 h-3.5" />
                  <span>Next Recommended</span>
                </div>
                {insights.nextDifficulty && (
                  <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-cyan-500/20 text-cyan-300 border border-cyan-500/30">
                    TARGET: {insights.nextDifficulty}
                  </span>
                )}
              </div>
              <div className="flex flex-wrap gap-1.5">
                {insights.recommendedTopics && insights.recommendedTopics.length > 0 ? (
                  insights.recommendedTopics.map((rt, idx) => (
                    <span
                      key={idx}
                      className="px-2.5 py-1 rounded-lg text-xs font-medium bg-cyan-500/10 text-cyan-300 border border-cyan-500/20"
                    >
                      {rt}
                    </span>
                  ))
                ) : (
                  <span className="text-xs text-slate-500">Arrays & Strings</span>
                )}
              </div>
            </div>
          </div>

          {/* Action Plan Drills */}
          {insights.actionPlan && insights.actionPlan.length > 0 && (
            <div className="space-y-3">
              <span className="text-xs uppercase tracking-wider text-slate-400 font-semibold">
                Action Plan & Preparation Drills
              </span>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {insights.actionPlan.map((step, idx) => (
                  <div
                    key={idx}
                    className="bg-slate-950/80 border border-slate-800 rounded-xl p-4 space-y-2 hover:border-slate-700 transition"
                  >
                    <div className="flex items-center gap-2">
                      <span className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 text-xs font-mono font-bold flex items-center justify-center">
                        {idx + 1}
                      </span>
                      <h4 className="text-white font-semibold text-sm">{step.topic}</h4>
                    </div>
                    <p className="text-xs text-slate-400 leading-relaxed">
                      <strong className="text-slate-300">Diagnosis:</strong> {step.reason}
                    </p>
                    <div className="pt-1 border-t border-slate-800/80 text-xs text-cyan-300/90 leading-relaxed">
                      <strong>Prescribed Drill:</strong> {step.recommendedPractice}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Summary verdict */}
          {insights.summary && (
            <div className="p-4 rounded-xl bg-cyan-950/30 border border-cyan-500/20 text-xs sm:text-sm text-cyan-200">
              {insights.summary}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
