import React from 'react';
import { Terminal, Github, Twitter, Linkedin, Heart } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-slate-850 bg-slate-950/90 py-12 text-slate-400">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-12">
          {/* Brand */}
          <div className="space-y-4 md:col-span-1">
            <div className="flex items-center gap-2">
              <div className="flex h-7 w-7 items-center justify-center rounded bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
                <Terminal className="h-4 w-4" />
              </div>
              <span className="font-bold text-white text-base">
                CodeMentor <span className="text-indigo-400 font-mono text-xs">AI</span>
              </span>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              AI-powered coding interview preparation platform engineered for software engineering candidates aiming for top-tier tech roles.
            </p>
            <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
              <span>Spring Boot</span>
              <span aria-hidden="true">·</span>
              <span>PostgreSQL</span>
              <span aria-hidden="true">·</span>
              <span>Gemini AI</span>
            </div>
          </div>

          {/* Product links */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-200 mb-3 font-mono">
              Product
            </h4>
            <ul className="space-y-2 text-xs">
              <li>
                <Link to="/interview-setup" className="hover:text-indigo-400 transition-colors">
                  Mock Interview Setup
                </Link>
              </li>
              <li>
                <Link to="/problems" className="hover:text-indigo-400 transition-colors">
                  Problem Bank (500+)
                </Link>
              </li>
              <li>
                <Link to="/problem/two-sum" className="hover:text-indigo-400 transition-colors">
                  Interactive Code Workspace
                </Link>
              </li>
              <li>
                <Link to="/analytics" className="hover:text-indigo-400 transition-colors">
                  Performance Analytics
                </Link>
              </li>
            </ul>
          </div>

          {/* Topics */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-200 mb-3 font-mono">
              Popular Tracks
            </h4>
            <ul className="space-y-2 text-xs">
              <li>
                <Link to="/interview-setup" className="hover:text-indigo-400 transition-colors">
                  Java & Spring Boot Engineering
                </Link>
              </li>
              <li>
                <Link to="/interview-setup" className="hover:text-indigo-400 transition-colors">
                  Data Structures & Algorithms
                </Link>
              </li>
              <li>
                <Link to="/interview-setup" className="hover:text-indigo-400 transition-colors">
                  System Design & Concurrency
                </Link>
              </li>
              <li>
                <Link to="/interview-setup" className="hover:text-indigo-400 transition-colors">
                  Full Stack TypeScript & React
                </Link>
              </li>
            </ul>
          </div>

          {/* Status & Architecture */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-200 mb-3 font-mono">
              Architecture & API
            </h4>
            <div className="space-y-2 text-xs">
              <p className="text-slate-400">
                Standalone Spring Boot REST backend on port 8080 with PostgreSQL & Gemini API.
              </p>
              <div className="pt-2">
                <a
                  href="http://localhost:8080/api/health"
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-1.5 font-mono text-[11px] text-emerald-400 hover:underline"
                >
                  <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
                  GET http://localhost:8080/api/health
                </a>
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row items-center justify-between border-t border-slate-900 pt-6 text-xs text-slate-400">
          <p>© {new Date().getFullYear()} CodeMentor AI. Built for software engineering excellence.</p>
          <div className="flex items-center gap-4 mt-4 sm:mt-0">
            <span className="hover:text-slate-300 cursor-pointer">Privacy Policy</span>
            <span className="hover:text-slate-300 cursor-pointer">Terms of Service</span>
            <span className="hover:text-slate-300 cursor-pointer">API Docs</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
