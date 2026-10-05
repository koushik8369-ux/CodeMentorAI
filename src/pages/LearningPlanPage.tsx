import React from 'react';
import { GraduationCap, CheckCircle2, Circle, ArrowRight, BookOpen, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';
import { DashboardLayout } from '../layouts/DashboardLayout';

export const LearningPlanPage: React.FC = () => {
  const roadmapModules = [
    {
      title: 'Week 1: Foundations & Hash-Based Data Structures',
      status: 'completed',
      topics: ['Two Sum & Hash Maps', 'Group Anagrams', 'Prefix Sum Arrays'],
      progress: '100%',
    },
    {
      title: 'Week 2: Two Pointers & Sliding Window',
      status: 'in-progress',
      topics: ['Container With Most Water', 'Longest Substring Without Repeating Characters', '3Sum'],
      progress: '65%',
    },
    {
      title: 'Week 3: Binary Search & Divide and Conquer',
      status: 'upcoming',
      topics: ['Rotated Sorted Array Search', 'Median of Two Sorted Arrays', 'Peak Element'],
      progress: '0%',
    },
    {
      title: 'Week 4: Enterprise Spring Boot & System Concurrency',
      status: 'upcoming',
      topics: ['Reactive Endpoints', 'Lock Contention & Reentrancy', 'Database Indexing Strategies'],
      progress: '0%',
    },
  ];

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto space-y-8">
        <div className="border-b border-slate-850 pb-6">
          <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
            <GraduationCap className="h-4 w-4" />
            <span>PERSONALIZED CURRICULUM</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Learning Plan
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Tailored study schedule calibrated to transition your skillset to Staff/Senior L5 SWE readiness.
          </p>
        </div>

        <div className="space-y-4">
          {roadmapModules.map((mod, idx) => (
            <div
              key={mod.title}
              className="rounded-xl border border-slate-850 bg-slate-900/60 p-6 space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  {mod.status === 'completed' ? (
                    <CheckCircle2 className="h-5 w-5 text-emerald-400" />
                  ) : mod.status === 'in-progress' ? (
                    <div className="h-5 w-5 rounded-full border-2 border-indigo-400 flex items-center justify-center">
                      <div className="h-2 w-2 rounded-full bg-indigo-400 animate-pulse" />
                    </div>
                  ) : (
                    <Circle className="h-5 w-5 text-slate-600" />
                  )}
                  <h3 className="text-sm font-bold text-white">{mod.title}</h3>
                </div>
                <span className="text-xs font-mono text-slate-400">{mod.progress}</span>
              </div>

              <div className="pl-8">
                <div className="flex flex-wrap gap-2 pt-1">
                  {mod.topics.map((t) => (
                    <span
                      key={t}
                      className="px-2.5 py-1 rounded bg-slate-800 text-[11px] font-mono text-slate-300"
                    >
                      {t}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </DashboardLayout>
  );
};
