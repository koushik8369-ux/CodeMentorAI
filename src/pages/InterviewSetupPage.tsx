import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api } from '../services/api';
import { Interview } from '../types';
import { InterviewSetup } from '../components/interview/InterviewSetup';
import { CodingWorkspace } from '../components/interview/CodingWorkspace';
import { InterviewResult } from '../components/interview/InterviewResult';
import { InterviewHistory } from '../components/interview/InterviewHistory';
import { Sparkles, History, Play } from 'lucide-react';

export const InterviewSetupPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'setup' | 'workspace' | 'result' | 'history'>('setup');
  const [currentInterview, setCurrentInterview] = useState<Interview | null>(null);
  const [historyList, setHistoryList] = useState<Interview[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isHistoryLoading, setIsHistoryLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Load history when entering history tab
  useEffect(() => {
    if (activeTab === 'history') {
      loadHistory();
    }
  }, [activeTab]);

  const loadHistory = async () => {
    setIsHistoryLoading(true);
    setErrorMessage(null);
    try {
      const data = await api.getInterviewHistory();
      setHistoryList(data);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load past interviews');
    } finally {
      setIsHistoryLoading(false);
    }
  };

  const handleStartInterview = async (topic: string, difficulty: string, language: string) => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const interview = await api.startInterview({ topic, difficulty, language });
      setCurrentInterview(interview);
      setActiveTab('workspace');
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to start interview. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmitSolution = async (code: string) => {
    if (!currentInterview) return;
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      const evaluated = await api.submitInterview(currentInterview.id, { code });
      setCurrentInterview(evaluated);
      setActiveTab('result');
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to submit and evaluate code.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSelectHistoryItem = (item: Interview) => {
    setCurrentInterview(item);
    if (item.status === 'COMPLETED') {
      setActiveTab('result');
    } else {
      setActiveTab('workspace');
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Navigation Tabs (only shown when not inside active workspace) */}
        {activeTab !== 'workspace' && (
          <div className="flex items-center justify-between border-b border-slate-800 pb-4">
            <div className="flex items-center gap-2">
              <button
                onClick={() => {
                  setActiveTab('setup');
                  setErrorMessage(null);
                }}
                className={`px-4 py-2 rounded-xl text-sm font-medium transition flex items-center gap-2 ${
                  activeTab === 'setup'
                    ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                }`}
              >
                <Play className="w-4 h-4" />
                <span>Configure Interview</span>
              </button>

              <button
                onClick={() => {
                  setActiveTab('history');
                  setErrorMessage(null);
                }}
                className={`px-4 py-2 rounded-xl text-sm font-medium transition flex items-center gap-2 ${
                  activeTab === 'history'
                    ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                }`}
              >
                <History className="w-4 h-4" />
                <span>Interview History</span>
              </button>
            </div>
          </div>
        )}

        {/* Setup Screen */}
        {activeTab === 'setup' && (
          <InterviewSetup
            onStart={handleStartInterview}
            isLoading={isLoading}
            error={errorMessage}
          />
        )}

        {/* Coding Workspace Screen */}
        {activeTab === 'workspace' && currentInterview && (
          <CodingWorkspace
            interview={currentInterview}
            onSubmit={handleSubmitSolution}
            onExit={() => {
              if (window.confirm('Leave active interview workspace? You can resume it anytime from history.')) {
                setActiveTab('setup');
              }
            }}
            isSubmitting={isSubmitting}
            error={errorMessage}
          />
        )}

        {/* Result Screen */}
        {activeTab === 'result' && currentInterview && (
          <InterviewResult
            interview={currentInterview}
            onNewInterview={() => {
              setCurrentInterview(null);
              setActiveTab('setup');
            }}
            onViewHistory={() => {
              setActiveTab('history');
            }}
          />
        )}

        {/* History Screen */}
        {activeTab === 'history' && (
          <InterviewHistory
            interviews={historyList}
            isLoading={isHistoryLoading}
            onSelectInterview={handleSelectHistoryItem}
            onNewInterview={() => {
              setCurrentInterview(null);
              setActiveTab('setup');
            }}
          />
        )}
      </div>
    </DashboardLayout>
  );
};
