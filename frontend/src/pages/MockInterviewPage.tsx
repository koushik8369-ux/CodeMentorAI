import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api } from '../services/api';
import { MockInterview, StartMockInterviewPayload } from '../types';
import { MockInterviewSetup } from '../components/mock-interview/MockInterviewSetup';
import { MockInterviewWorkspace } from '../components/mock-interview/MockInterviewWorkspace';
import { MockInterviewFinalReport } from '../components/mock-interview/MockInterviewFinalReport';
import { MockInterviewHistory } from '../components/mock-interview/MockInterviewHistory';
import { BrainCircuit, History, PlusCircle, AlertCircle } from 'lucide-react';

export const MockInterviewPage: React.FC = () => {
  const [activeInterview, setActiveInterview] = useState<MockInterview | null>(null);
  const [historyList, setHistoryList] = useState<MockInterview[]>([]);
  const [viewMode, setViewMode] = useState<'setup' | 'workspace' | 'report' | 'history'>('setup');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showEvaluation, setShowEvaluation] = useState<boolean>(false);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const data = await api.getMockInterviews();
      setHistoryList(data);
    } catch (err) {
      console.warn('Failed to fetch mock interview history', err);
    }
  };

  const handleStartInterview = async (payload: StartMockInterviewPayload) => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const interview = await api.startMockInterview(payload);
      setActiveInterview(interview);
      setShowEvaluation(false);
      setViewMode('workspace');
      loadHistory();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to start mock interview.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmitAnswer = async (answer: string) => {
    if (!activeInterview) return;
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      const updated = await api.submitMockAnswer(activeInterview.id, { answer });
      setActiveInterview(updated);
      setShowEvaluation(true);

      if (updated.status === 'COMPLETED') {
        setViewMode('report');
      }
      loadHistory();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to submit interview answer.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleProceedToNextRound = () => {
    if (!activeInterview) return;
    if (activeInterview.status === 'COMPLETED') {
      setViewMode('report');
    } else {
      setShowEvaluation(false);
    }
  };

  const handleSelectHistoryInterview = (interview: MockInterview) => {
    setActiveInterview(interview);
    if (interview.status === 'COMPLETED') {
      setViewMode('report');
    } else {
      setShowEvaluation(false);
      setViewMode('workspace');
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Navigation / Header Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-2xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <BrainCircuit className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                AI Mock Technical Interview
              </h1>
              <p className="text-xs sm:text-sm text-slate-400">
                Interactive interview simulation with Gemini asking dynamic follow-ups and scoring depth.
              </p>
            </div>
          </div>

          {/* Toggle between Interview and History */}
          <div className="flex items-center gap-2 self-start sm:self-auto bg-slate-900 border border-slate-800 rounded-xl p-1">
            <button
              onClick={() => {
                setViewMode('setup');
                setActiveInterview(null);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition flex items-center gap-1.5 ${
                viewMode === 'setup'
                  ? 'bg-cyan-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>New Mock</span>
            </button>

            <button
              onClick={() => setViewMode('history')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition flex items-center gap-1.5 ${
                viewMode === 'history'
                  ? 'bg-cyan-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <History className="w-3.5 h-3.5" />
              <span>History ({historyList.length})</span>
            </button>
          </div>
        </div>

        {/* Error notification */}
        {errorMessage && (
          <div className="bg-rose-950/30 border border-rose-500/30 rounded-2xl p-4 text-rose-300 text-sm flex items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-400" />
              <span>{errorMessage}</span>
            </div>
            <button
              onClick={() => setErrorMessage(null)}
              className="text-xs font-semibold underline hover:text-white"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* View Routing */}
        {viewMode === 'setup' && (
          <MockInterviewSetup
            onStart={handleStartInterview}
            isLoading={isLoading}
          />
        )}

        {viewMode === 'workspace' && activeInterview && (
          <MockInterviewWorkspace
            interview={activeInterview}
            onSubmitAnswer={handleSubmitAnswer}
            isSubmitting={isSubmitting}
            onProceedToNextRound={handleProceedToNextRound}
            showEvaluation={showEvaluation}
          />
        )}

        {viewMode === 'report' && activeInterview && (
          <MockInterviewFinalReport
            interview={activeInterview}
            onRestart={() => {
              setActiveInterview(null);
              setViewMode('setup');
            }}
          />
        )}

        {viewMode === 'history' && (
          <MockInterviewHistory
            interviews={historyList}
            onSelectInterview={handleSelectHistoryInterview}
            onNewInterview={() => {
              setActiveInterview(null);
              setViewMode('setup');
            }}
          />
        )}
      </div>
    </DashboardLayout>
  );
};
