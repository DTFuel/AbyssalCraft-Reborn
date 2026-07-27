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
| 实体（目录/专属行为/战利品/自然刷怪） | M3 / R4 | [entity-subsystem.md](entity-subsystem.md) | M3-CONTENT 完成：63内容类型/48蛋/44 placement；全族行为、69→97现代loot、双端11场真实自然生成、69死亡路径与重启持久化矩阵通过 |
| GeckoLib 模型移植（骨骼模型 · 转换脚本 · 双加载器） | Stage E / PE-4 | [geckolib-model-porting.md](geckolib-model-porting.md) | forge Chagaroth **目视验证忠实**（静态无动画）；两节点 compile 绿；neoforge runClient 未目视 |
| 网络（多路复用通道 + 23 消息双实现） | M7 / Stage S-A | [network-subsystem.md](network-subsystem.md) | 框架+23消息序列化已验证；RR-KNOWLEDGE将通道升v2并完成5条knowledge/necrodata handler，其余handler随所属系统 |
| 能力 / 玩家数据（necrodata + 物品转移） | M7 / Stage S-A | [capability-subsystem.md](capability-subsystem.md) | RR-KNOWLEDGE 完成 Forge capability/Neo attachment、mutation变化检测、协议v2增量/全量handler与配置延迟同步；真人死亡/重连实网矩阵留T7.2c；item transfer由PC-4覆盖 |
| 附魔（5：1.20 类 / 1.21 数据驱动） | M7 / Stage S-A | [enchantment-subsystem.md](enchantment-subsystem.md) | PS-3 交付 5 附魔双端可获得（`EnchantmentCompat` 1.20 类/1.21 datapack JSON + `ACEnchantments` ResourceKey）；两节点 `/enchant` 探针实证；效果延 PS-11 event hook，multi_rend/sapping 待 Staff |
| 药水 / 效果（5 MobEffect + 7 药水） | M7 / Stage S-A | [potion-subsystem.md](potion-subsystem.md) | 三DamageType、传播/carrier/immunity、解毒、转化/Purge、动态Dreadlands、专属宿主与6 mix完成；永久Dread Plague自测已接 |
| 势能 / 能量（PE 网络框架） | M7 / Stage S-B | [energy-subsystem.md](energy-subsystem.md) | PS-5 交付能量框架（能量 BE 接口 + `PEUtils` 算术/传输，fork-free 同 PC-4）；两节点编译 + selfTest 双端 PASS；能量方块/idol/charm/采集链 = 内容，待未移植块/物品/Necronomicon |
| 仪式（Necronomicon 仪式） | M7 / R4 | [ritual-subsystem.md](ritual-subsystem.md) | 62 注册项（40 infusion/3 creation/1 transformation/18 specialized）、持久 ceremony、18/18 行为、活祭/research/PE/disruption与客户端反馈完成；双端 Gate=`rituals=62 handlers=18`，真人视觉听觉留 R4-LIVE-GATE |
| 法术（Necronomicon 法术） | M7 / R4 | [spell-subsystem.md](spell-subsystem.md) | 14 manifest/behavior、六卷轴、即时/蓄力、多容器PE、MobSpell服务端重验、7槽Spellbook与旧铭文overlay完成；双端 Gate=`spells=14 handlers=14 spellbook=14`、资源=`itemModels=29` |
| 知识 / 死灵之书（研究/解锁条件） | M7 / Stage S-B | [knowledge-subsystem.md](knowledge-subsystem.md) | RR-KNOWLEDGE 完成42 research/42 conditions（33+9）/11 offerings、type5/6、核心触发、协议v2与五分类研究目录；双端永久Gate=`42/42/11`；完整旧页面/actions和真实page内容留T7.8c |
| 扰动（PE 扰动框架） | M7 / Stage S-C | [disruption-subsystem.md](disruption-subsystem.md) | PS-9 交付扰动框架（`Disruption` 基/`DisruptionHandler` deity 过滤随机/`PotionDisruption`+`LightningDisruption` pilot）读 PS-5 `DeityType`；两节点编译 + selfTest 双端 PASS；~27 具体扰动 + deity-image 检测 + 网络反馈 = 内容，待未移植实体/能量块 |
| Places of Power（PoP 多块框架） | M7 / Stage S-C | [places-of-power-subsystem.md](places-of-power-subsystem.md) | PS-10 交付 PoP 框架（`IPlaceOfPower`/`IStructureComponent`/`IStructureBase`/`StructureHandler` bookType 门控/`SimplePlaceOfPower` pilot）读 PS-5 `AmplifierType`；两节点编译 + selfTest 双端 PASS；`researchId` 解耦 PS-8；具体 PoP 多块 + construct/renderData + BE 实现 = 内容，待未移植能量块/statue |
| 存档 / 事件钩子（Necromancy SavedData + 知识钩子） | M7 / Stage S-C | [saveddata-hooks-subsystem.md](saveddata-hooks-subsystem.md) | RR-KNOWLEDGE 完成命名非Boss死亡快照生产、≤20/尺寸/NBT真实落盘重启、biome/plague/Purge/mutation hooks；复活仪式消费与page/附魔hooks留T7.11c |
| 死灵之书 GUI（Necronomicon 书界面） | M6 / Stage H2 | [necronomicon-gui-subsystem.md](necronomicon-gui-subsystem.md) | 五本书、常驻信息与42项五分类研究目录/状态/hint已接入；完整旧页面/actions/贴图布局和双端真人目视留T7.8c/T6.2b |
| HUD / 字体 / clientvars（客户端显示框架） | M6 / Stage H2 | [hud-font-clientvars-subsystem.md](hud-font-clientvars-subsystem.md) | PH-6 交付 HUD/字体/clientvars 框架（`AkloFont`+aklo.json / `ClientVars`+`ClientVarsManager` 热重载 / `platform/ClientHooksCompat` HUD overlay+reload fork[javap 双 jar 核] + pilot）；两节点编译 + runClient 抵标题屏；有 CR（platform + 主类 client block）；HUD 内容 + Aklo 位图 + 全 clientvars 色 + keybind = 内容/人工 |
| 客户端渲染（天空/雾 + 粒子 + 音效） | M6 / R5 | [client-rendering-subsystem.md](client-rendering-subsystem.md) | RR-CLIENT-FX(CR-73) 实现四维 tinted 天空盒（`DimensionSkyCompat`）、BlueFlame+ItemRitual 仪式粒子、shoggoth.step 与字幕闭包；PEStream 由 RR-NET。双端 compile/runData `RR_CLIENT_FX_SELF_TEST_OK`/build/JAR。四维天空目视(T6.3c)与声音/字幕矩阵(T6.5c)=人工待验 |
| 世界生成（4 维度 + 群系 + 特征 + 结构 + 传送门） | M5 / Stage G | [worldgen-subsystem.md](worldgen-subsystem.md) | PG-0/G0 交付框架（`ACDimensions`※/`ModWorldgen`/示例 `Feature`）+ `abyssalcraft:mini` 竖切 + R1/R2/R3 + ID 冻结（§5）；PG-3/G1 交付 Omothol + Dark Realm 2 维度（纯数据包 8 JSON，两节点 `runServer`）；余 G1（PG-1/2/4/5）/G2 规划中 |
| Portal（Gateway Key / Anchor / 维度门） | M5+M7 / R4 | [portal-subsystem.md](portal-subsystem.md) | 七维六边连通图、三级 Key+Silver、Anchor/BE、Portal Ritual、同步 renderer 与持久关联完成；自动 Gate=`dimensions=7 edges=6 keyTiers=4`，真人双端往返/重启留 R4-LIVE-GATE |
| 集成（JEI/配置/命令/进度 + IMC/插件 API） | M8 / Stage J | [integration-subsystem.md](integration-subsystem.md) | RR-ADV-API完成命令实网、9进度独立书知识、双端ServiceLoader API与Forge五项旧IMC；4 JEI基线与配置解析存在，剩余JEI分类/转移和配置消费者/GUI待办 |
