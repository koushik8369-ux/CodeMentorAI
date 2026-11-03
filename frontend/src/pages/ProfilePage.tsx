import React from 'react';
import { User, Mail, Award, Target, Briefcase, Calendar, ShieldCheck } from 'lucide-react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { useAuth } from '../context/AuthContext';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();

  const displayName = user?.name || 'Candidate Demo';
  const displayEmail = user?.email || 'candidate@codementor.ai';
  const initials = displayName
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto space-y-8">
        <div className="border-b border-slate-850 pb-6">
          <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
            <User className="h-4 w-4" />
            <span>CANDIDATE DOSSIER</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Candidate Profile
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Manage your interview goals, preferred languages, and profile preferences.
          </p>
        </div>

        <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-6 space-y-6">
          <div className="flex items-center gap-4">
            <div className="h-16 w-16 rounded-md bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-300 text-xl font-bold">
              {initials}
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">{displayName}</h2>
              <p className="text-xs text-slate-400 font-mono">{displayEmail}</p>
              <div className="flex items-center gap-2 text-xs font-mono text-emerald-400 mt-1">
                <ShieldCheck className="h-3.5 w-3.5" />
                <span>Local Candidate Profile · Zero Database Mode</span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-slate-800">
            <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-1">
              <span className="text-xs text-slate-400 font-mono">Target Career Level</span>
              <p className="text-sm font-semibold text-white">Senior Software Engineer (L5 / IC5)</p>
            </div>
            <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-1">
              <span className="text-xs text-slate-400 font-mono">Primary Interview Language</span>
              <p className="text-sm font-semibold text-white">Java 21 (Spring Boot ecosystem)</p>
            </div>
            <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-1">
              <span className="text-xs text-slate-400 font-mono">Target Companies</span>
              <p className="text-sm font-semibold text-white">Google, Meta, Stripe, Datadog</p>
            </div>
            <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-1">
              <span className="text-xs text-slate-400 font-mono">Next Scheduled Onsite</span>
              <p className="text-sm font-semibold text-indigo-400">In 3 weeks</p>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};
