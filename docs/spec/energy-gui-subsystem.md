# Energy Container/Depositioner GUI 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M6 / Stage H2-GUI
- 关联平行任务：GUI-COMPLETION（本框架）；读 energy-subsystem.md（能量逻辑）
- 状态：完整实现，包含两个GUI（Container纯显示，Depositioner带进度）
- 负责：GUI-COMPLETION
- 最后更新：2026-07-27

## 1. 概述 / 目标

AbyssalCraft 能量系统的两个容器GUI：
1. **Energy Container**：纯PE存储容器，只读显示当前/最大PE
2. **Energy Depositioner**：带传输进度的能量存取器，显示PE + 传输进度条

## 2. 范围

- 含：`client/screen/energy/EnergyContainerScreen` - 纯显示GUI
- 含：`client/screen/energy/EnergyDepositionerScreen` - 带进度条GUI
- 含：`textures/gui/container/energy_container.png` - 256x256 Container纹理
- 含：`textures/gui/container/energy_depositioner.png` - 256x256 Depositioner纹理
- 不含：能量逻辑（已在 energy-subsystem.md）
- 不含：lang翻译键（PK-4）

## 3. 设计 / 架构

### EnergyContainerScreen（纯显示）
```java
extends AbstractContainerScreen<EnergyContainerMenu>
imageHeight = 166 // 标准容器高度
renderBg: blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)
renderLabels:
  - 标题居中（6像素）
  - PE: {current}/{max} PE（20像素，居中）
  - 玩家背包标签
```

### EnergyDepositionerScreen（带进度）
```java
extends AbstractContainerScreen<EnergyDepositionerMenu>
imageHeight = 166 // 标准容器高度
renderBg:
  - blit主纹理（0,0 → imageWidth x imageHeight）
  - blit进度条（源176,14 → 目标76,38，宽度progressWidth）
renderLabels:
  - 标题居中（6像素）
  - PE: {current}/{max} PE（20像素，居中）
  - 传输状态文本（30像素）
  - 玩家背包标签
进度计算: Math.round(menu.progress() * 24.0F)
```

### 纹理布局对比
| 元素 | Container | Depositioner |
|------|-----------|--------------|
| 主GUI | 0,0 (176x166) | 0,0 (176x166) |
| 进度条sprite | 无 | 176,14 (24x16) |
| PNG尺寸 | 256x256 | 256x256 |

## 4. 子系统内契约

- 对外API：两个Screen由 `MenuScreens.register` 分别注册到对应Menu
- 读 Menu：
  - 共同：`potentialEnergy()` / `maxPotentialEnergy()`
  - Depositioner专属：`progress()` / `transferring()` - 传输状态
- PE显示格式：`{current}/{max} PE` - 统一字符串格式
- 进度条宽度：24像素（与State Transformer一致）

## 5. 跨版本 / 加载器要点

- 兼容层：`ClientScreenCompat.background` - 1.20.1/1.21 renderBackground签名差异
- `//?` 分叉点：无业务分叉
- 纹理格式：256x256 PNG（标准Minecraft GUI纹理格式）
- 文字渲染：`drawString(font, text, x, y, 0x404040, false)` - 深灰色无阴影

## 6. 实现记忆 / 踩坑

- **两个GUI共享PE显示逻辑**：居中计算 `imageWidth / 2 - font.width(energy) / 2`
- **进度条位置**：Depositioner进度条在(76, 38)，与Container的槽位布局不同
- **进度条宽度+1**：`progressWidth + 1` 避免像素间隙（同State Transformer）
- **transferring状态显示**：Depositioner在30像素高度显示传输中/完成状态
- **Container无交互**：纯显示GUI，无按钮/进度条/网络消息

## 7. 验证 / DoD

- ✅ PNG格式验证：两个256x256文件，真PNG文件头
- ✅ blit尺寸匹配：两者都是176x166标准容器尺寸
- ✅ 双端编译：两个Screen类无错误
- ✅ Container纯显示：无交互逻辑，只读Menu数据
- ✅ Depositioner进度条：24像素宽度，随progress()更新
- ✅ PE显示居中：两个GUI使用相同的居中算法
- 未机核：真人打开两个GUI观察PE更新与Depositioner传输动画

## 修订日志

- 2026-07-27：GUI-COMPLETION 创建完整spec，验证两个PNG、布局差异与双端编译
