import { Course } from '../interfaces/TeacherInterfaces';

@Component
export struct CourseManagement {
  @Link courses: Course[];
  @State showCourseDialog: boolean = false;
  @State newCourseName: string = '';
  @State newCourseDescription: string = '';
  @State newCourseCredit: number = 3;
  @State newCourseCategory: string = '专业课';
  @State searchText: string = '';
  @State selectedCategory: string = '全部';

  private categories: string[] = ['全部', '专业课', '公共课', '选修课'];

  build() {
    Column({ space: 20 }) {
      // 课程管理标题和操作
      Row({ space: 16 }) {
        Text('📚 课程管理')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b')
          .layoutWeight(1);

        Button('➕ 新建课程')
          .backgroundColor('#10b981')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .onClick(() => {
            this.showCourseDialog = true;
          });
      }

      // 搜索和筛选
      Row({ space: 12 }) {
        TextInput({ placeholder: '🔍 搜索课程...', text: this.searchText })
          .onChange(v => this.searchText = v)
          .layoutWeight(1)
          .backgroundColor('#ffffff')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 });

        Button('筛选')
          .backgroundColor('#f1f5f9')
          .fontColor('#64748b')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 })
          .onClick(() => {
            this.showCourseFilterDialog();
          });
      }

      // 课程分类筛选
      Scroll() {
        Row({ space: 8 }) {
          ForEach(this.categories, (category: string) => {
            Button(category)
              .backgroundColor(this.selectedCategory === category ? '#6366f1' : '#ffffff')
              .fontColor(this.selectedCategory === category ? Color.White : '#6b7280')
              .borderRadius(20)
              .padding({ left: 12, right: 12, top: 6, bottom: 6 })
              .fontSize(14)
              .border({ width: 1, color: this.selectedCategory === category ? '#6366f1' : '#e5e7eb' })
              .onClick(() => {
                this.selectedCategory = category;
              });
          });
        }
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off);

      // 课程列表
      ForEach(this.getFilteredCourses(), (course: Course) => {
        this.buildCourseCard(course);
      });

      // 新建课程对话框
      if (this.showCourseDialog) {
        this.buildCourseDialog();
      }
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildCourseCard(course: Course) {
    Column({ space: 12 }) {
      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Text(course.name)
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b');

          Text(`${course.category} • ${course.credit}学分`)
            .fontSize(14)
            .fontColor('#6b7280');
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Text(`👥 ${course.studentCount}`)
          .fontSize(14)
          .fontColor('#6b7280')
          .backgroundColor('#f8fafc')
          .padding({ left: 8, right: 8, top: 4, bottom: 4 })
          .borderRadius(12);
      }

      Text(course.description)
        .fontSize(14)
        .fontColor('#64748b')
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis });

      Row({ space: 12 }) {
        Text(`📅 创建时间：${course.createTime}`)
          .fontSize(12)
          .fontColor('#9ca3af');

        Blank();

        Button('编辑')
          .backgroundColor('#f59e0b')
          .fontColor(Color.White)
          .borderRadius(6)
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .fontSize(12)
          .onClick(() => {
            this.editCourse(course);
          });

        Button('删除')
          .backgroundColor('#ef4444')
          .fontColor(Color.White)
          .borderRadius(6)
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .fontSize(12)
          .onClick(() => {
            this.deleteCourse(course.id);
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
  buildCourseDialog() {
    Column({ space: 16 }) {
      Text('新建课程')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1e293b');

      TextInput({ placeholder: '课程名称', text: this.newCourseName })
        .onChange(v => this.newCourseName = v)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .padding(12);

      TextArea({ placeholder: '课程描述', text: this.newCourseDescription })
        .onChange(v => this.newCourseDescription = v)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .padding(12)
        .height(80);

      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Text('学分')
            .fontSize(14)
            .fontColor('#374151');
          
          TextInput({ text: this.newCourseCredit.toString() })
            .onChange(v => this.newCourseCredit = parseInt(v) || 3)
            .backgroundColor('#f8fafc')
            .borderRadius(8)
            .padding(8)
            .type(InputType.Number);
        }
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text('课程类别')
            .fontSize(14)
            .fontColor('#374151');
          
          Button(this.newCourseCategory)
            .backgroundColor('#f8fafc')
            .fontColor('#374151')
            .borderRadius(8)
            .padding(8);
        }
        .layoutWeight(1);
      }

      Row({ space: 12 }) {
        Button('取消')
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .layoutWeight(1)
          .onClick(() => {
            this.showCourseDialog = false;
            this.resetCourseForm();
          });

        Button('确定')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .layoutWeight(1)
          .onClick(() => {
            this.addNewCourse();
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
  getFilteredCourses(): Course[] {
    return this.courses.filter(course => {
      const matchesSearch = course.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
                           course.description.toLowerCase().includes(this.searchText.toLowerCase());
      const matchesCategory = this.selectedCategory === '全部' || course.category === this.selectedCategory;
      return matchesSearch && matchesCategory;
    });
  }

  editCourse(course: Course) {
    this.newCourseName = course.name;
    this.newCourseDescription = course.description;
    this.newCourseCredit = course.credit;
    this.newCourseCategory = course.category;
    this.showCourseDialog = true;
  }

  deleteCourse(courseId: string) {
    this.courses = this.courses.filter(course => course.id !== courseId);
  }

  showCourseFilterDialog() {
    console.log('显示课程筛选对话框');
  }

  addNewCourse() {
    if (this.newCourseName.trim() === '') {
      return;
    }

    const newCourse: Course = {
      id: Date.now().toString(),
      name: this.newCourseName,
      teacher: '当前教师',
      credit: this.newCourseCredit,
      category: this.newCourseCategory,
      description: this.newCourseDescription,
      createTime: new Date().toLocaleDateString(),
      studentCount: 0,
      status: '进行中'
    };

    this.courses.push(newCourse);
    this.resetCourseForm();
    this.showCourseDialog = false;
  }

  resetCourseForm() {
    this.newCourseName = '';
    this.newCourseDescription = '';
    this.newCourseCredit = 3;
    this.newCourseCategory = '专业课';
  }
}