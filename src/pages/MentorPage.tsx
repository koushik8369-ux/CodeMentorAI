import React, { useState, useEffect, useRef } from 'react';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { api } from '../services/api';
import { localStorageService } from '../services/localStorageService';
import { AiMentorMessage } from '../types';
import {
  Bot,
  User,
  Send,
  Sparkles,
  Trash2,
  Download,
  Copy,
  Check,
  Code2,
  Terminal,
  Cpu,
  Layers,
  BookOpen,
  AlertCircle,
  AlertTriangle,
  CheckCircle2,
  HelpCircle,
  RotateCcw,
  Compass,
} from 'lucide-react';

const QUICK_TOPICS = [
  {
    category: '☕ Java & OOP',
    prompt: 'Explain Spring Boot dependency injection and constructor injection vs field injection with code examples.',
    language: 'Java',
    context: 'Spring Boot & REST APIs',
  },
  {
    category: '🧩 DSA & LeetCode',
    prompt: 'How do I identify when to use Two Pointers vs Sliding Window for array and substring problems?',
    language: 'Java',
    context: 'Data Structures & Algorithms',
  },
  {
    category: '⚛️ React & TypeScript',
    prompt: 'Explain how to safely fetch async data in useEffect without race conditions and memory leaks.',
    language: 'TypeScript',
    context: 'React & Frontend',
  },
  {
    category: '🗄️ SQL & Indexing',
    prompt: 'How does a B-Tree index work under the hood in PostgreSQL, and when should I use composite indexing?',
    language: 'SQL',
    context: 'Database Concepts',
  },
  {
    category: '🐞 Code Review & Debugging',
    prompt: 'What are the most common memory leaks and concurrency pitfalls in backend services, and how do I profile them?',
    language: 'Java',
    context: 'Debugging & Code Review',
  },
  {
    category: '🎯 Tech Interview Prep',
    prompt: 'Give me a structured step-by-step framework for tackling ambiguous System Design interview questions.',
    language: 'System Architecture',
    context: 'Technical Interview Preparation',
  },
];

const AVAILABLE_MODELS = [
  { id: 'tencent/hy4', label: 'Tencent Hy4 (Active Default)' },
  { id: 'deepseek/deepseek-chat', label: 'DeepSeek V3' },
  { id: 'meta-llama/llama-3.3-70b-instruct', label: 'Llama 3.3 70B Instruct' },
  { id: 'openai/gpt-4o-mini', label: 'GPT-4o Mini' },
];

export const MentorPage: React.FC = () => {
  const [messages, setMessages] = useState<AiMentorMessage[]>(() => {
    const saved = localStorageService.getMentorChat();
    if (saved && saved.length > 0) return saved;
    return [
      {
        id: 'welcome-1',
        role: 'assistant',
        content: `### Welcome to CodeMentor AI Programming Mentor! 👋

I'm your **Senior Software Engineering Mentor**, powered by the **OpenRouter API** architecture.

I'm here to help you bridge theory and production-grade engineering across:
- **Core Java & OOP** (Design patterns, encapsulation, SOLID, Java 17/21)
- **Data Structures & Algorithms** (Pattern recognition, complexity $O(n)$, LeetCode breakdown)
- **Spring Boot & RESTful APIs** (Dependency injection, security, clean service boundaries)
- **React & TypeScript** (Hooks, state architecture, typing generics, render optimization)
- **SQL & Databases** (Indexing, execution plans, ACID transactions)
- **Technical Interview Strategy** (FAANG-style live screens & system design)

Choose a quick starter topic above or type any coding question or snippet below!`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        model: 'OpenRouter AI',
        status: 'SUCCESS',
      },
    ];
  });

  const [inputMessage, setInputMessage] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('Java');
  const [selectedContext, setSelectedContext] = useState('Full-Stack Software Engineering');
  const [selectedModel, setSelectedModel] = useState('tencent/hy4');
  const [hasUserManuallySelectedModel, setHasUserManuallySelectedModel] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [backendConfig, setBackendConfig] = useState<{
    defaultModel: string;
    fallbackModel?: string;
    isConfigured: boolean;
    provider: string;
  } | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    api.getMentorStatus()
      .then((status) => {
        setBackendConfig({
          defaultModel: status.defaultModel,
          fallbackModel: status.fallbackModel,
          isConfigured: status.isConfigured,
          provider: status.provider,
        });
        if (status.defaultModel && !hasUserManuallySelectedModel) {
          setSelectedModel(status.defaultModel);
        }
      })
      .catch(() => {
        // Safe default
      });
  }, [hasUserManuallySelectedModel]);

  useEffect(() => {
    localStorageService.saveMentorChat(messages);
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (messageToSend?: string) => {
    const text = (messageToSend ?? inputMessage).trim();
    if (!text || isSubmitting) return;

    const userMsg: AiMentorMessage = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      language: selectedLanguage,
      context: selectedContext,
    };

    const updated = [...messages, userMsg];
    setMessages(updated);
    setInputMessage('');
    setIsSubmitting(true);

    try {
      // Build lightweight conversation history
      const historyPayload = updated
        .slice(-8)
        .map((m) => ({ role: m.role, content: m.content }));

      // Only send customModel if the user explicitly changed the model to something
      // other than the authoritative backend default
      const customOverride = hasUserManuallySelectedModel && selectedModel !== backendConfig?.defaultModel
        ? selectedModel
        : undefined;

      const res = await api.chatWithMentor({
        message: text,
        conversationHistory: historyPayload,
        programmingLanguage: selectedLanguage,
        learningContext: selectedContext,
        customModel: customOverride,
      });

      const rawText = res.aiResponse || (res as any).response || (res as any).content || (res as any).message;
      const cleanContent = (rawText && typeof rawText === 'string' && rawText.trim() !== '' && rawText.trim() !== 'null')
        ? rawText.trim()
        : (res.notice || 'Heuristic mentor response provided. Upstream AI content was not available.');

      const resolvedStatus = (res.status === 'SUCCESS' && (!rawText || rawText === 'null' || rawText.trim() === ''))
        ? 'FALLBACK'
        : res.status;

      const assistantMsg: AiMentorMessage = {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: cleanContent,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        model: res.selectedModel || selectedModel,
        status: resolvedStatus,
        language: res.programmingLanguage,
        context: res.learningContext,
        validationStatus: res.validationStatus,
        validationDetails: res.validationDetails,
        validationSummary: res.validationSummary,
      };

      setMessages((prev) => [...prev, assistantMsg]);
    } catch (err: any) {
      const errorMsg: AiMentorMessage = {
        id: `err-${Date.now()}`,
        role: 'assistant',
        content: `### ⚠️ Mentor Connection Notice\n\nCould not connect to the backend mentor service: ${err.message || 'Network error'}.\n\nPlease ensure your backend is accessible and your \`OPENROUTER_API_KEY\` is configured in Render environment variables.`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        status: 'ERROR',
        model: selectedModel,
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleClearChat = () => {
    if (window.confirm('Are you sure you want to clear your mentor conversation history?')) {
      localStorageService.clearMentorChat();
      setMessages([
        {
          id: 'welcome-reset',
          role: 'assistant',
          content: 'Conversation history cleared. What would you like to learn or review next?',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          model: selectedModel,
          status: 'SUCCESS',
        },
      ]);
    }
  };

  const handleExportChat = () => {
    const markdown = messages
      .map(
        (m) =>
          `### ${m.role === 'user' ? 'Candidate' : 'AI Programming Mentor'} (${m.timestamp})\n\n${m.content}\n\n---\n`
      )
      .join('\n');

    const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `codementor-ai-mentor-session-${new Date().toISOString().split('T')[0]}.md`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  // Helper to render markdown content with syntax codeblocks & copy buttons
  const renderMessageContent = (msg: AiMentorMessage) => {
    const { content, id: messageId, validationDetails } = msg;
    if (!content || typeof content !== 'string') {
      return (
        <p className="text-amber-300 text-sm italic">
          No content received for this response. Please retry your query or check model configuration.
        </p>
      );
    }

    const sanitized = content
      .replace(/<(think|thought)[^>]*>[\s\S]*?<\/\1>/gi, '')
      .replace(/<\/?(think|thought)[^>]*>/gi, '')
      .trim();

    if (!sanitized || sanitized === 'null') {
      return (
        <p className="text-amber-300 text-sm italic">
          No content received for this response. Please retry your query or check model configuration.
        </p>
      );
    }

    const parts = sanitized.split(/(```[\s\S]*?```)/g);
    let codeBlockIndex = 0;

    return parts.map((part, index) => {
      if (part.startsWith('```') && part.endsWith('```')) {
        const currentBlockIdx = codeBlockIndex++;
        const validation = validationDetails?.find((v) => v.blockIndex === currentBlockIdx);
        const lines = part.slice(3, -3).trim().split('\n');
        const firstLine = lines[0].trim();
        const codeLang = firstLine && !firstLine.includes(' ') ? firstLine : 'code';
        const codeText = (codeLang === firstLine ? lines.slice(1) : lines).join('\n');
        const codeBlockId = `${messageId}-code-${index}`;

        return (
          <div
            key={index}
            className="my-3 rounded-xl border border-slate-800 bg-slate-950 overflow-hidden font-mono text-xs shadow-md"
          >
            <div className="flex items-center justify-between px-3 py-1.5 bg-slate-900 border-b border-slate-800/80 text-slate-400">
              <div className="flex items-center gap-2">
                <span className="text-[11px] font-semibold text-indigo-400 uppercase tracking-wider">
                  {codeLang}
                </span>
                {validation && validation.status === 'VALID' && (
                  <span
                    className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20"
                    title={validation.validationSummary || 'Java syntax parsed and verified using JavaParser (Java 21 grammar)'}
                  >
                    <Check className="w-3 h-3 text-emerald-400" />
                    <span>{validation.isCorrected ? 'Syntax Repaired (Java 21)' : 'Syntax Validated (Java 21)'}</span>
                  </span>
                )}
                {validation && validation.status === 'INVALID' && (
                  <span
                    className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-amber-500/10 text-amber-400 border border-amber-500/20"
                    title={validation.errorMessage || validation.validationSummary || 'JavaParser detected syntax errors'}
                  >
                    <AlertTriangle className="w-3 h-3 text-amber-400" />
                    <span>Syntax Warning: Parse issues</span>
                  </span>
                )}
                {validation && validation.status === 'NOT_CHECKED' && (
                  <span
                    className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-slate-800/60 text-slate-400 border border-slate-700/60"
                    title="Syntax validation applies specifically to Java snippets"
                  >
                    <span>Syntax check: N/A</span>
                  </span>
                )}
              </div>
              <button
                onClick={() => copyToClipboard(codeText, codeBlockId)}
                className="flex items-center gap-1 text-[11px] hover:text-white transition-colors cursor-pointer"
              >
                {copiedId === codeBlockId ? (
                  <>
                    <Check className="w-3 h-3 text-emerald-400" />
                    <span className="text-emerald-400">Copied!</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3 h-3" />
                    <span>Copy</span>
                  </>
                )}
              </button>
            </div>
            {validation && validation.isCorrected && (
              <div className="px-3 py-1 bg-emerald-950/20 border-b border-emerald-500/20 text-emerald-300 text-[11px] flex items-center gap-1.5 font-sans">
                <Check className="w-3 h-3 text-emerald-400 shrink-0" />
                <span>Automatically repaired and validated against Java 21 syntax grammar.</span>
              </div>
            )}
            {validation && validation.status === 'INVALID' && validation.errorMessage && (
              <div className="px-3 py-1.5 bg-amber-950/30 border-b border-amber-500/20 text-amber-300 text-[11px] flex items-center gap-1.5 font-sans">
                <AlertTriangle className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                <span className="truncate" title={validation.errorMessage}>
                  Syntax Notice: {validation.errorMessage}
                </span>
              </div>
            )}
            <pre className="p-4 overflow-x-auto text-slate-200 leading-relaxed font-mono">
              <code>{codeText}</code>
            </pre>
          </div>
        );
      }

      // Format markdown headings & bold text
      const formatted = part.split('\n').map((line, lIdx) => {
        if (line.startsWith('### ')) {
          return (
            <h4 key={lIdx} className="font-bold text-white text-sm sm:text-base mt-3 mb-1 text-indigo-300">
              {line.replace('### ', '')}
            </h4>
          );
        }
        if (line.startsWith('## ')) {
          return (
            <h3 key={lIdx} className="font-bold text-white text-base sm:text-lg mt-4 mb-2">
              {line.replace('## ', '')}
            </h3>
          );
        }
        if (line.startsWith('# ')) {
          return (
            <h2 key={lIdx} className="font-extrabold text-white text-lg sm:text-xl mt-4 mb-2">
              {line.replace('# ', '')}
            </h2>
          );
        }
        if (line.startsWith('- ')) {
          return (
            <li key={lIdx} className="ml-4 list-disc text-slate-300 text-sm my-0.5">
              {line.replace('- ', '')}
            </li>
          );
        }
        if (/^\d+\.\s/.test(line)) {
          return (
            <li key={lIdx} className="ml-4 list-decimal text-slate-300 text-sm my-0.5">
              {line.replace(/^\d+\.\s/, '')}
            </li>
          );
        }
        if (line.trim() === '') {
          return <div key={lIdx} className="h-1.5" />;
        }
        return (
          <p key={lIdx} className="text-slate-300 text-sm leading-relaxed my-1">
            {line}
          </p>
        );
      });

      return <div key={index}>{formatted}</div>;
    });
  };

  return (
    <DashboardLayout>
      <div className="flex flex-col h-[calc(100vh-4rem)] max-w-6xl mx-auto space-y-4">
        {/* Header Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 bg-slate-900 border border-slate-800 rounded-2xl shadow-lg shrink-0">
          <div>
            <div className="flex items-center gap-2 text-xs font-mono text-indigo-400">
              <Bot className="w-3.5 h-3.5" />
              <span>OPENROUTER AI ENGINE · SENIOR STAFF MENTOR</span>
            </div>
            <h1 className="text-xl sm:text-2xl font-bold text-white tracking-tight flex items-center gap-2">
              <span>AI Programming Mentor</span>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-normal">
                v2.0
              </span>
            </h1>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {/* Model Selector */}
            <div className="flex items-center gap-1.5 bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs">
              <Cpu className="w-3.5 h-3.5 text-slate-400" />
              <select
                value={selectedModel}
                onChange={(e) => {
                  setSelectedModel(e.target.value);
                  setHasUserManuallySelectedModel(true);
                }}
                className="bg-transparent text-slate-200 text-xs focus:outline-none cursor-pointer"
              >
                {backendConfig?.defaultModel &&
                  !AVAILABLE_MODELS.some((m) => m.id === backendConfig.defaultModel) && (
                    <option value={backendConfig.defaultModel} className="bg-slate-900 text-white">
                      {backendConfig.defaultModel} (Server Default)
                    </option>
                  )}
                {AVAILABLE_MODELS.map((m) => (
                  <option key={m.id} value={m.id} className="bg-slate-900 text-white">
                    {m.id === backendConfig?.defaultModel ? `${m.label} (Server Default)` : m.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Clear Chat */}
            <button
              onClick={handleClearChat}
              className="p-2 rounded-xl border border-slate-800 bg-slate-950 hover:bg-slate-800 text-slate-400 hover:text-rose-400 transition"
              title="Clear conversation history"
            >
              <Trash2 className="w-4 h-4" />
            </button>

            {/* Export Markdown */}
            <button
              onClick={handleExportChat}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-slate-800 bg-slate-950 hover:bg-slate-800 text-slate-300 hover:text-white text-xs font-medium transition"
              title="Export session as Markdown"
            >
              <Download className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Export</span>
            </button>
          </div>
        </div>

        {/* Quick Topics Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 shrink-0 no-scrollbar">
          <span className="text-xs font-mono text-slate-500 flex items-center gap-1 shrink-0">
            <Compass className="w-3.5 h-3.5 text-indigo-400" />
            <span>Topics:</span>
          </span>
          {QUICK_TOPICS.map((topic, idx) => (
            <button
              key={idx}
              onClick={() => {
                setSelectedLanguage(topic.language);
                setSelectedContext(topic.context);
                handleSend(topic.prompt);
              }}
              disabled={isSubmitting}
              className="shrink-0 px-3 py-1 rounded-lg border border-slate-800 bg-slate-900 hover:bg-slate-800 hover:border-indigo-500/40 text-slate-300 hover:text-white text-xs transition cursor-pointer"
            >
              {topic.category}
            </button>
          ))}
        </div>

        {/* Main Conversation Stream */}
        <div className="flex-1 overflow-y-auto space-y-4 pr-1">
          {messages.map((msg) => {
            const isUser = msg.role === 'user';

            return (
              <div
                key={msg.id}
                className={`flex gap-3 ${isUser ? 'justify-end' : 'justify-start'}`}
              >
                {!isUser && (
                  <div className="w-8 h-8 rounded-xl bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center shrink-0 mt-1">
                    <Bot className="w-4 h-4" />
                  </div>
                )}

                <div
                  className={`max-w-3xl rounded-2xl p-4 sm:p-5 shadow-lg ${
                    isUser
                      ? 'bg-indigo-600/20 border border-indigo-500/40 text-white ml-12'
                      : 'bg-slate-900 border border-slate-800 text-slate-200 mr-12'
                  }`}
                >
                  {/* Message Meta Header */}
                  <div className="flex items-center justify-between gap-3 mb-2 text-xs text-slate-400 font-mono border-b border-slate-800/60 pb-1.5">
                    <span className="font-semibold text-slate-300">
                      {isUser ? 'Candidate' : 'CodeMentor AI'}
                    </span>
                    <div className="flex items-center gap-2 text-[11px]">
                      {msg.model && (
                        <span className="text-indigo-400">
                          {msg.model}
                        </span>
                      )}
                      <span>{msg.timestamp}</span>
                    </div>
                  </div>

                  {/* Message Body */}
                  <div className="space-y-1">
                    {renderMessageContent(msg)}
                  </div>

                  {/* Java Syntax Validation Summary */}
                  {msg.validationStatus === 'VALID' && (
                    <div className="mt-2.5 text-[11px] text-emerald-400/90 flex items-center gap-1.5 font-mono">
                      <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                      <span>{msg.validationSummary || 'All Java code snippets validated successfully against Java 21 syntax grammar.'}</span>
                    </div>
                  )}
                  {msg.validationStatus === 'INVALID' && (
                    <div className="mt-2.5 text-[11px] text-amber-400/90 flex items-center gap-1.5 font-mono">
                      <AlertTriangle className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                      <span>{msg.validationSummary || 'One or more Java code snippets contain syntax parse issues.'}</span>
                    </div>
                  )}

                  {/* Fallback Notice if active */}
                  {msg.status === 'FALLBACK' && (
                    <div className="mt-3 p-2.5 rounded-lg bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs flex items-start gap-2">
                      <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-amber-400" />
                      <span>
                        {backendConfig?.isConfigured
                          ? 'Heuristic mentor engine response active (upstream OpenRouter API response unavailable or model routing issue).'
                          : <>Heuristic mentor engine response active. For full live LLM generations, add your rotated <code className="font-mono text-amber-200">OPENROUTER_API_KEY</code> to Render backend environment variables.</>}
                      </span>
                    </div>
                  )}
                </div>

                {isUser && (
                  <div className="w-8 h-8 rounded-xl bg-slate-800 text-slate-300 border border-slate-700 flex items-center justify-center shrink-0 mt-1">
                    <User className="w-4 h-4" />
                  </div>
                )}
              </div>
            );
          })}

          {isSubmitting && (
            <div className="flex gap-3 justify-start items-center">
              <div className="w-8 h-8 rounded-xl bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center shrink-0">
                <Bot className="w-4 h-4 animate-pulse" />
              </div>
              <div className="bg-slate-900 border border-slate-800 rounded-2xl px-5 py-3 text-xs text-slate-400 flex items-center gap-2">
                <div className="w-3.5 h-3.5 border-2 border-indigo-400 border-t-transparent rounded-full animate-spin" />
                <span>Formulating structured engineering mentorship via {selectedModel}...</span>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Input & Context Form */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-3 sm:p-4 shadow-xl shrink-0 space-y-2">
          {/* Context Selectors */}
          <div className="flex flex-wrap items-center gap-3 text-xs">
            <div className="flex items-center gap-1.5 text-slate-400 font-mono">
              <Code2 className="w-3.5 h-3.5 text-indigo-400" />
              <span>Language:</span>
              <select
                value={selectedLanguage}
                onChange={(e) => setSelectedLanguage(e.target.value)}
                className="bg-slate-950 border border-slate-800 rounded-lg px-2 py-1 text-slate-200 focus:outline-none"
              >
                <option value="Java">Java 21</option>
                <option value="TypeScript">React / TypeScript</option>
                <option value="Spring Boot">Spring Boot & REST</option>
                <option value="Python">Python</option>
                <option value="SQL">SQL & Database</option>
                <option value="C++">C++</option>
                <option value="Go">Go</option>
                <option value="System Architecture">System Architecture</option>
              </select>
            </div>

            <div className="flex items-center gap-1.5 text-slate-400 font-mono">
              <Layers className="w-3.5 h-3.5 text-indigo-400" />
              <span>Focus:</span>
              <select
                value={selectedContext}
                onChange={(e) => setSelectedContext(e.target.value)}
                className="bg-slate-950 border border-slate-800 rounded-lg px-2 py-1 text-slate-200 focus:outline-none"
              >
                <option value="Technical Interview Preparation">Technical Interview Prep</option>
                <option value="Data Structures & Algorithms">Data Structures & Algorithms</option>
                <option value="Spring Boot & REST APIs">Spring Boot & REST APIs</option>
                <option value="Debugging & Code Review">Debugging & Code Review</option>
                <option value="Project Architecture">Project Architecture</option>
                <option value="Beginner Fundamentals">Beginner Fundamentals</option>
              </select>
            </div>

            <span className="text-[11px] font-mono text-slate-500 ml-auto hidden md:inline">
              Ctrl+Enter to send
            </span>
          </div>

          {/* Textarea + Submit */}
          <div className="flex gap-2 items-end">
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isSubmitting}
              rows={2}
              placeholder="Ask anything: Java OOP, Spring Boot DI, LeetCode algorithms, React hooks, SQL indexing, or paste your code for review..."
              className="flex-1 bg-slate-950 border border-slate-800 rounded-xl p-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-400 resize-none leading-relaxed font-sans"
            />

            <button
              onClick={() => handleSend()}
              disabled={isSubmitting || !inputMessage.trim()}
              className="px-5 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold text-xs transition flex items-center justify-center gap-1.5 shadow-md shadow-indigo-600/20 cursor-pointer h-12"
            >
              <Send className="w-4 h-4" />
              <span className="hidden sm:inline">Ask Mentor</span>
            </button>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};
