import { KnowledgeNode } from '../interfaces/StudentInterfaces';

@Component
export struct KnowledgeGraph {
  @Link knowledgeNodes: KnowledgeNode[];
  @State selectedNode: KnowledgeNode | null = null;
  @State showNodeDetail: boolean = false;
  @State searchText: string = '';
  @State viewMode: string = 'graph'; // 'graph' 或 'list'

  private readonly NODE_SIZE = 70;
  private readonly CONNECTION_COLOR = '#cbd5e1';
  private readonly COMPLETED_COLOR = '#10b981';
  private readonly PENDING_COLOR = '#8b5cf6';
  private readonly LOCKED_COLOR = '#ef4444';
  private readonly HOVER_SCALE = 1.1;

  build() {
    Column({ space: 20 }) {
      // 知识图谱标题和控制
      Row({ space: 16 }) {
        Column({ space: 4 }) {
          Text('🧠 知识图谱')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1e293b')
            .textShadow({ radius: 1, color: '#00000020', offsetX: 0, offsetY: 1 });

          Text('探索知识的奥秘')
            .fontSize(14)
            .fontColor('#64748b')
            .fontStyle(FontStyle.Italic);
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1);

        Row({ space: 12 }) {
          Button(this.viewMode === 'graph' ? '🌐' : '📋')
            .backgroundColor(this.viewMode === 'graph' ? '#ddd6fe' : '#e0f2fe')
            .fontColor(this.viewMode === 'graph' ? '#7c3aed' : '#0369a1')
            .borderRadius(12)
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .shadow({ radius: 4, color: '#00000015', offsetX: 0, offsetY: 2 })
            .onClick(() => {
              this.viewMode = this.viewMode === 'graph' ? 'list' : 'graph';
            });

          Button('🔍')
            .backgroundColor('#f0fdf4')
            .fontColor('#16a34a')
            .borderRadius(12)
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .shadow({ radius: 4, color: '#00000015', offsetX: 0, offsetY: 2 })
            .onClick(() => {
              this.showSearchDialog();
            });
        }
      }

      // 搜索框
      if (this.searchText !== '') {
        TextInput({ placeholder: '🔍 搜索知识点...', text: this.searchText })
          .onChange(v => this.searchText = v)
          .backgroundColor('#ffffff')
          .borderRadius(16)
          .padding({ left: 20, right: 20, top: 16, bottom: 16 })
          .fontSize(16)
          .border({ width: 2, color: '#e2e8f0' })
          .focusOnTouch(true)
          .shadow({ radius: 8, color: '#00000010', offsetX: 0, offsetY: 4 });
      }

      // 学习进度统计
      this.buildProgressStats();

      // 主要内容区域
      if (this.viewMode === 'graph') {
        this.buildGraphView();
      } else {
        this.buildListView();
      }

      // 节点详情对话框
      if (this.showNodeDetail && this.selectedNode) {
        this.buildNodeDetailDialog();
      }
    }
    .width('100%')
    .padding(16);
  }

  @Builder
  buildProgressStats() {
    Row({ space: 16 }) {
      Column({ space: 8 }) {
        Text(`${this.getCompletedCount()}`)
          .fontSize(28)
          .fontWeight(FontWeight.Bold)
          .fontColor('#10b981')
          .textShadow({ radius: 1, color: '#00000020', offsetX: 0, offsetY: 1 });
        Text('✅ 已完成')
          .fontSize(13)
          .fontColor('#059669')
          .fontWeight(FontWeight.Medium);
      }
      .backgroundColor('#f0fdf4')
      .borderRadius(16)
      .padding({ left: 20, right: 20, top: 18, bottom: 18 })
      .layoutWeight(1)
      .border({ width: 1, color: '#bbf7d0' })
      .shadow({ radius: 8, color: '#10b98120', offsetX: 0, offsetY: 4 });

      Column({ space: 8 }) {
        Text(`${this.getPendingCount()}`)
          .fontSize(28)
          .fontWeight(FontWeight.Bold)
          .fontColor('#8b5cf6')
          .textShadow({ radius: 1, color: '#00000020', offsetX: 0, offsetY: 1 });
        Text('⏳ 待学习')
          .fontSize(13)
          .fontColor('#7c3aed')
          .fontWeight(FontWeight.Medium);
      }
      .backgroundColor('#faf5ff')
      .borderRadius(16)
      .padding({ left: 20, right: 20, top: 18, bottom: 18 })
      .layoutWeight(1)
      .border({ width: 1, color: '#e9d5ff' })
      .shadow({ radius: 8, color: '#8b5cf620', offsetX: 0, offsetY: 4 });

      Column({ space: 8 }) {
        Text(`${this.getProgressPercentage()}%`)
          .fontSize(28)
          .fontWeight(FontWeight.Bold)
          .fontColor('#0ea5e9')
          .textShadow({ radius: 1, color: '#00000020', offsetX: 0, offsetY: 1 });
        Text('📊 总进度')
          .fontSize(13)
          .fontColor('#0284c7')
          .fontWeight(FontWeight.Medium);
      }
      .backgroundColor('#f0f9ff')
      .borderRadius(16)
      .padding({ left: 20, right: 20, top: 18, bottom: 18 })
      .layoutWeight(1)
      .border({ width: 1, color: '#bae6fd' })
      .shadow({ radius: 8, color: '#0ea5e920', offsetX: 0, offsetY: 4 });
    }
  }

  @Builder
  buildGraphView() {
    Column({ space: 20 }) {
      Row({ space: 12 }) {
        Text('🌐')
          .fontSize(20);
        Text('知识点关系图')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b');
      }
      .alignSelf(ItemAlign.Start);

      // 图形视图容器
      Stack() {
        // 背景渐变
        Column()
          .width('100%')
          .height(450)
          .backgroundColor('#ffffff')
          .borderRadius(20)
          .border({ width: 2, color: '#e2e8f0' })
          .shadow({ radius: 12, color: '#00000015', offsetX: 0, offsetY: 6 })
          .backgroundImage('linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)');

        // 连接线背景层
        Canvas()
          .width('100%')
          .height(450)
          .onReady(() => {
            this.drawConnections();
          });

        // 知识节点
        ForEach(this.getFilteredNodes(), (node: KnowledgeNode, index: number) => {
          this.buildKnowledgeNode(node, index);
        });
      }
      .width('100%')
      .height(450);

      // 美化的图例
      Row({ space: 32 }) {
        Row({ space: 10 }) {
          Circle({ width: 16, height: 16 })
            .fill(this.COMPLETED_COLOR)
            .shadow({ radius: 4, color: '#10b98130', offsetX: 0, offsetY: 2 });
          Text('✅ 已完成')
            .fontSize(15)
            .fontColor('#059669')
            .fontWeight(FontWeight.Medium);
        }

        Row({ space: 10 }) {
          Circle({ width: 16, height: 16 })
            .fill(this.PENDING_COLOR)
            .shadow({ radius: 4, color: '#8b5cf630', offsetX: 0, offsetY: 2 });
          Text('⏳ 待学习')
            .fontSize(15)
            .fontColor('#7c3aed')
            .fontWeight(FontWeight.Medium);
        }

        Row({ space: 10 }) {
          Line()
            .width(24)
            .height(3)
            .backgroundColor(this.CONNECTION_COLOR)
            .borderRadius(2);
          Text('🔗 关联')
            .fontSize(15)
            .fontColor('#64748b')
            .fontWeight(FontWeight.Medium);
        }
      }
      .justifyContent(FlexAlign.Center)
      .padding(16)
      .backgroundColor('#ffffff')
      .borderRadius(16)
      .shadow({ radius: 6, color: '#00000010', offsetX: 0, offsetY: 3 });
    }
  }

  @Builder
  buildListView() {
    Column({ space: 16 }) {
      Text('📋 知识点列表')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#374151')
        .alignSelf(ItemAlign.Start);

      Scroll() {
        Column({ space: 12 }) {
          ForEach(this.getFilteredNodes(), (node: KnowledgeNode) => {
            this.buildNodeListItem(node);
          });
        }
      }
      .scrollable(ScrollDirection.Vertical)
      .scrollBar(BarState.Auto)
      .layoutWeight(1);
    }
  }

  @Builder
  buildKnowledgeNode(node: KnowledgeNode, index: number) {
    Column({ space: 6 }) {
      Stack() {
        // 外圈光晕效果
        Circle({ width: this.NODE_SIZE + 8, height: this.NODE_SIZE + 8 })
          .fill(node.completed ? '#10b98120' : '#8b5cf620')
          .blur(4);

        // 主节点
        Circle({ width: this.NODE_SIZE, height: this.NODE_SIZE })
          .fill(node.completed ?
            'linear-gradient(135deg, #10b981 0%, #059669 100%)' :
            'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)')
          .stroke('#ffffff')
          .strokeWidth(4)
          .shadow({ radius: 8, color: node.completed ? '#10b98140' : '#8b5cf640', offsetX: 0, offsetY: 4 })
          .scale({ x: 1, y: 1 })
          .animation({
            duration: 200,
            curve: Curve.EaseInOut,
            iterations: 1
          });

        // 节点图标
        Text(node.completed ? '✓' : '📚')
          .fontSize(24)
          .fontColor('#ffffff')
          .fontWeight(FontWeight.Bold)
          .textShadow({ radius: 2, color: '#00000040', offsetX: 0, offsetY: 1 });
      }
      .onClick(() => {
        this.selectedNode = node;
        this.showNodeDetail = true;
      });

      // 节点标题
      Text(node.title)
        .fontSize(13)
        .fontColor('#1e293b')
        .fontWeight(FontWeight.Medium)
        .textAlign(TextAlign.Center)
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
        .width(90)
        .padding({ left: 4, right: 4, top: 4, bottom: 4 })
        .backgroundColor('#ffffff')
        .borderRadius(8)
        .shadow({ radius: 4, color: '#00000015', offsetX: 0, offsetY: 2 });
    }
    .position({ x: node.x, y: node.y })
    .zIndex(10);
  }

  @Builder
  buildNodeListItem(node: KnowledgeNode) {
    Row({ space: 20 }) {
      Stack() {
        Circle({ width: 20, height: 20 })
          .fill(node.completed ?
            'linear-gradient(135deg, #10b981 0%, #059669 100%)' :
            'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)')
          .shadow({ radius: 4, color: node.completed ? '#10b98130' : '#8b5cf630', offsetX: 0, offsetY: 2 });

        Text(node.completed ? '✓' : '📖')
          .fontSize(12)
          .fontColor('#ffffff')
          .fontWeight(FontWeight.Bold);
      }

      Column({ space: 6 }) {
        Text(node.title)
          .fontSize(17)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1e293b');

        Text(node.description)
          .fontSize(15)
          .fontColor('#64748b')
          .maxLines(2)
          .lineHeight(22)
          .textOverflow({ overflow: TextOverflow.Ellipsis });
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1);

      Button(node.completed ? '🎉 已完成' : '🚀 开始学习')
        .backgroundColor(node.completed ? '#f0fdf4' : '#faf5ff')
        .fontColor(node.completed ? '#16a34a' : '#7c3aed')
        .borderRadius(12)
        .padding({ left: 16, right: 16, top: 10, bottom: 10 })
        .border({ width: 1, color: node.completed ? '#bbf7d0' : '#e9d5ff' })
        .shadow({ radius: 4, color: '#00000010', offsetX: 0, offsetY: 2 })
        .onClick(() => {
          this.selectedNode = node;
          this.showNodeDetail = true;
        });
    }
    .width('100%')
    .padding({ left: 20, right: 20, top: 18, bottom: 18 })
    .backgroundColor('#ffffff')
    .borderRadius(16)
    .border({ width: 1, color: '#e2e8f0' })
    .shadow({ radius: 8, color: '#00000012', offsetX: 0, offsetY: 4 })
    .onClick(() => {
      this.selectedNode = node;
      this.showNodeDetail = true;
    });
  }

  @Builder
  buildNodeDetailDialog() {
    Stack() {
      // 背景遮罩
      Column()
        .width('100%')
        .height('100%')
        .backgroundColor('#000000a0')
        .backdropBlur(8)
        .onClick(() => {
          this.showNodeDetail = false;
          this.selectedNode = null;
        });

      // 对话框内容
      Column({ space: 24 }) {
        Row({ space: 16 }) {
          Row({ space: 12 }) {
            Text('🎓')
              .fontSize(24);
            Text('知识点详情')
              .fontSize(20)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1e293b');
          }
          .layoutWeight(1);

          Button('✕')
            .backgroundColor('#f1f5f9')
            .fontColor('#64748b')
            .borderRadius(12)
            .padding({ left: 12, right: 12, top: 8, bottom: 8 })
            .shadow({ radius: 4, color: '#00000015', offsetX: 0, offsetY: 2 })
            .onClick(() => {
              this.showNodeDetail = false;
              this.selectedNode = null;
            });
        }

        if (this.selectedNode) {
          Column({ space: 20 }) {
            // 节点标题和状态
            Column({ space: 12 }) {
              Text(this.selectedNode.title)
                .fontSize(24)
                .fontWeight(FontWeight.Bold)
                .fontColor('#1e293b')
                .textShadow({ radius: 1, color: '#00000020', offsetX: 0, offsetY: 1 });

              Row({ space: 12 }) {
                Text('📊 状态:')
                  .fontSize(16)
                  .fontColor('#64748b')
                  .fontWeight(FontWeight.Medium);

                Text(this.selectedNode.completed ? '✅ 已完成' : '⏳ 待学习')
                  .fontSize(16)
                  .fontColor(this.selectedNode.completed ? '#059669' : '#7c3aed')
                  .fontWeight(FontWeight.Bold)
                  .padding({ left: 12, right: 12, top: 6, bottom: 6 })
                  .backgroundColor(this.selectedNode.completed ? '#f0fdf4' : '#faf5ff')
                  .borderRadius(8)
                  .border({ width: 1, color: this.selectedNode.completed ? '#bbf7d0' : '#e9d5ff' });
              }
              .alignSelf(ItemAlign.Start);
            }
            .alignItems(HorizontalAlign.Start);

            // 描述
            Column({ space: 8 }) {
              Text('📝 详细描述')
                .fontSize(16)
                .fontColor('#64748b')
                .fontWeight(FontWeight.Medium)
                .alignSelf(ItemAlign.Start);

              Text(this.selectedNode.description)
                .fontSize(16)
                .fontColor('#475569')
                .lineHeight(26)
                .padding(16)
                .backgroundColor('#f8fafc')
                .borderRadius(12)
                .border({ width: 1, color: '#e2e8f0' });
            }
            .alignItems(HorizontalAlign.Start);

            // 相关知识点
            if (this.selectedNode.connections.length > 0) {
              Column({ space: 12 }) {
                Text('🔗 相关知识点')
                  .fontSize(16)
                  .fontColor('#64748b')
                  .fontWeight(FontWeight.Medium)
                  .alignSelf(ItemAlign.Start);

                Column({ space: 8 }) {
                  ForEach(this.getConnectedNodes(this.selectedNode), (connectedNode: KnowledgeNode) => {
                    Row({ space: 12 }) {
                      Text('•')
                        .fontSize(16)
                        .fontColor('#8b5cf6');
                      Text(connectedNode.title)
                        .fontSize(15)
                        .fontColor('#475569')
                        .layoutWeight(1);
                    }
                    .width('100%')
                    .padding({ left: 12, right: 12, top: 8, bottom: 8 })
                    .backgroundColor('#ffffff')
                    .borderRadius(8)
                    .border({ width: 1, color: '#e2e8f0' });
                  });
                }
              }
              .alignItems(HorizontalAlign.Start);
            }

            // 操作按钮
            Button(this.selectedNode.completed ? '🔄 重新学习' : '🚀 开始学习')
              .backgroundColor(this.selectedNode.completed ? '#f0fdf4' : '#6366f1')
              .fontColor(this.selectedNode.completed ? '#16a34a' : '#ffffff')
              .borderRadius(12)
              .padding({ left: 32, right: 32, top: 16, bottom: 16 })
              .fontSize(16)
              .fontWeight(FontWeight.Bold)
              .border({ width: this.selectedNode.completed ? 1 : 0, color: '#bbf7d0' })
              .shadow({ radius: 8, color: '#00000020', offsetX: 0, offsetY: 4 })
              .onClick(() => {
                this.startLearning(this.selectedNode!);
              });
          }
          .alignItems(HorizontalAlign.Start);
        }
      }
      .width('92%')
      .height('85%')
      .backgroundColor('#ffffff')
      .borderRadius(20)
      .padding(28)
      .border({ width: 1, color: '#e2e8f0' })
      .shadow({ radius: 24, color: '#00000025', offsetX: 0, offsetY: 12 })
      .backgroundImage('linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)');
    }
    .width('100%')
    .height('100%')
    .position({ x: 0, y: 0 })
    .zIndex(1000);
  }

  // 辅助方法
  getFilteredNodes(): KnowledgeNode[] {
    if (!this.searchText) {
      return this.knowledgeNodes;
    }
    return this.knowledgeNodes.filter(node =>
    node.title.toLowerCase().includes(this.searchText.toLowerCase()) ||
    node.description.toLowerCase().includes(this.searchText.toLowerCase())
    );
  }

  getCompletedCount(): number {
    return this.knowledgeNodes.filter(node => node.completed).length;
  }

  getPendingCount(): number {
    return this.knowledgeNodes.filter(node => !node.completed).length;
  }

  getProgressPercentage(): number {
    if (this.knowledgeNodes.length === 0) return 0;
    return Math.round((this.getCompletedCount() / this.knowledgeNodes.length) * 100);
  }

  getConnectedNodes(node: KnowledgeNode): KnowledgeNode[] {
    return this.knowledgeNodes.filter(n => node.connections.includes(n.id));
  }

  drawConnections() {
    // 绘制知识点之间的连接线
    // 这里可以实现Canvas绘制连接线的逻辑
    // 由于HarmonyOS的Canvas API限制，这里提供一个简化的实现思路
  }

  showSearchDialog() {
    // 实现搜索对话框
    this.searchText = this.searchText === '' ? 'search' : '';
  }

  startLearning(node: KnowledgeNode) {
    // 实现开始学习功能
    node.completed = !node.completed;
    this.showNodeDetail = false;
    this.selectedNode = null;
  }
}