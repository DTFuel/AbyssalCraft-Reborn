# 药水 / 效果 (Potion / MobEffect) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-A
- 关联平行任务：PS-4（本层）；下游 PS-11（效果 event hook）、PC-8（酿造配方）
- 状态：5 MobEffect + 7 Potion、三自定义 DamageType、传播/免疫/解毒/唯一转化/Purge、动态Dreadlands群系扩散、专属宿主与6条brewing mix已实现
- 负责：PS-4
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的药水效果 + 药水。忠实 1.12.2 `MiscHandler`/`PotionBuilder`：珊瑚瘟疫（coralium plague）、恐惧瘟疫（dread plague）、反物质（antimatter）三个有害效果 + 两个解毒（antidote）效果，及其 7 个可酿造药水变体。玩家可见：效果可 `/effect` 施加、显示名/颜色、有害效果每秒扣血；药水可获得（酿造待 PC-8）。

## 2. 范围

- 含：`MobEffectCompat`、`ACEffects`、`ACDamageTypes`、`EffectHooks`、`PurgeHooks`、两种10剂 `AntidoteItem`、效果食物与 `PotionBrewingCompat` 六条旧 mix。
- 行为：周期传播、on-hit、死亡 cloud/衰减传播、内建/配置 carrier+immunity、Coralium/Dread/Antimatter死亡转化、同一次死亡最多一个替代体、Purged 中传播/转化与六类交互限制。
- Dread Plague：高阶效果（或Hardcore）每100t在服务端改写3x3方块覆盖的群系列，动态消费`no_dreadlands_spread`；排除Dark Realm、Omothol、既有Dreadlands变体与Purged。Dreaded Ghoul、legacy Dread mobs、Cha'garoth族、Dreadguard、Dread Shoggoth、Dread Slug、Dreaded Charge均有专属施加点。

## 3. 设计 / 架构

- 包结构：`platform/MobEffectCompat`（fork）· `system/effect/ACEffects`（fork-free 注册 + 回调）。
- 关键类与职责：
  - `platform/MobEffectCompat.ACMobEffect extends MobEffect`：per-tick 行为 = fork-free `Tick` 回调（`(LivingEntity, int amplifier)`）；每 tick 触发（`isDurationEffectTick`/`shouldApplyEffectTickThisTick` → true）。**即 1.12.2 `PotionBuilder` 的等价**（color/category via ctor、performEffect via 回调）。
  - `platform/MobEffectCompat.effectInstance(Supplier<MobEffect>, dur, amp)`：构造 `MobEffectInstance`（fork：raw MobEffect / `Holder` via `wrapAsHolder`）。
  - `system/effect/ACEffects`：`ModRegistrar<MobEffect>`（5）+ `ModRegistrar<Potion>`（7）+ 每效果的 fork-free tick 回调（伤害）。
- 数据流：`/effect` 或药水施加 `MobEffectInstance` → 每 tick `LivingEntity.tickEffects` → `ACMobEffect.applyEffectTick` → `Tick` 回调 → （有害）`entity.hurt(magic, n)`。

## 4. 子系统内契约

- 效果 id：`abyssalcraft:{coralium_plague,dread_plague,antimatter,coralium_antidote,dread_antidote}`（`Registries.MOB_EFFECT`）。
- 药水 id：`abyssalcraft:{cplague,cplague_long,dplague,dplague_long,dplague_strong,antimatter,antimatter_long}`（`Registries.POTION`）。
- i18n：`effect.abyssalcraft.<name>`（效果）；`item.minecraft.potion.effect.<potion_name>`（药水；3 个 distinct name cplague/dplague/antimatter，normal/long/strong 共享显示名，忠实 1.12.2 `PotionType` 命名）。
- 颜色（忠实 1.12.2 `ACClientVars`）：coralium_plague 0x00FFFF / dread_plague 0xAD1313 / antimatter 0xFFFFFF / 两 antidote 0x00FF06。
- duration（忠实 1.12.2 `MiscHandler`）：normal 3600t / long 9600t / dplague_strong 432t amp1。
- 对外 API：`ACEffects.{CORALIUM_PLAGUE,DREAD_PLAGUE,ANTIMATTER,CORALIUM_ANTIDOTE,DREAD_ANTIDOTE}`（`Supplier<MobEffect>`），供 PS-11 效果 hook 引用。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：新增 `platform/MobEffectCompat`（本层 fork 边界）。
- **MobEffect 两端仍代码化**（异于 1.21 数据驱动的附魔）；ctor `MobEffect(MobEffectCategory, int color)` + `Potion(String, MobEffectInstance...)` 两端签名稳定 → 注册走标准 fork-free `ModRegistrar`。两处 fork（javap 双 jar 核）：
  - **per-tick hook**：1.20.1 `void applyEffectTick(LivingEntity,int)` + `boolean isDurationEffectTick(int,int)` ↔ 1.21 `boolean applyEffectTick(LivingEntity,int)`（返回是否续存）+ `boolean shouldApplyEffectTickThisTick(int,int)`（重命名）。
  - **`MobEffectInstance` 构造**：1.20.1 `(MobEffect,...)` ↔ 1.21 `(Holder<MobEffect>,...)`（用 `BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)` 取 Holder；注册顺序保证 Potion 注册时 MOB_EFFECT 已冻结）。
- `//?` 分叉点：**业务零 `//?`**。tick + instance fork 全在 `MobEffectCompat`；`ACEffects` 只用 `ModRegistrar`/`MobEffectCompat`/vanilla。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **effect 探针须控制日晒燃烧**：僵尸在 y=100 日照下燃烧掉血 → 会混淆「效果扣血」判定（我首测僵尸死于日晒非效果）。用**牛**（被动、不燃烧、10HP）+ **唯一 tag** 单目标：`/effect give @e[tag=X] antimatter` → 牛死 = 伤害 tick 实证。
- **多目标 `@e[type=zombie,limit=1]` 串目标**：若场上有 2 只僵尸，`/effect` 与 `/data get` 可能命中不同实体（首测 coralium 显示 HP 20 = 读到未中招的另一只）。用唯一 `Tags:["X"]` + `@e[tag=X,limit=1]` 保证同一实体。
- **每 tick 扣血 vs 无敌帧**：`hurt` 后有 ~10-20t 无敌（仅更大伤害穿透）；回调用 `tickCount % N == 0` 控节奏（现每 20t 一次），配合足够伤害（antimatter 5）可观测。
- **`wrapAsHolder` 注册顺序**：1.21 `effectInstance` 在 Potion 注册期调 `wrapAsHolder(effect.get())`，依赖 MOB_EFFECT 先于 POTION 注册（vanilla 注册表依赖序保证）；runServer `Done` 即证无崩。
- **死亡转化必须共享 `spawned`**：Coralium/Dread/Antimatter、配置型 demon 与 Shadow 转化在一个死亡事件中串行仲裁；传播先冻结效果状态并全部执行，替代体只允许首个成功 `addFreshEntity`。

## 7. 验证 / DoD

- 两节点 `compileJava`：BUILD SUCCESSFUL。
- 两节点 `runServer`：`Done`（`MOB_EFFECT`+`POTION` 冻结无崩；neo `wrapAsHolder` 建 7 potion 无崩）+ 干净 stop。
- **两节点 `/effect` 探针实证效果生效**：`summon minecraft:cow ... {Tags:["ps4test"]}`（10HP、不燃烧）→ `/effect give @e[tag=ps4test] abyssalcraft:antimatter` → **`Applied effect Antimatter to Cow`**（注册 + i18n 显示名）→ 数秒后牛 10HP→死（`data get Health` = No entity）= **伤害 tick 生效双端**。
- 两节点 `runClient`：抵标题屏（effect/potion 注册不破客户端加载）。
- 双端 production build/JAR通过；JAR含三 damage_type 与 bypass tags。
- Forge黑盒：三 DamageType实际扣血；Antimatter→AntiPig；多效果同死即时`count:1`；Coralium antidote后NBT只剩antidote。Neo黑盒：三 DamageType实际扣血，Antimatter→AntiPig。
- 永久`RR_DREAD_PLAGUE_SELF_TEST_OK`覆盖配置开/关与动态重载、amplifier/Hardcore、线程/tick/维度/群系边界、8个宿主registry id及chunk unsaved持久化契约。本轮两节点执行均被Gradle缓存空Zip在配置期阻塞；效果图标/粒子/药水表现仍属客户端视觉验收。

## 修订日志

- 2026-07-27：T7.10c完成Dread Plague动态群系扩散、配置消费、宿主registry审计和永久自测。
- 2026-07-25：RR-KNOWLEDGE（CR-70）完成三DamageType、传播/carrier/immunity/两解毒/唯一转化/Purge、食物与六条brewing；双端专服关键行为通过，动态群系扩散拆T7.10c。
- 2026-07-22：PS-4 建层——`platform/MobEffectCompat`（MobEffect tick + instance fork）+ `system/effect/ACEffects`（5 MobEffect + 7 Potion）；两节点编译 + runServer `/effect`「antimatter 杀牛」双端实证伤害 tick + runClient 标题屏。完整效果延 PS-11、酿造延 PC-8。见平行表 CR-46。
