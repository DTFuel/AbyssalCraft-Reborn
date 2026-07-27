# 世界生成 (Worldgen) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M5 / Stage G（G0 契约+竖切+调研 → G1 逐维度/特征/结构 → G2 传送门/群系/验证）
- 关联平行任务：PG-0（本文档，G0）..PG-7
- 状态：**M5-WORLD-AUTO 已交付** —— 四维现代混合保真地形、真实材料/carver、六 Darklands、37 模板壳体与结构拓扑双端成立；旧噪声 oracle、动态 marker 真实玩法、人工视觉、自然刷怪统计和玩家 portal 仍未完成。
- 负责：PG-0（框架+竖切+调研）；各维度/特征/结构 PG-1..7
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的世界生成：4 个自定义维度（Abyssal Wasteland / Dreadlands / Omothol / Dark Realm）+ 18 群系 + 特征（树/湖/石笋/monolith/洞穴/峡谷）+ 36 nbt 结构 + 传送门。1.12.2 用代码 `ChunkGenerator` + `BiomeProvider`（GenLayer）+ `MapGenStructure`/`WorldGenerator`；现代（1.20.1/1.21.1）迁到**数据驱动为主 + 必要代码**的混合方案。

**Stage G0 目标（本文档）**：不做 4 个真维度，而是①搭 worldgen 框架（Java 基建 + 注册入口）、②做 1 个数据驱动**迷你维度竖切**（穿透 dimension_type→noise_settings→surface_rule→biome→dimension→feature 全层）证明管线、③把 R1/R2/R3 调研结论 + worldgen ID 命名**冻结**到 [平行任务表 §5](../porting/02-porting-parallel-tasks.md#5-调研结论填充区)，供 G1 的 PG-1..5 只读引用、互不依赖并行。

## 2. 范围

- **G0 含**：`world/ACDimensions`※（维度 `ResourceKey<Level>` 契约）、`registry/ModWorldgen`（代码 worldgen 对象注册入口）、`world/feature/MiniPillarFeature`（示例代码特征）、`abyssalcraft:mini` 竖切数据包（dimension/dimension_type/noise_settings/biome/configured_feature/placed_feature）、R1/R2/R3 结论 + ID 冻结。
- **G0 不含**（→ G1/G2）：4 个真维度的 dimension/noise/biome/surface（PG-1..3）、特征全量（PG-4）、36 结构（PG-5）、传送门/Teleporter（PG-6）、群系放置 biome_modifier + 世界生成验证矩阵（PG-7）。

## 3. 设计 / 架构

### 3.1 数据驱动优先（R3）
稳定路径 = **数据包维度** + vanilla `NoiseBasedChunkGenerator`（`minecraft:noise`）：`dimension.json` → `dimension_type` + `generator`(settings=`noise_settings` + `biome_source`)。仅在纯数据包表达不了旧观感时才退代码 `ChunkGenerator`/`BiomeSource`（其注册值类型 1.20.1 `Codec` ↔ 1.21 `MapCodec` **分叉** → 落 `platform/`）。

### 3.2 框架三件（G0 交付）
- `world/ACDimensions`※ — 4 真维度 + `MINI` 的 `ResourceKey<Level>` 常量（`ResourceKey.create(Registries.DIMENSION, ACRef.id(path))`，两端同 → fork-free）。传送/portal/entity-dim-check/knowledge-condition 只读引用此契约。标 ※ = 预授权承 loader/version fork（今无需）。
- `registry/ModWorldgen` — `ModRegistrar<Feature<?>>`（`Registries.FEATURE`）注册代码特征；接 `ModRegistries.ALL`（Gate 集成一行）。是 G1 自定义 ChunkGenerator/BiomeSource/DensityFunction/StructureType/StructurePiece codec 的登记入口（注释文档化）。
- `world/feature/MiniPillarFeature` — `Feature<NoneFeatureConfiguration>`，`place()` 放 4-block glowstone 柱。证 PG-4 的代码特征 → configured/placed_feature JSON → biome 装饰步 全链路。`Registries.FEATURE` 值类型 `Feature<?>` 两端稳定、无 fork。

### 3.3 竖切数据流（`abyssalcraft:mini`）
`dimension/mini.json`(`minecraft:noise` + `minecraft:fixed`→`abyssalcraft:mini` biome + settings=`abyssalcraft:mini`) → `noise_settings/mini.json`(default_block=stone + 自定义 `final_density` = vanilla `y_clamped_gradient` from_y0/+1→to_y128/-1 → 实心到 y64/上空气 + depth-gated `stone_depth` surface_rule 顶层 grass) → `biome/mini.json`(features 步 9 挂 `abyssalcraft:mini_pillar`) → `placed_feature/mini_pillar.json`(count 2 + heightmap + biome) → `configured_feature/mini_pillar.json`(type=`abyssalcraft:mini_pillar`)。**无需自定义 DensityFunction codec**（全用 vanilla density 类型）。

## 4. 子系统内契约（G0 冻结 · G1 只读；权威副本见平行表 §5）

- **维度（4）**：`abyssalcraft:{abyssal_wasteland, dreadlands, omothol, dark_realm}`（旧数字 dim id 50/51/52/53 不沿用）。
- **群系（18 注册 + 3 抽象基类无 ID）**：AW(5) `abyssal_wastelands/abyssal_swamp/abyssal_desert/abyssal_plateau/coralium_lake`；DL(4) `dreadlands/dreadlands_mountains/dreadlands_forest/dreadlands_ocean`；`omothol`；`dark_realm`；主世界 Darklands(6) `darklands/darklands_forest/darklands_plains/darklands_hills/darklands_mountains/coralium_infested_swamp`；`purged`。基类无 ID：BiomeAbyssalWastelandBase/BiomeDarklandsBase/BiomeDreadlandsBase。
- **特征（~11，configured/placed 同名）**：`dead_tree/darklands_tree/dreadlands_tree`、`lake_liquid_coralium/lake_liquid_antimatter`、`abyssal_stalagmite/dreadlands_stalagmite`、`shoggoth_monolith`、carver `abyssal_cave/dreadlands_cave/abyssal_ravine`。
- **结构**：nbt→jigsaw（36）`graveyard(3)/chagaroth_lair(7)/shoggoth_pit(3)/omothol 城建(15)/jzahar_temple(7)/dark_shrine(1)`；程序化 `StructureType` `abyssal_stronghold/dreadlands_mineshaft/ethaxium_house/abyruin/chains`。
- **竖切示例**：`abyssalcraft:mini`（dimension/dimension_type/noise_settings/biome）+ `abyssalcraft:mini_pillar`（configured/placed_feature）；Java `ACDimensions.MINI` / `ModWorldgen.MINI_PILLAR`。

## 5. 跨版本 / 加载器要点

- **worldgen 数据包路径跨版本不分叉**：`data/<ns>/dimension/`、`dimension_type/`、`worldgen/{noise_settings,biome,configured_feature,placed_feature,structure,structure_set,template_pool}/` 在 1.20.1 与 1.21.1 **同名** → 共享单一 `src/main/resources/data/abyssalcraft/` 目录（异于 `loot_tables`/`structures`(nbt) 等 1.20.1 复数→1.21 单数的目录）。
- **`dimension_type.monster_spawn_light_level` 的 IntProvider 写法分叉（一手实证）**：1.20.1 uniform 需 `{"type":"minecraft:uniform","value":{"min_inclusive":0,"max_inclusive":7}}`（min/max 套 `"value"`）；1.21.1 需 min/max 在**顶层**（`{"type":"minecraft:uniform","min_inclusive":0,"max_inclusive":7}`）。共享 JSON 用 1.20.1 格式 → neo `runServer` 崩 `Failed to parse either … No key max_inclusive`。**解**：用**纯 int**（`"monster_spawn_light_level": 0`）——两端 codec 均 `either(Codec.INT, IntProvider)`，纯 int 走 ConstantInt 分支、双端接受。**G1 通则**：共享 worldgen JSON 一律走跨版本安全写法（纯 int / 避 IntProvider 对象形态歧义）+ 逐维度 `runServer` 双端验。
- **自定义生成器 codec 分叉**：`Registries.CHUNK_GENERATOR`/`BIOME_SOURCE`/`DENSITY_FUNCTION_TYPE` 的值类型 1.20.1 `Codec<? extends X>` ↔ 1.21 `MapCodec<? extends X>` → G1 若上代码生成器，注册与 codec 声明落 `platform/`。G0 竖切纯数据包，未触此分叉。
- **`Registries.FEATURE` 无 fork**：值类型 `Feature<?>` 两端同；`ModWorldgen.FEATURES` fork-free。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **纯数据包出地形**：`final_density` 用 vanilla `minecraft:y_clamped_gradient`（from_y/from_value 实心→to_y/to_value 空气，过 0 处 = 地表）即可，`noise_router` 其余项常量 0（fixed biome source 下 climate 项不影响群系选择）。`aquifers_enabled:false`+`ore_veins_enabled:false`+空 carvers → 深层纯 default_block、便于探针。（证据级别：一手 `runServer` 探针）
- **surface_rule 必须 depth-gated**：裸 `minecraft:block` 会刷满整列埋掉 default_block；用 `minecraft:stone_depth`(surface_type=floor) 限顶层带。
- **mod 维度默认不载区块**：无玩家专服只 tick overworld 出生区；测 mod 维度前须 `execute in <dim> run forceload add <x> <z>` 才能 gen/`fill`/探针（否则 `That position is not loaded`）。
- **noise_settings 改需重启**（`/reload` 不重跑 worldgen）、测新鲜区块（旧区块存盘保留旧地形）。
- **headless 探针**：`locate biome <ns>:<b>`（采样 biome_source，无需 gen）；`fill <region> minecraft:air replace <X>` 报 changed-count = 实数（`fill X replace X` 同块永报 0，不能探存在性）；`execute if block <pos> <X> run say ...`。
- **runServer 交互 stdin**：`gradlew … runServer` **不可**管道 `| Tee-Object`/`2>&1`——会占用/EOF 关闭 stdin，服务器抵 `Done` 后随即 -1 退出、无法送控制台命令。必须裸跑 + 送命令到终端 stdin。（本会话一手实证：Tee 版抵 Done 即退；裸跑版可送 `/execute in …`）
- **无玩家专服探针后 ~17s 空闲即被收**：送命令要紧凑；或重启（forceload 的区块+特征方块已存盘，重启后 forceload 重载可再探）。
- **并发隔离**：共享 run 目录 → 临时改 `run/server.properties` 的 `level-name`+`server-port`（避 25565 与他人服冲突/`session.lock`），跑完还原 + 删临时世界。

## 7. 验证 / DoD

- **两节点 `compileJava --rerun-tasks`（新增 Java 文件）**：forge/neo BUILD SUCCESSFUL。
- **两节点 `runServer`（隔离 level-name/port）**：
  - forge/neo 均 `Done` **零 worldgen 加载错**（6 worldgen JSON 全 parse+register；malformed → `Failed to load registries` 硬崩 → 反证全有效）。
  - `execute in abyssalcraft:mini run forceload add 0 0` → `Marked chunk [0,0]…`（维度存在+可 `/execute in`+区块 gen）。
  - `locate biome abyssalcraft:mini` → `0 blocks away`（自定义 biome 载入+被 fixed biome source 用）。
  - `if block 0 10 0 minecraft:stone` → 命中（default_block + `final_density` 实心地形）。
  - `fill 0 60 0 15 80 15 air replace glowstone` → **8**（2 柱×4，`MiniPillarFeature` 在自然装饰阶段执行——forge/neo 一致）。
- **未机核项**：真玩家步行进出维度 + 目视地形/天空/雾 = 人工目视（headless 只证维度注册/可 `/execute in`/地形数据探针，天空雾 `DimensionSpecialEffects` 待 T6.3 + 目视）。

## 8. G1 已交付维度

> G1（PG-1..5）逐维度/特征/结构落地，据 §4 冻结 ID。下方记已交付维度的实现要点。

### 8.1 Omothol + Dark Realm（PG-3，简单纯数据包维度）

两维度均属 R3「简单维度纯数据包」——vanilla `minecraft:noise` 生成器 + `minecraft:fixed` biome source + 复用 G0 竖切 noise_settings 模板（`y_clamped_gradient` final_density），**无 Java**（`ACDimensions.OMOTHOL`/`DARK_REALM` 的 `ResourceKey<Level>` G0 已冻结、dim id 对齐即「接入 ACDimensions」）。各维度 4 JSON（dimension / dimension_type / noise_settings / biome）。

- **忠实换块**：Omothol default_block=`abyssalcraft:omothol_stone`（一手核 `ChunkGeneratorOmothol` setBlocksInChunk + `BiomeOmothol` top/filler）；Dark Realm=`abyssalcraft:darkstone`（`ChunkGeneratorDarkRealm` + `BiomeDarkRealm`）。
- **暗天**（对旧 WorldProvider：celestial 0.5 定夜 + 暗雾 10518688×0.15 + isSurfaceWorld false + canRespawnHere false；Dark Realm getSkyColor 黑）→ dimension_type `effects=minecraft:the_end`（暗天占位、真 `ACSkyRenderer` 天空雾延 T6.3）+ `has_skylight:false` + `natural:false` + `bed_works:false` + `respawn_anchor_works:false` + ambient_light 0.15(Omothol，对旧 f=0.25) / 0.05(Dark Realm，对旧 f=0.10)；biome `has_precipitation:false`（无天气）、fog_color 1577752(Omothol 暗紫) / 0(Dark Realm 黑)。
- **noise_settings**：`default_fluid=air`（无海——旧 setBlocksInChunk 只填 stone 不填水）、`final_density`=`y_clamped_gradient`（实体地形）、surface_rule 同 default_block、`aquifers`/`ore_veins` off、单群系 `minecraft:fixed`。
- **跨版本安全（承 §5）**：`monster_spawn_light_level` **纯 int 7**（避 1.20.1↔1.21 IntProvider 分叉）；worldgen 路径两端不分叉 → 共享单目录。
- **验证**：两节点 `runServer` 均 `Done` 零 worldgen 加载错；forge 全 runtime（`/execute in abyssalcraft:{omothol,dark_realm}` 进入 + forceload 生成 + `if block 0 10 0` = omothol_stone/darkstone + `locate biome` 各 0 blocks）；neo `Done` + `/execute in abyssalcraft:omothol` 进入 + forceload crashless 生成。**诚实**：neo 显式块/biome/dark_realm 探针未获（neo runServer 抵 Done 后 ~20s 空闲即退 + 1 次并发环境早崩，3 runServer 后止试）；据 neo Done + omothol chunk-gen crashless + G0 已在 neo 全证同一 vanilla noise/fixed-biome 机制 + darkstone 已注册 + forge 全探针 判 neo 等价。
- **延后（诚实）**：①**浮空岛真形**——旧 `ChunkGeneratorOmothol` 是 5-octave 噪声浮岛、Dark Realm 旧 overworld 式 + 洞穴/峡谷（`MapGenCavesAC`/`MapGenRavineAC` → PG-4 carver）；现用平实体 stone（R3 简单维度足够「可进 + 地形成立」）。②结构（omothol 城建/temple/graveyard/shoggoth_pit → PG-5）。③自然刷怪（remnant/omothol_ghoul/gatekeeper_minion/shadow* → biome spawners 空、待 biome_modifier D2a/PL-4）。④自定义天空雾（`ACSkyRenderer` → T6.3/PH-2）。

### 8.2 Dreadlands（PG-2，multi_noise 多群系维度）

**异于 §8.1 简单维度**：Dreadlands 有 4 群系（dreadlands / dreadlands_forest / dreadlands_mountains / dreadlands_ocean），故用 vanilla `minecraft:multi_noise` biome source 做数据驱动分布（R3「多群系维度首选 multi_noise 数据包」），**无 Java**（`ACDimensions.DREADLANDS` `ResourceKey<Level>` G0 已冻结）。7 JSON（dimension / dimension_type / noise_settings / 4 biome）。

- **群系分布（multi_noise，关键）**：`dimension.json` 的 `biome_source` = `minecraft:multi_noise` + 4 biome 各一 climate 参数点。分布轴：**continentalness 三段**（ocean -0.85 / base+forest 0 / mountains 0.85）+ **humidity** 分 forest（0.75）与 base（0）。**noise_router 必须给 climate 项真噪声**（ref06：全 0 会塌缩成单群系）——`continents`/`erosion`/`ridges` 引 vanilla `minecraft:overworld/{continents,erosion,ridges}` density fn、`temperature`/`vegetation` 用 inline `shifted_noise`（noise=`minecraft:temperature`/`vegetation`，shift_x/z=`minecraft:shift_x`/`shift_z`）。这些 climate fn 只驱动群系选择、不影响地形（地形跟 `final_density`）。`legacy_random_source:false`（正确 seed overworld density fn）。
- **忠实地形**（一手核 `ChunkGeneratorDreadlands` + 4 biome + `BiomeProviderDreadlands`）：default_block=`abyssalcraft:dreadstone`（旧 setBlocksInChunk 全填 dreadstone）；**无水海**——`default_fluid=minecraft:lava` + `sea_level=31`（对旧 genLava y6-31 的岩浆、非水）；**noise-hilly**——`final_density`=`y_clamped_gradient`(48→96) + `minecraft:overworld/base_3d_noise`（滚动地表 ~y60-84）；`aquifers`/`ore_veins` off。
- **surface_rule 按 biome 换块**（`minecraft:biome` 条件 + `stone_depth` floor）：forest → `dreadlands_grass`(顶) over `dreadlands_dirt`(带)；ocean → `dreadlands_muck`；base/mountains → 无规则（露 default_block dreadstone，对旧 `BiomeDreadlands` top/filler=dreadstone）。
- **观感/氛围**（对旧 `ACClientVars` + `WorldProviderDreadlands`）：biome foliage/grass_color=`0x910000`(dread 红)、fog_color=`0x330808`(暗红)、`has_precipitation:false`（旧 setRainDisabled）；dimension_type nether-like——`effects=minecraft:the_nether`(暗雾占位、真 `ACSkyRenderer` 延 T6.3)、`fixed_time:18000`(对旧 celestialAngle 0.5 定夜)、`has_skylight:true`(旧有天)、`natural:false`/`bed_works:false`/`respawn_anchor_works:false`(旧 canRespawnHere false)、ambient_light 0.1(旧 nether-like 亮度地板)。
- **跨版本安全（承 §5）**：`monster_spawn_light_level` **纯 int 7**（避 IntProvider 分叉）；overworld density fn 引用 + multi_noise schema 两端同 → 共享单目录、无 fork。
- **验证（两节点全 runtime，隔离 level-name/port 避并发 PG-1 awtest agent）**：forge/neo 均 `runServer` `Done` 零 worldgen 加载错（7 JSON + multi_noise + overworld density fn 引用双端 schema 有效、无 1.21 IntProvider 崩）；两端 `/execute in abyssalcraft:dreadlands` 进入；**4 群系全 `locate biome` 命中双端**（forge dreadlands 0 / forest 0 / mountains 128 / ocean 1538；neo dreadlands 181 / forest 0 / mountains 731 / ocean 430 blocks —— **群系分布双端成立** = T5.3 验收）；forceload 生成 + `if block 0 40 0`=dreadstone 双端。
- **延后（诚实）**：①特征（dreadwood 树 `WorldGenDrT`、dread 矿 `dreaded/dreadlands_abyssalnite_ore`、`dreadlands_stalagmite`、`dreadlands_cave` carver → PG-4；现 biome features 全空）。②结构（`dreadlands_mineshaft`/`chagaroth_lair` → PG-5）。③mob 刷怪（dreadling/dreadguard/demon animals 等 → biome spawners 空、待 biome_modifier D2a/PL-4）。④**逐 biome 真高度差**——旧 mountains baseHeight 1.3/heightVar 0.9、ocean -1.0/0.1；现 4 群系共享同一 hilly `final_density`（R3「群系分布成立」已达，高度差需把 depth 接入 biome offset、留迭代）。⑤Darklands 第五群系已由 RR-ENTITY-CATALOG 后续工作补入 Dreadlands biome source（见 §13）；其具体高度/地表过渡仍待保真。⑥忠实天空雾（`ACSkyRenderer` → T6.3）。

### 8.3 Abyssal Wasteland（PG-1，multi_noise 多群系维度）

**同 §8.2 多群系（异于 §8.1 简单维度）**：AW 有 5 群系（abyssal_wastelands / abyssal_desert / abyssal_swamp / abyssal_plateau / coralium_lake），用 vanilla `minecraft:multi_noise` biome source（R3「多群系维度首选 multi_noise 数据包」），**无 Java / 无 relay / 无 main / 无 lang**（`ACDimensions.ABYSSAL_WASTELAND` `ResourceKey<Level>` G0 已冻结 → 纯数据包完全并行安全）。8 JSON（dimension / dimension_type / noise_settings / 5 biome）。

- **忠实维度**（一手核 `WorldProviderAbyssalWasteland`）：dimension_type `has_skylight:true`（旧 hasNoSky 被注释=有天光）+ 无 ceiling + `ambient_light:0.25`（对旧 generateLightBrightnessTable f=0.25，干净映射）+ `fixed_time:18000`（对旧 celestialAngle 0.5 定夜）+ `bed_works:false`（旧 canRespawnHere false）+ 干维度（旧 canDoRainSnowIce false）；`effects=minecraft:overworld` 占位（真 `ACSkyRenderer` 暗绿天空 RGB(0,105,45) 非数据包 → T6.3/PH-2）。
- **忠实地形**（一手核 `ChunkGeneratorAbyssalWasteland`——旧为 vanilla overworld 噪声拷贝仅换块）：default_block=`abyssalcraft:abyssal_stone`；**海=`minecraft:water`@sea_level 55**（对旧 setBlocksInChunk b0=55 的 `liquid_coralium` 海——**忠实=liquid_coralium，流体未移植故用 water 占位**）；`final_density`=`y_clamped_gradient`(45→75) + `minecraft:noise`(continentalness) 起伏（滚动废土地表 ~y55-65）；`aquifers`/`ore_veins` off。
- **biome-conditioned surface_rule**（`minecraft:biome` 条件 + `stone_depth` floor，忠实各 biome sub-class top/filler）：plateau → `coralium_stone`（旧 top+filler=coralium_stone）；wastelands·swamp → `fused_abyssal_sand`(顶 1 块) over `abyssal_sand`(填)；desert/coralium_lake/默认 → `abyssal_sand`(顶) over default_block abyssal_stone（coralium_lake 忠实 filler=abyssal_stone）。
- **观感**（对旧 `ACClientVars`）：biome grass/foliage_color 忠实——wastelands·swamp `0x447329`、desert `0x789455`、plateau `0x2e7e67`、coralium_lake `0x59c6b4`；water_color `0x24FF83`；`has_precipitation:false`（旧 setRainDisabled）。
- **5 群系分布（关键 R3 决策）**：旧 `GenLayerBiomesAW` 是 **8 群系均匀随机**（含 3 darklands、无主群系；HillsAW 死代码、RiverAW 画 coralium_lake）。`multi_noise` 无法复现均匀随机（无区分信号），故**编造 temperature/humidity climate 带**给 5 群系各一 distinct 点（wastelands 中心 0,0 / desert 热干 0.7,-0.7 / swamp 湿 -0.3,0.7 / plateau 0.5,0.5 / coralium_lake 冷 -0.7,-0.3），noise_router 用 inline `shifted_noise`(temperature/vegetation) 驱动、其余 climate 项 0 → 5 群系按 temp/humidity Voronoi 散布。**3 darklands-in-AW legacy 怪癖不移植**（清洁化，仅 5 AW 群系；darklands 属 overworld）。
- **跨版本安全**（承 §5 R1）：`monster_spawn_light_level` 纯 int（未犯 G0 记录的 IntProvider 分叉）；`dimension`/`dimension_type`/`worldgen/*` 路径跨版本不分叉 → 共享单目录。
- **验证（两节点全 runtime，隔离 level-name=awtest/port 25590·25591 避并发 PG-2）**：forge/neo 均 `runServer` `Done` 零 worldgen 加载错（8 JSON + multi_noise + shifted_noise + biome-cond surface_rule 双端 schema 有效）；两端 `/execute in abyssalcraft:abyssal_wasteland` 进入 + forceload 生成；**5 群系 `locate biome`**（forge 全 5 命中 wastelands 0 / desert 115 / swamp 704 / plateau 747 / coralium_lake 320 blocks；neo plateau 32 blocks —— multi_noise 分布双端成立 = T5.2 验收）；`if block 0 10 0`=abyssal_stone 双端；surface 双端（forge 块[0,0] abyssal_sand 1111 + wastelands 块[8,0] fused_abyssal_sand 256=1/柱；neo 块[0,0] plateau coralium_stone 542 —— biome-cond surface_rule 双端成立）。
- **延后（诚实）**：①sea=liquid_coralium（流体阶段，现 water 占位）。②特征（`abyssal_stalagmite`/`lake_liquid_coralium`/`dead_tree`(swamp) → PG-4；现 biome features 全空、Gate G1 挂 biome）。③结构（`graveyard`/`shoggoth_pit`/`abyssal_stronghold`/`abyruin`/`chains` → PG-5）。④自然刷怪（CoraliumSquid/AbyssalZombie/DepthsGhoul/SkeletonGoliath 等 → biome spawners 空、待 biome_modifier D2a/PL-4）。⑤**per-biome 真高度差**——旧 plateau baseHeight 1.5(mesa)/coralium_lake -1.0(basin)；现 5 群系共享同一 hilly `final_density`（R3「群系分布成立」已达，高度差需 multi_noise depth 接 biome offset / 代码 BiomeSource、留迭代）。⑥忠实天空雾（`ACSkyRenderer` → T6.3/PH-2）。⑦精确地形观感 = 人工目视。

## 9. G1 已交付特征（PG-4）

**范围**：全维度装饰特征（石笋 / 石碑 / 枯树 / 树 / 湖 / 洞穴 / 峡谷）迁至 Configured/Placed Feature。**3 代码 `Feature`**（`world/feature/**`，fork-free：`place(FeaturePlaceContext)` / `BlockStateConfiguration` / `setBlock(pos,state,2)` 两端稳定，同 G0 `MiniPillarFeature` 证过的路径）+ **29 数据包 JSON**。触碰 1 处 G0 框架 `registry/ModWorldgen`（`FEATURES` registrar +3 register，无 relay/main/lang 改；registrar 本身 G0 已入 `ModRegistries.ALL`）。

- **3 代码 Feature**（`Feature<BlockStateConfiguration>`，方块由 JSON config 注入 → 一类服多群系）：
  - `StalagmiteFeature` — 锥形尖塔（height 5-12，半径随高递减），服 `abyssal_stalagmite`(abyssal_stone) / `dreadlands_stalagmite`(dreadstone)。
  - `MonolithFeature` — 3 宽 × 1 深 × 7-12 高 `monolith_stone` 板（shoggoth 石碑）。
  - `DeadTreeFeature` — 裸干 4-7 + 3 短枝（**dead_tree_log 未移植 → 占位 darklands_oak_log**）。
- **8 configured_feature**：`abyssal_stalagmite` / `dreadlands_stalagmite`（自定义 stalagmite 类型）、`shoggoth_monolith`（自定义 monolith）、`dead_tree`（自定义 dead_tree）、`darklands_tree` / `dreadlands_tree`（`minecraft:tree` **数据驱动**挂 AC log/leaves，忠实 distance/persistent/waterlogged blockstate 属性）、`lake_liquid_coralium` / `lake_liquid_antimatter`（`minecraft:lake` **water 占位**——忠实=liquid_coralium/antimatter，待流体阶段）。
- **8 placed_feature**：rarity/count + `in_square` + heightmap `WORLD_SURFACE_WG` + biome placement（同名不同注册表，与 configured 无冲突）。
- **3 configured_carver**：`abyssal_cave` / `dreadlands_cave`（`minecraft:cave`）、`abyssal_ravine`（`minecraft:canyon`）；**全 plain-float provider**（`ConstantFloat` via `either(FLOAT,…)`，避 G0/CR-39 记录的 value-wrapper 跨版本分叉）；`replaceable`=`#minecraft:overworld_carver_replaceables`。
- **2 additive tag** `tags/{block,blocks}/overworld_carver_replaceables`（`replace:false` 加 abyssal_stone/dreadstone/coralium_stone/darkstone，required:false）→ 让 vanilla carver 能凿 AC 基岩。
- **跨版本**：worldgen 路径（configured_feature/placed_feature/configured_carver）两端共享单目录（异于 loot_table 双目录）。
- **验证（两节点全 runtime，隔离 level-name/port）**：forge/neo 均 `runServer` `Done` 零 worldgen 加载错（29 JSON schema 双端有效）；forge `/place feature abyssalcraft:abyssal_stalagmite`→abyssal_stone（`STALAGMITE_STONE_OK`）；neo `/place feature shoggoth_monolith`→monolith_stone（`MONOLITH_STONE_OK`）+ `darklands_tree`→darklands_oak_log（`TREE_LOG_OK`）。
- **延后（诚实）**：①lake=liquid_coralium/antimatter（流体阶段，现 water 占位）。②dead_tree=darklands_oak_log 占位（dead_tree_log 未移植）。③特征挂 biome `features[]` / carver 挂 biome `carvers[]` = **Gate G1 集成**（biome 归 PG-1/2/3，用 §5 冻结 ID，本任务只交付 feature/carver 定义）。④精确视觉观感 = 人工目视。

## 10. G1 已交付结构（PG-5，程序化子集 ☑；36 nbt 模板 → T5.6b）

**范围**：结构迁移（R2 盘点 36 nbt 模板 + 程序化结构）。**本任务交付程序化 `StructureType` 子集**（3 简化-忠实 kind + loot 箱）；**36 二进制 `.nbt` 模板的 jigsaw 路径移出 → T5.6b**（见下）。**4 Java** + **15 数据包 JSON**。触碰 relay `registry/ModRegistries.ALL` +2（`ModWorldgen.STRUCTURE_TYPES` / `STRUCTURE_PIECES`）+ G0 框架 `ModWorldgen`（STRUCTURE_TYPE/PIECE registrar + AC_STRUCTURE/AC_PIECE）+ 无 main/lang 改。

- **`platform/StructureCompat`**（fork 边界，2 站点）：
  - `structureType(MapCodec<S>)`：1.20.1 `() -> codec.codec()` ↔ 1.21 `() -> codec`（`StructureType` SAM 返回类型分叉）。
  - `setChestLoot(level, pos, lootId, seed)`：1.20.1 `chest.setLootTable(ResourceLocation, seed)` ↔ 1.21 `chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootId), seed)`（`RandomizableContainerBlockEntity`；1.21 loot 引用改 `ResourceKey<LootTable>`）。
- **`world/structure/StructureKind`**（enum）：GRAVEYARD / ABYRUIN / DARK_SHRINE，`implements StringRepresentable`（CODEC=`StringRepresentable.fromEnum`）；`block()`=`BuiltInRegistries.BLOCK.get(blockId).defaultBlockState()`（graveyard/dark_shrine=darkstone_brick、abyruin=abyssal_stone_brick）、`lootTable()`=`ACRef.id("chests/"+name)`。
- **`world/structure/ACStructure`**（`extends Structure`）：`MapCodec` = `settingsCodec` + `kind`；`findGenerationPoint` 用 `context.chunkGenerator().getFirstOccupiedHeight(x,z,WORLD_SURFACE_WG,…)` 定 origin（**注**：读 noise 高度图、非实际方块 → 定位落自然地表）。
- **`world/structure/ACStructurePiece`**（`extends StructurePiece`）：5×5 floor + 4 角柱（graveyard h2 / 余 h3）+ 中心 loot 箱（`Blocks.CHEST` + `StructureCompat.setChestLoot`）；**load-ctor SAM=`(StructurePieceSerializationContext, CompoundTag)`**（非 `StructureTemplateManager` —— `StructurePieceType` 基接口签名，编译期实证修正）；全 `box.isInside` 门控（跨 chunk 边界安全，见 ref07）。
- **15 数据包 JSON**：3 structure（`type=abyssalcraft:structure` + kind + `biomes=#abyssalcraft:has_structure/<name>` + `step=surface_structures` + `terrain_adaptation=beard_thin`）+ 3 structure_set（`minecraft:random_spread` spacing 20/22/24、separation 8/9/10、异 salt）+ 3 has_structure biome tag（graveyard→abyssal_wastelands+abyssal_swamp / abyruin→abyssal_wastelands+abyssal_desert+abyssal_plateau / dark_shrine→dark_realm，required:false）+ **6 loot table 双目录**（`loot_table/`[1.21] + `loot_tables/`[1.20.1] `chests/{graveyard,abyruin,dark_shrine}`，vanilla 物品 rolls const/uniform + set_count uniform）。
- **验证（两节点全 runtime，隔离 level-name/port）**：forge/neo 均 `runServer` `Done` 零加载错（15 JSON schema 双端）；`/place structure abyssalcraft:<kind>` 建体（floor+柱+箱）双端；`data get block <chest> LootTable`=`abyssalcraft:chests/<kind>`（**forge dark_shrine @312,68,312 / neo graveyard @200,63,200 —— 证 setChestLoot 分叉双端**）；`execute in abyssalcraft:abyssal_wasteland run locate structure`（**forge graveyard [128,~,96] 160 blocks / neo abyruin [-304,~,-320] 441 blocks —— structure_set random_spread + has_structure biome tag 自然生成双端**）= T5.6「`/locate` 找到并生成、战利品箱正确」验收（程序化子集范围内）。
- **移出本任务（已挪到应属阶段，诚实）**：①**36 个二进制 1.12.2 `.nbt` 模板**（graveyard(3) / chagaroth_lair(7) / shoggoth_pit(3) / omothol 城建(15) / jzahar_temple(7) / dark_shrine(1)）——1.12.2-era 调色板/data-value 与 1.16+ 结构 NBT 不兼容，须**游戏内结构方块重导入重存 + 调色板重映射**到移植后方块（**非 headless** → jigsaw template_pool 路径）→ **T5.6b**。②其余程序化结构（ethaxium_house / chains / dreadlands_mineshaft / abyssal_stronghold）→ **T5.6b**。③箱 loot 现用 vanilla 物品（AC 专属 loot 物品 datagen → **PK-5**，Stage K / T1.9·T1.10）。④has_structure biome 交叉引用校验 → **Gate G1 / PG-7**。⑤精确视觉/结构真形 = 人工目视/后续。

## 11. G2 群系特征放置（PG-7，T5.8/T5.9）

**范围**：把 §9 的 PG-4 placed_feature 挂到各维度 biome（Gate G1 集成的特征侧），并做 worldgen 端到端验证（T5.9）。**纯数据包、无 Java/relay/main/lang**——14 新 biome_modifier JSON（7 feature × forge/neoforge 双份）。走 biome_modifier（非改 biome JSON）故不碰 PG-1/2/3 owned 的 biome 文件、无 owner 冲突。

- **文件**：`data/abyssalcraft/{forge,neoforge}/biome_modifier/feature_<name>.json`；forge=`forge:add_features`、neoforge=`neoforge:add_features`（两 loader schema 同：`biomes` 列表 + `features` placed_feature 列表 + `step` GenerationStep.Decoration）。biome_modifier 注册表路径本身分 loader 目录（`forge/`↔`neoforge/`），异于 worldgen 单目录。
- **挂接映射**（忠实 1.12.2 populate + §4 冻结 ID）：
  - `abyssal_stalagmite` → AW `{abyssal_wastelands, abyssal_desert, abyssal_plateau}`（step `surface_structures`；1.12.2 stalagmite 非 swamp/lake）。
  - `lake_liquid_coralium` → `abyssal_swamp`（`lakes`；1.12.2 coralium 湖仅 swamp）。
  - `dead_tree` → `abyssal_swamp`（`vegetal_decoration`；1.12.2 swamp treesPerChunk=1）。
  - `dreadlands_stalagmite` → dreadlands `{dreadlands, dreadlands_forest, dreadlands_mountains, dreadlands_ocean}`（`surface_structures`）。
  - `dreadlands_tree` → `dreadlands_forest`（`vegetal_decoration`）。
  - `shoggoth_monolith` → `omothol`（`surface_structures`）。
  - `lake_liquid_antimatter` → `dark_realm`（`lakes`）。
- **验证（两节点全 runtime，隔离 level-name/port）**：forge/neo 均 `runServer` `Done` 零 biome_modifier 加载错——modifier schema + `biomes`/`features` 引用双端 resolve（引用错会阻 Done，同 datapack codec 强校验先例）；**neo omothol 100 chunk 采样（forceload 0,0-159,159 + `execute in abyssalcraft:omothol run fill … air replace abyssalcraft:monolith_stone`）= 12 monolith_stone**——`shoggoth_monolith` 经 `neoforge:add_features` 挂 omothol 单固定 biome + gen 期实际生成，**特征挂接端到端实证**（omothol 默认块 omothol_stone、monolith_stone 可区分）。
- **移出本任务（已挪到应属阶段，诚实）**：①**carver 挂 biome**（`abyssal_cave`/`dreadlands_cave`/`abyssal_ravine`，configured_carver 已由 PG-4 备）——Forge **无** `add_carvers` biome_modifier，且 biome JSON `carvers` 字段 1.20.1（`{"air":[…]}` Map<Carving,HolderSet>）↔1.21（flat `[…]`，carving-step 已删）**分叉** → 挂接需 loader-specific carver modifier 或分叉 biome JSON，**待办**。②`darklands_tree` + 五个主世界 Darklands 已由 RR-ENTITY-CATALOG 后续工作补齐（见 §13）；`coralium_infested_swamp` 仍待 T5.8e。③spawn biome_modifier 双份已由 RR-ENTITY-CATALOG 补齐，实际自然生成统计仍待 T5.8d。

## 12. G2 传送门 teleport 框架（PG-6，T5.7，◐）

**范围**：跨维传送核心（1.12.2 `TeleporterAC`/portal 的现代化）。PG-6 的 teleport 框架已由 R4 扩展为玩家可达实现：Portal Anchor/BE、Gateway/Silver Keys、Portal Ritual、DimensionData 图、portal UUID 生命周期、目标同步与 renderer 均已落地；真人双端往返/重启仍由 R4-LIVE-GATE 验收。

- **`platform/TeleportCompat`**（portal 子系统**唯一深分叉**）：Mojang 1.21 重写传送 →
  - 1.20.1 Forge：`Entity.changeDimension(ServerLevel, ITeleporter)`，`ITeleporter.getPortalInfo` 返 `PortalInfo(pos, speed, yRot, xRot)` 落点。
  - 1.21 NeoForge：`Entity.changeDimension(DimensionTransition)`（record 载 dest level/pos/speed/rot + `DO_NOTHING` post-hook）。
  - 两端在**显式目标 pos** 放置（caller 算落点）、免 portal-frame 搜索、mob+player 通用（loader 亦 patch `ServerPlayer`）。
- **`world/portal/DimensionTeleport`**（1.12.2 `TeleporterAC` 落点数学现代化）：`src.dimensionType().coordinateScale() / dest…`（旧 `WorldProvider.getMovementFactor()`）缩放 XZ + world border clamp + `getHeight(MOTION_BLOCKING_NO_LEAVES)` 地表落点 + `getChunk` 强制生成落点 chunk，再交 `TeleportCompat`。
- **接线 `DimensionPortal`**（PD-6 placeholder → 功能）：`destination` `ResourceKey<Level>` **plain server 字段 + NBT `Destination`**（`ACRef.parse` fork-free；**不用 synched data** 避 `defineSynchedData` fork——客户端 portal 视觉是 Stage E，传送纯 server 侧）；`tick()` server 侧把 box 内非-portal 且 `!isOnPortalCooldown` 的实体交 `DimensionTeleport`；singleUse 变体传送后 `discard`。业务零 `//?`（fork 全封 `TeleportCompat`）。
- **验证**：两节点 `compileJava` BUILD SUCCESSFUL（1.21 `DimensionTransition` + 1.20.1 `ITeleporter`/`PortalInfo` fork 双端 resolve——本任务风险点）；**两节点 `runServer` 全 runtime**：`summon abyssalcraft:portal <pos> {Destination:"abyssalcraft:abyssal_wasteland"}`（NBT→`readAdditionalSaveData` 设 dest）+ 同位 tagged cow → portal tick 传送 → **cow 跨维 overworld→abyssal_wasteland 双端**（forge `FORGE_TELEPORT_OK` / neo `NEO_TELEPORT_OK`；forceload dest chunk 后 `execute in abyssalcraft:abyssal_wasteland run execute if entity @e[tag]` 命中——落点 chunk 若未 forceload 会随 chunk 卸载存盘、须 forceload 才在 live list）。
- **R4 后续事实（2026-07-26）**：上述 Anchor/BE、Key、Portal Ritual、DimensionData、synched target/unchained、目标 mob、Home spell 与客户端 renderer 已实现；永久 Gate=`RR_PORTAL_SELF_TEST_OK dimensions=7 edges=6 keyTiers=4`。旧自动黑曜石 frame 建造没有被虚构为新入口，现代链以显式 Anchor 为落点。尚未完成的是 Forge/Neo 真人玩家正反向、目标 Anchor、破坏清理与同世界停服重启矩阵。

## 13. 五 Darklands + TerraBlender（Agent C，T5.8c）

- **依赖与注册**：Forge 使用 TerraBlender `3.0.1.10`，NeoForge 使用 `4.1.0.8`；两份 mod metadata 均声明 required。`DarklandsWorldgenCompat` 在 common setup 注册 `abyssalcraft:darklands_overworld` Region 与 Overworld surface rules，权重由 `ACConfig.darklandsRegionWeight`（默认 2）控制。
- **五群系**：`DarklandsBiomes` 定义 `darklands`、`darklands_forest`、`darklands_plains`、`darklands_hills`、`darklands_mountains`。`DarklandsRegion` 为五者配置 temperature/humidity/continentalness/erosion/weirdness climate points；`DarklandsSurfaceRules` 给 mountains 暗石、hills 草土/暗石和基础三群系草土地表。
- **装饰与兼容**：三档 `darklands_tree_{base,forest,sparse}` placed feature 各有 Forge/Neo modifier。基础 Darklands 同时加入 Dreadlands multi-noise biome source；AW 不混入 Darklands，避免恢复旧版不清晰的 AW 随机混入怪癖。
- **验证**：Forge/Neo 全新专服均记录 Region 注册与 TerraBlender Overworld 初始化，五群系分别 `/locate biome` 成功；`execute in abyssalcraft:abyssal_wasteland run locate biome abyssalcraft:darklands` 双端失败（预期负测），Dreadlands 同命令双端成功。双节点完整 `build` 通过。
- **当时未完成（2026-07-24 历史状态）**：本节交付时第六群系尚未注册；该缺口已由 §14 的 RR-WORLD/T5.8e 补齐。自然刷怪权重统计与人工地形观感仍未完成。

## 14. RR-WORLD 现代混合保真收口（2026-07-25）

> 本节是 §8-§13 历史交付记录之上的最新事实源。旧段落中的 water/dead-log 占位、平实体 Omothol、未挂 carver、缺第六群系和“模板必须人工重存”等描述已经被本轮实现取代；保留旧段仅用于追溯。

### 14.1 已完成自动切片

- **四维地形**：AW 使用真实 Liquid Coralium 海并建立 `plateau > base > coralium_lake`；Dreadlands 建立 `mountains > base > ocean`、无露天 lava 海且低层洞腔可灌 lava；Omothol 使用 End 噪声形成悬空岛体；Dark Realm 由 `DarkRealmCavityMask` 以固定 seed `1251393890L` 提供 world-seed 无关洞腔。
- **材料与装饰**：Liquid Antimatter 完整 source/flowing/block/bucket 双加载器注册并恢复接触语义；两湖改真实流体；dead tree 改专属 `dead_tree_log` 和旧式枝根轮廓；三 configured carver 已按目标群系挂接。
- **第六群系**：`coralium_infested_swamp` 经 TerraBlender 注入主世界，含 antimatter 湖、两类 Coralium ore 与旧式植被；不加入 `is_darklands` tag，避免共享逻辑泄漏。
- **模板转换**：36 个 DataVersion 1343 模板无需人工结构方块重存。`scripts/convert_legacy_structures.js` 以 manifest + 54 个旧 AC palette ID 显式映射确定性转换，未知 ID 硬失败；另将 Ethaxium House 编译为第 37 模板。Forge 打包 `structures/legacy/*.nbt`，Neo 打包 `structure/legacy/*.nbt`。
- **结构运行时**：`LegacyTemplatePiece`/`LegacyStructureLayout` 放置 Graveyard、Shoggoth Pit、Omothol、Chagaroth、Jzahar 等模板布局并清理 DATA marker；House 与 Chains 已实现。Dreadlands Mineshaft/Abyssal Stronghold 采用现代 vanilla 拓扑基线，并通过只在对应 AC structure ID 活跃时生效的 Mixin 映射 Dreadwood/Abyssal palette 与 `chests/{mineshaft,stronghold_corridor,stronghold_crossing}`；原版结构不受影响。

### 14.2 自动验证证据

- 全部 `src/main/resources` 共 **494 JSON** 经 Node `JSON.parse`；双端 `compileJava` 成功。Neo `runData` 输出 `RR_WORLD_INVARIANT_OK cavityHash=e19b7f024699d36e samples=6`，并行采样一致。
- 生产 JAR：Forge **37 复数模板 / 0 单数模板 / 3 新结构 loot**；Neo **37 单数模板 / 0 复数模板 / 3 新结构 loot**；Mixin class 与关键 worldgen JSON 均入包。
- Neo 全新专服：第六群系定位 `[-240,93,-608]`；AW plateau `Y68`、lake `Y55`、16x16x6 区域含 912 Liquid Coralium；Dread mountains `Y79`、ocean `Y64`、海平面 lava=0；Omothol `Y10` 空气且同列岛面 `Y185`/`Y184` Omothol Stone；Dark Realm 固定层洞腔成立。
- Neo 结构：Chains、Ethaxium House、Omothol City、Chagaroth Lair、Mineshaft、Stronghold 均生成；Mineshaft/Stronghold 可自然 locate。Mineshaft 实测 **109 Dreadwood** 且最近矿车箱为 `abyssalcraft:chests/mineshaft`；Stronghold 实测 **1201 Abyssal Stone Brick**；AC corridor loot 可加载。停服后同世界重启 `Done (2.081s)`，House 区仍有 **423 Ethaxium Brick**。
- Forge 新 Mixin 抽查：Mineshaft 实测 **46 Dreadwood** 且最近矿车箱为 AC mineshaft loot；Stronghold 实测 **1958 Abyssal Stone Brick**。两服均干净 stop 并保存所有维度。
- `/place` vanilla Mineshaft/Stronghold 会输出 `Unprimed heightmap`/`mark a block for PostProcessing` 诊断，但命令最终成功；自然 `/locate` 与新区块生成路径不依赖该调试命令的未完成 chunk 状态。

### 14.3 显式未完成

- **T5.4c**：固定 seed/算法/hash 已证，不等于与 1.12.2 旧噪声逐位相同；取得旧生成器 oracle 后再做逐位对照。
- **T5.6c**：crate/spawner/pedestal/lock/biomass/ooze 等缺宿主内容仍是确定性 marker/稳定替代；须由对应内容任务接入真实玩法。模板旋转、拼缝以及 Mineshaft/Stronghold 旧 piece 图仍需 fixture/人工对账。
- **T5.2c/T5.3c/T5.4c/T5.9b**：四维地形与结构的双端 spectator 视觉/性能矩阵未执行；自然刷怪统计和玩家正常 portal 链仍分别归 T5.8d/T5.7b。自动服务器证据不能替代这些验收。

## 15. 自动验证矩阵（RR-WORLD-FIDELITY-AUTO）

### 15.1 当前工具

- `WorldgenPerformanceSampler`：在真实 `ServerLevel` 上采样 AW/Dreadlands 固定路线的 p50/p95。
- `DarkRealmNoiseOracle`：比较 Dark Realm 固定采样点与 1.12.2 oracle；没有基线时明确返回未执行原因，不算通过。
- `StructureFixtureValidator`：逐个读取当前节点 classpath 中的 37 个 legacy `.nbt`。
- `EntitySpawnStatistics`：读取真实 biome 注册表并统计 AC spawn 条目。
- `WorldgenFinalMatrix`：datagen 入口执行静态可用项，server 入口执行需要 `ServerLevel` 的项目；任何 `FAIL` 均使门禁失败。

### 15.2 结构 Fixture 验证（T5.6c）

模板清单直接使用实际资源相对路径，包含 `omothol/ethaxium_house`，不再维护与资源名不一致的逻辑名称数组。Forge 从 `data/abyssalcraft/structures/legacy/` 读取，NeoForge 从 `data/abyssalcraft/structure/legacy/` 读取。

每个模板均通过 Minecraft `NbtIo` 解压为 `CompoundTag`，并验证：

1. 根 NBT 可解析，且 `size`、`palette`、`blocks` 类型正确。
2. `size` 为三个正整数，`palette` 与 `blocks` 非空。
3. 每个 palette 条目含字符串 `Name`，可解析为 `ResourceLocation`，并存在于 `BuiltInRegistries.BLOCK`。
4. 每个 block 含整数 `state`，且索引落在 palette 范围内。
5. structure block 的 `metadata`（兼容旧 `name`）可由 `LegacyTemplatePiece` 的动态 marker 分支判读。
6. 方块实体 NBT 中的 `LootTable` 可解析为合法 `ResourceLocation`。

缺资源、压缩流损坏、字段缺失或类型错误、未注册 block、越界 state、未知 marker、非法 loot 均立即返回带模板路径和具体原因的 `RR_WORLD_FIXTURE_FAIL`，不存在忽略异常或只读首字节的成功路径。

成功输出包含实际审计计数：

```text
RR_WORLD_FIXTURE_OK templates=37 procedural=2 paletteEntries=<n> blocks=<n> markers=<n> lootRefs=<n>
```

旋转、拼缝和整体视觉仍属于 U-WORLD 人工验收；这不降低 NBT、palette、marker 与 loot 审计的硬失败语义。

### 15.3 门禁语义与执行

`WorldgenValidationData` 已在 datagen 注册并调用 `WorldgenFinalMatrix.executeMatrix()`。矩阵只有在全部要求项完成且没有未执行项时才输出 `PASS`；存在外部基线或 server context 缺口时输出 `FAIL`，不再使用“带阻塞项也通过”的表述。

双节点最窄验证命令：

```powershell
.\gradlew.bat :1.20.1-forge:compileJava :1.21.1-neoforge:compileJava --rerun-tasks
.\gradlew.bat :1.20.1-forge:runData
.\gradlew.bat :1.21.1-neoforge:runData
```

性能与 spawn 采样必须通过 `WorldgenFinalMatrix.executeServerMatrix(...)` 在真实 server level 上执行。Dark Realm 逐位旧版保真仍以 `scripts/capture_dark_realm_oracle.js` 取得的 1.12.2 基线为前置证据；缺少该证据时不得写成通过。

### 15.4 与 U-WORLD 的边界

自动验证负责 NBT/palette/marker/loot 完整性、固定点噪声对照、性能数值和 spawn data。U-WORLD 负责四维视觉、结构旋转与拼缝、天空/雾、实际自然刷怪体验、玩家 Portal 往返和长时生态。自动检查失败时不进入人工验收；自动检查通过也不能替代这些人工项目。

## 修订日志

- 2026-07-25：RR-WORLD 自动切片收口（§14）：真实世界材料/carver、四维现代混合地形、第六群系、36+House 模板转换与布局、Chains、Mineshaft/Stronghold AC palette+loot 通过双端 compile/runData/production JAR/runServer 与 Neo 重启矩阵。将旧噪声 oracle、动态 marker 真实玩法和人工视觉拆为独立未完成任务。
- 2026-07-24：Agent C 补齐五 Darklands（§13）：required TerraBlender 双版本依赖、Overworld Region、surface rules、五 biome、三档树特征与 Dreadlands 基础 Darklands；双端五 `/locate`、AW 负测、Dreadlands 正测和完整 build 通过。`coralium_infested_swamp`、自然刷怪统计与地形观感仍显式待办。
- 2026-07-22：PG-6 交付传送门 teleport 框架（§12，◐）：`platform/TeleportCompat`（跨维 `changeDimension` fork 1.20.1 `ITeleporter`/`PortalInfo` ↔ 1.21 `DimensionTransition`）+ `world/portal/DimensionTeleport`（coordinateScale 缩放 + border clamp + 地表落点）+ 接线 PD-6 `DimensionPortal`（destination server 字段+NBT、tick 传送、singleUse discard；避 synched-data fork）。两节点 `compileJava` + `runServer`：`summon portal{Destination:AW}`+cow → cow 跨维 overworld→abyssal_wasteland 双端（FORGE/NEO_TELEPORT_OK）。portal_anchor 方块+激活 ritual（PS-6）/gateway key/目标 mob 刷怪/portal-frame/home·dark-realm teleporter/渲染延后。见平行表 CR-51。

- 2026-07-22：PG-7 交付群系特征放置（§11，T5.8/T5.9）：14 feature biome_modifier（7 feature × forge/neoforge `{forge,neoforge}:add_features`）挂 PG-4 placed_feature 到各维度 biome（abyssal_stalagmite→AW / lake_liquid_coralium+dead_tree→abyssal_swamp / dreadlands_stalagmite→dreadlands / dreadlands_tree→dreadlands_forest / shoggoth_monolith→omothol / lake_liquid_antimatter→dark_realm，忠实 §4 ID）。纯数据包。forge/neo 均 `Done` 零 biome_modifier 加载错 + neo omothol 100chunk 采样 monolith_stone×12（shoggoth_monolith gen 期实证）。当时移出的 darklands_tree/主世界 Darklands 待办已由 2026-07-24 §13 补齐；carver 挂 biome 仍待完成。T5.9 worldgen 验证收口：4 维度进入+地形+locate biome（PG-1..3）、结构 place+locate+chest loot（PG-5）、特征 place+omothol gen（PG-4/7）、teleport cow overworld→AW（PG-6）双端零崩。见平行表 CR-50。

- 2026-07-22：PG-5 交付程序化结构子集（§10）：3 kind（GRAVEYARD/ABYRUIN/DARK_SHRINE）`StructureType` + 15 JSON（3 structure/3 structure_set random_spread/3 has_structure biome tag/6 loot table 双目录）+ `platform/StructureCompat`（2 fork：`structureType` MapCodec↔Codec + `setChestLoot` `ResourceLocation`↔`ResourceKey<LootTable>`）+ `world/structure/{StructureKind,ACStructure,ACStructurePiece}`（floor+角柱+loot 箱；load-ctor SAM=`StructurePieceSerializationContext`）+ relay `ModRegistries.ALL` +2。两节点 `compileJava` + `runServer` `Done`、`/place structure` 建箱体双端、`data get block LootTable`=`abyssalcraft:chests/<kind>`（forge dark_shrine/neo graveyard 证 setChestLoot 分叉双端）、`/locate structure` AW 自然生成双端（forge graveyard 160/neo abyruin 441 blocks）。**移出本任务（不阻塞收口）**：36 二进制 `.nbt` jigsaw（游戏内格式重存+调色板重映射，非 headless）+ 其余程序化结构 → **T5.6b**；箱 AC 专属 loot 物品 → **PK-5**；has_structure biome 交叉引用 → **Gate G1/PG-7**。见平行表 CR-48。

- 2026-07-22：PG-4 交付特征（§9）：3 代码 `Feature`（`world/feature/{Stalagmite,Monolith,DeadTree}Feature`，fork-free）+ 29 JSON（8 configured_feature + 8 placed_feature + 3 configured_carver + 2 additive `overworld_carver_replaceables` tag）+ G0 `ModWorldgen.FEATURES` +3 register（无 relay/main/lang）。石笋/monolith 自定义 `Feature<BlockStateConfiguration>`；树/湖=`minecraft:tree`/`minecraft:lake` 数据驱动（lake=water 占位待流体、dead_tree_log 占位 darklands_oak_log）；carver 用纯 float 避 value-wrapper 分叉；worldgen 单目录。两节点 `compileJava` + `runServer` `Done` 零加载错、forge `/place feature abyssal_stalagmite`→abyssal_stone、neo `shoggoth_monolith`→monolith_stone + `darklands_tree`→darklands_oak_log。特征挂 biome features[]/carver 挂 carvers[]=Gate G1（biome 归 PG-1/2/3）。见平行表 CR-47。

- 2026-07-22：PG-1 交付 Abyssal Wasteland（§8.3）：5 群系维度（8 JSON、无 Java/relay/main/lang），用 `minecraft:multi_noise` biome source 分布 5 群系（编造 temp/humidity climate 带逼近旧 `GenLayerBiomesAW` 均匀分布，noise_router inline `shifted_noise` 驱动 temperature/vegetation）；忠实 abyssal_stone 基岩 / sea water@55(忠实 liquid_coralium 待流体) / noise-hilly / biome-cond surface_rule(plateau coralium_stone / wastelands·swamp fused_abyssal_sand+abyssal_sand / 余 abyssal_sand) / 干维度暗天(ambient 0.25/fixed_time 18000/bed false)；`monster_spawn_light_level` 纯 int（承 §5）。两节点 `runServer` 均 `Done`、`/execute in abyssalcraft:abyssal_wasteland` 进入、**5 群系全 `locate biome` 命中**（forge 全 5 / neo plateau 32 = 分布双端成立）、abyssal_stone 地形 + surface 双端（abyssal_sand 1111 / wastelands fused_abyssal_sand 256=1柱 / neo plateau coralium_stone 542）。sea 流体 / 特征(PG-4) / 结构(PG-5) / 刷怪(D2a·PL-4) / per-biome 高度差 / 天空雾 / 精确观感人工目视延后；3 darklands-in-AW 怪癖不移植。

- 2026-07-22：PG-2 交付 Dreadlands（§8.2）：4 群系维度（7 JSON、无 Java），用 `minecraft:multi_noise` biome source 分布 4 群系（continentalness 三段 + humidity 分 forest；noise_router climate 引 `overworld/{continents,erosion,ridges}` + inline `shifted_noise`，避全 0 塌缩）；忠实 dreadstone 基岩 / 无海 lava / noise-hilly(`overworld/base_3d_noise`) / surface_rule 按 biome 换块 / dread 红观感 / nether-like；`monster_spawn_light_level` 纯 int（承 §5）。两节点 `runServer` 均 `Done`、`/execute in abyssalcraft:dreadlands` 进入、**4 群系全 `locate biome` 命中双端**（群系分布双端成立）、dreadstone 地形双端。特征/结构/刷怪/逐 biome 高度差/darklands 第 5 群系/天空雾延后。

- 2026-07-22：PG-3 交付 Omothol + Dark Realm（§8.1）：2 简单纯数据包维度（8 JSON、无 Java），忠实换块 omothol_stone/darkstone + 暗天（`the_end`/`has_skylight:false`）+ 单群系 `fixed` + `monster_spawn_light_level` 纯 int（承 §5）。两节点 `runServer` 抵 `Done`、forge 全 runtime（两维度 `execute in` + 地形 + locate biome 0）、neo `Done` + omothol chunk-gen crashless（余探针据 G0 同机制 + forge 全探针，诚实）。结构/刷怪/天空雾/浮空岛真形延后。

- 2026-07-22：PG-0 交付 Stage G0（§3-§7）：框架 `world/ACDimensions`※ + `registry/ModWorldgen` + `world/feature/MiniPillarFeature`、`abyssalcraft:mini` 6-JSON 竖切、R1/R2/R3 结论 + worldgen ID 冻结（平行表 §5）。两节点 `compileJava`+`runServer` 实证（`/execute in` 生成地形 + locate biome 0 + 特征 8 glowstone 双端一致）。修 `monster_spawn_light_level` IntProvider 1.20.1↔1.21 分叉（纯 int）——顺解 PS-1/CR-38 记录的 neo runServer 崩。解锁 Stage G1。
