import { Course, Student, Assignment, QuestionBank, Analytics } from './interfaces/TeacherInterfaces';
import { CourseManagement } from './components/CourseManagement';
import { StudentManagement } from './components/StudentManagement';
import { AssignmentManagement } from './components/AssignmentManagement';
import { QuestionBankManagement } from './components/QuestionBankManagement';
import { AnalyticsOverview } from './components/AnalyticsOverview';
// import { AIAssistant } from './components/AIAssistant'; // AI功能已移除
import router from '@ohos.router';

@Component
export struct TeacherMain {
  @State currentTab: number = 0;
  @State courses: Course[] = [];
  @State students: Student[] = [];
  @State assignments: Assignment[] = [];
  @State questionBank: QuestionBank[] = [];
  @State analytics: Analytics = {
    totalStudents: 0,
    activeCourses: 0,
    pendingGrading: 0,
    averageCompletion: 0,
    recentActivity: []
  };

  aboutToAppear() {
    this.initializeData();
  }

  initializeData() {
    // 初始化课程数据
    this.courses = [
      {
        id: '1',
        name: '高等数学',
        teacher: '张教授',
        credit: 4,
        category: '专业课',
        description: '高等数学基础课程',
        createTime: '2024-01-15',
        studentCount: 45,
        status: '进行中'
      },
      {
        id: '2',
        name: '线性代数',
        teacher: '李教授',
        credit: 3,
        category: '专业课',
        description: '线性代数基础理论',
        createTime: '2024-01-20',
        studentCount: 38,
        status: '进行中'
      }
    ];

    // 初始化学生数据
    this.students = [
      {
        id: '1',
        name: '张三',
        studentId: '2024001',
        email: 'zhangsan@example.com',
        phone: '13800138001',
        enrolledCourses: ['1', '2'],
        totalScore: 92,
        completionRate: 95,
        lastActive: '2024-01-25 14:30'
      },
      {
        id: '2',
        name: '李四',
        studentId: '2024002',
        email: 'lisi@example.com',
        phone: '13800138002',
        enrolledCourses: ['1'],
        totalScore: 85,
        completionRate: 88,
        lastActive: '2024-01-25 10:15'
      },
      {
        id: '3',
        name: '王五',
        studentId: '2024003',
        email: 'wangwu@example.com',
        phone: '13800138003',
        enrolledCourses: ['1', '2'],
        totalScore: 78,
        completionRate: 82,
        lastActive: '2024-01-24 16:45'
      },
      {
        id: '4',
        name: '赵六',
        studentId: '2024004',
        email: 'zhaoliu@example.com',
        phone: '13800138004',
        enrolledCourses: ['2'],
        totalScore: 88,
        completionRate: 90,
        lastActive: '2024-01-24 09:20'
      }
    ];

    // 初始化作业数据
    this.assignments = [
      {
        id: '1',
        title: '微积分练习题',
        courseId: '1',
        type: '作业',
        description: '第一章微积分基础练习',
        deadline: '2024-02-01',
        totalPoints: 100,
        submissionCount: 35,
        averageScore: 82,
        status: '进行中'
      }
    ];

    // 初始化题库数据
    this.questionBank = [
      {
        id: '1',
        subject: '高等数学',
        difficulty: '中等',
        type: '选择题',
        question: '求函数f(x)=x²的导数',
        options: ['2x', 'x²', '2', 'x'],
        correctAnswer: '2x',
        knowledgePoints: ['导数', '基本函数'],
        usageCount: 15,
        successRate: 85
      }
    ];

    // 初始化分析数据
    this.analytics = {
      totalStudents: this.students.length,
      activeCourses: this.courses.length,
      pendingGrading: 5,
      averageCompletion: 85,
      recentActivity: [
        {
          id: '1',
          type: '提交作业',
          studentName: '王小明',
          description: '提交了微积分练习题',
          timestamp: '2024-01-25 14:30'
        }
      ]
    };
  }

  @Builder
  buildQuickStatsCards() {
    Row({ space: 12 }) {
      // 学生总数卡片
      Column({ space: 8 }) {
        Text('👥')
          .fontSize(24);
        Text(`${this.students.length}`)
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b');
        Text('学生总数')
          .fontSize(12)
          .fontColor('#64748b');
      }
      .width('0')
      .layoutWeight(1)
      .height(80)
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
      .borderRadius(16)
      .backdropBlur(10)
      .border({ width: 1, color: '#e5e7eb' });

      // 课程总数卡片
      Column({ space: 8 }) {
        Text('📚')
          .fontSize(24);
        Text(`${this.courses.length}`)
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b');
        Text('课程总数')
          .fontSize(12)
          .fontColor('#64748b');
      }
      .width('0')
      .layoutWeight(1)
      .height(80)
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
      .borderRadius(16)
      .backdropBlur(10)
      .border({ width: 1, color: '#e5e7eb' });

      // 作业总数卡片
      Column({ space: 8 }) {
        Text('📝')
          .fontSize(24);
        Text(`${this.assignments.length}`)
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b');
        Text('作业总数')
          .fontSize(12)
          .fontColor('#64748b');
      }
      .width('0')
      .layoutWeight(1)
      .height(80)
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
      .borderRadius(16)
      .backdropBlur(10)
      .border({ width: 1, color: '#e5e7eb' });
    }
  }

  @Builder
  buildFunctionCards() {
    Column({ space: 16 }) {
      // 第一行功能卡片
      Row({ space: 12 }) {
        // 课程管理卡片
        Column({ space: 12 }) {
          Text('📚')
            .fontSize(32);
          Text('课程管理')
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');
          Text('管理课程信息\n查看课程详情')
            .fontSize(12)
            .fontColor('#64748b')
            .textAlign(TextAlign.Center)
            .lineHeight(16);
        }
        .width('0')
        .layoutWeight(1)
        .height(120)
        .justifyContent(FlexAlign.Center)
        .backgroundColor('#ffffff')
        .borderRadius(20)
        .shadow({ radius: 8, color: '#00000015', offsetY: 4 })
        .onClick(() => {
          this.currentTab = 1;
        });

        // 学生管理卡片
        Column({ space: 12 }) {
          Text('👥')
            .fontSize(32);
          Text('学生管理')
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');
          Text('管理学生信息\n查看学习进度')
            .fontSize(12)
            .fontColor('#64748b')
            .textAlign(TextAlign.Center)
            .lineHeight(16);
        }
        .width('0')
        .layoutWeight(1)
        .height(120)
        .justifyContent(FlexAlign.Center)
        .backgroundColor('#ffffff')
        .borderRadius(20)
        .shadow({ radius: 8, color: '#00000015', offsetY: 4 })
        .onClick(() => {
          console.log('点击学生管理，切换到tab 2');
          this.currentTab = 2;
          console.log('当前tab:', this.currentTab);
        });
      }

      // 第二行功能卡片
      Row({ space: 12 }) {
        // 作业管理卡片
        Column({ space: 12 }) {
          Text('📝')
            .fontSize(32);
          Text('作业管理')
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');
          Text('发布作业\n批改作业')
            .fontSize(12)
            .fontColor('#64748b')
            .textAlign(TextAlign.Center)
            .lineHeight(16);
        }
        .width('0')
        .layoutWeight(1)
        .height(120)
        .justifyContent(FlexAlign.Center)
        .backgroundColor('#ffffff')
        .borderRadius(20)
        .shadow({ radius: 8, color: '#00000015', offsetY: 4 })
        .onClick(() => {
          this.currentTab = 3;
        });

        // 题库管理卡片
        Column({ space: 12 }) {
          Text('❓')
            .fontSize(32);
          Text('题库管理')
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');
          Text('管理题目\n组织试卷')
            .fontSize(12)
            .fontColor('#64748b')
            .textAlign(TextAlign.Center)
            .lineHeight(16);
        }
        .width('0')
        .layoutWeight(1)
        .height(120)
        .justifyContent(FlexAlign.Center)
        .backgroundColor('#ffffff')
        .borderRadius(20)
        .shadow({ radius: 8, color: '#00000015', offsetY: 4 })
        .onClick(() => {
          this.currentTab = 4;
        });
      }
    }
  }

  @Builder
  buildRecentActivityCard() {
    Column({ space: 16 }) {
      Row() {
        Text('📈 最近活动')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b')
          .layoutWeight(1);
        Text('查看全部')
          .fontSize(14)
          .fontColor('#3b82f6')
          .onClick(() => {
            router.pushUrl({ url: 'pages/teacher/components/ActivityLog' });
          });
      }
      .width('100%');

      Column({ space: 12 }) {
        // 活动项目
        Row({ space: 12 }) {
          Text('📚')
            .fontSize(16);
          Column({ space: 4 }) {
            Text('新增课程《高等数学》')
              .fontSize(14)
              .fontColor('#1e293b');
            Text('2小时前')
              .fontSize(12)
              .fontColor('#64748b');
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1);
        }
        .width('100%')
        .padding(12);

        Row({ space: 12 }) {
          Text('📝')
            .fontSize(16);
          Column({ space: 4 }) {
            Text('发布作业《线性代数练习》')
              .fontSize(14)
              .fontColor('#1e293b');
            Text('5小时前')
              .fontSize(12)
              .fontColor('#64748b');
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1);
        }
        .width('100%')
        .padding(12);

        Row({ space: 12 }) {
          Text('👥')
            .fontSize(16);
          Column({ space: 4 }) {
            Text('新学生加入课程')
              .fontSize(14)
              .fontColor('#1e293b');
            Text('1天前')
              .fontSize(12)
              .fontColor('#64748b');
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1);
        }
        .width('100%')
        .padding(12);
      }
    }
    .width('100%')
    .padding(20)
    .backgroundColor('#ffffff')
    .borderRadius(20)
    .shadow({ radius: 8, color: '#00000015', offsetY: 4 });
  }

  @Builder
  buildBottomNavigation() {
    Row({ space: 0 }) {
      // 首页
      Column({ space: 4 }) {
        Text('🏠')
          .fontSize(20);
        Text('首页')
          .fontSize(12)
          .fontColor('#3b82f6');
      }
      .width('0')
      .layoutWeight(1)
      .height(60)
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
      .onClick(() => {
        this.currentTab = 0;
      });

      // 数据分析
      Column({ space: 4 }) {
        Text('📊')
          .fontSize(20);
        Text('分析')
          .fontSize(12)
          .fontColor('#64748b');
      }
      .width('0')
      .layoutWeight(1)
      .height(60)
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
      .onClick(() => {
        this.currentTab = 5;
      });

      // 消息中心
      Column({ space: 4 }) {
        Text('💬')
          .fontSize(20);
        Text('消息')
          .fontSize(12)
          .fontColor('#64748b');
      }
      .width('0')
      .layoutWeight(1)
      .height(60)
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
      .onClick(() => {
        this.currentTab = 6;
      });

      // 设置
      Column({ space: 4 }) {
        Text('⚙️')
          .fontSize(20);
        Text('设置')
          .fontSize(12)
          .fontColor('#64748b');
      }
      .width('0')
      .layoutWeight(1)
      .height(60)
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
      .onClick(() => {
        this.currentTab = 7;
      });
    }
    .width('100%')
    .shadow({ radius: 8, color: '#00000015', offsetY: -4 });
  }

  @Builder
  TabBuilder(title: string, icon: string, index: number) {
    Column({ space: 6 }) {
      Text(icon)
        .fontSize(22)
        .animation({
          duration: 200,
          curve: Curve.EaseInOut
        });
      Text(title)
        .fontSize(13)
        .fontWeight(this.currentTab === index ? FontWeight.Bold : FontWeight.Medium)
        .fontColor(this.currentTab === index ? '#3b82f6' : '#64748b')
        .animation({
          duration: 200,
          curve: Curve.EaseInOut
        });
    }
    .width('100%')
    .height(60)
    .justifyContent(FlexAlign.Center)
    .backgroundColor(this.currentTab === index ?
      'linear-gradient(135deg, #3b82f6 0%, #1e40af 100%)' : Color.Transparent)
    .borderRadius(12)
    .shadow(this.currentTab === index ? {
      radius: 8,
      color: '#3b82f640',
      offsetX: 0,
      offsetY: 4
    } : {
      radius: 0,
      color: Color.Transparent,
      offsetX: 0,
      offsetY: 0
    })
    .scale(this.currentTab === index ? { x: 1.05, y: 1.05 } : { x: 1, y: 1 })
    .animation({
      duration: 300,
      curve: Curve.EaseInOut
    })
    .onClick(() => {
      this.currentTab = index;
    })
  }

  build() {
    Stack() {
      // 背景渐变
      Column()
        .width('100%')
        .height('100%')
        .linearGradient({
          angle: 135,
          colors: [['#3b82f6', 0.0], ['#1e40af', 0.3], ['#1d4ed8', 0.6], ['#2563eb', 1.0]]
        });

      Column() {
        // 顶部导航栏
        Row() {
          Column({ space: 4 }) {
            Text('🎓 教师管理平台')
              .fontSize(26)
              .fontWeight(FontWeight.Bold)
              .fontColor('#ffffff')
              .textShadow({ radius: 2, color: '#00000030', offsetX: 1, offsetY: 1 });
            Text('智能教学管理系统')
              .fontSize(14)
              .fontColor('#ffffff90')
              .opacity(0.9);
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1);

          Row({ space: 16 }) {
            Button() {
              Row({ space: 6 }) {
                Text('←')
                  .fontSize(18)
                  .fontColor('#ffffff');
                Text('返回')
                  .fontSize(12)
                  .fontColor('#ffffff');
              }
            }
            .backgroundColor('#8b5cf6')
            .borderRadius(25)
            .padding({ left: 12, right: 12, top: 8, bottom: 8 })
            .backdropBlur(10)
            .border({ width: 1, color: '#a855f7' })
            .onClick(() => {
              // 返回上一页
              router.back();
            });

            Button() {
              Row({ space: 6 }) {
                Text('✕')
                  .fontSize(18)
                  .fontColor('#ffffff');
                Text('退出')
                  .fontSize(12)
                  .fontColor('#ffffff');
              }
            }
            .backgroundColor('#8b5cf6')
            .borderRadius(25)
            .padding({ left: 12, right: 12, top: 8, bottom: 8 })
            .backdropBlur(10)
            .border({ width: 1, color: '#a855f7' })
            .onClick(() => {
              // 退出应用或返回首页
              router.replaceUrl({ url: 'pages/Index' });
            });
          }
        }
        .width('100%')
        .padding({ left: 20, right: 20, top: 20, bottom: 16 });

        // 主要内容区域
        if (this.currentTab === 0) {
          // 功能卡片网格
          Column({ space: 20 }) {
            // 快速统计卡片
            this.buildQuickStatsCards();

            // 功能导航卡片
            this.buildFunctionCards();

            // 最近活动卡片
            this.buildRecentActivityCard();
          }
          .padding({ left: 20, right: 20, top: 20, bottom: 20 })
          .layoutWeight(1);
        } else if (this.currentTab === 1) {
          CourseManagement({ courses: $courses })
            .padding(20)
            .layoutWeight(1)
            .backgroundColor('#f8fafc');
        } else if (this.currentTab === 2) {
          // 学生管理页面
          Column() {
            // 固定顶部标题栏
            Row() {
              Column({ space: 4 }) {
                Text('👥 学生管理')
                  .fontSize(24)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#1e293b');
                Text(`当前有 ${this.students.length} 名学生`)
                  .fontSize(16)
                  .fontColor('#64748b');
              }
              .alignItems(HorizontalAlign.Start)
              .layoutWeight(1);

              // 添加学生按钮
              Button() {
                Row({ space: 8 }) {
                  Text('+')
                    .fontSize(16)
                    .fontColor('#ffffff')
                    .fontWeight(FontWeight.Bold);
                  Text('添加学生')
                    .fontSize(14)
                    .fontColor('#ffffff')
                    .fontWeight(FontWeight.Medium);
                }
              }
              .backgroundColor('#3b82f6')
              .borderRadius(10)
              .padding({ left: 14, right: 14, top: 8, bottom: 8 })
              .shadow({ radius: 6, color: '#3b82f640', offsetY: 3 })
              .onClick(() => {
                console.log('添加新学生');
              });
            }
            .width('100%')
            .padding({ left: 20, right: 20, top: 20, bottom: 16 })
            .backgroundColor('#ffffff')
            .shadow({ radius: 4, color: '#00000010', offsetY: 2 });

            // 可滚动的学生列表区域
            Scroll() {
              Column({ space: 16 }) {
                ForEach(this.students, (student: Student) => {
                  Column({ space: 12 }) {
                    // 学生基本信息行
                    Row({ space: 12 }) {
                      // 学生头像
                      Column() {
                        Text('👤')
                          .fontSize(20)
                          .fontColor('#3b82f6');
                      }
                      .width(44)
                      .height(44)
                      .justifyContent(FlexAlign.Center)
                      .backgroundColor('#f0f9ff')
                      .borderRadius(22)
                      .border({ width: 1.5, color: '#bfdbfe' });

                      // 学生基本信息
                      Column({ space: 4 }) {
                        Row({ space: 8 }) {
                          Text(student.name)
                            .fontSize(16)
                            .fontWeight(FontWeight.Bold)
                            .fontColor('#1e293b');
                          Text(student.studentId)
                            .fontSize(12)
                            .fontColor('#64748b')
                            .backgroundColor('#f1f5f9')
                            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
                            .borderRadius(4);
                        }
                        .alignItems(VerticalAlign.Center);

                        Text(`📧 ${student.email}`)
                          .fontSize(12)
                          .fontColor('#64748b')
                          .maxLines(1)
                          .textOverflow({ overflow: TextOverflow.Ellipsis });
                      }
                      .alignItems(HorizontalAlign.Start)
                      .layoutWeight(1);

                      // 操作按钮组
                      Row({ space: 6 }) {
                        Button() {
                          Text('详情')
                            .fontSize(12)
                            .fontColor('#ffffff');
                        }
                        .backgroundColor('#10b981')
                        .borderRadius(6)
                        .padding({ left: 10, right: 10, top: 4, bottom: 4 })
                        .onClick(() => {
                          console.log(`查看学生详情: ${student.name}`);
                        });

                        Button() {
                          Text('编辑')
                            .fontSize(12)
                            .fontColor('#ffffff');
                        }
                        .backgroundColor('#f59e0b')
                        .borderRadius(6)
                        .padding({ left: 10, right: 10, top: 4, bottom: 4 })
                        .onClick(() => {
                          console.log(`编辑学生信息: ${student.name}`);
                        });
                      }
                    }
                    .width('100%')
                    .alignItems(VerticalAlign.Center);

                    // 学生详细信息行
                    Row({ space: 16 }) {
                      Column({ space: 2 }) {
                        Text('📱 联系方式')
                          .fontSize(10)
                          .fontColor('#94a3b8');
                        Text(student.phone)
                          .fontSize(12)
                          .fontColor('#64748b');
                      }
                      .alignItems(HorizontalAlign.Start)
                      .layoutWeight(1);

                      Column({ space: 2 }) {
                        Text('📊 总分')
                          .fontSize(10)
                          .fontColor('#94a3b8');
                        Text(`${student.totalScore}`)
                          .fontSize(12)
                          .fontColor('#059669')
                          .fontWeight(FontWeight.Medium);
                      }
                      .alignItems(HorizontalAlign.Start)
                      .layoutWeight(1);

                      Column({ space: 2 }) {
                        Text('✅ 完成率')
                          .fontSize(10)
                          .fontColor('#94a3b8');
                        Text(`${student.completionRate}%`)
                          .fontSize(12)
                          .fontColor('#dc2626')
                          .fontWeight(FontWeight.Medium);
                      }
                      .alignItems(HorizontalAlign.Start)
                      .layoutWeight(1);

                      Column({ space: 2 }) {
                        Text('🕒 最后活跃')
                          .fontSize(10)
                          .fontColor('#94a3b8');
                        Text(student.lastActive.split(' ')[0])
                          .fontSize(12)
                          .fontColor('#64748b');
                      }
                      .alignItems(HorizontalAlign.Start)
                      .layoutWeight(1);
                    }
                    .width('100%');
                  }
                  .width('100%')
                  .padding(16)
                  .backgroundColor('#ffffff')
                  .borderRadius(12)
                  .shadow({ radius: 4, color: '#00000008', offsetY: 2 })
                  .border({ width: 1, color: '#f1f5f9' });
                });
              }
              .width('100%')
              .padding({ left: 20, right: 20, top: 16, bottom: 20 });
            }
            .layoutWeight(1)
            .scrollable(ScrollDirection.Vertical)
            .scrollBar(BarState.Auto)
            .edgeEffect(EdgeEffect.Spring);
          }
          .width('100%')
          .height('100%')
          .backgroundColor('#f8fafc');
        } else if (this.currentTab === 3) {
          AssignmentManagement({ assignments: $assignments, courses: $courses })
            .padding(20)
            .layoutWeight(1)
            .backgroundColor('#f8fafc');
        } else if (this.currentTab === 4) {
          QuestionBankManagement({ questionBank: $questionBank })
            .padding(20)
            .layoutWeight(1)
            .backgroundColor('#f8fafc');
        } else if (this.currentTab === 5) {
          AnalyticsOverview({ analytics: $analytics })
            .padding(20)
            .layoutWeight(1)
            .backgroundColor('#f8fafc');
        } else if (this.currentTab === 6) {
          // 消息中心页面
          Column({ space: 20 }) {
            Text('💬 消息中心')
              .fontSize(24)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1e293b');
            Text('暂无新消息')
              .fontSize(16)
              .fontColor('#64748b');
          }
          .padding(20)
          .layoutWeight(1)
          .backgroundColor('#f8fafc')
          .justifyContent(FlexAlign.Center);
        } else if (this.currentTab === 7) {
          // 设置页面
          Column({ space: 20 }) {
            Text('⚙️ 系统设置')
              .fontSize(24)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1e293b');
            Text('设置功能开发中...')
              .fontSize(16)
              .fontColor('#64748b');
          }
          .padding(20)
          .layoutWeight(1)
          .backgroundColor('#f8fafc')
          .justifyContent(FlexAlign.Center);
        }

        // 底部导航栏已移除
      }
      .width('100%')
      .height('100%')

      // AI助手组件已移除
    }
    .width('100%')
    .height('100%')
  }
}