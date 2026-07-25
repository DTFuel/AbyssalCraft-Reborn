# 存档数据 / 事件钩子 (Necromancy SavedData + Knowledge Hooks) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-C
- 关联平行任务：PS-11（本框架）；**写** PS-2 `NecroData`（知识触发）→ **PS-8** `KnowledgeGate` 读（知识环路闭合）；反馈 PS-1（necrodata 同步消息）
- 状态：命名非Boss死亡快照生产、真实SavedData落盘/重启、biome/plague/Purge/mutation hooks与同步接线已完成；复活消费和page/附魔剩余hooks留T7.11c
- 负责：PS-11
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的世界存档数据 + 游戏事件钩子。现代实现以 `NecromancyData` 保存最近 20 个命名非Boss Mob死亡快照，以 `GameHooksCompat` 封装双加载器事件，再委托 `KnowledgeHooks`、`EffectHooks` 与 `PurgeHooks` 完成知识触发、瘟疫传播/转化和 Purged 交互限制。复活仪式消费快照仍是 T7.11c/T7.6b 的下游内容。

## 2. 范围

- 含：
  - `platform/SavedDataCompat`（`SavedData` 持久化 + 访问的版本 fork）+ `system/data/NecromancyData`（复活快照存档，业务）。
  - `platform/GameHooksCompat`（game-bus 事件订阅的 loader fork）+ `common/handlers/KnowledgeHooks`（知识触发回调，业务）。
  - 主类 `init` 一行 `GameHooksCompat.attach()`（永久，激活钩子）。
- 已补内容：`GameHooksCompat` 生产命名非Boss Mob快照；`KnowledgeHooks` 杀怪/维度/200t群系/plague/book触发和延迟同步；`EffectHooks`/`PurgeHooks`承载Plague/Purge事件。
- 不含：`NecronomiconResurrectionRitual`读取并`clearEntry`快照；真实page内容与5附魔事件（T7.11c）。

## 3. 设计 / 架构

- 关键类：
  - `platform/SavedDataCompat`（abstract extends `SavedData`）：`save(...)` fork（1.20.1 `save(CompoundTag)` ↔ 1.21 `save(CompoundTag,HolderLookup.Provider)`）委托 fork-free `saveData(CompoundTag)`；静态 `getOrCreate(ServerLevel,name,create,load)` fork（`getDataStorage().computeIfAbsent(load,create,name)`[1.20.1] ↔ `computeIfAbsent(new SavedData.Factory<>(create,(tag,reg)->load.apply(tag)),name)`[1.21]）。
  - `system/data/NecromancyData`（extends `SavedDataCompat`，业务 fork-free）：`List<Entry(name,CompoundTag)>` ≤20（超 20 丢最旧）+ `storeData(name,tag,crystalSize)`（写 `ResurrectionRitualCrystalSize`）/ `getDataForName` / `clearEntry` / `getData` / `saveData`（按 name 分组入 ListTag）/ `load`（重建）；`get(ServerLevel)` 经 `SavedDataCompat.getOrCreate`。
  - `platform/GameHooksCompat`（loader fork）：`attach()` = `EventBuses.game().addListener(...)` 订阅 `LivingDeathEvent`（杀怪）+ `PlayerEvent.PlayerChangedDimensionEvent`（换维度）；事件类名跨 loader 分叉（`net.minecraftforge.event.*` ↔ `net.neoforged.neoforge.event.*`）封于此，body 抽取 (player, 数据) 调 fork-free 回调。
  - `common/handlers/KnowledgeHooks`（业务 fork-free）：`onEntityKilled(player,victim)`（`BuiltInRegistries.ENTITY_TYPE.getKey` → `NecroDataCapability.get(player).triggerEntityUnlock`）+ `onDimensionChanged(player,dim)`（`dim.location()` → `triggerDimensionUnlock`）；均 server 端（`player.level().isClientSide` 早退）。

## 4. 子系统内契约

- **写 PS-2（核心跨任务契约）**：知识钩子经 PS-2 `NecroDataCapability.get(player)` 写触发列表（entity/dimension）；PS-8 `KnowledgeGate` 读之解锁研究 → **知识环路闭合**（PS-11 写、PS-8 判、PS-2 存）。
- **PS-6 仪式**：`NecromancyData` 的复活快照供 PS-6 `NecronomiconResurrectionRitual`（延后）消费。
- 反馈 → 协议v2 `KnowledgeUnlockMessage`增量 + `NecroDataCapMessage`完整权威快照；开书兼容ShouldSync往返。
- 对外 API：`NecromancyData.get(serverLevel)` 供死亡钩子存快照 + 复活仪式读；`SavedDataCompat` 供其它世界存档子系统复用（如 PS-1 necromancy 相关）。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：**新增 2**——`SavedDataCompat`（版本轴：`SavedData.save` + `computeIfAbsent`）+ `GameHooksCompat`（加载器轴：game-bus event 类名）。业务 `NecromancyData`/`KnowledgeHooks` 零 `//?`。
- **`SavedData` fork**（版本，同 `BlockEntityCompat` 先例）：1.21 给 `save` 加 `HolderLookup.Provider` 参 + `computeIfAbsent` 改收 `SavedData.Factory`（构造 + 反序列化 BiFunction）。
- **game-bus event fork**（加载器，同 `EventBuses` 先例）：event 类在 `net.minecraftforge.event.*`（Forge）↔ `net.neoforged.neoforge.event.*`（Neo）；`EventBuses.game()` 已 fork-free 给出 game bus，`GameHooksCompat` 只封 event import + `addListener`。
- **坑**：`PlayerEvent.PlayerChangedDimensionEvent.getEntity()` 已返 `Player`（非 `Entity`）→ 不可再 `instanceof Player`（Java 报「模式类型 Player 是表达式类型 Player 子类型」编译错，两端皆然）→ 直接用 `event.getEntity()`。`LivingDeathEvent.getEntity()` 返 `LivingEntity`（可 `instanceof Player` 排除玩家死亡）。
- fork-free 业务用 `BuiltInRegistries.ENTITY_TYPE.getKey` / `player.level()` / `ResourceKey.location()`（两端同）。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容 + 补 PS-2 消费侧**：知识环路的「存」（PS-2）+「判」（PS-8）已建，PS-11 补「写触发」（钩子）+「复活存档」（NecromancyData）；Plague/Purge + 复活消费依赖未移植 plague 传播/dread/仪式内容 → 延后。
- **`NecromancyData` 数据/持久分离可测**：把「≤20 快照 + save/load」逻辑放业务类（fork-free NBT），持久/访问 fork 隔在 `SavedDataCompat` → selfTest 在 `new NecromancyData()` 裸实例上测 store/cap-20/round-trip，无需 live world（`setDirty()` 在未附着实例上只置布尔、安全）。
- **`PlayerChangedDimensionEvent.getEntity()` 冗余 instanceof 编译错**（见 §5 坑）——首次编译即撞，去 instanceof 后两端过。
- **selfTest 触发 + 永久挂钩分离**：`NecromancyData.selfTest()` 临时挂主类（`runData` 触发后**还原**，含 selfTest-only `LOGGER` import）；但 `GameHooksCompat.attach()` **永久保留**（激活钩子）→ 该行随框架留存（有 CR，同 PS-1/PS-2 bootstrap 先例）。
- **runData 即验挂钩**：`GameHooksCompat.attach()` 在 mod init 跑 → `runData` 完成 = 钩子注册无崩；另两节点 `runServer` `Done` = 服务器启动带钩子无崩（钩子被动、无 gameplay 不触发，故 runServer 仅证注册有效，实际触发需活会话）。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL（`SavedDataCompat`/`GameHooksCompat` 两 fork 双端有效）。
- **`NecromancyData` selfTest（临时，已还原）**：store 22 → cap 20（丢 e0/e1 最旧）+ `getDataForName`（e21 在、crystal size=21）+ `clearEntry`（e21 去、size 19）+ save→load round-trip（alpha/beta 值 + crystal size 保真）—— **forge/neo 均 `PASS`**（`runData` 触发 init）。
- **两节点 `runServer` `Done`**（forge 17.560s / neo 5.456s + 干净 stop）：`GameHooksCompat.attach()` 永久挂钩在服务器启动无崩。
- 永久自测覆盖≤20淘汰、save/load round-trip与crystal-size边界；双端runData为42/42/11 Gate的一部分。
- Forge独立专服召唤命名牛并死亡，`abyssalcraft_necromancy.dat`压缩NBT确认含`RRK_SNAPSHOT`与`ResurrectionRitualCrystalSize`；同世界重启抵`Done`并正常保存八维。
- 未完成：复活仪式消费、page/附魔hooks与双端真人玩家知识实网矩阵。

## 修订日志

- 2026-07-25：RR-KNOWLEDGE（CR-70）完成死亡快照生产、真实NBT落盘/重启、biome/plague/Purge/mutation hooks与同步；复活消费拆T7.11c。
- 2026-07-22：PS-11 建框架——`platform/{SavedDataCompat,GameHooksCompat}` + `system/data/NecromancyData` + `common/handlers/KnowledgeHooks` + 主类 `GameHooksCompat.attach()`（永久，CR）。两节点编译 + selfTest（NecromancyData round-trip）+ `runServer` `Done` 双端 PASS。写 PS-2 `NecroData` 触发 → PS-8 读（知识环路闭合）。Plague/Purge 钩子 + 复活消费 + 客户端同步接线 + biome 触发延后（依赖未移植 plague/dread/仪式 + 活会话）。见平行表 PS-11。
