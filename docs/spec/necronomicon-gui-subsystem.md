# 死灵之书 GUI (Necronomicon Book Screen) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M6 / Stage H2
- 关联平行任务：PH-5（本框架）；**读 PS-8**（研究解锁门控 entry 显示）+ **读 PS-2**（同步 necrodata）；被 PS-6（仪式）/PS-7（法术）/PS-10（PoP）的 entry 引用
- 状态：已由 Patchouli 完整接管书籍 UI；五个原业务 Item 对应五本独立累积数据书，401 条内容 manifest、42 项研究、旧页研究门禁、页面动作、法术书入口和 crafting 快速查找均已接入。322 个旧页与无产物仪式/力量之地均使用人工审阅的专用标题，不再复用章节级泛称。
- 负责：PH-5
- 最后更新：2026-07-29

## 1. 概述 / 目标

AbyssalCraft 的 Necronomicon 由 Patchouli 提供展示、目录、翻页、配方和 advancement 门禁。五个 AbyssalCraft 书 Item 保留各自 ID、PE 容器与潜行动作，普通右键按书阶打开对应 Patchouli 数据书。五本书相互独立且严格累积：高阶书包含所有低阶条目，不将物品替换为 `patchouli:guide_book`。

## 2. 范围

- 含：
  - `PatchouliNecronomicon` 五本书 ID、书阶到书 ID 的映射和普通右键打开入口。
  - `PatchouliBookData` 五套累积书、401 内容条目、42 research quest、双版本 advancement、模板和 recipe 快速查找映射。
  - `PatchouliActionComponent` 页面研读和法术书按钮；`PatchouliManifestComponent` 权威 ritual/spell/PoP/research 数据展示。
  - `NecronomiconPageManifest` / `LegacyNecronomiconPageManifest` / `NecronomiconRecipePages` 内容、门禁、真实配方结果与可审计状态。
- 不含（最终人工验收）：
  - **旧页专用渲染**：322 个旧页均保留旧变量名、源码顺序、页码、book tier、标题/正文引用、视觉类型/引用、research 引用和原始构造表达式。89 个 IMAGE 页从 1.12.2 仓内快照迁移 75 张真实 PNG；精确复现旧 `drawTexturedModalRect` 的固定 1/256 UV，256 图采样 255x192、1024 图采样 1020x768，再缩放到 255x192 书页，而不是把 1024 图误裁成左上 256x256。扁平目录使用当前语言正文摘要区分同章节页面，页顶标题附源页码。
  - **忠实 1.12.2 书壳**：当前使用 Patchouli 自带五种书纹理，视觉占位已由用户明确接受；功能验收不以复刻旧双页像素布局为前提。
  - **Aklo 字体正文**：`INFORMATION_KNOWLEDGE_PAGE_4` 使用稳定的 `necronomicon.text.knowledge.aklo` 正文；screen 仅对 `aklo-content` owner 的正文应用 `Style.withFont(abyssalcraft:aklo)`，标题与内容状态仍使用默认字体。
  - **其余语言翻译**：新增目录键已有 en_us/zh_cn；其他语言回退 en_us。

## 3. 设计 / 架构

- 关键类：
  - `PatchouliNecronomicon`：五本书的稳定 `ResourceLocation`、按 Item/书阶选书和 `PatchouliAPI.openBookGUI` 入口。
  - `PatchouliBookData`：唯一书资源生成 owner。每阶生成 `book.json`、8 内容分类、research 父分类和 5 子分类、可用条目、42 quest、action/manifest 模板及 advancement 双 schema。
  - `PatchouliActionComponent`：解析模板变量；页面显示时发送 `NecronomiconPageActionMessage`，按钮调用现有 Spellbook menu。
  - `PatchouliManifestComponent`：直接消费 62 ritual、14 spell、3 PoP 与 42 research 权威目录，渲染 PE、书阶、维度、供品/试剂、目标、活祭、增幅和研究条件，避免 JSON 复制漂移。
  - `LegacyNecronomiconPageCatalog` / `LegacyNecronomiconPageManifest` / `NecronomiconPageManifest`：冻结 322 个旧页并追加 79 个现代目录条目，总计 401。

## 4. 子系统内契约

- 书籍展示只读 Patchouli 数据；研究真值仍由服务端 `NecroData` 与 `KnowledgeGate` 所有。
- 42 项 research 各映射为 `abyssalcraft:research/<id>` goal advancement。`ResearchAdvancementCompat` 在登录和知识变化时双向合并 advancement 与 `NecroData.completedResearches`，兼容旧存档和新进度。
- `hasUnlockedAllKnowledge` 通过命令与登录同步授予/撤销 Patchouli progression；永久锁定的 Aklo 特殊页使用不会授予的 hidden advancement，不能被全知识绕过。
- 页面研读不是客户端直接写知识：自定义组件仅发页面 ID，服务器验证 `NecronomiconPageManifest.findActionable` 后执行既有动作。
- ACTIVE crafting 页声明真实 result ID，Patchouli `extra_recipe_mappings` 指向该 entry 的 crafting 页索引；历史名称错位的装饰雕像按真实 recipe 结果映射。
- 旧版把多个页面放在同一章节标题下；Patchouli 将每页扁平化为独立 entry 后，必须使用 `gui.abyssalcraft.necronomicon.entry.<legacy_id>.title` 专用键。标题由正文主题人工审阅，使用“概览、起源、用途、合成、运作、生态”等语义区分，不向玩家暴露内部 ID。
- 18 个无产物仪式使用 `gui.abyssalcraft.necronomicon.ritual.<id>.title`，避免显示 “Portal”“Weather”“Dragon Boss”等泛称；3 个力量之地使用独立短标题键，不复用描述句。

## 5. 跨版本 / 加载器要点

- Patchouli 版本固定为 Forge `1.20.1-85-FORGE`、NeoForge `1.21.1-93-NEOFORGE`，元数据中均为 required dependency。
- `PatchouliManifestComponent.onVariablesAvailable` 的 1.20 单参和 1.21 `HolderLookup.Provider` 双参签名由 Stonecutter 隔离；其余组件逻辑共享。
- advancement 数据仍双 schema：1.20.1 `advancements/` + `icon.item`，1.21.1 `advancement/` + `icon.id`。
- 客户端类不从专服路径直接加载；书 Item 只通过 `SideExecutor` 延迟调用 Patchouli 打开入口。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- Patchouli 模板字段不会自动替换到自定义组件普通 String 字段；必须在 `onVariablesAvailable` 中对 `kind/id/page/action` 调用 lookup。
- 标题键不同不代表玩家看到的文本不同；必须解析每种语言的最终显示值再做逐书唯一性检查。`scripts/audit_necronomicon_titles.js` 覆盖 5 本书 x 8 种语言，缺键或同书同语言重名即失败。
- 旧页面 research 引用必须从 manifest 原样迁移；只迁正文会导致旧存档研究门禁失效。
- `BlockState[][][]` 的 PoP render data 使用 `null` 表示空气，自定义组件遍历时必须跳过空位。
- 标签型 ritual ingredient 的代表物品必须从 item tag holder 中解析，不能只读显式 item ID。
- 祭坛形成只校验维度、材料、书阶和九个指定点位；不得要求九个方块位于同一 chunk，也不得检查点位周围是否为空。真实 ServerLevel fixture 必须覆盖跨 chunk 和邻近实体方块两种场景，并确认只替换九个指定点位。
- 不用客户端自动操作替代真人验收。自动阶段只运行编译、datagen、JSON/资源契约和静态检查；所有翻页、点击、锁定状态与书内跳转在功能全部完成后一次性人工测试。

## 7. 验证 / DoD

- 自动 DoD：双端 `compileJava --rerun-tasks`；Forge `runData` 产出 Patchouli PASS marker；所有生成 JSON 可解析；双端 `processResources`；编辑器 diagnostics 和 `git diff --check` 无本次新增错误。
- Provider 契约：五本书严格累积；401 manifest 完整；42 research advancement/quest 完整；动作页 ID、研究门禁、manifest 类型与 recipe 快速查找索引均自检。
- 标题契约：Provider 先检查逐书标题键唯一；正式标题审计再检查 40 个语言/书籍组合的解析值唯一。当前共 14,056 个 entry view，missing=0、duplicateValues=0。
- 人工 DoD：按最终统一清单在 Forge/Neo 各测试五本书打开、累积目录、研究锁定/解锁/旧存档回填、研读动作、法术书按钮、recipe 快速查找，以及 ritual/spell/PoP 结构化内容。

## 修订日志

- 当前：人工优化 322 个旧页、18 个无产物仪式与 3 个力量之地标题；最高阶书原有 26 组重复标题（影响 319 个条目）清零，并加入八语言显示值审计。
- 当前：移除旧 `NecronomiconScreen`/`NecronomiconEntry`/`ACNecronomicon` 架构说明，记录 Patchouli 五本累积书、401 manifest、42 advancement research、自定义 action/manifest 组件和 recipe 快速查找。
- 2026-07-29：修复 IMAGE 页 1024 纹理被错误裁为左上 256 区域、信息目录标题重复与首次/重复开书卡顿；恢复整页透明覆盖层坐标，加入本地化摘要、页码标题、PNG 头探测和五阶树缓存。
- 2026-07-27：迁移 75 张 1.12.2 Necronomicon PNG，恢复 89 个 IMAGE 页的完整纹理/UV renderer 与资源解码 selftest，移除 `necronomicon-image-renderer=89` BLOCKED。
- 2026-07-27：恢复 `INFORMATION_KNOWLEDGE_PAGE_4` 的稳定 Aklo 正文与 `abyssalcraft:aklo` renderer，加入资源/font/glyph/text 自动契约并移除 `aklo-content=1` BLOCKED。
- 2026-07-25：RR-KNOWLEDGE（CR-70）接入42项五分类研究目录、状态/hint与协议v2同步；完整旧页面/actions和真人目视继续待办。
- 2026-07-22：PH-5 建框架——`client/necronomicon/**`（递归 `NecronomiconEntry` + `NecronomiconScreen` 导航 + PS-8 只读门控 + pilot）；两节点编译 + runClient 抵标题屏。读 PS-8/PS-2 necrodata 门控 entry。20+ 具体章节/页 + 书贴图/布局 + 书物品 + lang 延后（依赖未移植 research/物品/贴图）；翻页/渲染观感人工。见平行表 PH-5。
