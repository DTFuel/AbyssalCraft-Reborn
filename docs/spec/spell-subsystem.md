# 法术 (Necronomicon Spell) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-B
- 关联平行任务：PS-7 / R4 RR-RITUAL-SPELL-PORTAL；上游 RR-ENERGY、RR-KNOWLEDGE；网络入口 `MobSpellMessage`/`OpenSpellbookMessage`
- 状态：**14 个旧版法术、六卷轴、7 槽 Spellbook、即时/蓄力施法与必要网络/资源已实现并通过 Forge/Neo 双端自动 Gate**；真人客户端施法/视觉矩阵仍归 R4-LIVE-GATE
- 负责：GitHub Copilot
- 最后更新：2026-07-26

## 1. 概述 / 目标

AbyssalCraft 的 Necronomicon 法术系统。现代实现以 1.12.2 的 14 个 spell 类和注册顺序为权威来源，由 `SpellManifestCatalog` 驱动运行时注册、施法、Spellbook 铭刻与永久自测。**旧版法术载体是铭刻卷轴**；Gatekeeper Staff 是传送工具，Staff of Rending 属独立 rending 系统，不存在“法杖选择 14 法术”的生产契约。

## 2. 范围

- 含：
  - `SpellManifestCatalog` 的 14 项顺序、规范 ID/alias、book tier、PE、ScrollType、target type、charging、颜色、5 槽试剂、parent/research/glyph 合同。
  - `SpellIngredient` 延迟 item/tag/alternative/count/strict 匹配；`SpellRegistry.find` 校验 book tier、卷轴质量、父铭文与无序非空试剂。
  - `SpellBehaviors` 的 14/14 服务端效果：Entropy、Life Drain、Mining、Grasp、Invisibility、Detachment、Steal Vigor、Siren's Song、Undeath to Dust、Ooze Removal、Teleport Hostiles、Floating、Teleport Home、Compass。
  - `ScrollItem` 的 `Spell` 自定义数据、instant/50 tick charging、成功后消耗、失败不消耗与旧版四级铭文 overlay；两种 UNIQUE 卷轴保持各自图标。
  - `SpellbookMenu`：手持 Necronomicon 打开，slot0 卷轴、slot1-5 试剂、slot6 只读输出；取出前服务端重算并原子消费，关闭返还输入，源书手/热栏槽锁定。
  - `MobSpellMessage`：客户端只提供实体 ID 提示；服务端从当前正在使用的卷轴重新解析 spell/quality，校验 50 tick、世界、距离、PvP、PE 与目标。包内 spell ID/quality 不参与决策。
- 不含：完整 Necronomicon spell compendium 页面、Aklo glyph 字体与真人客户端逐法术视觉验收；分别留 T6.2b/T6.6c/R4-LIVE-GATE。

## 3. 设计 / 架构

- 关键类：
  - `ScrollType`（枚举 NONE(-1)/BASIC(0)/LESSER(1)/MODERATE(2)/GREATER(3)/UNIQUE(4)，带 `quality()` + `byQuality`）；`IScroll`（scroll 物品 marker，`getScrollType(ItemStack)`）。
  - `SpellManifest` / `ManifestSpell`：不可变声明与运行时适配器，具体效果委托 `SpellBehaviorRegistry`。
  - `SpellUtils.castManifest`：仅服务端执行；解析 entity/block/self target，ENTITY_OR_SELF 对无效目标回退自身；从 source、双手、背包多个能量容器事务扣款，异常全额回滚，零成本 spell 不要求能量容器。
  - `ACDamageTypes.SPELL`：记录施法玩家并加入绕甲/抗性/附魔/效果四个 damage tag，恢复旧 `causePlayerDamage(...).setDamageBypassesArmor().setDamageIsAbsolute()` 语义。
  - Mining 按旧算法逐深度层处理核心区域与外围环，硬度总量是处理预算，不是额外 PE；solid lava 用现代 `magma_block` 等价。
  - Undeath to Dust 显式排除 AC Boss/Elite、Ender Dragon 与 Wither；Life Drain/Grasp/Teleport Hostiles 保留施法者伤害归属。

## 4. 子系统内契约

- 对外 API：`Spell` 供具体法术 `extends`（实现 `canCastSpell`/`castSpellServer`/`castSpellClient`，或 `EntityTargetSpell` 的 `castSpellOnTarget`）；`SpellRegistry.instance().registerSpell(...)` 供内容注册；`ScrollType`/`IScroll` 供卷轴物品与门控。
- **PE**：可跨多个 `IEnergyContainerItem` 聚合扣款；实际 debit 列表用于失败回滚。Entropy 回充首个可接收 PE 的权威容器。
- **Knowledge**：Spellbook 在输出预览和提交时都通过 `ResearchRegistry`/`KnowledgeGate` 服务端重验。
- **网络**：`OpenSpellbookMessage` 无载荷，服务端自行寻找主/副手 Necronomicon；`MobSpellMessage` 不信任 spell ID/quality；`StaffModeMessage` 只切换手持 Gatekeeper Staff 的旧 0/1 兼容字段。
- **资源**：旧版没有 14 张独立 glyph；四种普通卷轴使用统一 `spell_overlay` 的 `abyssalcraft:inscribed` predicate，UNIQUE 卷轴不套 overlay。

## 5. 跨版本 / 加载器要点

- `platform/ItemDataCompat`：1.20 NBT 与 1.21 `CUSTOM_DATA` component。
- `platform/PlayerRespawnCompat` / `TeleportCompat`：Teleport Home 的跨维重生点传送。
- `platform/TamableCompat`：马匹驯服差异。
- undead 判定在 `SpellBehaviors` 通过 Stonecutter 分支：1.20 `MobType.UNDEAD`，1.21 `EntityTypeTags.UNDEAD`。
- `ScrollItem.getUseDuration` 保留 1.20/1.21 参数签名分支；`ItemProperties.register` 两端签名一致，但必须延后到 `FMLClientSetupEvent`，不可在模组构造期读取 registry supplier。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- 旧版卷轴蓄力时长代码是 50 tick，旧语言文本写“3 秒”；实现以源码 50 tick 为权威。
- Spellbook 输入是 6 槽而不是独立 Spellbook item；它由手持 Necronomicon 页面发送无载荷打开请求。
- datagen 不绑定 `minecraft:beds` tag 内容，永久测试对 Teleport Home 验证精确 tag ID，其余 13 项做实际示例配方解析。
- 多容器扣款必须记录每个 debit，不能只算总量，否则 effect 抛异常时无法精确回滚。
- `ENTITY_OR_SELF` 不能仅“射线为空时”回退；射线命中已有 invisibility 等不适用目标时也要回退自身。

## 7. 验证 / DoD

- Forge/Neo `compileJava --rerun-tasks`：BUILD SUCCESSFUL。
- Forge/Neo `runData`：`RR_SPELL_MANIFEST_SELF_TEST_OK spells=14 entity=7 entityOrSelf=1 block=2 self=4 charging=11 handlers=14 spellbook=14`。
- 资源 Gate：`RR_RITUAL_SPELL_RESOURCES_OK itemModels=29 blockSets=3 spells=14 damageTags=4`，覆盖卷轴铭刻模型、统一 overlay、语言和绝对伤害 tags。
- 未机核项：真人客户端 14 法术逐项效果/粒子、MobSpell 实网延迟/丢包回退、Spellbook 交互目视；归 R4-LIVE-GATE。

## 修订日志

- 2026-07-26：R4 完成 14 项 manifest/behavior、六卷轴、Spellbook、MobSpell 服务端重验、铭文 overlay、绝对伤害与双端永久 Gate。
- 2026-07-22：PS-7 初始框架与 Life Drain pilot。
