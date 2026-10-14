import {
  Interview,
  MockInterview,
  AnalyticsOverview,
  TopicAnalytics,
  DifficultyAnalytics,
  LanguageAnalytics,
  PerformanceTrend,
} from '../types';

const STORAGE_VERSION = 'v1';
const CODING_INTERVIEWS_KEY = `codementor_${STORAGE_VERSION}_interviews`;
const MOCK_INTERVIEWS_KEY = `codementor_${STORAGE_VERSION}_mock_interviews`;
const PREFERENCES_KEY = `codementor_${STORAGE_VERSION}_preferences`;

export interface UserPreferences {
  preferredLanguage?: string;
  targetRole?: string;
  defaultDifficulty?: string;
}

function isStorageAvailable(): boolean {
  try {
    const testKey = '__cm_storage_test__';
    window.localStorage.setItem(testKey, testKey);
    window.localStorage.removeItem(testKey);
    return true;
  } catch {
    return false;
  }
}

function safeGetItem<T>(key: string, defaultValue: T): T {
  if (!isStorageAvailable()) return defaultValue;
  try {
    const raw = window.localStorage.getItem(key);
    if (!raw) return defaultValue;
    const parsed = JSON.parse(raw);
    return parsed ?? defaultValue;
  } catch (err) {
    console.warn(`[CodeMentor Storage] Corrupted data for key "${key}", resetting to default.`, err);
    try {
      window.localStorage.removeItem(key);
    } catch {
      // ignore
    }
    return defaultValue;
  }
}

function safeSetItem<T>(key: string, value: T): boolean {
  if (!isStorageAvailable()) return false;
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
    return true;
  } catch (err) {
    console.error(`[CodeMentor Storage] Failed to persist data to "${key}".`, err);
    return false;
  }
}

export const localStorageService = {
  // Coding Interviews
  getInterviews(): Interview[] {
    const interviews = safeGetItem<Interview[]>(CODING_INTERVIEWS_KEY, []);
    return Array.isArray(interviews)
      ? interviews.sort((a, b) => {
          const dateA = a.startedAt || a.createdAt || '';
          const dateB = b.startedAt || b.createdAt || '';
          return dateB.localeCompare(dateA);
        })
      : [];
  },

  getInterviewById(id: number): Interview | null {
    const list = this.getInterviews();
    return list.find((i) => i.id === id) || null;
  },

  saveInterview(interview: Interview): void {
    if (!interview || typeof interview.id !== 'number') return;
    const list = this.getInterviews();
    const index = list.findIndex((i) => i.id === interview.id);
    if (index >= 0) {
      list[index] = { ...list[index], ...interview };
    } else {
      list.unshift(interview);
    }
    safeSetItem(CODING_INTERVIEWS_KEY, list);
  },

  // Mock Interviews
  getMockInterviews(): MockInterview[] {
    const mocks = safeGetItem<MockInterview[]>(MOCK_INTERVIEWS_KEY, []);
    return Array.isArray(mocks)
      ? mocks.sort((a, b) => {
          const dateA = a.startedAt || '';
          const dateB = b.startedAt || '';
          return dateB.localeCompare(dateA);
        })
      : [];
  },

  getMockInterviewById(id: number): MockInterview | null {
    const list = this.getMockInterviews();
    return list.find((m) => m.id === id) || null;
  },

  saveMockInterview(mock: MockInterview): void {
    if (!mock || typeof mock.id !== 'number') return;
    const list = this.getMockInterviews();
    const index = list.findIndex((m) => m.id === mock.id);
    if (index >= 0) {
      list[index] = { ...list[index], ...mock };
    } else {
      list.unshift(mock);
    }
    safeSetItem(MOCK_INTERVIEWS_KEY, list);
  },

  // Preferences
  getPreferences(): UserPreferences {
    return safeGetItem<UserPreferences>(PREFERENCES_KEY, {
      preferredLanguage: 'Java',
      targetRole: 'Software Engineer',
      defaultDifficulty: 'Medium',
    });
  },

  savePreferences(prefs: Partial<UserPreferences>): void {
    const current = this.getPreferences();
    safeSetItem(PREFERENCES_KEY, { ...current, ...prefs });
  },

  // Clear all local browser history
  clearLocalHistory(): void {
    if (!isStorageAvailable()) return;
    try {
      window.localStorage.removeItem(CODING_INTERVIEWS_KEY);
      window.localStorage.removeItem(MOCK_INTERVIEWS_KEY);
    } catch (e) {
      console.warn('Failed to clear local history', e);
    }
  },

  // Local Analytics Computations (Zero Database Dependency)
  calculateOverview(): AnalyticsOverview {
    const completed = this.getInterviews().filter(
      (i) => i.status?.toUpperCase() === 'COMPLETED' && typeof i.score === 'number'
    );

    if (completed.length === 0) {
      return {
        totalInterviews: 0,
        averageScore: 0.0,
        highestScore: 0,
        lowestScore: 0,
        completedInterviews: 0,
        strongestTopic: 'N/A',
        weakestTopic: 'N/A',
        recentAverageScore: 0.0,
      };
    }

    const scores = completed.map((i) => i.score as number);
    const totalInterviews = this.getInterviews().length;
    const completedInterviews = completed.length;
    const highestScore = Math.max(...scores);
    const lowestScore = Math.min(...scores);
    const averageScore = Math.round((scores.reduce((a, b) => a + b, 0) / completedInterviews) * 10) / 10;

    // Recent 5 average
    const recent5 = scores.slice(0, 5);
    const recentAverageScore =
      recent5.length > 0 ? Math.round((recent5.reduce((a, b) => a + b, 0) / recent5.length) * 10) / 10 : 0.0;

    // Strongest & Weakest topics
    const topicMap = new Map<string, number[]>();
    for (const interview of completed) {
      const topic = interview.topic || 'General';
      if (!topicMap.has(topic)) topicMap.set(topic, []);
      topicMap.get(topic)!.push(interview.score as number);
    }

    let strongestTopic = 'N/A';
    let weakestTopic = 'N/A';
    let bestAvg = -1;
    let worstAvg = 101;

    topicMap.forEach((scoresList, topic) => {
      const avg = scoresList.reduce((a, b) => a + b, 0) / scoresList.length;
      if (avg > bestAvg) {
        bestAvg = avg;
        strongestTopic = topic;
      }
      if (avg < worstAvg) {
        worstAvg = avg;
        weakestTopic = topic;
      }
    });

    return {
      totalInterviews,
      averageScore,
      highestScore,
      lowestScore,
      completedInterviews,
      strongestTopic,
      weakestTopic,
      recentAverageScore,
    };
  },

  calculateTopicAnalytics(): TopicAnalytics[] {
    const completed = this.getInterviews().filter(
      (i) => i.status?.toUpperCase() === 'COMPLETED' && typeof i.score === 'number'
    );

    const topicMap = new Map<string, number[]>();
    for (const interview of completed) {
      const topic = interview.topic || 'General';
      if (!topicMap.has(topic)) topicMap.set(topic, []);
      topicMap.get(topic)!.push(interview.score as number);
    }

    const results: TopicAnalytics[] = [];
    topicMap.forEach((scores, topic) => {
      const attempts = scores.length;
      const averageScore = Math.round((scores.reduce((a, b) => a + b, 0) / attempts) * 10) / 10;
      const bestScore = Math.max(...scores);
      results.push({ topic, attempts, averageScore, bestScore });
    });

    return results.sort((a, b) => b.averageScore - a.averageScore);
  },

  calculateDifficultyAnalytics(): DifficultyAnalytics[] {
    const completed = this.getInterviews().filter(
      (i) => i.status?.toUpperCase() === 'COMPLETED' && typeof i.score === 'number'
    );

    const difficulties = ['EASY', 'MEDIUM', 'HARD'];
    return difficulties.map((diff) => {
      const matching = completed.filter((i) => i.difficulty?.toUpperCase() === diff);
      const attempts = matching.length;
      if (attempts === 0) {
        return { difficulty: diff, attempts: 0, averageScore: 0.0, bestScore: 0 };
      }
      const scores = matching.map((i) => i.score as number);
      const averageScore = Math.round((scores.reduce((a, b) => a + b, 0) / attempts) * 10) / 10;
      const bestScore = Math.max(...scores);
      return { difficulty: diff, attempts, averageScore, bestScore };
    });
  },

  calculateLanguageAnalytics(): LanguageAnalytics[] {
    const completed = this.getInterviews().filter(
      (i) => i.status?.toUpperCase() === 'COMPLETED' && typeof i.score === 'number'
    );

    const langMap = new Map<string, number[]>();
    for (const interview of completed) {
      const lang = interview.language ? interview.language.toUpperCase() : 'OTHER';
      if (!langMap.has(lang)) langMap.set(lang, []);
      langMap.get(lang)!.push(interview.score as number);
    }

    const results: LanguageAnalytics[] = [];
    langMap.forEach((scores, language) => {
      const attempts = scores.length;
      const averageScore = Math.round((scores.reduce((a, b) => a + b, 0) / attempts) * 10) / 10;
      results.push({ language, attempts, averageScore });
    });

    return results.sort((a, b) => b.attempts - a.attempts);
  },

  calculatePerformanceTrend(): PerformanceTrend[] {
    const completed = this.getInterviews().filter(
      (i) => i.status?.toUpperCase() === 'COMPLETED' && typeof i.score === 'number'
    );

    return completed
      .map((i) => {
        const rawDate = i.completedAt || i.startedAt || i.createdAt || new Date().toISOString();
        const dateStr = rawDate.split('T')[0];
        return {
          date: dateStr,
          score: i.score as number,
        };
      })
      .reverse(); // Chronological (oldest to newest)
  },
};
