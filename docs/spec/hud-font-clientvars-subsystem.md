# HUD / 字体 / clientvars (Client Display) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M6 / Stage H2
- 关联平行任务：PH-6（本框架）；HUD 内容读 PS-5（PE meter，延后）；Aklo 字体供 PH-5 书 whisper 页 + 聊天
- 状态：HUD/字体/clientvars 框架交付并验证（两节点编译 + runClient 抵标题屏）；HUD 内容 + Aklo 位图 + 全 clientvars 色 + keybind = 内容/人工，待其能量物品/staff/PK 资产依赖落地
- 负责：PH-6
- 最后更新：2026-07-22

## 1. 概述 / 目标

AbyssalCraft 的客户端显示三件套：**HUD 叠加**（PE meter / spirit-tablet 路径 / 维度信息等 in-game overlay）、**Aklo 字体**（埃尔德里奇字母，用于 Necronomicon whisper / lore）、**clientvars**（`clientvars.json` 驱动的可热重载客户端颜色，供群系/维度/药水着色）。本任务交付**框架**（字体 accessor + 字体资产 / clientvars POJO + 热重载 loader / HUD overlay 注册 compat + pilot）；HUD 具体内容、忠实 Aklo 位图、全 ~100 clientvars 色字段 = 内容（延后，见 §2）。

## 2. 范围

- 含：
  - **字体** `client/font/AkloFont`（`location()=ACRef.id("aklo")`）+ `assets/abyssalcraft/font/aklo.json`（`reference→minecraft:default` 占位）。
  - **clientvars** `client/hud/{ClientVars(Gson POJO 色子集),ClientVarsManager(ResourceManagerReloadListener 热重载 + accessor)}` + `assets/abyssalcraft/clientvars.json`。
  - **HUD** `platform/ClientHooksCompat`（HUD overlay + reload listener 注册的 loader fork）+ `client/hud/ACHud`（pilot overlay + 注册 relay）。
  - 主类 client block（`SideExecutor.runWhenClient`）+2 行 `ACHud.register()` + `ClientHooksCompat.attach(modBus)`。
- 不含（延后内容，依赖未移植 / 需人工）：
  - **HUD 内容**：PE meter（读手持 `IEnergyContainerItem`，PS-5 能量物品未移植）、spirit-tablet path/filter 显示、维度信息——依赖未移植能量物品 / staff / spirit tablet。
  - **忠实 Aklo glyph 位图**：`assets/.../textures/font/aklo.png` + bitmap provider（现 reference 占位）——资产迁移（PK）。
  - **全 ~100 clientvars 色字段**：1.12.2 `ClientVars` 全量（全群系 grass/foliage/water/sky + 全维度 + 全 boss death + crystal colors）——现代表子集，余 PK。
  - **5 keybind**：staff_mode / use_cage / spirit_tablet_mode/filter/path——依赖未移植 staff / cage / spirit tablet 物品。

## 3. 设计 / 架构

- 关键类：
  - `client/font/AkloFont`：`location():ResourceLocation`=`ACRef.id("aklo")`；用法 `Style.EMPTY.withFont(AkloFont.location())`。`aklo.json` 现 `{"providers":[{"type":"reference","id":"minecraft:default"}]}`（可加载占位，Aklo 位图落地时换 bitmap provider）。
  - `client/hud/ClientVars`：Gson POJO（代表色子集：coralium/dread plague + antimatter potion color、AW/dreadlands RGB）。字段名匹配 `clientvars.json`。
  - `client/hud/ClientVarsManager implements ResourceManagerReloadListener`：`onResourceManagerReload` 从资源管理器读 `abyssalcraft:clientvars.json` → Gson 解析 → 缓存；`static get():ClientVars`（默认值直到加载 / 解析失败）。热重载：作为 client reload listener 注册，`/reload`+F3+T 触发重解析。
  - `platform/ClientHooksCompat`（**loader fork**，client-only）：中性 `HudRenderer`（`render(GuiGraphics,int width,int height)`）；`queueOverlay`/`queueReloadListener` + `attach(modBus)`——forge 分支 `RegisterGuiOverlaysEvent.registerAboveAll(String,IGuiOverlay)`（`IGuiOverlay.render(gui,gg,pt,w,h)`）↔ neo 分支 `RegisterGuiLayersEvent.registerAboveAll(ResourceLocation,LayeredDraw.Layer)`（`Layer.render(gg,DeltaTracker)`，宽高经 `gg.guiWidth/guiHeight`）；reload listener 两端同 `registerReloadListener(PreparableReloadListener)`。
  - `client/hud/ACHud`：`register()` = `queueReloadListener(ClientVarsManager.instance())` + `queueOverlay("pe_meter", ...)`；pilot overlay 现画空（PE meter 待能量物品）。

## 4. 子系统内契约

- 对外 API：`AkloFont.location()` 供文本着 Aklo 字体；`ClientVarsManager.get()` 供渲染读颜色；`ClientHooksCompat.queueOverlay/queueReloadListener` 供内容注册 HUD/listener。
- **HUD 读 PS-5**（延后）：PE meter overlay 读手持 `IEnergyContainerItem`（PS-5 能量物品变体，未移植）→ 画 PE 条。
- **字体供 PH-5**：Necronomicon whisper 页用 `AkloFont`。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：**新增 1**——`platform/ClientHooksCompat`（加载器轴：HUD overlay 事件 + overlay 接口 + reload listener 事件）。业务 `AkloFont`/`ClientVars`/`ClientVarsManager`/`ACHud` 零 `//?`。
- **HUD overlay fork**（加载器，1.20↔1.21 Mojang/loader 重构 in-game GUI）：forge 1.20.1 `RegisterGuiOverlaysEvent`+`IGuiOverlay`（`ForgeGui,GuiGraphics,float,int,int`）↔ neo 1.21 `RegisterGuiLayersEvent`+`LayeredDraw.Layer`（`GuiGraphics,DeltaTracker`）。**javap 双 jar 核**两端 `registerAboveAll` 签名 + overlay/layer render 签名 + `GuiGraphics.guiWidth/guiHeight`（neo）。中性 `HudRenderer(gg,w,h)` 吸收。
- **reload listener** 两端**同签名** `registerReloadListener(PreparableReloadListener)`（javap 核）→ 仅 event 类 import 分叉（forge `net.minecraftforge.client.event.RegisterClientReloadListenersEvent` ↔ neo `net.neoforged.neoforge.client.event.*`），`attach` 共享该行。
- **字体/clientvars 资产** 跨版本不分叉（`assets/<ns>/font/*.json`、任意 `assets/<ns>/*.json` 两端同路径）；`reference` 字体 provider 两端支持（1.20+）。
- **client-only**：`ClientHooksCompat` + 全 PH-6 类经 `SideExecutor.runWhenClient` 挂，dedicated server 不加载。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容**（同 PS-5..11/PH-5 先例）：HUD 内容耦合未移植能量/staff/spirit-tablet 物品；Aklo 位图 + 全 clientvars 色 = PK 资产 → 先交付 accessor + loader + overlay 注册 compat + pilot，内容随物品/资产落地。
- **overlay/reload 签名先 javap 后写**（本会话实证价值）：HUD overlay 是 1.20↔1.21 大 fork（`IGuiOverlay`→`LayeredDraw.Layer`）；先 `javap -cp <jar>` 双端核 `RegisterGuiOverlaysEvent`/`RegisterGuiLayersEvent`/`IGuiOverlay`/`LayeredDraw$Layer`/`RegisterClientReloadListenersEvent`/`GuiGraphics.guiWidth` 确切签名 → 一次编译通过（避多轮猜）。发现 reload listener 两端同签名（仅 import 分叉）。
- **aklo.json reference 占位**：忠实 Aklo 位图（PK）未到 → `aklo.json` 用 `reference→minecraft:default`，资源加载零错（bitmap provider 缺 PNG 会崩）；accessor `location()` 已可用，落地位图时只换 provider。
- **clientvars 代表子集**：1.12.2 `ClientVars` ~100 字段 → 现代表子集（potion/dimension 色）验证 Gson 解析 + 热重载管线；全字段随用色处（biome/维度/药水着色）落地补。
- **验证限于加载**：headless `runClient` 不能看 HUD/字体渲染/热重载观感 → 只验「aklo.json/clientvars.json 解析 + HUD overlay 注册 + 抵标题屏不崩」；实际观感 = 人工目视（同 E1/E2 先例）。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL（`ClientHooksCompat` HUD overlay + reload listener fork 双端）。
- **两节点 `runClient` 抵标题屏资源初始化**：Sound engine started + 全 texture atlas；`aklo.json` 字体 provider 解析零错（malformed 会抛字体错，无）+ `clientvars.json` 经 `ClientVarsManager` reload listener 解析零错（失败会日志 "Failed to load clientvars.json"，无）+ HUD overlay 注册零错。
- 未机核项（如实标注）：**HUD 渲染 / Aklo 字体外观 / clientvars 着色 / 热重载**需 live 客户端 + 手持能量物品（内容）+ 人工目视；overlay 的实际绘制仅 compile + 注册验证。

## 修订日志

- 2026-07-22：PH-6 建框架——`client/font/AkloFont`+aklo.json、`client/hud/{ClientVars,ClientVarsManager 热重载,ACHud pilot}`+clientvars.json、`platform/ClientHooksCompat`（HUD overlay + reload listener fork，javap 双 jar 核）、主类 client block +2 行。两节点编译 + runClient 抵标题屏（资源解析 + HUD 注册零错）。HUD 内容 + Aklo 位图 + 全 clientvars 色 + keybind 延后（依赖未移植能量/staff 物品 + PK 资产）；观感人工。见平行表 PH-6。
