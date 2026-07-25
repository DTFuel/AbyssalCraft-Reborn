# 仪式 (Necronomicon Ritual) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-B
- 关联平行任务：PS-6（本框架）；上游 PS-5（能量，altar 耗能读 `IEnergyContainer`）；解耦下游 PS-8（知识，读 `researchId` 门控）
- 状态：仪式框架交付并验证（两节点编译 + selfTest）；altar/pedestal 块 + 13 具体仪式 + 产物 = 内容，待其块/物品/实体/Necronomicon 依赖落地
- 负责：PS-6
- 最后更新：2026-07-22

## 1. 概述 / 目标

AbyssalCraft 的 Necronomicon 仪式系统。1.12.2：玩家在祭坛（altar）四周的基座（pedestal）上摆放材料（offerings），手持对应等级的 Necronomicon，在正确维度、满足势能（PE）与可选活祭（sacrifice）后触发仪式，产出物品 / 召唤实体 / 开启维度门。本任务交付**框架**（`Ritual` 抽象基 + `RitualRegistry` 注册/查找 + `InfusionRitual` pilot）；祭坛/基座方块、13 个具体仪式、产物 = 内容（延后，见 §2）。

## 2. 范围

- 含：`system/ritual/{Ritual,RitualRegistry,InfusionRitual}`——仪式抽象基（offerings/energy/sacrifice/book-tier/dimension 门控 + order-free `matches` + 抽象 `complete`）+ 注册表（register/`find` by offerings+book+dim）+ infusion pilot（产 `ItemStack`）。
- 不含（延后内容，依赖未移植）：
  - **祭坛 / 基座方块**（altar / pedestal）+ 其 BE：BE 扫描四周基座凑 offerings → 查 `RitualRegistry.find` → 校验 PE（PS-5 `IEnergyContainer`）+ 活祭 → 调 `ritual.complete`。落地时 BE `extends` PC-1 `ACBlockEntity`、经 `ModRegistrar` 注册。
  - **13 个具体仪式**（infusion / portal / summon / creation / transformation 各族）：依赖未移植的产物物品 / 实体 / 目标维度。
  - **产物**：transmutation gem / oblivion catalyst / gateway key 等物品、召唤 Asorah 龙等实体、开启 Dreadlands/Omothol 等维度门。
  - **Necronomicon 书**（book tier 来源，未移植）：现以 `find(..., bookTier, ...)` 的 `int` 形参占位，落地书后由手持书提供 tier。
  - **RitualUtil 基座扫描**（pedestal-scan / 消耗 / 视觉反馈）：反馈用 PS-1 `RitualMessage` / `RitualStartMessage`（handler 现 stub）。

## 3. 设计 / 架构

- 关键类：
  - `Ritual`（抽象）：字段 `name` / `bookType` / `dimension`（-1 = 任意）/ `requiredEnergy`（对 PS-5）/ `requiresSacrifice` / `offerings:List<ItemStack>`（≤8，一基座一件）/ 可选 `researchId:ResourceLocation`。抽象 `complete(Level,BlockPos,Player)` 决定产物；`matches(List<ItemStack> provided)` 做 **order-free 物品匹配**（`ItemStack.isSameItem` + `count >=`，逐项消去副本，忠实 1.12.2「不计顺序」语义）；`setResearch(id)` builder 供 PS-8 解耦门控。
  - `RitualRegistry`（单例 `instance()`）：`register` / `getRituals`（unmodifiable）/ `find(provided, bookTier, dimension)` —— 遍历返回首个满足 `bookTier >= bookType` && (`dimension == -1` || 相等) && `matches` 的仪式，否则 `null`。
  - `InfusionRitual extends Ritual`（pilot，最常见类型）：存 `result:ItemStack`，`complete` 在祭坛上方（+0.5, +1.2, +0.5）`addFreshEntity` 一个 `ItemEntity`（fork-free item spawn，同 PD-4 死亡替身）；服务端才产出（`isClientSide` 早退）。

## 4. 子系统内契约

- 对外 API：`Ritual` 供具体仪式 `extends`（实现 `complete`）；`RitualRegistry.instance().register(...)` 供内容注册仪式；altar BE 调 `find` + `complete`。
- **PS-5 能量**：`requiredEnergy()` 由 altar BE 校验其 `IEnergyContainer.getContainedEnergy()`（PS-6 不直接持能量，只声明需求）。
- **PS-8 知识（解耦）**：1.12.2 `IResearchable` 门控 → 简化为可选 `researchId()`；PS-8 读之判定玩家是否解锁该仪式。PS-6 **不依赖并行的 PS-8**（`researchId` 为 null 即无门控）。
- 反馈视觉 → PS-1 `net.client.RitualMessage` / `RitualStartMessage`（handler 现 stub，由 altar BE 落地时接线）。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：**无**（`Ritual` / `RitualRegistry` / `InfusionRitual` 全 fork-free）。
- `//?` 分叉点：**零**。仅用 vanilla `Level` / `BlockEntity`（未来）/ `BlockPos` / `ItemStack` / `ItemEntity` / `Player` / `ResourceLocation`。item spawn 用 `level.addFreshEntity(new ItemEntity(...))`（两端同签名，同 PD-4）。
- 落地祭坛方块时：BE fork（save/load）走 PC-1 `BlockEntityCompat`；块经 `ModRegistrar` 注册；均已有先例，本层无新增 fork。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容**（同 PP-1/PC-1/PD-1/PG-0/PS-5）：1.12.2 仪式深耦合祭坛/基座方块 + 产物物品/实体 + 维度门 + Necronomicon 书（全未移植）→ 先交付可 selfTest 的抽象基 + 注册表 + 一个 pilot，13 具体仪式随依赖落地时 `extends`/`register`。
- **`matches` order-free**：忠实 1.12.2「基座材料不计摆放顺序」——复制 `provided`，对每个 needed 找首个 `isSameItem` 且 `count>=` 的项并消去；size 不等直接 false。selfTest 覆盖：精确命中 / 数量不足（count 少）/ 缺项（少一件）三种反例。
- **`find` 门控顺序**：先 `bookTier` 再 `dimension`（-1 通配）再 `matches`；返回**首个**匹配（注册顺序敏感 → 具体仪式落地时注意更特化的先注册）。selfTest 覆盖 order-free find（打乱 provided 顺序仍命中）。
- **`IResearchable` 解耦**：直接依赖 PS-8 会造成 S-B 内 PS-6↔PS-8 并行耦合 → 降为可选 `researchId`（数据而非接口），PS-8 单向读，符合平行表「同 Stage 任务零冲突」。
- **selfTest 触发**：临时 `RitualRegistry.selfTest()` 挂主类 `init`（`EnchantmentCompat.bootstrap` 之后），`runData` 触发 mod init 打印 PASS（同 PS-5/PC-4 先例）；核完**还原** selfTest 方法 + init 调用。
- **并发干扰（非本码）**：还原后整项目重编一次撞到并发 PG-5 结构注册中途改坏 `registry/ModWorldgen.java:82`（`(StructurePieceType) ACStructurePiece::new` 构造器引用推断失败）→ 等并发 agent 收尾后重跑即 BUILD SUCCESSFUL（本 PS-6 文件从未报错，仅外部文件瞬态）。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL。
- **`RitualRegistry` selfTest（临时，已还原）**：注册 `InfusionRitual`（offerings 若干）→ `matches` 精确命中 / 数量不足 / 缺项三反例 → `find` order-free 命中 + book/dim 门控 —— **forge/neo 均 `PASS`**（`runData` 触发 init）。
- 未机核项（如实标注）：**祭坛 BE 全链**（扫基座 → 凑 offerings → 校 PE[PS-5] → 活祭 → `complete` 产物）需 live 祭坛/基座方块 + 产物物品/实体 + Necronomicon（内容延后）；`InfusionRitual.complete` 的实际掉落仅 compile 验证，未运行期目视。

## 修订日志

- 2026-07-22：PS-6 建框架——`system/ritual/**`（`Ritual` 基 + `RitualRegistry` + `InfusionRitual` pilot）；两节点编译 + selfTest 双端 PASS。`IResearchable` 门控解耦为 `researchId`（PS-8 读）。祭坛/基座块 + 13 具体仪式 + 产物延后（依赖未移植块/物品/实体/Necronomicon）。见平行表 PS-6。
