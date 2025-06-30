import { router } from '@kit.ArkUI';
import { Analytics, ActivityLog } from '../interfaces/TeacherInterfaces';

@Component
export struct AnalyticsOverview {
  @Link analytics: Analytics;

  build() {
    Column({ space: 20 }) {
      // 数据概览标题
      Row({ space: 16 }) {
        Text('📊 数据概览')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b')
          .layoutWeight(1);

        Button('📈 详细报告')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .onClick(() => {
            router.pushUrl({ url: 'pages/DataPage' }).catch((err: Error) => {
              console.error('Navigation failed:', err.message);
            });
          });
      }

      // 统计卡片网格
      Grid() {
        GridItem() {
          this.buildStatCard('👥', '学生总数', this.analytics.totalStudents.toString(), '#10b981');
        }
        
        GridItem() {
          this.buildStatCard('📚', '活跃课程', this.analytics.activeCourses.toString(), '#6366f1');
        }
        
        GridItem() {
          this.buildStatCard('📝', '待批改', this.analytics.pendingGrading.toString(), '#f59e0b');
        }
        
        GridItem() {
          this.buildStatCard('📈', '平均完成率', `${Math.round(this.analytics.averageCompletion * 100)}%`, '#8b5cf6');
        }
      }
      .columnsTemplate('1fr 1fr')
      .rowsTemplate('1fr 1fr')
      .columnsGap(16)
      .rowsGap(16)
      .height(200);

      // 最近活动
      this.buildRecentActivity();

      // 快速操作
      this.buildQuickActions();
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildStatCard(icon: string, title: string, value: string, color: string) {
    Column({ space: 8 }) {
      Row({ space: 8 }) {
        Text(icon)
          .fontSize(24);
        
        Blank();
        
        Text(value)
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
          .fontColor(color);
      }
      .width('100%');

      Text(title)
        .fontSize(14)
        .fontColor('#6b7280')
        .width('100%')
        .textAlign(TextAlign.Start);
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 })
    .border({ width: 1, color: '#f3f4f6' });
  }

  @Builder
  buildRecentActivity() {
    Column({ space: 16 }) {
      Row({ space: 12 }) {
        Text('🕒 最近活动')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b')
          .layoutWeight(1);

        Button('查看全部')
          .backgroundColor('transparent')
          .fontColor('#6366f1')
          .fontSize(14)
          .padding({ left: 8, right: 8, top: 4, bottom: 4 });
      }

      Column({ space: 12 }) {
        if (this.analytics.recentActivity.length === 0) {
          Column({ space: 8 }) {
            Text('📭')
              .fontSize(48)
              .fontColor('#d1d5db');
            
            Text('暂无最近活动')
              .fontSize(16)
              .fontColor('#9ca3af');
          }
          .width('100%')
          .padding(32)
          .justifyContent(FlexAlign.Center);
        } else {
          ForEach(this.analytics.recentActivity.slice(0, 5), (activity: ActivityLog) => {
            this.buildActivityItem(activity);
          });
        }
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 });
  }

  @Builder
  buildActivityItem(activity: ActivityLog) {
    Row({ space: 12 }) {
      Text(this.getActivityIcon(activity.type))
        .fontSize(20)
        .width(32)
        .textAlign(TextAlign.Center);

      Column({ space: 4 }) {
        Text(activity.description)
          .fontSize(15)
          .fontColor('#374151')
          .fontWeight(FontWeight.Medium);
        
        Row({ space: 8 }) {
          Text(activity.studentName)
            .fontSize(13)
            .fontColor('#6366f1')
            .backgroundColor('#eef2ff')
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .borderRadius(8);
          
          Text(this.formatTime(activity.timestamp))
            .fontSize(13)
            .fontColor('#9ca3af');
        }
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1);
    }
    .width('100%')
    .padding({ left: 8, right: 8, top: 12, bottom: 12 })
    .backgroundColor('#f8fafc')
    .borderRadius(8);
  }

  @Builder
  buildQuickActions() {
    Column({ space: 16 }) {
      Text('⚡ 快速操作')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1e293b')
        .width('100%')
        .textAlign(TextAlign.Start);

      Grid() {
        GridItem() {
          this.buildActionCard('📚', '新建课程', '#10b981', () => {
            this.createNewCourse();
          });
        }
        
        GridItem() {
          this.buildActionCard('📝', '发布作业', '#6366f1', () => {
            this.createNewAssignment();
          });
        }
        
        GridItem() {
          this.buildActionCard('❓', '添加题目', '#f59e0b', () => {
            this.addNewQuestion();
          });
        }
        
        GridItem() {
          this.buildActionCard('👥', '学生管理', '#8b5cf6', () => {
            this.manageStudents();
          });
        }
      }
      .columnsTemplate('1fr 1fr')
      .rowsTemplate('1fr 1fr')
      .columnsGap(12)
      .rowsGap(12)
      .height(160);
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 });
  }

  @Builder
  buildActionCard(icon: string, title: string, color: string, action: () => void) {
    Column({ space: 8 }) {
      Text(icon)
        .fontSize(32)
        .fontColor(color);
      
      Text(title)
        .fontSize(14)
        .fontColor('#374151')
        .fontWeight(FontWeight.Medium)
        .textAlign(TextAlign.Center);
    }
    .width('100%')
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .border({ width: 2, color: color })
    .onClick(() => {
      action();
    });
  }

  // 工具方法
  getActivityIcon(type: string): string {
    switch (type) {
      case 'submission': return '📤';
      case 'completion': return '✅';
      case 'login': return '🔑';
      case 'quiz': return '📋';
      case 'discussion': return '💬';
      default: return '📌';
    }
  }

  formatTime(timestamp: string): string {
    const now = new Date();
    const time = new Date(timestamp);
    const diffMs = now.getTime() - time.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 1) {
      return '刚刚';
    } else if (diffMins < 60) {
      return `${diffMins}分钟前`;
    } else if (diffHours < 24) {
      return `${diffHours}小时前`;
    } else if (diffDays < 7) {
      return `${diffDays}天前`;
    } else {
      return time.toLocaleDateString();
    }
  }

  // 快速操作方法
  createNewCourse() {
    console.log('创建新课程');
    // 触发父组件的新建课程操作
  }

  createNewAssignment() {
    console.log('发布新作业');
    // 触发父组件的新建作业操作
  }

  addNewQuestion() {
    console.log('添加新题目');
    // 触发父组件的新建题目操作
  }

  manageStudents() {
    console.log('管理学生');
    // 切换到学生管理标签页
  }
}