import { Quiz, QuizQuestion } from '../interfaces/StudentInterfaces';

// 移除了不再使用的接口定义，改为直接的条件判断

// 测验状态枚举
enum QuizState {
  LIST = 'list',
  ACTIVE = 'active',
  RESULT = 'result'
}

// 分数等级枚举
enum ScoreLevel {
  EXCELLENT = 'excellent',
  GOOD = 'good',
  AVERAGE = 'average',
  PASS = 'pass',
  FAIL = 'fail'
}

@Component
export struct QuizManagement {
  @Link quizzes: Quiz[];
  @State currentQuiz: Quiz | null = null;
  @State currentQuestionIndex: number = 0;
  @State userAnswers: (string | string[])[] = [];
  @State quizTimeRemaining: number = 0;
  @State quizTimer: number = -1;
  @State currentScore: number = 0;
  @State searchText: string = '';
  @State selectedDifficulty: string = '全部';
  @State quizState: QuizState = QuizState.LIST;

  // 常量定义
  private readonly DIFFICULTIES: string[] = ['全部', 'easy', 'medium', 'hard'];
  private readonly WARNING_TIME_THRESHOLD: number = 300; // 5分钟警告阈值
  private readonly TIMER_INTERVAL: number = 1000; // 计时器间隔

  /**
   * 组件构建方法
   */
  build() {
    Column({ space: 20 }) {
      this.renderCurrentView();
    }
    .width('100%')
    .padding(16);
  }

  /**
   * 根据当前状态渲染对应视图
   */
  @Builder
  renderCurrentView() {
    if (this.quizState === QuizState.ACTIVE) {
      if (this.currentQuiz) {
        this.buildQuizInterface();
      }
    } else if (this.quizState === QuizState.RESULT) {
      this.buildQuizResult();
    } else {
      this.buildQuizList();
    }
  }

  /**
   * 构建测验列表界面
   */
  @Builder
  buildQuizList() {
    Column({ space: 20 }) {
      this.buildHeader();
      this.buildSearchAndFilter();
      this.buildDifficultyFilter();
      this.buildQuizCards();
    }
  }

  /**
   * 构建页面头部
   */
  @Builder
  buildHeader() {
    Row({ space: 16 }) {
      Text('📝 在线测验')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1f2937')
        .layoutWeight(1);

      Button('📊 成绩统计')
        .backgroundColor('#10b981')
        .fontColor(Color.White)
        .borderRadius(8)
        .padding({ left: 16, right: 16, top: 8, bottom: 8 })
        .onClick(() => this.handleShowScoreStats());
    }
  }

  /**
   * 构建搜索和筛选区域
   */
  @Builder
  buildSearchAndFilter() {
    Row({ space: 16 }) {
      TextInput({ placeholder: '🔍 搜索测验...', text: this.searchText })
        .onChange(value => this.handleSearchTextChange(value))
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
        .fontSize(16)
        .onClick(() => this.handleShowFilter());
    }
  }

  /**
   * 构建难度筛选器
   */
  @Builder
  buildDifficultyFilter() {
    Scroll() {
      Row({ space: 12 }) {
        ForEach(this.DIFFICULTIES, (difficulty: string) => {
          this.buildDifficultyButton(difficulty);
        });
      }
      .padding({ left: 4, right: 4 });
    }
    .scrollable(ScrollDirection.Horizontal)
    .scrollBar(BarState.Off);
  }

  /**
   * 构建难度按钮
   */
  @Builder
  buildDifficultyButton(difficulty: string) {
    Button(this.getDifficultyLabel(difficulty))
      .backgroundColor(this.selectedDifficulty === difficulty ? '#6366f1' : '#ffffff')
      .fontColor(this.selectedDifficulty === difficulty ? Color.White : '#6b7280')
      .borderRadius(24)
      .padding({ left: 16, right: 16, top: 8, bottom: 8 })
      .fontSize(15)
      .border({
        width: 1,
        color: this.selectedDifficulty === difficulty ? '#6366f1' : '#e5e7eb'
      })
      .onClick(() => this.handleDifficultyChange(difficulty));
  }

  /**
   * 构建测验卡片列表
   */
  @Builder
  buildQuizCards() {
    ForEach(this.getFilteredQuizzes(), (quiz: Quiz) => {
      this.buildQuizCard(quiz);
    });
  }

  /**
   * 构建单个测验卡片
   */
  @Builder
  buildQuizCard(quiz: Quiz) {
    Column({ space: 16 }) {
      this.buildQuizCardHeader(quiz);
      this.buildQuizCardFooter(quiz);
    }
    .width('100%')
    .padding(20)
    .backgroundColor('#ffffff')
    .borderRadius(16)
    .shadow({ radius: 6, color: '#00000012', offsetX: 0, offsetY: 3 })
    .border({ width: 1, color: '#f3f4f6' });
  }

  /**
   * 构建测验卡片头部
   */
  @Builder
  buildQuizCardHeader(quiz: Quiz) {
    Row({ space: 16 }) {
      Column({ space: 8 }) {
        Text(quiz.title)
          .fontSize(19)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .lineHeight(26);

        this.buildQuizTags(quiz);
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1);

      this.buildScoreDisplay(quiz);
    }
  }

  /**
   * 构建测验标签
   */
  @Builder
  buildQuizTags(quiz: Quiz) {
    Row({ space: 8 }) {
      Text(`${quiz.questions.length}题`)
        .fontSize(14)
        .fontColor('#6366f1')
        .backgroundColor('#eef2ff')
        .padding({ left: 8, right: 8, top: 4, bottom: 4 })
        .borderRadius(6);

      Text(`${quiz.timeLimit}分钟`)
        .fontSize(14)
        .fontColor('#f59e0b')
        .backgroundColor('#fef3c7')
        .padding({ left: 8, right: 8, top: 4, bottom: 4 })
        .borderRadius(6);
    }
  }

  /**
   * 构建分数显示
   */
  @Builder
  buildScoreDisplay(quiz: Quiz) {
    Column({ space: 4 }) {
      Text(`${quiz.bestScore}分`)
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor(this.getScoreColor(quiz.bestScore))
        .textAlign(TextAlign.Center);

      Text('最高分')
        .fontSize(12)
        .fontColor('#9ca3af')
        .textAlign(TextAlign.Center);
    }
    .alignItems(HorizontalAlign.Center);
  }

  /**
   * 构建测验卡片底部
   */
  @Builder
  buildQuizCardFooter(quiz: Quiz) {
    Row({ space: 16 }) {
      Text(`📊 已答：${quiz.attempts}次`)
        .fontSize(14)
        .fontColor('#6b7280')
        .padding({ left: 8, right: 8, top: 4, bottom: 4 })
        .backgroundColor('#f9fafb')
        .borderRadius(6);

      Blank();

      Button('开始测验')
        .backgroundColor('#6366f1')
        .fontColor(Color.White)
        .borderRadius(10)
        .padding({ left: 16, right: 16, top: 8, bottom: 8 })
        .fontSize(15)
        .onClick(() => this.handleStartQuiz(quiz));
    }
  }

  /**
   * 构建测验界面
   */
  @Builder
  buildQuizInterface() {
    Column({ space: 20 }) {
      this.buildQuizHeader();
      this.buildProgressBar();
      this.buildCurrentQuestion();
      this.buildNavigationButtons();
    }
  }

  /**
   * 构建测验头部
   */
  @Builder
  buildQuizHeader() {
    Row({ space: 16 }) {
      this.buildQuizInfo();
      this.buildTimeDisplay();
    }
  }

  /**
   * 构建测验信息
   */
  @Builder
  buildQuizInfo() {
    Column({ space: 4 }) {
      Text(this.currentQuiz ? this.currentQuiz.title : '')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1f2937');

      Text(`第 ${this.currentQuestionIndex + 1} 题 / 共 ${this.getCurrentQuizQuestionCount()} 题`)
        .fontSize(14)
        .fontColor('#6b7280');
    }
    .alignItems(HorizontalAlign.Start)
    .layoutWeight(1);
  }

  /**
   * 构建时间显示
   */
  @Builder
  buildTimeDisplay() {
    Column({ space: 4 }) {
      Text(this.formatTime(this.quizTimeRemaining))
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor(this.getTimeColor())
        .textAlign(TextAlign.Center);

      Text('剩余时间')
        .fontSize(12)
        .fontColor('#9ca3af')
        .textAlign(TextAlign.Center);
    }
    .alignItems(HorizontalAlign.Center);
  }

  /**
   * 构建进度条
   */
  @Builder
  buildProgressBar() {
    Progress({
      value: this.currentQuestionIndex + 1,
      total: this.getCurrentQuizQuestionCount(),
      type: ProgressType.Linear
    })
      .color('#6366f1')
      .backgroundColor('#e5e7eb')
      .width('100%')
      .height(8);
  }

  /**
   * 构建当前题目
   */
  @Builder
  buildCurrentQuestion() {
    if (this.currentQuiz && this.isValidQuestionIndex()) {
      this.buildQuestionCard(this.currentQuiz.questions[this.currentQuestionIndex]);
    }
  }

  /**
   * 构建导航按钮
   */
  @Builder
  buildNavigationButtons() {
    Row({ space: 16 }) {
      this.buildPreviousButton();
      Blank();
      this.buildNextOrSubmitButton();
    }
  }

  /**
   * 构建上一题按钮
   */
  @Builder
  buildPreviousButton() {
    Button('上一题')
      .backgroundColor('#f3f4f6')
      .fontColor('#6b7280')
      .borderRadius(10)
      .padding({ left: 20, right: 20, top: 12, bottom: 12 })
      .enabled(this.currentQuestionIndex > 0)
      .onClick(() => this.handlePreviousQuestion());
  }

  /**
   * 构建下一题或提交按钮
   */
  @Builder
  buildNextOrSubmitButton() {
    if (this.isLastQuestion()) {
      Button('提交测验')
        .backgroundColor('#10b981')
        .fontColor(Color.White)
        .borderRadius(10)
        .padding({ left: 20, right: 20, top: 12, bottom: 12 })
        .onClick(() => this.handleSubmitQuiz());
    } else {
      Button('下一题')
        .backgroundColor('#6366f1')
        .fontColor(Color.White)
        .borderRadius(10)
        .padding({ left: 20, right: 20, top: 12, bottom: 12 })
        .onClick(() => this.handleNextQuestion());
    }
  }

  /**
   * 构建题目卡片
   */
  @Builder
  buildQuestionCard(question: QuizQuestion) {
    Column({ space: 20 }) {
      this.buildQuestionContent(question);
      this.buildQuestionOptions(question);
    }
    .width('100%')
    .padding(20)
    .backgroundColor('#ffffff')
    .borderRadius(16)
    .shadow({ radius: 6, color: '#00000012', offsetX: 0, offsetY: 3 })
    .border({ width: 1, color: '#f3f4f6' });
  }

  /**
   * 构建题目内容
   */
  @Builder
  buildQuestionContent(question: QuizQuestion) {
    Column({ space: 12 }) {
      this.buildQuestionTags(question);

      Text(question.question)
        .fontSize(18)
        .fontColor('#1f2937')
        .lineHeight(26)
        .fontWeight(FontWeight.Medium);
    }
  }

  /**
   * 构建题目标签
   */
  @Builder
  buildQuestionTags(question: QuizQuestion) {
    Row({ space: 8 }) {
      Text(this.getDifficultyLabel(question.difficulty))
        .fontSize(12)
        .fontColor(this.getDifficultyColor(question.difficulty))
        .backgroundColor(this.getDifficultyBgColor(question.difficulty))
        .padding({ left: 8, right: 8, top: 4, bottom: 4 })
        .borderRadius(6);

      Text(question.type === 'choice' ? '单选题' : '多选题')
        .fontSize(12)
        .fontColor('#6366f1')
        .backgroundColor('#eef2ff')
        .padding({ left: 8, right: 8, top: 4, bottom: 4 })
        .borderRadius(6);
    }
  }

  /**
   * 构建题目选项
   */
  @Builder
  buildQuestionOptions(question: QuizQuestion) {
    if (question.type === 'choice') {
      this.buildChoiceOptions(question);
    } else if (question.type === 'multiple') {
      this.buildMultipleOptions(question);
    }
  }

  /**
   * 构建单选题选项
   */
  @Builder
  buildChoiceOptions(question: QuizQuestion) {
    Column({ space: 12 }) {
      ForEach(this.getQuestionOptions(question), (option: string, index: number) => {
        this.buildChoiceOption(option, index);
      });
    }
  }

  /**
   * 构建单选选项
   */
  @Builder
  buildChoiceOption(option: string, index: number) {
    Row({ space: 12 }) {
      Radio({ value: option, group: 'quiz_options' })
        .checked(this.isOptionSelected(option))
        .onChange((isChecked: boolean) => {
          if (isChecked) {
            this.handleSelectAnswer(option);
          }
        });

      Text(`${String.fromCharCode(65 + index)}. ${option}`)
        .fontSize(16)
        .fontColor('#374151')
        .lineHeight(24)
        .layoutWeight(1)
        .onClick(() => this.handleSelectAnswer(option));
    }
    .width('100%')
    .padding({ left: 12, right: 12, top: 12, bottom: 12 })
    .backgroundColor(this.isOptionSelected(option) ? '#eef2ff' : '#ffffff')
    .borderRadius(12)
    .border({
      width: 2,
      color: this.isOptionSelected(option) ? '#6366f1' : '#e5e7eb'
    })
    .onClick(() => this.handleSelectAnswer(option));
  }

  /**
   * 构建多选题选项
   */
  @Builder
  buildMultipleOptions(question: QuizQuestion) {
    Column({ space: 12 }) {
      ForEach(this.getQuestionOptions(question), (option: string, index: number) => {
        this.buildMultipleOption(option, index);
      });
    }
  }

  /**
   * 构建多选选项
   */
  @Builder
  buildMultipleOption(option: string, index: number) {
    Row({ space: 12 }) {
      Checkbox({ name: option, group: 'quiz_multiple' })
        .select(this.isMultipleOptionSelected(option))
        .onChange((value: boolean) => {
          this.handleToggleMultipleAnswer(option, value);
        });

      Text(`${String.fromCharCode(65 + index)}. ${option}`)
        .fontSize(16)
        .fontColor('#374151')
        .lineHeight(24)
        .layoutWeight(1)
        .onClick(() => {
          const isSelected = this.isMultipleOptionSelected(option);
          this.handleToggleMultipleAnswer(option, !isSelected);
        });
    }
    .width('100%')
    .padding({ left: 12, right: 12, top: 12, bottom: 12 })
    .backgroundColor(this.isMultipleOptionSelected(option) ? '#eef2ff' : '#ffffff')
    .borderRadius(12)
    .border({
      width: 2,
      color: this.isMultipleOptionSelected(option) ? '#6366f1' : '#e5e7eb'
    })
    .onClick(() => {
      const isSelected = this.isMultipleOptionSelected(option);
      this.handleToggleMultipleAnswer(option, !isSelected);
    });
  }

  /**
   * 构建测验结果界面
   */
  @Builder
  buildQuizResult() {
    Column({ space: 24 }) {
      this.buildResultTitle();
      this.buildScoreSection();
      this.buildStatisticsSection();
      this.buildActionButtons();
    }
    .width('100%')
    .padding(20);
  }

  /**
   * 构建结果标题
   */
  @Builder
  buildResultTitle() {
    Text('🎉 测验完成')
      .fontSize(24)
      .fontWeight(FontWeight.Bold)
      .fontColor('#1f2937')
      .textAlign(TextAlign.Center);
  }

  /**
   * 构建分数区域
   */
  @Builder
  buildScoreSection() {
    Column({ space: 16 }) {
      Text(`${this.currentScore}`)
        .fontSize(48)
        .fontWeight(FontWeight.Bold)
        .fontColor(this.getScoreColor(this.currentScore))
        .textAlign(TextAlign.Center);

      Text('分')
        .fontSize(20)
        .fontColor('#6b7280')
        .textAlign(TextAlign.Center);

      Text(this.getScoreLevelText(this.currentScore))
        .fontSize(18)
        .fontColor(this.getScoreColor(this.currentScore))
        .textAlign(TextAlign.Center)
        .fontWeight(FontWeight.Medium);
    }
    .width('100%')
    .padding(32)
    .backgroundColor('#ffffff')
    .borderRadius(20)
    .shadow({ radius: 8, color: '#00000015', offsetX: 0, offsetY: 4 });
  }

  /**
   * 构建统计信息区域
   */
  @Builder
  buildStatisticsSection() {
    Row({ space: 20 }) {
      this.buildStatItem('总题数', `${this.getCurrentQuizQuestionCount()}`, '#6366f1');
      this.buildStatItem('答对题数', `${this.getCorrectCount()}`, '#10b981');
      this.buildStatItem('正确率', `${this.getAccuracyRate()}%`, '#f59e0b');
    }
    .width('100%')
    .padding(20)
    .backgroundColor('#ffffff')
    .borderRadius(16)
    .shadow({ radius: 6, color: '#00000012', offsetX: 0, offsetY: 3 });
  }

  /**
   * 构建统计项
   */
  @Builder
  buildStatItem(label: string, value: string, color: string) {
    Column({ space: 8 }) {
      Text(value)
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor(color)
        .textAlign(TextAlign.Center);

      Text(label)
        .fontSize(14)
        .fontColor('#6b7280')
        .textAlign(TextAlign.Center);
    }
    .layoutWeight(1);
  }

  /**
   * 构建操作按钮
   */
  @Builder
  buildActionButtons() {
    Column({ space: 12 }) {
      Button('查看答案解析')
        .backgroundColor('#6366f1')
        .fontColor(Color.White)
        .borderRadius(12)
        .padding({ left: 24, right: 24, top: 14, bottom: 14 })
        .width('100%')
        .fontSize(16)
        .onClick(() => this.handleViewAnswers());

      Button('重新测验')
        .backgroundColor('#10b981')
        .fontColor(Color.White)
        .borderRadius(12)
        .padding({ left: 24, right: 24, top: 14, bottom: 14 })
        .width('100%')
        .fontSize(16)
        .onClick(() => this.handleRestartQuiz());

      Button('返回测验列表')
        .backgroundColor('#f3f4f6')
        .fontColor('#6b7280')
        .borderRadius(12)
        .padding({ left: 24, right: 24, top: 14, bottom: 14 })
        .width('100%')
        .fontSize(16)
        .onClick(() => this.handleExitQuiz());
    }
  }

  // ==================== 事件处理方法 ====================

  /**
   * 处理搜索文本变化
   */
  private handleSearchTextChange(value: string): void {
    this.searchText = value;
  }

  /**
   * 处理显示筛选
   */
  private handleShowFilter(): void {
    // TODO: 实现筛选功能
    console.log('显示筛选选项');
  }

  /**
   * 处理难度变化
   */
  private handleDifficultyChange(difficulty: string): void {
    this.selectedDifficulty = difficulty;
  }

  /**
   * 处理开始测验
   */
  private handleStartQuiz(quiz: Quiz): void {
    this.initializeQuiz(quiz);
    this.quizState = QuizState.ACTIVE;
    this.startQuizTimer();
  }

  /**
   * 处理显示成绩统计
   */
  private handleShowScoreStats(): void {
    // TODO: 实现成绩统计功能
    console.log('显示成绩统计');
  }

  /**
   * 处理选择答案
   */
  private handleSelectAnswer(answer: string): void {
    this.userAnswers[this.currentQuestionIndex] = answer;
  }

  /**
   * 处理切换多选答案
   */
  private handleToggleMultipleAnswer(answer: string, selected: boolean): void {
    let currentAnswers = this.getCurrentMultipleAnswers();

    if (selected) {
      if (!currentAnswers.includes(answer)) {
        currentAnswers.push(answer);
      }
    } else {
      currentAnswers = currentAnswers.filter(a => a !== answer);
    }

    this.userAnswers[this.currentQuestionIndex] = currentAnswers;
  }

  /**
   * 处理上一题
   */
  private handlePreviousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  /**
   * 处理下一题
   */
  private handleNextQuestion(): void {
    if (!this.isLastQuestion()) {
      this.currentQuestionIndex++;
    }
  }

  /**
   * 处理提交测验
   */
  private handleSubmitQuiz(): void {
    this.stopQuizTimer();
    this.calculateAndSetScore();
    this.updateQuizStatistics();
    this.quizState = QuizState.RESULT;
  }

  /**
   * 处理查看答案
   */
  private handleViewAnswers(): void {
    // TODO: 实现答案解析功能
    console.log('查看答案解析');
  }

  /**
   * 处理重新测验
   */
  private handleRestartQuiz(): void {
    if (this.currentQuiz) {
      this.handleStartQuiz(this.currentQuiz);
    }
  }

  /**
   * 处理退出测验
   */
  private handleExitQuiz(): void {
    this.resetQuizState();
    this.quizState = QuizState.LIST;
  }

  // ==================== 核心业务方法 ====================

  /**
   * 初始化测验
   */
  private initializeQuiz(quiz: Quiz): void {
    this.currentQuiz = quiz;
    this.currentQuestionIndex = 0;
    this.userAnswers = new Array(quiz.questions.length).fill(null);
    this.quizTimeRemaining = quiz.timeLimit * 60; // 转换为秒
    this.currentScore = 0;
  }

  /**
   * 重置测验状态
   */
  private resetQuizState(): void {
    this.currentQuiz = null;
    this.currentQuestionIndex = 0;
    this.userAnswers = [];
    this.quizTimeRemaining = 0;
    this.currentScore = 0;
    this.stopQuizTimer();
  }

  /**
   * 启动测验计时器
   */
  private startQuizTimer(): void {
    this.scheduleQuizTick();
  }

  /**
   * 调度测验计时
   */
  private scheduleQuizTick(): void {
    this.quizTimer = setTimeout(() => {
      this.quizTimeRemaining--;
      if (this.quizTimeRemaining <= 0) {
        this.handleSubmitQuiz();
      } else {
        this.scheduleQuizTick();
      }
    }, this.TIMER_INTERVAL);
  }

  /**
   * 停止测验计时器
   */
  private stopQuizTimer(): void {
    if (this.quizTimer !== -1) {
      clearTimeout(this.quizTimer);
      this.quizTimer = -1;
    }
  }

  /**
   * 计算并设置分数
   */
  private calculateAndSetScore(): void {
    this.currentScore = this.calculateScore();
  }

  /**
   * 计算分数
   */
  private calculateScore(): number {
    if (!this.currentQuiz) return 0;

    const correctCount = this.getCorrectCount();
    return Math.round((correctCount / this.currentQuiz.questions.length) * 100);
  }

  /**
   * 获取正确答案数量
   */
  private getCorrectCount(): number {
    if (!this.currentQuiz) return 0;

    let correctCount = 0;

    for (let i = 0; i < this.currentQuiz.questions.length; i++) {
      const question = this.currentQuiz.questions[i];
      const userAnswer = this.userAnswers[i];

      if (this.isAnswerCorrect(question, userAnswer)) {
        correctCount++;
      }
    }

    return correctCount;
  }

  /**
   * 判断答案是否正确
   */
  private isAnswerCorrect(question: QuizQuestion, userAnswer: string | string[]): boolean {
    if (question.type === 'choice') {
      return userAnswer === question.correctAnswer;
    } else if (question.type === 'multiple') {
      const correctAnswers = question.correctAnswer as string[];
      const userAnswerArray = this.ensureStringArray(userAnswer);

      return correctAnswers.length === userAnswerArray.length &&
      correctAnswers.every(answer => userAnswerArray.includes(answer));
    }

    return false;
  }

  /**
   * 更新测验统计信息
   */
  private updateQuizStatistics(): void {
    if (!this.currentQuiz) return;

    // 更新最高分
    if (this.currentScore > this.currentQuiz.bestScore) {
      this.currentQuiz.bestScore = this.currentScore;
    }

    // 增加尝试次数
    this.currentQuiz.attempts++;
  }

  // ==================== 工具方法 ====================

  /**
   * 获取筛选后的测验列表
   */
  private getFilteredQuizzes(): Quiz[] {
    return this.quizzes.filter(quiz => {
      const matchesSearch = this.matchesSearchCriteria(quiz);
      const matchesDifficulty = this.matchesDifficultyCriteria(quiz);
      return matchesSearch && matchesDifficulty;
    });
  }

  /**
   * 检查是否匹配搜索条件
   */
  private matchesSearchCriteria(quiz: Quiz): boolean {
    return quiz.title.toLowerCase().includes(this.searchText.toLowerCase());
  }

  /**
   * 检查是否匹配难度条件
   */
  private matchesDifficultyCriteria(quiz: Quiz): boolean {
    if (this.selectedDifficulty === '全部') {
      return true;
    }
    return quiz.questions.some(q => q.difficulty === this.selectedDifficulty);
  }

  /**
   * 获取难度标签
   */
  private getDifficultyLabel(difficulty: string): string {
    if (difficulty === '全部') return '全部';
    if (difficulty === 'easy') return '简单';
    if (difficulty === 'medium') return '中等';
    if (difficulty === 'hard') return '困难';
    return difficulty;
  }

  /**
   * 获取难度颜色
   */
  private getDifficultyColor(difficulty: string): string {
    if (difficulty === 'easy') return '#10b981';
    if (difficulty === 'medium') return '#f59e0b';
    if (difficulty === 'hard') return '#ef4444';
    return '#6b7280';
  }

  /**
   * 获取难度背景颜色
   */
  private getDifficultyBgColor(difficulty: string): string {
    if (difficulty === 'easy') return '#d1fae5';
    if (difficulty === 'medium') return '#fef3c7';
    if (difficulty === 'hard') return '#fee2e2';
    return '#f3f4f6';
  }

  /**
   * 获取分数颜色
   */
  private getScoreColor(score: number): string {
    if (score >= 90) return '#10b981'; // 优秀 - 绿色
    if (score >= 80) return '#3b82f6'; // 良好 - 蓝色
    if (score >= 70) return '#f59e0b'; // 中等 - 橙色
    if (score >= 60) return '#eab308'; // 及格 - 黄色
    return '#ef4444'; // 不及格 - 红色
  }

  /**
   * 获取分数等级文本
   */
  private getScoreLevelText(score: number): string {
    if (score >= 90) return '优秀';
    if (score >= 80) return '良好';
    if (score >= 70) return '中等';
    if (score >= 60) return '及格';
    return '不及格';
  }

  /**
   * 获取时间颜色
   */
  private getTimeColor(): string {
    return this.quizTimeRemaining <= this.WARNING_TIME_THRESHOLD ? '#ef4444' : '#6366f1';
  }

  /**
   * 格式化时间
   */
  private formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
  }

  /**
   * 获取当前测验题目数量
   */
  private getCurrentQuizQuestionCount(): number {
    return this.currentQuiz ? this.currentQuiz.questions.length : 0;
  }

  /**
   * 检查是否为有效题目索引
   */
  private isValidQuestionIndex(): boolean {
    return this.currentQuestionIndex < this.getCurrentQuizQuestionCount();
  }

  /**
   * 检查是否为最后一题
   */
  private isLastQuestion(): boolean {
    return this.currentQuestionIndex >= this.getCurrentQuizQuestionCount() - 1;
  }

  /**
   * 获取题目选项
   */
  private getQuestionOptions(question: QuizQuestion): string[] {
    return question.options ? question.options : [];
  }

  /**
   * 检查选项是否被选中
   */
  private isOptionSelected(option: string): boolean {
    return this.userAnswers[this.currentQuestionIndex] === option;
  }

  /**
   * 检查多选选项是否被选中
   */
  private isMultipleOptionSelected(option: string): boolean {
    const currentAnswers = this.getCurrentMultipleAnswers();
    return currentAnswers.includes(option);
  }

  /**
   * 获取当前多选答案
   */
  private getCurrentMultipleAnswers(): string[] {
    const answer = this.userAnswers[this.currentQuestionIndex];
    return this.ensureStringArray(answer);
  }

  /**
   * 确保返回字符串数组
   */
  private ensureStringArray(value: string | string[]): string[] {
    if (Array.isArray(value)) {
      return value;
    }
    return [];
  }

  /**
   * 获取正确率
   */
  private getAccuracyRate(): string {
    const total = this.getCurrentQuizQuestionCount();
    if (total === 0) return '0.0';

    const correct = this.getCorrectCount();
    return ((correct / total) * 100).toFixed(1);
  }
}