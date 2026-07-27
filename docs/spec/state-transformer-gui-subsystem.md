# State Transformer GUI 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M6 / Stage H2-GUI
- 关联平行任务：GUI-COMPLETION（本框架）；读 state-transformer-subsystem.md（机器逻辑）
- 状态：完整实现，包含模式切换按钮、进度显示、双端同步
- 负责：GUI-COMPLETION
- 最后更新：2026-07-27

## 1. 概述 / 目标

State Transformer 的容器GUI界面，显示机器状态、insert/extract模式切换按钮与转换进度条。玩家可通过按钮切换模式，实时查看转换进度。

## 2. 范围

- 含：`client/screen/machine/statetransformer/StateTransformerScreen` - GUI渲染与按钮交互
- 含：`textures/gui/container/state_transformer.png` - 256x256 GUI纹理（包含进度条sprite）
- 含：`net/server/UpdateModeMessage` - 模式切换网络消息
- 不含：机器逻辑（已在 state-transformer-subsystem.md）
- 不含：lang翻译键（PK-4）

## 3. 设计 / 架构

### 关键类
- `StateTransformerScreen extends AbstractContainerScreen<StateTransformerMenu>`
  - `imageHeight = 238`：扩展高度以容纳更多槽位
  - `modeButton`：insert/extract模式切换按钮（2, 95位置，40x20）
  - `renderBg`：绘制主纹理 + 进度条（24像素宽度，源自176,14）
  - `containerTick`：每tick更新按钮状态（processing时禁用）

### 纹理布局
- 主GUI：0,0起始，176x238基础区域
- 进度条sprite：176,14起始，24x16最大宽度
- 按钮文本：通过Component.translatable动态生成

### 网络协议
- `UpdateModeMessage(nextMode, 0)` → 服务端验证 → Menu同步 → 按钮标签更新

## 4. 子系统内契约

- 对外API：`StateTransformerScreen` 由 `MenuScreens.register` 注册到 `StateTransformerMenu`
- 读 Menu：`menu.mode()` / `menu.processing()` / `menu.progress()` - 只读同步状态
- 写网络：`ACNetwork.sendToServer(new UpdateModeMessage(nextMode, 0))` - 模式切换请求
- 进度计算：`Math.round(menu.progress() * 24.0F)` - 24像素进度条

## 5. 跨版本 / 加载器要点

- 兼容层：`ClientScreenCompat.background` - 1.20.1/1.21 renderBackground签名差异
- `//?` 分叉点：无业务分叉
- 纹理格式：256x256 PNG（标准Minecraft GUI纹理格式）
- blit坐标：使用imageWidth/imageHeight相对坐标，适配任意窗口大小

## 6. 实现记忆 / 踩坑

- **进度条重叠修复**：进度条宽度使用 `progressWidth + 1` 而非 `progressWidth`，避免像素间隙
- **按钮禁用逻辑**：processing时必须禁用按钮，防止模式切换中断转换
- **containerTick同步**：每tick检查menu状态并更新按钮，确保UI与服务端同步
- **inventoryLabelY调整**：设为 `imageHeight - 94` 以适配238高度

## 7. 验证 / DoD

- ✅ PNG格式验证：256x256，真PNG文件头
- ✅ blit尺寸匹配：imageWidth=176, imageHeight=238，与纹理布局一致
- ✅ 双端编译：无错误，AbstractContainerScreen继承正确
- ✅ 按钮交互：模式切换发送网络消息，processing时禁用
- ✅ 进度条渲染：24像素宽度，随menu.progress()同步更新
- 未机核：真人点击按钮观察模式切换与进度条动画

## 修订日志

- 2026-07-27：GUI-COMPLETION 创建完整spec，验证PNG、blit尺寸、网络协议与双端编译
