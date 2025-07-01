// 学生平台相关接口定义

export interface Task {
  id: string;
  title: string;
  type: string; // 章节作业、试卷答题、课程视频观看、阅读教材/PPT、报告类文档上传等
  deadline: string;
  description: string;
  status: 'pending' | 'in_progress' | 'completed';
  progress: number;
  resources?: Resource[];
}

export interface Resource {
  id: string;
  name: string;
  type: 'ppt' | 'pdf' | 'video' | 'document' | 'image';
  url: string;
  size: string;
  uploadTime: string;
}

export interface KnowledgeNode {
  id: string;
  title: string;
  description: string;
  x: number;
  y: number;
  connections: string[]; // 连接的其他节点ID
  completed: boolean;
}

export interface WatchedSegment {
  start: number;
  end: number;
}

export interface VideoProgress {
  videoId: string;
  currentTime: number;
  totalTime: number;
  watchedSegments: WatchedSegment[];
  completionRate: number;
}

export interface QuizQuestion {
  id: string;
  type: 'choice' | 'multiple' | 'short_answer' | 'programming';
  question: string;
  options?: string[];
  correctAnswer: string | string[];
  difficulty: 'easy' | 'medium' | 'hard';
  knowledgePoints: string[];
}

export interface Quiz {
  id: string;
  title: string;
  questions: QuizQuestion[];
  timeLimit: number; // 分钟
  attempts: number;
  bestScore: number;
}