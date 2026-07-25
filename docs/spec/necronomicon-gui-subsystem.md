# 死灵之书 GUI (Necronomicon Book Screen) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M6 / Stage H2
- 关联平行任务：PH-5（本框架）；**读 PS-8**（研究解锁门控 entry 显示）+ **读 PS-2**（同步 necrodata）；被 PS-6（仪式）/PS-7（法术）/PS-10（PoP）的 entry 引用
- 状态：五本书物品、常驻信息树与42项五分类研究目录/状态/hint已接入，服务端necrodata同步handler已落地；完整旧recipe/ritual/spell/PoP页面、actions、忠实贴图布局与双端真人目视留T7.8c/T6.2b
- 负责：PH-5
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的 Necronomicon 书界面——一本自定义 GUI 书，按章节/页组织全 mod 的说明。当前五本书可打开 Screen，常驻信息树之外新增42项研究目录，按 biome/dimension/entity/misc/book 分类显示 locked/completed 与具体条件 hint；普通内容节点继续读取客户端已同步的 necrodata 只读门控。

## 2. 范围

- 含：`client/necronomicon/{NecronomiconEntry,NecronomiconScreen,ACNecronomicon}`——递归书数据节点 + 书 Screen（导航 + PS-8 门控渲染）+ pilot 内容树 + open 入口。
- 不含（延后内容，依赖未移植 / 需人工）：
  - **20+ 具体章节/页**：1.12.2 `Chapters`/`Pages` 全量 + 各 recipe/ritual/spell/PoP entry（`GuiNecronomicon*Entry`）——依赖未移植 research 物品、页贴图、正文文本。
  - **忠实书贴图 / 页布局**：1.12.2 book 纹理 + 双页布局 + 按钮贴图（next/home/info/category）——资产迁移（PK）；本框架用 vanilla 背景 + 文字，fork-free。
  - **配方页渲染**：crystallizer/materializer/transmutator/anvil recipe 页（机器已移植，但页渲染 + 配方拉取 = 内容细节）。
  - **Aklo 字体正文**：whisper 页用 Aklo 字体（PH-6 `AkloFont`）——字体框架 PH-6 已交付，具体 whisper 页属内容。
  - **lang**：`gui.abyssalcraft.necronomicon.*` 翻译键（PK-4）。

## 3. 设计 / 架构

- 关键类：
  - `NecronomiconEntry`（递归数据节点，忠实 `api.necronomicon.NecroData` 树）：`id` / `titleKey` / `textKey`（nullable 正文）/ `icon:ItemStack` / 可选 `researchId:ResourceLocation`（门控）/ `children:List<NecronomiconEntry>`。builder `addChild`/`setResearch`。
  - `NecronomiconScreen extends Screen`（忠实 `GuiNecronomicon`，**plain Screen 非 menu**）：`Deque<NecronomiconEntry> path` 导航栈；`init` 为当前 entry 的**可见** children 建 category 按钮（下钻 `navigateTo`）+ path>1 时 back 按钮 + done；`render` 画背景（compat）+ super（按钮）+ 标题/正文（`font.split` 换行）最上层；`open(root)` 静态 = `Minecraft.getInstance().setScreen(...)`。
  - `ACNecronomicon`：pilot 内容树（root + intro entry）+ `open()`（供未移植书物品右键调）。

## 4. 子系统内契约

- 对外 API：内容注册 `NecronomiconEntry` 树 + `addChild` 挂章节/页；书物品调 `ACNecronomicon.open()`；仪式/法术/PoP 的说明 entry 挂本树、`setResearch(rid)` 门控。
- **读 PS-8 / PS-2（核心跨任务契约）**：entry 显示门控读玩家 necrodata（PS-2 `NecroDataCapability.get(clientPlayer)`）——`researchId==null || data.hasUnlockedAllKnowledge() || data.getCompletedResearches().contains(rid.toString())`。服务端通过协议v2增量/全量消息同步权威副本；登录/重生/换维延迟与开书补同步受配置控制。
- **只读门控**（避副作用）：不调 PS-8 `KnowledgeGate.isUnlocked`（其有 auto-complete 写副作用，不宜每帧客户端调）；改用 PS-2 `NecroData` 公 getter 只读判。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：**无新增**（复用既有 `platform/ClientScreenCompat.background`）。
- `//?` 分叉点：**零**（业务）。用 vanilla `Screen`/`Button.builder`/`GuiGraphics.drawCenteredString`/`font.split`/`Minecraft.setScreen`/`ItemStack`/`ResourceLocation`。
- **`renderBackground` 签名分叉**（1.20.1 `(GuiGraphics)` ↔ 1.21 `(GuiGraphics,int,int,float)`）→ 走既有 `ClientScreenCompat.background`（PA-1/机器屏幕已建）。
- **`Screen.render` 行为分叉坑**（关键）：1.20.1 `Screen.render` **不**自画背景（子类须先 `renderBackground`）；1.21 `super.render` 自画背景 → 若自定义文字在 `super.render` 前画会被 1.21 的背景覆盖 → **自定义标题/正文放 `super.render` 之后**（最上层），两端皆正确（1.21 背景被画两次无害）。
- **client-only**：`NecronomiconScreen` 等仅客户端加载（书物品右键 = 客户端上下文 `setScreen`），dedicated server 不构造；编译入合并 jar 无碍。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容**（同 PS-5..11 先例）：书深耦合 20+ research entry + 页贴图/文本 + 书物品（全未移植）→ 先交付可编译/加载的 Screen 外壳 + 递归模型 + 导航 + PS-8 门控 + pilot，具体章节/页随 research/物品/资产落地时 `addChild`。
- **`open` 命名冲突**：`NecronomiconScreen` 曾同时有私有实例 `open(entry)`（导航）+ 静态 `open(root)`（打开）→ 同签名冲突（"已定义方法 open"）→ 导航方法改名 `navigateTo`。
- **门控只读避副作用**：见 §4；client 每帧渲染不宜触发 PS-8 的研究 auto-complete，故用 PS-2 `NecroData` getter 只读判。
- **验证限于加载**：headless `runClient` 不能开 Screen/交互/截图 → 只验「类加载 + 资源解析 + 抵标题屏不崩」；实际翻页/分类/渲染观感 = 人工目视（同 E1/E2 先例）。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL（`Screen` render fork 顺序 + `Button.builder`/`font.split` 双端）。
- **两节点 `runClient` 抵标题屏资源初始化**：Sound engine started + 全 texture atlas 建成；`NecronomiconScreen`/`NecronomiconEntry`/`ACNecronomicon` 类加载零 link 错、零崩（客户端不因本框架破加载）。
- 已机核：双端编译/build/JAR，42项研究与19个研究UI语言键在8语言对等；五本书、bookType门控与客户端同步类可加载。
- 未机核：双端真人打开/翻页/同步即时变化与长文本布局目视；完整旧页面/actions仍属T7.8c/T6.2b。

## 修订日志

- 2026-07-25：RR-KNOWLEDGE（CR-70）接入42项五分类研究目录、状态/hint与协议v2同步；完整旧页面/actions和真人目视继续待办。
- 2026-07-22：PH-5 建框架——`client/necronomicon/**`（递归 `NecronomiconEntry` + `NecronomiconScreen` 导航 + PS-8 只读门控 + pilot）；两节点编译 + runClient 抵标题屏。读 PS-8/PS-2 necrodata 门控 entry。20+ 具体章节/页 + 书贴图/布局 + 书物品 + lang 延后（依赖未移植 research/物品/贴图）；翻页/渲染观感人工。见平行表 PH-5。
