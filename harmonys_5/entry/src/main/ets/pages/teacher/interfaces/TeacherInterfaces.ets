// 教师平台相关接口定义

export interface Course {
  id: string;
  name: string;
  teacher: string;
  credit: number;
  category: string;
  description: string;
  createTime: string;
  studentCount: number;
  status: string;
}

export interface Student {
  id: string;
  name: string;
  studentId: string;
  email: string;
  phone: string;
  enrolledCourses: string[];
  totalScore: number;
  completionRate: number;
  lastActive: string;
}

export interface Assignment {
  id: string;
  title: string;
  courseId: string;
  type: string;
  description: string;
  deadline: string;
  totalPoints: number;
  submissionCount: number;
  averageScore: number;
  status: string;
}

export interface QuestionBank {
  id: string;
  subject: string;
  difficulty: string;
  type: string;
  question: string;
  options: string[];
  correctAnswer: string | string[];
  knowledgePoints: string[];
  usageCount: number;
  successRate: number;
}

export interface ActivityLog {
  id: string;
  type: string;
  studentName: string;
  description: string;
  timestamp: string;
}

export interface Analytics {
  totalStudents: number;
  activeCourses: number;
  pendingGrading: number;
  averageCompletion: number;
  recentActivity: ActivityLog[];
}

export interface AssignmentType {
  value: string;
  label: string;
}

// AI助手相关接口 - 已移除
/*
export interface AIFunction {
  id: string;
  title: string;
  description: string;
  icon: string;
  category: 'recommend' | 'delegate' | 'design' | 'analysis';
  enabled: boolean;
}

export interface AIRecommendation {
  id: string;
  type: 'course' | 'assignment' | 'question' | 'layout';
  title: string;
  description: string;
  confidence: number;
  context: string;
  action: string;
}

export interface AITask {
  id: string;
  title: string;
  description: string;
  type: 'generate' | 'optimize' | 'analyze' | 'design';
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  result?: string | object;
  createdTime: string;
  completedTime?: string;
}

export interface AIDesignSuggestion {
  id: string;
  category: 'layout' | 'color' | 'typography' | 'component';
  title: string;
  description: string;
  preview: string;
  applicable: boolean;
}

export interface AIAssistantState {
  isVisible: boolean;
  currentMode: 'floating' | 'panel' | 'fullscreen';
  activeFunction: string | null;
  recommendations: AIRecommendation[];
  tasks: AITask[];
  designSuggestions: AIDesignSuggestion[];
}
*/