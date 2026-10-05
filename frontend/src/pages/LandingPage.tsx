import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Terminal,
  Code2,
  Sparkles,
  Cpu,
  BarChart3,
  BookOpen,
  ArrowRight,
  CheckCircle2,
  Play,
  RotateCcw,
  Zap,
  Check,
  ShieldCheck,
  Layers,
  ChevronRight,
} from 'lucide-react';
import { Navbar } from '../components/Navbar';
import { Footer } from '../components/Footer';

export const LandingPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'editor' | 'review' | 'metrics'>('editor');
  const [selectedLang, setSelectedLang] = useState<'Java' | 'Python' | 'TypeScript'>('Java');

  const sampleCodes = {
    Java: `class Solution {
    // Optimal Two-Sum implementation in O(n) time
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[] { map.get(complement), i };
            }
            map.put(nums[i], i);
        }
        return new int[]{};
    }
}`,
    Python: `class Solution:
    def twoSum(self, nums: list[int], target: int) -> list[int]:
        # Fast one-pass hash map solution
        seen = {}
        for idx, val in enumerate(nums):
            diff = target - val
            if diff in seen:
                return [seen[diff], idx]
            seen[val] = idx
        return []`,
    TypeScript: `function twoSum(nums: number[], target: number): number[] {
    const map = new Map<number, number>();
    for (let i = 0; i < nums.length; i++) {
        const complement = target - nums[i];
        if (map.has(complement)) {
            return [map.get(complement)!, i];
        }
        map.set(nums[i], i);
    }
    return [];
}`,
  };

  const features = [
    {
      icon: Cpu,
      title: 'AI Coding Questions',
      description: 'Dynamic problem generation dynamically calibrated to your target role, preferred tech stack, and weak technical topics.',
    },
    {
      icon: Code2,
      title: 'Real-Time Code Practice',
      description: 'In-browser realistic code execution environment supporting Java 21, Python, TypeScript, and Go with automated test runners.',
    },
    {
      icon: Sparkles,
      title: 'AI Code Review',
      description: 'Receive line-by-line feedback on time complexity, algorithmic bottlenecks, edge case omissions, and code cleanliness.',
    },
    {
      icon: Terminal,
      title: 'Technical Interview Simulation',
      description: 'Timed end-to-end interview simulations that replicate real pressure, conversational prompts, and behavioral rubrics.',
    },
    {
      icon: BarChart3,
      title: 'Performance Analytics',
      description: 'Granular telemetry tracking topic mastery, average time-to-solution, runtime efficiency percentiles, and streak metrics.',
    },
    {
      icon: BookOpen,
      title: 'Personalized Learning',
      description: 'Tailored roadmaps that pinpoint your specific gaps—whether dynamic programming, concurrency, or Spring Boot architectures.',
    },
  ];

  const steps = [
    {
      step: '01',
      title: 'Choose Your Interview',
      desc: 'Select your target engineering level (L4–L6), primary programming language, and focus topics.',
    },
    {
      step: '02',
      title: 'Solve Coding Problems',
      desc: 'Write clean code in a production-grade workspace with real-time test execution and edge-case suites.',
    },
    {
      step: '03',
      title: 'Get AI Evaluation',
      desc: 'Receive immediate algorithmic scoring, complexity analysis, and senior-level architectural feedback.',
    },
    {
      step: '04',
      title: 'Improve Your Weak Areas',
      desc: 'Review data-driven analytics and tackle curated problem sets targeting your specific skill gaps.',
    },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-indigo-500/20 selection:text-indigo-300">
      <Navbar />

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 md:pt-20 md:pb-32 border-b border-slate-850">
        {/* Subtle grid background */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b15_1px,transparent_1px),linear-gradient(to_bottom,#1e293b15_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] pointer-events-none" />

        <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 text-center">
          {/* Unboxed domain kicker */}
          <div className="inline-flex items-center gap-2 text-xs font-mono text-indigo-400 mb-6 tracking-wide">
            <span className="h-1.5 w-1.5 rounded-full bg-indigo-400 animate-pulse" />
            <span>AI-POWERED INTERVIEW PLATFORM</span>
            <span aria-hidden="true" className="text-slate-600">·</span>
            <span>SPRING BOOT & GEMINI AI</span>
          </div>

          <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold tracking-tight text-white max-w-4xl mx-auto leading-tight sm:leading-none">
            Master Coding Interviews <br className="hidden sm:inline" />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-sky-300 to-indigo-200">
              with AI
            </span>
          </h1>

          <p className="mt-6 text-base sm:text-lg text-slate-400 max-w-2xl mx-auto leading-relaxed">
            Practice coding problems, simulate technical interviews, and receive intelligent feedback designed around your skills.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/interview-setup"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-6 py-3 text-sm font-semibold text-white hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500 transition-all shadow-md shadow-indigo-500/20"
            >
              <span>Start Practicing</span>
              <ArrowRight className="h-4 w-4" />
            </Link>

            <a
              href="#features"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-lg border border-slate-800 bg-slate-900/80 px-6 py-3 text-sm font-semibold text-slate-300 hover:text-white hover:bg-slate-800/80 transition-all"
            >
              <span>Explore Features</span>
            </a>
          </div>

          {/* Interactive Hero Preview Component */}
          <div className="mt-16 mx-auto max-w-5xl rounded-xl border border-slate-800 bg-slate-900/90 shadow-2xl overflow-hidden text-left">
            {/* Window title bar */}
            <div className="flex items-center justify-between border-b border-slate-800 px-4 py-3 bg-slate-950/60">
              <div className="flex items-center gap-2">
                <span className="h-3 w-3 rounded-full bg-rose-500/80" />
                <span className="h-3 w-3 rounded-full bg-amber-500/80" />
                <span className="h-3 w-3 rounded-full bg-emerald-500/80" />
                <span className="ml-2 text-xs font-mono text-slate-400">
                  codementor_workspace // TwoSum.java
                </span>
              </div>

              {/* View Switcher Tabs */}
              <div className="flex items-center gap-1 bg-slate-900 p-0.5 rounded-lg border border-slate-800 text-xs">
                <button
                  onClick={() => setActiveTab('editor')}
                  className={`px-3 py-1 rounded font-medium transition-colors ${
                    activeTab === 'editor'
                      ? 'bg-slate-800 text-white shadow-xs'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Code Editor
                </button>
                <button
                  onClick={() => setActiveTab('review')}
                  className={`px-3 py-1 rounded font-medium transition-colors ${
                    activeTab === 'review'
                      ? 'bg-slate-800 text-indigo-300 shadow-xs'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  AI Review
                </button>
                <button
                  onClick={() => setActiveTab('metrics')}
                  className={`px-3 py-1 rounded font-medium transition-colors ${
                    activeTab === 'metrics'
                      ? 'bg-slate-800 text-cyan-300 shadow-xs'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Live Metrics
                </button>
              </div>
            </div>

            {/* Editor Workspace Content */}
            {activeTab === 'editor' && (
              <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[380px]">
                {/* Left Problem Specs */}
                <div className="lg:col-span-5 p-5 border-b lg:border-b-0 lg:border-r border-slate-800 bg-slate-950/40 space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono text-emerald-400">Easy Difficulty</span>
                    <span className="text-xs font-mono text-slate-400">Acceptance 49.8%</span>
                  </div>

                  <h3 className="text-base font-bold text-white">1. Two Sum</h3>

                  <p className="text-xs text-slate-300 leading-relaxed">
                    Given an array of integers <code className="text-indigo-300 font-mono">nums</code> and an integer <code className="text-indigo-300 font-mono">target</code>, return indices of the two numbers such that they add up to <code className="text-indigo-300 font-mono">target</code>.
                  </p>

                  <div className="p-3 rounded bg-slate-900/80 border border-slate-800 font-mono text-xs space-y-1">
                    <span className="text-slate-400 block text-[11px]">Example 1:</span>
                    <div className="text-slate-300">Input: nums = [2,7,11,15], target = 9</div>
                    <div className="text-emerald-400">Output: [0,1]</div>
                  </div>

                  <div className="pt-2 flex items-center gap-3">
                    <Link
                      to="/problem/two-sum"
                      className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-400 hover:text-indigo-300 font-mono"
                    >
                      <span>Open Full Interactive Workspace</span>
                      <ChevronRight className="h-3.5 w-3.5" />
                    </Link>
                  </div>
                </div>

                {/* Right Code Editor Pane */}
                <div className="lg:col-span-7 flex flex-col bg-slate-950/70">
                  {/* Language bar */}
                  <div className="flex items-center justify-between px-4 py-2 border-b border-slate-850 bg-slate-900/40 text-xs">
                    <div className="flex items-center gap-2">
                      {(['Java', 'Python', 'TypeScript'] as const).map((lang) => (
                        <button
                          key={lang}
                          onClick={() => setSelectedLang(lang)}
                          className={`px-2.5 py-1 rounded text-[11px] font-mono transition-colors ${
                            selectedLang === lang
                              ? 'bg-indigo-600/30 text-indigo-300 border border-indigo-500/40 font-semibold'
                              : 'text-slate-400 hover:text-slate-200'
                          }`}
                        >
                          {lang}
                        </button>
                      ))}
                    </div>

                    <div className="flex items-center gap-2">
                      <Link
                        to="/problem/two-sum"
                        className="inline-flex items-center gap-1 px-3 py-1 rounded bg-indigo-600 text-white text-xs font-medium hover:bg-indigo-500"
                      >
                        <Play className="h-3 w-3 fill-current" />
                        <span>Run Code</span>
                      </Link>
                    </div>
                  </div>

                  {/* Code body */}
                  <div className="p-4 font-mono text-xs text-slate-300 overflow-x-auto leading-relaxed flex-1">
                    <pre className="text-slate-200">
                      <code>{sampleCodes[selectedLang]}</code>
                    </pre>
                  </div>

                  {/* Output summary footer */}
                  <div className="border-t border-slate-850 px-4 py-2.5 bg-slate-950/80 flex items-center justify-between text-xs font-mono">
                    <div className="flex items-center gap-2 text-emerald-400">
                      <CheckCircle2 className="h-3.5 w-3.5" />
                      <span>Test cases passed: 3 / 3</span>
                    </div>
                    <span className="text-slate-400">Runtime: 2ms · O(n) Time</span>
                  </div>
                </div>
              </div>
            )}

            {/* AI Review Tab */}
            {activeTab === 'review' && (
              <div className="p-6 space-y-4 min-h-[380px] bg-slate-950/50">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center gap-2">
                    <Sparkles className="h-5 w-5 text-indigo-400" />
                    <span className="font-semibold text-white text-sm">Gemini AI Feedback Assessment</span>
                  </div>
                  <span className="text-xs font-mono text-emerald-400">Overall Score: 94/100</span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
                  <div className="p-4 rounded-lg bg-slate-900/60 border border-slate-800 space-y-1">
                    <span className="text-xs font-mono text-slate-400">Time Complexity</span>
                    <p className="text-lg font-bold text-white font-mono">O(n)</p>
                    <p className="text-xs text-emerald-400">Optimal single pass hash lookups</p>
                  </div>
                  <div className="p-4 rounded-lg bg-slate-900/60 border border-slate-800 space-y-1">
                    <span className="text-xs font-mono text-slate-400">Space Complexity</span>
                    <p className="text-lg font-bold text-white font-mono">O(n)</p>
                    <p className="text-xs text-slate-400">Acceptable trade-off for speed</p>
                  </div>
                  <div className="p-4 rounded-lg bg-slate-900/60 border border-slate-800 space-y-1">
                    <span className="text-xs font-mono text-slate-400">Code Style</span>
                    <p className="text-lg font-bold text-white font-mono">Senior (Clean)</p>
                    <p className="text-xs text-indigo-400">Follows idiomatic Java standards</p>
                  </div>
                </div>

                <div className="p-4 rounded-lg bg-slate-900/40 border border-slate-800/80 text-xs text-slate-300 space-y-2">
                  <p className="font-semibold text-slate-200">Interviewer Insights:</p>
                  <p>
                    "Great explanation of edge cases. You correctly identified that duplicate values can be handled without premature collisions by doing lookup before insertion. To reach staff level, discuss memory locality on primitive arrays."
                  </p>
                </div>
              </div>
            )}

            {/* Live Metrics Tab */}
            {activeTab === 'metrics' && (
              <div className="p-6 min-h-[380px] bg-slate-950/50 flex flex-col justify-center space-y-6">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="p-4 rounded-lg bg-slate-900/80 border border-slate-800">
                    <span className="text-xs text-slate-400 font-mono">Interview Readiness</span>
                    <p className="text-2xl font-bold text-white mt-1">78%</p>
                    <span className="text-[11px] text-emerald-400">+5% this week</span>
                  </div>
                  <div className="p-4 rounded-lg bg-slate-900/80 border border-slate-800">
                    <span className="text-xs text-slate-400 font-mono">Problems Solved</span>
                    <p className="text-2xl font-bold text-white mt-1">42</p>
                    <span className="text-[11px] text-slate-400">32 Med · 10 Hard</span>
                  </div>
                  <div className="p-4 rounded-lg bg-slate-900/80 border border-slate-800">
                    <span className="text-xs text-slate-400 font-mono">Interviews Completed</span>
                    <p className="text-2xl font-bold text-white mt-1">8</p>
                    <span className="text-[11px] text-indigo-400">Avg score 81%</span>
                  </div>
                  <div className="p-4 rounded-lg bg-slate-900/80 border border-slate-800">
                    <span className="text-xs text-slate-400 font-mono">Current Streak</span>
                    <p className="text-2xl font-bold text-white mt-1">6 days</p>
                    <span className="text-[11px] text-amber-400">Keep it going!</span>
                  </div>
                </div>

                <div className="text-center pt-2">
                  <Link
                    to="/dashboard"
                    className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-5 py-2.5 text-xs font-semibold text-white hover:bg-indigo-500"
                  >
                    <span>View Complete Candidate Dashboard</span>
                    <ArrowRight className="h-4 w-4" />
                  </Link>
                </div>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-slate-950 border-b border-slate-850">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-xs font-mono uppercase tracking-widest text-indigo-400 mb-2">
              ENGINEERED FOR EXCELLENCE
            </h2>
            <h3 className="text-3xl font-extrabold text-white tracking-tight sm:text-4xl">
              Everything You Need to Ace Technical Rounds
            </h3>
            <p className="mt-4 text-slate-400 text-sm sm:text-base">
              From dynamic algorithmic challenges to rigorous system design critiques, CodeMentor AI replicates high-bar hiring bars.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((feature, idx) => {
              const Icon = feature.icon;
              return (
                <div
                  key={feature.title}
                  className="group relative rounded-xl border border-slate-800 bg-slate-900/50 p-6 hover:border-slate-700 hover:bg-slate-900/90 transition-all flex flex-col justify-between"
                >
                  <div className="space-y-4">
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-600/10 border border-indigo-500/20 text-indigo-400 group-hover:scale-105 group-hover:bg-indigo-600/20 transition-all">
                      <Icon className="h-5 w-5" />
                    </div>
                    <h4 className="text-lg font-semibold text-white">{feature.title}</h4>
                    <p className="text-xs sm:text-sm text-slate-400 leading-relaxed">
                      {feature.description}
                    </p>
                  </div>
                  <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs font-mono text-slate-400">
                    <span>Feature 0{idx + 1}</span>
                    <span className="text-indigo-400 opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1">
                      Learn more →
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" className="py-20 bg-slate-900/30 border-b border-slate-850">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-xs font-mono uppercase tracking-widest text-indigo-400 mb-2">
              STRUCTURED PATHWAY
            </h2>
            <h3 className="text-3xl font-extrabold text-white tracking-tight sm:text-4xl">
              How It Works
            </h3>
            <p className="mt-4 text-slate-400 text-sm sm:text-base">
              A proven 4-step framework designed to eliminate interview anxiety and accelerate mastery.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {steps.map((item) => (
              <div
                key={item.step}
                className="relative rounded-xl border border-slate-800 bg-slate-900/60 p-6 flex flex-col justify-between"
              >
                <div>
                  <span className="text-3xl font-extrabold font-mono text-indigo-500/40 block mb-3">
                    {item.step}
                  </span>
                  <h4 className="text-base font-bold text-white mb-2">{item.title}</h4>
                  <p className="text-xs text-slate-400 leading-relaxed">{item.desc}</p>
                </div>
                <div className="mt-6 pt-3 border-t border-slate-800/60 flex items-center gap-1 text-[11px] font-mono text-indigo-400">
                  <CheckCircle2 className="h-3.5 w-3.5" />
                  <span>Verified Rubric</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Pricing / Plan Preview */}
      <section id="pricing" className="py-20 bg-slate-950 border-b border-slate-850">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-xs font-mono uppercase tracking-widest text-indigo-400 mb-2">
              SIMPLE TRANSPARENT ACCESS
            </h2>
            <h3 className="text-3xl font-extrabold text-white tracking-tight sm:text-4xl">
              Invest in Your Software Engineering Career
            </h3>
            <p className="mt-4 text-slate-400 text-sm">
              Free during initial preview. Scale up when you are ready to prepare for FAANG onsite loops.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 max-w-3xl mx-auto gap-6">
            {/* Free Tier */}
            <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-6 flex flex-col justify-between">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h4 className="text-base font-bold text-white">Starter Candidate</h4>
                  <span className="text-xs font-mono text-slate-400">FREE</span>
                </div>
                <p className="text-xs text-slate-400">Ideal for daily practice on classic problems.</p>
                <div className="text-3xl font-extrabold text-white font-mono">$0</div>
                <ul className="space-y-2.5 text-xs text-slate-300 pt-2">
                  <li className="flex items-center gap-2">
                    <Check className="h-4 w-4 text-emerald-400" />
                    <span>Access to curated problem bank (500+ problems)</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-4 w-4 text-emerald-400" />
                    <span>Java 21, Python & TypeScript code execution</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-4 w-4 text-emerald-400" />
                    <span>Basic algorithmic complexity breakdown</span>
                  </li>
                </ul>
              </div>
              <Link
                to="/interview-setup"
                className="mt-8 block text-center rounded-lg border border-slate-700 py-2.5 text-xs font-medium text-slate-200 hover:bg-slate-800"
              >
                Get Started Free
              </Link>
            </div>

            {/* Pro Tier */}
            <div className="rounded-xl border border-indigo-500/50 bg-slate-900/90 p-6 flex flex-col justify-between relative shadow-lg shadow-indigo-500/10">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h4 className="text-base font-bold text-white">Pro Interviewer</h4>
                  <span className="text-xs font-mono text-indigo-400 font-semibold">POPULAR</span>
                </div>
                <p className="text-xs text-slate-400">Unlimited AI mock sessions & behavioral feedback.</p>
                <div className="text-3xl font-extrabold text-white font-mono">
                  $29 <span className="text-xs font-normal text-slate-400">/ month</span>
                </div>
                <ul className="space-y-2.5 text-xs text-slate-300 pt-2">
                  <li className="flex items-center gap-2">
                    <Check className="h-4 w-4 text-emerald-400" />
                    <span>Unlimited AI question generation tailored to company</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-4 w-4 text-emerald-400" />
                    <span>Full Gemini 2.5 code review & bottleneck detection</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-4 w-4 text-emerald-400" />
                    <span>Comprehensive analytics & weak topic diagnosis</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-4 w-4 text-emerald-400" />
                    <span>System Design & Spring Boot specialized tracks</span>
                  </li>
                </ul>
              </div>
              <Link
                to="/interview-setup"
                className="mt-8 block text-center rounded-lg bg-indigo-600 py-2.5 text-xs font-semibold text-white hover:bg-indigo-500 shadow-md shadow-indigo-500/20"
              >
                Start 14-Day Free Trial
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Final Call to Action */}
      <section className="py-20 bg-gradient-to-b from-slate-950 to-slate-900/80">
        <div className="mx-auto max-w-4xl px-4 text-center space-y-6">
          <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Ready to Land Your Dream Software Engineering Offer?
          </h2>
          <p className="text-slate-400 text-sm sm:text-base max-w-xl mx-auto leading-relaxed">
            Join thousands of candidates who transformed their technical interview performance with CodeMentor AI.
          </p>
          <div className="pt-2">
            <Link
              to="/interview-setup"
              className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-8 py-3.5 text-sm font-semibold text-white hover:bg-indigo-500 transition-all shadow-lg shadow-indigo-500/25"
            >
              <span>Launch Your First Interview</span>
              <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};
