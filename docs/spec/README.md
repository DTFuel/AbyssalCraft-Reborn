# 子系统详细规格 (Subsystem Specs) — 索引

> 每个子系统的**详细设计 / 子系统内契约 / 实现记忆 / 踩坑 / 逐项验证细节**写在本目录下，**一子系统一文件** `docs/spec/<子系统>.md`。
> 目的：让主文档（[设计案](../porting/00-porting-design.md) / [总任务表](../porting/01-porting-task-plan.md) / [平行任务表](../porting/02-porting-parallel-tasks.md)）与仓库记忆（仓库根 `DEVELOPMENT.md`、`/memories/repo/development.md`）**保持精简**——只留必要信息 + 指向这里的索引，避免子系统细节把主文档撑爆或彼此漂移。

## 分工：什么写哪里

| 内容 | 去处 |
|---|---|
| 里程碑/阶段任务勾选、验收标准、文件归属、Gate 状态 | 主文档（01 / 02） |
| 高层设计原则、跨子系统共享契约（02 §2 冻结面）、总体架构 | 主文档（00 / 02 §2） |
| 一句话进度 + 变更日志条目（**指向 spec**，不复制细节） | 仓库记忆（`DEVELOPMENT.md` §7/§9） |
| 子系统的类/接口/数据流设计、包结构 | **本目录 `<子系统>.md`** |
| 子系统内契约（注册名/ID、i18n/网络/能力 key、对外 API） | **本目录 `<子系统>.md`** |
| 该子系统的实现记忆、踩坑（一手证据）、javap 核实结论、`//?` 分叉点 | **本目录 `<子系统>.md`** |
| 该子系统逐节点 build/run 的验证细节与判据 | **本目录 `<子系统>.md`** |

## 新增一个子系统 spec

1. 复制 [`_TEMPLATE.md`](_TEMPLATE.md) 为 `docs/spec/<子系统>.md`（小写连字符，如 `machine-subsystem.md`、`dimensions.md`、`entities.md`）。
2. 填写各节。
3. 在下方**索引表**加一行。
4. 在相关主文档处只留一行指针：「详见 `docs/spec/<子系统>.md`」，**不复制细节**。

## 索引

| 子系统 | 里程碑 / Stage | spec 文件 | 状态 |
|---|---|---|---|
| 合成机器与菜单宿主（结晶器/物质化器/嬗变器/研究桌/顺序酿造） | MP / Stage P + R2 | [machine-subsystem.md](machine-subsystem.md) | RR-MACHINE 全切片与 RR-MENU-HOST Research/Brewing 已双端真实客户端/服务端验证 |
| 物品转移（Spirit Tablet / 全 BE 附件 / Spirit Altar） | M2 / R2 | [item-transfer-subsystem.md](item-transfer-subsystem.md) | T2.8c/T2.9b 完成；双端重启、真实菜单/按钮/主副手与调度矩阵通过 |
| 物品内容（食物/杂项/材料/工具/护甲…） | M1 / Stage B | [item-content.md](item-content.md) | 进行中（PB-2 食物·杂项双端 `runServer` 验证；余 PB-* 待办） |
| 材料/晶体物品（PB-1，含晶体染色 `ClientColorCompat`） | M1 / Stage B1 | [materials-subsystem.md](materials-subsystem.md) | PB-1 交付并**两节点验证**（117 物品 build+runClient 零告警） |
| 实体（注册/属性/基类 + AI/寻路框架） | M3 / Stage D1 | [entity-subsystem.md](entity-subsystem.md) | PD-1 框架 + `pilot_mob` 示例双端验证（compile + forge runServer `/summon` Health 20.0f）；PD-2 AI/寻路框架；具体实体族延 D2a |
| GeckoLib 模型移植（骨骼模型 · 转换脚本 · 双加载器） | Stage E / PE-4 | [geckolib-model-porting.md](geckolib-model-porting.md) | forge Chagaroth **目视验证忠实**（静态无动画）；两节点 compile 绿；neoforge runClient 未目视 |
| 网络（多路复用通道 + 23 消息双实现） | M7 / Stage S-A | [network-subsystem.md](network-subsystem.md) | 框架+23消息序列化已验证；RR-KNOWLEDGE将通道升v2并完成5条knowledge/necrodata handler，其余handler随所属系统 |
| 能力 / 玩家数据（necrodata + 物品转移） | M7 / Stage S-A | [capability-subsystem.md](capability-subsystem.md) | RR-KNOWLEDGE 完成 Forge capability/Neo attachment、mutation变化检测、协议v2增量/全量handler与配置延迟同步；真人死亡/重连实网矩阵留T7.2c；item transfer由PC-4覆盖 |
| 附魔（5：1.20 类 / 1.21 数据驱动） | M7 / Stage S-A | [enchantment-subsystem.md](enchantment-subsystem.md) | PS-3 交付 5 附魔双端可获得（`EnchantmentCompat` 1.20 类/1.21 datapack JSON + `ACEnchantments` ResourceKey）；两节点 `/enchant` 探针实证；效果延 PS-11 event hook，multi_rend/sapping 待 Staff |
| 药水 / 效果（5 MobEffect + 7 药水） | M7 / Stage S-A | [potion-subsystem.md](potion-subsystem.md) | RR-KNOWLEDGE完成三DamageType、传播/carrier/immunity/两解毒/唯一转化/Purge、食物与6 mix；双端专服关键行为通过；动态Dreadlands与剩余宿主留T7.10c |
| 势能 / 能量（PE 网络框架） | M7 / Stage S-B | [energy-subsystem.md](energy-subsystem.md) | PS-5 交付能量框架（能量 BE 接口 + `PEUtils` 算术/传输，fork-free 同 PC-4）；两节点编译 + selfTest 双端 PASS；能量方块/idol/charm/采集链 = 内容，待未移植块/物品/Necronomicon |
| 仪式（Necronomicon 仪式框架） | M7 / Stage S-B | [ritual-subsystem.md](ritual-subsystem.md) | PS-6 交付仪式框架（`Ritual` 基/`RitualRegistry`/`InfusionRitual` pilot，order-free offering match + book/dim 门控）；两节点编译 + selfTest 双端 PASS；`IResearchable` 门控解耦为 `researchId`（PS-8 读）；altar/pedestal 块 + 13 具体仪式 + 产物 = 内容，待未移植块/物品/实体/Necronomicon |
| 法术（Necronomicon 法术框架） | M7 / Stage S-B | [spell-subsystem.md](spell-subsystem.md) | PS-7 交付法术框架（`Spell` 基/`SpellRegistry`/`ScrollType`/`IScroll`/`EntityTargetSpell`+`LifeDrainSpell` pilot，order-free reagent match + bookTier/scrollType 门控）；两节点编译 + selfTest 双端 PASS；`IResearchable` 门控解耦为 `researchId`（PS-8 读）；`SpellUtils`（PE 自能量物品 + NBT + raytrace）+ 14 法术 + scroll/staff/spellbook GUI = 内容，待未移植试剂/物品/内部 handler |
| 知识 / 死灵之书（研究/解锁条件） | M7 / Stage S-B | [knowledge-subsystem.md](knowledge-subsystem.md) | RR-KNOWLEDGE 完成42 research/42 conditions（33+9）/11 offerings、type5/6、核心触发、协议v2与五分类研究目录；双端永久Gate=`42/42/11`；完整旧页面/actions和真实page内容留T7.8c |
| 扰动（PE 扰动框架） | M7 / Stage S-C | [disruption-subsystem.md](disruption-subsystem.md) | PS-9 交付扰动框架（`Disruption` 基/`DisruptionHandler` deity 过滤随机/`PotionDisruption`+`LightningDisruption` pilot）读 PS-5 `DeityType`；两节点编译 + selfTest 双端 PASS；~27 具体扰动 + deity-image 检测 + 网络反馈 = 内容，待未移植实体/能量块 |
| Places of Power（PoP 多块框架） | M7 / Stage S-C | [places-of-power-subsystem.md](places-of-power-subsystem.md) | PS-10 交付 PoP 框架（`IPlaceOfPower`/`IStructureComponent`/`IStructureBase`/`StructureHandler` bookType 门控/`SimplePlaceOfPower` pilot）读 PS-5 `AmplifierType`；两节点编译 + selfTest 双端 PASS；`researchId` 解耦 PS-8；具体 PoP 多块 + construct/renderData + BE 实现 = 内容，待未移植能量块/statue |
| 存档 / 事件钩子（Necromancy SavedData + 知识钩子） | M7 / Stage S-C | [saveddata-hooks-subsystem.md](saveddata-hooks-subsystem.md) | RR-KNOWLEDGE 完成命名非Boss死亡快照生产、≤20/尺寸/NBT真实落盘重启、biome/plague/Purge/mutation hooks；复活仪式消费与page/附魔hooks留T7.11c |
| 死灵之书 GUI（Necronomicon 书界面） | M6 / Stage H2 | [necronomicon-gui-subsystem.md](necronomicon-gui-subsystem.md) | 五本书、常驻信息与42项五分类研究目录/状态/hint已接入；完整旧页面/actions/贴图布局和双端真人目视留T7.8c/T6.2b |
| HUD / 字体 / clientvars（客户端显示框架） | M6 / Stage H2 | [hud-font-clientvars-subsystem.md](hud-font-clientvars-subsystem.md) | PH-6 交付 HUD/字体/clientvars 框架（`AkloFont`+aklo.json / `ClientVars`+`ClientVarsManager` 热重载 / `platform/ClientHooksCompat` HUD overlay+reload fork[javap 双 jar 核] + pilot）；两节点编译 + runClient 抵标题屏；有 CR（platform + 主类 client block）；HUD 内容 + Aklo 位图 + 全 clientvars 色 + keybind = 内容/人工 |
| 客户端渲染（天空/雾 + 粒子 + 音效） | M6 / Stage H1 | [client-rendering-subsystem.md](client-rendering-subsystem.md) | PH-2/3/4 交付（`ModSounds` 45 SoundEvent + sounds.json·106 ogg + 42 subtitle / `ACDimensionEffects` 4 维忠实雾色 + `DimensionEffectsCompat` fork / `ModParticles` abyssal_fx + `ACFadeParticle` + `ParticleCompat` fork）；两节点编译 + forge runClient 进世界干净退出（sounds.json·particle atlas·4 维 effects 绑定零错）；有 CR-52；天空盒渲染 + 带数据粒子（PEStream/ItemRitual 近似 vanilla dust/item）+ 物品容器屏幕延后；雾/天空/粒子观感 = 人工目视 |
| 世界生成（4 维度 + 群系 + 特征 + 结构 + 传送门） | M5 / Stage G | [worldgen-subsystem.md](worldgen-subsystem.md) | PG-0/G0 交付框架（`ACDimensions`※/`ModWorldgen`/示例 `Feature`）+ `abyssalcraft:mini` 竖切 + R1/R2/R3 + ID 冻结（§5）；PG-3/G1 交付 Omothol + Dark Realm 2 维度（纯数据包 8 JSON，两节点 `runServer`）；余 G1（PG-1/2/4/5）/G2 规划中 |
| 集成（JEI/配置/命令/进度 + IMC/插件 API） | M8 / Stage J | [integration-subsystem.md](integration-subsystem.md) | 130标量与RR-KNOWLEDGE非标量解析/reload均完成；命令/9进度/4 JEI基线存在。全配置消费者+GUI、真人命令、进度联动、IMC/API仍为独立待办 |
