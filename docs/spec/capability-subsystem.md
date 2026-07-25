# 能力 / 玩家数据 (Capability) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-A
- 关联平行任务：PS-2（本层）；下游消费者 PS-8（知识/死灵之书）、PS-1（同步消息）
- 状态：necrodata 存储、变化检测、客户端 handler 与延迟同步实现已双端编译/runData/server/build验证；活玩家死亡复制、断线重连与实网 UI 矩阵留 T7.2c
- 负责：PS-2
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的「附着在游戏对象上的持久数据」层。1.12.2 用 Forge Capability 附两类数据：**物品转移能力**（附在方块实体上，存转移路由）与 **necrodata 能力**（附在玩家上，存死灵之书知识解锁进度）。移植后：

- **物品转移**：已由 PC-4 现代化为 `system/transfer/ItemTransferHost`（方块实体直接实现的接口，`instanceof` 查询，fork-free），不再走 loader 能力机制 → 本子系统不重复。
- **necrodata**：玩家附着的持久 + 可同步数据，是本子系统的核心交付。

## 2. 范围

- 含：`platform/PlayerDataCompat`（每玩家中性 `CompoundTag` 附着的 loader 兼容层）、`system/cap/necrodata/**`（necrodata 数据层 + accessor）、主类 `init` 一行 `PlayerDataCompat.bootstrap(modBus)`。
- 不含：
  - 物品转移能力（PC-4 `ItemTransferHost`，见 [machine/transfer 相关]）。
  - necrodata 的**消费逻辑**（`isUnlocked`、解锁条件处理、研究门控）——属知识子系统 PS-8。
  - 活玩家死亡、断线、重连、停服重启的双客户端实网验收（T7.2c）；handler 实现本身已接线。

## 3. 设计 / 架构

- 包结构：`platform/PlayerDataCompat` · `system/cap/necrodata/{KnowledgeType,NecroData,NecroDataCapability}`。
- 关键类与职责：
  - `platform/PlayerDataCompat`：把「每玩家持久数据」的 loader 分叉封成一个中性 `CompoundTag`。`bootstrap(Object modBus)` 注册；`getTag(Player) -> CompoundTag`（活引用，就地改即持久）；`setTag(Player, CompoundTag)`。
  - `system/cap/necrodata/KnowledgeType`：5 知识分枝 enum（BASE/ABYSSAL/DREAD/OMOTHOL/SHADOW）。
  - `system/cap/necrodata/NecroData`：`CompoundTag` 的薄视图（fork-free vanilla NBT），忠实 1.12.2 数据面——7 解锁触发列表（entity/biome/dimension/artifact/page/whisper/misc）+ 完成研究列表 + 全知识标志 + 知识等级 + 5 枝知识点。改动直接写回 backing tag。
  - `system/cap/necrodata/NecroDataCapability`：`get(player) -> NecroData` accessor（承 1.12.2 `NecroDataCapability.getCap`）+ `save(player) -> CompoundTag`（发客户端用）+ `apply(player, tag)`（客户端应用同步）。
- 数据流：业务 `NecroDataCapability.get(player)` → `PlayerDataCompat.getTag(player)`（loader 附着）→ 包成 `NecroData` → 读写触发 backing tag 的就地修改 → 存档时 loader 自动序列化。客户端同步：服务端 `save(player)` → PS-1 necrodata 消息 → 客户端 handler（PS-8）`apply(clientPlayer, tag)`。

## 4. 子系统内契约

- 附着 key：`abyssalcraft:necrodata`（Forge 能力 ResourceLocation / Neo AttachmentType 注册名一致）。
- NBT 键（NecroData over the tag）：`entityTriggers`/`biomeTriggers`/`dimensionTriggers`/`artifactTriggers`/`pageTriggers`/`whisperTriggers`/`miscTriggers`/`completedResearches`（均 `ListTag<StringTag>`）、`HasAllKnowledge`(boolean)、`knowledgeLevel`(int)、`kp_<TYPE>`(int，每枝一键)。**现代化**：1.12.2 的 `dimensionTriggers`（int dim id 列表）→ String（dimension `ResourceLocation`）；1.12.2 的 `knowledgePoints`（`"TYPE;value"` 字符串列表）→ 每枝独立 `kp_<TYPE>` int 键。无旧存档迁移（全新移植）。
- 对外 API：`NecroDataCapability.{get,save,apply}`（供 PS-8 知识逻辑 + PS-1 同步 handler）；`PlayerDataCompat.{getTag,setTag}`（供其他将来的玩家持久数据复用，中性 CompoundTag）。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：新增 `platform/PlayerDataCompat`（本层唯一 fork 边界）。
- **最大 loader 分歧**（javap 双 jar 核）：
  - **Forge 1.20.1 实体能力**：`Capability<Holder>`（`CapabilityManager.get(new CapabilityToken<>(){})`）+ `Holder implements ICapabilitySerializable<CompoundTag>`（自托管 provider，`LazyOptional<Holder>`）；`RegisterCapabilitiesEvent`（mod 总线）注册 `Holder.class`；游戏总线 `AttachCapabilitiesEvent<Entity>` 给 Player 附 `Holder`；游戏总线 `PlayerEvent.Clone`（`isWasDeath()`）+ `Entity.reviveCaps()/invalidateCaps()` 死亡拷贝。
  - **NeoForge 1.21 数据附着**（1.21 删实体能力）：`AttachmentType<CompoundTag>` via `DeferredRegister` 到 `NeoForgeRegistries.Keys.ATTACHMENT_TYPES`，`.serialize(CompoundTag.CODEC)`（存档序列化）+ `.copyOnDeath()`（死亡拷贝）；`IAttachmentHolder.getData/setData`（`Entity` 已实现）。
- `//?` 分叉点：**业务零 `//?`**。能力/附着分叉全在 `PlayerDataCompat`；`NecroData`/`NecroDataCapability` 只用 vanilla NBT + `PlayerDataCompat`，两端同构。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **neo `AttachmentType.builder(CompoundTag::new)` 方法引用歧义**（编译报错级证据）：`builder` 有 `builder(Supplier<T>)` 与 `builder(Function<IAttachmentHolder,T>)` 两重载，`CompoundTag::new`（无参 ctor 方法引用）在两者间歧义 → 用显式 0 参 lambda `() -> new CompoundTag()`（arity 0 只匹配 Supplier）消歧。
- **Forge `PlayerEvent.Clone` 读原玩家能力须 `reviveCaps()`**：玩家死亡后原实体能力被 invalidate，Clone 中读之前先 `event.getOriginal().reviveCaps()`、读完 `invalidateCaps()`（Forge 文档模式）。
- **中性 `CompoundTag` 存储**：把 loader 能力/附着的 payload 定为 vanilla `CompoundTag`（`CompoundTag.CODEC` 1.21 存在）→ 业务只碰 NBT、fork 边界最小；`getTag` 返回活引用，就地改即持久（两端 provider/attachment 都存同一实例）。
- **真人矩阵与实现门禁分离**：headless 可验证 capability/attachment 注册、mutation、codec/client handler classload与延迟调度；死亡 clone/copyOnDeath、断线重连和书 UI 实网表现仍需 T7.2c 真人会话。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL。
- 运行期（临时主类 `init` 挂 `NecroDataCapability.selfTest()`，**验证后已还原**）：
  - **NecroData NBT 数据层 round-trip**：写触发（含去重）/研究/等级/知识点 → `tag.copy()` 重新包 `NecroData` 读回全等 → **forge/neo 均 `PASS`**。
  - **能力/附着注册 bootstrap 不崩**：两节点 `runServer` 抵 `Done` + 干净 stop（Forge `RegisterCapabilitiesEvent`+token+游戏总线监听 / Neo `AttachmentType` DeferredRegister 均无崩）——**neo 现抵 Done**（得益 G0/CR-39 修 `dimension_type/mini.json`）；两节点 `runClient` 抵标题屏（客户端侧注册无崩）。
- `NecroData` mutation 全部返回是否变化；协议 v2 的 `KnowledgeUnlockMessage`/`NecroDataCapMessage` 客户端 apply、ShouldSync 往返、登录/重生/换维延迟同步与开书配置均已接线并通过双端编译/服务端加载。
- 未机核项（如实标注）：活玩家死亡 clone/copyOnDeath、跨维/断线重连和客户端书副本的双端实网矩阵，归 T7.2c。

## 修订日志

- 2026-07-25：RR-KNOWLEDGE（CR-70）完成 mutation 变化检测、协议 v2 客户端 handler、延迟同步与配置门控；实现切片 T7.2b 完成，真人实网拆 T7.2c。
- 2026-07-22：PS-2 建层——`platform/PlayerDataCompat`（Forge 能力 / Neo 数据附着）+ `system/cap/necrodata/**`；两节点编译 + runServer/runClient 注册 + NecroData NBT self-test 双端 PASS。item transfer 能力确认由 PC-4 `ItemTransferHost` 覆盖。见平行表 CR-41。
