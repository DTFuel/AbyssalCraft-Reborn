# Places of Power (PoP) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-C
- 关联平行任务：PS-10（本框架）；上游 PS-5（能量，PoP 让 manipulator 无扰动抽 PE）；PS-9（免扰动对象）；解耦 PS-8（`researchId` 门控）
- 状态：Basic / Totem Pole / Archway具体内容、主BE与书本成型接线完成并双端验证
- 负责：GitHub Copilot
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的 Places of Power（PoP）系统。1.12.2：PoP 是多方块结构，让 energy manipulator（PS-5）**不触怒神祇**地采集势能（即免扰动，PS-9）；玩家用对应等级 Necronomicon 在候选块上右键成型；PoP 可给其内 manipulator/container 的统计（range/duration/power）加成（amplifier）。本任务交付**框架**（`IPlaceOfPower` 接口 + `StructureHandler` 注册/成型 + 多块 BE marker + pilot）；具体 PoP 多块、construct/render 逻辑 = 内容（延后，见 §2）。

## 2. 范围

- 含：框架接口/注册表、`multi_block`主块与持久BE、`monolith_pillar`范围放大块、三个具体PoP及潜行Necronomicon成型入口。
- Basic：三层3×3，4神像，RANGE +2；Totem Pole：巨石+3神像竖列，RANGE +3；Archway：6墙/4楼梯/6半砖+1神像的单面拱门，RANGE +1。
- 主BE每100t验证结构；成型写入成员`basePosition`，断裂/破坏解除成员；结构identifier持久化。

## 3. 设计 / 架构

- 关键类：
  - `IPlaceOfPower`：`getIdentifier` / `getBookType` / 可选 `getResearchId():ResourceLocation`（解耦 PS-8）/ `getAmplifier(AmplifierType)`（PS-5）/ `canConstruct(Level,BlockPos,Player)` / `construct(Level,BlockPos)` / `validate` / `getRenderData():BlockState[][][]` / `getActivationPointForRender` / `getAmbientEffectCooldown` / `triggerAmbientEffect` + 默认 `getDescriptionKey`/`getRequiredBlockNamesKey`。
  - `IStructureComponent`（多块成员 BE marker）：`isInMultiblock`/`setInMultiblock` + `getBasePosition`/`setBasePosition`（PoP 主块位）。
  - `IStructureBase`（主块 BE marker）：`getMultiblock`/`setMultiblock`（`IPlaceOfPower`）+ `getAmplifier(AmplifierType)`（桥接到 PoP）。
  - `StructureHandler`（单例 `instance()`）：`registerStructure`（identifier 去重）/ `getStructures` / `getStructureByName` / `canFormStructure(level,pos,bookType,player)`（`bookType>=getBookType()` && `canConstruct`）/ `formStructure`（首个满足者 `construct`，server 端）。
  - `SimplePlaceOfPower`（pilot）：`identifier`/`bookType` + `formable` 布尔字段（`setFormable`）—— `canConstruct` 返回该布尔，其余 no-op / 空 `BlockState[0][0][0]`，让框架无 live world 可测。

## 4. 子系统内契约

- 对外 API：`IPlaceOfPower` 供具体 PoP 实现；`StructureHandler.instance().registerStructure(...)` 供内容注册；Necronomicon 右键调 `canFormStructure`/`formStructure`；`IStructureComponent`/`IStructureBase` 供 PS-5 能量 BE 实现。
- **PS-5 能量**：`getAmplifier(AmplifierType)` 用 PS-5 `AmplifierType`（RANGE/DURATION/POWER）；成型对象 = PS-5 manipulator/container BE。
- **PS-8 知识（解耦）**：`getResearchId()`（可选 `ResourceLocation`）替代 1.12.2 `IResearchItem` 门控；成型方读之判解锁（PS-8 `KnowledgeGate`）。PS-10 不依赖 PS-8。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：**无**（5 类全 fork-free）。
- `//?` 分叉点：**零**。用 vanilla `Level`/`BlockPos`/`Player`/`BlockState`/`ResourceLocation`。`BlockState[][][]` render data 两端同类型。
- 落地能量块 BE 时：BE `extends` PC-1 `ACBlockEntity`（save/load 走 `BlockEntityCompat`）+ `implements` `IStructureComponent`/`IStructureBase`；均已有先例，本层无新增 fork。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容**（同 PS-5..9 先例）：PoP 深耦合未移植能量块（collector/container/pedestal）+ deity statue + Necronomicon 书 → 先交付可 selfTest 的接口 + 注册表 + pilot，具体 PoP 随能量块落地 `implements`/`register`。
- **pilot 用布尔 canConstruct 避 world**：`SimplePlaceOfPower.canConstruct` 返回 `formable` 字段（非扫描 world）→ selfTest 可控 `canFormStructure` 的三态（bookType 过低 / 达标+formable / canConstruct false）无需 live world。
- **`IResearchItem` 解耦**：直接依赖 PS-8 会耦合 → 降为可选 `researchId`（数据而非接口），同 PS-6/7 先例。
- **selfTest 触发**：临时 `StructureHandler.selfTest()` 挂主类 `init`，`runData` 触发（同 PS-5..9 先例）；`new StructureHandler()` 避污染单例；核完**还原**。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL。
- **`StructureHandler` selfTest（临时，已还原）**：register（有效 + identifier 重复被拒 → size==1）+ `getStructureByName` + `canFormStructure` 三态（bookType 1<2 不成型 / bookType 3>=2 且 formable 成型 / canConstruct false 不成型）—— **forge/neo 均 `PASS`**（`runData` 触发 init）。
- 双端真实ServerLevel已分别完成Basic/Totem/Archway `canConstruct→construct→amplifier`；Basic拆成员后其余神像立即解除PoP成员。
- 双端停服重启后主BE恢复`basic` identifier，神像恢复`IsMultiblock/BasePosition`。

## 修订日志

- 2026-07-25：三个具体PoP、主块/BE、潜行书成型、周期validate和拆除解绑落地；双端真实Level与重启矩阵通过。

- 2026-07-22：PS-10 建框架——`system/energy/structure/**`（`IPlaceOfPower` + `StructureHandler` bookType 门控 + 多块 BE marker + pilot）；两节点编译 + selfTest 双端 PASS。`IResearchItem` 门控解耦为 `researchId`（PS-8 读）。具体 PoP 多块 + construct/renderData + BE 实现延后（依赖未移植能量块/statue/书）。见平行表 PS-10。
