import { QuestionBank } from '../interfaces/TeacherInterfaces';

@Component
export struct QuestionBankManagement {
  @Link questionBank: QuestionBank[];
  @State showQuestionDialog: boolean = false;
  @State newQuestionSubject: string = '';
  @State newQuestionDifficulty: string = 'easy';
  @State newQuestionType: string = 'choice';
  @State newQuestionContent: string = '';
  @State newQuestionOptions: string[] = ['', '', '', ''];
  @State newQuestionAnswer: string = '';
  @State newQuestionKnowledgePoints: string = '';
  @State searchText: string = '';
  @State selectedDifficulty: string = '全部';
  @State selectedType: string = '全部';

  private difficulties: string[] = ['全部', 'easy', 'medium', 'hard'];
  private questionTypes: string[] = ['全部', 'choice', 'multiple', 'short_answer', 'programming'];

  build() {
    Column({ space: 20 }) {
      // 题库管理标题和操作
      Row({ space: 16 }) {
        Text('📚 题库管理')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b')
          .layoutWeight(1);

        Button('➕ 新建题目')
          .backgroundColor('#10b981')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .onClick(() => {
            this.showQuestionDialog = true;
          });
      }

      // 搜索和筛选
      Row({ space: 12 }) {
        TextInput({ placeholder: '🔍 搜索题目...', text: this.searchText })
          .onChange(v => this.searchText = v)
          .layoutWeight(1)
          .backgroundColor('#ffffff')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 });

        Button('导入题目')
          .backgroundColor('#f1f5f9')
          .fontColor('#64748b')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 })
          .onClick(() => {
            this.importQuestions();
          });
      }

      // 难度筛选
      Row({ space: 8 }) {
        Text('难度：')
          .fontSize(14)
          .fontColor('#6b7280');
        
        ForEach(this.difficulties, (difficulty: string) => {
          Button(this.getDifficultyText(difficulty))
            .backgroundColor(this.selectedDifficulty === difficulty ? '#6366f1' : '#ffffff')
            .fontColor(this.selectedDifficulty === difficulty ? Color.White : '#6b7280')
            .borderRadius(20)
            .padding({ left: 12, right: 12, top: 6, bottom: 6 })
            .fontSize(14)
            .border({ width: 1, color: this.selectedDifficulty === difficulty ? '#6366f1' : '#e5e7eb' })
            .onClick(() => {
              this.selectedDifficulty = difficulty;
            });
        });
      }

      // 题型筛选
      Row({ space: 8 }) {
        Text('题型：')
          .fontSize(14)
          .fontColor('#6b7280');
        
        ForEach(this.questionTypes, (type: string) => {
          Button(this.getTypeText(type))
            .backgroundColor(this.selectedType === type ? '#6366f1' : '#ffffff')
            .fontColor(this.selectedType === type ? Color.White : '#6b7280')
            .borderRadius(20)
            .padding({ left: 12, right: 12, top: 6, bottom: 6 })
            .fontSize(14)
            .border({ width: 1, color: this.selectedType === type ? '#6366f1' : '#e5e7eb' })
            .onClick(() => {
              this.selectedType = type;
            });
        });
      }

      // 题目列表
      ForEach(this.getFilteredQuestions(), (question: QuestionBank) => {
        this.buildQuestionCard(question);
      });

      // 新建题目对话框
      if (this.showQuestionDialog) {
        this.buildQuestionDialog();
      }
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildQuestionCard(question: QuestionBank) {
    Column({ space: 12 }) {
      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Row({ space: 8 }) {
            Text(this.getTypeIcon(question.type))
              .fontSize(16);
            
            Text(question.subject)
              .fontSize(16)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1e293b');
          }

          Row({ space: 8 }) {
            Text(this.getDifficultyText(question.difficulty))
              .fontSize(12)
              .fontColor(this.getDifficultyColor(question.difficulty))
              .backgroundColor(this.getDifficultyBgColor(question.difficulty))
              .padding({ left: 8, right: 8, top: 4, bottom: 4 })
              .borderRadius(12);
            
            Text(`使用${question.usageCount}次`)
              .fontSize(12)
              .fontColor('#6b7280')
              .backgroundColor('#f8fafc')
              .padding({ left: 8, right: 8, top: 4, bottom: 4 })
              .borderRadius(12);
          }
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text(`${Math.round(question.successRate * 100)}%`)
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor(question.successRate >= 0.8 ? '#10b981' : question.successRate >= 0.6 ? '#f59e0b' : '#ef4444');
          
          Text('正确率')
            .fontSize(12)
            .fontColor('#9ca3af');
        }
        .alignItems(HorizontalAlign.Center);
      }

      Text(question.question)
        .fontSize(15)
        .fontColor('#374151')
        .lineHeight(22)
        .maxLines(3)
        .textOverflow({ overflow: TextOverflow.Ellipsis });

      // 选项显示（仅对选择题）
      if (question.type === 'choice' || question.type === 'multiple') {
        Column({ space: 6 }) {
          ForEach(question.options, (option: string, index: number) => {
            if (option.trim() !== '') {
              Row({ space: 8 }) {
                Text(String.fromCharCode(65 + index) + '.')
                  .fontSize(14)
                  .fontColor('#6b7280')
                  .width(20);
                
                Text(option)
                  .fontSize(14)
                  .fontColor('#374151')
                  .layoutWeight(1);
              }
            }
          });
        }
        .alignItems(HorizontalAlign.Start)
        .width('100%')
        .padding({ left: 12, top: 8, bottom: 8 })
        .backgroundColor('#f8fafc')
        .borderRadius(8);
      }

      // 知识点标签
      if (question.knowledgePoints.length > 0) {
        Row({ space: 6 }) {
          Text('🏷️')
            .fontSize(12);
          
          ForEach(question.knowledgePoints.slice(0, 3), (point: string) => {
            Text(point)
              .fontSize(12)
              .fontColor('#6366f1')
              .backgroundColor('#eef2ff')
              .padding({ left: 6, right: 6, top: 2, bottom: 2 })
              .borderRadius(8);
          });
          
          if (question.knowledgePoints.length > 3) {
            Text(`+${question.knowledgePoints.length - 3}`)
              .fontSize(12)
              .fontColor('#9ca3af');
          }
        }
      }

      Row({ space: 12 }) {
        Blank();

        Button('编辑')
          .backgroundColor('#f59e0b')
          .fontColor(Color.White)
          .borderRadius(6)
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .fontSize(12)
          .onClick(() => {
            this.editQuestion(question);
          });

        Button('删除')
          .backgroundColor('#ef4444')
          .fontColor(Color.White)
          .borderRadius(6)
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .fontSize(12)
          .onClick(() => {
            this.deleteQuestion(question.id);
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
  buildQuestionDialog() {
    Column({ space: 16 }) {
      Text('新建题目')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1e293b');

      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Text('学科')
            .fontSize(14)
            .fontColor('#374151');
          
          TextInput({ placeholder: '如：HarmonyOS开发', text: this.newQuestionSubject })
            .onChange(v => this.newQuestionSubject = v)
            .backgroundColor('#f8fafc')
            .borderRadius(8)
            .padding(8);
        }
        .layoutWeight(1);

        Column({ space: 4 }) {
          Text('难度')
            .fontSize(14)
            .fontColor('#374151');
          
          Button(this.getDifficultyText(this.newQuestionDifficulty))
            .backgroundColor('#f8fafc')
            .fontColor('#374151')
            .borderRadius(8)
            .padding(8)
            .onClick(() => {
              this.showDifficultySelector();
            });
        }
        .layoutWeight(1);
      }

      Column({ space: 8 }) {
        Text('题型')
          .fontSize(14)
          .fontColor('#374151')
          .alignSelf(ItemAlign.Start);
        
        Row({ space: 8 }) {
          Button('单选题')
            .backgroundColor(this.newQuestionType === 'choice' ? '#6366f1' : '#f8fafc')
            .fontColor(this.newQuestionType === 'choice' ? Color.White : '#374151')
            .borderRadius(8)
            .padding({ left: 12, right: 12, top: 8, bottom: 8 })
            .fontSize(14)
            .onClick(() => {
              this.newQuestionType = 'choice';
            });
          
          Button('多选题')
            .backgroundColor(this.newQuestionType === 'multiple' ? '#6366f1' : '#f8fafc')
            .fontColor(this.newQuestionType === 'multiple' ? Color.White : '#374151')
            .borderRadius(8)
            .padding({ left: 12, right: 12, top: 8, bottom: 8 })
            .fontSize(14)
            .onClick(() => {
              this.newQuestionType = 'multiple';
            });
          
          Button('简答题')
            .backgroundColor(this.newQuestionType === 'short_answer' ? '#6366f1' : '#f8fafc')
            .fontColor(this.newQuestionType === 'short_answer' ? Color.White : '#374151')
            .borderRadius(8)
            .padding({ left: 12, right: 12, top: 8, bottom: 8 })
            .fontSize(14)
            .onClick(() => {
              this.newQuestionType = 'short_answer';
            });
        }
      }

      TextArea({ placeholder: '请输入题目内容...', text: this.newQuestionContent })
        .onChange(v => this.newQuestionContent = v)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .padding(12)
        .height(80);

      // 选择题选项
      if (this.newQuestionType === 'choice' || this.newQuestionType === 'multiple') {
        Column({ space: 8 }) {
          Text('选项设置')
            .fontSize(14)
            .fontColor('#374151')
            .alignSelf(ItemAlign.Start);
          
          ForEach([0, 1, 2, 3], (index: number) => {
            Row({ space: 8 }) {
              Text(String.fromCharCode(65 + index) + '.')
                .fontSize(14)
                .fontColor('#6b7280')
                .width(20);
              
              TextInput({ placeholder: `选项${String.fromCharCode(65 + index)}`, text: this.newQuestionOptions[index] })
                .onChange(v => {
                  this.newQuestionOptions[index] = v;
                })
                .backgroundColor('#f8fafc')
                .borderRadius(8)
                .padding(8)
                .layoutWeight(1);
            }
          });
        }
      }

      TextInput({ placeholder: '正确答案', text: this.newQuestionAnswer })
        .onChange(v => this.newQuestionAnswer = v)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .padding(12);

      TextInput({ placeholder: '知识点（用逗号分隔）', text: this.newQuestionKnowledgePoints })
        .onChange(v => this.newQuestionKnowledgePoints = v)
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
            this.showQuestionDialog = false;
            this.resetQuestionForm();
          });

        Button('确定')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .layoutWeight(1)
          .onClick(() => {
            this.addNewQuestion();
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
  getFilteredQuestions(): QuestionBank[] {
    return this.questionBank.filter(question => {
      const matchesSearch = question.question.toLowerCase().includes(this.searchText.toLowerCase()) ||
                           question.subject.toLowerCase().includes(this.searchText.toLowerCase());
      const matchesDifficulty = this.selectedDifficulty === '全部' || question.difficulty === this.selectedDifficulty;
      const matchesType = this.selectedType === '全部' || question.type === this.selectedType;
      return matchesSearch && matchesDifficulty && matchesType;
    });
  }

  getDifficultyText(difficulty: string): string {
    switch (difficulty) {
      case 'easy': return '简单';
      case 'medium': return '中等';
      case 'hard': return '困难';
      case '全部': return '全部';
      default: return difficulty;
    }
  }

  getDifficultyColor(difficulty: string): string {
    switch (difficulty) {
      case 'easy': return '#10b981';
      case 'medium': return '#f59e0b';
      case 'hard': return '#ef4444';
      default: return '#6b7280';
    }
  }

  getDifficultyBgColor(difficulty: string): string {
    switch (difficulty) {
      case 'easy': return '#ecfdf5';
      case 'medium': return '#fef3c7';
      case 'hard': return '#fee2e2';
      default: return '#f8fafc';
    }
  }

  getTypeText(type: string): string {
    switch (type) {
      case 'choice': return '单选题';
      case 'multiple': return '多选题';
      case 'short_answer': return '简答题';
      case 'programming': return '编程题';
      case '全部': return '全部';
      default: return type;
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'choice': return '🔘';
      case 'multiple': return '☑️';
      case 'short_answer': return '📝';
      case 'programming': return '💻';
      default: return '❓';
    }
  }

  editQuestion(question: QuestionBank) {
    this.newQuestionSubject = question.subject;
    this.newQuestionDifficulty = question.difficulty;
    this.newQuestionType = question.type;
    this.newQuestionContent = question.question;
    this.newQuestionOptions = [...question.options];
    this.newQuestionAnswer = Array.isArray(question.correctAnswer) ? 
      question.correctAnswer.join(', ') : question.correctAnswer;
    this.newQuestionKnowledgePoints = question.knowledgePoints.join(', ');
    this.showQuestionDialog = true;
  }

  deleteQuestion(questionId: string) {
    this.questionBank = this.questionBank.filter(question => question.id !== questionId);
  }

  importQuestions() {
    console.log('导入题目');
    // 实现题目导入功能
  }

  showDifficultySelector() {
    console.log('显示难度选择器');
    // 实现难度选择对话框
  }

  addNewQuestion() {
    if (this.newQuestionContent.trim() === '') {
      return;
    }

    const newQuestion: QuestionBank = {
      id: Date.now().toString(),
      subject: this.newQuestionSubject,
      difficulty: this.newQuestionDifficulty,
      type: this.newQuestionType,
      question: this.newQuestionContent,
      options: this.newQuestionOptions.filter(option => option.trim() !== ''),
      correctAnswer: this.newQuestionAnswer,
      knowledgePoints: this.newQuestionKnowledgePoints.split(',').map(k => k.trim()).filter(k => k !== ''),
      usageCount: 0,
      successRate: 0
    };

    this.questionBank.push(newQuestion);
    this.resetQuestionForm();
    this.showQuestionDialog = false;
  }

  resetQuestionForm() {
    this.newQuestionContent = '';
    this.newQuestionSubject = '';
    this.newQuestionDifficulty = 'easy';
    this.newQuestionType = 'choice';
    this.newQuestionOptions = ['', '', '', ''];
    this.newQuestionAnswer = '';
    this.newQuestionKnowledgePoints = '';
  }
}