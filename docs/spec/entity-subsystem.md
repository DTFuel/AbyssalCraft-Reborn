# 实体子系统规格 (Entity Subsystem Spec)

- 里程碑 / Stage：M3 / Stage D1（框架）→ D2a/D2b（具体实体族/BOSS）→ Stage E1（渲染注册框架，Gate E1 ✅）
- 关联平行任务：PD-1（EntityType/属性/基类框架，本文档主体）、PD-2（AI goals + pathfinding，Agent 13）；PD-3..PD-7（anti/demon/ghoul+shoggoth/misc+projectile/BOSS）；PE-1（渲染注册框架，§12）
- 状态：**RR-ENTITY-BEHAVIOR 已交付（Gate M3-CONTENT ✅）** —— 旧 63 个内容 EntityType、48 刷怪蛋、44 placement、全族专属行为、69 份旧实体 loot 与双加载器自然生成矩阵均已收口。现代 loot 由单一 datagen owner 生成 97 张逻辑表/194 张双路径物理表；双端行为、69 死亡路径、11 场真实 `NaturalSpawner`、持久化重启、无属性专服 `/reload`、production build/JAR 审计全部通过。
- 负责：PD-1 框架 · PD-3 anti 家族 · RR-ENTITY-CATALOG · RR-ENTITY-BEHAVIOR（GitHub Copilot）
- 最后更新：2026-07-26

## 0A. RR-ENTITY-BEHAVIOR 实现边界（2026-07-25）

- 冻结基线：63 个内容 EntityType、48 蛋、44 placement、34 个唯一实体 loot 与 Forge/Neo 各 9 个 spawn modifier；本任务不重新注册或改名。
- 当前真实已实现而只需回归：Evil 剪毛/死亡变身、Depths Ghoul plague、Omothol Ghoul 三效果与火/毒免、Ink/DreadSlug 效果、Legacy Dread 聚合/分裂/远程与 Shadow breath/死亡转化。
- 旧资源目录精确包含 69 份实体 loot。永久 `EntityLootAudit` 将每份标为 DIRECT、CONDITIONAL 或 REPLACED（当前无未解释 RETIRED），并验证所有目标现代 EntityType 已注册。
- 本任务主路径包含 anti 湮灭、demon/ghoul 边角、Shoggoth food/acid/ooze/worship/建碑/多部件、misc/projectile 完整生命周期、Boss 阶段/召唤/Dragon parts、Remnant Merchant、69 loot 与双端自然生成统计；不再以“后续系统”笼统延后。
- 文件边界：不修改 `client/render/**`、仪式/法术/能量/知识业务目录或 BER host；仅消费 R2/R3 已冻结 API。共享 compat、datagen relay、文档和状态只在串行 Gate 修改。

## 0B. RR-ENTITY-BEHAVIOR 最终交付（2026-07-26）

- **专属行为**：anti、demon/evil、ghoul、Shoggoth、misc/projectile、Boss/elite 与 9 个 legacy 实体均完成生产接线。双端行为矩阵输出 `RR_ENTITY_BEHAVIOR_LIVE_OK loot=5 shoggoth=6 monolith=1 boss=4 remnant=7 legacy=3`；Shoggoth 酸击/喷酸、腐蚀、进食/繁殖、ooze、膜拜、建碑、多部件与 5 TYPE，四 Boss 阶段链、Dragon parts、Remnant 七职业交易/剪毛，以及 legacy 墨弹、breath/转化、周期生成与 plague 均走真实实体逻辑。
- **现代战利品表**：`EntityLootAudit` 永久冻结 69 项旧表为 `32 DIRECT / 21 CONDITIONAL / 16 REPLACED / 0 RETIRED`，并维护 28 个现代 alias、97 张逻辑表与 8 张合法空表。`EntityLootData` 是唯一 owner，同时生成 1.20.1 `loot_tables/entities/` 与 1.21.1 `loot_table/entities/` 共 194 张物理表；Boss 固定奖励也在表中，不使用 Java 硬编码死亡掉落。双端运行矩阵输出 `RR_ENTITY_LOOT_OK tables=97 nonEmpty=89 empty=8 scenarios=69 outputItems=53 deathPaths=69`。
- **真实自然生成**：验证直接调用 vanilla `NaturalSpawner.spawnCategoryForPosition`，使用真实 FakePlayer、强加载 chunk、暗场/水场和 after-spawn 回调，不以 `/summon` 或静态候选列表代替。11 场覆盖主世界、四个 AC 维度、Dreadlands 四种群系上下文、水生场景及两个 `Y<=5` 覆盖场景；双端均输出 `RR_ENTITY_NATURAL_SPAWN_OK scenarios=11 start=0 end=10`。`SpawnCandidateCompat` 使用事件 remove/add API，并缓存稳定 `SpawnerData` 对象以满足 vanilla 二次候选查询的对象身份要求。
- **存档与重启**：双端均以真实世界完成 5 个代表实体及 5 个 owner 关联的 create→stop→verify，输出 `RR_ENTITY_PERSISTENCE_CREATE_OK entities=5 owners=5` 与 `RR_ENTITY_PERSISTENCE_VERIFY_OK entities=5 owners=5`。
- **永久 Gate 与生产制品**：双端 `runData` 输出 `RR_ENTITY_LOOT_DATA_OK audit=69 aliases=28 logical=97 physical=194` 和 `RR_ENTITY_BEHAVIOR_SELF_TEST_OK ... logicalLoot=97 emptyLoot=8 spawnPairs=9 snapshots=18`；双端 production build 成功。最终 JAR 各含 97 单数 + 97 复数实体 loot、9 Forge + 9 Neo spawn modifier、正确且互斥的 loader metadata，以及生产 audit/self-test/candidate class；临时 `RREntity*` class、验证属性和快照标记为 0。SHA-256：Forge `13DAFBEBD5F666B32B2E4A63A8D4F43124105790801B38937463E41F1B9D3A5C`，NeoForge `BBA3A29203EDEC30720EC17DEC12DFF81E72F35706276207D0B7B7880AD7EF7F`。
- **发布态 smoke**：Forge/NeoForge 均在不带 RR 验证属性时启动至 `Done`，输出 `RR_ENTITY_CATALOG_OK content=63 all_ac=64 eggs=48 placements=44`，执行 `/reload` 后无实体 loot/spawn 解析错误，再正常 `stop` 并保存全部 8 个维度。Forge 仍报告既有 9 个 advancement 格式错误，与本实体交付无关。
- **历史段落说明**：§8–§11A 保留各 PD/RR-CATALOG 交付当日的实现快照；其中“延后/未完成”描述已由本节和当前总任务表取代，不再代表实体子系统现状。

## 1. 概述 / 目标

把 1.12.2 的实体体系（~40 生物 + 投射物，`EntityRegistry.registerModEntity` + `applyEntityAttributes` + `EntitySpawnPlacementRegistry`）迁到现代 MC 的 `DeferredRegister<EntityType>` + 属性事件 + 刷怪放置模型。

**PD-1 交付实体注册框架**：`EntityType` 注册器 + 跨加载器属性注册兼容层 + 忠实基类，并以一个 `pilot_mob` 示例实体证明「`/summon` 即成活」（注册路径 + 属性事件 + tick 全链路通，先于任何具体实体存在）。玩家可见效果延后：渲染归 Stage E，具体生物归 D2a/D2b。

## 2. 范围

- **含（PD-1）**：`content/entity/base/**`（AC 生物基类）、`registry/ModEntities`（`ENTITY_TYPE` 注册器 + 属性接线 + 示例实体）、`platform/EntityAttributeCompat`（属性创建事件兼容层）。
- **含（PD-2，Agent 13）**：`content/entity/{ai,pathfinding}/**`（可复用 Goal + canonical 导航；详见 §3.4）。
- **不含**：具体生物族（anti/demon/ghoul/shoggoth/misc/projectile → PD-3..6，D2a）、BOSS（PD-7，D2b）、实体渲染器/模型（Stage E）、战利品表（T3.8）、刷怪蛋 + 群系自然刷怪（T3.9，neoforge 双份延 M10/PL-4）、**刷怪放置注册**（`SpawnPlacements` — PD-1 延后、**PD-5 已交付 `SpawnPlacementCompat`**，见 §5）。

## 3. 设计 / 架构

### 3.1 包结构
```
content/entity/base/ACMob.java        —— AC 敌对生物基类（PD-1）
content/entity/ai/**                  —— 可复用 Goal（PD-2）
content/entity/pathfinding/**         —— canonical 导航（PD-2）
registry/ModEntities.java             —— ENTITY_TYPE 注册器 + 属性接线（PD-1）
platform/EntityAttributeCompat.java   —— 属性创建事件兼容层（PD-1）
```

### 3.2 关键类与职责
- **`ACMob extends Monster`**：忠实承 1.12.2 `common.entity.base.EntityMobBase`（`extends EntityMob`）。具体类（非抽象，同 `EntityMobBase`），供 D2a 各族 subclass。静态 `createAttributes()` 返回属性模板（默认 = vanilla `Monster.createMonsterAttributes()`；子类覆写填忠实血量/伤害/速度/跟随距离）。
- **`ModEntities`**：`ModRegistrar<EntityType<?>>`（`Registries.ENTITY_TYPE`）。注册示例 `pilot_mob`（= `ACMob` 直接注册，同 PC-1 block-less BE smoke test 先例）。`static {}` 块把 `pilot_mob` 的属性 supplier 登记进 `EntityAttributeCompat`（class-init 时机由 `ModRegistries.ALL` 强制先加载，早于主类 `attach`）。
- **`EntityAttributeCompat`**（platform）：收集 `(EntityType, AttributeSupplier.Builder)` → 在 mod-bus `EntityAttributeCreationEvent` 里 `event.put(...)` 发布。`register(type, attrs)` 由各注册器登记、`attach(modBus)` 由主类挂一次。

### 3.3 控制流（属性接线）
`ModRegistries.ALL`（含 `ModEntities.ENTITIES`）在主类 `init` 被遍历 attach → 强制 `ModEntities` class-init → `static{}` 块调 `EntityAttributeCompat.register` 填表；随后主类调 `EntityAttributeCompat.attach(modBus)` 挂监听器；晚些 `EntityAttributeCreationEvent` 触发 → 遍历表 `put` 每个 `build()` 出的 `AttributeSupplier`。**要点**：`LivingEntity` 若无注册属性，`/summon` 当场崩 → 成功 summon 即证属性链通。

### 3.4 PD-2 AI/寻路框架（Agent 13，同子系统，无 CR）
现代 vanilla 已吸收 1.12.2 多数 AI/寻路；PD-2 只交付无 vanilla 等价物：`content/entity/ai/WorshipGoal`（膜拜，`PathfinderMob` + `TagKey<Block>` 目标 + 可选 `SoundEvent`）、`content/entity/ai/{SwellGoal<T extends Mob & SwellingMob>,SwellingMob}`（膨胀，解耦 vanilla `SwellGoal` 的 `Creeper` 约束，供 anti-creeper 非 Creeper 基类）、`content/entity/pathfinding/{ACGroundPathNavigation,ACWallClimberNavigation}`（薄扩展点，1.12.2 `PatchedPathNavigate*` 修的 spinning bug 现已 vanilla-native）。远程→vanilla `RangedBowAttackGoal`、近战 reach→vanilla（现代 width-based）文档化不重造。D2a 具体实体消费本框架。

## 4. 子系统内契约

- **注册名 / ID**：`abyssalcraft:pilot_mob`（示例基类实体；D2a 各族用忠实旧名，如 `abyssalcraft:abyssalzombie`、`abyssalcraft:depthsghoul`…）。
- **i18n key**：`entity.abyssalcraft.<name>`（已加 `entity.abyssalcraft.pilot_mob` = "Pilot Mob"）。
- **属性注册 API**（供 D2a 复用）：`EntityAttributeCompat.register(Supplier<EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier.Builder>)`——在各族注册器 `static{}` 里登记，主类已挂 `attach`。
- **基类**：D2a 各族 `extends ACMob`（或其子基类），覆写 `createAttributes()` 填忠实数值 + 构造器加 Goal（用 PD-2 框架）。

## 5. 跨版本 / 加载器要点

- **触及兼容层**：新增 `platform/EntityAttributeCompat`。`EntityAttributeCreationEvent` 仅 import 分叉——Forge `net.minecraftforge.event.entity.EntityAttributeCreationEvent` ↔ NeoForge `net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent`；两端方法面同（`put(EntityType, AttributeSupplier)`），`//? if forge` 门控 import + `IEventBus` 类型（同 `EventBuses` 先例）。
- **编译核实结论（两节点 `compileJava` BUILD SUCCESSFUL）**：`EntityType.Builder.<T>of(factory, MobCategory).sized(w,h).build(String)` 在 1.20.1 **与** 1.21.1 **同签名**（`build(String)` 未在 1.21.1 改成 `ResourceKey`）；`MobCategory`/`Monster.createMonsterAttributes()`/`AttributeSupplier.Builder` 两端同包。→ 实体注册本体 fork-free，无需 `EntityCompat`。
- **`//?` 分叉点**：仅 `platform/EntityAttributeCompat`（import）+ 主类 `attach` 一行。业务（`ACMob`/`ModEntities`）零分叉。
- **刷怪放置（`SpawnPlacements`）—— PD-5 已交付 `platform/SpawnPlacementCompat`**：Forge `SpawnPlacementRegisterEvent` ↔ NeoForge `RegisterSpawnPlacementsEvent` 事件名 + `SpawnPlacements.Type`(1.20.1)↔`SpawnPlacementTypes.ON_GROUND`(1.21，`SpawnPlacements.Type` 枚举在 1.21 被抽出为 `SpawnPlacementType` 接口 + `SpawnPlacementTypes` 常量) 双分叉，**全封兼容层内**；业务经 `registerGroundMonster(Supplier<EntityType<T extends Monster>>)` 零分叉（内部硬编 ON_GROUND + `MOTION_BLOCKING_NO_LEAVES` + `Monster::checkMonsterSpawnRules`）；主类挂一行 `attach`（同 `EntityAttributeCompat` 先例）。javap 双 jar 核 neo 5-arg `register(EntityType, SpawnPlacementType, Heightmap.Types, SpawnPlacements.SpawnPredicate, Operation)` + `Monster.checkMonsterSpawnRules` 1.21 仍在。两节点 `runServer` 实证（PD-5 §9）；自然刷怪片段（forge biome_modifier）随各族落地、neo 双份 M10/PL-4、跨维度族群系待 G。**注**：`SpawnPlacements.Type` bug 曾瞬态阻断并发 PD-3/PD-4 的 neo 编译，PD-5 修复后两端全绿。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **无属性 = summon 崩**：`Mob`/`LivingEntity` 无注册 `AttributeSupplier` 时 `/summon` 抛错。→ 成功 summon（"Summoned new entity"）本身即证 EntityType 注册 + 属性事件发布 + 构造不崩三合一。
- **敌对 Monster 无玩家即刻消失**：空服务器（仅 console，无玩家）里 hostile mob 无 128 格内玩家会**下一 tick 即 despawn**（非 fall/框架 bug）。验「成活」须加 `{PersistenceRequired:1b}`（禁 despawn）；`{NoGravity:1b}` 免落地摔伤混淆。证据：裸 summon 后 `data get` = "No entity was found"；加 Persistence 后 Health 稳定 20.0f。
- **无渲染器 → `runClient` 启动崩**：现代 Forge/Neo 客户端启动 `EntityRenderers.validateRegistrations` 对无渲染器的 EntityType 抛 `IllegalStateException`。→ Stage E 前实体框架**只能 `runServer` 验**（专用服不碰渲染器）；`runClient` 崩属预期、延 E。
- **延后自 `EntityMobBase` 的两件（非删、随属主任务回补）**：①自定义地面导航（`PatchedPathNavigateGround`）→ PD-2 `content/entity/pathfinding`，子类覆写 `createNavigation` 接入；②硬核穿甲 chip 伤害 + elite/boss 伤害放大 → 依赖尚未移植的 `ACConfig.hardcoreMode`/`damageAmpl`（config 轴）+ `IEliteEntity` API marker。当前 `ACMob` 保持干净 `Monster` 基类，不引用未移植 config。

## 7. 验证 / DoD

- **两节点 `compileJava`**：BUILD SUCCESSFUL（forge 3s / neo 6s）——跨版本属性事件 fork + `EntityType.Builder.build(String)` 编译器核实一致。
- **forge `runServer`（专用服，端口默认 25565 空闲、干净）**：`AbyssalCraft starting up` → `Done (2.363s)`；console `summon abyssalcraft:pilot_mob 0 100 0 {PersistenceRequired:1b,NoGravity:1b}` → `Summoned new entity.abyssalcraft.pilot_mob`；`data get entity @e[type=abyssalcraft:pilot_mob,limit=1] Health` → **`20.0f`**（属性事件已 put MAX_HEALTH）；持续存活 00:22:11→00:23:04 tick 无异常；`stop` 干净存盘 → BUILD SUCCESSFUL。**全程日志零 exception/stacktrace**。
- **DoD「`/summon` 生成、有血量/AI、不崩」达成**：生成 ✓、Health 20.0f ✓、`Monster` tick/AI 系统运行 ✓（具体 Goal 由 D2a 各族 + PD-2 框架加）、无崩 ✓。
- **未机核项**：客户端渲染（Stage E，`runClient` 现会因缺渲染器崩，属预期）；具体实体族的忠实数值/行为（D2a）。

## 8. 已交付家族 · anti 反物质（PD-3）

- **11 实体**：antizombie/antiabyssalzombie/anticreeper/antiskeleton/antispider/antighoul/antiplayer/anticow/antipig/antichicken/antibat（忠实 1.12.2 id + egg 白/白）。
- **设计决策（extend vanilla 而非承 `EntityMobBase`）**：1.12.2 把 vanilla AI 抄进 `EntityMobBase`；现代 8 个 anti **直 extends vanilla**（`Zombie`/`Creeper`/`Skeleton`/`Spider`/`Cow`/`Pig`/`Chicken`/`Bat`）白拿正确行为（child/膨胀/爬墙/弓箭/繁殖/悬挂），只叠 anti 掉落+属性+`AntiEntity` marker——更忠实且免重造 vanilla AI + 免跨版本 AI/DamageSource 分叉。`AntiGhoul`/`AntiPlayer`(无 vanilla 等价) extends PD-1 `ACMob` + 显式 vanilla goals（`Float`/`MeleeAttack`/`WaterAvoidingRandomStroll`/`LookAtPlayer`/`RandomLookAround` + `HurtByTarget`/`NearestAttackableTarget<Player>`）。
- **属性**：各 vanilla `createAttributes()`（`Zombie` 必须——含 `SPAWN_REINFORCEMENTS_CHANCE`，否则 zombie 逻辑 NPE）/`AbstractSkeleton`/`Spider`；`Mob.createMobAttributes()`(农场动物+bat)；`Monster.createMonsterAttributes()`(creeper/ghoul/player)。`.add(Attributes.X, v)` 覆写；`Attributes.X` 字段类型与 `.add` 参数在两版本共变 → fork-free。经 PD-1 `EntityAttributeCompat.register`（`static{}` 登记、复用主类已挂 attach、**无主类改动**）。
- **刷怪蛋**：新 `platform/SpawnEggCompat.create(type,bg,hl)`——forge `net.minecraftforge.common.ForgeSpawnEggItem` ↔ neo `net.neoforged.neoforge.common.DeferredSpawnEggItem` **类名分叉**（FQN 在 forked return，非 import；47.4.4 无 `DeferredSpawnEggItem`）。供 PD-4..7 家族复用。
- **战利品**：9 表 `loot_table(s)/entities/anti*.json` 双目录同容（`set_count` uniform、**避** 1.21 改名的 `looting_enchant`→`enchanted_count_increase` 故两目录同 JSON；默认表 id `<ns>:entities/<id>` 自动派生免 override）。bat/player 无掉落故无表。
- **专属行为已收口**：11 个 anti 通过共享 `AntiEntity` 对应体映射在接触时湮灭；普通模式爆炸强度 5，核爆模式读取 `antimatterExplosionSize`，并遵守现代 `MOB` 爆炸交互。AntiGhoul 在 coralium/dread DamageType 死亡时转为 Omothol Ghoul；Chicken/Cow/Pig 繁殖保持 anti 后代；AntiCow 空桶交互产出液态反物质桶。vanilla 基类继续负责鸡产蛋、Creeper 唱片、Skeleton 弓与通用繁殖 AI。
- **验证**：forge `compileJava`+`runServer`：11 anti 各 `/summon` 成活（全显 lang 名）+ Anti-Ghoul Health `45.0f`/Anti-Creeper `30.0f` + 干净 stop 零异常。**neo**：13+1 文件零错（报错 100% 在并发 PD-5 `SpawnPlacementCompat`）+ `DeferredSpawnEggItem` 一手核在 neoforge-21.1.193.jar → neo-正确、全绿待 PD-5 修其 `SpawnPlacements.Type`→1.21 `SpawnPlacementType` bug。

## 9. 已交付家族 · ghoul + shoggoth（PD-5）

- **8 实体**：ghoul/depths_ghoul/dreaded_ghoul/omothol_ghoul/shadow_ghoul（5 ghoul）+ lesser_shoggoth/shoggoth/greater_shoggoth（3 shoggoth）。刷怪蛋色取自 1.12.2 `registerEntityWithEgg`（ghoul 0xA1A766/0x40460C、depths 0x36A880/0x012626、dreaded 0xE60000/0xCC0000、omothol/shoggoth 全 0x133133/0x342122、shadow_ghoul 0x000000/0xFFFFFF）。
- **设计（承 1.12.2 base 到 abstract）**：`AbstractGhoul`/`AbstractShoggoth`（均 extends PD-1 `ACMob`）承 `EntityGhoulBase`/`EntityShoggothBase` 的属性 + AI（vanilla goal 集：Float/MeleeAttack/MoveTowardsRestriction/WaterAvoidingRandomStroll/RandomLookAround/LookAtPlayer + HurtByTarget/NearestAttackableTarget-Player）；各 concrete 独立 `createAttributes()`。ghoul 基线 FR42/速0.23 + 各血/攻（ghoul 30/5、depths/dreaded/shadow 35/6、omothol 60/15+FR64+KB0.2）；shoggoth 基线 KB0.2/速0.25/FR20 + 各血/攻（lesser 25/6、shoggoth 50/8、greater 100/10）。尺寸烘进 `EntityType.Builder.sized`（omothol 1.3x2.7、shoggoth 0.9x1.3/1.2x1.8/1.8x2.6，余 0.9x1.7）。
- **忠实特性（fork-free）**：omothol on-hit slow(100)/blind(20)/nightvis(20)（`doHurtTarget` + vanilla `MobEffects`，`MobEffectInstance(constant,dur)` 两版共变）+ 火免（`fireImmune`）+ 毒免（`canBeAffected`）；dreaded 火免；shadow 毒免；shoggoth 爬墙（`createNavigation`→PD-2 `ACWallClimberNavigation` + `onClimbable`=`horizontalCollision`）+ 不被流体推（`isPushedByFluid`=false）+ 水中滑行（`aiStep` 加 lookAngle*0.005）。
- **刷怪放置**：新 `platform/SpawnPlacementCompat`（见 §5）——8 实体经 `registerGroundMonster` 登记 ON_GROUND + `Monster::checkMonsterSpawnRules`（地面 + 黑暗）。
- **战利品**：16 表 `loot_table(s)/entities/*.json` 双目录同容（各掉对应 flesh `set_count` 0-2、避 `looting_enchant`、默认表 id 自动派生）。**跨主 gap-fill**：PB-2 `MiscItems` +5 shoggoth flesh plain item（1.12.2 非食用 `ItemACBasic`、PB-2 漏移；ghoul flesh 已在 PB-2）。
- **自然刷怪（forge biome_modifier）**：`spawn_ghoul` ghoul→[plains,desert,taiga,savanna]（1.12.2 EntityHandler `EntityGhoul` addSpawn 忠实）；`spawn_shoggoth` lesser_shoggoth→`#minecraft:is_overworld` **占位**（shoggoth 忠实群系 = AC 维度 abyssal_wastelands/dreadlands/omothol/dark_realm，待 G 建群系后重定向）。neo 忽略 `forge/biome_modifier/` 目录 → neo 自然刷怪双份延 PL-4；depths 水生 + dreaded/omothol/shadow ghoul 维度刷怪待 G。
- **延后**：水下呼吸（`canBreatheUnderwater` 1.21 为 `final` 标签驱动、1.20.1 可覆写 → 需版本分叉兼容层）、coralium/dread plague on-hit（药水 `MobEffect` 未移植）、shoggoth 招牌 AI（酸击 `EntityAIShoggothAttackMelee`/喷酸 `EntityAcidProjectile`/建巨石 `EntityAIShoggothBuildMonolith`+`WorldGenShoggothMonolith`→G/ooze 块/进食变体/`IEntityMultiPart`/`EntityAIWorship`——依赖未移植；PD-2 框架预留待依赖落地）、5 shoggoth TYPE 变体、depths 彩蛋名/baby、自定义音效（`ACSounds`）/shadow 粒子、硬核 chip/精英浮动（config）、depths `dghead` 稀有掉落（物品未移植）、渲染 E。
- **验证**：两节点 `compileJava --rerun-tasks`+`runServer`：8 实体各 `/summon` 成活（无属性即崩→成活即证属性链）+ ghoul `attribute ... max_health base get`=30.0（值精确）+ `/loot spawn abyssalcraft:entities/{ghoul,greater_shoggoth,shoggoth}` 双端掉 Ghoul Flesh/Shoggoth Flesh（loot 运行期解析 + 1.20 复数/1.21 单数目录路径两端通）+ 干净 stop（forge Done 3.280s / neo 1.028s）。

## 10. 已交付家族 · misc + projectile（PD-6）

- **15 EntityType（非生物）**：5 projectile（acidprojectile/dreadslug/inkprojectile/coraliumarrow/dreadedcharge）+ 10 misc（blackhole/implosion/primedodb/primedodbcore/compasstentacle/powerstonetracker/portal/singleportal/spirititem/gatekeeperessence，忠实 1.12.2 id）。非 `LivingEntity` → 无属性 / 无战利品 / 无刷怪蛋。
- **2 platform fork-base（吸收 `Entity.defineSynchedData` 分叉）**：`Entity.defineSynchedData` 是 abstract 且跨加载器分叉（forge `()` ↔ neo 1.21 `(SynchedEntityData.Builder)`）→ 任何 raw `extends Entity`/`extends ThrowableProjectile` 的 concrete 子类都必须实现它。故建 `platform/ACSimpleEntity extends Entity`（misc 用，空 synched + 空 `read/addAdditionalSaveData`）+ `platform/ACThrowableProjectile extends ThrowableProjectile`（3 投掷物用），各含唯一 `//? if forge` defineSynchedData 空实现，业务子类零分叉。`AbstractHurtingProjectile`/`Arrow`/`ItemEntity` 已自实现 defineSynchedData → 直 extends 无需 base。
- **5 projectile 设计**：`AcidProjectile`/`DreadSlug`/`InkProjectile` extends `ACThrowableProjectile`（onHitEntity→`damageSources().thrown` 6/6/2，base `ThrowableProjectile.onHit` 自动 server-discard）；`CoraliumArrow` extends 具体 `Arrow`（避 `AbstractArrow` 的 abstract pickup 方法 1.20↔1.21 分叉，白拿飞行/碰撞/2 伤害/拾取）；`DreadedCharge` extends `AbstractHurtingProjectile`（`shouldBurn`=false、`getTrailParticle`=FLAME、onHitEntity hit 4）。
- **10 misc 设计（8 类，DRY 塌缩）**：`BlackHole`(48 格拉拽 living 300t)、`Implosion`(16 格拉拽 60t)、`PrimedODB`(boolean core 塌缩 primedodb+primedodbcore：fuse 200/40→`level.explode(this,x,y,z,4/6,MOB)`→discard、fuse 存 NBT)、`CompassTentacle`(**忠实**：首 tick 看向固定点 (4,54,85) + 120t `sendParticles(EXPLOSION)` discard)、`PSDLTracker`(despawn 占位)、`DimensionPortal`(boolean singleUse 塌缩 portal+singleportal 占位)、`SpiritItem`/`GatekeeperEssence`(extends `ItemEntity` 占位)。2 registrar `ProjectileEntities`(5)+`MiscEntities`(10)，`MobCategory.MISC`。
- **业务零 `//?`**：javap 双 jar 核 `ThrowableProjectile`/`Arrow`/`AbstractHurtingProjectile`/`ItemEntity` 的 `(EntityType,Level)` ctor + `Projectile.onHit/onHitEntity/onHitBlock` + `DamageSources.thrown(Entity,Entity)` + `Level.explode(Entity,ddd,float,ExplosionInteraction)`(MOB 常量) 全双端同签名；分叉仅 `defineSynchedData`（封 2 base）。
- **延后（依赖未移植）**：自定义伤害源 acid/dread（用 vanilla thrown 占位）、potion coralium_plague/dread_plague/blindness/slowness（`MobEffectInstance` holder 构造 1.20↔1.21 分叉）、酸蚀方块（ACConfig/ACBlocks）、dreaded_charge 群系→dreadlands + `AreaEffectCloud`（ACBiomes/BiomeUtil）、hardcore 穿甲（ACConfig）、Jzahar 关联（BlackHole/Implosion→PD-7）、`ExplosionUtil` 自定义爆炸 + Sacthoth 生成、维度传送（Portal/SinglePortal→维度系统 DimensionData/Registry/Teleporter）、spirit 路径（SpiritItem→SpiritItemUtil）、essence 物品（GatekeeperEssence→ACItems）、PSDL float-to-target + 掉落、发射运动/客户端 tracking 调优、渲染 E。
- **验证**：两节点 `compileJava` + `runServer`：15 各 `/summon`→`Summoned new X`（13 具名 + spirititem/gatekeeperessence 显 "Air" = 空 `ItemEntity` 预期）、零异常；`execute unless entity @e[type=…primedodbcore] run say` 双端命中（fuse→`explode`→discard 实证）+ blackhole 存活期 `execute if entity` 命中（tick 实证）+ 干净 stop（forge Done 3.441s / neo 0.900s）。

## 11. 已交付家族 · BOSS（PD-7，Stage D2b）

- **12 EntityType**：4 血条 boss（chagaroth / jzahar / shadowboss=Sacthoth / dragonboss）+ 8 elite/minion（dreadguard / gskeleton=Skeleton Goliath / remnant / shuboffspring / jzaharminion=Gatekeeper Minion / chagarothfist / chagarothspawn / dragonminion，忠实 1.12.2 id）。12 忠实刷怪蛋（色取自 1.12.2 `registerEntityWithEgg`）。Asorah / Spectral Dragon 无独立 code（flavor 名，不建）；RemnantTrader 1.12.2 已注释禁用（跳过）。
- **新 boss-bar 框架 `content/entity/boss/ACBossMob extends ACMob`**：持 final `ServerBossEvent`（ctor 传 `BossEvent.BossBarColor` + `setDarkenScreen(true)`）；`customServerAiStep`→`setProgress(hp/maxHp)` + 按血量三段变色（blue &gt; 66% / green &gt; 33% / red，作 1.12.2 per-boss 阶段色的轻量替身）；`startSeenByPlayer`/`stopSeenByPlayer`→`addPlayer`/`removePlayer`（vanilla `WitherBoss`/`EnderDragon` 惯用法）。javap 双 jar 核 `ServerBossEvent`(ctor + setProgress/addPlayer/removePlayer/setVisible/setDarkenScreen) / `Entity.start·stopSeenByPlayer(ServerPlayer)` / `Mob.customServerAiStep()` / `BossEvent.BossBarColor.{BLUE,GREEN,RED}` 全双端同签名 → 血条框架 fork-free。
- **enum 塌缩（同 PD-4，全 skill 延后→当前纯数据）**：`BossKind`(4)+`BossMob extends ACBossMob`（super 传 kind.color()）、`EliteKind`(8)+`EliteMob extends ACMob`——kind 烘进 EntityType 工厂 lambda。忠实属性（非 hardcore）：Chagaroth 1000/15/spd0/kbr1、Jzahar 500/30/FR80/armor10、Sacthoth 300/15/FR160/kbr0.4、DragonBoss 400/atk10 占位/kbr1、Dreadguard 60/10/armor20、SkeletonGoliath 60/10/kbr0.3、Remnant 50/10/FR64、ShubOffspring 40/4、GatekeeperMinion 100/18/FR64、ChagarothFist 40/7.5、ChagarothSpawn 30/8/spd0.45、DragonMinion 30。fire-immune 按 1.12.2；标准敌对 goal（Float/MeleeAttack/WaterAvoidingRandomStroll/LookAt/RandomLookAround + HurtByTarget/NearestAttackableTarget&lt;Player&gt;）。
- **无自然刷怪**：boss 由仪式/结构召唤（非自然 spawn）→ 不注册 `SpawnPlacementCompat`。boss 战利品依赖未移植物品 → 延后（无 loot 表）。
- **延后**：各 boss 招牌多阶段技能（Chagaroth 召唤 fist/spawn + barf、Jzahar earthquake/blackhole/implosion/shout、Sacthoth shadowflame、dragon 飞行 + `MultiPartEntityPart` 多部件 + 治疗圈、Remnant `IMerchant` 交易 + `IShearable` 剪羊毛、minion 由 boss 召唤）、dread_plague on-hit（药水）、`IElite/IDread/IOmotholEntity` marker、硬核属性（config）、Sacthoth/ChagarothSpawn 爬墙（`EntityClimbingMobBase`→可选 `ACWallClimberNavigation`）、dragon 24×12/8×3 真尺寸（占位缩小 7×5/4×2）、血条视觉显示（需客户端渲染器 Stage E）、渲染 E。
- **验证**：两节点 `compileJava` + `runServer`：12 boss 各 `/summon`→`Summoned new X`（全显 lang 名）+ 属性精确（Chagaroth `1000.0f` 双端 / Jzahar 500 (477 落地伤后) / Dreadguard 60 (36)）+ 4 血条 boss `customServerAiStep` 逐 tick setProgress/变色 **零 exception**（~3min）+ 干净 stop（forge Done 3.940s / neo 1.346s）。血条视觉需客户端（Stage E `runClient` 缺渲染器崩、属预期）。

## 11A. RR-ENTITY-CATALOG · 九漏实体与生态（Agent C）

- **目录**：新增 `content/entity/legacy/**`，注册 `abyssalzombie`、`coraliumsquid`、`dreadling`、`dreadspawn`、`greaterdreadspawn`、`lesserdreadbeast`、`shadowcreature`、`shadowmonster`、`shadowbeast`。`EntityCatalogInvariant` 在专服启动时按精确 ID 集合验证 `content=63 / all_ac=64 / eggs=48 / placements=44`，不是只比数量。
- **属性与分类**：尺寸、血量、攻击、速度、跟随距离、护甲和击退抗性按 1.12.2 普通模式迁移。1.20.1 继续覆写 `MobType`/`canBreatheUnderwater`；1.21 的 final 行为改由 `data/minecraft/tags/entity_type/{undead,arthropod,can_breathe_under_water}.json` 驱动。Lesser Dreadbeast 保留节肢分类，Abyssal Zombie/Dreadling 保留不死分类。
- **服务端行为**：`AbyssalZombie` 复用现代 Zombie 的幼体/日晒/NBT，命中施 Coralium Plague，并绕过 vanilla 村民→Zombie Villager 转化，按旧 Normal/Hard 规则把玩家/村民/僵尸转为 Abyssal Zombie。`CoraliumSquid` 近距锁定玩家、接触感染并发射 `InkProjectile`。`LegacyHostileMob` 按 kind 实现 Dread plague、wall-climber 导航、5→1 聚合、死亡分裂、周期生成、远近战切换、`DreadSlug`，以及 Shadow 毒免、Shadow Beast 双手攻击和 breath ray。`GameHooksCompat` 补 Dark Realm/Shadow 击杀转化。
- **构造期坑**：`Mob` 构造期间会调用 `createNavigation`，此时 factory lambda 传入的 `kind` 尚未赋值；仅覆写 `createNavigation` 会让 Dread 永久拿到普通导航。构造器在 `kind` 赋值后显式替换 `navigation = new ACWallClimberNavigation(...)`。
- **资产与数据**：新增 17 个蛋后旧版 48 蛋齐全，datagen 生成 48 模型。`LegacyEntityLootData` 同时生成 1.20.1 `loot_tables/` 与 1.21.1 `loot_table/` 的九表；Abyssal Zombie 稀有池不用不可达 `alternatives`。双加载器各有 9 个镜像 spawn modifier；`SpawnCandidateCompat` 用 `PotentialSpawns` 表达 AW/DL `Y<=5` Shadow 表和 Dreadlands Darklands 表。
- **验证**：双节点 `runData` 均输出 `RR_ENTITY_CATALOG_OK` 与 `R1_CONTENT_SELF_TEST_OK`，资源计数为 `spawnPairs=9/9 eggModels=48 lootSingle=9 lootPlural=9`；双节点完整 `build` 成功。Forge/Neo 全新专服均 `Done`、热重载零实体 loot/spawn/tag 解析错、九实体各 `/summon` 成功。两端五个主世界 Darklands 均可定位，AW 查不到 Darklands，Dreadlands 可定位基础 Darklands。Forge 实测 Dread Spawn `5→1` 与 Greater Dread Spawn `1→2`；两端 Hard 村民转化均命中 `RR_ABYSSAL_CONVERSION_OK` 且无 Zombie Villager 残留。
- **未完成**：自然刷怪尚未做足量统计，`PotentialSpawns` 只证明事件注册和专服无异常；Squid 墨弹、Shadow breath/死亡转化、周期生成与 plague 仍需双端黑盒矩阵。Abyssal Zombie 着火传递/装备拾取规则、Shadow 粒子/节日装备等边角细节尚未迁。九实体仍进入自动占位 renderer，忠实模型/贴图由 T4.2b/T4.3c 承接；旧 69 loot 全量仍由 T3.8b 承接。

## 12. 渲染框架（PE-1，Stage E1）

- **范围**：E1 = 渲染**注册管线** framework（**非**各族 faithful 模型/渲染器/图层——那是 E2 = PE-2..6）。初始目标是给 54 实体注册渲染器使 `runClient` 过 `EntityRenderers.validateRegistrations`；注册器按 namespace 自动遍历，RR-ENTITY-CATALOG 新增九实体后也会自动获得占位 renderer，但不代表 T4.2b/T4.3c 忠实渲染完成。
- **文件**：新 `platform/EntityRendererCompat`（**client-only**；`EntityRenderersEvent`（`RegisterRenderers`/`RegisterLayerDefinitions`）forge↔neo **仅 import 分叉**——neo 保留 forge 的 `registerEntityRenderer`/`registerLayerDefinition` 方法名故 body 中性；暴露 fork-free 中性 `Renderers`/`Layers` sink）+ `registry/ModModelLayers`（client-only，`PLACEHOLDER` `ModelLayerLocation` + 单 cube `LayerDefinition`）+ `client/render/entity/ACPlaceholderRenderer extends EntityRenderer<Entity>`（bake PLACEHOLDER + 小 cube + vanilla creeper 贴图占位）+ `client/render/ACEntityRenderers`（**遍历冻结 `BuiltInRegistries.ENTITY_TYPE` 按 namespace** 给每个 `abyssalcraft:` 实体注册占位——自动覆盖全族+boss+未来，免引用各族 registrar）。
- **接线**：主类 `SideExecutor.runWhenClient` 块 +1 `EntityRendererCompat.attach(modBus, ACEntityRenderers::registerRenderers, ACEntityRenderers::registerLayers)`（与 `ClientScreenCompat.attach` 并列；client-only 不上服务端）。无 relay/lang 改动。
- **跨版本 fork-free 要点**：分叉仅 `EntityRendererCompat` 的事件 import。`ACPlaceholderRenderer` 只用两版稳定 API：4 参 `ModelPart.render(pose,vc,light,overlay)`、`RenderType.entityCutoutNoCull`、`OverlayTexture.NO_OVERLAY`、`EntityRenderer.render`+`getTextureLocation`、`Context.bakeLayer`；`LayerDefinition.create`/`MeshDefinition`/`CubeListBuilder`/`PartPose`/`ModelLayerLocation` 稳定。**坑**：`PartPose` 在 `net.minecraft.client.model.geom` 非 `.builders`（编译报错才现）。
- **E2 复用**：PE-2..6 编辑 `client/render/ACEntityRenderers` 用 `renderers.register(TYPE, YourRenderer::new)` 覆盖占位、往 `ModModelLayers` 加真图层 + `layers.register(LOC, def)`。中性 `Renderers`/`Layers` sink 不碰 loader 事件。
- **验证**：两节点 `compileJava` BUILD SUCCESSFUL + 两节点 `runClient`：**0 missing-renderer / 0 validateRegistrations / 0 crash**，各抵标题屏（ResourceManager reload 过渲染器验证 → OpenAL/Sound engine → texture atlases → Realms/title）。占位 = 小 cube；faithful 视觉/尺寸/贴图/朝向/动画/图层 → E2 + 人工目视。

### 12.1 anti + demon 渲染（PE-2）

- **19 实体**：anti(11) + demon(8)。忠实 1.12.2 `RenderAnti*`/`RenderDemon*`：**anti/demon = vanilla 模型 + AC 贴图**（`textures/model/anti/*.png`、`demon_*.png`）；**evil = vanilla 模型 + vanilla 贴图**（evil 动物长得像正常动物，忠实 `RenderEvilCow` 用 `textures/entity/cow/cow.png`）。
- **文件**：新 `client/render/entity/ACTexturedRenderer<T extends Mob> extends MobRenderer<T,EntityModel<T>>`（vanilla 模型 + 固定贴图，`getTextureLocation` 返回固定）+ `client/render/entity/AntiDemonRenderers`（注册 19：`reg(type,shadow,tex,modelFn)`，raw 注册中性 sink）。迁 18 贴图从 1.12.2 `textures/model/`。
- **模型映射**：bipeds(zombie/abyssalzombie/skeleton/player/ghoul)→`HumanoidModel` + `ModelLayers.{ZOMBIE,SKELETON,PLAYER}`；creeper→`CreeperModel`；spider→`SpiderModel`；cow/pig/chicken/sheep→各 vanilla 动物模型；bat→`BatModel`。全 `ctx.bakeLayer(ModelLayers.X)` 复用 vanilla 层（**无新 `ModModelLayers`**）。
- **接线**：`ACEntityRenderers.registerRenderers` 加 `handled` Set → 先调 `AntiDemonRenderers.register(renderers, handled)`（真渲染器）、占位 loop 跳过 handled（**避 `EntityRenderers` 重复注册崩**）。这是 E2 编辑 E1 relay 的既定范式（PE-3/4 按同法加 register 调用）。
- **跨版本坑**：`BatModel` 非泛型（`new BatModel(part)` 不带 `<>`，与 `CowModel<T>` 等不同）；其余 rendering API（`MobRenderer`/`EntityModel`/`HumanoidModel`/`getTextureLocation`/`bakeLayer`/`ModelLayers`）两端稳定。业务零 `//?`。
- **延后**：自定义 `ModelGhoul`（antighoul 现 biped 近似）、sheep wool 层（`demon_sheep_fur`）、antispider 发光眼层（`anti/spider_eyes`）、armor 层（→PE-5）、creeper charge 层、5 TYPE 变体、**精确视觉对位（人工目视 + 进世界 summon）**。
- **验证**：两节点 `compileJava` BUILD SUCCESSFUL + 两节点 `runClient`：各抵标题屏、**0 missing-renderer / 0 missing-texture(anti/demon) / 0 duplicate-registration / 0 crash**。

### 12.2 ghoul + shoggoth 渲染（PE-3）

- **8 实体**：ghoul(5: ghoul/depths/dreaded/omothol/shadow) + shoggoth(3: lesser/shoggoth/greater)。**异于 §12.1**：ghoul/shoggoth 贴图 UV 绑其 1.12.2 专属网格（`ModelGhoul` 128x64、`ModelLesserShoggoth` 128x128），vanilla biped/slime 会 UV 错位 → 必须交付专属模型。
- **文件**：新 `client/model/entity/{GhoulModel,ShoggothModel} extends HierarchicalModel<T>`（**简化-忠实**：主体块用真 1.12.2 texOffs/坐标——ghoul 头/颚/脊柱/颈/肩/双段臂/双段趾行腿 ~13 块，shoggoth 三段体堆叠 bodyBase→Mid→Upper + 主嘴上下 + 双 hind 触手；细节 cube [ghoul 牙/指/肋、shoggoth 全 100 部件触手/多嘴/眼阵] 略去以保盲移植可验证）+ `client/render/entity/{GhoulRenderer,ShoggothRenderer} extends MobRenderer<T,M>`（每型固定贴图 + 眼层）+ `GhoulShoggothRenderers`（注册 8 + `registerLayers` 注册 2 层）+ `layers/SimpleEyesLayer<T,M> extends EyesLayer`。迁 1.12.2 `textures/model/{ghoul,shoggoth}/` 22 png。
- **发光眼（T4.4）**：`SimpleEyesLayer extends vanilla EyesLayer` 只覆写 `renderType()`=`RenderType.eyes(tex)`——**关键**：`EntityModel.renderToBuffer` 在 1.20.1(rgba float)↔1.21(packed int) **分叉**；模型 extends `HierarchicalModel`（继承不覆写 renderToBuffer）+ 眼层用 vanilla `EyesLayer`（其 render 内部调 renderToBuffer=vanilla 处理）→ 全避开 fork、业务零 `//?`。omothol_ghoul 无 eyes 贴图（跳）。
- **接线**：`ACEntityRenderers.registerRenderers` 加 `GhoulShoggothRenderers.register(renderers, handled)`（接 PE-2 后、占位跳过 handled）；`registerLayers` 加 `GhoulShoggothRenderers.registerLayers` 注册 `ModModelLayers.{GHOUL,SHOGGOTH}`（**首个自定义模型层**，异于 PE-2 全复用 vanilla 层）。无 main/lang 改。
- **跨版本 fork-free**：javap 双 jar 核 `MobRenderer(Context,M,float)`/`EyesLayer(RenderLayerParent)`+`renderType()`/`RenderType.eyes`/`HierarchicalModel` 同签名；唯一 client fork（`renderToBuffer`）经 `HierarchicalModel` + vanilla `EyesLayer` 规避。**踩坑**：`Move-Item` 移动源文件后 gradle 增量编译未识别新路径 → `--rerun-tasks` 清编译解决。
- **延后**：细节 cube、armor/held-item/custom-head 层（armor→PE-5）、ghoul TYPE 彩蛋贴图（depths pete/wilson/orange）、shadow-shoggoth type&gt;=4 半透明、复杂 idle/attack 动画、**精确视觉（简化块/位姿/贴图对位）= 人工目视**——headless `runClient` 只验注册/图层烘焙/加载不崩、不能目视模型（同项目 placeholder + 人工目视 惯例）。
- **验证**：两节点 `compileJava` BUILD SUCCESSFUL + 两节点 `runClient`：各抵标题屏、**0 missing-renderer / 0 layer-bake / 0 validateRegistrations / 0 crash**（GHOUL/SHOGGOTH `LayerDefinition` 烘焙 OK、8 faithful 渲染器覆盖 E1 占位）。

### 12.3 标准护甲穿戴层（PE-5）

- **范围**：7 材料 × 4 件 = 28 护甲（PB-7 `content/item/armor/ArmorItems` 注册）**穿戴时**的图层渲染。标准护甲穿戴显示由 **vanilla `HumanoidArmorLayer`** 负责——原版 humanoid 实体渲染器（player/zombie/skeleton…）自带该层，穿戴 `ArmorItem` 时按材质自动解析并渲染护甲贴图 → **标准护甲零自定义渲染码**（`client/render/armor/**` 现为空）。**非** E2 各族那种自定义 `Renderer`/`Layer`。
- **贴图路径解析**（两端殊途同归到 `abyssalcraft:textures/models/armor/<name>_layer_{1,2}.png`）：
  - **1.20.1**：材质匿名类 `getName()` 返 namespaced `AbyssalCraft.MODID + ":" + name` = `abyssalcraft:<name>`；Forge patch 的 `HumanoidArmorLayer.getArmorLocation` 按 `:` 拆 domain/path → `abyssalcraft:textures/models/armor/<name>_layer_N.png`。
  - **1.21**：`ArmorMaterial` record 的 `layers` 参传单元素 `List.of(new ArmorMaterial.Layer(ACRef.id(name)))`；`Layer` 内部即以该 id 解析为 `namespace:textures/models/armor/path_layer_N.png`。
- **接线**：全在 `platform/ArmorCompat.piece()`（PB-7 冻结的 compat 文件、其 note 已预告「穿戴层延 E 渲染」的 E5 集成）。1.21 分支 `layers` 由 `List.of()`（空）改为单元素 `Layer` 列表；1.20.1 分支 `getName()` 本已 namespaced 无需改。同步类 Javadoc（原述「空层/无穿戴贴图」→ 述两端解析路径）。**无 main/lang/relay/`ModModelLayers` 改、无新客户端文件**。
- **贴图迁移**：14 png（7 材料 × 2 层）从 1.12.2 `assets/abyssalcraft/textures/armor/` 迁到 `textures/models/armor/`，重命名对齐材质名：`{abyssalnite,coralium→refined_coralium,coraliump→plated_coralium,depths(用 outer),dreadium,dreadiums→dreadium_samurai,ethaxium}_{1,2}` → `<material>_layer_{1,2}`。
- **延后（诚实）**：
  - **自定义护甲模型**：武士甲 `dreadium_samurai` 1.12.2 有专属 3D 外形（非标准 biped 甲）→ 现代需 `IClientItemExtensions#getHumanoidArmorModel` 返自定义 `HumanoidModel`；depths 甲 1.12.2 有 inner/outer 双层 + 发光。
  - **怪物护甲层**：ghoul/skeleton_goliath 的 `LayerGhoulArmor`（怪物身上渲染护甲，贴图 `textures/armor/{ghoul,skeleton_goliath}/`）——需协调 PE-3(ghoul 渲染已成)/PE-4(goliath 渲染未成)，贴图未迁。
  - **穿戴视觉 = 人工目视**：护甲贴图**按需在穿戴渲染时**加载（非启动期 atlas），headless `runClient` 标题屏无穿戴者 → 不触发、机器不可验；需进世界 `/give` + 穿戴目视（同项目渲染视觉惯例）。
- **验证**：两节点 `compileJava` BUILD SUCCESSFUL（护甲材质带层构造 + Javadoc）+ neo `runClient` 抵标题屏 **0 crash / 0 armor-tex-missing**（材质带层构造正常、客户端加载不崩）；14 贴图落盘于两端均解析的路径。

### 12.4 BOSS + misc + projectile 渲染（PE-4）

- **范围**：27 实体 = 12 boss（4 血条 boss + 8 elite/minion）+ 10 misc + 5 projectile。分三档：**忠实 GeckoLib**（Chagaroth）、**vanilla 复用**（CoraliumArrow / Spirit / Essence）、**sized stand-in**（其余 11 boss + custom-misc + 小 projectile）。
- **文件**（PE-4 own）：新 `client/render/entity/ACStandInRenderer` + `MiscRenderers` + `ProjectileRenderers` + `client/render/entity/projectile/CoraliumArrowRenderer`，扩 `client/render/entity/BossRenderers`（Chagaroth 保持 `ChagarothGeoRenderer` GeckoLib）；`registry/ModModelLayers`(※) 加 `STANDIN` 层 + `standin()`。
- **设计**：
  - **`ACStandInRenderer<T extends Entity>`**（比 E1 占位方块升级 = 正确尺寸 + 忠实贴图）：bake 16px cube（`STANDIN` 层），`render` 里 `scale(-1,-1,1)` 再 `scale(0.0625*w, 0.0625*h, 0.0625*w)`（w=`getBbWidth()`、h=`getBbHeight()`）→ 箱体精确填满实体包围盒；`entityCutoutNoCull(texture)` 贴忠实贴图（箱体 UV 近似、非精确网格）。**fork-free：仅版本稳定 4-arg `ModelPart.render`**，刻意不手写 `VertexConsumer`（其 `vertex/endVertex`↔`addVertex` 1.20↔1.21 分叉）→ 也不做 billboard（billboard 需手写顶点或会引 fork）。
  - **CoraliumArrow** → `CoraliumArrowRenderer extends ArrowRenderer<CoraliumArrow>` + `corarrow.png`（忠实 `RenderCoraliumArrow`）。**Spirit/Gatekeeper Essence**（extends `ItemEntity`）→ vanilla `ItemEntityRenderer`（忠实 `RenderEntityItem`）。
  - **id→贴图**：boss `jzahar`/`shadowboss`=sacthoth/`dragonboss`（`textures/model/boss/*`）；elite `dreadguard`/`gskeleton`=skeletongoliath/`jzaharminion`=gatekeeperminion/`dragonminion`（`elite/*`）+ `remnant`（`remnant/*`）+ `shuboffspring`=shub_offspring/`chagarothfist`/`chagarothspawn`=spawn_of_chagaroth（root）；misc BlackHole/Implosion/tracker=black_hole、CompassTentacle=compass_tentacle、portals=omothol_portal、ODB×2=vanilla `tnt_side`；proj acid=coralium_fireball、dreadslug/dreadedcharge=dreaded_fireball、ink=vanilla `ink_sac`。迁 17 忠实 AC 贴图（`j'zahar.png`→`jzahar.png` 去撇号）。
- **接线**：`ACEntityRenderers.registerRenderers` 加 `MiscRenderers.register`+`ProjectileRenderers.register`（handled-set，接 PE-3 后、占位跳过 handled）；`registerLayers` 加 `STANDIN` 层。provider 均先赋具型局部（`EntityRendererProvider<Entity>`）再传，避通配符捕获编译失败。业务零 `//?`。
- **PE-4b 已交付（2026-07-22，CR-42 + 视觉修正）**：**12 boss 全 GeckoLib 忠实网格**。初版 9 网格（chagaroth + jzahar + shadowboss + 6 elite，经 `Boss/EliteGeoModel` 按 `getType()` id 解析）+ `EliteMob` 加 `GeoEntity`（`.core` `//?` 例外，同 BossMob）。**用户目视**：8 转换网格全对，仅 3 非-GeckoLib 的坏 → 修正：**dreadguard** 手写 `dreadguard.geo.json`（标准 biped、64×32 UV、左肢 mirror；原 vanilla `HumanoidModel`+64×32 皮肤在 64×64 模型上花屏）；**dragonboss/dragonminion** 扩转换脚本支持 EnderDragon 式（`setTextureOffset`+具名 addBox+float 变量表达式）→ 转出 geo 接 `Boss/EliteGeoModel`（原 stand-in 箱体花屏）。两节点 `compileJava` 绿 + forge `runClient` 全 **12 geo** 零 `GeckoLibException`、抵并保持标题屏。详见 [geckolib-model-porting.md](geckolib-model-porting.md) §6/§7。
- **PE-4b 续 · 2 dragon 改 Java 模型（2026-07-22，CR-49）**：用户目视——dragon 的静态 geo **渲成扁平/塌陷**（1.12.2 dragon 的 neck/tail 是 `render()` 内程序化样条、无法烘进静态网格）→ **弃 GeckoLib、改忠实 Java 模型**：新 `client/model/entity/DragonModel`（`extends HierarchicalModel<Mob>`，盒子/UV 承 1.12.2 = vanilla `EnderDragonModel`，**手编固定悬停姿势**）+ `client/render/entity/boss/DragonRenderer`（`extends MobRenderer<Mob,DragonModel>`，**fork-free**——`renderToBuffer` 分叉交 vanilla，承 PE-3 GhoulModel 先例）；`ModModelLayers` +DRAGON 层、`ACEntityRenderers` 注册 `DragonModel::createBodyLayer`、`BossRenderers` 新 `dragon()` helper 注册两龙并从 GeckoLib 列表摘除；删两 geo + 从 `Boss/EliteGeoModel` TEXTURES 摘除。**GeckoLib 侧余 10 boss 网格不变**。两节点 `compileJava` 绿 + forge `runClient` 抵标题屏 0 crash（DRAGON 层烘焙在标题屏前成功）。**姿势/比例为手编近似，需用户 `/summon` 截图逐轮调**。
- **PE-4b 续 · dragon idle 动画（2026-07-22）**：用户确认静态模型「基本没问题」→ 加 `DragonModel.setupAnim` **程序化 idle 动画**。1.12.2 的 neck/tail 靠 `getMovementOffsets`（末影龙运动历史缓冲）定位、自定义 `Mob` 无此 → 改 `ageInTicks` 驱动正弦摆。每帧 `root.getAllParts().forEach(ModelPart::resetPose)`（两端 vanilla API）复位烘焙姿势后**加性**叠加：翅膀拍打（`wing.zRot+=cos(wave)*0.18`、翼尖相位滞后）+ 下颚开合（忠实 vanilla `jaw`）+ 脖/尾蛇形摆（越近尾尖越大）+ 四腿微动 + 头部 yaw/pitch 追踪（分摊 neck/neck2/head）。`ANIM_SPEED=0.2`≈1.5s/周期可调；构造器 `getChild` 取部件引用（名字核对 `createBodyLayer`）。两节点 `compileJava` 绿 + forge `runClient` 干净启动无 `NoSuchElement`/烘焙错。**动画观感待人工目视**（速度/幅度按反馈可调）。详见 [geckolib-model-porting.md](geckolib-model-porting.md) 修订日志。
- **延后（PE-4b 续 + 人工目视）**：dragon 姿势/比例逐轮调姿、动画、billboard sprite（projectile）、severed-head、**精确视觉全靠人工目视**。
- **验证**：两节点 `compileJava` BUILD SUCCESSFUL（曾瞬态被并发 PS-1 `net/ACNetwork.java` WIP 阻断 forge，报错 100% 在 ACNetwork、我文件零错，PS-1 修复后全绿）；**forge `runClient` 抵标题屏 0 crash / 0 validateRegistrations / 0 missing-renderer**（STANDIN 层烘焙 + 27 渲染器注册 OK）；**neoforge `runClient` 渲染全初始化后原生退出 -1**（全 texture atlas 烘焙 + GeckoLib 加载 + 渲染器注册无 validateReg 崩 + 0 Java 异常 / 0 hs_err / 0 crash-report）——**未定位**：可能环境 LWJGL 3.3.3/GL/本机虚拟显示适配器、或并发 S-A 变更、或本任务（但渲染码已成功执行、forge 同码抵标题屏；PE-5/CR-35 曾记 neo 抵标题屏 → 疑其后引入）→ 待人工在本机 neo 客户端确认/隔离。

### 12.5 BlockEntity 渲染框架（PE-6）

- **注册管线**：`EntityRendererCompat.Renderers#registerBlockEntity` 保持 loader-neutral；`ACBlockEntityRenderers` 是唯一 relay。
- **现有宿主**：`ResearchTableRenderer` 按 FACING 渲染旧版 feather；`RitualPedestalRenderer` 渲染真实 `getOffering()` 浮空物品。两者均已注册，双端 layer/resource 初始化通过。
- **缺宿主拆分**：四断头、Jzahar spawner、sealing lock、rending/sacrificial/energy/ODB 等尚无对应现代 BlockEntityType，独立为 T4.6d；不得为完成数字虚构宿主。
- **剩余验证**：放置四朝向 Research Table、给 Pedestal 设置 offering，在 Forge/Neo 实际目视位置、旋转、光照与浮动。

### 12.6 RR-RENDER 自动实现收口（2026-07-24，CR-65）

- **实体/effect**：九个 legacy 实体全部进入专用 Java/vanilla renderer；12 个 stand-in 改为 billboard、fixed item、fuse-aware ODB、Implosion 与 Compass Tentacle，`ACStandInRenderer`/STANDIN 层已删除。64 个 AC EntityType 中仅 `pilot_mob` 走 E1 placeholder。
- **动态层**：Demon/Evil Sheep wool、Anti Spider eyes、held/custom-head、Star Spawn 玩家触手、Dread carrier 触手、Shadow body+eyes brightness alpha、boss/dragon eyes 已接。Carrier 用 `LegacyEntities.isDread` 的真实类型语义；Star Spawn 用旧 UUID/名称判定。
- **Boss death**：`ACDeathTime` 为服务端权威 `SynchedEntityData` + NBT；Jzahar 800 tick、Chagaroth/Sacthoth/Dragon 200 tick。Jzahar 后 400 tick与 Dragon 全程恢复固定种子 rays；Dragon 按旧顺序绘 exploding/body、eyes、rays；Jzahar/Chagaroth恢复死亡骨骼隐藏/姿态。Sacthoth 旧版没有 rays，未虚构添加。
- **护甲**：Samurai 64x64 专属 `HumanoidModel` 经 Forge item extension / Neo extension event；Depths 标准层纠正为旧 inner，player/armor stand/zombie/skeleton 加 old outer pass 与 glint；Ghoul 读真实 armor slots，Dreadguard 无条件固定 overlay。Goliath 的 Geo 怪物甲层会覆盖头骨与胸骨，现已完全移除：装备仍保留属性，但 renderer 不注册盔甲层、也不解析 `skeleton_goliath` 盔甲贴图。21 张怪物甲资源与特殊甲资源仍保留在资源树中。
- **自动验证**：Forge/Neo `compileJava` 与全量 `build` 成功；两端 `runClient` 均抵 Sound Engine + atlas 完成，0 missing renderer / layer bake / AC render texture / GeckoLib 异常。43 个 AC 自有 Java PNG 引用全部存在（另 8 个是 vanilla namespace）；生产 JAR required entries=all、render validation entries=0。
- **Forge Legacy-A 目视（2026-07-25）**：Abyssal Zombie、Coralium Squid、Dreadling 已由用户确认通过。首轮发现旧 eyes PNG 的全透明像素保留非黑 RGB，现代 additive `RenderType.eyes` 将其放大成白模/白触手；11 张 eyes PNG 仅把 alpha=0 像素 RGB 归零，所有可见像素逐像素不变。Abyssal Zombie 另因旧 64x32 `ModelZombie(..., true)` 贴图误用现代 64x64 Zombie layer 而错位，现用专用 64x32 body/inner/outer 三层修复。其余批次及 Neo 复验仍待完成。
- **Forge Legacy-B 目视（2026-07-25）**：Dread Spawn、Greater Dread Spawn、Lesser Dreadbeast 已由用户确认通过。首轮 carrier 触手错误统一固定在模型坐标 `(0,8,2)`；旧 `LayerDreadTentacles` 实际按主模型首 cube 的底面动态定位。现将等价锚点显式固化：Dreadling `(0,3,0)`，Dread Spawn/Greater/Lesser `(0,22,0)`，保留各 renderer 的 1x/2x/3x 整体比例。Forge 复测通过，NeoForge 编译通过。
- **Forge Legacy-C 目视（2026-07-25）**：Shadow Creature、Shadow Monster、Shadow Beast 已由用户确认通过；专用几何/UV、亮度驱动半透明本体与 eyes alpha 均无新增问题。Forge legacy 现为 9/9，NeoForge 九实体复验仍待完成。
- **NeoForge Legacy-A 目视（2026-07-25）**：Abyssal Zombie、Coralium Squid、Dreadling 已由用户确认通过；64x32 Zombie layer、透明 eyes 与 Dreadling carrier 锚点均无跨版本偏差。NeoForge legacy 现为 3/9。
- **NeoForge Legacy-B 目视（2026-07-25）**：Dread Spawn、Greater Dread Spawn、Lesser Dreadbeast 已由用户确认通过；1x/2x/3x 比例、UV 与 `(0,22,0)` carrier 锚点均无跨版本偏差。NeoForge legacy 现为 6/9。
- **NeoForge Legacy-C 目视（2026-07-25）**：Shadow Creature、Shadow Monster、Shadow Beast 已由用户确认通过；几何/UV、明暗透明度与 eyes alpha 均无跨版本偏差。双端 legacy 九实体视觉切片现为 Forge 9/9 + NeoForge 9/9。
- **Forge effect A1 目视（2026-07-25）**：Black Hole、Portal、Single Portal 已由用户确认通过；billboard 相机朝向、透明边缘、循环旋转/形变与 4.0/2.0/1.2 尺寸层级均无问题。Forge effect 现为 3/12。
- **Forge effect A2 目视（2026-07-25）**：Implosion、Compass Tentacle、Powerstone Tracker 已由用户确认通过。Compass Tentacle 首测过小：旧 renderer 同时使用外层 `scale(0.3)` 与 `ModelBase.render(..., scale=0.5)`，现代 `ModelPart` 固定按 1/16 单位输出；迁移时遗漏后者，故独立 renderer 从 0.3 校正为 2.4（8 倍），共享 carrier 模型不变。Forge 强制重编译与复测通过；Forge effect 现为 6/12。
- **Forge Boss 动态锚点目视（2026-07-25）**：Skeleton Goliath 双持、Sacthoth Soul Reaper 与 Dreadguard carrier 经隔离正/侧视通过。持物根因是自定义 `renderForBone` 绕过 `BlockAndItemGeoLayer` 的完整 bone pivot+rotation 矩阵，恢复官方矩阵链并为 offhand 传入左手 flag 后物品随腕端旋转；carrier 锚点使用 Geo 全局层坐标，按 Dreadguard head cube `originY=24 + sizeY=8` 取 `Y=32`，不可混用旧 vanilla 翻转后的局部 `Y=-7`。NeoForge 强制编译与 generated 源断言通过，游戏内目视仍待验。
- **双端视觉完成与生产清理（2026-07-25）**：Forge/NeoForge 各以一次六区总场景完成 legacy、effect/projectile/ODB、动态层、持物/carrier、7套护甲与特殊宿主、Boss death、Research/Pedestal BER 目视，用户最终确认无问题。修复 Chagaroth carrier、Goliath甲遮骨、Dreadguard carrier、Gecko持物、护甲alpha、Anti基础甲层、Skeleton细肢外层与Anti Ghoul模型链。已删除 `RRRenderValidation`、`acrrrender`、Boss/ODB validation setter、Star Spawn `Dev` 名称触发及五个 effect 生命周期豁免；源码与双端 production JAR 七类残留均为0。双端完整 production build（含主 remapJar）通过，关键条目/修复贴图哈希/loader元数据审计通过。
- **范围边界**：上述结论覆盖全部已实现的 renderer/layer 与两个现有 BER。四断头、Jzahar spawner、sealing lock、rending/sacrificial/energy/ODB 等缺现代 BlockEntityType 的 BER 继续归 T4.6d；护甲物品图标 missing item model 仍属 Stage K，不影响已通过的穿戴模型链。

## 修订日志

- 2026-07-26：RR-ENTITY-BEHAVIOR/CR-72 收口：全族专属行为、69→97 现代 loot、双端 69 死亡路径、11 场真实自然生成、实体/owner 重启持久化、runData/build/JAR 与无属性专服 reload 全部通过；临时 fixture/属性/快照残留为 0，Gate M3-CONTENT 完成。
- 2026-07-25：RR-RENDER-VISUAL 双端六区总场景完成，用户最终确认无问题；现有渲染面全部收口，T4.6d 缺宿主 BER 保持独立未完成。临时验证命令/属性/setter/生命周期分支已删除，双端完整 production build 与主 JAR 零残留门禁通过。
- 2026-07-24：RR-RENDER-AUTO/CR-65，实现侧与自动门禁收口；详见 §12.6。视觉矩阵与缺宿主 BER 按任务表拆分保留。
- 2026-07-24：RR-ENTITY-CATALOG（Agent C）交付九漏实体目录与核心服务端行为（§11A）：目录扩至旧 63 内容类型、48 蛋、44 placement、34 唯一实体 loot 基线；新增九实体 loot 双目录、9 对 spawn modifier、PotentialSpawns 上下文候选、1.21 EntityType 分类 tag 和精确 ID invariant。双端 build/runData/runServer、热重载、九 `/summon`、五 Darklands 分布与 Abyssal Zombie 转化通过；Forge Dread 5→1/1→2 通过。自然生成统计、边角行为、69 loot 全量与九实体忠实渲染仍显式待办。
- 2026-07-22：**Gate E2 收口（PE-2..6 全 ☑）**。PE-4 交付 boss+misc+projectile 渲染（§12.4）：27 实体渲染器 = Chagaroth GeckoLib 忠实网格 + 余 11 boss/custom-misc/proj `ACStandInRenderer`（按 bb 缩放的贴图箱体，fork-free 仅 4-arg `ModelPart.render`）+ CoraliumArrow `ArrowRenderer` / Spirit·Essence `ItemEntityRenderer`（vanilla 复用）+ 迁 17 忠实贴图 + `ModModelLayers` STANDIN 层；两节点 `compileJava` BUILD SUCCESSFUL、forge `runClient` 抵标题屏 0 crash/0 missing-renderer/0 validateReg；**neoforge `runClient` 渲染全初始化后原生退出 -1**（无 Java 错/hs_err，未定位、待人工确认）；忠实网格(PE-4b)/动画/billboard/视觉延后。PE-6 交付 BlockEntity 渲染框架（§12.5）：扩 `EntityRendererCompat` 加 fork-free `registerBlockEntity` BER sink + 新 `client/render/block/ACBlockEntityRenderers` relay（当前 0 BER——机器无需 BER、1.12.2 TESR 方块[祭坛/基座浮空物品·断头·刷怪器]全属未移植 Stage S）；两节点 `compileJava` BUILD SUCCESSFUL、forge `runClient` 抵标题屏零崩。
- 2026-07-22：PE-5 交付标准护甲穿戴层（§12.3）：7 材料×4=28 护甲穿戴经 vanilla `HumanoidArmorLayer` 渲染（**标准护甲零自定义渲染码**、`client/render/armor/**` 现为空）；接线全在 `platform/ArmorCompat.piece()`（1.20.1 namespaced `getName()` / 1.21 单元素 `List.of(new ArmorMaterial.Layer(ACRef.id(name)))` → 两端均解析 `abyssalcraft:textures/models/armor/<name>_layer_{1,2}.png`）+ 迁 14 贴图（1.12.2 `textures/armor/*` → `textures/models/armor/*`）+ 同步 Javadoc；两节点 `compileJava` BUILD SUCCESSFUL + neo `runClient` 抵标题屏 0 crash/0 armor-tex-missing。延后：武士甲专属模型(`IClientItemExtensions`) / depths 内外双层发光 / ghoul·goliath 怪物甲层 `LayerGhoulArmor`（协调 PE-3/4、贴图未迁）/ 穿戴视觉人工目视。
- 2026-07-22：PE-3 交付 ghoul(5)+shoggoth(3) 渲染（§12.2）：专属 `GhoulModel`/`ShoggothModel`（`HierarchicalModel` 简化-忠实、贴图绑专属 UV）+ `Ghoul/ShoggothRenderer` + `GhoulShoggothRenderers` + `SimpleEyesLayer`（vanilla `EyesLayer` 子类避 `renderToBuffer` fork）+ 迁 22 贴图 + 注册 2 自定义 `ModModelLayers` 层；两节点 `runClient` 0 missing-renderer/layer-bake/validateRegistrations/crash。细节 cube / armor(→PE-5) / 彩蛋 / 半透明 / 精确视觉(人工目视) 延后。
- 2026-07-22：PE-1 交付渲染注册框架（Stage E1）：新 `platform/EntityRendererCompat`（`EntityRenderersEvent` fork + 中性 sink）+ `registry/ModModelLayers`（PLACEHOLDER 层）+ `client/render/entity/ACPlaceholderRenderer`（cube 占位）+ `client/render/ACEntityRenderers`（遍历 registry 给全 54 实体注册占位）+ 主类 client attach；两节点 `compileJava`+`runClient` **0 missing-renderer/0 crash**、各抵标题屏（§12）。解了 D2a/D2b 「缺渲染器 runClient 崩」，解锁 E2（PE-2..6）。
- 2026-07-22：PD-7 交付 BOSS 家族（12 EntityType = 4 血条 boss + 8 elite/minion；新 `content/entity/boss/ACBossMob` boss-bar 框架 `ServerBossEvent` + `BossKind/BossMob` / `EliteKind/EliteMob` enum 塌缩 + `BossEntities` registrar + 12 蛋；boss 非自然刷怪无 SpawnPlacement、战利品延后）；两节点 `compileJava`+`runServer` 12 `/summon` + 属性精确 + 4 boss 血条 tick 零 exception 双端实证（§11）。**Gate D2 收口**（54 EntityType）。skill / 多部件 / 商人 / 渲染延后。
- 2026-07-22：PD-6 交付 misc + projectile 家族（5 projectile + 10 misc = 15 非生物 EntityType；2 platform fork-base `ACSimpleEntity`/`ACThrowableProjectile` 吸收 `defineSynchedData` 分叉 + `ProjectileEntities`/`MiscEntities` registrar；ODB/Portal 各 2 变体塌缩；非生物无属性/loot/蛋）；两节点 `compileJava`+`runServer` 15 `/summon` + primedodbcore fuse→explode→discard + blackhole tick 双端实证（§10）。受益 PD-5 修好的 `SpawnPlacements.Type` bug（neo 不再受阻）。
- 2026-07-22：PD-5 交付 ghoul(5)+shoggoth(3) 家族（`AbstractGhoul`/`AbstractShoggoth` extends `ACMob` + `Ghoul/ShoggothEntities` registrar + 8 蛋 + 16 战利品双目录 + 2 forge biome_modifier）+ **新 `platform/SpawnPlacementCompat`**（并修好瞬态阻断 PD-3/PD-4 neo 编译的 `SpawnPlacements.Type`→1.21 `SpawnPlacementTypes` bug、neo 现两端全绿）；两节点 `runServer` 8 `/summon` + max_health=30 + `/loot` 掉 Ghoul/Shoggoth Flesh 实证（§9）。跨主 gap-fill PB-2 `MiscItems` +5 shoggoth flesh。
- 2026-07-22：PD-3 交付 anti 反物质族（11 实体 extend-vanilla + `AntiEntities` + 新 `platform/SpawnEggCompat` + 9 战利品表）；forge `runServer` 11 anti 各 `/summon` + Health 实证（§8）。neo 待并发 PD-5 `SpawnPlacementCompat` 解阻（我方文件零错）。
- 2026-07-22：PD-1 交付实体注册框架（`ACMob` 基类 + `ModEntities` + `EntityAttributeCompat`）+ `pilot_mob` 示例；两节点编译 + forge `runServer` `/summon` Health 20.0f 实证。并入 PD-2（Agent 13）AI/寻路框架为同子系统 §3.4。
