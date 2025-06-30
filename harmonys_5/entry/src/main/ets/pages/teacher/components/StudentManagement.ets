import { Student, Course } from '../interfaces/TeacherInterfaces';

@Component
export struct StudentManagement {
  @Link students: Student[];
  @Link courses: Course[];
  @State searchStudentText: string = '';
  @State selectedCourse: string = '全部';
  @State sortBy: string = 'name';

  build() {
    Column({ space: 20 }) {
      // 学生管理标题
      Row({ space: 16 }) {
        Text('👥 学生管理')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b')
          .layoutWeight(1);

        Button('📊 导出数据')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .onClick(() => {
            this.exportStudentData();
          });
      }

      // 搜索和筛选
      Row({ space: 12 }) {
        TextInput({ placeholder: '🔍 搜索学生...', text: this.searchStudentText })
          .onChange(v => this.searchStudentText = v)
          .layoutWeight(1)
          .backgroundColor('#ffffff')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 });

        Button('排序')
          .backgroundColor('#f1f5f9')
          .fontColor('#64748b')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 })
          .onClick(() => {
            this.showSortDialog();
          });
      }

      // 课程筛选
      Scroll() {
        Row({ space: 8 }) {
          Button('全部')
            .backgroundColor(this.selectedCourse === '全部' ? '#6366f1' : '#ffffff')
            .fontColor(this.selectedCourse === '全部' ? Color.White : '#6b7280')
            .borderRadius(20)
            .padding({ left: 12, right: 12, top: 6, bottom: 6 })
            .fontSize(14)
            .border({ width: 1, color: this.selectedCourse === '全部' ? '#6366f1' : '#e5e7eb' })
            .onClick(() => {
              this.selectedCourse = '全部';
            });

          ForEach(this.courses, (course: Course) => {
            Button(course.name)
              .backgroundColor(this.selectedCourse === course.id ? '#6366f1' : '#ffffff')
              .fontColor(this.selectedCourse === course.id ? Color.White : '#6b7280')
              .borderRadius(20)
              .padding({ left: 12, right: 12, top: 6, bottom: 6 })
              .fontSize(14)
              .border({ width: 1, color: this.selectedCourse === course.id ? '#6366f1' : '#e5e7eb' })
              .onClick(() => {
                this.selectedCourse = course.id;
              });
          });
        }
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off);

      // 学生列表
      ForEach(this.getFilteredStudents(), (student: Student) => {
        this.buildStudentCard(student);
      });
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildStudentCard(student: Student) {
    Column({ space: 12 }) {
      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Text(student.name)
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');

          Text(`学号：${student.studentId}`)
            .fontSize(14)
            .fontColor('#6b7280');
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text(`${student.totalScore}分`)
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor(student.totalScore >= 90 ? '#10b981' : student.totalScore >= 80 ? '#f59e0b' : '#ef4444');
          
          Text('总分')
            .fontSize(12)
            .fontColor('#9ca3af');
        }
        .alignItems(HorizontalAlign.Center);
      }

      Row({ space: 16 }) {
        Column({ space: 4 }) {
          Text('📧 邮箱')
            .fontSize(12)
            .fontColor('#9ca3af');
          
          Text(student.email)
            .fontSize(14)
            .fontColor('#374151');
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text('📱 电话')
            .fontSize(12)
            .fontColor('#9ca3af');
          
          Text(student.phone)
            .fontSize(14)
            .fontColor('#374151');
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);
      }

      // 完成率进度条
      Column({ space: 8 }) {
        Row() {
          Text('学习完成率')
            .fontSize(13)
            .fontColor('#9ca3af');
          
          Blank();
          
          Text(`${Math.round(student.completionRate * 100)}%`)
            .fontSize(13)
            .fontColor('#9ca3af');
        }
        
        Progress({ value: student.completionRate * 100, total: 100, type: ProgressType.Linear })
          .color('#6366f1')
          .backgroundColor('#e5e7eb')
          .width('100%')
          .height(6);
      }

      Row({ space: 12 }) {
        Text(`🕒 最后活跃：${student.lastActive}`)
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
            this.viewStudentDetails(student);
          });
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 });
  }

  // 功能方法
  getFilteredStudents(): Student[] {
    let filtered = this.students.filter(student => {
      const matchesSearch = student.name.toLowerCase().includes(this.searchStudentText.toLowerCase()) ||
                           student.studentId.toLowerCase().includes(this.searchStudentText.toLowerCase()) ||
                           student.email.toLowerCase().includes(this.searchStudentText.toLowerCase());
      
      const matchesCourse = this.selectedCourse === '全部' || 
                           student.enrolledCourses.includes(this.selectedCourse);
      
      return matchesSearch && matchesCourse;
    });

    // 排序
    return filtered.sort((a, b) => {
      switch (this.sortBy) {
        case 'name':
          return a.name.localeCompare(b.name);
        case 'score':
          return b.totalScore - a.totalScore;
        case 'completion':
          return b.completionRate - a.completionRate;
        case 'lastActive':
          return new Date(b.lastActive).getTime() - new Date(a.lastActive).getTime();
        default:
          return 0;
      }
    });
  }

  viewStudentDetails(student: Student) {
    console.log('查看学生详情:', student.name);
    // 这里可以导航到学生详情页面或显示详情对话框
  }

  exportStudentData() {
    console.log('导出学生数据');
    // 实现数据导出功能
  }

  showSortDialog() {
    console.log('显示排序对话框');
    // 实现排序选择对话框
  }

  getEnrolledCoursesText(student: Student): string {
    const courseNames = student.enrolledCourses.map(courseId => {
      const course = this.courses.find(c => c.id === courseId);
      return course ? course.name : '未知课程';
    });
    return courseNames.join(', ');
  }
}