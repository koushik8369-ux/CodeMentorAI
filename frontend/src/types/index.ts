export type Difficulty = 'Easy' | 'Medium' | 'Hard';

export type Role = 'Software Engineer' | 'Frontend Engineer' | 'Backend Engineer' | 'Full Stack Engineer' | 'Machine Learning Engineer';

export type ProgrammingLanguage = 'Java' | 'Python' | 'TypeScript' | 'Go' | 'C++';

export type InterviewType = 'Coding Interview' | 'System Design' | 'Data Structures & Algorithms' | 'Framework Deep Dive';

export type InterviewStatus = 'Completed' | 'In Progress' | 'Scheduled' | 'Needs Review';

export interface TestCase {
  input: string;
  expectedOutput: string;
  explanation?: string;
}

export interface Problem {
  id: string;
  title: string;
  difficulty: Difficulty;
  topic: string;
  language: ProgrammingLanguage;
  description: string;
  acceptanceRate?: string;
  examples: {
    input: string;
    output: string;
    explanation?: string;
  }[];
  constraints: string[];
  starterCode: {
    [lang in ProgrammingLanguage]?: string;
  };
  testCases: TestCase[];
}

export interface InterviewSession {
  id: string;
  userId?: string;
  role: Role;
  language: ProgrammingLanguage;
  difficulty: Difficulty;
  type: InterviewType;
  topics: string[];
  questionCount: number;
  score?: number;
  status: InterviewStatus;
  createdAt: string;
  durationMinutes?: number;
}

export interface QuestionGenerationRequest {
  role: string;
  language: string;
  difficulty: Difficulty;
  topic: string;
}

export interface QuestionGenerationResponse {
  title: string;
  description: string;
  difficulty: Difficulty;
  topic: string;
  examples: {
    input: string;
    output: string;
    explanation?: string;
  }[];
  constraints: string[];
  starterCode?: {
    [lang: string]: string;
  };
}

export interface SkillScore {
  name: string;
  score: number;
  totalProblems: number;
  solvedProblems: number;
}

export interface UserMetrics {
  readinessPercentage: number;
  problemsSolved: number;
  interviewsCompleted: number;
  averageScore: number;
  currentStreakDays: number;
  skills: SkillScore[];
}

export interface User {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN' | string;
  createdAt?: string;
}

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface ProblemExample {
  input: string;
  output: string;
  explanation?: string;
}

export interface StartInterviewPayload {
  topic: string;
  difficulty: string;
  language: string;
}

export interface SubmitInterviewPayload {
  code: string;
}

export interface Interview {
  id: number;
  userId?: number;
  role?: string;
  topic: string;
  difficulty: string;
  language: string;
  problemTitle: string;
  problemDescription: string;
  inputFormat?: string;
  outputFormat?: string;
  constraints?: string[];
  examples?: ProblemExample[];
  starterCode?: string;
  userCode?: string;
  score?: number;
  status: 'IN_PROGRESS' | 'COMPLETED' | string;
  correctness?: string;
  codeQuality?: string;
  timeComplexity?: string;
  spaceComplexity?: string;
  strengths?: string[];
  weaknesses?: string[];
  recommendations?: string[];
  aiFeedback?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt?: string;
}

export interface AnalyticsOverview {
  totalInterviews: number;
  averageScore: number;
  highestScore: number;
  lowestScore: number;
  completedInterviews: number;
  strongestTopic: string;
  weakestTopic: string;
  recentAverageScore: number;
}

export interface TopicAnalytics {
  topic: string;
  attempts: number;
  averageScore: number;
  bestScore: number;
}

export interface DifficultyAnalytics {
  difficulty: string;
  attempts: number;
  averageScore: number;
  bestScore: number;
}

export interface LanguageAnalytics {
  language: string;
  attempts: number;
  averageScore: number;
}

export interface PerformanceTrend {
  date: string;
  score: number;
}

export interface AiActionPlan {
  topic: string;
  reason: string;
  recommendedPractice: string;
}

export interface AiInsights {
  overallAssessment: string;
  strongTopics: string[];
  weakTopics: string[];
  recommendedTopics: string[];
  actionPlan: AiActionPlan[];
  nextDifficulty: string;
  summary: string;
}
