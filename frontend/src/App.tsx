import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LandingPage } from './pages/LandingPage';
import { DashboardPage } from './pages/DashboardPage';
import { InterviewSetupPage } from './pages/InterviewSetupPage';
import { ProblemPage } from './pages/ProblemPage';
import { ProblemsListPage } from './pages/ProblemsListPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { LearningPlanPage } from './pages/LearningPlanPage';
import { ProfilePage } from './pages/ProfilePage';
import { SettingsPage } from './pages/SettingsPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Landing Page */}
        <Route path="/" element={<LandingPage />} />

        {/* Candidate Dashboard */}
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* Interview Calibration */}
        <Route path="/interview-setup" element={<InterviewSetupPage />} />
        <Route path="/interviews" element={<InterviewSetupPage />} />

        {/* Interactive Problem Workspace */}
        <Route path="/problem/:id" element={<ProblemPage />} />
        <Route path="/practice" element={<ProblemPage />} />

        {/* Problems Repository */}
        <Route path="/problems" element={<ProblemsListPage />} />

        {/* Analytics & Diagnostics */}
        <Route path="/analytics" element={<AnalyticsPage />} />

        {/* Learning Plan */}
        <Route path="/learning-plan" element={<LearningPlanPage />} />

        {/* User Dossier & Settings */}
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/settings" element={<SettingsPage />} />

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
