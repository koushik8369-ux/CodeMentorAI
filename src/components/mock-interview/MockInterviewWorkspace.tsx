import React, { useState } from 'react';
import { Send, Bot, User, AlertCircle, Sparkles, CheckCircle2 } from 'lucide-react';
import { MockInterview } from '../../types';
import { MockInterviewProgress } from './MockInterviewProgress';
import { MockInterviewQuestion } from './MockInterviewQuestion';
import { MockInterviewEvaluation } from './MockInterviewEvaluation';

interface MockInterviewWorkspaceProps {
  interview: MockInterview;
  onSubmitAnswer: (answer: string) => Promise<void>;
  isSubmitting: boolean;
  onProceedToNextRound: () => void;
  showEvaluation: boolean;
}

export const MockInterviewWorkspace: React.FC<MockInterviewWorkspaceProps> = ({
  interview,
  onSubmitAnswer,
  isSubmitting,
  onProceedToNextRound,
  showEvaluation,
}) => {
  const [answerText, setAnswerText] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);

  const currentQ = interview.currentQuestion;
  const isFollowUpPhase = currentQ?.followUpRequired && !currentQ?.followUpCompleted;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!answerText.trim()) {
      setValidationError('Please enter a response before submitting.');
      return;
    }
    setValidationError(null);
    await onSubmitAnswer(answerText.trim());
    setAnswerText('');
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Round Progress Tracker */}
      <MockInterviewProgress interview={interview} />

      {/* Primary Question or Active Follow-Up */}
      {currentQ && (
        <MockInterviewQuestion
          question={currentQ}
          isFollowUp={isFollowUpPhase}
        />
      )}

      {/* Answer Evaluation Feedback if answered and showing evaluation */}
      {showEvaluation && currentQ && currentQ.score !== undefined && (
        <MockInterviewEvaluation
          question={currentQ}
          onProceed={onProceedToNextRound}
          isLastRound={interview.currentRound >= interview.totalRounds}
        />
      )}

      {/* Candidate Response Workspace (visible when actively waiting for answer) */}
      {(!showEvaluation || isFollowUpPhase) && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-7 space-y-4 shadow-xl">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="p-2 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                <User className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-white font-bold text-sm sm:text-base">
                  {isFollowUpPhase ? 'Your Response to Follow-Up' : 'Your Response'}
                </h3>
                <p className="text-xs text-slate-400">
                  Formulate a clear answer with relevant technical details, trade-offs, and examples.
                </p>
              </div>
            </div>

            <span className="text-xs font-mono text-slate-500">
              {answerText.trim().length} chars
            </span>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="relative">
              <textarea
                value={answerText}
                onChange={(e) => {
                  setAnswerText(e.target.value);
                  if (validationError) setValidationError(null);
                }}
                disabled={isSubmitting}
                rows={7}
                placeholder={
                  isFollowUpPhase
                    ? "Clarify the specific edge cases, implementation nuance, or reasoning for the follow-up question..."
                    : "Structure your explanation: 1) Core principle/concept, 2) Practical implementation or design, 3) Real-world operational trade-offs..."
                }
                className="w-full bg-slate-950 border border-slate-800 rounded-xl p-4 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-cyan-400 font-sans leading-relaxed resize-y min-h-[140px]"
              />
            </div>

            {validationError && (
              <div className="flex items-center gap-2 text-rose-400 text-xs">
                <AlertCircle className="w-4 h-4" />
                <span>{validationError}</span>
              </div>
            )}

            <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2">
              <div className="text-xs text-slate-400 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
                <span>AI will evaluate correctness, technical depth, and communication clarity</span>
              </div>

              <button
                type="submit"
                disabled={isSubmitting || !answerText.trim()}
                className="w-full sm:w-auto px-6 py-3 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs sm:text-sm transition flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/20 disabled:opacity-50 cursor-pointer"
              >
                {isSubmitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                    <span>Evaluating Response with Gemini...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" />
                    <span>{isFollowUpPhase ? 'Submit Follow-Up Answer' : 'Submit Answer'}</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
