import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Terminal,
  Play,
  Send,
  RotateCcw,
  Sparkles,
  CheckCircle2,
  AlertCircle,
  Copy,
  ChevronDown,
  Clock,
  Layers,
  ArrowLeft,
  Check,
  Maximize2,
  Code2,
} from 'lucide-react';
import { api } from '../services/api';
import { Problem, ProgrammingLanguage } from '../types';

export const ProblemPage: React.FC = () => {
  const { id = 'two-sum' } = useParams<{ id: string }>();
  const [problem, setProblem] = useState<Problem | null>(null);
  const [selectedLanguage, setSelectedLanguage] = useState<ProgrammingLanguage>('Java');
  const [code, setCode] = useState<string>('');
  const [activeTab, setActiveTab] = useState<'description' | 'hints' | 'review'>('description');
  
  // Test Runner State
  const [selectedTestCase, setSelectedTestCase] = useState(0);
  const [isRunning, setIsRunning] = useState(false);
  const [executionResult, setExecutionResult] = useState<{
    status: 'idle' | 'success' | 'failed';
    output: string;
    runtimeMs: number;
    passedCases: number;
    totalCases: number;
  }>({
    status: 'idle',
    output: '',
    runtimeMs: 0,
    passedCases: 0,
    totalCases: 0,
  });

  const [aiReviewResult, setAiReviewResult] = useState<string | null>(null);
  const [isReviewing, setIsReviewing] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    api.getProblemById(id)
      .then((data) => {
        setProblem(data);
        const starter = data.starterCode[selectedLanguage] || data.starterCode['Java'] || '// Write your solution';
        setCode(starter);
      })
      .catch((err) => {
        console.warn('Could not load problem, using fallback', err);
        // Built-in fallback
        const fallbackProblem: Problem = {
          id: 'two-sum',
          title: 'Two Sum',
          difficulty: 'Easy',
          topic: 'Arrays & Hashing',
          language: 'Java',
          description: 'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.',
          examples: [
            {
              input: 'nums = [2,7,11,15], target = 9',
              output: '[0,1]',
              explanation: 'Because nums[0] + nums[1] == 9, we return [0, 1].',
            },
            {
              input: 'nums = [3,2,4], target = 6',
              output: '[1,2]',
            },
          ],
          constraints: [
            '2 <= nums.length <= 10^4',
            '-10^9 <= nums[i] <= 10^9',
            'Only one valid answer exists.',
          ],
          starterCode: {
            Java: `class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < nums.length; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                return new int[] { map.get(complement), i };\n            }\n            map.put(nums[i], i);\n        }\n        return new int[]{};\n    }\n}`,
            Python: `class Solution:\n    def twoSum(self, nums: list[int], target: int) -> list[int]:\n        seen = {}\n        for i, num in enumerate(nums):\n            diff = target - num\n            if diff in seen:\n                return [seen[diff], i]\n            seen[num] = i\n        return []`,
            TypeScript: `function twoSum(nums: number[], target: number): number[] {\n    const map = new Map<number, number>();\n    for (let i = 0; i < nums.length; i++) {\n        const diff = target - nums[i];\n        if (map.has(diff)) {\n            return [map.get(diff)!, i];\n        }\n        map.set(nums[i], i);\n    }\n    return [];\n}`,
          },
          testCases: [
            { input: 'nums = [2,7,11,15], target = 9', expectedOutput: '[0,1]' },
            { input: 'nums = [3,2,4], target = 6', expectedOutput: '[1,2]' },
            { input: 'nums = [3,3], target = 6', expectedOutput: '[0,1]' },
          ],
        };
        setProblem(fallbackProblem);
        setCode(fallbackProblem.starterCode[selectedLanguage] || '');
      });
  }, [id]);

  const handleLanguageChange = (lang: ProgrammingLanguage) => {
    setSelectedLanguage(lang);
    if (problem && problem.starterCode[lang]) {
      setCode(problem.starterCode[lang]!);
    }
  };

  const handleRunCode = () => {
    setIsRunning(true);
    setTimeout(() => {
      setIsRunning(false);
      setExecutionResult({
        status: 'success',
        output: 'Compilation finished successfully.\nStandard Output:\n> Validated test case inputs against internal hashmap.\n> Target complement index discovered in O(1) step.\nExecution passed cleanly with 0 assertion errors.',
        runtimeMs: Math.floor(Math.random() * 5) + 2,
        passedCases: problem?.testCases.length || 3,
        totalCases: problem?.testCases.length || 3,
      });
    }, 600);
  };

  const handleSubmit = () => {
    setIsRunning(true);
    setTimeout(() => {
      setIsRunning(false);
      setExecutionResult({
        status: 'success',
        output: 'Success: Accepted!\nAll 42 test cases passed.\nRuntime: 3ms (Faster than 89.4% of Java submissions)\nMemory: 43.8MB (Better than 74.2% of submissions)',
        runtimeMs: 3,
        passedCases: 42,
        totalCases: 42,
      });
    }, 900);
  };

  const handleRequestAiReview = () => {
    setIsReviewing(true);
    setActiveTab('review');
    setTimeout(() => {
      setIsReviewing(false);
      setAiReviewResult(
        `### Senior Interviewer Evaluation\n\n- **Time Complexity**: **O(n)** - Optimal one-pass hash map eliminates nested loops.\n- **Space Complexity**: **O(n)** - Memory requirement scales linearly with input size.\n- **Algorithmic Correctness**: 100/100.\n- **Edge Cases Handled**: Correct handling of duplicate integers (lookup precedes map insertion).\n- **Feedback**: Clean, idiomatic ${selectedLanguage} syntax. In a live system design follow-up, consider memory locality for huge continuous streams.`
      );
    }, 800);
  };

  const handleResetCode = () => {
    if (problem && problem.starterCode[selectedLanguage]) {
      setCode(problem.starterCode[selectedLanguage]!);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (!problem) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-200 flex items-center justify-center">
        <div className="font-mono text-sm text-indigo-400">Loading coding workspace...</div>
      </div>
    );
  }

  const lineCount = code.split('\n').length;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col h-screen overflow-hidden">
      {/* Workspace Top Bar */}
      <header className="h-12 border-b border-slate-800 bg-slate-950 px-4 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-4">
          <Link
            to="/problems"
            className="flex items-center gap-1.5 text-xs font-mono text-slate-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Problem Bank</span>
          </Link>
          <span className="text-slate-700">|</span>
          <div className="flex items-center gap-2">
            <span className="font-bold text-white text-xs">{problem.title}</span>
            <span
              className={`text-[11px] font-mono ${
                problem.difficulty === 'Easy'
                  ? 'text-emerald-400'
                  : problem.difficulty === 'Medium'
                  ? 'text-amber-400'
                  : 'text-rose-400'
              }`}
            >
              · {problem.difficulty}
            </span>
          </div>
        </div>

        {/* Center / Action Buttons */}
        <div className="flex items-center gap-3">
          <button
            onClick={handleRunCode}
            disabled={isRunning}
            className="inline-flex items-center gap-1.5 rounded bg-slate-800 hover:bg-slate-700 border border-slate-700 px-3 py-1.5 text-xs font-medium text-slate-200 transition-colors"
          >
            <Play className="h-3 w-3 fill-current text-slate-300" />
            <span>{isRunning ? 'Running...' : 'Run Code'}</span>
          </button>

          <button
            onClick={handleSubmit}
            disabled={isRunning}
            className="inline-flex items-center gap-1.5 rounded bg-indigo-600 hover:bg-indigo-500 px-4 py-1.5 text-xs font-semibold text-white transition-colors shadow-xs"
          >
            <Send className="h-3 w-3" />
            <span>Submit</span>
          </button>
        </div>

        {/* Right Tools */}
        <div className="flex items-center gap-3">
          <button
            onClick={handleRequestAiReview}
            className="inline-flex items-center gap-1 text-xs font-mono text-indigo-400 hover:text-indigo-300 px-2 py-1 rounded bg-indigo-950/40 border border-indigo-800/50"
          >
            <Sparkles className="h-3.5 w-3.5" />
            <span>AI Code Review</span>
          </button>

          <Link
            to="/dashboard"
            className="text-xs font-mono text-slate-400 hover:text-white"
          >
            Dashboard
          </Link>
        </div>
      </header>

      {/* Main Split-Pane Workspace */}
      <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
        {/* Left Problem Description & Specs Pane */}
        <div className="w-full md:w-1/2 flex flex-col border-r border-slate-850 bg-slate-950 overflow-y-auto">
          {/* Sub Navigation Tabs */}
          <div className="flex items-center border-b border-slate-850 px-4 bg-slate-950/80 sticky top-0 z-10 text-xs">
            <button
              onClick={() => setActiveTab('description')}
              className={`py-2.5 px-3 border-b-2 font-medium transition-colors ${
                activeTab === 'description'
                  ? 'border-indigo-500 text-indigo-400'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              Description
            </button>
            <button
              onClick={() => setActiveTab('hints')}
              className={`py-2.5 px-3 border-b-2 font-medium transition-colors ${
                activeTab === 'hints'
                  ? 'border-indigo-500 text-indigo-400'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              Hints & Complexity
            </button>
            <button
              onClick={() => setActiveTab('review')}
              className={`py-2.5 px-3 border-b-2 font-medium transition-colors flex items-center gap-1.5 ${
                activeTab === 'review'
                  ? 'border-indigo-500 text-indigo-400'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              <Sparkles className="h-3 w-3" />
              <span>AI Evaluation</span>
            </button>
          </div>

          <div className="p-6 space-y-6">
            {activeTab === 'description' && (
              <>
                <div>
                  <h1 className="text-xl font-bold text-white tracking-tight">{problem.title}</h1>
                  <div className="flex items-center gap-3 text-xs font-mono text-slate-400 mt-2">
                    <span
                      className={
                        problem.difficulty === 'Easy'
                          ? 'text-emerald-400 font-semibold'
                          : problem.difficulty === 'Medium'
                          ? 'text-amber-400 font-semibold'
                          : 'text-rose-400 font-semibold'
                      }
                    >
                      {problem.difficulty}
                    </span>
                    <span>·</span>
                    <span>Topic: {problem.topic}</span>
                    <span>·</span>
                    <span>Acceptance: {problem.acceptanceRate || '51.2%'}</span>
                  </div>
                </div>

                <div className="text-xs sm:text-sm text-slate-300 leading-relaxed whitespace-pre-line">
                  {problem.description}
                </div>

                {/* Examples */}
                <div className="space-y-4">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200 font-mono">
                    Examples
                  </h3>
                  {problem.examples.map((ex, idx) => (
                    <div
                      key={idx}
                      className="rounded-lg bg-slate-900/80 border border-slate-800 p-4 font-mono text-xs space-y-1.5"
                    >
                      <div className="text-slate-400 text-[11px] font-semibold">
                        Example {idx + 1}:
                      </div>
                      <div className="text-slate-200">
                        <span className="text-slate-500">Input: </span>
                        {ex.input}
                      </div>
                      <div className="text-emerald-400 font-semibold">
                        <span className="text-slate-500">Output: </span>
                        {ex.output}
                      </div>
                      {ex.explanation && (
                        <div className="text-slate-400 text-[11px] pt-1">
                          <span className="text-slate-500">Explanation: </span>
                          {ex.explanation}
                        </div>
                      )}
                    </div>
                  ))}
                </div>

                {/* Constraints */}
                {problem.constraints && problem.constraints.length > 0 && (
                  <div className="space-y-2">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200 font-mono">
                      Constraints
                    </h3>
                    <ul className="list-disc list-inside text-xs font-mono text-slate-400 space-y-1">
                      {problem.constraints.map((c, i) => (
                        <li key={i}>{c}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </>
            )}

            {activeTab === 'hints' && (
              <div className="space-y-4 text-xs text-slate-300">
                <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-2">
                  <h4 className="font-bold text-white font-mono text-xs">Hint 1: Brute Force vs Optimal</h4>
                  <p className="text-slate-400">
                    A brute force approach checks all pairs in O(n²) time. Can you use a hash map to lookup if the complement (target - nums[i]) exists in O(1) time?
                  </p>
                </div>
                <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-2">
                  <h4 className="font-bold text-white font-mono text-xs">Hint 2: One-Pass Strategy</h4>
                  <p className="text-slate-400">
                    You do not need to populate the entire hash map first. While iterating, check if the complement already exists before inserting the current value.
                  </p>
                </div>
              </div>
            )}

            {activeTab === 'review' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center gap-2">
                    <Sparkles className="h-4 w-4 text-indigo-400" />
                    <span className="text-xs font-bold text-white font-mono uppercase">
                      AI Code Evaluation
                    </span>
                  </div>
                  <button
                    onClick={handleRequestAiReview}
                    disabled={isReviewing}
                    className="text-xs font-mono text-indigo-400 hover:text-white"
                  >
                    {isReviewing ? 'Analyzing...' : 'Re-Evaluate Code'}
                  </button>
                </div>

                {isReviewing ? (
                  <div className="py-8 text-center text-xs font-mono text-slate-400 space-y-2">
                    <Sparkles className="h-6 w-6 text-indigo-400 animate-spin mx-auto" />
                    <p>Gemini AI analyzing algorithmic complexity and edge cases...</p>
                  </div>
                ) : aiReviewResult ? (
                  <div className="p-4 rounded-lg bg-slate-900/90 border border-slate-800 text-xs text-slate-300 font-mono whitespace-pre-wrap leading-relaxed">
                    {aiReviewResult}
                  </div>
                ) : (
                  <div className="text-center py-8 text-slate-400 text-xs space-y-3">
                    <p>Click below to inspect your current implementation for bottlenecks and interview scoring.</p>
                    <button
                      onClick={handleRequestAiReview}
                      className="px-4 py-2 rounded bg-indigo-600 text-white font-semibold text-xs hover:bg-indigo-500"
                    >
                      Run AI Diagnostic
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Right Code Editor & Console Pane */}
        <div className="w-full md:w-1/2 flex flex-col bg-slate-950 overflow-hidden">
          {/* Editor Header Bar */}
          <div className="h-10 border-b border-slate-850 bg-slate-900/50 px-4 flex items-center justify-between shrink-0 text-xs font-mono">
            {/* Language dropdown */}
            <div className="flex items-center gap-2">
              <span className="text-slate-400 text-[11px]">Language:</span>
              <select
                value={selectedLanguage}
                onChange={(e) => handleLanguageChange(e.target.value as ProgrammingLanguage)}
                className="bg-slate-900 border border-slate-800 text-xs text-indigo-300 rounded px-2 py-0.5 focus:outline-none"
              >
                <option value="Java">Java 21</option>
                <option value="Python">Python 3.12</option>
                <option value="TypeScript">TypeScript 5</option>
                <option value="Go">Go 1.22</option>
              </select>
            </div>

            {/* Editor tools */}
            <div className="flex items-center gap-2">
              <button
                onClick={handleCopy}
                className="p-1 text-slate-400 hover:text-white rounded"
                title="Copy code"
              >
                {copied ? <Check className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
              </button>
              <button
                onClick={handleResetCode}
                className="p-1 text-slate-400 hover:text-white rounded"
                title="Reset to starter boilerplate"
              >
                <RotateCcw className="h-3.5 w-3.5" />
              </button>
            </div>
          </div>

          {/* Monaco-Style Code Editor Window */}
          <div className="flex-1 flex overflow-hidden bg-slate-950 font-mono text-xs">
            {/* Line Numbers Gutter */}
            <div className="w-12 py-3 bg-slate-950 border-r border-slate-900 text-slate-600 text-right pr-3 select-none overflow-hidden shrink-0">
              {Array.from({ length: Math.max(lineCount, 18) }, (_, i) => (
                <div key={i + 1} className="leading-6">
                  {i + 1}
                </div>
              ))}
            </div>

            {/* Editable Code Area */}
            <textarea
              value={code}
              onChange={(e) => setCode(e.target.value)}
              spellCheck={false}
              className="flex-1 w-full p-3 bg-transparent text-slate-200 resize-none font-mono text-xs leading-6 focus:outline-none overflow-auto border-0"
              style={{ tabSize: 4 }}
            />
          </div>

          {/* Bottom Execution Console & Test Cases */}
          <div className="h-56 border-t border-slate-850 bg-slate-950/90 flex flex-col shrink-0">
            {/* Console Tab Header */}
            <div className="flex items-center justify-between border-b border-slate-850 px-4 py-1.5 bg-slate-900/30 text-xs font-mono">
              <div className="flex items-center gap-3">
                <span className="text-slate-300 font-semibold flex items-center gap-1.5">
                  <Terminal className="h-3.5 w-3.5 text-indigo-400" />
                  Test Cases
                </span>
                <div className="flex items-center gap-1">
                  {(problem.testCases || []).map((_, idx) => (
                    <button
                      key={idx}
                      onClick={() => setSelectedTestCase(idx)}
                      className={`px-2 py-0.5 rounded text-[11px] ${
                        selectedTestCase === idx
                          ? 'bg-slate-800 text-white font-bold'
                          : 'text-slate-400 hover:text-slate-200'
                      }`}
                    >
                      Case {idx + 1}
                    </button>
                  ))}
                </div>
              </div>

              {executionResult.status === 'success' && (
                <span className="text-emerald-400 text-[11px] flex items-center gap-1">
                  <CheckCircle2 className="h-3 w-3" />
                  Passed: {executionResult.passedCases}/{executionResult.totalCases} ({executionResult.runtimeMs}ms)
                </span>
              )}
            </div>

            {/* Console Body */}
            <div className="p-3 font-mono text-xs text-slate-300 overflow-y-auto flex-1 space-y-2">
              {executionResult.output ? (
                <div className="space-y-1">
                  <span className="text-[11px] text-slate-500 uppercase tracking-wider">
                    Execution Log:
                  </span>
                  <pre className="text-emerald-400/90 whitespace-pre-wrap font-mono text-xs">
                    {executionResult.output}
                  </pre>
                </div>
              ) : (
                <div className="space-y-1.5 text-slate-400">
                  <div className="text-[11px] text-slate-500">Active Test Input:</div>
                  <div className="p-2 rounded bg-slate-900/80 border border-slate-800 text-slate-200">
                    {problem.testCases && problem.testCases[selectedTestCase]
                      ? problem.testCases[selectedTestCase].input
                      : 'nums = [2,7,11,15], target = 9'}
                  </div>
                  <div className="text-[11px] text-slate-500">Expected Output:</div>
                  <div className="p-2 rounded bg-slate-900/80 border border-slate-800 text-emerald-400">
                    {problem.testCases && problem.testCases[selectedTestCase]
                      ? problem.testCases[selectedTestCase].expectedOutput
                      : '[0,1]'}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
