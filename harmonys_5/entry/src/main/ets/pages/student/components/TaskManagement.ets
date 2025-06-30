import { Task, Resource } from '../interfaces/StudentInterfaces';

interface TaskTypeStat {
  type: string;
  count: number;
  progress: number;
}

interface TaskTypeData {
  count: number;
  totalProgress: number;
}

@Component
export struct TaskManagement {
  @Link tasks: Task[];
  @State searchText: string = '';
  @State selectedTaskType: string = '全部';
  @State showUploadDialog: boolean = false;
  @State uploadFile: Resource | null = null;
  @State showStatsDialog: boolean = false;

  private taskTypes: string[] = ['全部', '章节作业', '试卷答题', '课程视频观看', '阅读教材/PPT', '报告类文档上传'];

  build() {
    Column({ space: 20 }) {
      // 任务管理标题
      Row({ space: 16 }) {
        Text('📝 我的任务')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .layoutWeight(1);

        Button('📊 学习统计')
          .backgroundColor('#10b981')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .onClick(() => {
            this.showLearningStats();
          });
      }

      // 搜索和筛选
      Row({ space: 16 }) {
        TextInput({ placeholder: '🔍 搜索任务...', text: this.searchText })
          .onChange(v => this.searchText = v)
          .layoutWeight(1)
          .backgroundColor('#ffffff')
          .borderRadius(12)
          .padding({ left: 18, right: 18, top: 14, bottom: 14 })
          .fontSize(16);

        Button('筛选')
          .backgroundColor('#f1f5f9')
          .fontColor('#64748b')
          .borderRadius(12)
          .padding({ left: 18, right: 18, top: 14, bottom: 14 })
          .fontSize(16);
      }

      // 任务类型筛选
      Scroll() {
        Row({ space: 12 }) {
          ForEach(this.taskTypes, (type: string) => {
            Button(type)
              .backgroundColor(this.selectedTaskType === type ? '#6366f1' : '#ffffff')
              .fontColor(this.selectedTaskType === type ? Color.White : '#6b7280')
              .borderRadius(24)
              .padding({ left: 16, right: 16, top: 8, bottom: 8 })
              .fontSize(15)
              .border({ width: 1, color: this.selectedTaskType === type ? '#6366f1' : '#e5e7eb' })
              .onClick(() => {
                this.selectedTaskType = type;
              });
          });
        }
        .padding({ left: 4, right: 4 });
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off);

      // 任务列表
      Scroll() {
        Column({ space: 16 }) {
          ForEach(this.getFilteredTasks(), (task: Task) => {
            this.buildTaskCard(task);
          });
        }
      }
      .scrollable(ScrollDirection.Vertical)
      .scrollBar(BarState.Auto)
      .layoutWeight(1)
      .width('100%')

      // 上传对话框
      if (this.showUploadDialog) {
        this.buildUploadDialog();
      }

      // 学习统计对话框
      if (this.showStatsDialog) {
        this.buildStatsDialog();
      }
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildTaskCard(task: Task) {
    Column({ space: 16 }) {
      Row({ space: 16 }) {
        Column({ space: 8 }) {
          Text(task.title)
            .fontSize(19)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1f2937')
            .lineHeight(26);

          Text(task.type)
            .fontSize(14)
            .fontColor('#6366f1')
            .backgroundColor('#eef2ff')
            .padding({ left: 12, right: 12, top: 6, bottom: 6 })
            .borderRadius(8)
            .border({ width: 1, color: '#ddd6fe' });
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text(`${task.progress}%`)
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor(task.progress === 100 ? '#10b981' : '#f59e0b')
            .textAlign(TextAlign.Center);

          Text('完成度')
            .fontSize(12)
            .fontColor('#9ca3af')
            .textAlign(TextAlign.Center);
        }
        .alignItems(HorizontalAlign.Center);
      }

      Text(task.description)
        .fontSize(15)
        .fontColor('#6b7280')
        .lineHeight(22)
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis });

      // 进度条
      Column({ space: 8 }) {
        Row() {
          Text('学习进度')
            .fontSize(13)
            .fontColor('#9ca3af');

          Blank();

          Text(`${task.progress}/100`)
            .fontSize(13)
            .fontColor('#9ca3af');
        }

        Progress({ value: task.progress, total: 100, type: ProgressType.Linear })
          .color('#6366f1')
          .backgroundColor('#e5e7eb')
          .width('100%')
          .height(6);
      }

      Row({ space: 16 }) {
        Text(`📅 截止：${task.deadline}`)
          .fontSize(14)
          .fontColor('#6b7280')
          .padding({ left: 8, right: 8, top: 4, bottom: 4 })
          .backgroundColor('#f9fafb')
          .borderRadius(6);

        Blank();

        if (task.status === 'pending') {
          Button('开始学习')
            .backgroundColor('#10b981')
            .fontColor(Color.White)
            .borderRadius(10)
            .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            .fontSize(15)
            .onClick(() => {
              this.startTask(task);
            });
        } else if (task.status === 'in_progress') {
          Button('继续学习')
            .backgroundColor('#f59e0b')
            .fontColor(Color.White)
            .borderRadius(10)
            .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            .fontSize(15)
            .onClick(() => {
              this.continueTask(task);
            });
        } else {
          Button('已完成')
            .backgroundColor('#6b7280')
            .fontColor(Color.White)
            .borderRadius(10)
            .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            .fontSize(15)
            .enabled(false);
        }
      }
    }
    .width('100%')
    .padding(20)
    .backgroundColor('#ffffff')
    .borderRadius(16)
    .shadow({ radius: 6, color: '#00000012', offsetX: 0, offsetY: 3 })
    .border({ width: 1, color: '#f3f4f6' });
  }

  @Builder
  buildUploadDialog() {
    Column({ space: 16 }) {
      Text('上传文件')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1f2937');

      Column({ space: 12 }) {
        Text('📁 选择文件')
          .fontSize(16)
          .fontColor('#374151')
          .width('100%')
          .textAlign(TextAlign.Start);

        Button('点击选择文件')
          .backgroundColor('#f8fafc')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 12, bottom: 12 })
          .width('100%')
          .border({ width: 2, color: '#e5e7eb', style: BorderStyle.Dashed })
          .onClick(() => {
            this.selectFile();
          });

        if (this.uploadFile) {
          Row({ space: 12 }) {
            Text('📄')
              .fontSize(20);

            Column({ space: 4 }) {
              Text(this.uploadFile.name)
                .fontSize(14)
                .fontColor('#374151')
                .fontWeight(FontWeight.Medium);

              Text(this.uploadFile.size)
                .fontSize(12)
                .fontColor('#9ca3af');
            }
            .alignItems(HorizontalAlign.Start)
            .layoutWeight(1);

            Button('❌')
              .backgroundColor('transparent')
              .fontColor('#ef4444')
              .padding(4)
              .onClick(() => {
                this.uploadFile = null;
              });
          }
          .width('100%')
          .padding(12)
          .backgroundColor('#f8fafc')
          .borderRadius(8);
        }
      }

      Row({ space: 12 }) {
        Button('取消')
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .layoutWeight(1)
          .onClick(() => {
            this.showUploadDialog = false;
            this.uploadFile = null;
          });

        Button('上传')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .layoutWeight(1)
          .enabled(this.uploadFile !== null)
          .onClick(() => {
            this.uploadFileToServer();
          });
      }
    }
    .width('100%')
    .padding(20)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 8, color: '#00000020', offsetX: 0, offsetY: 4 });
  }

  // 功能方法
  getFilteredTasks(): Task[] {
    return this.tasks.filter(task => {
      let matchesSearch = task.title.toLowerCase().includes(this.searchText.toLowerCase()) ||
      task.description.toLowerCase().includes(this.searchText.toLowerCase());
      let matchesType = this.selectedTaskType === '全部' || task.type === this.selectedTaskType;
      return matchesSearch && matchesType;
    });
  }

  startTask(task: Task) {
    console.log('开始任务:', task.title);
    task.status = 'in_progress';
    if (task.progress === 0) {
      task.progress = 10;
    }
    // 根据任务类型执行不同的操作
    this.handleTaskByType(task);
  }

  continueTask(task: Task) {
    console.log('继续任务:', task.title);
    this.handleTaskByType(task);
  }

  handleTaskByType(task: Task) {
    switch (task.type) {
      case '章节作业':
        this.openAssignment(task);
        break;
      case '试卷答题':
        this.openExam(task);
        break;
      case '课程视频观看':
        this.openVideo(task);
        break;
      case '阅读教材/PPT':
        this.openReading(task);
        break;
      case '报告类文档上传':
        this.showUploadDialog = true;
        break;
      default:
        console.log('未知任务类型');
    }
  }

  openAssignment(task: Task) {
    console.log('打开作业:', task.title);
    // 模拟打开作业页面
    this.showTaskProgress(task, '正在加载作业内容...');
    // 实际项目中这里应该导航到作业组件
    // router.pushUrl({ url: 'pages/assignment', params: { taskId: task.id } });
  }

  openExam(task: Task) {
    console.log('打开考试:', task.title);
    // 模拟打开考试页面
    this.showTaskProgress(task, '正在进入考试模式...');
    // 实际项目中这里应该导航到在线测验组件
    // router.pushUrl({ url: 'pages/quiz', params: { taskId: task.id } });
  }

  openVideo(task: Task) {
    console.log('打开视频:', task.title);
    // 模拟打开视频页面
    this.showTaskProgress(task, '正在加载视频播放器...');
    // 实际项目中这里应该导航到视频学习组件
    // router.pushUrl({ url: 'pages/video', params: { taskId: task.id } });
  }

  openReading(task: Task) {
    console.log('打开阅读材料:', task.title);
    // 模拟打开阅读页面
    this.showTaskProgress(task, '正在加载阅读材料...');
    // 实际项目中这里应该导航到资源阅读页面
    // router.pushUrl({ url: 'pages/reading', params: { taskId: task.id } });
  }

  showTaskProgress(task: Task, message: string) {
    // 显示任务进度提示
    console.log(message);

    // 模拟学习进度更新
    setTimeout(() => {
      if (task.progress < 100) {
        task.progress = Math.min(100, task.progress + 10);
        if (task.progress === 100) {
          task.status = 'completed';
          console.log(`任务 "${task.title}" 已完成！`);
        }
      }
    }, 1000);
  }

  selectFile() {
    console.log('选择文件');
    // 模拟文件选择
    this.uploadFile = {
      id: Date.now().toString(),
      name: '学习报告.pdf',
      type: 'pdf',
      url: '',
      size: '2.5MB',
      uploadTime: new Date().toISOString()
    } as Resource;
  }

  uploadFileToServer() {
    console.log('上传文件到服务器');
    // 实现文件上传逻辑
    this.showUploadDialog = false;
    this.uploadFile = null;
  }

  showLearningStats() {
    console.log('显示学习统计');
    this.showStatsDialog = true;
  }

  @Builder
  buildStatsDialog() {
    Column({ space: 20 }) {
      // 标题栏
      Row({ space: 16 }) {
        Text('📊 学习统计')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .layoutWeight(1);

        Button('✕')
          .backgroundColor('transparent')
          .fontColor('#6b7280')
          .borderRadius(6)
          .padding(8)
          .onClick(() => {
            this.showStatsDialog = false;
          });
      }

      // 总体统计
      Column({ space: 16 }) {
        Text('总体进度')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .width('100%')
          .textAlign(TextAlign.Start);

        Row({ space: 20 }) {
          this.buildStatCard('总任务数', this.tasks.length.toString(), '#6366f1');
          this.buildStatCard('已完成', this.getCompletedTasksCount().toString(), '#10b981');
          this.buildStatCard('进行中', this.getInProgressTasksCount().toString(), '#f59e0b');
          this.buildStatCard('待开始', this.getPendingTasksCount().toString(), '#6b7280');
        }

        // 完成率
        Column({ space: 8 }) {
          Row() {
            Text('整体完成率')
              .fontSize(16)
              .fontColor('#374151')
              .fontWeight(FontWeight.Medium);

            Blank();

            Text(`${this.getOverallProgress()}%`)
              .fontSize(16)
              .fontColor('#6366f1')
              .fontWeight(FontWeight.Bold);
          }

          Progress({ value: this.getOverallProgress(), total: 100, type: ProgressType.Linear })
            .color('#6366f1')
            .backgroundColor('#e5e7eb')
            .width('100%')
            .height(8);
        }
      }
      .width('100%')
      .padding(16)
      .backgroundColor('#f8fafc')
      .borderRadius(12);

      // 任务类型统计
      Column({ space: 16 }) {
        Text('任务类型分布')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .width('100%')
          .textAlign(TextAlign.Start);

        ForEach(this.getTaskTypeStats(), (stat: TaskTypeStat) => {
          Row({ space: 12 }) {
            Text(stat.type)
              .fontSize(14)
              .fontColor('#374151')
              .layoutWeight(1);

            Text(`${stat.count}个`)
              .fontSize(14)
              .fontColor('#6b7280');

            Text(`${stat.progress}%`)
              .fontSize(14)
              .fontColor('#6366f1')
              .fontWeight(FontWeight.Medium);
          }
          .width('100%')
          .padding({ left: 12, right: 12, top: 8, bottom: 8 })
          .backgroundColor('#ffffff')
          .borderRadius(8);
        });
      }
      .width('100%')
      .padding(16)
      .backgroundColor('#f8fafc')
      .borderRadius(12);

      // 学习建议
      Column({ space: 12 }) {
        Text('学习建议')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .width('100%')
          .textAlign(TextAlign.Start);

        ForEach(this.getLearningAdvice(), (advice: string) => {
          Row({ space: 8 }) {
            Text('💡')
              .fontSize(16);

            Text(advice)
              .fontSize(14)
              .fontColor('#374151')
              .lineHeight(20)
              .layoutWeight(1);
          }
          .width('100%')
          .padding(12)
          .backgroundColor('#ffffff')
          .borderRadius(8);
        });
      }
      .width('100%')
      .padding(16)
      .backgroundColor('#f8fafc')
      .borderRadius(12);
    }
    .width('100%')
    .padding(24)
    .backgroundColor('#ffffff')
    .borderRadius(16)
    .shadow({ radius: 12, color: '#00000025', offsetX: 0, offsetY: 6 })
    .margin({ left: 20, right: 20, top: 40, bottom: 40 });
  }

  @Builder
  buildStatCard(title: string, value: string, color: string) {
    Column({ space: 8 }) {
      Text(value)
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .fontColor(color);

      Text(title)
        .fontSize(12)
        .fontColor('#6b7280');
    }
    .alignItems(HorizontalAlign.Center)
    .padding(16)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .layoutWeight(1)
    .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 });
  }

  // 统计相关方法
  getCompletedTasksCount(): number {
    return this.tasks.filter(task => task.status === 'completed').length;
  }

  getInProgressTasksCount(): number {
    return this.tasks.filter(task => task.status === 'in_progress').length;
  }

  getPendingTasksCount(): number {
    return this.tasks.filter(task => task.status === 'pending').length;
  }

  getOverallProgress(): number {
    if (this.tasks.length === 0) return 0;
    const totalProgress = this.tasks.reduce((sum, task) => sum + task.progress, 0);
    return Math.round(totalProgress / this.tasks.length);
  }

  getTaskTypeStats(): TaskTypeStat[] {
    const typeMap = new Map<string, TaskTypeData>();

    this.tasks.forEach(task => {
      if (task.type !== '全部') {
        const existing: TaskTypeData = typeMap.get(task.type) || { count: 0, totalProgress: 0 };
        const newData: TaskTypeData = {
          count: existing.count + 1,
          totalProgress: existing.totalProgress + task.progress
        };
        typeMap.set(task.type, newData);
      }
    });

    return Array.from(typeMap.entries()).map((entry) => {
      const result: TaskTypeStat = {
        type: entry[0],
        count: entry[1].count,
        progress: Math.round(entry[1].totalProgress / entry[1].count)
      };
      return result;
    });
  }

  getLearningAdvice(): string[] {
    const advice: string[] = [];
    const pendingTasks = this.getPendingTasksCount();
    const inProgressTasks = this.getInProgressTasksCount();
    const overallProgress = this.getOverallProgress();

    if (pendingTasks > 0) {
      advice.push(`您还有 ${pendingTasks} 个任务未开始，建议优先处理截止日期较近的任务`);
    }

    if (inProgressTasks > 3) {
      advice.push('您有较多进行中的任务，建议专注完成其中几个，避免任务分散');
    }

    if (overallProgress < 30) {
      advice.push('学习进度较慢，建议制定详细的学习计划，每天安排固定的学习时间');
    } else if (overallProgress > 80) {
      advice.push('学习进度很好！继续保持，注意复习已完成的内容');
    }

    if (advice.length === 0) {
      advice.push('学习进度良好，继续保持规律的学习节奏');
    }

    return advice;
  }
}