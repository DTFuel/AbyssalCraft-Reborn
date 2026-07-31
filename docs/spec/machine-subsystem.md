# machine-subsystem 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：MP / Stage P（P1 框架 · P2 结晶器 · P3 物质化器 · P4 嬗变器 · P3 JEI 试点）
- 关联平行任务：RR-MACHINE（已完成）· RR-MENU-HOST-CORE/BREWING（已完成）· PP-1..PP-5（历史试点）
- 状态：RR-MACHINE 全切片、`RR-MENU-HOST-CORE`、`RR-MENU-HOST-BREWING` ☑
- 负责：Agent A（RR-MACHINE / R1 Gate）；GitHub Copilot（RR-MENU-HOST）；Agent 6 等（历史 PP 记录）
- 最后更新：2026-07-25

## 0A. RR-MENU-HOST 边界与完成证据（2026-07-25）

- Research Table 的本阶段交付是旧版 176×238 菜单布局、方块朝向/形状/亮度/粒子、专用 GUI 与双端实际交互；羽毛 BER 独立归 `T2.6c/RR-RENDER`。
- Sequential Brewing 本阶段恢复 8 槽约束、智能 shift-click、上/侧/下自动化、brewing hooks、容器余留物、进度 GUI 和连续两级酿造。
- 旧版 item-transfer capability 附着到所有非黑名单 BlockEntity，配置由 Spirit Tablet 写入，真正执行器是 Spirit Altar（每 20 tick），并非 State Transformer/Rending Pedestal 自驱。现代实现保持该语义：Forge capability、NeoForge BlockEntity attachment。
- 本阶段只交付 Spirit Tablet 的 5 槽过滤容器。Spellbook 归 `T2.8d/T7.7b`；State Transformer 与 Rending Pedestal 的完整机器分别归 `T2.9c/T2.9d`。
- R2 并行边界：本任务不修改 `client/render/block/**`；`ModRegistries`、`ModCreativeTabs`、`ACClientSetup` 与主类 bootstrap 只在 R2-Gate 串行接线。

### 0A.1 Research Table

- 旧 176×238 书页式背景、玩家库存 y=156/热栏 y=214、`FACING`、14.4 高碰撞形状、亮度 6 与火焰/烟粒子已恢复；知识浏览内容仍由知识子系统提供，不属于本菜单切片。
- Forge/NeoForge 真实联网客户端均打开生产 `ResearchTableMenu`/`ResearchTableScreen`（36 玩家槽），通过正常 `PICKUP` 网络包在两个库存槽间往返移动物品；实际截图确认资源非空、无裁切/重叠，并修正深色木纹上的标题对比度。

### 0A.2 Sequential Brewing

- 8 槽语义为药水 0–2、材料 3、燃料 4、传出 5–7；`WorldlyContainer` 上/侧/下槽面、专用 Slot/shift-click、Forge hook、容器余留物、输出堵塞与每 20t 朝 `FACING` 邻机传递均已落地。
- `PotionBrewingCompat` 在 Forge 联合 mod registry 与 vanilla `PotionBrewing` fallback，NeoForge 使用 `level.potionBrewing()`；两端水瓶→粗制→夜视连续两级真实专服测试均在 820 tick 内完成。
- 生产 Screen 显示燃料、400t 箭头和气泡；双端真实客户端以 shift-click 放入燃料/材料/水瓶，观察活动态后网络取出传出槽结果，日志断言 `fuel=true progress=true`，活动/完成截图目视通过。

### 0A.3 验证与边界

- `MenuHostSelfTest` 经双端 `runData` 输出 `RR_MENU_HOST_SELF_TEST_OK`，覆盖 Research 菜单、Brewing 槽路由和 Tablet 数据面；完整转移细节见 [item-transfer-subsystem.md](item-transfer-subsystem.md)。
- 双端生产 build/JAR 含三套 Screen、Tablet/Altar 类与目标 GUI 资源；临时活玩家夹具、命令和 JVM 属性入口已删除。`T2.6b/T2.7b` 已完成；羽毛 BER `T2.6c` 仍独立归 RR-RENDER。

## 0. RR-MACHINE 当前契约（权威）

> 本节覆盖下方 2026-07-21 的三槽试点描述。下方 §1–§8 保留为 PP/CR 历史证据，不再代表当前机器架构或完成度。

### 0.1 三类机器

- **Crystallizer**：4 个真实槽（input / fuel / primary output / secondary output）；完整 `CrystallizationRecipe` 为 input + 主/可选第二输出 + XP + time。两个输出必须同时可容纳才加工；默认 200 tick；按 recipe ID/输出槽记录可持久化 XP ledger。
- **Materializer**：2 个真实槽（Crystal Bag / Necronomicon）+ 菜单会话中的 18 个只读虚拟结果槽；没有燃料、进度、XP 或 550 个持久槽。旧 550 真义是 2 个真实槽 + 最多 548 个派生结果缓存，现代实现改为不限总数的 3×6 分页。制作在服务端按 recipe ID 重验并事务扣除 1–5 种 counted crystal。
- **Transmutator**：3 个真实槽（input / fuel / output）；完整 `TransmutationRecipe` 为 input + result + XP + time。默认 200 tick，拒绝任意物品和煤，仅接受专用燃料 resolver。

### 0.2 配方与注册

- `registry/ModRecipes` 是 crystallization/materialization/transmutation/anvil_forging/rending 五类配方的唯一注册 owner；三台机器不再自持 `ProcessingRecipe` alias。
- `platform/{CrystallizationRecipeSerializer,MaterializationRecipeSerializer,TransmutationRecipeSerializer}` 吸收 1.20 JSON/network 与 1.21 `MapCodec`/`StreamCodec` 分叉。
- `DataRecipeCompat.Entry` 保留跨版本 recipe ID；计时机用 ID 持久化 active recipe/XP ledger，Materializer 用 ID 授权制作，避免按输出反查错配。
- `MachineRecipeData` 每次同时生成 `recipes/`+`result.item` 与 `recipe/`+`result.id`，现有 7+9+11 条示例均已迁为完整 schema。

### 0.3 库存、持久化与自动化

- `MachineBlockEntity` 支持可配置槽数，保存 schema、items、progress/max、burn/max、active recipe 与每输出槽 XP ledger；配方变化会重置 progress。
- Crystallizer/Transmutator 实现 `WorldlyContainer`：上方输入、侧面燃料、下方输出与容器余留物；Forge BE capability 与 NeoForge block capability 由 compat 层暴露同一 sided 语义。
- 三机器使用 `FACING`；两计时机另有 `LIT`。破坏掉真实库存，支持比较器。Materializer 旧三槽 pilot 存档中的非法 bag/book/fuel/output 进入 `PendingMigration`，破坏时安全掉落。
- 四级 Crystal Bag（18/36/54/72）使用 1.20 NBT / 1.21 custom-data 完整 `ItemStack` 列表；仅 crystal/shard/fragment 可保存，升级容量按当前 Bag 等级扩展，失败消费不修改原数据。

### 0.4 已验证证据（更新至 2026-07-24）

- Forge 1.20.1 与 NeoForge 1.21.1：`compileJava`、`runData`（`RR_MACHINE_SELF_TEST_OK` / `R1_CONTENT_SELF_TEST_OK`）与同一 invocation 全量 `build` 均通过。
- `MachineSelfTest` 直接执行生产菜单的 fuel/Bag/book shift-click、Bag 宿主槽锁定、结果槽 XP ledger 单次消费、自动提取 ledger 清理、槽面映射与 NBT 重载。
- Forge 1.20.1 与 NeoForge 1.21.1 真实联网玩家均打开实际 Bag/三机器 Menu 与 Screen，并通过正常容器网络包完成：普通点击放入、shift-click 路由、Materializer 虚拟结果制作、Crystallizer 双输出取出、Transmutator 容器余留物和玩家 XP。
- Bag 两端均验证 54 槽 Screen、宿主热栏槽锁定、普通放入 2 + shift-click 3，关闭重开后服务端和客户端均为 5。
- Crystallizer 两端均验证 diamond×10 普通放入、blaze powder×2 shift-click、`progress=0.005` / `burn=1.0`、双输出各 10、网络取出后玩家 `xp=2`；Materializer 验证 Bag 普通放入、Book shift-click、五晶体结果位于 slot 6 并经网络制作；Transmutator 验证 gravel×10、Liquid Coralium bucket shift-click、20 flint、空桶返还、`xp=4` 与工作态视觉。
- NeoForge 加工中断线/卸载/停服重启：重启前后均为 `Progress=129`、`BurnTime=672`、diamond×56、blaze rod×63、双输出各 8，完整双输出 XP ledger 逐字段一致；重开 Screen 精确同步 `0.645` / `0.28`。
- Forge 加工中断线/卸载/停服重启：重启前后均为 `Progress=80`、`BurnTime=2121`、diamond×63、blaze rod×63、双输出各 1，两个输出槽各一条 XP ledger 逐字段一致；重开 Screen 精确同步 `0.4` / `0.88375`。
- 活会话使用显式 JVM 属性门控的真实 ServerPlayer/Menu/Screen/容器网络验证夹具；矩阵通过后已删除夹具、命令和 Screen hook。发布源码中 `R1LiveValidation` / `R1ClientValidation` / `acr1validate` / 验证属性引用均为 0。
- 两端 `runServer` 抵 `Done`；NeoForge 加载 1382 recipes，完整双输出和 5 输入 codec 无解析错误；Forge 的 RR-MACHINE 静态 pilot 无解析错误（Forge dev generated source-set gap 仍使总数仅 12）。发布清理后双端 `runData`、同一 invocation `build` 与无验证属性 dedicated-server `Done`/干净 stop 再次通过。
- 两端客户端资源 smoke 中 RR-MACHINE 三方块/四 Bag/三 Screen/Menu 相关 warning/error 为 0。日志中的护甲、书和其他 item missing-model 为既存非本任务资产缺口。

### 0.5 后续范围（不属于 R1 Gate）

- JEI 两燃料分类、完整 transfer/click area（`TP.5b`），忠实 front/side/top/active 贴图与全量旧 223 项配方（`TP.6b/T2.10b`）。

### 0.6 RR-MACHINE-CONTENT 已完成（2026-07-24）

- 四级 Crystal Bag 已有真实右键 `use` 入口、18/36/54/72 专用菜单与 Screen、仅晶体槽、宿主热栏槽锁定、shift-click 和服务端持久化。
- Liquid Coralium 已有 source/flowing/block/bucket、双 loader bucket capability、still/flowing 资源与无几何流体 block model；Transmutation Gem 是 10 次可复用燃料。
- 26 crystal cluster 已注册、染色/命名、loot/tag/model 完整，并生成 52 条双版本压缩/拆解配方；Neo 服务器 recipes 从 1330 增至 1382。

### 0.7 R6 RR-DATAGEN-MACHINE（2026-07-27）

- 冻结源为 `docs/AbyssalCraft-1.12.2/.../common/AbyssalCrafting.java`：crystallization 109、transmutation 46、materialization 68，合计精确 223 条调用。`LegacyMachineRecipeSource` 保留每条原调用、源行、现代输入/输出与数量；`LegacyMachineRecipeCatalog` 为每条分配 `MIGRATED` / `REPLACED` / `BLOCKED` / `RETIRED`，四态总和必须为 223。
- `MIGRATED` 与 `REPLACED` 才生成 recipe；两者必须有唯一现代 recipe id 和非空输入/输出。`BLOCKED` 不计完成，必须精确指向 `item-registry/<id>`、`common-item-tag/<tag>` 或 recipe model owner；不得使用 pending、catalog 自身或泛化 owner。`RETIRED` 仅用于 Aluminum/Aluminium 等映射到同一现代 tag 的旧重复 alias。
- `MachineRecipeData` 直接消费 catalog，不维护第二份手写子集。Crystallization 写真实 `secondary_result`；materialization 写 1–5 个 counted `inputs`；transmutation 保留 XP/time。Anvil forging 与四条 production rending 同一 provider 双写，但不计入 223 的 synthesis 审计口径。
- 每次 datagen 同时写 1.20.1 `recipes/` + `result.item` + `forge:` common tags，以及 1.21.1 `recipe/` + `result.id` + `c:` common tags。根 `data/abyssalcraft/machine_recipe_catalog.json` 输出 `source=223`、四态计数和全部逐条记录（旧调用、现代 recipe id、owner/reason、输入输出）。
- 永久不变量拒绝：源调用数变化、ordinal 重复、四态求和不等于 223、可执行 recipe id 缺失/重复、非执行项无 owner、任意空输入/输出。2026-07-27 编辑器 Java 诊断、223 调用独立解析检查（109+46+68）和 diff check 通过；Gradle 在 javac 前仍被外部缓存的 `java.util.zip.ZipException: zip file is empty` 阻断，工作区内零字节 jar/zip 为 0，故本轮未声称 runData/build 完成。
- 单一 integrator 闭包固定为 **142 MIGRATED + 77 REPLACED + 4 RETIRED + 0 BLOCKED = 223**，可执行项 219。`scripts/audit_machine_catalog.ps1` 是不依赖 Gradle 的旧源解析器；它从仓库根或任一 `versions/*` 子项目目录都先向上定位根目录，再核 109/46/68 分类、25 次输出解析、48 条多输入、48 条双输出、alias、重复 classification key 与 schema 断言。
- 运行时不再依赖 `docs/...` 相对当前工作目录：脚本从权威旧 Java 源确定性写出 `data/abyssalcraft/catalog/legacy_machine_calls.txt`（保留原源行位置），`LegacyMachineRecipeSource` 优先从 classpath 读取该打包资源，仅在开发 classpath 缺资源时向上寻找仓库旧源。`legacy_machine_catalog.json` 是同一脚本生成并自验 stale 的 source-derived audit，不是 recipe datagen 产物，也不声称执行过 `runData`；真实双版本 recipe ID/JSON 仍由 `MachineRecipeData` 产生。
- 本轮 `:1.20.1-forge:compileJava` 在 javac 前被工作区外 Gradle 9 Kotlin DSL immutable workspace 修改错误阻断；未访问或修改该外部缓存，未声称 compile/runData 成功。可重复的工作区内验证为三种 cwd 的纯脚本 audit、永久 JSON parse/闭包核和限定 diff check。

#### 旧 OreDictionary 输出解析

- 旧 API 允许输出使用 OreDictionary 字符串；现代 recipe result 必须是具体 item，禁止运行时枚举 tag。`MachineOutputResolutionCatalog` 按插入顺序冻结全部 25 个旧输出 tag，`LegacyMachineRecipeSource` 仅在输出参数位置解析；输入参数仍保留现代 tag。
- 原版已有精确物品时固定原版代表：raw meat→beef，六类 ore→对应普通 ore，logs/planks/saplings/leaves→oak 系列，vines→vine，iron nugget→iron nugget，copper ingot→copper ingot。
- AC 当前注册了对应元素材料时优先 AC：aluminium/zinc/magnesium/calcium 的 ingot 语义固定为对应 crystal，nugget 语义固定为对应 crystal shard。铜/锡兼容内容现已注册 `crystal_copper` / `crystal_tin`、对应 shard 和 cluster；四个旧输出 tag 因此精确映射到同元素 crystal/shard，不再降级为铜锭或铁系占位。`blockCopper` / `blockTin` 输入仍分别保留 `c:storage_blocks/copper` / `c:storage_blocks/tin` tag，输出 cluster 直接解析为 AC 注册方块物品。
- 永久 audit 拒绝：显式映射目录不是 25 项、冻结源不再发生恰好 25 次输出解析、任一实际输出 tag 未被 catalog 覆盖、目标 item 不在运行时 registry、重建 catalog 后映射或顺序变化。25 次调用中 Aluminum/Aluminium 两组旧拼写 alias 各归一到同一现代 tag；copper ingot 仅作输入，仍保留 tag。`machine_recipe_catalog.json` 同时记录全局映射及每条 recipe 使用的 tag/item/reason；输出解析项计为 `REPLACED`，不再产生 `machine-recipe-model/output-tag/*` BLOCKED。

---

> **以下为 2026-07-21 历史试点记录，只读。** 其中“三机器共用三槽炉”“任意燃料”和 `ProcessingRecipe` 等描述已被 §0 取代。

## 1. 概述 / 目标

自定义合成机器子系统：炉式机器（输入 + 燃料 → 按配方产出），作为全工具链模板与回归基线。三机器：结晶器 crystallizer / 物质化器 materializer / 嬗变器 transmutator。

## 2. 范围

- 含：机器方块 + 方块实体（3 槽 input/fuel/output + 进度/燃烧计时）+ 菜单/屏幕 + 自定义配方类型/序列化器 + JEI 试点集成（PP-5：三机器分类 + 催化剂 + 燃料信息页，JEI 为可选依赖）。
- 不含：PE 势能驱动（后续里程碑，试点用燃料驱动）；创造栏分类（暂 `/give` 获取）；正式 JEI 全量分类（PJ-1，Stage J）。

## 3. 设计 / 架构

### 框架（PP-1，冻结，复用）
- `content/blockentity/base/MachineBlockEntity`：具体基类，`Container` + `MenuProvider`，3 槽（SLOT_INPUT=0 / FUEL=1 / OUTPUT=2），`ContainerData`（progress/maxProgress/burnTime/maxBurnTime），静态 ticker `serverTick(...)`，save/load 经 `ContainerCompat`/`BlockEntityCompat`。子类覆写 protected `serverTick()`。
- `content/menu/base/MachineMenu`：`AbstractContainerMenu`，炉式布局（input 56,17 / fuel 56,53 / output 116,35 / 箭头 79,34），`ContainerData`，`progress()`，`quickMoveStack`，客户端 ctor 忽略额外 buffer。**机器复用此类，不建子类**；各机器只注册自己的 `MenuType<MachineMenu>`。
- `content/recipe/base/ProcessingRecipe` + `platform/RecipeSerializerCompat.processing`：单输入配方（Ingredient input + ItemStack result + int time）。
- 注册模式（每机器一个 `Xxxs.java`）：BLOCK / ITEM(BlockItem) / BLOCK_ENTITY_TYPE / MENU(`MenuType<MachineMenu>`) / RECIPE_TYPE / RECIPE_SERIALIZER。

### 通用 BE 框架（PC-1，Stage C1）
> 把 PP-1 机器 BE 泛化为非机器 BE 也能复用的基类；PP-1 `MachineBlockEntity`/`MachineBlockEntities` **原样不动**（机器仍走菜单驱动炉式基类）。
- `content/blockentity/base/ACBlockEntity`：通用基类 extends `platform/BlockEntityCompat`（吸收 1.20↔1.21 save/load 分叉）+ `markUpdated()`（setChanged + `level.sendBlockUpdated` 推客户端同步）。非机器 AC BE 的根基类。
- `content/blockentity/base/DirectionalBlockEntity`（承 1.12.2 `TEDirectional`）：存 `Direction facing`，save/load "Facing"（3D 数据值），`getFacing`/`setFacing`(+markUpdated)。供 idol/spawner/portal anchor/sealing lock 等需存朝向的 BE 继承（现代块 FACING 一般走 blockstate，此类供 BE 级需求）。
- `content/blockentity/base/InventoryBlockEntity`（承 1.12.2 `ISingletonInventory`）：可复用 `Container`（`NonNullList` + `ContainerHelper`），ctor 传 size；**size=1 = 单例库存**（altar/pedestal 的 `getStoredItem`/`setStoredItem` 便捷），size>1 = 箱笼；save/load 经 `ContainerCompat`。机器不用此类（走 `MachineBlockEntity`）。
- `content/blockentity/base/TickingBlockEntity`（接口，承 1.12.2 `ITickable`）：`serverTick()` + 静态 `serverTicker()` 返回 `BlockEntityTicker`（instanceof 转发）。**泛化 3 机器 `getTicker → MachineBlockEntity::serverTick` 模式**：新 tick BE = implements 本接口 + 块 `getTicker` 返回 `TickingBlockEntity.serverTicker()`（server 侧守卫）。
- `registry/ModBlockEntities`：通用 BE 类型注册器（对齐 `ModRegistries`/`ModCreativeTabs` 的 registry/ 聚合先例）。注册 directional/inventory 两 block-less 示例类型（smoke test，`build(null)`，同 PP-1 MACHINE，证双端注册路径）；后续具体 BE 各自 register 子类型于此。接 `ModRegistries.ALL`。
- **业务零 `//?`**（版本分叉全在 `BlockEntityCompat`/`ContainerCompat`）；两节点 `compileJava`+`runServer` 抵 Done（forge 2.342s / neo 0.841s）、`BLOCK_ENTITY_TYPE` 冻结零错。tick 生命周期由 P2 机器运行期实证（同 ticker 模式）+ 本接口 compile 核。

### 自定义配方框架（PC-2，Stage C1）
> 5 类自定义配方 `RecipeType`+`RecipeSerializer` 的中央框架。PP-1 的 `ProcessingRecipe`（单输入→result+time）已被 3 试点机器复用注册 crystallization/materialization/transmutation（frozen）；PC-2 补 2 类真实形状 + 建 `registry/ModRecipes` 中央目录。**不动 frozen 机器文件**——3 试点类型的集中化 + 升级到完整形状归 C2a 回归。
- `registry/ModRecipes`：全 5 类 catalog。owns `anvil_forging`+`rending` 的 `RecipeType`（`RecipeType.simple`）+`RecipeSerializer`；alias 引用 `Crystallizers.CRYSTALLIZATION`/`Materializers.MATERIALIZATION`/`Transmutators.TRANSMUTATION`（3 试点仍机器自持）。接 `ModRegistries.ALL`（`RECIPE_TYPES`+`RECIPE_SERIALIZERS` 2 registrar）。
- `content/recipe/anvil/AnvilForgingRecipe`：仿 1.12.2 `api.recipe.AnvilForging` —— `input1`+`input2`(Ingredient) → `result` + `price`(int) + `forging_type`(str；DEFAULT/RITUAL_CHARM)。
- `content/recipe/rending/RendingRecipe`：仿 `api.rending.Rending` —— `name`(能量名) + `max_energy`(int) + `entity`(目标实体 id 串) → `result` essence + `dimension`(可选)。原 `Predicate<EntityLiving>` 数据化为 entity id（能量/谓词逻辑机器侧、延后）。
- `platform/DataRecipeCompat`：抽象 `Recipe<Container↔RecipeInput>` fork base（镜像 `RecipeCompat` 结构），吸收 1.21 Recipe 接口大改（Container→RecipeInput、RegistryAccess→HolderLookup.Provider、getId 移除）。**`matches`→stub(false)**：这些 data 配方不走容器匹配，由机器/物品 `getAllRecipesFor` 枚举 + 自定义匹配（匹配逻辑机器侧、延后）。业务配方类零 `//?`：只提供字段 + 公有访问器（供 platform 序列化器**跨包**读字段）+ getType/getSerializer。
- `platform/{AnvilForgingRecipeSerializer,RendingRecipeSerializer}`：forked 序列化器（`fromJson`/`fromNetwork`/`toNetwork`(1.20.1) ↔ `MapCodec codec()` + `StreamCodec streamCodec()`(1.21)）。javap 双 jar 核 `ByteBufCodecs.STRING_UTF8` + `StreamCodec.composite` 6 元重载（anvil/rending 各 5 字段 ≤6 OK）。
- 示例配方 `data/abyssalcraft/{recipe,recipes}/{anvil_forging,rending}_pilot.json`：双目录双格式（forge `recipes/` result 用 `"item"` / neo `recipe/` 用 `"id"`；镜像既有 pilot），验证序列化器两端解析。
- **验证**：两节点 `compileJava` 绿；forge `runServer` `Loaded 12 recipes`（可复现）+ `/reload` 零 parse 错 + 干净 stop；neo `runServer` `Loaded 1295 recipes`（anvil/rending 两端零 parse 错加载）。**forge 12 vs neo 1295 = vanilla 配方在 forge-dev/neo-dev 数据包可用性的可复现差异（与 PC-2 无关；与旧 CR-18 记「forge 1293」不符，疑其当时不准）**。
- **延 C2a 回归**：3 试点类型集中进 `ModRecipes` + 升级到 1.12.2 完整形状（crystallization 2 输出+xp、materialization ≤5 crystal 输入、transmutation+xp）；机器 BE 改引 `ModRecipes.*`（`ModRecipes` 已 alias 便于迁移）。

### 机器（content/machine/<name>/ + client/screen/machine/<name>/）
- `<Name>Block extends platform/InteractiveBlockCompat implements EntityBlock`：`onUse` 开菜单（`MenuCompat.open`）、`newBlockEntity`、`getTicker`（type 判定 + `MachineBlockEntity::serverTick`）。
- `<Name>BlockEntity extends MachineBlockEntity`：ctor 设 maxProgress；覆写 `serverTick`（燃料 → 配方 → 产出）、`createMenu`、`getDisplayName`。配方查找经 `RecipeCompat.findResult`（→ result ItemStack）或 `RecipeLookup.findProcessing`（→ recipe，需 `time()`）。
- `<Name>Screen extends AbstractContainerScreen<MachineMenu>`：`ACRef.vanilla` furnace.png，`renderBg`（blit 背景 + 进度箭头），`render` → `ClientScreenCompat.background`。
- 屏幕注册：`client/ACClientSetup.registerScreens()` 加 `ClientScreenCompat.queue(MENU, Screen::new)`；主类经 `SideExecutor.runWhenClient` 挂 `ClientScreenCompat.attach`（无需逐机器改主类）。
- 接线：`registry/ModRegistries.ALL` 追加该机器 6 个 registrar。

### 研究桌 + 顺序酿造台（PC-8，Stage C2a）
> 复用 PC-1 BE 框架 + PC-3 菜单 + 上方机器模板；PP-1 `MachineBlockEntity` + 3 试点机器**原样不动**。
- **研究桌**（`content/machine/researchtable/`）：`ResearchTableBlockEntity extends ACBlockEntity`（空壳 marker + `MenuProvider`——1.12.2 `TileEntityResearchTable` 即空、无库存；**知识/研究 hook 延 S-B**）；`ResearchTableBlock extends InteractiveBlockCompat`（onUse 开菜单，无 ticker）；`ResearchTableMenu extends ContainerMenuBase`（contentSlots=0，仅玩家背包——1.12.2 container 亦仅背包）；`ResearchTableScreen`（vanilla `crafting_table.png` 占位 bg，界面可开）。注册器 `ResearchTables`（block/item/BE/menu，无 RecipeType）。
- **顺序酿造台**（`content/machine/brewing/`）：`BrewingStandBlockEntity extends InventoryBlockEntity(size=8) implements MenuProvider, TickingBlockEntity`——槽 0/1/2=药水、3=材料、4=燃料(blaze powder→20 fuel)、5/6/7=传出；`brewTime`(400)/`fuel` 经 `ContainerData`；vanilla 酿造经 `platform/PotionBrewingCompat`；**顺序链**：`serverTick` 每 20t 把传出槽(5/6/7)推入 `facing()` 邻居台的输入槽(0/1/2)、brew 完把 0/1/2 移到 5/6/7 → 药水沿链流动。`BrewingStandBlock extends InteractiveBlockCompat`（`FACING`=输出方向 blockstate + getTicker `TickingBlockEntity.serverTicker()` + 开菜单）；`BrewingStandMenu extends ContainerMenuBase`(8 槽复刻 1.12.2 槽位 + `addDataSlots`)；`BrewingStandScreen`（vanilla `brewing_stand.png` bg）。注册器 `BrewingStands`。
- **`platform/PotionBrewingCompat`**（新）：`isIngredient/hasMix/mix(level, ...)`——1.20.1 静态 `PotionBrewing.*`、1.21 `level.potionBrewing()` 实例（`//? if <1.21` 门控 import；编译双端核）。
- **验证**（两节点）：`compileJava`+`runServer`（BLOCK/BE/MENU 冻结零错、抵 Done forge 2.330s/neo 1.037s，unique 端口避并发撞车）+ forge `runClient`（2 机器模型烘焙零告警、屏幕注册、进世界 JEI 1896、干净退出）。**待人工目视**：右键开界面 + 酿造顺序链。**延 PK**：faithful 贴图/模型、药水 GUI 泡泡/燃料进度、槽位校验(potion/ingredient/fuel)。

### JEI 试点集成（PP-5，`integration/jei/`）
- `ACJEIPlugin`（`@JeiPlugin implements IModPlugin`，JEI 注解扫描器自动发现、**不入 mods.toml**）：三机器各注册一个 JEI 分类 + 一个方块催化剂（`addRecipeCatalyst`）；`registerRecipes` 经 `RecipeCompat.allOfType` 收集各机器 MC 配方（JEI 于 world-join 时调，`level` 空则跳过），另加 pilot_fuel 信息页。
- `MachineRecipeCategory implements IRecipeCategory<ProcessingRecipe>`：单个泛型分类类，三机器各实例化一次（仅差 JEI RecipeType + 标题 + 图标）；用 JEI 内置 drawable（`getSlotDrawable`/`createAnimatedRecipeArrow`/`getRecipeFlameFilled`）+ `ProcessingRecipe` 的 fork-free accessor（`input()`/`result()`），免贴图、零分叉。
- 依赖：`build.gradle.kts` 按 loader 加 `modCompileOnly`(common + loader API) + `modLocalRuntime`(全 jar，**dev 专属、不入发布**)；版本在各 `gradle.properties` 的 `vers.deps.jei`（forge 15.20.0.134 / neo 19.39.0.369）。
- **缺 JEI 不崩**：无其它代码引用 `integration.jei`、无 `mezz.jei` import 泄漏、非 mods.toml 硬依赖（grep 核）→ 无 JEI 时 `@JeiPlugin` 类根本不加载。

### 机器配方 datagen（PC-9，Stage C2b）
> `data/gen/MachineRecipeData`（`DataProvider`，接 `ACDataGenerators`，server pack）datagen 生成 3 合成机器示例配方 = crystallization 7 + transmutation 11 + materialization 9 = **27 条**（`ProcessingRecipe` 形状 input→result+time）。忠实 1.12.2 `common/AbyssalCrafting` 的 `addCrystallization`/`addTransmutation`/`addMaterialization` 表，裁剪到现存物品（crystallization 取单输出子集、materialization 取单晶体输入子集；多输出/多输入待 `ProcessingRecipe` 升级到完整形状=C2a 回归）。id 一手核对 PB-1 crystal/material + PB-2 anti-food + PB-3 darkstone + vanilla。
- **业务零 `//?`**：`DataProvider.run(CachedOutput)` + `DataProvider.saveStable(CachedOutput, JsonElement, Path)` javap 双 jar 同签名（1.21 另有 Codec 5-arg 重载，靠参数数量消歧、不冲突）。
- **★关键坑（自定义配方 datagen 跨 loader clobber）**：自定义配方有**双分叉**——目录 1.20.1 `data/<ns>/recipes/`（复数）↔ 1.21 `recipe/`（单数）+ 结果物 key 1.20.1 `"item"` ↔ 1.21 `"id"`（forge `ShapedRecipe.itemStackFromJson` vs neo `ItemStack.CODEC`）。而两 loader `runData` 共享同一 `--output src/main/generated`，datagen 的 HashCache **会删除本次未生成的旧文件** → 若按 loader 只生成自己那一个目录（如靠 `DataDirs.RECIPE` fork），**neo runData 会清掉 forge 上次生成的 `recipes/`**（实测）。**正解**：provider 每次 runData **无条件生成两目录两格式**（`recipes/`+`"item"` 供 1.20.1、`recipe/`+`"id"` 供 1.21），各 loader 运行期只读自己的目录、互不干扰、HashCache 不互删。因此 provider 反而 fork-free（两格式都硬写，无 `//?`）；曾建的 `platform/RecipeDataCompat`(result key fork) 遂删、platform 净零。
- **验证**：两节点 `compileJava`+`runData`（各生成 27+27、`src/main/generated` 两目录并存无 clobber）绿；**neo `runServer` `Loaded 1322 recipes`（= 基线 1295 + 27）零 parse 错、干净 stop → 27 条两端格式全加载可用**（recipe item id 存在性由 runServer 的 RecipeManager 校验，不由 runData）。
- **forge-dev runServer 限制（非缺陷）**：forge `runServer` 仍 `Loaded 12`——27 条已在 forge `build/resources/main/data/abyssalcraft/recipes/`(32 files)→入 jar/生产环境正常，但 **forge-dev runServer 只挂 `src/main/resources` 不挂 `src/main/generated`**（PC-2 已记的 forge-dev 只加载 12 配方的可复现现象同源）→ datagen 产出的 recipe 在 forge-dev 运行期不加载。datagen 正确性以 neo（1322）+ forge build/resources(32) 双证；forge-dev 挂载 gap 属 PA-4 基建，待查。

## 4. 子系统内契约

- 方块/物品 id：`abyssalcraft:{crystallizer,materializer,transmutator}`。
- 配方类型 id（5，PC-2）：`abyssalcraft:{crystallization,materialization,transmutation}`（3 试点，绑 `ProcessingRecipe`、机器自持）+ `abyssalcraft:{anvil_forging,rending}`（PC-2 真实形状，`registry/ModRecipes`）。
- i18n：`block.abyssalcraft.<machine>` + `container.abyssalcraft.<machine>`。
- 菜单同步：`ContainerData` 4 槽（DATA_PROGRESS / MAX_PROGRESS / BURN / MAX_BURN）。

## 5. 跨版本 / 加载器要点（javap 双 jar 核）

- 交互 `use`(1.20,+InteractionHand) 与 `useWithoutItem`(1.21,protected)：`platform/InteractiveBlockCompat`（abstract `onUse`）。
- 开菜单 `NetworkHooks.openScreen`(forge) 与 `player.openMenu`(neo)：`MenuCompat.open`。
- 配方查找 `getRecipeFor` Container/`Optional<T>`(1.20) 与 RecipeInput/`Optional<RecipeHolder<T>>`(1.21)：`RecipeCompat.findResult` / `RecipeLookup.findProcessing`（forge 传 Container；neo 用 `SingleRecipeInput` + `RecipeHolder::value`）。
- 配方序列化 `fromJson`/`toNetwork`(1.20) 与 `MapCodec`/`StreamCodec`(1.21)：`RecipeSerializerCompat`。
- 屏幕注册 `FMLClientSetupEvent`+`MenuScreens.register`(forge) 与 `RegisterMenuScreensEvent`(neo)：`ClientScreenCompat`；`renderBackground` 1.21 加 mouse/pt → `ClientScreenCompat.background`。
- **同签名无需分叉**：`EntityBlock.newBlockEntity`/`getTicker`、`blit`、`AbstractContainerScreen`(renderBg/render/init/renderLabels)、`MenuScreens.register`、`InteractionResult.sidedSuccess`、vanilla `AbstractFurnaceBlockEntity.getFuel`/`isFuel` → 机器块/BE/屏幕业务代码零 `//?`。

### 铁砧锻造闭包（2026-07-31）

- 1.12.2 `AbyssalCrafting#addForgings` 冻结为 **74 项**：8 个 charm family × range/power/duration = 24，五类 PE 方块的 10 条跨级升级 = 50。
- `LegacyAnvilForgingCatalog` 与 `AnvilForgingRecipeData` 将 74 项全部生成到 `recipes/anvil/` 与 `recipe/anvil/`，审计为 `migrated=74 retired=0 blocked=0 files=148`。
- 恢复五级独立 Sacrificial Altar（不是 Ritual Altar）：生命力收集、1–5 目标、5000–15000 PE、1200–400 tick 冷却、单槽 PE 输出与持久化；旧普通合成重新产出 `sacrificialaltar`。
- `GameHooksCompat` 恢复双加载器 `AnvilUpdateEvent`：左右 Ingredient 精确匹配，输出配方结果，材料消耗 1，等级价格取 recipe `price`。双端 live RecipeManager 输出 `RR_ANVIL_FORGING_RUNTIME_OK recipes=74 charm=ok peUpgrade=ok altarUpgrade=ok reversedRejected=true`。
- **JEI API 双版本同签名（javap 双 jar 核）**：JEI 15(forge 1.20.1) 与 19(neo 1.21.1) 我所用的 `IModPlugin` / `IRecipeCategory`(getRecipeType/getTitle/getWidth/getHeight/getIcon/setRecipe/draw) / `RecipeType.create` / `IGuiHelper` / `IRecipeLayoutBuilder` / `IRecipeSlotBuilder`(addSlot/setBackground/addIngredients/addItemStack) / `IRecipeCatalystRegistration.addRecipeCatalyst` / `RecipeIngredientRole` 逐一同签名（19 仅多我不覆写的 default 方法）→ `integration/jei` 业务零 `//?`。唯一版本分叉 = 配方枚举 `RecipeManager.getAllRecipesFor`（1.20 `List<T>` / 1.21 `List<RecipeHolder<T>>`），落 `RecipeCompat.allOfType`。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **配方 JSON 双写**：数据包目录 1.20.1 `recipes/`（复数）、1.21.1 `recipe/`（单数）；且 result 字段 1.20 用 `{"item":...}`、1.21 用 `{"id":...}`（ItemStack.CODEC）。→ 同一配方在两目录各写一份（input `{"item":...}` 两版通用；含 `"time"`）。免双写的 proper datagen 留 TP.6。
- **注册器自引用**：menu/serializer 的 lambda 引用自身字段必须**类限定** `Xxxs.FIELD.get()`；简单名 → javac "self-reference in initializer"，且该错误会级联成别处 "cannot find symbol" 假象。（PP-4 遗留 bug 即此，已修 1 行。）
- 燃料：试点用固定 `FUEL_BURN`（任意非空燃料燃烧固定 tick），未用 vanilla `getFuel`（虽两版同签名可用）；各机器自定（transmutator 用 PILOT_FUEL/coal 白名单）。
- 机器块非目录性（`cube_all` + 原版贴图，免二进制 PNG）：crystallizer=amethyst_block，materializer=blast_furnace_top，transmutator=smithing_table_top（三贴图 1.20.1/1.21.1 皆存在）。
- **客户端资产不能只靠 runServer 验**：blockstate/model 是客户端资源，runServer 不加载它们 → 缺 transmutator 三资产（blockstate + block model + item model）时 runServer 全绿、但只有 runClient 才暴露紫黑缺失模型方块 + 无图标物品。PP-4 初版"runClient 可视留 P3"只跑了 runServer，漏检 → 结论：每机器竖切**必须**两节点 runClient 核模型烘焙零告警才算竖切成立。
- `pilot_crystal`/`pilot_fuel`（PP-1 物品）原无 item model → runClient 报 missing-model WARN（非致命）；PP-2 补了引用原版 amethyst_shard/blaze_powder 的 model。

## 7. 验证 / DoD

### PP-2 Crystallizer（2026-07-21，两节点，判据 = 日志/退出码 + disk + jar 字节）
- 两节点 `compileJava --rerun-tasks` 绿；两节点 `build` 绿，remap jar 含 Crystallizers/CrystallizerBlock/CrystallizerScreen + blockstate/model/两配方（ZipFile 核）。
- 两节点 `runServer`：mod 加载、注册表冻结无错、配方解析无错（forge Loaded 9 / neo Loaded 1292 recipes）、Done（forge 2.285s / neo 0.854s）、stop 干净。
- 两节点 `runClient`：crystallizer 方块/物品模型 + pilot 模型烘焙**零错**、屏幕注册无错、抵标题屏、干净退出。
- **未机核（如实标注）**：放料 → 产结晶 → GUI 进度/箭头 属客户端交互，需人工目视（`/give abyssalcraft:crystallizer`，input=diamond，fuel=任意物品）。

### PP-4 Transmutator 补完 + PP-3 Materializer 复核（2026-07-21 接手，两节点）
- **PP-4 症结**：Agent 3（CR-8）交付了 transmutator 代码/配方/lang 且已接线（ModRegistries.ALL + ACClientSetup），但**漏了 3 个客户端资产**（blockstate + block model + item model），且自述"runClient 可视留 P3"仅跑 runServer（服务端不加载客户端模型）→ 缺资产未被发现。本次补齐三资产（镜像 materializer，texture=smithing_table_top）。
- 两节点 `build` 绿；remap jar 含 transmutator 三资产 + 三 class（ZipFile 核）。
- 两节点 `runClient`：transmutator 方块/物品模型烘焙**零告警**（贴图集干净、无 `Unable to load model`/无 missing texture/无异常），materializer/crystallizer/pilot 亦零告警，抵稳定态后干净 `Stopping!` + `BUILD SUCCESSFUL`。→ Agent 3 推迟的 runClient 可视验证补上。
- **PP-3 Materializer**：代码与 Crystallizer 逐行一致（Compare-Object 仅 javadoc/常量/命名差异），资产/配方齐全正确；上述 runClient 一并核其模型零告警 → 竖切成立。
- **未机核（如实标注）**：放料 → 产出 → GUI 进度 属客户端交互，需人工目视（`/give abyssalcraft:transmutator`，input=gravel，fuel=任意物品 → 出 flint；materializer input=cobblestone）。

### PP-5 JEI 试点（2026-07-21，两节点，判据 = 日志/退出码 + disk + jar 字节）
- 两节点 `compileJava --rerun-tasks` 绿（同一 `integration/jei` 业务同时过 JEI 15/19 API）；两节点 `build` 绿，remap jar 含 `integration/jei/{ACJEIPlugin,ACJEIPlugin$Entry,MachineRecipeCategory}.class`（ZipFile 核）。
- 两节点 `runClient`：JEI 加载（Mod List：forge `jei 15.20.0.134` / neo `Just Enough Items 19.39.0.369`）、`PluginCaller: Sending ConfigManager` → `@JeiPlugin` 实例化（静态初始化 `RecipeType.create` 等零错）、`jei:textures/atlas/gui.png-atlas` 建、抵标题屏 `Stopping!` 干净退出，**JEI/integration 零异常**。
- 缺 JEI 不崩：grep 核无外部引用 `integration.jei` / 无 `mezz.jei` import 泄漏 / 两 mods.toml 无 jei → 设计保证（`@JeiPlugin` 仅 JEI 注解扫描器加载）。
- **未机核（如实标注）**：JEI 于**进入世界**时才注册分类/配方/催化剂（title 屏只跑到 onConfigManagerAvailable）→ 三分类/配方/催化剂的实际展示 + 点击催化剂跳转需人工进世界开 JEI 目视。

## 8. 遗留 / TODO

- ~~PP-4 Transmutator 收尾~~ ✅ 已完成（CR-9）。~~PP-5 JEI 试点~~ ✅ 已完成（CR-10：两节点 build+runClient 加载/实例化零错；进世界展示待目视）。
- 机器创造栏分类（三机器 + pilot 物品）待 PK。
- 免双写的 datagen 配方待 TP.6。
- 正式 JEI 全量分类（9 类）+ 配方传输/点击区待 PJ-1（Stage J）——复用 PP-5 试点模板。

## 修订日志

- 2026-07-21（PP-5）：JEI 试点交付（`integration/jei/{ACJEIPlugin,MachineRecipeCategory}`——三机器分类 + 催化剂 + 燃料信息页，零 `//?`，唯一版本分叉落 `RecipeCompat.allOfType`）；javap 双 jar 核 JEI 15/19 所用 API 同签名；`build.gradle.kts`+两 `gradle.properties` 加 JEI dev 依赖；两节点 build+runClient 加载/插件实例化零错。更新 状态/§2/§3/§5/§7/§8。见平行表 CR-10。
- 2026-07-21（接手）：补 PP-4 Transmutator 3 个缺失客户端资产（blockstate/block model/item model，texture=smithing_table_top）；两节点 `build`+`runClient` 核三机器模型烘焙零告警、干净退出；复核 PP-3 Materializer 竖切成立。更新 状态/§6/§7/§8。见平行表 CR-9。
- 2026-07-21：PP-2 Crystallizer 交付并双端验证；补 pilot 物品模型；记录 PP-4 遗留。首次建档（含 PP-1 框架 + PP-3 Materializer 现状引用）。
