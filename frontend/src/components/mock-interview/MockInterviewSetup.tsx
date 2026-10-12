import React, { useState } from 'react';
import {
  BrainCircuit,
  MessageSquare,
  Sparkles,
  Play,
  Layers,
  Flame,
  CheckCircle2,
} from 'lucide-react';
import { MockInterviewType, StartMockInterviewPayload } from '../../types';

interface MockInterviewSetupProps {
  onStart: (payload: StartMockInterviewPayload) => void;
  isLoading: boolean;
}

export const MockInterviewSetup: React.FC<MockInterviewSetupProps> = ({ onStart, isLoading }) => {
  const [interviewType, setInterviewType] = useState<MockInterviewType>('TECHNICAL');
  const [topic, setTopic] = useState<string>('Java');
  const [difficulty, setDifficulty] = useState<'EASY' | 'MEDIUM' | 'HARD'>('MEDIUM');
  const [totalRounds, setTotalRounds] = useState<number>(5);

  const technicalTopics = [
    'Java',
    'Data Structures & Algorithms',
    'OOP',
    'DBMS',
    'Operating Systems',
    'Computer Networks',
    'Web Development',
    'System Design',
  ];

  const behavioralTopics = [
    'Introduction',
    'Teamwork',
    'Leadership',
    'Conflict Resolution',
    'Problem Solving',
    'Career Goals',
    'Project Experience',
  ];

  const mixedTopics = [
    'System Design & Leadership',
    'Java & Project Experience',
    'Data Structures & Problem Solving',
    'Full Stack Architecture & Teamwork',
    'DBMS & Conflict Resolution',
  ];

  const currentTopicOptions =
    interviewType === 'TECHNICAL'
      ? technicalTopics
      : interviewType === 'BEHAVIORAL'
      ? behavioralTopics
      : mixedTopics;

  const handleTypeChange = (type: MockInterviewType) => {
    setInterviewType(type);
    if (type === 'TECHNICAL') setTopic(technicalTopics[0]);
    else if (type === 'BEHAVIORAL') setTopic(behavioralTopics[0]);
    else setTopic(mixedTopics[0]);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onStart({
      interviewType,
      topic,
      difficulty,
      totalRounds,
    });
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-10 max-w-3xl mx-auto shadow-2xl space-y-8">
      {/* Header */}
      <div className="space-y-2 text-center sm:text-left border-b border-slate-800 pb-6">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 text-cyan-300 border border-cyan-500/20 text-xs font-semibold uppercase tracking-wider">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Interactive AI Interviewer</span>
        </div>
        <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
          Configure Your Mock Technical Interview
        </h2>
        <p className="text-slate-400 text-sm">
          Experience a realistic, multi-round technical interview loop with Gemini assessing your responses and asking intelligent follow-up inquiries.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 1. Interview Type */}
        <div className="space-y-3">
          <label className="text-xs uppercase font-bold tracking-wider text-slate-300 flex items-center gap-2">
            <BrainCircuit className="w-4 h-4 text-cyan-400" />
            <span>1. Select Interview Type</span>
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            {[
              { type: 'TECHNICAL' as MockInterviewType, label: 'Technical', desc: 'Architecture, DSA, Java, DBMS & systems' },
              { type: 'BEHAVIORAL' as MockInterviewType, label: 'Behavioral', desc: 'STAR scenarios, leadership, teamwork' },
              { type: 'MIXED' as MockInterviewType, label: 'Mixed', desc: 'Combines technical deep-dives and behavioral rounds' },
            ].map((item) => (
              <button
                type="button"
                key={item.type}
                onClick={() => handleTypeChange(item.type)}
                className={`p-4 rounded-2xl border text-left transition-all ${
                  interviewType === item.type
                    ? 'bg-cyan-500/10 border-cyan-400 text-white shadow-md shadow-cyan-500/10'
                    : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                }`}
              >
                <div className="font-bold text-sm text-white flex items-center justify-between">
                  <span>{item.label}</span>
                  {interviewType === item.type && <CheckCircle2 className="w-4 h-4 text-cyan-400" />}
                </div>
                <p className="text-xs mt-1 text-slate-400">{item.desc}</p>
              </button>
            ))}
          </div>
        </div>

        {/* 2. Topic/Domain */}
        <div className="space-y-3">
          <label className="text-xs uppercase font-bold tracking-wider text-slate-300 flex items-center gap-2">
            <Layers className="w-4 h-4 text-indigo-400" />
            <span>2. Select Topic / Domain</span>
          </label>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            {currentTopicOptions.map((t) => (
              <button
                type="button"
                key={t}
                onClick={() => setTopic(t)}
                className={`px-3 py-2.5 rounded-xl border text-xs font-semibold transition text-center ${
                  topic === t
                    ? 'bg-indigo-600/20 border-indigo-400 text-indigo-200 shadow-sm shadow-indigo-500/20'
                    : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                }`}
              >
                {t}
              </button>
            ))}
          </div>
        </div>

        {/* 3. Difficulty */}
        <div className="space-y-3">
          <label className="text-xs uppercase font-bold tracking-wider text-slate-300 flex items-center gap-2">
            <Flame className="w-4 h-4 text-amber-400" />
            <span>3. Target Difficulty Level</span>
          </label>
          <div className="grid grid-cols-3 gap-3">
            {[
              { level: 'EASY' as const, label: 'Easy', color: 'text-emerald-400', desc: 'Fundamentals & syntax' },
              { level: 'MEDIUM' as const, label: 'Medium', color: 'text-amber-400', desc: 'Trade-offs & internals' },
              { level: 'HARD' as const, label: 'Hard', color: 'text-rose-400', desc: 'Complex distributed systems' },
            ].map((d) => (
              <button
                type="button"
                key={d.level}
                onClick={() => setDifficulty(d.level)}
                className={`p-3.5 rounded-2xl border text-left transition-all ${
                  difficulty === d.level
                    ? 'bg-slate-800 border-slate-600 text-white'
                    : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                }`}
              >
                <div className={`font-bold text-sm ${d.color}`}>{d.label}</div>
                <div className="text-[11px] text-slate-400 mt-0.5">{d.desc}</div>
              </button>
            ))}
          </div>
        </div>

        {/* 4. Number of Rounds */}
        <div className="space-y-3">
          <label className="text-xs uppercase font-bold tracking-wider text-slate-300 flex items-center gap-2">
            <MessageSquare className="w-4 h-4 text-emerald-400" />
            <span>4. Number of Interview Rounds (3 to 10)</span>
          </label>
          <div className="flex gap-3">
            {[3, 5, 7, 10].map((num) => (
              <button
                type="button"
                key={num}
                onClick={() => setTotalRounds(num)}
                className={`flex-1 py-2.5 rounded-xl border text-xs font-mono font-bold transition text-center ${
                  totalRounds === num
                    ? 'bg-emerald-500/20 border-emerald-400 text-emerald-200'
                    : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                }`}
              >
                {num} Rounds
              </button>
            ))}
          </div>
        </div>

        {/* Submit CTA */}
        <div className="pt-4 border-t border-slate-800 flex justify-end">
          <button
            type="submit"
            disabled={isLoading}
            className="w-full sm:w-auto px-8 py-3.5 rounded-2xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-sm transition flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/20 disabled:opacity-50 cursor-pointer"
          >
            {isLoading ? (
              <>
                <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                <span>Generating First Interview Question...</span>
              </>
            ) : (
              <>
                <Play className="w-4 h-4 fill-current" />
                <span>Start Mock Technical Interview</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
