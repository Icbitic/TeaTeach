import { VideoProgress, WatchedSegment } from '../interfaces/StudentInterfaces';

interface Video {
  id: string;
  title: string;
  category: string;
  duration: number;
  thumbnail: string;
  description: string;
}

interface VideoNote {
  time: number;
  note: string;
}

@Component
export struct VideoLearning {
  @Link videoProgresses: VideoProgress[];
  @State currentVideo: VideoProgress | null = null;
  @State isPlaying: boolean = false;
  @State currentTime: number = 0;
  @State playbackSpeed: number = 1.0;
  @State showSpeedMenu: boolean = false;
  @State showNotes: boolean = false;
  @State noteText: string = '';
  @State videoNotes: VideoNote[] = [];
  @State searchText: string = '';
  @State selectedCategory: string = '全部';
  @State showStatsDialog: boolean = false;

  private playbackTimer: number | null = null;

  private categories: string[] = ['全部', 'HarmonyOS基础', 'ArkTS语法', '组件开发', '应用架构', '性能优化'];
  private speedOptions: number[] = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0];

  // 模拟视频数据
  private videoList: Video[] = [
    {
      id: 'video1',
      title: 'HarmonyOS应用开发入门',
      category: 'HarmonyOS基础',
      duration: 1800, // 30分钟
      thumbnail: '🎬',
      description: '学习HarmonyOS应用开发的基础知识和开发环境搭建'
    },
    {
      id: 'video2',
      title: 'ArkTS语法详解',
      category: 'ArkTS语法',
      duration: 2400, // 40分钟
      thumbnail: '📚',
      description: '深入了解ArkTS语言特性和语法规则'
    },
    {
      id: 'video3',
      title: '组件化开发实战',
      category: '组件开发',
      duration: 3000, // 50分钟
      thumbnail: '🔧',
      description: '通过实际案例学习组件化开发技巧'
    }
  ];

  build() {
    Column({ space: 20 }) {
      if (this.currentVideo) {
        this.buildVideoPlayer();
      } else {
        this.buildVideoList();
      }
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildVideoList() {
    Column({ space: 20 }) {
      // 视频学习标题
      Row({ space: 16 }) {
        Text('🎥 视频学习')
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
        TextInput({ placeholder: '🔍 搜索视频...', text: this.searchText })
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

      // 分类筛选
      Scroll() {
        Row({ space: 12 }) {
          ForEach(this.categories, (category: string) => {
            Button(category)
              .backgroundColor(this.selectedCategory === category ? '#6366f1' : '#ffffff')
              .fontColor(this.selectedCategory === category ? Color.White : '#6b7280')
              .borderRadius(24)
              .padding({ left: 16, right: 16, top: 8, bottom: 8 })
              .fontSize(15)
              .border({ width: 1, color: this.selectedCategory === category ? '#6366f1' : '#e5e7eb' })
              .onClick(() => {
                this.selectedCategory = category;
              });
          });
        }
        .padding({ left: 4, right: 4 });
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off);

      // 学习进度概览
      this.buildProgressOverview();

      // 视频列表
      ForEach(this.getFilteredVideos(), (video: Video) => {
        this.buildVideoCard(video);
      });
    }
  }

  @Builder
  buildProgressOverview() {
    Column({ space: 16 }) {
      Text('学习进度概览')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1f2937')
        .width('100%')
        .textAlign(TextAlign.Start);

      Row({ space: 20 }) {
        Column({ space: 8 }) {
          Text(`${this.getTotalWatchTime()}`)
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .fontColor('#6366f1')
            .textAlign(TextAlign.Center);

          Text('总观看时长')
            .fontSize(14)
            .fontColor('#6b7280')
            .textAlign(TextAlign.Center);
        }
        .layoutWeight(1);

        Column({ space: 8 }) {
          Text(`${this.getCompletedVideoCount()}`)
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .fontColor('#10b981')
            .textAlign(TextAlign.Center);

          Text('已完成视频')
            .fontSize(14)
            .fontColor('#6b7280')
            .textAlign(TextAlign.Center);
        }
        .layoutWeight(1);

        Column({ space: 8 }) {
          Text(`${this.getAverageProgress()}%`)
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .fontColor('#f59e0b')
            .textAlign(TextAlign.Center);

          Text('平均进度')
            .fontSize(14)
            .fontColor('#6b7280')
            .textAlign(TextAlign.Center);
        }
        .layoutWeight(1);
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
  buildVideoCard(video: Video) {
    Column({ space: 16 }) {
      Row({ space: 16 }) {
        // 视频缩略图
        Column() {
          Text(video.thumbnail)
            .fontSize(32)
            .textAlign(TextAlign.Center);
        }
        .width(80)
        .height(60)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .justifyContent(FlexAlign.Center);

        // 视频信息
        Column({ space: 8 }) {
          Text(video.title)
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1f2937')
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis });

          Text(video.description)
            .fontSize(14)
            .fontColor('#6b7280')
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis });

          Row({ space: 8 }) {
            Text(video.category)
              .fontSize(12)
              .fontColor('#6366f1')
              .backgroundColor('#eef2ff')
              .padding({ left: 8, right: 8, top: 4, bottom: 4 })
              .borderRadius(6);

            Text(this.formatDuration(video.duration))
              .fontSize(12)
              .fontColor('#f59e0b')
              .backgroundColor('#fef3c7')
              .padding({ left: 8, right: 8, top: 4, bottom: 4 })
              .borderRadius(6);
          }
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);
      }

      // 进度条
      if (this.getVideoProgress(video.id)) {
        Column({ space: 8 }) {
          Row() {
            Text('观看进度')
              .fontSize(13)
              .fontColor('#9ca3af');

            Blank();

            Text(`${this.getVideoProgress(video.id)?.completionRate?.toFixed(1) || '0'}%`)
              .fontSize(13)
              .fontColor('#9ca3af');
          }

          Progress({ value: this.getVideoProgress(video.id)?.completionRate || 0, total: 100, type: ProgressType.Linear })
            .color('#6366f1')
            .backgroundColor('#e5e7eb')
            .width('100%')
            .height(6);
        }
      }

      // 操作按钮
      Row({ space: 12 }) {
        if (this.getVideoProgress(video.id) && (this.getVideoProgress(video.id)?.completionRate || 0) > 0) {
          Button('继续观看')
            .backgroundColor('#f59e0b')
            .fontColor(Color.White)
            .borderRadius(10)
            .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            .fontSize(15)
            .layoutWeight(1)
            .onClick(() => {
              this.playVideo(video.id);
            });
        } else {
          Button('开始观看')
            .backgroundColor('#6366f1')
            .fontColor(Color.White)
            .borderRadius(10)
            .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            .fontSize(15)
            .layoutWeight(1)
            .onClick(() => {
              this.playVideo(video.id);
            });
        }

        Button('下载')
          .backgroundColor('#10b981')
          .fontColor(Color.White)
          .borderRadius(10)
          .padding({ left: 16, right: 16, top: 8, bottom: 8 })
          .fontSize(15)
          .onClick(() => {
            this.downloadVideo(video.id);
          });
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
  buildVideoPlayer() {
    Column({ space: 20 }) {
      // 播放器头部
      Row({ space: 16 }) {
        Button('← 返回')
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 })
          .onClick(() => {
            this.exitPlayer();
          });

        Text(this.getCurrentVideoTitle())
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .layoutWeight(1)
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis });

        Button('📝')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(8)
          .padding(8)
          .onClick(() => {
            this.showNotes = !this.showNotes;
          });
      }

      // 视频播放区域
      this.buildVideoPlayerArea();

      // 播放控制
      this.buildPlaybackControls();

      // 笔记区域
      if (this.showNotes) {
        this.buildNotesArea();
      }
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildVideoPlayerArea() {
    Stack() {
      // 视频播放区域背景
      Rect()
        .width('100%')
        .height(250)
        .fill('#000000')
        .borderRadius(12);

      // 播放状态显示
      Column({ space: 16 }) {
        Text('🎬')
          .fontSize(48)
          .fontColor('#ffffff');

        Text(this.isPlaying ? '播放中...' : '已暂停')
          .fontSize(16)
          .fontColor('#ffffff')
          .fontWeight(FontWeight.Medium);

        Text(this.formatTime(this.currentTime) + ' / ' + this.formatTime(this.currentVideo ? this.currentVideo.totalTime : 0))
          .fontSize(14)
          .fontColor('#cccccc');
      }
      .justifyContent(FlexAlign.Center);

      // 播放/暂停按钮
      Button(this.isPlaying ? '⏸️' : '▶️')
        .backgroundColor('rgba(0, 0, 0, 0.6)')
        .fontColor('#ffffff')
        .borderRadius(30)
        .width(60)
        .height(60)
        .fontSize(24)
        .onClick(() => {
          this.togglePlayPause();
        });
    }
    .width('100%')
    .height(250)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 6, color: '#00000012', offsetX: 0, offsetY: 3 });
  }

  @Builder
  buildPlaybackControls() {
    Column({ space: 16 }) {
      // 进度条
      Column({ space: 8 }) {
        Slider({
          value: this.currentTime,
          min: 0,
          max: this.currentVideo ? this.currentVideo.totalTime : 100,
          step: 1
        })
          .trackColor('#e5e7eb')
          .selectedColor('#6366f1')
          .blockColor('#6366f1')
          .onChange((value: number) => {
            this.seekTo(value);
          });

        Row() {
          Text(this.formatTime(this.currentTime))
            .fontSize(12)
            .fontColor('#6b7280');

          Blank();

          Text(this.formatTime(this.currentVideo ? this.currentVideo.totalTime : 0))
            .fontSize(12)
            .fontColor('#6b7280');
        }
      }

      // 控制按钮
      Row({ space: 20 }) {
        Button('⏮️')
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding(12)
          .onClick(() => {
            this.seekBackward();
          });

        Button(this.isPlaying ? '⏸️' : '▶️')
          .backgroundColor('#6366f1')
          .fontColor(Color.White)
          .borderRadius(12)
          .padding(16)
          .fontSize(20)
          .onClick(() => {
            this.togglePlayPause();
          });

        Button('⏭️')
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding(12)
          .onClick(() => {
            this.seekForward();
          });

        Blank();

        // 倍速控制
        Button(`${this.playbackSpeed}x`)
          .backgroundColor('#f3f4f6')
          .fontColor('#6b7280')
          .borderRadius(8)
          .padding({ left: 12, right: 12, top: 8, bottom: 8 })
          .onClick(() => {
            this.showSpeedMenu = !this.showSpeedMenu;
          });
      }
      .justifyContent(FlexAlign.Center);

      // 倍速选择菜单
      if (this.showSpeedMenu) {
        Row({ space: 8 }) {
          ForEach(this.speedOptions, (speed: number) => {
            Button(`${speed}x`)
              .backgroundColor(this.playbackSpeed === speed ? '#6366f1' : '#f3f4f6')
              .fontColor(this.playbackSpeed === speed ? Color.White : '#6b7280')
              .borderRadius(6)
              .padding({ left: 8, right: 8, top: 4, bottom: 4 })
              .fontSize(12)
              .onClick(() => {
                this.setPlaybackSpeed(speed);
              });
          });
        }
        .justifyContent(FlexAlign.Center);
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#ffffff')
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 });
  }

  @Builder
  buildNotesArea() {
    Column({ space: 16 }) {
      Text('📝 学习笔记')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1f2937');

      // 添加笔记
      Column({ space: 12 }) {
        TextArea({ placeholder: '在此添加学习笔记...', text: this.noteText })
          .onChange(v => this.noteText = v)
          .backgroundColor('#f8fafc')
          .borderRadius(8)
          .padding(12)
          .height(80);

        Row({ space: 12 }) {
          Text(`当前时间: ${this.formatTime(this.currentTime)}`)
            .fontSize(14)
            .fontColor('#6b7280')
            .layoutWeight(1);

          Button('添加笔记')
            .backgroundColor('#6366f1')
            .fontColor(Color.White)
            .borderRadius(8)
            .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            .enabled(this.noteText.trim().length > 0)
            .onClick(() => {
              this.addNote();
            });
        }
      }

      // 笔记列表
      if (this.videoNotes.length > 0) {
        Column({ space: 12 }) {
          Text('已添加的笔记')
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#374151');

          ForEach(this.videoNotes, (note: VideoNote, index: number) => {
            Row({ space: 12 }) {
              Column({ space: 4 }) {
                Text(this.formatTime(note.time))
                  .fontSize(12)
                  .fontColor('#6366f1')
                  .fontWeight(FontWeight.Medium);

                Text(note.note)
                  .fontSize(14)
                  .fontColor('#374151')
                  .maxLines(3)
                  .textOverflow({ overflow: TextOverflow.Ellipsis });
              }
              .alignItems(HorizontalAlign.Start)
              .layoutWeight(1);

              Button('跳转')
                .backgroundColor('#f3f4f6')
                .fontColor('#6b7280')
                .borderRadius(6)
                .padding({ left: 8, right: 8, top: 4, bottom: 4 })
                .fontSize(12)
                .onClick(() => {
                  this.seekTo(note.time);
                });
            }
            .width('100%')
            .padding(12)
            .backgroundColor('#f8fafc')
            .borderRadius(8)
            .border({ width: 1, color: '#e5e7eb' });
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

  // 功能方法
  getFilteredVideos(): Video[] {
    return this.videoList.filter(video => {
      const matchesSearch = video.title.toLowerCase().includes(this.searchText.toLowerCase()) ||
      video.description.toLowerCase().includes(this.searchText.toLowerCase());
      const matchesCategory = this.selectedCategory === '全部' || video.category === this.selectedCategory;
      return matchesSearch && matchesCategory;
    });
  }

  getVideoProgress(videoId: string): VideoProgress | undefined {
    return this.videoProgresses.find(p => p.videoId === videoId);
  }

  playVideo(videoId: string) {
    let progress = this.getVideoProgress(videoId);
    if (!progress) {
      // 创建新的进度记录
      const video = this.videoList.find(v => v.id === videoId);
      if (video) {
        progress = {
          videoId: videoId,
          currentTime: 0,
          totalTime: video.duration,
          watchedSegments: [],
          completionRate: 0
        };
        this.videoProgresses.push(progress);
      }
    }

    if (progress) {
      this.currentVideo = progress;
      this.currentTime = progress.currentTime;
      this.isPlaying = true;
      this.startPlaybackTimer();
    }
  }

  exitPlayer() {
    this.stopPlaybackTimer();
    this.saveProgress();
    this.currentVideo = null;
    this.isPlaying = false;
    this.showNotes = false;
    this.videoNotes = [];
  }

  togglePlayPause() {
    this.isPlaying = !this.isPlaying;
    if (this.isPlaying) {
      this.startPlaybackTimer();
    } else {
      this.stopPlaybackTimer();
    }
  }

  seekTo(time: number) {
    this.currentTime = time;
    if (this.currentVideo) {
      this.currentVideo.currentTime = time;
    }
  }

  seekForward() {
    this.seekTo(Math.min(this.currentTime + 10, this.currentVideo!.totalTime));
  }

  seekBackward() {
    this.seekTo(Math.max(this.currentTime - 10, 0));
  }

  setPlaybackSpeed(speed: number) {
    this.playbackSpeed = speed;
    this.showSpeedMenu = false;
  }

  addNote() {
    if (this.noteText.trim().length > 0) {
      this.videoNotes.push({
        time: this.currentTime,
        note: this.noteText.trim()
      });
      this.noteText = '';
    }
  }

  startPlaybackTimer() {
    this.stopPlaybackTimer(); // 确保之前的定时器被清除
    // 模拟播放进度更新
    this.playbackTimer = setInterval(() => {
      if (this.isPlaying && this.currentVideo) {
        this.currentTime += this.playbackSpeed;
        if (this.currentTime >= this.currentVideo.totalTime) {
          this.currentTime = this.currentVideo.totalTime;
          this.isPlaying = false;
          this.onVideoCompleted();
        }
        this.updateWatchedSegments();
      }
    }, 1000);
  }

  stopPlaybackTimer() {
    if (this.playbackTimer !== null) {
      clearInterval(this.playbackTimer);
      this.playbackTimer = null;
    }
  }

  updateWatchedSegments() {
    if (this.currentVideo) {
      // 更新观看片段
      const currentSegment = this.currentVideo.watchedSegments.find(
        seg => seg.start <= this.currentTime && seg.end >= this.currentTime
      );

      if (!currentSegment) {
        this.currentVideo.watchedSegments.push({
          start: this.currentTime,
          end: this.currentTime + 1
        });
      } else {
        currentSegment.end = Math.max(currentSegment.end, this.currentTime + 1);
      }

      this.updateCompletionRate();
    }
  }

  updateCompletionRate() {
    if (this.currentVideo) {
      const totalWatched = this.currentVideo.watchedSegments.reduce(
        (total, segment) => total + (segment.end - segment.start), 0
      );
      this.currentVideo.completionRate = Math.min(
        (totalWatched / this.currentVideo.totalTime) * 100, 100
      );
    }
  }

  saveProgress() {
    if (this.currentVideo) {
      this.currentVideo.currentTime = this.currentTime;
    }
  }

  onVideoCompleted() {
    console.log('视频播放完成');
    if (this.currentVideo) {
      this.currentVideo.completionRate = 100;
    }
  }

  downloadVideo(videoId: string) {
    console.log('下载视频:', videoId);
    // 实现视频下载功能
  }

  getCurrentVideoTitle(): string {
    if (this.currentVideo) {
      const video = this.videoList.find(v => v.id === this.currentVideo!.videoId);
      return video?.title || '未知视频';
    }
    return '';
  }

  formatTime(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  formatDuration(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    return `${minutes}分钟`;
  }

  getTotalWatchTime(): string {
    const totalSeconds = this.videoProgresses.reduce((total, progress) => {
      return total + progress.watchedSegments.reduce(
        (segTotal, segment) => segTotal + (segment.end - segment.start), 0
      );
    }, 0);
    return this.formatTime(totalSeconds);
  }

  getCompletedVideoCount(): number {
    return this.videoProgresses.filter(p => p.completionRate >= 100).length;
  }

  getAverageProgress(): number {
    if (this.videoProgresses.length === 0) return 0;
    const totalProgress = this.videoProgresses.reduce((total, p) => total + p.completionRate, 0);
    return Math.round(totalProgress / this.videoProgresses.length);
  }

  showLearningStats() {
    console.log('显示学习统计');
    this.showStatsDialog = true;
  }

  @Builder
  buildStatsDialog() {
    Column({ space: 20 }) {
      Text('📊 学习统计')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1f2937');

      Column({ space: 16 }) {
        // 总观看时长
        Row({ space: 12 }) {
          Text('⏱️')
            .fontSize(24);

          Column({ space: 4 }) {
            Text('总观看时长')
              .fontSize(14)
              .fontColor('#6b7280');

            Text(this.getTotalWatchTime())
              .fontSize(18)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1f2937');
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1);
        }
        .width('100%')
        .padding(16)
        .backgroundColor('#f8fafc')
        .borderRadius(12);

        // 已完成视频数量
        Row({ space: 12 }) {
          Text('✅')
            .fontSize(24);

          Column({ space: 4 }) {
            Text('已完成视频')
              .fontSize(14)
              .fontColor('#6b7280');

            Text(`${this.getCompletedVideoCount()}/${this.videoList.length}`)
              .fontSize(18)
              .fontWeight(FontWeight.Bold)
              .fontColor('#10b981');
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1);
        }
        .width('100%')
        .padding(16)
        .backgroundColor('#f0fdf4')
        .borderRadius(12);

        // 平均进度
        Row({ space: 12 }) {
          Text('📈')
            .fontSize(24);

          Column({ space: 4 }) {
            Text('平均进度')
              .fontSize(14)
              .fontColor('#6b7280');

            Text(`${this.getAverageProgress()}%`)
              .fontSize(18)
              .fontWeight(FontWeight.Bold)
              .fontColor('#6366f1');
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1);
        }
        .width('100%')
        .padding(16)
        .backgroundColor('#eef2ff')
        .borderRadius(12);
      }

      Button('关闭')
        .backgroundColor('#6366f1')
        .fontColor(Color.White)
        .borderRadius(8)
        .padding({ left: 24, right: 24, top: 12, bottom: 12 })
        .onClick(() => {
          this.showStatsDialog = false;
        });
    }
    .width('100%')
    .padding(24)
    .backgroundColor('#ffffff')
    .borderRadius(16)
    .shadow({ radius: 12, color: '#00000020', offsetX: 0, offsetY: 6 });
  }
}