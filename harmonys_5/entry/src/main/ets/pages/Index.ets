import { router } from '@kit.ArkUI';

interface User {
  username: string;
  password: string;
}

let userDB: User[] = [
  { username: 'test', password: '123456' }
];

@Entry
@Component
struct LoginPage {
  @State username: string = '';
  @State password: string = '';
  @State errorMsg: string = '';
  @State isLoading: boolean = false;
  @State showPassword: boolean = false;
  @State userType: string = 'student'; // 'student' 或 'teacher'

  build(): void {
    Stack() {
      // 背景渐变
      Column()
        .width('100%')
        .height('100%')
        .linearGradient({
          angle: 135,
          colors: [['#667eea', 0], ['#764ba2', 1]]
        });

      // 使用滚动容器确保内容不被遮挡
      Scroll() {
        Column({ space: 0 }) {
          // 顶部装饰区域
          Column({ space: 16 }) {
            // Logo区域
            Column() {
              Text('🎓')
                .fontSize(56)
                .fontColor('#ffffff')
                .textAlign(TextAlign.Center);
            }
            .width(100)
            .height(100)
            .backgroundColor('#ffffff20')
            .borderRadius(50)
            .justifyContent(FlexAlign.Center)
            .shadow({ radius: 20, color: '#00000030', offsetX: 0, offsetY: 10 });

            Text('智能学习系统')
              .fontSize(28)
              .fontWeight(FontWeight.Bold)
              .fontColor('#ffffff')
              .textAlign(TextAlign.Center)
              .textShadow({ radius: 4, color: '#00000040', offsetX: 0, offsetY: 2 });

            Text('让学习更高效，让管理更简单')
              .fontSize(14)
              .fontColor('#ffffff')
              .opacity(0.9)
              .textAlign(TextAlign.Center);
          }
          .margin({ top: 40, bottom: 30 });

          // 登录卡片
          Column({ space: 24 }) {
            Text('欢迎回来')
              .fontSize(24)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1f2937')
              .textAlign(TextAlign.Center)
              .margin({ bottom: 8 });

            // 用户类型选择
            Column({ space: 12 }) {
              Text('选择身份')
                .fontSize(16)
                .fontWeight(FontWeight.Medium)
                .fontColor('#374151')
                .alignSelf(ItemAlign.Start);

              Row({ space: 0 }) {
                Button('👨‍🎓 学生')
                  .backgroundColor(this.userType === 'student' ? '#6366f1' : '#f8fafc')
                  .fontColor(this.userType === 'student' ? '#ffffff' : '#6b7280')
                  .borderRadius({ topLeft: 12, bottomLeft: 12, topRight: 0, bottomRight: 0 })
                  .border({ width: 1, color: this.userType === 'student' ? '#6366f1' : '#e5e7eb' })
                  .layoutWeight(1)
                  .padding({ top: 14, bottom: 14 })
                  .onClick(() => {
                    this.userType = 'student';
                  });

                Button('👨‍🏫 教师')
                  .backgroundColor(this.userType === 'teacher' ? '#6366f1' : '#f8fafc')
                  .fontColor(this.userType === 'teacher' ? '#ffffff' : '#6b7280')
                  .borderRadius({ topLeft: 0, bottomLeft: 0, topRight: 12, bottomRight: 12 })
                  .border({ width: 1, color: this.userType === 'teacher' ? '#6366f1' : '#e5e7eb' })
                  .layoutWeight(1)
                  .padding({ top: 14, bottom: 14 })
                  .onClick(() => {
                    this.userType = 'teacher';
                  });
              }
              .width('100%');
            }

            // 用户名输入框
            Column({ space: 8 }) {
              Row({ space: 12 }) {
                Text('👤')
                  .fontSize(20)
                  .fontColor('#6b7280');

                TextInput({ placeholder: '请输入用户名', text: this.username })
                  .onChange((val: string) => this.username = val)
                  .fontSize(16)
                  .backgroundColor('transparent')
                  .border({ width: 0 })
                  .layoutWeight(1)
                  .placeholderColor('#9ca3af')
                  .fontColor('#1f2937');
              }
              .width('100%')
              .padding({ left: 16, right: 16, top: 14, bottom: 14 })
              .backgroundColor('#f8fafc')
              .borderRadius(12)
              .border({ width: 1, color: '#e2e8f0' });
            }

            // 密码输入框
            Column({ space: 8 }) {
              Row({ space: 12 }) {
                Text('🔒')
                  .fontSize(20)
                  .fontColor('#6b7280');

                TextInput({
                  placeholder: '请输入密码',
                  text: this.password
                })
                  .onChange((val: string) => this.password = val)
                  .type(this.showPassword ? InputType.Normal : InputType.Password)
                  .fontSize(16)
                  .backgroundColor('transparent')
                  .border({ width: 0 })
                  .layoutWeight(1)
                  .placeholderColor('#9ca3af')
                  .fontColor('#1f2937');

                Button(this.showPassword ? '🙈' : '👁️')
                  .backgroundColor('transparent')
                  .fontSize(18)
                  .fontColor('#6b7280')
                  .padding(0)
                  .width(32)
                  .height(32)
                  .onClick(() => {
                    this.showPassword = !this.showPassword;
                  });
              }
              .width('100%')
              .padding({ left: 16, right: 16, top: 14, bottom: 14 })
              .backgroundColor('#f8fafc')
              .borderRadius(12)
              .border({ width: 1, color: '#e2e8f0' });
            }

            // 错误信息
            if (this.errorMsg !== '') {
              Row({ space: 8 }) {
                Text('⚠️')
                  .fontSize(16);

                Text(this.errorMsg)
                  .fontSize(14)
                  .fontColor('#ef4444')
                  .flexGrow(1);
              }
              .width('100%')
              .padding(12)
              .backgroundColor('#fef2f2')
              .borderRadius(8)
              .border({ width: 1, color: '#fecaca' });
            }

            // 登录按钮
            Button(this.isLoading ? '登录中...' : `🚀 ${this.userType === 'student' ? '学生' : '教师'}登录`)
              .width('100%')
              .height(52)
              .fontSize(18)
              .fontWeight(FontWeight.Bold)
              .fontColor('#ffffff')
              .backgroundColor(this.isLoading ? '#9ca3af' : (this.userType === 'student' ? '#3b82f6' : '#10b981'))
              .borderRadius(12)
              .shadow({ radius: 8, color: this.userType === 'student' ? '#3b82f630' : '#10b98130', offsetX: 0, offsetY: 4 })
              .enabled(!this.isLoading)
              .onClick(() => {
                if (this.username === '' || this.password === '') {
                  this.errorMsg = '请输入用户名和密码';
                  return;
                }

                this.isLoading = true;
                this.errorMsg = '';

                // 模拟登录延迟
                setTimeout(() => {
                  let match = userDB.find(u => u.username === this.username && u.password === this.password);
                  if (match) {
                    // 根据用户类型跳转到不同页面
                    if (this.userType === 'student') {
                      router.pushUrl({ url: 'pages/student/student' }).catch((err: Error) => {
                        console.error('Navigation failed:', err.message);
                      }); // 学生主页
                    } else {
                      router.pushUrl({ url: 'pages/teacher/teacher' }).catch((err: Error) => {
                        console.error('Navigation failed:', err.message);
                      }); // 教师主页
                    }
                  } else {
                    this.errorMsg = `${this.userType === 'student' ? '学生' : '教师'}用户名或密码错误，请重试`;
                  }
                  this.isLoading = false;
                }, 1000);
              });

            // 分割线
            Row({ space: 16 }) {
              Divider()
                .strokeWidth(1)
                .color('#e2e8f0')
                .layoutWeight(1);

              Text('或')
                .fontSize(14)
                .fontColor('#6b7280');

              Divider()
                .strokeWidth(1)
                .color('#e2e8f0')
                .layoutWeight(1);
            }
            .width('100%');

            // 注册按钮
            Button(`📝 创建${this.userType === 'student' ? '学生' : '教师'}账户`)
              .width('100%')
              .height(48)
              .fontSize(16)
              .fontWeight(FontWeight.Medium)
              .fontColor(this.userType === 'student' ? '#3b82f6' : '#10b981')
              .backgroundColor('#ffffff')
              .borderRadius(12)
              .border({ width: 2, color: this.userType === 'student' ? '#3b82f6' : '#10b981' })
              .shadow({ radius: 4, color: this.userType === 'student' ? '#3b82f620' : '#10b98120', offsetX: 0, offsetY: 2 })
              .onClick(() => {
                router.pushUrl({
              url: 'pages/RegisterPage'
            }).catch((err: Error) => {
              console.error('Navigation failed:', err.message);
            });
              });

            // 底部提示
            Text('登录即表示您同意我们的服务条款和隐私政策')
              .fontSize(12)
              .fontColor('#6b7280')
              .textAlign(TextAlign.Center)
              .lineHeight(18)
              .margin({ top: 16 });
          }
          .width('100%')
          .padding(24)
          .backgroundColor('#ffffff')
          .borderRadius(24)
          .shadow({ radius: 30, color: '#00000020', offsetX: 0, offsetY: 15 })
          .margin({ left: 20, right: 20, bottom: 40 });
        }
        .width('100%')
        .padding({ left: 0, right: 0, top: 20, bottom: 40 });
      }
      .width('100%')
      .height('100%')
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring);
    }
    .width('100%')
    .height('100%');
  }
}