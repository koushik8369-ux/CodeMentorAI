import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Sparkles,
  Terminal,
  Code2,
  CheckCircle2,
  Cpu,
  Clock,
  ArrowRight,
  Layers,
  Sliders,
  Check,
  Loader2,
  FileCode,
} from 'lucide-react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api } from '../services/api';
import { QuestionGenerationResponse } from '../types';

export const InterviewSetupPage: React.FC = () => {
  const navigate = useNavigate();

  // Form State
  const [role, setRole] = useState('Software Engineer');
  const [language, setLanguage] = useState<'Java' | 'Python' | 'TypeScript' | 'Go' | 'C++'>('Java');
  const [difficulty, setDifficulty] = useState<'Easy' | 'Medium' | 'Hard'>('Medium');
  const [interviewType, setInterviewType] = useState('Coding Interview');
  const [selectedTopics, setSelectedTopics] = useState<string[]>(['Arrays', 'HashMap']);
  const [questionCount, setQuestionCount] = useState<number>(5);

  // Execution / Preparation State
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [generatedQuestion, setGeneratedQuestion] = useState<QuestionGenerationResponse | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const availableRoles = [
    'Software Engineer',
    'Backend Engineer',
    'Frontend Engineer',
    'Full Stack Engineer',
    'Machine Learning Engineer',
  ];

  const availableLanguages: ('Java' | 'Python' | 'TypeScript' | 'Go' | 'C++')[] = [
    'Java',
    'Python',
    'TypeScript',
    'Go',
    'C++',
  ];

  const availableTypes = [
    'Coding Interview',
    'Data Structures & Algorithms',
    'Framework Deep Dive (Spring / React)',
    'System Design & Concurrency',
  ];

  const allTopics = [
    'Arrays',
    'Strings',
    'HashMap',
    'Trees',
    'Dynamic Programming',
    'Binary Search',
    'Spring Boot',
    'SQL & Transactions',
    'Graphs',
    'Concurrency',
  ];

  const toggleTopic = (topic: string) => {
    if (selectedTopics.includes(topic)) {
      if (selectedTopics.length > 1) {
        setSelectedTopics(selectedTopics.filter((t) => t !== topic));
      }
    } else {
      setSelectedTopics([...selectedTopics, topic]);
    }
  };

  const handleStartInterview = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg(null);

    try {
      // 1. Post to Interview Session API
      await api.createInterview({
        role: role as any,
        language,
        difficulty,
        type: interviewType as any,
        topics: selectedTopics,
        questionCount,
      });

      // 2. Generate initial AI Question for this candidate
      const question = await api.generateQuestion({
        role,
        language,
        difficulty,
        topic: selectedTopics[0] || 'Arrays',
      });

      setGeneratedQuestion(question);
    } catch (err: any) {
      console.error('Error starting interview', err);
      setErrorMsg('Failed to initialize interview session. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Header */}
        <div className="border-b border-slate-850 pb-6">
          <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
            <Sliders className="h-4 w-4" />
            <span>SESSION CALIBRATION</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Start New Interview
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Configure your technical scope. The AI interviewer will calibrate coding challenges and evaluation rubrics accordingly.
          </p>
        </div>

        {/* Preparation / Success Screen when Question is Generated */}
        {generatedQuestion ? (
          <div className="rounded-xl border border-indigo-500/50 bg-slate-900/90 p-6 sm:p-8 space-y-6 shadow-xl shadow-indigo-500/10">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                  <CheckCircle2 className="h-6 w-6" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">Interview Session Initialized!</h2>
                  <p className="text-xs text-slate-400 font-mono">
                    Target Role: {role} · {language} · {difficulty}
                  </p>
                </div>
              </div>

              <span className="text-xs font-mono text-emerald-400">
                Question 1 of {questionCount}
              </span>
            </div>

            {/* Generated Question Preview */}
            <div className="rounded-lg bg-slate-950 p-5 border border-slate-800 space-y-3">
              <div className="flex items-center justify-between text-xs font-mono">
                <span className="text-indigo-400 font-semibold">{generatedQuestion.title}</span>
                <span className="text-slate-400">{generatedQuestion.topic}</span>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">
                {generatedQuestion.description}
              </p>

              {generatedQuestion.examples && generatedQuestion.examples.length > 0 && (
                <div className="mt-3 p-3 rounded bg-slate-900 font-mono text-xs text-slate-300">
                  <span className="text-[11px] text-slate-400 block mb-1">Example:</span>
                  <div>Input: {generatedQuestion.examples[0].input}</div>
                  <div className="text-emerald-400">Output: {generatedQuestion.examples[0].output}</div>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
              <button
                onClick={() => setGeneratedQuestion(null)}
                className="text-xs font-mono text-slate-400 hover:text-white"
              >
                ← Reconfigure Parameters
              </button>

              <button
                onClick={() => navigate('/problem/two-sum')}
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-6 py-3 text-xs font-semibold text-white hover:bg-indigo-500 shadow-md shadow-indigo-500/20"
              >
                <span>Enter Coding Workspace</span>
                <ArrowRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        ) : (
          /* Calibration Form */
          <form onSubmit={handleStartInterview} className="space-y-6">
            {errorMsg && (
              <div className="p-4 rounded-lg bg-rose-500/10 border border-rose-500/30 text-xs text-rose-300">
                {errorMsg}
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Target Role */}
              <div className="space-y-2">
                <label className="block text-xs font-semibold text-slate-200 uppercase tracking-wider font-mono">
                  Target Role
                </label>
                <div className="relative">
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value)}
                    className="w-full appearance-none rounded-lg border border-slate-800 bg-slate-900/90 px-4 py-2.5 text-xs text-slate-200 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  >
                    {availableRoles.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Primary Language */}
              <div className="space-y-2">
                <label className="block text-xs font-semibold text-slate-200 uppercase tracking-wider font-mono">
                  Primary Language
                </label>
                <div className="relative">
                  <select
                    value={language}
                    onChange={(e) => setLanguage(e.target.value as any)}
                    className="w-full appearance-none rounded-lg border border-slate-800 bg-slate-900/90 px-4 py-2.5 text-xs text-slate-200 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  >
                    {availableLanguages.map((lang) => (
                      <option key={lang} value={lang}>
                        {lang} (Official compiler)
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Difficulty */}
              <div className="space-y-2">
                <label className="block text-xs font-semibold text-slate-200 uppercase tracking-wider font-mono">
                  Difficulty Level
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {(['Easy', 'Medium', 'Hard'] as const).map((diff) => (
                    <button
                      type="button"
                      key={diff}
                      onClick={() => setDifficulty(diff)}
                      className={`py-2 px-3 rounded-lg text-xs font-mono font-medium transition-all ${
                        difficulty === diff
                          ? 'bg-indigo-600/20 border border-indigo-500 text-indigo-300 font-semibold'
                          : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white'
                      }`}
                    >
                      {diff}
                    </button>
                  ))}
                </div>
              </div>

              {/* Interview Type */}
              <div className="space-y-2">
                <label className="block text-xs font-semibold text-slate-200 uppercase tracking-wider font-mono">
                  Interview Type
                </label>
                <select
                  value={interviewType}
                  onChange={(e) => setInterviewType(e.target.value)}
                  className="w-full appearance-none rounded-lg border border-slate-800 bg-slate-900/90 px-4 py-2.5 text-xs text-slate-200 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  {availableTypes.map((t) => (
                    <option key={t} value={t}>
                      {t}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Topics Multi-Select */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <label className="block text-xs font-semibold text-slate-200 uppercase tracking-wider font-mono">
                  Focus Topics
                </label>
                <span className="text-[11px] text-slate-400 font-mono">
                  Selected: {selectedTopics.length}
                </span>
              </div>
              <div className="flex flex-wrap gap-2 pt-1">
                {allTopics.map((topic) => {
                  const active = selectedTopics.includes(topic);
                  return (
                    <button
                      type="button"
                      key={topic}
                      onClick={() => toggleTopic(topic)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-mono transition-all flex items-center gap-1.5 ${
                        active
                          ? 'bg-indigo-600 text-white shadow-xs'
                          : 'bg-slate-900/80 border border-slate-800 text-slate-400 hover:text-white hover:border-slate-700'
                      }`}
                    >
                      {active && <Check className="h-3 w-3" />}
                      <span>{topic}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Number of Questions */}
            <div className="space-y-2">
              <label className="block text-xs font-semibold text-slate-200 uppercase tracking-wider font-mono">
                Number of Questions
              </label>
              <div className="flex items-center gap-3">
                {[3, 5, 10].map((num) => (
                  <button
                    type="button"
                    key={num}
                    onClick={() => setQuestionCount(num)}
                    className={`px-4 py-2 rounded-lg text-xs font-mono transition-all ${
                      questionCount === num
                        ? 'bg-slate-800 border border-slate-700 text-white font-bold'
                        : 'bg-slate-900/60 border border-slate-800 text-slate-400 hover:text-white'
                    }`}
                  >
                    {num} Questions
                  </button>
                ))}
              </div>
            </div>

            {/* Submit Button */}
            <div className="pt-4 border-t border-slate-850 flex items-center justify-between">
              <div className="text-xs font-mono text-slate-400">
                Estimated duration: ~{questionCount * 15} minutes
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-6 py-3 text-xs font-semibold text-white hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500 transition-all shadow-md shadow-indigo-500/20 disabled:opacity-50"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    <span>Calibrating AI Interview...</span>
                  </>
                ) : (
                  <>
                    <Sparkles className="h-4 w-4" />
                    <span>Start Interview</span>
                  </>
                )}
              </button>
            </div>
          </form>
        )}
      </div>
    </DashboardLayout>
  );
};
