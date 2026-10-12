import React from 'react';
import { Bot, HelpCircle, AlertCircle, Sparkles } from 'lucide-react';
import { MockInterviewQuestion as QuestionType } from '../../types';

interface MockInterviewQuestionProps {
  question: QuestionType;
  isFollowUp?: boolean;
}

export const MockInterviewQuestion: React.FC<MockInterviewQuestionProps> = ({ question, isFollowUp }) => {
  return (
    <div className={`rounded-2xl p-6 sm:p-7 border transition-all ${
      isFollowUp
        ? 'bg-amber-950/20 border-amber-500/30 shadow-lg shadow-amber-500/5'
        : 'bg-slate-900 border-slate-800 shadow-xl'
    }`}>
      <div className="flex items-center justify-between gap-3 mb-4">
        <div className="flex items-center gap-2.5">
          <div className={`p-2 rounded-xl border ${
            isFollowUp
              ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
              : 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30'
          }`}>
            <Bot className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs uppercase tracking-wider font-semibold text-slate-400">
                {isFollowUp ? 'Follow-Up Inquiry' : `AI Interviewer — Round ${question.roundNumber}`}
              </span>
              <span className="text-[10px] px-1.5 py-0.5 rounded font-mono bg-cyan-500/20 text-cyan-300 border border-cyan-500/30">
                Gemini 3.8 Flash
              </span>
            </div>
            <p className="text-xs text-slate-500">
              {isFollowUp ? 'Drilling into specific trade-offs from your previous answer' : 'Listen carefully and formulate a structured, professional response'}
            </p>
          </div>
        </div>

        {isFollowUp && (
          <span className="px-2.5 py-1 rounded-lg text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30 flex items-center gap-1.5">
            <AlertCircle className="w-3.5 h-3.5" />
            <span>Follow-Up Question</span>
          </span>
        )}
      </div>

      <div className="bg-slate-950/80 border border-slate-800/80 rounded-xl p-5 sm:p-6">
        <p className="text-white text-base sm:text-lg font-medium leading-relaxed">
          {isFollowUp && question.followUpQuestion ? question.followUpQuestion : question.question}
        </p>
      </div>
    </div>
  );
};
