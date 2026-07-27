# 扰动 (PE Disruption) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-C
- 关联平行任务：PS-9框架 + RR-ENERGY内容；反馈网络仍归R5
- 状态：旧27项全部MIGRATED、BLOCKED=0，statue/depositioner触发链完成
- 负责：GitHub Copilot
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的势能扰动系统。statue/depositioner在PoP外传输累积容差，达到100/200后由`DisruptionHandler`按deity过滤并执行随机扰动。当前27项旧扰动全部迁移，内容依赖由永久 registry 审计 fail-closed。

## 2. 范围

- MIGRATED 27项：原22项 + ooze、randomSwarm、randomSpawn、invisibleSwarmHastur、invisibleSwarmNyarlathotep。
- Animal corruption覆盖旧明确分支：牛/鸡/猪/羊→Evil，普通马→亡灵马，驯服狼/猫解除驯服，兔→杀手兔。
- BLOCKED 0项：ooze使用现有分层`shoggoth_ooze`；random两项忠实读取当前位置群系MONSTER加权表并排除lesserdreadbeast；两invisible项忠实生成2-5只隐身II 12000t的Enderman。
- R5边界：`DisruptionMessage`网络记录/客户端FX仍延后；当前服务端向附近玩家发送本地化可观察反馈。

## 3. 设计 / 架构

- 关键类：
  - `Disruption`（抽象）：`name` + 可选 `deity`（PS-5 `DeityType`，null = 任意）+ `translationKey`（`ac.disruption.` + name）+ 抽象 `disrupt(Level, BlockPos, List<Player>)`（server 端对 16 格内玩家/实体作恶）。
  - `DisruptionHandler`（单例 `instance()`）：`registerDisruption`（name 去重）/ `getDisruptions`（unmodifiable）/ `find(name)` / `getRandom(DeityType, RandomSource)`（**deity 过滤**：`deity()==null || deity()==given` —— deity-less 恒合格；deity==null 时只取 deity-less；deity!=null 时取匹配 + deity-less）/ `generate(deity, level, pos, players)`（server 端 `getRandom` + `disrupt`）。
  - `PotionDisruption`（pilot，忠实 `DisruptionPotion`）：`Supplier<MobEffectInstance>` 施于 16 格内每个 `LivingEntity`（工厂供每实体一个新实例）。
  - `LightningDisruption`（pilot，忠实 `LIGHTNING` = `DisruptionSpawn` 家族代表）：`EntityType.LIGHTNING_BOLT.create(level)` + `moveTo` + `addFreshEntity`。
  - `PlayerDisruption`（忠实 1.12.2 player-list 扰动族 `DisruptionFire`/`DisruptionFreeze`/`DisruptionFamine`/`DisruptionTeleportRandomly`）：`BiConsumer<Player, Level>` 施于传入 `players` 列表每个玩家（server 端 guard）。
  - `Disruptions`（内容注册，`bootstrap()` 主类 `init` 挂）：注册27项MIGRATED扰动，覆盖spawn/swarm、药水、玩家、PE、腐化、位移、ooze和火焰族；集合及deity映射由`DisruptionAudit`永久校验。

## 4. 子系统内契约

- 对外 API：`Disruption` 供具体扰动 `extends`（实现 `disrupt`）；`DisruptionHandler.instance().registerDisruption(...)` 供内容注册；manipulator BE 调 `generate`。
- **PS-5 能量**：`deity` 用 PS-5 `DeityType`（7 神）；触发方 = PS-5 的 energy manipulator BE（内容）。
- 反馈：服务端本地化可观察反馈已接入；PS-1 `net.client.DisruptionMessage`客户端网络FX仍留R5。

## 5. 跨版本 / 加载器要点

- 跨版本分叉封于`IgniteCompat`、`MobSpawnCompat`、`TamableCompat`；具体扰动业务代码无loader API。
- **`new MobEffectInstance(MobEffects.POISON, dur)` 两端 fork-free**（关键）：常量 `MobEffects.POISON` 1.20.1 是 `MobEffect`、1.21.1 是 `Holder<MobEffect>`；`MobEffectInstance` ctor 参 1.20.1 收 `MobEffect`、1.21.1 收 `Holder<MobEffect>` —— **常量与 ctor 参 co-vary**，同一表达式两端各自成立、无需 fork（异于 PS-4 的 AC 效果需 `wrapAsHolder`——那是自建 `MobEffect` 无现成 Holder）。
- `EntityType.LIGHTNING_BOLT.create(Level)`（PD-4 已核双端同签名）/ `addFreshEntity`（PS-6 已用）/ `AABB(BlockPos).inflate` / `getEntitiesOfClass` / `LivingEntity.addEffect` 两端同签名，**编译实证**。
- **内容扩充 fork-free API**（编译 + runData 实证）：`Player.getFoodData().setFoodLevel(int)`（famine）、`LivingEntity.randomTeleport(double,double,double,boolean)`（randomTeleport，替 1.12.2 手写 `EnderTeleportEvent` 落点循环）、`MobEffects.MOVEMENT_SLOWDOWN`（freeze/slowness 用同一常量名，两端存在）两端同签名；coralium 用 PS-4 `MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 600, 0)`（Holder-wrap fork 封于 compat，异于 vanilla 效果 co-var——AC 自建效果无现成 Holder）。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容（历史实施路径）**：PS-9先交付抽象基、handler和fork-free pilot；RR-ENERGY扩至22项后，本轮在ooze块和实体生态就绪时闭合为27项。
- **deity 过滤单条件覆盖两分支**：忠实 1.12.2「deity==null 只取 deity-less；deity!=null 取匹配+deity-less」→ 单条件 `d.deity()==null || d.deity()==given` 两情形皆正确（given==null 时退化为只取 deity-less）。selfTest 三向覆盖（null / 非配 / 配 deity）。
- **potion 用 Supplier 非实例**：`MobEffectInstance` 施加时被 copy，但为稳妥每实体取新实例（`Supplier.get()`），避免共享可变实例；vanilla 效果工厂 fork-free（见 §5）。
- **PS-9阶段selfTest（历史）**：临时 `DisruptionHandler.selfTest()` 挂主类 `init`，`runData` 触发；用 `new DisruptionHandler()`（私有 ctor 类内可访问）避污染单例；核完已还原。当前由永久`DisruptionAudit`和`EnergySelfTest`覆盖注册闭包。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`与`runData`：BUILD SUCCESSFUL。
- **PS-9阶段`DisruptionHandler` selfTest（临时，已还原）**：register（有效 + name 重复被拒 → size==2）+ `find` + `getRandom` deity 过滤（null-deity 只取 deity-less / 非配 deity 排除 deity-locked / 配 deity 含两者）—— **forge/neo 均 `PASS`**（`runData` 触发 init）。
- 永久`DisruptionAudit`精确断言注册集合=27、BLOCKED=0、总闭包27、deity映射无漂移，并对 `abyssalcraft:shoggoth_ooze`、`abyssalcraft:lesserdreadbeast`、`minecraft:enderman` 依赖 fail-closed；标记为`RR_ENERGY_SELF_TEST_OK ... disruptions=27 blocked=0`。
- 双端真实Level验证statue/depositioner状态和PoP免容差持久化；实体/火焰扰动注册与参数由永久自测/审计覆盖，客户端视觉反馈留R5。

## 修订日志

- 2026-07-27：T7.5c闭合ooze、random spawn/swarm、两invisible swarm；永久审计更新为27 MIGRATED / 0 BLOCKED。
- 2026-07-25：RR-ENERGY将扰动扩至22项并接入两类manipulator容差触发；新增22+5四态审计，5项依赖不足内容保持BLOCKED。

- 2026-07-22（PS-9 内容扩充 / CR-54）：新 `Disruptions`（`bootstrap()` 主类 init 挂，注册 10 vanilla-only 具体扰动）+ `PlayerDisruption`（玩家列表 `BiConsumer` 施动）。10 忠实扰动：lightning + 5 药水云（poison/slowness/weakness/wither + coralium）+ 4 玩家定向（freeze/randomTeleport/famine×2）。两节点编译 + runData selfTest `count=10 ok=true`。余具体扰动（实体 spawn/swarm/ooze/PE/corruption/fire）分离为 PS-9b。见平行表 CR-54。
- 2026-07-22：PS-9 建框架——`system/energy/disruption/**`（`Disruption` 基 + `DisruptionHandler` deity 过滤随机 + potion/lightning pilot）；两节点编译 + selfTest 双端 PASS。~27 具体扰动 + deity-image 检测 + 网络反馈延后（依赖未移植实体/能量块/书）。见平行表 PS-9。
