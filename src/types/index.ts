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
