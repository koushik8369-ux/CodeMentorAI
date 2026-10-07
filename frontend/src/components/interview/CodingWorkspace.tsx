import React, { useState, useEffect } from 'react';
import {
  Clock,
  Send,
  ArrowLeft,
  RotateCcw,
  Sparkles,
  Terminal,
  FileCode,
  CheckCircle2,
  AlertCircle,
  Copy,
  Check,
} from 'lucide-react';
import { Interview } from '../../types';

interface CodingWorkspaceProps {
  interview: Interview;
  onSubmit: (code: string) => void;
  onExit: () => void;
  isSubmitting: boolean;
  error?: string | null;
}

export const CodingWorkspace: React.FC<CodingWorkspaceProps> = ({
  interview,
  onSubmit,
  onExit,
  isSubmitting,
  error,
}) => {
  const [code, setCode] = useState<string>(interview.starterCode || '');
  const [elapsedSeconds, setElapsedSeconds] = useState<number>(0);
  const [copied, setCopied] = useState<boolean>(false);

  // Timer started when workspace mounts
  useEffect(() => {
    const timer = setInterval(() => {
      setElapsedSeconds((prev) => prev + 1);
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (totalSec: number) => {
    const mins = Math.floor(totalSec / 60);
    const secs = totalSec % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCopyStarter = () => {
    if (interview.starterCode) {
      navigator.clipboard.writeText(interview.starterCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleResetCode = () => {
    if (window.confirm('Reset code back to original starter template?')) {
      setCode(interview.starterCode || '');
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim() || isSubmitting) return;
    onSubmit(code);
  };

  const difficultyColors: Record<string, string> = {
    EASY: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    MEDIUM: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    HARD: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
  };

  const difficultyBadge =
    difficultyColors[interview.difficulty.toUpperCase()] ||
    'bg-slate-800 text-slate-300 border-slate-700';

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] min-h-[700px] space-y-4">
      {/* Top Bar Navigation & Timer */}
      <div className="flex flex-wrap items-center justify-between gap-4 bg-slate-900 border border-slate-800 px-5 py-3 rounded-xl">
        <div className="flex items-center gap-3">
          <button
            onClick={onExit}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
            title="Exit interview workspace"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-white font-semibold text-base sm:text-lg">
                {interview.problemTitle}
              </h2>
              <span className={`px-2 py-0.5 rounded text-xs font-semibold border ${difficultyBadge}`}>
                {interview.difficulty}
              </span>
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-400 mt-0.5">
              <span>Topic: <strong className="text-slate-300">{interview.topic}</strong></span>
              <span>•</span>
              <span>Language: <strong className="text-slate-300 font-mono">{interview.language}</strong></span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {/* Visible Interview Timer */}
          <div className="flex items-center gap-2 px-3 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-cyan-400 font-mono text-sm shadow-inner">
            <Clock className="w-4 h-4" />
            <span>{formatTime(elapsedSeconds)}</span>
          </div>

          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitting || !code.trim()}
            className="px-5 py-2 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold text-sm transition shadow-md shadow-cyan-500/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            {isSubmitting ? (
              <>
                <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                <span>AI Evaluating...</span>
              </>
            ) : (
              <>
                <Send className="w-4 h-4" />
                <span>Submit Solution</span>
              </>
            )}
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-rose-950/30 border border-rose-500/30 rounded-xl p-3 text-rose-300 text-sm flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Main Split Workspace */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 flex-1 min-h-0">
        {/* Left Column: Problem Details & Constraints */}
        <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-xl p-6 overflow-y-auto space-y-6">
          {/* Problem Statement */}
          <div>
            <h3 className="text-xs uppercase tracking-wider text-slate-400 font-semibold mb-2">
              Problem Description
            </h3>
            <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-line">
              {interview.problemDescription}
            </p>
          </div>

          {/* Input & Output Format */}
          {(interview.inputFormat || interview.outputFormat) && (
            <div className="space-y-3 bg-slate-950/60 border border-slate-800/80 rounded-xl p-4">
              {interview.inputFormat && (
                <div>
                  <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                    Input Format
                  </h4>
                  <p className="text-xs text-slate-300 font-mono leading-relaxed">
                    {interview.inputFormat}
                  </p>
                </div>
              )}
              {interview.outputFormat && (
                <div className="pt-2 border-t border-slate-800/60">
                  <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                    Output Format
                  </h4>
                  <p className="text-xs text-slate-300 font-mono leading-relaxed">
                    {interview.outputFormat}
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Examples */}
          {interview.examples && interview.examples.length > 0 && (
            <div className="space-y-3">
              <h3 className="text-xs uppercase tracking-wider text-slate-400 font-semibold">
                Examples
              </h3>
              <div className="space-y-3">
                {interview.examples.map((ex, idx) => (
                  <div
                    key={idx}
                    className="bg-slate-950/80 border border-slate-800 rounded-xl p-3 text-xs space-y-1.5 font-mono"
                  >
                    <div className="text-slate-400">
                      <strong className="text-slate-200">Input:</strong> {ex.input}
                    </div>
                    <div className="text-slate-400">
                      <strong className="text-slate-200">Output:</strong> {ex.output}
                    </div>
                    {ex.explanation && (
                      <div className="text-slate-400 font-sans text-xs pt-1 border-t border-slate-800/60">
                        <strong className="text-slate-300">Explanation:</strong> {ex.explanation}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Constraints */}
          {interview.constraints && interview.constraints.length > 0 && (
            <div>
              <h3 className="text-xs uppercase tracking-wider text-slate-400 font-semibold mb-2">
                Constraints
              </h3>
              <ul className="list-disc list-inside space-y-1 text-xs text-slate-300 font-mono">
                {interview.constraints.map((c, idx) => (
                  <li key={idx}>{c}</li>
                ))}
              </ul>
            </div>
          )}
        </div>

        {/* Right Column: Code Editor */}
        <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-xl flex flex-col overflow-hidden">
          {/* Editor Header Toolbar */}
          <div className="bg-slate-950 border-b border-slate-800 px-4 py-2.5 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <FileCode className="w-4 h-4 text-cyan-400" />
              <span className="text-xs font-mono font-medium text-slate-300">
                Solution.{interview.language === 'JAVA' ? 'java' : interview.language === 'PYTHON' ? 'py' : interview.language === 'CPP' ? 'cpp' : 'js'}
              </span>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={handleCopyStarter}
                className="p-1.5 text-xs text-slate-400 hover:text-white rounded hover:bg-slate-800 transition flex items-center gap-1.5"
                title="Copy starter code"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                <span className="text-xs">{copied ? 'Copied' : 'Copy'}</span>
              </button>
              <button
                type="button"
                onClick={handleResetCode}
                className="p-1.5 text-xs text-slate-400 hover:text-white rounded hover:bg-slate-800 transition flex items-center gap-1.5"
                title="Reset to starter template"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                <span className="text-xs">Reset</span>
              </button>
            </div>
          </div>

          {/* Code Textarea Editor */}
          <div className="flex-1 relative bg-slate-950 flex flex-col">
            <textarea
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="// Write your code solution here..."
              spellCheck={false}
              className="w-full flex-1 p-5 font-mono text-sm bg-slate-950 text-cyan-100 placeholder-slate-600 focus:outline-none resize-none leading-relaxed border-none selection:bg-cyan-500/30"
            />
          </div>

          {/* Editor Status Footer */}
          <div className="bg-slate-950 border-t border-slate-800/80 px-4 py-2 flex items-center justify-between text-xs text-slate-500 font-mono">
            <div>Lines: {code.split('\n').length} | Characters: {code.length}</div>
            <div className="flex items-center gap-1.5 text-emerald-400">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
              <span>Workspace Ready</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
