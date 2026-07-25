# 知识 / 死灵之书 (Necronomicon Knowledge) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-B
- 关联平行任务：PS-8（本框架）；**上游 PS-2**（necrodata 存储层，PS-8 直接读其 `NecroData`）；被 PS-6（仪式）/PS-7（法术）的 `researchId` 引用（解耦门控）
- 状态：RR-KNOWLEDGE 实现切片完成：42 research、42 conditions（33 内联+9 运行期）、11 offerings、type 5/6、核心触发、协议 v2 同步与分类研究目录已双端验证；完整旧页面/actions 与 artifact/page/whisper 生产内容留 T7.8c
- 负责：PS-8
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的 Necronomicon 知识系统。现代实现以 `KnowledgeContent` 冻结旧版 42 个具体 research、42 个条件常量与 11 个 offering，消费玩家 `NecroData`，由 `KnowledgeHooks` 写触发并通过协议 v2 同步；客户端研究目录按 biome/dimension/entity/misc/book 分类显示 locked/completed 状态与具体 hint。旧 42 research 的 `requiredLevel/pointsCost` 均为 0，不新增原版不存在的研究点经济。

## 2. 范围

- 含：
  - `KnowledgeContent` 权威目录（42 research / 42 conditions / 11 offerings）与永久 `KnowledgeSystemSelfTest`。
  - 12 类条件对象与 10 类编号处理器，其中 type 5/6 用稳定 `KnowledgePredicate` 解析现代实体族/群系；book 条件由带 `bookType` 上下文的 `KnowledgeGate` 评估。
  - 杀怪、换维、200t 群系采样、plague misc、开书 tier 触发；变化时增量+全量同步，登录/重生/换维按配置延迟完整同步。
  - `ACNecronomicon` 42 项研究目录、五分类、状态、条件 hint 与 8 语言键集合。
- 不含：
  - 完整旧 recipe/ritual/spell/PoP 页面正文、潜行 actions 与内容导航（T7.8c/T6.2b）。
  - artifact/whisper：旧版无生产调用，不虚构玩法；page 仅在真实现代 `ItemPage` 内容落地后接生产触发（T7.8c）。
  - 双端真人玩家的死亡复制、断线重连、五本书与客户端实时更新矩阵（T7.2c）。

## 3. 设计 / 架构

- 关键类：
  - `IResearchItem` / `ResearchItem`：研究条目（`id:ResourceLocation` / `name` / `type:KnowledgeType`[PS-2] / `requiredLevel`[-1 恒解 / -2 恒锁] / `pointsCost` / `unlockConditions:IUnlockCondition[]`）。
  - `ResearchRegistry`（单例）：`registerResearchItem`（id 去重）/ `getResearchItems` / `getResearchItemById` + 每 `KnowledgeType` 的 Crystallizer 供品表（`addOffering` / `isOffering` / `isOfferingOfType` / `getOfferingsForType`，`EnumMap<KnowledgeType,List<ItemStack>>`）。
  - `IUnlockCondition` / `UnlockCondition`：单条件（`getType():int`→processor / `getConditionObject():Object` / `knowledgeType` / `pointsCost` / `hint`）。子类 `EntityCondition`(1) / `DimensionCondition`(2) / `MiscCondition`(10) / `MandatoryMultiEntityCondition`(11，`String[]` 全满足)。
  - `IConditionProcessor`（函数式 `processUnlock(IUnlockCondition,NecroData,Player):boolean`）+ `ConditionProcessorRegistry`（单例 `int→processor`，**构造期**注册 10 个 fork-free 处理器：0 biome / 1 entity / 2 dimension / 3 multi-biome(any) / 4 multi-entity(any) / 7 artifact / 8 page / 9 whisper / 10 misc / 11 mandatory multi-entity(all)，均读 PS-2 `NecroData` 触发列表 contains）。
  - **`KnowledgeGate`**（判定层，读 PS-2 `NecroData`）：
    - `isUnlocked(NecroData,IUnlockCondition,Player)`：`type==-2`→false；`type==-1 || hasAllKnowledge && type!=11`→true；否则 `getProcessor(type).processUnlock(...)`。
    - `isUnlocked(NecroData,IResearchItem,Player)`：`requiredLevel==-2`→false；`-1 || hasAllKnowledge || 已完成`→true；否则全 `unlockConditions` 满足则 **`completeResearch` auto-complete** 并返回结果。

## 4. 子系统内契约

- **读 PS-2（核心跨任务契约）**：PS-8 直接消费 PS-2 的 `system/cap/necrodata/{NecroData,KnowledgeType,NecroDataCapability}`。PS-2 `NecroData` 提供 7 触发列表（`getBiome/Entity/Dimension/Artifact/Page/Whisper/MiscTriggers`，均 `List<String>`）+ `getCompletedResearches` + `completeResearch(String)` + `hasUnlockedAllKnowledge` + 知识点。**PS-2 只存、PS-8 判**（PS-2 `NecroData` javadoc 明言"消费（解锁条件处理、研究门控）是知识子系统的活（PS-8）"）。运行期取 `NecroData` 用 PS-2 `NecroDataCapability.get(player)`。
- 被 PS-6/PS-7 引用：仪式 / 法术的可选 `researchId` 指向本层的 `IResearchItem.getID()`；落地书 GUI 时用 `KnowledgeGate.isUnlocked` 门控其显示。
- 对外 API：`ResearchRegistry.instance().registerResearchItem(...)` / `addOffering(...)` 供内容注册；`KnowledgeGate.isUnlocked(...)` 供书 GUI / 仪式 / 法术门控；`ConditionProcessorRegistry.instance().registerProcessor(...)` 供内容注册谓词处理器（type 5/6）。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：**无**（12 类全 fork-free）。
- `//?` 分叉点：**零**。用 vanilla `ResourceLocation`/`ItemStack`/`Player`/`CompoundTag` + `com.mojang.logging.LogUtils` + PS-2 `NecroData`/`KnowledgeType`。
- **`ResourceLocation` 构造是 1.20↔1.21 fork**（`new ResourceLocation` vs `fromNamespaceAndPath`）→ **永久 PS-8 代码不构造 RL**（研究 id 由内容经既有 `platform/ACRef.of/id` 传入）；仅临时 selfTest 用 `ACRef.of(...)`（既有 platform 助手，只读引用，不改 platform → 无 CR）。
- `ItemStack` 供品比对用 `ItemStack.isSameItem`（两端 static 同签名；忠实 1.12.2 `APIUtils.areStacksEqual` 的 item 语义，NBT 敏感留内容）。
- 处理器注册从 1.12.2 `MiscHandler.init` **上移进 `ConditionProcessorRegistry` 构造期**（单例 class-load 即就绪）→ PS-8 **无需 mod-init 钩子**，与 PS-5/PS-6/PS-7 一致（无 main 改、无 CR）。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容 + 复用 PS-2**（同 PP-1/PC-1/PD-1/PG-0/PS-5/6/7）：知识深耦合 20+ 具体实体/群系/维度条件 + Necronomicon 书 GUI（全未移植）→ 先交付可 selfTest 的注册表 + 条件 + 处理器 + 判定层，具体条件 / 书 GUI / 触发钩子随依赖落地。**关键**：存储层 PS-2 已建（7 触发列表 + completedResearches + points），PS-8 只补"判定"这一层，零重复。
- **`hasAllKnowledge` 不越 type-11**：忠实原版——"全知"标志短路所有条件**除** mandatory multi-entity（type 11，"杀齐所有 boss"），否则该硬性成就会被作弊指令绕过。selfTest 专门覆盖（allKnow 下 type-1 true 但 type-11 缺项仍 false）。
- **研究 auto-complete**：`isUnlocked(research)` 判定全条件满足时**顺带** `completeResearch(id)` 写回 necrodata（忠实原版 side-effect）→ 之后走"已完成"快路。selfTest 覆盖（解锁后 `getCompletedResearches` 含该 id）。
- **type-5/6 使用稳定谓词键**：条件不序列化 Java `Predicate`；`KnowledgePredicate` 保存 DARKLANDS/CORALIUM/ANTI/DREAD/EVIL/DEMON/SHOGGOTH 等稳定语义，处理器从当前 registry 与实体族判断。永久 Gate 验证每个目录条件均有可达 processor 和可解析 ID。
- **selfTest 触发**：临时 `KnowledgeGate.selfTest()` 挂主类 `init`（`EnchantmentCompat.bootstrap` 之后），`runData` 触发 mod init 打印 PASS（同 PS-5/6/7/PC-4 先例）；用 `new NecroData(new CompoundTag())` 内存构造（无需 world / 活玩家，`Player` 传 null——fork-free 处理器不解引用 player）；核完**还原** selfTest 方法 + init 调用 + selfTest-only imports（`ACRef`/`KnowledgeType`/`CompoundTag`/`EntityCondition`/`MandatoryMultiEntityCondition`/`LogUtils`）。

## 7. 验证 / DoD

- 双端 `compileJava`、`runData`、production `build` 通过；永久 provider 输出 `RR_KNOWLEDGE_SELF_TEST_OK research=42 conditions=42 offerings=11`。
- 自测覆盖 mutation 去重、type-11/allKnowledge、auto-complete、实体 ID、SavedData cap/round-trip、配置 parser 与 AntiPlayer 名称反转。
- 8 语言均含 42 research 键与 19 个研究 UI 键；双端 production JAR 含知识核心且无 `rrk_`/`rr-knowledge` 临时残留。
- 未完成：双端真人触发→网络→客户端书目录端到端矩阵，以及 T7.8c 的旧页面/actions/真实 page 内容。

## 修订日志

- 2026-07-25：RR-KNOWLEDGE（CR-70）补齐42/42/11目录、type5/6、触发/同步、研究目录与语言；双端永久Gate、build/server/JAR通过。完整旧书消费与真人实网拆为T7.8c/T7.2c。
- 2026-07-22：PS-8 建框架——`system/knowledge/**`（研究注册表 + 研究条目 + 解锁条件 + 条件处理器注册表[10 fork-free 处理器] + `KnowledgeGate.isUnlocked` 条件/研究双版 auto-complete），**读 PS-2** `NecroData`。两节点编译 + selfTest 双端 PASS。Necronomicon 书 GUI + 20+ 具体条件 + type 5/6 谓词 + 触发事件钩子 + 同步 handler 接线延后（依赖未移植实体/群系/维度/书；PS-1 necrodata 消息 stub、PS-2 save/apply 就绪）。见平行表 PS-8。
