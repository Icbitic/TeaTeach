import { Task, Resource, Quiz, QuizQuestion, KnowledgeNode, VideoProgress, WatchedSegment } from './interfaces/StudentInterfaces';
import { TaskManagement } from './components/TaskManagement';
import { QuizManagement } from './components/QuizManagement';
import { KnowledgeGraph } from './components/KnowledgeGraph';
import { VideoLearning } from './components/VideoLearning';

interface LearningStats {
  completed: number;
  total: number;
  averageProgress?: number;
  progress?: number;
  averageScore?: number;
}

interface StudentStats {
  tasks: LearningStats;
  quizzes: LearningStats;
  knowledge: LearningStats;
  videos: LearningStats;
}

@Component
export struct StudentMain {
  @State currentTab: number = 0;
  @State tasks: Task[] = [];
  @State quizzes: Quiz[] = [];
  @State knowledgeNodes: KnowledgeNode[] = [];
  @State videoProgresses: VideoProgress[] = [];
  @State userName: string = '张同学';
  @State userAvatar: string = '👨‍🎓';

  private tabTitles: string[] = ['任务管理', '在线测验', '知识图谱', '视频学习'];
  private tabIcons: string[] = ['📝', '🧪', '🧠', '🎥'];

  aboutToAppear() {
    this.initializeData();
  }

  build() {
    Column() {
      this.buildHeader();
      this.buildTabNavigation();
      this.buildContent();
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#f8fafc');
  }

  @Builder
  buildHeader() {
    Row({ space: 16 }) {
      // 用户头像和信息
      Row({ space: 12 }) {
        Text(this.userAvatar)
          .fontSize(32)
          .width(48)
          .height(48)
          .textAlign(TextAlign.Center)
          .backgroundColor('#ffffff')
          .borderRadius(24)
          .shadow({ radius: 4, color: '#00000015', offsetX: 0, offsetY: 2 });

        Column({ space: 4 }) {
          Text(`你好，${this.userName}`)
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1f2937');

          Text('继续你的学习之旅')
            .fontSize(14)
            .fontColor('#6b7280');
        }
        .alignItems(HorizontalAlign.Start);
      }
      .layoutWeight(1);

      // 通知和设置
      Row({ space: 12 }) {
        Button('🔔')
          .backgroundColor('#ffffff')
          .fontColor('#6b7280')
          .borderRadius(12)
          .padding(12)
          .shadow({ radius: 2, color: '#00000010', offsetX: 0, offsetY: 1 })
          .onClick(() => {
            this.showNotifications();
          });

        Button('⚙️')
          .backgroundColor('#ffffff')
          .fontColor('#6b7280')
          .borderRadius(12)
          .padding(12)
          .shadow({ radius: 2, color: '#00000010', offsetX: 0, offsetY: 1 })
          .onClick(() => {
            this.showSettings();
          });
      }
    }
    .width('100%')
    .padding({ left: 20, right: 20, top: 16, bottom: 16 })
    .backgroundColor('#ffffff')
    .shadow({ radius: 4, color: '#00000008', offsetX: 0, offsetY: 2 });
  }

  @Builder
  buildTabNavigation() {
    Row() {
      ForEach(this.tabTitles, (title: string, index: number) => {
        Column({ space: 8 }) {
          Text(this.tabIcons[index])
            .fontSize(20)
            .fontColor(this.currentTab === index ? '#6366f1' : '#9ca3af');

          Text(title)
            .fontSize(14)
            .fontColor(this.currentTab === index ? '#6366f1' : '#9ca3af')
            .fontWeight(this.currentTab === index ? FontWeight.Bold : FontWeight.Normal);

          if (this.currentTab === index) {
            Rect()
              .width(24)
              .height(3)
              .fill('#6366f1')
              .borderRadius(2);
          }
        }
        .layoutWeight(1)
        .padding({ top: 16, bottom: 12 })
        .onClick(() => {
          this.currentTab = index;
        });
      });
    }
    .width('100%')
    .backgroundColor('#ffffff')
    .shadow({ radius: 2, color: '#00000005', offsetX: 0, offsetY: 1 });
  }

  @Builder
  buildContent() {
    Scroll() {
      Column() {
        if (this.currentTab === 0) {
          TaskManagement({ tasks: $tasks });
        } else if (this.currentTab === 1) {
          QuizManagement({ quizzes: $quizzes });
        } else if (this.currentTab === 2) {
          KnowledgeGraph({ knowledgeNodes: $knowledgeNodes });
        } else if (this.currentTab === 3) {
          VideoLearning({ videoProgresses: $videoProgresses });
        }
      }
      .width('100%')
      .height('100%');
    }
    .layoutWeight(1)
    .scrollBar(BarState.Auto)
    .backgroundColor('#f8fafc');
  }

  // 数据初始化
  initializeData() {
    this.initializeTasks();
    this.initializeQuizzes();
    this.initializeKnowledgeNodes();
    this.initializeVideoProgresses();
  }

  initializeTasks() {
    this.tasks = [
      {
        id: 'task1',
        title: 'HarmonyOS应用开发基础',
        type: '章节作业',
        deadline: '2024-01-15',
        description: '完成HarmonyOS应用开发基础章节的练习题，包括环境搭建、项目创建等内容。',
        status: 'in_progress',
        progress: 65,
        resources: [
          {
            id: 'res1',
            name: 'HarmonyOS开发指南.pdf',
            type: 'pdf',
            url: '/resources/harmony_guide.pdf',
            size: '5.2MB',
            uploadTime: '2024-01-10T10:00:00Z'
          }
        ]
      },
      {
        id: 'task2',
        title: 'ArkTS语法测验',
        type: '试卷答题',
        deadline: '2024-01-20',
        description: '完成ArkTS语法相关的在线测验，测试对TypeScript扩展语法的掌握程度。',
        status: 'pending',
        progress: 0
      },
      {
        id: 'task3',
        title: '组件开发实战视频',
        type: '课程视频观看',
        deadline: '2024-01-25',
        description: '观看组件开发实战系列视频，学习自定义组件的创建和使用方法。',
        status: 'in_progress',
        progress: 30
      },
      {
        id: 'task4',
        title: '应用架构设计文档',
        type: '阅读教材/PPT',
        deadline: '2024-01-30',
        description: '阅读应用架构设计相关的PPT和文档，理解MVVM架构模式。',
        status: 'pending',
        progress: 0
      },
      {
        id: 'task5',
        title: '项目实战报告',
        type: '报告类文档上传',
        deadline: '2024-02-05',
        description: '完成一个完整的HarmonyOS应用项目，并提交项目报告和源代码。',
        status: 'pending',
        progress: 0
      }
    ];
  }

  initializeQuizzes() {
    this.quizzes = [
      {
        id: 'quiz1',
        title: 'HarmonyOS基础测验',
        questions: [
          {
            id: 'q1',
            type: 'choice',
            question: 'HarmonyOS是由哪家公司开发的操作系统？',
            options: ['华为', '小米', '阿里巴巴', '腾讯'],
            correctAnswer: '华为',
            difficulty: 'easy',
            knowledgePoints: ['HarmonyOS基础']
          },
          {
            id: 'q2',
            type: 'multiple',
            question: 'HarmonyOS支持哪些设备类型？（多选）',
            options: ['手机', '平板', '智能手表', '智能电视', '车载设备'],
            correctAnswer: ['手机', '平板', '智能手表', '智能电视', '车载设备'],
            difficulty: 'medium',
            knowledgePoints: ['HarmonyOS特性']
          },
          {
            id: 'q3',
            type: 'choice',
            question: 'ArkTS是基于哪种编程语言扩展的？',
            options: ['JavaScript', 'TypeScript', 'Java', 'C++'],
            correctAnswer: 'TypeScript',
            difficulty: 'easy',
            knowledgePoints: ['ArkTS语法']
          }
        ],
        timeLimit: 30,
        attempts: 0,
        bestScore: 0
      },
      {
        id: 'quiz2',
        title: 'ArkTS语法测验',
        questions: [
          {
            id: 'q4',
            type: 'choice',
            question: '在ArkTS中，@State装饰器的作用是什么？',
            options: ['定义常量', '管理组件状态', '创建函数', '导入模块'],
            correctAnswer: '管理组件状态',
            difficulty: 'medium',
            knowledgePoints: ['状态管理']
          },
          {
            id: 'q5',
            type: 'multiple',
            question: 'ArkTS中常用的装饰器有哪些？（多选）',
            options: ['@State', '@Prop', '@Link', '@Watch', '@Component'],
            correctAnswer: ['@State', '@Prop', '@Link', '@Watch', '@Component'],
            difficulty: 'hard',
            knowledgePoints: ['装饰器']
          }
        ],
        timeLimit: 25,
        attempts: 0,
        bestScore: 0
      },
      {
        id: 'quiz3',
        title: '组件开发实战',
        questions: [
          {
            id: 'q6',
            type: 'choice',
            question: '创建自定义组件时，必须使用哪个装饰器？',
            options: ['@State', '@Component', '@Builder', '@Styles'],
            correctAnswer: '@Component',
            difficulty: 'easy',
            knowledgePoints: ['组件开发']
          },
          {
            id: 'q7',
            type: 'choice',
            question: '@Builder装饰器的主要用途是什么？',
            options: ['管理状态', '创建可复用UI', '定义样式', '处理事件'],
            correctAnswer: '创建可复用UI',
            difficulty: 'medium',
            knowledgePoints: ['UI构建']
          }
        ],
        timeLimit: 20,
        attempts: 0,
        bestScore: 0
      }
    ];
  }

  initializeKnowledgeNodes() {
    this.knowledgeNodes = [
      {
        id: 'node1',
        title: 'HarmonyOS基础',
        description: '了解HarmonyOS的基本概念、特性和应用场景',
        x: 50,
        y: 50,
        connections: ['node2', 'node3'],
        completed: true
      },
      {
        id: 'node2',
        title: 'ArkTS语法',
        description: '学习ArkTS语言的基本语法和特性',
        x: 200,
        y: 50,
        connections: ['node4', 'node5'],
        completed: true
      },
      {
        id: 'node3',
        title: '开发环境搭建',
        description: '配置HarmonyOS开发环境和工具',
        x: 50,
        y: 150,
        connections: ['node4'],
        completed: true
      },
      {
        id: 'node4',
        title: '组件开发',
        description: '学习创建和使用自定义组件',
        x: 200,
        y: 150,
        connections: ['node6', 'node7'],
        completed: false
      },
      {
        id: 'node5',
        title: '状态管理',
        description: '掌握应用状态管理的方法和最佳实践',
        x: 350,
        y: 50,
        connections: ['node6'],
        completed: false
      },
      {
        id: 'node6',
        title: '应用架构',
        description: '理解HarmonyOS应用的架构设计模式',
        x: 280,
        y: 250,
        connections: ['node8'],
        completed: false
      },
      {
        id: 'node7',
        title: '界面布局',
        description: '学习各种布局组件的使用方法',
        x: 120,
        y: 250,
        connections: ['node8'],
        completed: false
      },
      {
        id: 'node8',
        title: '项目实战',
        description: '完成一个完整的HarmonyOS应用项目',
        x: 200,
        y: 350,
        connections: [],
        completed: false
      }
    ];
  }

  initializeVideoProgresses() {
    this.videoProgresses = [
      {
        videoId: 'video1',
        currentTime: 900, // 15分钟
        totalTime: 1800, // 30分钟
        watchedSegments: [
          { start: 0, end: 600 },
          { start: 700, end: 900 }
        ],
        completionRate: 50
      },
      {
        videoId: 'video2',
        currentTime: 0,
        totalTime: 2400, // 40分钟
        watchedSegments: [],
        completionRate: 0
      },
      {
        videoId: 'video3',
        currentTime: 600, // 10分钟
        totalTime: 3000, // 50分钟
        watchedSegments: [
          { start: 0, end: 600 }
        ],
        completionRate: 20
      }
    ];
  }

  // 功能方法
  showNotifications() {
    console.log('显示通知');
    // 实现通知功能
  }

  showSettings() {
    console.log('显示设置');
    // 实现设置功能
  }

  // 获取学习统计数据
  getLearningStats(): StudentStats {
    const completedTasks = this.tasks.filter(task => task.status === 'completed').length;
    const totalTasks = this.tasks.length;
    const averageProgress = this.tasks.reduce((sum, task) => sum + task.progress, 0) / totalTasks;

    const completedQuizzes = this.quizzes.filter(quiz => quiz.bestScore > 0).length;
    const averageScore = this.quizzes.reduce((sum, quiz) => sum + quiz.bestScore, 0) / this.quizzes.length;

    const completedNodes = this.knowledgeNodes.filter(node => node.completed).length;
    const knowledgeProgress = (completedNodes / this.knowledgeNodes.length) * 100;

    const completedVideos = this.videoProgresses.filter(progress => progress.completionRate >= 100).length;
    const averageVideoProgress = this.videoProgresses.reduce((sum, progress) => sum + progress.completionRate, 0) / this.videoProgresses.length;

    return {
      tasks: { completed: completedTasks, total: totalTasks, averageProgress: averageProgress },
      quizzes: { completed: completedQuizzes, total: this.quizzes.length, averageScore: averageScore },
      knowledge: { completed: completedNodes, total: this.knowledgeNodes.length, progress: knowledgeProgress },
      videos: { completed: completedVideos, total: this.videoProgresses.length, averageProgress: averageVideoProgress }
    } as StudentStats;
  }
}