# Rending Pedestal GUI 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M6 / Stage H2-GUI
- 关联平行任务：GUI-COMPLETION（本框架）；读 rending-subsystem.md（能量逻辑）
- 状态：完整实现，包含PE显示、四类Rending能量分账显示
- 负责：GUI-COMPLETION
- 最后更新：2026-07-27

## 1. 概述 / 目标

Rending Pedestal 的容器GUI界面，显示Potential Energy (PE) 总量与四类Rending能量（Abyssal/Dread/Omothol/Shadow）的当前值/阈值分账。只读显示，无交互按钮。

## 2. 范围

- 含：`client/screen/machine/rendingpedestal/RendingPedestalScreen` - GUI渲染与能量显示
- 含：`textures/gui/container/rending_pedestal.png` - 256x256 GUI纹理
- 含：`system/rending/RendingEnergyType` - 四类能量类型枚举
- 不含：能量逻辑（已在 rending-subsystem.md）
- 不含：lang翻译键（PK-4）

## 3. 设计 / 架构

### 关键类
- `RendingPedestalScreen extends AbstractContainerScreen<RendingPedestalMenu>`
  - `imageHeight = 166`：标准容器高度
  - `renderBg`：绘制主纹理（无动画元素）
  - `renderLabels`：绘制标题、PE总量、四类能量分账
  - `drawLedger`：通用能量显示方法（prefix + current/threshold）

### 能量显示布局
```
标题居中（6像素高度）
PE: {current}/{max} PE（20像素高度，居中）
A: {abyssal}/{threshold}  (55, 29)
D: {dread}/{threshold}     (108, 29)
O: {omothol}/{threshold}   (55, 37)
S: {shadow}/{threshold}    (108, 37)
玩家背包标签（inventoryLabelY）
```

### Menu数据契约
- `menu.potentialEnergy()` - 当前PE
- `menu.maxPotentialEnergy()` - 最大PE
- `menu.rendingEnergy(RendingEnergyType)` - 四类能量当前值
- `RendingEnergyType.threshold()` - 各类型阈值常量

## 4. 子系统内契约

- 对外API：`RendingPedestalScreen` 由 `MenuScreens.register` 注册到 `RendingPedestalMenu`
- 读 Menu：所有能量数据只读，无写操作
- 能量类型：`ABYSSAL` / `DREAD` / `OMOTHOL` / `SHADOW` - 四类Rending能量
- 字符串格式：`prefix + ": " + current + "/" + threshold` - 统一显示格式

## 5. 跨版本 / 加载器要点

- 兼容层：`ClientScreenCompat.background` - 1.20.1/1.21 renderBackground签名差异
- `//?` 分叉点：无业务分叉
- 纹理格式：256x256 PNG（标准Minecraft GUI纹理格式）
- 文字渲染：使用 `GuiGraphics.drawString` 带阴影=false，颜色=0x404040

## 6. 实现记忆 / 踩坑

- **四类能量分两列显示**：左列（A/O）在55x，右列（D/S）在108x，间隔8像素
- **PE显示居中**：使用 `imageWidth / 2 - font.width(pe) / 2` 计算x坐标
- **drawLedger复用**：统一方法避免重复代码，传入类型/前缀/坐标
- **无交互逻辑**：纯显示GUI，无按钮/点击/网络消息

## 7. 验证 / DoD

- ✅ PNG格式验证：256x256，真PNG文件头
- ✅ blit尺寸匹配：imageWidth=176, imageHeight=166，标准容器尺寸
- ✅ 双端编译：无错误，AbstractContainerScreen继承正确
- ✅ 能量显示布局：四类能量分两列，PE居中显示
- ✅ 只读契约：无写操作，纯从Menu读取数据
- 未机核：真人打开GUI观察能量数值更新

## 修订日志

- 2026-07-27：GUI-COMPLETION 创建完整spec，验证PNG、布局、只读契约与双端编译
