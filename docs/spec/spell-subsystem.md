# 法术 (Necronomicon Spell) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-B
- 关联平行任务：PS-7（本框架）；上游 PS-5（能量，PE 消耗读能量物品 `IEnergyContainerItem`）；解耦下游 PS-8（知识，读 `researchId` 门控）；反馈 PS-1（`MobSpellMessage`/`OpenSpellbookMessage`）
- 状态：法术框架 + **`SpellUtils` 施放链 + pilot 法杖交付并验证（CR-64，两节点编译 + runData selfTest `found=life_drain ok=true` + forge runServer Done）**；余 14 具体法术 + scroll/spellbook 物品 + spellbook GUI = 内容（PS-7b），待其试剂/物品/GUI 依赖落地
- 负责：PS-7
- 最后更新：2026-07-23

## 1. 概述 / 目标

AbyssalCraft 的 Necronomicon 法术系统。1.12.2：玩家用试剂（reagents）在羊皮纸（parchment/scroll）上铭刻（inscribe）法术，手持对应等级 Necronomicon + 卷轴，消耗势能（PE，取自背包能量物品）施放；法术分即时（instant）与蓄力（charging），部分针对实体目标（EntityTargetSpell，客户端射线取目标→服务端处理）。本任务交付**框架**（`Spell` 抽象基 + `SpellRegistry` 注册/查找 + `ScrollType`/`IScroll` + `EntityTargetSpell` pilot 基 + `LifeDrainSpell` 具体 pilot）；`SpellUtils` 施放链、14 具体法术、卷轴/法杖/法术书 GUI = 内容（延后，见 §2）。

## 2. 范围

- 含：`system/spell/{ScrollType,IScroll,Spell,SpellRegistry,EntityTargetSpell,LifeDrainSpell}`——法术抽象基（reagents order-free `matches` + bookType/scrollType 门控 + 抽象 `canCastSpell`/`castSpellServer`/`castSpellClient` + `castSpell` side 派发）+ 注册表（register 校验/去重 + `find`）+ 卷轴品级枚举 + scroll marker + 目标法术 pilot 基 + 一个具体 pilot（生命汲取）。
- 不含（延后内容，依赖未移植）：
  - **`SpellUtils` 施放链**：`castInstantSpell`/`castChargingSpell`（持物 → 取法术 → `hasEnoughPE`/`drainPE` → `castSpell`）。PE 取自背包 **能量物品 `IEnergyContainerItem`**（PS-5 的**物品变体**，1.12.2 `api.energy.IEnergyContainerItem`，尚未移植；PS-5 只交付了 BE 侧 `IEnergyContainer`）。
  - **持物 NBT 读写**：`inscribeSpell`（写 `Spell` 标签到 parchment）+ `getSpell(ItemStack)`（读之）+ areSpellsEqual 的 parchment parent-门控——依赖 **卷轴物品**（未移植）且 **ItemStack NBT 是 1.20↔1.21 fork**（1.20.1 `getOrCreateTag`/`CompoundTag` vs 1.21.1 `DataComponents.CUSTOM_DATA`/`CustomData`）→ 落地卷轴物品时封入 compat。
  - **14 具体法术**：Entropy / LifeDrain / Mining / GraspOfCthulhu / Invisibility / Detachment / StealVigor / SirensSong / UndeathToDust / OozeRemoval / TeleportHostiles / Floating / TeleportHome / Compass——依赖未移植试剂物品 / 内部方法 handler / 目标效果。
  - **client 射线取目标**：`SpellUtils.rayTraceTarget`（1.12.2 走 `AbyssalCraftAPI.getInternalMethodHandler()`）+ `processEntitySpell`（发 **PS-1 `MobSpellMessage`** 到服务端，handler 现 stub）。
  - **卷轴 / 法杖 / 法术书 GUI**：scroll 物品（`IScroll` 实现）+ staff + Necronomicon spellbook 界面（铭刻/选法术，PS-1 `OpenSpellbookMessage`/`StaffModeMessage`/`StaffOfRendingMessage` handler 现 stub）+ glyph 贴图。

## 3. 设计 / 架构

- 关键类：
  - `ScrollType`（枚举 NONE(-1)/BASIC(0)/LESSER(1)/MODERATE(2)/GREATER(3)/UNIQUE(4)，带 `quality()` + `byQuality`）；`IScroll`（scroll 物品 marker，`getScrollType(ItemStack)`）。
  - `Spell`（抽象）：字段 `id`/`bookType`(0-4)/`requiredEnergy`(对 PS-5)/`reagents:List<ItemStack>`/`color`/`scrollType`/`parent:Spell`/`nbtSensitive`/`requiresCharging`/`canBeCastByOthers`/`glyph`/ 可选 `researchId`。builder setters。`matches(provided)` order-free item 匹配（`ItemStack.isSameItem` + `count>=`，忠实「试剂不计顺序」）。抽象 `canCastSpell` + `castSpellServer` + `castSpellClient`；`castSpell` 按 `level.isClientSide` 派发；`castSpellOther` 非玩家施法 hook。
  - `SpellRegistry`（单例 `instance()`）：`registerSpell`（bookType 0-4 校验 + id 去重，`LogUtils` 日志，忠实 1.12.2）/ `getSpells`（unmodifiable）/ `getSpell(id)` / `find(bookTier, held:ScrollType, reagents)` —— 遍历首个满足 `bookType <= bookTier` && `held.quality() >= scrollType.quality()` && `matches(reagents)`（= 忠实 areSpellsEqual 减去 parchment-NBT parent-门控，后者待卷轴物品）。
  - `EntityTargetSpell extends Spell`（目标法术 pilot 基）：`range` + 抽象 `canCastSpellOnTarget(LivingEntity,ScrollType)` + `castSpellOnTarget(Level,BlockPos,Player,ScrollType,LivingEntity)`（服务端效果入口）。`canCastSpell`/`castSpellClient`/`castSpellServer` 为**延后 stub**（client 射线 + PS-1 网络 round-trip 待落地），`castSpellOnTarget` 已交付供具体法术编译 + 落地时由网络 handler 驱动。
  - `LifeDrainSpell extends EntityTargetSpell`（具体 pilot，忠实 1.12.2 `LIFE_DRAIN`）：`castSpellOnTarget` = `target.hurt(level.damageSources().magic(), amount)` + `player.heal(amount)`（fork-free）。

## 4. 子系统内契约

- 对外 API：`Spell` 供具体法术 `extends`（实现 `canCastSpell`/`castSpellServer`/`castSpellClient`，或 `EntityTargetSpell` 的 `castSpellOnTarget`）；`SpellRegistry.instance().registerSpell(...)` 供内容注册；`ScrollType`/`IScroll` 供卷轴物品与门控。
- **PS-5 能量**：`requiredEnergy()` 由 `SpellUtils`（延后）校验/扣减背包 `IEnergyContainerItem`（PS-5 物品变体，未移植）。PS-7 只声明需求，不直接持能量。
- **PS-8 知识（解耦）**：1.12.2 `Spell implements IResearchable<Spell,Spell>` → 简化为可选 `researchId()`；PS-8 读之判定是否解锁。PS-7 **不依赖并行的 PS-8**（`researchId` 为 null 即无门控）。
- 反馈 / 网络 → PS-1 `net.server.MobSpellMessage`（client 取目标 → 服务端施法）/ `OpenSpellbookMessage` / `StaffModeMessage` / `StaffOfRendingMessage`（handler 现 stub，卷轴/法杖/GUI 落地时接线）。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：**无**（6 类全 fork-free）。
- `//?` 分叉点：**零**。仅用 vanilla `Level`/`BlockPos`/`Player`/`LivingEntity`/`ItemStack`/`ResourceLocation` + `com.mojang.logging.LogUtils`；效果用 `target.hurt(damageSources().magic(), f)` + `player.heal(f)`（1.20.1 & 1.21.1 同签名，**编译双端实证**——`hurtServer` 拆分是 1.21.2+，1.21.1 仍 `hurt`）；reagent 比对用 `ItemStack.isSameItem`（两端 static 同签名）。
- **延后内容的已知 fork**（落地时封 compat，非本框架）：
  - **ItemStack NBT**（inscribe/getSpell(ItemStack)）：1.20.1 `getOrCreateTag`/`getTag`/`setTag(CompoundTag)` vs 1.21.1 `DataComponents.CUSTOM_DATA`/`CustomData`（tag API 移除）。
  - **NBT-sensitive reagent 比对**：1.20.1 `isSameItemSameTags` vs 1.21.1 `isSameItemSameComponents`（本框架 `matches` 仅 item 比对，`nbtSensitive` 仅存标志供内容用，规避此 fork）。
  - `@SideOnly`/`@OnlyIn`（`getLocalizedName`/`getDescription`）：本框架不含 side-annotated 方法，仅 `translationKey()` 返回字符串键（lang 延后）。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容**（同 PP-1/PC-1/PD-1/PG-0/PS-5/PS-6）：1.12.2 法术深耦合卷轴/法杖物品 + 试剂物品 + 能量物品 + 内部射线 handler + Necronomicon GUI（全未移植）→ 先交付可 selfTest 的抽象基 + 注册表 + 一个 pilot，14 法术随依赖落地时 `extends`/`register`。
- **`find` 去 parchment 化**：忠实 areSpellsEqual 有 bookType/reagent/scrollType/parchment-parent 四关；parchment parent-门控读 ItemStack NBT + 依赖卷轴物品（未移植 + NBT fork）→ 本框架 `find` 保留前三关（全 fork-free、可测），parent-门控留给卷轴落地。
- **PE 是物品变体不是 BE**：法术扣 PE 自**背包能量物品** `IEnergyContainerItem`（`api.energy` 的 item 侧），**PS-5 只交付了 BE 侧 `IEnergyContainer`**；`IEnergyContainerItem` + 具体能量物品均未移植 → `SpellUtils` 的 `hasEnoughPE`/`drainPE` 整体延后（非 PS-7 遗漏，是依赖未到）。
- **`matches` order-free**：同 PS-6 offering match（复制 provided，逐 needed 找首个 `isSameItem` 且 `count>=` 消去；size 不等 false）。selfTest 覆盖精确命中 / 错试剂 / 数量。
- **`IResearchable` 解耦**：直接依赖 PS-8 会造成 S-B 内 PS-7↔PS-8 并行耦合 → 降为可选 `researchId`（数据而非接口），符合平行表「同 Stage 任务零冲突」（同 PS-6）。
- **selfTest 触发**：临时 `SpellRegistry.selfTest()` 挂主类 `init`（`EnchantmentCompat.bootstrap` 之后），`runData` 触发 mod init 打印 PASS（同 PS-5/PS-6/PC-4 先例）；用 `new SpellRegistry()`（私有 ctor 类内可访问）避免污染单例；核完**还原** selfTest 方法 + init 调用。
- **`SpellUtils` 施放链交付（CR-64）· server-raytrace 替 client 往返**：1.12.2 `EntityTargetSpell` 走 client 射线取目标 → `MobSpellMessage` 发服务端施法（PS-1 handler 现 stub）。CR-64 用 **server 侧 raytrace**（`ProjectileUtil.getEntityHitResult` 沿 `getEyePosition`+`getViewVector`×range，fork-free 两端同）在法杖 `use()` 直接取目标 + `castSpellOnTarget`，功能等价且规避 client 射线 + 网络往返的 stub。**非玩家施法者 / 远程施法仍需 `MobSpellMessage`**（延后 PS-7b）。PE 扣减用 CR-58 `IEnergyContainerItem`（本 spec §6「PE 是物品变体」记的依赖已由 CR-58 落地）；`SpellUtils` 与 `EntityTargetSpell.canCastSpellOnTarget`（protected）同包 `system/spell/` 故可访。pilot 法杖 `content/item/staff/{StaffItem,StaffSpells,StaffItems}` = 能量物品（同 `NecronomiconItem` extends `TooltipCompat` implements `IEnergyTransporterItem`）右键即施 pilot `LifeDrainSpell`。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL（含 `LifeDrainSpell` 的 `hurt`/`heal`/`damageSources().magic()` 双端同签名实证）。
- **`SpellRegistry` selfTest（临时，已还原）**：register（有效 + bookType 5 无效被拒 + id 重复被拒 → size==1）+ `getSpell(id)` + `find`（reagent match / 错 reagent → null / bookTier 门控[给 -1 → null] / scrollType 门控[held NONE → null]）—— **forge/neo 均 `PASS`**（`runData` 触发 init）。
- **`SpellUtils` + pilot 法杖（CR-64）**：两节点 `runData` selfTest（已还原）`found=life_drain req=50.0 range=15.0 ok=true`（`StaffSpells.bootstrap` 注册 pilot 入 `SpellRegistry.instance()` + `EntityTargetSpell` 参数核）+ **forge `runServer` `Done`（4.448s）**（`spell_staff` item + spell bootstrap 注册无碰撞，无 eager client-classload）。实际施放（充能法杖 → 瞄 mob 右键 → mob 掉血 + 玩家回血 + PE 扣）= 活客户端人工验；施放逻辑用已验证件（`IEnergyContainerItem` CR-58 + `LifeDrainSpell` server 效果 + vanilla `ProjectileUtil` 射线）。
- 未机核项（如实标注）：**实际施放全链**（持物 → 取法术 → PE 扣减[PS-5 能量物品] → client 射线取目标 → PS-1 MobSpell → `castSpellOnTarget` 效果）需 live 卷轴/能量物品 + 内部 handler + 网络会话（内容延后）；`LifeDrainSpell.castSpellOnTarget` 的 hurt/heal 仅 compile 验证，未运行期目视。

## 修订日志

- 2026-07-22：PS-7 建框架——`system/spell/**`（`Spell` 基 + `SpellRegistry` + `ScrollType`/`IScroll` + `EntityTargetSpell` + `LifeDrainSpell` pilot）；两节点编译 + selfTest 双端 PASS。`IResearchable` 门控解耦为 `researchId`（PS-8 读）。`SpellUtils` 施放链 + 14 法术 + scroll/staff/spellbook GUI 延后（依赖未移植试剂/物品/内部 handler；含 ItemStack NBT 1.20↔1.21 fork）。见平行表 PS-7。
