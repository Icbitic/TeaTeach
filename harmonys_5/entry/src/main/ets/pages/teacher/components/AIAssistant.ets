import { AIFunction, AIRecommendation, AITask, AIDesignSuggestion, AIAssistantState } from '../interfaces/TeacherInterfaces';

@Component
export struct AIAssistant {
  @State assistantState: AIAssistantState = {
    isVisible: false,
    currentMode: 'floating',
    activeFunction: null,
    recommendations: [],
    tasks: [],
    designSuggestions: []
  };

  @State aiFunctions: AIFunction[] = [
    {
      id: 'recommend',
      title: '智能推荐',
      description: '基于当前操作上下文，提供功能建议和内容补全',
      icon: '🎯',
      category: 'recommend',
      enabled: true
    },
    {
      id: 'delegate',
      title: 'AI代办',
      description: '自动生成作业题目、课程描述、页面布局等内容',
      icon: '🤖',
      category: 'delegate',
      enabled: true
    },
    {
      id: 'design',
      title: '样式设计',
      description: '指导页面风格、配色方案和排版结构设计',
      icon: '🎨',
      category: 'design',
      enabled: true
    },
    {
      id: 'analysis',
      title: '智能分析',
      description: '分析教学数据，提供优化建议和趋势预测',
      icon: '📊',
      category: 'analysis',
      enabled: true
    }
  ];

  @State mockRecommendations: AIRecommendation[] = [
    {
      id: '1',
      type: 'course',
      title: '推荐课程结构',
      description: '基于您的教学内容，建议采用模块化课程结构',
      confidence: 0.92,
      context: '当前正在创建新课程',
      action: '应用推荐结构'
    },
    {
      id: '2',
      type: 'assignment',
      title: '作业类型建议',
      description: '建议添加实践性作业以提高学生参与度',
      confidence: 0.85,
      context: '作业管理页面',
      action: '查看详情'
    }
  ];

  @State mockTasks: AITask[] = [
    {
      id: '1',
      title: '生成数学练习题',
      description: '为高等数学课程生成20道练习题',
      type: 'generate',
      status: 'completed',
      progress: 100,
      createdTime: '2024-01-15 10:30',
      completedTime: '2024-01-15 10:32'
    },
    {
      id: '2',
      title: '优化课程页面布局',
      description: '根据用户体验原则优化课程详情页面',
      type: 'design',
      status: 'processing',
      progress: 65,
      createdTime: '2024-01-15 11:00'
    }
  ];

  build() {
    Stack() {
      // 浮动AI图标
      if (!this.assistantState.isVisible) {
        this.buildFloatingIcon();
      }

      // AI助手面板
      if (this.assistantState.isVisible) {
        this.buildAssistantPanel();
      }
    }
    .width('100%')
    .height('100%');
  }

  @Builder
  buildFloatingIcon() {
    Button() {
      Row({ space: 8 }) {
        Text('🤖')
          .fontSize(20);
        Text('AI助手')
          .fontSize(14)
          .fontColor(Color.White)
          .fontWeight(FontWeight.Medium);
      }
    }
    .backgroundColor('#6366f1')
    .borderRadius(25)
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .shadow({ radius: 8, color: '#6366f140', offsetX: 0, offsetY: 4 })
    .position({ x: '85%', y: '85%' })
    .zIndex(1000)
    .onClick(() => {
      this.assistantState.isVisible = true;
      this.assistantState.currentMode = 'panel';
      this.loadRecommendations();
    })
    .gesture(
      PanGesture()
        .onActionUpdate((event: GestureEvent) => {
          // 实现拖拽功能
        })
    );
  }

  @Builder
  buildAssistantPanel() {
    Stack() {
      // 背景遮罩
      Column()
        .width('100%')
        .height('100%')
        .backgroundColor('#00000040')
        .onClick(() => {
          this.assistantState.isVisible = false;
          this.assistantState.activeFunction = null;
        });

      // 主面板
      Column({ space: 0 }) {
        // 头部
        this.buildPanelHeader();

        // 内容区域
        if (this.assistantState.activeFunction) {
          this.buildFunctionContent();
        } else {
          this.buildMainContent();
        }
      }
      .width('90%')
      .constraintSize({ maxWidth: 400 })
      .backgroundColor('#ffffff')
      .borderRadius(16)
      .shadow({ radius: 20, color: '#00000020', offsetX: 0, offsetY: 10 })
      .position({ x: '50%', y: '50%' })
      .translate({ x: '-50%', y: '-50%' });
    }
    .width('100%')
    .height('100%')
    .zIndex(2000);
  }

  @Builder
  buildPanelHeader() {
    Row({ space: 16 }) {
      if (this.assistantState.activeFunction) {
        Button('←')
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding(8)
          .onClick(() => {
            this.assistantState.activeFunction = null;
          });
      }

      Text(this.assistantState.activeFunction ?
      this.getFunctionTitle(this.assistantState.activeFunction) : '🤖 AI智能助手')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1f2937')
        .layoutWeight(1);

      Button('✕')
        .backgroundColor('#f3f4f6')
        .fontColor('#6b7280')
        .borderRadius(8)
        .padding(8)
        .onClick(() => {
          this.assistantState.isVisible = false;
          this.assistantState.activeFunction = null;
        });
    }
    .width('100%')
    .padding(20)
    .borderRadius({ topLeft: 16, topRight: 16 })
    .backgroundColor('#ffffff');
  }

  @Builder
  buildMainContent() {
    Column({ space: 20 }) {
      // 功能入口
      Text('🚀 AI赋能功能')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#374151')
        .alignSelf(ItemAlign.Start);

      Grid() {
        ForEach(this.aiFunctions, (func: AIFunction) => {
          GridItem() {
            this.buildFunctionCard(func);
          }
        });
      }
      .columnsTemplate('1fr 1fr')
      .rowsGap(12)
      .columnsGap(12)
      .height(200);

      // 快速推荐
      if (this.mockRecommendations.length > 0) {
        Column({ space: 12 }) {
          Text('💡 智能推荐')
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#374151')
            .alignSelf(ItemAlign.Start);

          ForEach(this.mockRecommendations.slice(0, 2), (rec: AIRecommendation) => {
            this.buildRecommendationItem(rec);
          });
        }
        .alignItems(HorizontalAlign.Start);
      }

      // 最近任务
      if (this.mockTasks.length > 0) {
        Column({ space: 12 }) {
          Row({ space: 8 }) {
            Text('📋 最近任务')
              .fontSize(16)
              .fontWeight(FontWeight.Medium)
              .fontColor('#374151')
              .layoutWeight(1);

            Text('查看全部')
              .fontSize(14)
              .fontColor('#6366f1')
              .onClick(() => {
                this.assistantState.activeFunction = 'delegate';
              });
          }
          .width('100%');

          ForEach(this.mockTasks.slice(0, 2), (task: AITask) => {
            this.buildTaskItem(task);
          });
        }
        .alignItems(HorizontalAlign.Start);
      }
    }
    .width('100%')
    .padding(20)
    .alignItems(HorizontalAlign.Start);
  }

  @Builder
  buildFunctionCard(func: AIFunction) {
    Column({ space: 8 }) {
      Text(func.icon)
        .fontSize(24);

      Text(func.title)
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#1f2937')
        .textAlign(TextAlign.Center);

      Text(func.description)
        .fontSize(12)
        .fontColor('#6b7280')
        .textAlign(TextAlign.Center)
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis });
    }
    .width('100%')
    .padding(16)
    .backgroundColor(func.enabled ? '#ffffff' : '#f9fafb')
    .borderRadius(12)
    .border({ width: 1, color: func.enabled ? '#e5e7eb' : '#f3f4f6' })
    .shadow({ radius: 2, color: '#00000010', offsetX: 0, offsetY: 1 })
    .onClick(() => {
      if (func.enabled) {
        this.assistantState.activeFunction = func.id;
      }
    });
  }

  @Builder
  buildFunctionContent() {
    Scroll() {
      Column({ space: 16 }) {
        if (this.assistantState.activeFunction === 'recommend') {
          this.buildRecommendationContent();
        } else if (this.assistantState.activeFunction === 'delegate') {
          this.buildDelegateContent();
        } else if (this.assistantState.activeFunction === 'design') {
          this.buildDesignContent();
        } else if (this.assistantState.activeFunction === 'analysis') {
          this.buildAnalysisContent();
        }
      }
      .width('100%')
      .padding(20);
    }
    .scrollable(ScrollDirection.Vertical)
    .scrollBar(BarState.Auto)
    .layoutWeight(1);
  }

  @Builder
  buildRecommendationContent() {
    Column({ space: 16 }) {
      Text('基于您当前的操作上下文，AI为您推荐以下功能和内容：')
        .fontSize(14)
        .fontColor('#6b7280')
        .lineHeight(20);

      ForEach(this.mockRecommendations, (rec: AIRecommendation) => {
        this.buildRecommendationItem(rec);
      });

      Button('刷新推荐')
        .backgroundColor('#6366f1')
        .fontColor(Color.White)
        .borderRadius(8)
        .padding({ left: 24, right: 24, top: 12, bottom: 12 })
        .onClick(() => {
          this.loadRecommendations();
        });
    }
    .alignItems(HorizontalAlign.Start);
  }

  @Builder
  buildDelegateContent() {
    Column({ space: 16 }) {
      Text('AI可以帮您自动完成以下任务，节省您的时间：')
        .fontSize(14)
        .fontColor('#6b7280')
        .lineHeight(20);

      // 快速任务按钮
      Grid() {
        GridItem() {
          this.buildQuickTaskButton('生成作业题目', '📝', () => {
            this.createTask('生成作业题目', '为当前课程生成练习题', 'generate');
          });
        }
        GridItem() {
          this.buildQuickTaskButton('创建课程描述', '📚', () => {
            this.createTask('创建课程描述', '生成课程介绍和大纲', 'generate');
          });
        }
        GridItem() {
          this.buildQuickTaskButton('设计页面布局', '🎨', () => {
            this.createTask('设计页面布局', '优化当前页面的布局结构', 'design');
          });
        }
        GridItem() {
          this.buildQuickTaskButton('分析学习数据', '📊', () => {
            this.createTask('分析学习数据', '生成学习效果分析报告', 'analyze');
          });
        }
      }
      .columnsTemplate('1fr 1fr')
      .rowsGap(12)
      .columnsGap(12)
      .height(120);

      Text('任务列表')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#374151');

      ForEach(this.mockTasks, (task: AITask) => {
        this.buildTaskItem(task);
      });
    }
    .alignItems(HorizontalAlign.Start);
  }

  @Builder
  buildDesignContent() {
    Column({ space: 16 }) {
      Text('AI设计助手可以帮您优化界面设计和用户体验：')
        .fontSize(14)
        .fontColor('#6b7280')
        .lineHeight(20);

      // 设计类别
      Row({ space: 12 }) {
        Button('布局优化')
          .backgroundColor('#dbeafe')
          .fontColor('#2563eb')
          .borderRadius(20)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .fontSize(12);

        Button('配色方案')
          .backgroundColor('#dcfce7')
          .fontColor('#16a34a')
          .borderRadius(20)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .fontSize(12);

        Button('组件样式')
          .backgroundColor('#fef3c7')
          .fontColor('#d97706')
          .borderRadius(20)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .fontSize(12);
      }

      // 设计建议
      Column({ space: 12 }) {
        this.buildDesignSuggestionItem({
          id: '1',
          category: 'layout',
          title: '卡片式布局优化',
          description: '建议采用更现代的卡片式布局，提升视觉层次',
          preview: '预览效果',
          applicable: true
        });

        this.buildDesignSuggestionItem({
          id: '2',
          category: 'color',
          title: '配色方案调整',
          description: '建议使用更柔和的配色，提升用户体验',
          preview: '预览效果',
          applicable: true
        });
      }
    }
    .alignItems(HorizontalAlign.Start);
  }

  @Builder
  buildAnalysisContent() {
    Column({ space: 16 }) {
      Text('AI智能分析为您提供数据洞察和优化建议：')
        .fontSize(14)
        .fontColor('#6b7280')
        .lineHeight(20);

      // 分析类型
      Grid() {
        GridItem() {
          this.buildAnalysisCard('学习效果分析', '📈', '分析学生学习数据和进度');
        }
        GridItem() {
          this.buildAnalysisCard('课程优化建议', '🎯', '基于数据提供课程改进建议');
        }
        GridItem() {
          this.buildAnalysisCard('趋势预测', '🔮', '预测学习趋势和潜在问题');
        }
        GridItem() {
          this.buildAnalysisCard('个性化推荐', '👤', '为每个学生提供个性化建议');
        }
      }
      .columnsTemplate('1fr 1fr')
      .rowsGap(12)
      .columnsGap(12)
      .height(160);

      Button('开始分析')
        .backgroundColor('#6366f1')
        .fontColor(Color.White)
        .borderRadius(8)
        .padding({ left: 24, right: 24, top: 12, bottom: 12 })
        .onClick(() => {
          this.startAnalysis();
        });
    }
    .alignItems(HorizontalAlign.Start);
  }

  @Builder
  buildRecommendationItem(rec: AIRecommendation) {
    Row({ space: 12 }) {
      Column({ space: 4 }) {
        Text(rec.title)
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .fontColor('#1f2937');

        Text(rec.description)
          .fontSize(12)
          .fontColor('#6b7280')
          .maxLines(2)
          .textOverflow({ overflow: TextOverflow.Ellipsis });

        Row({ space: 8 }) {
          Text(`置信度: ${Math.round(rec.confidence * 100)}%`)
            .fontSize(11)
            .fontColor('#9ca3af');
          Text(rec.context)
            .fontSize(11)
            .fontColor('#9ca3af');
        }
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1);

      Button(rec.action)
        .backgroundColor('#dbeafe')
        .fontColor('#2563eb')
        .borderRadius(6)
        .padding({ left: 12, right: 12, top: 6, bottom: 6 })
        .fontSize(12)
        .onClick(() => {
          this.applyRecommendation(rec);
        });
    }
    .width('100%')
    .padding(12)
    .backgroundColor('#ffffff')
    .borderRadius(8)
    .border({ width: 1, color: '#e5e7eb' });
  }

  @Builder
  buildTaskItem(task: AITask) {
    Column({ space: 8 }) {
      Row({ space: 12 }) {
        Text(this.getTaskIcon(task.type))
          .fontSize(16);

        Column({ space: 2 }) {
          Text(task.title)
            .fontSize(14)
            .fontWeight(FontWeight.Medium)
            .fontColor('#1f2937');

          Text(task.description)
            .fontSize(12)
            .fontColor('#6b7280')
            .maxLines(1)
            .textOverflow({ overflow: TextOverflow.Ellipsis });
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Text(this.getStatusText(task.status))
          .fontSize(12)
          .fontColor(this.getStatusColor(task.status))
          .backgroundColor(this.getStatusBgColor(task.status))
          .borderRadius(4)
          .padding({ left: 8, right: 8, top: 4, bottom: 4 });
      }

      if (task.status === 'processing') {
        Progress({ value: task.progress, total: 100, type: ProgressType.Linear })
          .width('100%')
          .color('#6366f1')
          .backgroundColor('#e5e7eb');
      }
    }
    .width('100%')
    .padding(12)
    .backgroundColor('#ffffff')
    .borderRadius(8)
    .border({ width: 1, color: '#e5e7eb' })
    .onClick(() => {
      this.viewTaskDetail(task);
    });
  }

  @Builder
  buildQuickTaskButton(title: string, icon: string, action: () => void) {
    Column({ space: 4 }) {
      Text(icon)
        .fontSize(20);
      Text(title)
        .fontSize(12)
        .fontColor('#374151')
        .textAlign(TextAlign.Center)
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis });
    }
    .width('100%')
    .padding(12)
    .backgroundColor('#ffffff')
    .borderRadius(8)
    .border({ width: 1, color: '#e5e7eb' })
    .onClick(action);
  }

  @Builder
  buildDesignSuggestionItem(suggestion: AIDesignSuggestion) {
    Row({ space: 12 }) {
      Column({ space: 4 }) {
        Text(suggestion.title)
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .fontColor('#1f2937');

        Text(suggestion.description)
          .fontSize(12)
          .fontColor('#6b7280')
          .maxLines(2)
          .textOverflow({ overflow: TextOverflow.Ellipsis });
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1);

      Button('应用')
        .backgroundColor(suggestion.applicable ? '#dcfce7' : '#f3f4f6')
        .fontColor(suggestion.applicable ? '#16a34a' : '#9ca3af')
        .borderRadius(6)
        .padding({ left: 12, right: 12, top: 6, bottom: 6 })
        .fontSize(12)
        .enabled(suggestion.applicable)
        .onClick(() => {
          this.applyDesignSuggestion(suggestion);
        });
    }
    .width('100%')
    .padding(12)
    .backgroundColor('#ffffff')
    .borderRadius(8)
    .border({ width: 1, color: '#e5e7eb' });
  }

  @Builder
  buildAnalysisCard(title: string, icon: string, description: string) {
    Column({ space: 8 }) {
      Text(icon)
        .fontSize(20);
      Text(title)
        .fontSize(12)
        .fontWeight(FontWeight.Medium)
        .fontColor('#1f2937')
        .textAlign(TextAlign.Center);
      Text(description)
        .fontSize(10)
        .fontColor('#6b7280')
        .textAlign(TextAlign.Center)
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis });
    }
    .width('100%')
    .padding(12)
    .backgroundColor('#ffffff')
    .borderRadius(8)
    .border({ width: 1, color: '#e5e7eb' })
    .onClick(() => {
      this.startSpecificAnalysis(title);
    });
  }

  // 辅助方法
  getFunctionTitle(functionId: string): string {
    const func = this.aiFunctions.find(f => f.id === functionId);
    return func ? `${func.icon} ${func.title}` : 'AI功能';
  }

  getTaskIcon(type: string): string {
    switch (type) {
      case 'generate': return '📝';
      case 'optimize': return '⚡';
      case 'analyze': return '📊';
      case 'design': return '🎨';
      default: return '🤖';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'pending': return '等待中';
      case 'processing': return '处理中';
      case 'completed': return '已完成';
      case 'failed': return '失败';
      default: return '未知';
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'pending': return '#f59e0b';
      case 'processing': return '#3b82f6';
      case 'completed': return '#10b981';
      case 'failed': return '#ef4444';
      default: return '#6b7280';
    }
  }

  getStatusBgColor(status: string): string {
    switch (status) {
      case 'pending': return '#fef3c7';
      case 'processing': return '#dbeafe';
      case 'completed': return '#dcfce7';
      case 'failed': return '#fee2e2';
      default: return '#f3f4f6';
    }
  }

  // 业务方法
  loadRecommendations() {
    // 模拟加载推荐内容
    this.assistantState.recommendations = this.mockRecommendations;
  }

  applyRecommendation(rec: AIRecommendation) {
    // 应用推荐
    console.log('应用推荐:', rec.title);
  }

  createTask(title: string, description: string, type: string) {
    const newTask: AITask = {
      id: Date.now().toString(),
      title,
      description,
      type: type as 'generate' | 'optimize' | 'analyze' | 'design',
      status: 'pending',
      progress: 0,
      createdTime: new Date().toLocaleString()
    };
    this.mockTasks.unshift(newTask);

    // 模拟任务处理
    setTimeout(() => {
      newTask.status = 'processing';
      this.simulateTaskProgress(newTask);
    }, 1000);
  }

  simulateTaskProgress(task: AITask) {
    const interval = setInterval(() => {
      task.progress += Math.random() * 20;
      if (task.progress >= 100) {
        task.progress = 100;
        task.status = 'completed';
        task.completedTime = new Date().toLocaleString();
        clearInterval(interval);
      }
    }, 500);
  }

  viewTaskDetail(task: AITask) {
    // 查看任务详情
    console.log('查看任务详情:', task.title);
  }

  applyDesignSuggestion(suggestion: AIDesignSuggestion) {
    // 应用设计建议
    console.log('应用设计建议:', suggestion.title);
  }

  startAnalysis() {
    // 开始分析
    this.createTask('智能数据分析', '分析当前教学数据并生成报告', 'analyze');
  }

  startSpecificAnalysis(type: string) {
    // 开始特定分析
    this.createTask(type, `执行${type}任务`, 'analyze');
  }
}