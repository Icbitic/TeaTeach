import { router } from '@kit.ArkUI';

interface User {
  phone: string;
  password: string;
}

declare let userDB: User[]; // 与 loginPage 共用数据

@Entry
@Component
struct RegisterPage {
  @State phone: string = '';
  @State password: string = '';
  @State confirm: string = '';
  @State errorMsg: string = '';
  @State isLoading: boolean = false;
  @State showPassword: boolean = false;
  @State showConfirmPassword: boolean = false;

  build(): void {
    Stack() {
      // 背景渐变
      Column()
        .width('100%')
        .height('100%')
        .linearGradient({
          angle: 135,
          colors: [['#10b981', 0], ['#059669', 1]]
        });

      // 使用滚动容器确保内容不被遮挡
      Scroll() {
        Column({ space: 0 }) {
          // 顶部装饰区域
          Column({ space: 16 }) {
            // Logo区域
            Column() {
              Text('📝')
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

            Text('用户注册')
              .fontSize(28)
              .fontWeight(FontWeight.Bold)
              .fontColor('#ffffff')
              .textAlign(TextAlign.Center)
              .textShadow({ radius: 4, color: '#00000040', offsetX: 0, offsetY: 2 });

            Text('加入我们，开启智能学习之旅')
              .fontSize(14)
              .fontColor('#ffffff')
              .opacity(0.9)
              .textAlign(TextAlign.Center);
          }
          .margin({ top: 40, bottom: 30 });

          // 注册卡片
          Column({ space: 24 }) {
            Text('创建新账户')
              .fontSize(24)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1f2937')
              .textAlign(TextAlign.Center)
              .margin({ bottom: 8 });

            // 手机号输入框
            Column({ space: 8 }) {
              Row({ space: 12 }) {
                Text('📱')
                  .fontSize(20)
                  .fontColor('#6b7280');

                TextInput({ placeholder: '请输入手机号', text: this.phone })
                  .onChange((val: string) => this.phone = val)
                  .type(InputType.PhoneNumber)
                  .fontSize(16)
                  .backgroundColor('transparent')
                  .border({ width: 0 })
                  .layoutWeight(1)
                  .placeholderColor('#9ca3af')
                  .fontColor('#1f2937')
                  .maxLength(11);
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

            // 确认密码输入框
            Column({ space: 8 }) {
              Row({ space: 12 }) {
                Text('🔐')
                  .fontSize(20)
                  .fontColor('#6b7280');

                TextInput({
                  placeholder: '请确认密码',
                  text: this.confirm
                })
                  .onChange((val: string) => this.confirm = val)
                  .type(this.showConfirmPassword ? InputType.Normal : InputType.Password)
                  .fontSize(16)
                  .backgroundColor('transparent')
                  .border({ width: 0 })
                  .layoutWeight(1)
                  .placeholderColor('#9ca3af')
                  .fontColor('#1f2937');

                Button(this.showConfirmPassword ? '🙈' : '👁️')
                  .backgroundColor('transparent')
                  .fontSize(18)
                  .fontColor('#6b7280')
                  .padding(0)
                  .width(32)
                  .height(32)
                  .onClick(() => {
                    this.showConfirmPassword = !this.showConfirmPassword;
                  });
              }
              .width('100%')
              .padding({ left: 16, right: 16, top: 14, bottom: 14 })
              .backgroundColor('#f8fafc')
              .borderRadius(12)
              .border({ width: 1, color: '#e2e8f0' });
            }

            // 密码强度提示
            if (this.password !== '') {
              Column({ space: 8 }) {
                Text('密码强度')
                  .fontSize(14)
                  .fontColor('#6b7280');

                Row({ space: 4 }) {
                  ForEach(Array.from({length: 4}), (_: number, index: number) => {
                    Divider()
                      .strokeWidth(4)
                      .color(this.getPasswordStrengthColor(index))
                      .layoutWeight(1);
                  }, (_: number, index: number) => index.toString());
                }
                .height(4);

                Text(this.getPasswordStrengthText())
                  .fontSize(12)
                  .fontColor(this.getPasswordStrengthTextColor());
              }
              .width('100%')
              .padding(12)
              .backgroundColor('#f8fafc')
              .borderRadius(8)
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

            // 注册按钮
            Button(this.isLoading ? '注册中...' : '🚀 立即注册')
              .width('100%')
              .height(52)
              .fontSize(18)
              .fontWeight(FontWeight.Bold)
              .fontColor('#ffffff')
              .backgroundColor(this.isLoading ? '#9ca3af' : '#10b981')
              .borderRadius(12)
              .shadow({ radius: 8, color: '#10b98130', offsetX: 0, offsetY: 4 })
              .enabled(!this.isLoading)
              .onClick(() => {
                if (this.phone === '' || this.password === '' || this.confirm === '') {
                  this.errorMsg = '请填写完整信息';
                  return;
                }

                if (!this.isValidPhone(this.phone)) {
                  this.errorMsg = '请输入正确的手机号格式';
                  return;
                }

                if (this.password.length < 6) {
                  this.errorMsg = '密码至少需要6个字符';
                  return;
                }

                if (this.password !== this.confirm) {
                  this.errorMsg = '两次密码输入不一致';
                  return;
                }

                this.isLoading = true;
                this.errorMsg = '';

                // 模拟注册延迟
                setTimeout(() => {
                  userDB.push({ phone: this.phone, password: this.password });
                  router.pushUrl({ url: 'pages/LoginPage' }).catch((err: Error) => {
                    console.error('Navigation failed:', err.message);
                  });
                  this.isLoading = false;
                }, 100);
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

            // 返回登录按钮
            Button('🔙 返回登录')
              .width('100%')
              .height(48)
              .fontSize(16)
              .fontWeight(FontWeight.Medium)
              .fontColor('#10b981')
              .backgroundColor('#ffffff')
              .borderRadius(12)
              .border({ width: 2, color: '#10b981' })
              .shadow({ radius: 4, color: '#10b98120', offsetX: 0, offsetY: 2 })
              .onClick(() => {
                router.back();
              });

            // 底部提示
            Text('注册即表示您同意我们的服务条款和隐私政策')
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

  // 获取密码强度颜色
  getPasswordStrengthColor(index: number): string {
    const strength = this.calculatePasswordStrength();
    if (index < strength) {
      switch (strength) {
        case 1: return '#ef4444'; // 弱
        case 2: return '#f59e0b'; // 一般
        case 3: return '#3b82f6'; // 良好
        case 4: return '#10b981'; // 强
        default: return '#e5e7eb';
      }
    }
    return '#e5e7eb';
  }

  // 获取密码强度文本
  getPasswordStrengthText(): string {
    const strength = this.calculatePasswordStrength();
    switch (strength) {
      case 1: return '弱';
      case 2: return '一般';
      case 3: return '良好';
      case 4: return '强';
      default: return '';
    }
  }

  // 获取密码强度文本颜色
  getPasswordStrengthTextColor(): string {
    const strength = this.calculatePasswordStrength();
    switch (strength) {
      case 1: return '#ef4444';
      case 2: return '#f59e0b';
      case 3: return '#3b82f6';
      case 4: return '#10b981';
      default: return '#6b7280';
    }
  }

  // 计算密码强度
  calculatePasswordStrength(): number {
    let strength = 0;
    if (this.password.length >= 6) strength++;
    if (/[a-z]/.test(this.password)) strength++;
    if (/[A-Z]/.test(this.password)) strength++;
    if (/[0-9]/.test(this.password)) strength++;
    return strength;
  }

  // 验证手机号格式
  isValidPhone(phone: string): boolean {
    const phoneRegex = /^1[3-9]\d{9}$/;
    return phoneRegex.test(phone);
  }
}