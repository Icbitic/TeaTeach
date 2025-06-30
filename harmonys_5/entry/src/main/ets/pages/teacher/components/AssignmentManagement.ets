import { router } from '@kit.ArkUI';
import { Assignment, Course, AssignmentType } from '../interfaces/TeacherInterfaces';

@Component
export struct AssignmentManagement {
  @Link assignments: Assignment[];
  @Link courses: Course[];
  @State showAssignmentDialog: boolean = false;
  @State newAssignmentTitle: string = '';
  @State newAssignmentDescription: string = '';
  @State newAssignmentType: string = 'homework';
  @State newAssignmentDeadline: string = '';
  @State newAssignmentPoints: number = 100;
  @State selectedAssignmentCourse: string = '';
  @State searchText: string = '';
  @State selectedType: string = '全部';

  private assignmentTypes: AssignmentType[] = [
    { value: 'homework', label: '📝 章节作业' },
    { value: 'exam', label: '📋 试卷答题' },
    { value: 'video', label: '🎥 课程视频观看' },
    { value: 'reading', label: '📚 阅读教材/PPT' },
    { value: 'project', label: '📄 报告类文档上传' }
  ];

  build() {
    Column({ space: 20 }) {
      // 作业管理标题和操作
      Row({ space: 16 }) {
        Text('📝 作业管理')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b')
          .layoutWeight(1);

        Button('➕ 新建作业')
          .backgroundColor('#10b981')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .onClick(() => {
            this.showAssignmentDialog = true;
          });
      }

      // 搜索和筛选
      Row({ space: 12 }) {
        TextInput({ placeholder: '🔍 搜索作业...', text: this.searchText })
          .onChange(v => this.searchText = v)
          .layoutWeight(1)
          .backgroundColor('#ffffff')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 });

        Button('筛选')
          .backgroundColor('#f1f5f9')
          .fontColor('#64748b')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 });
      }

      // 作业类型筛选
      Scroll() {
        Row({ space: 8 }) {
          Button('全部')
            .backgroundColor(this.selectedType === '全部' ? '#6366f1' : '#ffffff')
            .fontColor(this.selectedType === '全部' ? Color.White : '#6b7280')
            .borderRadius(20)
            .padding({ left: 12, right: 12, top: 6, bottom: 6 })
            .fontSize(14)
            .border({ width: 1, color: this.selectedType === '全部' ? '#6366f1' : '#e5e7eb' })
            .onClick(() => {
              this.selectedType = '全部';
            });

          ForEach(this.assignmentTypes, (type: AssignmentType) => {
            Button(type.label)
              .backgroundColor(this.selectedType === type.value ? '#6366f1' : '#ffffff')
              .fontColor(this.selectedType === type.value ? Color.White : '#6b7280')
              .borderRadius(20)
              .padding({ left: 12, right: 12, top: 6, bottom: 6 })
              .fontSize(14)
              .border({ width: 1, color: this.selectedType === type.value ? '#6366f1' : '#e5e7eb' })
              .onClick(() => {
                this.selectedType = type.value;
              });
          });
        }
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off);

      // 作业列表
      ForEach(this.getFilteredAssignments(), (assignment: Assignment) => {
        this.buildAssignmentCard(assignment);
      });

      // 新建作业对话框
      if (this.showAssignmentDialog) {
        this.buildAssignmentDialog();
      }
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildAssignmentCard(assignment: Assignment) {
    Column({ space: 12 }) {
      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Text(assignment.title)
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');

          Text(this.getAssignmentTypeLabel(assignment.type))
            .fontSize(14)
            .fontColor('#6366f1')
            .backgroundColor('#eef2ff')
            .padding({ left: 8, right: 8, top: 4, bottom: 4 })
            .borderRadius(12);
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text(`${assignment.totalPoints}分`)
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');
          
          Text('总分')
            .fontSize(12)
            .fontColor('#9ca3af');
        }
        .alignItems(HorizontalAlign.Center);
      }

      Text(assignment.description)
        .fontSize(14)
        .fontColor('#64748b')
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis });

      Row({ space: 16 }) {
        Column({ space: 4 }) {
          Text('📅 截止时间')
            .fontSize(12)
            .fontColor('#9ca3af');
          
          Text(assignment.deadline)
            .fontSize(14)
            .fontColor('#374151');
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text('📊 提交情况')
            .fontSize(12)
            .fontColor('#9ca3af');
          
          Text(`${assignment.submissionCount}人提交`)
            .fontSize(14)
            .fontColor('#374151');
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text('📈 平均分')
            .fontSize(12)
            .fontColor('#9ca3af');
          
          Text(`${assignment.averageScore}分`)
            .fontSize(14)
            .fontColor(assignment.averageScore >= 80 ? '#10b981' : assignment.averageScore >= 60 ? '#f59e0b' : '#ef4444');
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);
      }

      Row({ space: 12 }) {
        Text(`📚 课程：${this.getCourseName(assignment.courseId)}`)
          .fontSize(12)
          .fontColor('#9ca3af')
          .layoutWeight(1);

        Button('查看详情')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(6)
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .fontSize(12)
          .onClick(() => {
            this.viewAssignmentDetails(assignment);
          });

        Button('编辑')
          .backgroundColor('#f59e0b')
          .fontColor(Color.White)
          .borderRadius(6)
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .fontSize(12)
          .onClick(() => {
            this.editAssignment(assignment);
          });
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 });
  }

  @Builder
  buildAssignmentDialog() {
    Column({ space: 16 }) {
      Text('新建作业')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1e293b');

      TextInput({ placeholder: '作业标题', text: this.newAssignmentTitle })
        .onChange(v => this.newAssignmentTitle = v)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .padding(12);

      TextArea({ placeholder: '作业描述', text: this.newAssignmentDescription })
        .onChange(v => this.newAssignmentDescription = v)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .padding(12)
        .height(80);

      // 作业类型选择
      Column({ space: 8 }) {
        Text('作业类型')
          .fontSize(14)
          .fontColor('#374151')
          .alignSelf(ItemAlign.Start);
        
        Scroll() {
          Row({ space: 8 }) {
            ForEach(this.assignmentTypes, (type: AssignmentType) => {
              Button(type.label)
                .backgroundColor(this.newAssignmentType === type.value ? '#6366f1' : '#f8fafc')
                .fontColor(this.newAssignmentType === type.value ? Color.White : '#374151')
                .borderRadius(8)
                .padding({ left: 12, right: 12, top: 8, bottom: 8 })
                .fontSize(14)
                .onClick(() => {
                  this.newAssignmentType = type.value;
                });
            });
          }
        }
        .scrollable(ScrollDirection.Horizontal)
        .scrollBar(BarState.Off);
      }

      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Text('所属课程')
            .fontSize(14)
            .fontColor('#374151');
          
          Button(this.getCourseName(this.selectedAssignmentCourse) || '选择课程')
            .backgroundColor('#f8fafc')
            .fontColor('#374151')
            .borderRadius(8)
            .padding(8)
            .onClick(() => {
              this.showCourseSelector();
            });
        }
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text('总分')
            .fontSize(14)
            .fontColor('#374151');
          
          TextInput({ text: this.newAssignmentPoints.toString() })
            .onChange(v => this.newAssignmentPoints = parseInt(v) || 100)
            .backgroundColor('#f8fafc')
            .borderRadius(8)
            .padding(8)
            .type(InputType.Number);
        }
        .layoutWeight(1);
      }

      TextInput({ placeholder: '截止时间 (YYYY-MM-DD)', text: this.newAssignmentDeadline })
        .onChange(v => this.newAssignmentDeadline = v)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .padding(12);

      Row({ space: 12 }) {
        Button('取消')
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .layoutWeight(1)
          .onClick(() => {
            this.showAssignmentDialog = false;
            this.resetAssignmentForm();
          });

        Button('确定')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .layoutWeight(1)
          .onClick(() => {
            this.addNewAssignment();
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
  getFilteredAssignments(): Assignment[] {
    return this.assignments.filter(assignment => {
      const matchesSearch = assignment.title.toLowerCase().includes(this.searchText.toLowerCase()) ||
                           assignment.description.toLowerCase().includes(this.searchText.toLowerCase());
      const matchesType = this.selectedType === '全部' || assignment.type === this.selectedType;
      return matchesSearch && matchesType;
    });
  }

  getAssignmentTypeLabel(type: string): string {
    const assignmentType = this.assignmentTypes.find(t => t.value === type);
    return assignmentType ? assignmentType.label : type;
  }

  getCourseName(courseId: string): string {
    const course = this.courses.find(c => c.id === courseId);
    return course ? course.name : '未知课程';
  }

  viewAssignmentDetails(assignment: Assignment) {
    router.pushUrl({
      url: 'pages/TaskPage',
      params: {
        assignmentId: assignment.id,
        title: assignment.title
      }
    }).catch((err: Error) => {
      console.error('Navigation failed:', err.message);
    });
  }

  editAssignment(assignment: Assignment) {
    this.newAssignmentTitle = assignment.title;
    this.newAssignmentDescription = assignment.description;
    this.newAssignmentType = assignment.type;
    this.newAssignmentDeadline = assignment.deadline;
    this.newAssignmentPoints = assignment.totalPoints;
    this.selectedAssignmentCourse = assignment.courseId;
    this.showAssignmentDialog = true;
  }

  showCourseSelector() {
    console.log('显示课程选择器');
    // 实现课程选择对话框
  }

  addNewAssignment() {
    if (this.newAssignmentTitle.trim() === '') {
      return;
    }

    const newAssignment: Assignment = {
      id: Date.now().toString(),
      title: this.newAssignmentTitle,
      courseId: this.selectedAssignmentCourse,
      type: this.newAssignmentType,
      description: this.newAssignmentDescription,
      deadline: this.newAssignmentDeadline,
      totalPoints: this.newAssignmentPoints,
      submissionCount: 0,
      averageScore: 0,
      status: '进行中'
    };

    this.assignments.push(newAssignment);
    this.resetAssignmentForm();
    this.showAssignmentDialog = false;
  }

  resetAssignmentForm() {
    this.newAssignmentTitle = '';
    this.newAssignmentDescription = '';
    this.newAssignmentDeadline = '';
    this.newAssignmentPoints = 100;
    this.selectedAssignmentCourse = '';
    this.newAssignmentType = 'homework';
  }
}