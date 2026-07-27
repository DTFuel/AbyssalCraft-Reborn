# 仪式 (Necronomicon Ritual) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-B
- 关联平行任务：PS-6 / R4 RR-RITUAL-SPELL-PORTAL；上游 RR-ENERGY、RR-KNOWLEDGE；Portal 与复活快照为直接消费者
- 状态：**62 个旧版注册项、祭坛 ceremony 与 18 个专用行为已实现并通过 Forge/Neo 双端自动 Gate**；真人客户端仪式视觉/听觉矩阵仍归 R4-LIVE-GATE
- 负责：GitHub Copilot
- 最后更新：2026-07-26

## 1. 概述 / 目标

AbyssalCraft 的 Necronomicon 仪式系统。现代实现以旧 `AbyssalCrafting.addRitualRecipes()` 为权威来源，将 **62 个注册项**冻结进不可变 manifest，并由同一数据驱动注册、祭坛匹配、执行与永久自测。旧文档中的“13 个仪式”是早期误计；准确分类为 40 Infusion、3 Creation、1 Transformation、18 specialized registrations。

## 2. 范围

- 含：
  - `RitualManifestCatalog` 的 62 项顺序、规范/旧 ID、类型、book tier、维度、PE、活祭、center、8 槽供品、结果、数据复制、research 与 hidden 合同。
  - `RitualIngredient` 延迟 item/tag/alternative/strict/count 匹配；8 槽是展示布局，旧版执行语义仍为无序多重集。
  - `ManifestRitual` 通用 Infusion/Creation/Transformation 执行；`RitualBehaviorRegistry` 的 18 个专用处理器。
  - `RitualAltarBlockEntity` 持久 PREPARE/CHANT/WAIT_SACRIFICE/COMPLETE/FAIL 状态，原子供品消费、crafting remainder、每 20 tick 抽 PE、center 锁、活祭 UUID、重启恢复/安全失败、research 与 disruption。
  - Portal、Boss summon/respawn、breeding、Dread Spawn、3 Potion AoE、resurrection、5 biome mutation、mass enchanting、weather、house。
  - `ClientRitualEffects` 的活动法阵、祭品连线与成功/失败反馈；消息仅由服务端发送，客户端不决定仪式结果。
- 不含：真人双端仪式视觉/听觉验收、完整 Necronomicon ritual 页面与专用 ItemRitual/PEStream 粒子类型；这些分别留 R4-LIVE-GATE、T6.2b/T6.4b。

## 3. 设计 / 架构

- 关键类：
  - `RitualManifest` / `RitualManifestCatalog`：旧版事实源；注册顺序稳定且引用在 registry 冻结后验证。
  - `RitualRegistry`：按当前维度、book tier、center 与无序 pedestal offerings 找首个精确匹配项。
  - `ManifestRitual`：通用策略把结果写回 center 或替换八个 pedestal；按 manifest 精确复制指定数据键。
  - `RitualBehaviors`：18 个 specialized ID 到真实行为的唯一映射；缺处理器是硬错误。
  - `BiomeRitualTasks`：大半径群系仪式的持久 `SavedData` chunk 队列，每世界每 tick 处理一个 chunk，避免单 tick 扫描默认 256 格半径。
  - `ResurrectionBehavior`：读取命名死亡快照与晶体尺寸，实体成功加入世界后才清除快照。

## 4. 子系统内契约

- **center 与活祭严格分离**：旧构造器参数 `sacrifice` 指祭坛中心物品；`requiresSacrifice` 才表示活祭。
- **服务端权威**：祭坛在消费前一次性规划全部供品；用户离线、结构/center 改变、PE 不足、祭品引用不安全均失败且不产出。
- **能量**：启动前验证总 PE，ceremony 每 20 tick 从手持书/背包 `IEnergyContainerItem` 抽取，失败触发 disruption（受 `no_disruptions` 控制）。
- **知识**：有 `researchId` 时通过 `ResearchRegistry` + `KnowledgeGate` 服务端判定。
- **网络**：`RitualStartMessage`/`RitualMessage` 只发给同维度玩家，经 `SideExecutor` 进入客户端状态机。

## 5. 跨版本 / 加载器要点

- `platform/BiomeMutationCompat`：chunk biome 容器重填与客户端 biome resend。
- `platform/RitualTaskCompat`：Forge/Neo server tick 事件分叉。
- `platform/EnchantmentDataCompat`：1.20 enchantment object map 与 1.21 Holder/component。
- `platform/ItemNameCompat`、`MobSpawnCompat`、`TamableCompat`：名称、触发生成与驯服差异。
- 业务 manifest、匹配、状态机与 18 个处理器不含 loader 分叉。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- 旧源码实际有 **62 次注册**，不是 13；“13”既不是总项数，也不是专用注册项数。当前永久分类是 40/3/1/18。
- 旧 offerings 是无序多重集；manifest 的八槽顺序只用于稳定展示与审计，不改变匹配语义。
- 大范围 biome ritual 必须分 chunk 持久执行；旧配置 `32` 按源码语义对应 `32×8=256` 格半径。
- Forge/Neo 构建严格串行；并行 Stonecutter 生成会造成错误 loader import 的瞬态假失败。

## 7. 验证 / DoD

- Forge/Neo `compileJava --rerun-tasks`：BUILD SUCCESSFUL。
- Forge/Neo `runData`：`RR_RITUAL_MANIFEST_SELF_TEST_OK rituals=62 infusion=40 creation=3 transformation=1 special=18 handlers=18`。
- 同一 Gate 校验顺序、唯一 ID、所有 item/entity/effect 引用、runtime registry 镜像、持续时间公式、18/18 行为覆盖及 29 item model/3 block set 资源合同。
- 未机核项：真人客户端逐仪式的 chant/祭品/成功失败视觉听觉矩阵；Portal 玩家往返另见 R4-LIVE-GATE。

## 修订日志

- 2026-07-26：R4 完成 62 项 manifest、持久 ceremony、18 个专用行为、客户端反馈、必要宿主/资源与双端永久 Gate；纠正旧“13 个仪式”误计。
- 2026-07-22：PS-6 初始框架与 pilot。
