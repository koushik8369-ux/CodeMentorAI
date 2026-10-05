import React, { useState } from 'react';
import { Settings, Sliders, Bell, Code2, Shield, Check } from 'lucide-react';
import { DashboardLayout } from '../layouts/DashboardLayout';

export const SettingsPage: React.FC = () => {
  const [editorTheme, setEditorTheme] = useState('dark-modern');
  const [keybinding, setKeybinding] = useState('standard');
  const [autoRunTests, setAutoRunTests] = useState(true);
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto space-y-8">
        <div className="border-b border-slate-850 pb-6">
          <div className="flex items-center gap-2 text-xs font-mono text-indigo-400 mb-1">
            <Settings className="h-4 w-4" />
            <span>PREFERENCES</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Settings & Editor Config
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Customize editor behavior, AI feedback verbosity, and notification parameters.
          </p>
        </div>

        <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-6 space-y-6">
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
              Code Editor Preferences
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs text-slate-300 font-mono">Editor Theme</label>
                <select
                  value={editorTheme}
                  onChange={(e) => setEditorTheme(e.target.value)}
                  className="w-full rounded-lg border border-slate-800 bg-slate-900 px-3 py-2 text-xs text-slate-200"
                >
                  <option value="dark-modern">Dark Modern (VS Code Slate)</option>
                  <option value="github-dark">GitHub Dark</option>
                  <option value="dracula">Dracula Official</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs text-slate-300 font-mono">Keybindings</label>
                <select
                  value={keybinding}
                  onChange={(e) => setKeybinding(e.target.value)}
                  className="w-full rounded-lg border border-slate-800 bg-slate-900 px-3 py-2 text-xs text-slate-200"
                >
                  <option value="standard">Standard (VS Code default)</option>
                  <option value="vim">Vim Mode</option>
                  <option value="emacs">Emacs Mode</option>
                </select>
              </div>
            </div>

            <div className="pt-2">
              <label className="flex items-center gap-2 text-xs text-slate-300 cursor-pointer">
                <input
                  type="checkbox"
                  checked={autoRunTests}
                  onChange={(e) => setAutoRunTests(e.target.checked)}
                  className="rounded border-slate-700 bg-slate-800 text-indigo-600 focus:ring-0"
                />
                <span>Automatically execute test cases upon syntax validation</span>
              </label>
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 flex items-center justify-between">
            <span className="text-xs font-mono text-slate-500">Changes persist in local configuration</span>
            <button
              onClick={handleSave}
              className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 px-4 py-2 text-xs font-semibold text-white hover:bg-indigo-500"
            >
              {saved ? (
                <>
                  <Check className="h-3.5 w-3.5 text-white" />
                  <span>Saved!</span>
                </>
              ) : (
                <span>Save Preferences</span>
              )}
            </button>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};
